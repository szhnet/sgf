package io.jpower.sgf.common.hotswap;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.jpower.sgf.utils.JavaUtils;
import javassist.bytecode.ClassFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HotSwap class file base on {@link Instrumentation}
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class HotSwap {

    private static final Logger log = LoggerFactory.getLogger(HotSwap.class);

    private static final String CLASS_FILE = "class";

    private static final String ZIP_FILE = "zip";

    private static final String JAR_FILE = "jar";

    private final ClassLoader classLoader;

    private final Path newClassPath;

    /**
     * @param newClassFilePath the path to the file to store new class files
     */
    public HotSwap(Path newClassFilePath) {
        this(HotSwap.class.getClassLoader(), newClassFilePath);
    }

    /**
     * @param classLoader
     * @param newClassFilePath the path to the file to store new class files
     */
    public HotSwap(ClassLoader classLoader, Path newClassFilePath) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
        try {
            HotSwapHelper.checkDir(newClassFilePath, "newClassFilePath", log);
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        this.newClassPath = newClassFilePath;
    }

    public void swap(Path file) {
        Map<String, byte[]> classBytes = scan(file);

        List<ClassDefinition> classDefs = new ArrayList<>();
        Map<String, byte[]> newClasses = new HashMap<>();
        for (Map.Entry<String, byte[]> e : classBytes.entrySet()) {
            String className = e.getKey();
            byte[] bytes = e.getValue();
            boolean newClass = false;
            Class<?> clazz = null;
            try {
                clazz = classLoader.loadClass(className);
            } catch (ClassNotFoundException e1) {
                newClass = true;
            }
            if (newClass) {
                newClasses.put(className, bytes);
            } else {
                classDefs.add(new ClassDefinition(clazz, bytes));
            }
        }

        if (JavaUtils.bool(newClasses)) {
            createNewClass(newClasses); // create new class files
        }

        if (JavaUtils.bool(classDefs)) {
            redefineClass(classDefs); // redefine Classes
        }
    }

    /**
     * Create new class file which is not exists current classpath
     *
     * @param newClasses
     */
    private void createNewClass(Map<String, byte[]> newClasses) {
        for (Map.Entry<String, byte[]> en : newClasses.entrySet()) {
            String className = en.getKey();
            byte[] bytes = en.getValue();

            Path classFile = getNewClassFilePath(className);
            String sname = getSimpleClassName(className);
            Path tempFile = null;
            try {
                Path classFileDir = classFile.getParent();
                if (Files.notExists(classFileDir)) {
                    Files.createDirectories(classFileDir);
                }

                tempFile = Files.createTempFile(sname, null);
                Files.write(tempFile, bytes);

                Files.move(tempFile, classFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                if (log.isDebugEnabled()) {
                    log.debug("Create new class file: {}", classFile);
                }
            } catch (IOException e) {
                throw JavaUtils.sneakyThrow(e);
            } finally {
                delete(tempFile);
            }
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

    private Path getNewClassFilePath(String className) {
        String fileStr = className.replace(".", newClassPath.getFileSystem().getSeparator())
                + "." + CLASS_FILE;
        return newClassPath.resolve(fileStr);
    }

    private String getSimpleClassName(String className) {
        int index = className.lastIndexOf('.');
        if (index < 0) {
            return className;
        } else {
            return className.substring(index + 1);
        }
    }

    private void redefineClass(List<ClassDefinition> classDefs) {
        try {
            InstrumentationSupport.instrumentation().redefineClasses(
                    classDefs.toArray(new ClassDefinition[classDefs.size()]));
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        if (log.isDebugEnabled()) {
            for (ClassDefinition cdef : classDefs) {
                log.debug("Redefine class: {}", cdef.getDefinitionClass());
            }
        }
    }

    /**
     * @return key: className value: bytes of class file
     */
    private Map<String, byte[]> scan(Path file) {
        Map<String, byte[]> classBytes = new HashMap<>();
        scan(file, classBytes);
        return classBytes;
    }

    private void scan(Path file, Map<String, byte[]> classBytes) {
        if (Files.isDirectory(file)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(file)) {
                for (Path f : ds) {
                    scan(f, classBytes);
                }
            } catch (IOException e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            scanFile(file, classBytes);
        }
    }

    private void scanFile(Path file, Map<String, byte[]> classBytes) {
        String fileName = file.getFileName().toString();
        int idx = fileName.lastIndexOf('.');
        if (idx == -1) {
            return;
        }
        String suffix = fileName.substring(idx + 1);
        if (suffix.equals(CLASS_FILE)) {
            scanClassFile(file, classBytes);
        } else if (suffix.equals(ZIP_FILE)) {
            scanZipFile(file, classBytes);
        } else if (suffix.equals(JAR_FILE)) {
            scanJarFile(file, classBytes);
        }
    }

    private void scanClassFile(Path file, Map<String, byte[]> classBytes) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(file);
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        String className = getClassName(bytes);
        classBytes.put(className, bytes);
    }

    private void scanZipFile(Path file, Map<String, byte[]> classBytes) {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            for (Enumeration<? extends ZipEntry> enu = zip.entries(); enu.hasMoreElements(); ) {
                ZipEntry zipEntry = enu.nextElement();
                if (zipEntry.isDirectory()) {
                    continue;
                }

                String entryName = zipEntry.getName();
                if (!isClassFile(entryName)) {
                    continue;
                }
                byte[] bytes;
                try (InputStream in = zip.getInputStream(zipEntry)) {
                    bytes = readAllBytes(in, (int) zipEntry.getSize());
                } catch (Exception e) {
                    throw new IOException("Error while reading bytes. ZipFile=" + file.toAbsolutePath() + ", ZipEntry=" + entryName, e);
                }
                String className = getClassName(bytes);
                classBytes.put(className, bytes);
            }
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    private void scanJarFile(Path file, Map<String, byte[]> classBytes) {
        try (JarFile jar = new JarFile(file.toFile())) {
            for (Enumeration<? extends JarEntry> enu = jar.entries(); enu.hasMoreElements(); ) {
                JarEntry jarEntry = enu.nextElement();
                if (jarEntry.isDirectory()) {
                    continue;
                }

                String entryName = jarEntry.getName();
                String className = getClassNameFromEntryName(entryName);
                if (className == null) {
                    continue;
                }
                byte[] bytes;
                try (InputStream in = jar.getInputStream(jarEntry)) {
                    bytes = readAllBytes(in, (int) jarEntry.getSize());
                } catch (Exception e) {
                    throw new IOException("Error while reading bytes. JarFile=" + file.toAbsolutePath() + ", JarEntry=" + entryName, e);
                }
                classBytes.put(className, bytes);
            }
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    private static byte[] readAllBytes(InputStream in, int size) throws IOException {
        byte[] bytes = new byte[size];
        int rsize = 0;
        while (rsize < size) {
            int len = in.read(bytes, rsize, size - rsize);
            if (len == -1) {
                break;
            }
            rsize += len;
        }
        if (rsize != size) {
            throw new IOException("read size error. expectSize=" + size + ", actualSize=" + rsize);
        }

        return bytes;
    }

    private static String getClassName(byte[] bytes) {
        return getClassName(new ByteArrayInputStream(bytes));
    }

    private static String getClassName(InputStream ins) {
        ClassFile cf;
        try (DataInputStream in = new DataInputStream(ins)) {
            cf = new ClassFile(in);
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        return cf.getName();
    }

    private boolean isClassFile(String entryName) {
        int idx = entryName.lastIndexOf('.');
        if (idx == -1 || idx == 0) {
            return false;
        }
        return entryName.substring(idx + 1).equals(CLASS_FILE);
    }

    /**
     * Return the entry's class name
     *
     * @param entryName
     * @return Return class name of the entry or {@code null} if the entry is not class file
     */
    private String getClassNameFromEntryName(String entryName) {
        int idx = entryName.lastIndexOf('.');
        if (idx == -1 || idx == 0) {
            return null;
        }
        if (!entryName.substring(idx + 1).equals(CLASS_FILE)) {
            return null;
        }
        return entryName.substring(0, idx).replace('/', '.');
    }

}
