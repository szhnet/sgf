package io.jpower.sgf.common.compile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import io.jpower.sgf.utils.JavaUtils;

/**
 * 可以在运行时编译java代码
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class DynamicJavaCompiler {

    private final ClassLoader parentClassLoader;

    private final String classPath;

    private final String lineSeparator = System.getProperty("line.separator");

    public DynamicJavaCompiler() {
        this.parentClassLoader = DynamicJavaCompiler.class.getClassLoader();
        this.classPath = getClassPath(this.parentClassLoader);
    }

    private String getClassPath(ClassLoader classLoader) {
        StringBuilder sb = new StringBuilder();
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) this.parentClassLoader;
            for (URL url : urlClassLoader.getURLs()) {
                // System.out.println(url);
                sb.append(fixUrlPath(url.getFile())).append(File.pathSeparator);
            }
        }
        return sb.toString();
    }

    /**
     * 在resin下会有叹号
     *
     * @param path
     * @return
     */
    private Object fixUrlPath(String path) {
        int idx = path.indexOf('!');
        if (idx == -1) {
            return path;
        } else {
            return path.substring(0, idx);
        }
    }

    public Class<?> compile(String fullClassName, String code) {
        MemClassFileManager fileManager = null;
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            DiagnosticCollector<JavaFileObject> diagListener = new DiagnosticCollector<>();
            fileManager = new MemClassFileManager(
                    compiler.getStandardFileManager(diagListener, null, Charset.forName("UTF-8")));

            StringSourceJavaObject sourceObject = new StringSourceJavaObject(fullClassName, code);

            // source
            Iterable<? extends JavaFileObject> fileObjects = Collections
                    .singletonList(sourceObject);
            // 设置编译选项
            List<String> opts = Arrays.asList("-encoding", "UTF-8", "-cp", this.classPath);

            // 编译
            CompilationTask task = compiler.getTask(null, fileManager, diagListener, opts, null,
                    fileObjects);
            boolean result = task.call();
            if (result) {
                Map<String, byte[]> classBytes = fileManager.pollClassBytes(); // 拿到编译后的字节
                fileManager.flush();
                DynaClassLoader dynaClassLoader = AccessController.doPrivileged(new PrivilegedAction<DynaClassLoader>() {
                    @Override
                    public DynaClassLoader run() {
                        return new DynaClassLoader(parentClassLoader,
                                classBytes);
                    }
                });

                Class<?> clazz = null;
                try {
                    clazz = dynaClassLoader.loadClass(fullClassName);
                } catch (ClassNotFoundException e) {
                    throw JavaUtils.sneakyThrow(e);
                }

                return clazz;
            } else {
                String diagInfo = collectDiagnostic(diagListener.getDiagnostics());
                throw new RuntimeException("compile error" + lineSeparator + diagInfo);
            }
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        } finally {
            if (fileManager != null) {
                try {
                    fileManager.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String collectDiagnostic(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        if (diagnostics == null || diagnostics.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
            sb.append("Message: ").append(d.getMessage(Locale.getDefault())).append(lineSeparator);
            sb.append("Source: ").append(d.getSource()).append(lineSeparator);
            sb.append("LineNumber: ").append(d.getLineNumber()).append(lineSeparator);
            sb.append("ColumnNumber: ").append(d.getColumnNumber()).append(lineSeparator);
            sb.append(lineSeparator);
        }
        return sb.toString();
    }

    /**
     * 将输出流保存到内存中，不生成文件
     */
    private static class MemClassFileObject extends SimpleJavaFileObject {

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        MemClassFileObject(String name, JavaFileObject.Kind kind) {
            super(URI.create("string:///" + name.replace('.', '/') + kind.extension),
                    kind);
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return bos;
        }

        byte[] getBytes() {
            return bos.toByteArray();
        }

    }

    /**
     * 配合MemClassFileObject的JavaFileManager
     */
    private static class MemClassFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        /**
         * 这里需要一个map，因为有可能会编译出多个class，比如代码中有内部类之类的
         */
        private Map<String, MemClassFileObject> fileMap = new HashMap<>();

        MemClassFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
                                                   FileObject sibling) throws IOException {
            MemClassFileObject f = new MemClassFileObject(className, kind);
            fileMap.put(className, f);
            return f;
        }

        /**
         * 获得编译后的字节。key:类的全名 value:class的字节
         *
         * @return
         */
        Map<String, byte[]> pollClassBytes() {
            if (fileMap.isEmpty()) {
                return Collections.emptyMap();
            } else {
                Map<String, byte[]> m = new HashMap<>();
                for (Map.Entry<String, MemClassFileObject> en : fileMap.entrySet()) {
                    m.put(en.getKey(), en.getValue().getBytes());
                }
                fileMap.clear();
                return m;
            }
        }
    }

    /**
     * 将源代码保存在一个字符串中，不需要输入文件
     */
    private static class StringSourceJavaObject extends SimpleJavaFileObject {

        private String content = null;

        StringSourceJavaObject(String name, String content) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
                    Kind.SOURCE);
            this.content = content;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return content;
        }
    }

    private static class DynaClassLoader extends ClassLoader {

        private Map<String, byte[]> classBytes = null;

        DynaClassLoader(ClassLoader parent, Map<String, byte[]> classBytes) {
            super(parent);
            this.classBytes = classBytes;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = this.classBytes.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.findClass(name);
        }

        /*
         * @Override protected void finalize() throws Throwable {
         * System.out.println("DynaClassLoader finalize"); }
         */

    }

}
