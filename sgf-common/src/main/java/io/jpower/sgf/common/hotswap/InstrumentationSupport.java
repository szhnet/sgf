package io.jpower.sgf.common.hotswap;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import io.jpower.sgf.utils.JavaUtils;
import javassist.ClassPool;
import javassist.CtClass;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class InstrumentationSupport {

    /**
     * instrumentation
     */
    private static volatile Instrumentation instrumentation;

    /**
     * Instrumentation permain
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
    }

    /**
     * Instrumentation agentmain
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        // The method is called by Attach Listener Thread
        instrumentation = inst;
    }

    public static Instrumentation instrumentation() {
        if (instrumentation == null) {
            synchronized (Instrumentation.class) {
                if (instrumentation == null) {
                    ensureAgentLoaded();
                }
            }
        }
        return instrumentation;
    }

    /**
     * 在运行之后，使用agentmain的方式来获取Instrumentation实例
     */
    private static void ensureAgentLoaded() {
        Path agentJarFile = null;

        try {
            // agent jar
            agentJarFile = createAgentJarFile();
            // attach
            Class<?> vmClass = loadVirtualMachineClass();
            Method attach = vmClass.getMethod("attach", String.class);
            Method loadAgent = vmClass.getMethod("loadAgent", String.class);
            Method detach = vmClass.getMethod("detach");

            boolean attached = false;
            Object vm = null;
            try {
                vm = attach.invoke(null, pid());
                attached = true;
                loadAgent.invoke(vm, agentJarFile.toAbsolutePath().toString()); // 这里会等待agentmain结束才返回
            } finally {
                if (attached) {
                    detach.invoke(vm);
                }
            }
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        } finally {
            delete(agentJarFile);
        }
    }

    private static void delete(Path f) {
        try {
            if (f != null) {
                Files.deleteIfExists(f);
            }
        } catch (IOException ignored) {
        }
    }

    private static Path createAgentJarFile() throws Exception {
        Path jarFile = Files.createTempFile("agent", ".jar");

        String className = InstrumentationSupport.class.getName();

        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mainAttributes.put(new Attributes.Name("Agent-Class"), className);
        mainAttributes.put(new Attributes.Name("Can-Retransform-Classes"), "true");
        mainAttributes.put(new Attributes.Name("Can-Redefine-Classes"), "true");

        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            // add the agent .class into the .jar
            JarEntry agent = new JarEntry(className.replace('.', '/') + ".class");
            jos.putNextEntry(agent);

            // dump the class bytecode into the entry
            ClassPool pool = new ClassPool();
            pool.appendSystemPath();
            CtClass ctClass = pool.get(className);
            jos.write(ctClass.toBytecode());
            jos.closeEntry();
        }

        return jarFile;
    }

    private static Class<?> loadVirtualMachineClass() throws Exception {
        String name = "com.sun.tools.attach.VirtualMachine";

        Class<?> vmClass = null;
        try {
            // JDK 9 ?
            vmClass = ClassLoader.getSystemClassLoader().loadClass(name);
        } catch (Exception ignored) {

        }

        if (vmClass == null) {
            // JDK 8
            Path javaHomeDir = Paths.get(System.getProperty("java.home"));
            Path subPath = Paths.get("lib", "tools.jar");
            Path toolsFile = javaHomeDir.resolve(subPath); // jdk/jre/lib/tools.jar
            if (!Files.exists(toolsFile)) {
                toolsFile = javaHomeDir.getParent().resolve(subPath); // jdk/lib/tools.jar
            }
            if (!Files.exists(toolsFile)) {
                throw new IOException("tools.jar not found");
            }
            URL url = toolsFile.normalize().toUri().toURL();
            ClassLoader classLoader = new URLClassLoader(new URL[]{url});
            vmClass = classLoader.loadClass(name);
        }

        return vmClass;
    }

    private static String pid() {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        int idx = pid.indexOf('@');
        if (idx >= 0) {
            pid = pid.substring(0, idx);
        }
        return pid;
    }

}
