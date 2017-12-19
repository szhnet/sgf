package io.jpower.sgf.common.resource;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jpower.sgf.utils.JavaUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeBuilder;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 用来加载资源文件
 * <p>
 * 可以从文件系统路径加载或者从classpath加载
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class ResourceLoader {

    private static final int RES_TYPE_XML = 1;

    private static final int RES_TYPE_JSON = 2;

    /**
     * 获得资源文件的File
     *
     * @param filePath
     * @return 不能存在返回null
     */
    public static Path getResourceFile(String filePath) {
        Path resFile = Paths.get(filePath);

        if (Files.notExists(resFile)) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
            if (url == null) {
                return null;
            }
            try {
                resFile = Paths.get(url.toURI());
            } catch (URISyntaxException e) {
                throw JavaUtils.sneakyThrow(e);
            }
            if (Files.notExists(resFile)) {
                return null;
            }
        }
        return resFile;
    }

    /**
     * 获得资源文件的InputStream
     *
     * @param filePath
     * @return 不存在返回null
     */
    public static InputStream getResourceInputStream(String filePath) {
        InputStream in = null;
        Path resFile = Paths.get(filePath);

        try {
            if (Files.notExists(resFile)) {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
            } else {
                in = Files.newInputStream(resFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return in;
    }

    /**
     * 判断资源文件是否存在
     *
     * @param filePath
     * @return
     */
    public static boolean hasResource(String filePath) {
        Path resFile = Paths.get(filePath);

        if (Files.notExists(resFile)) {
            InputStream in = null;
            try {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
                if (in == null) {
                    return false;
                } else {
                    return true;
                }
            } finally {
                closeQuietly(in);
            }
        } else {
            return true;
        }
    }

    /**
     * 加载xml格式的资源
     *
     * @param clazz
     * @param filePath
     * @return
     */
    public static <T> T loadXmlResource(Class<T> clazz, String filePath) {
        return loadResource(clazz, filePath, RES_TYPE_XML);
    }

    /**
     * 加载json格式的资源
     *
     * @param clazz
     * @param filePath
     * @return
     */
    public static <T> T loadJsonResource(Class<T> clazz, String filePath) {
        return loadResource(clazz, filePath, RES_TYPE_JSON);
    }

    private static <T> T loadResource(Class<T> clazz, String filePath, int resType) {
        InputStream in = null;
        Path resFile = Paths.get(filePath);

        try {
            if (Files.notExists(resFile)) {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
                if (in == null) {
                    throw new IllegalArgumentException("Resource not found: " + filePath);
                }
            } else {
                in = Files.newInputStream(resFile);
            }

            T res = null;
            switch (resType) {
                case RES_TYPE_XML:
                    res = parseXml(clazz, in);
                    break;
                case RES_TYPE_JSON:
                    res = parseJson(clazz, in);
                    break;
                default:
                    throw new IllegalArgumentException("type mismatch: " + resType);
            }
            invokePostConstruct(res);

            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {

                }
            }
        }
    }

    private static <T> T parseXml(Class<T> clazz, InputStream in) throws Exception {
        InputNode inputNode = NodeBuilder.read(in);
        Serializer ser = new Persister();
        T o = ser.read(clazz, inputNode, false);
        return o;
    }

    private static <T> T parseJson(Class<T> clazz, InputStream in) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(Feature.ALLOW_COMMENTS);
        T o = mapper.readValue(in, clazz);
        return o;
    }

    private static void invokePostConstruct(Object obj) {
        Method method = findPostConstructMethod(obj.getClass());
        if (method == null) {
            return;
        }
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalStateException("PostConstruct method is static. method=" + method);
        }
        try {
            method.invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    private static Method findPostConstructMethod(Class<?> clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            PostConstruct annotation = m.getAnnotation(PostConstruct.class);
            if (annotation != null) {
                return m;
            }
        }

        // 找接口
        for (Class<?> ifc : clazz.getInterfaces()) {
            Method method = findPostConstructMethod(ifc);
            if (method != null) {
                return method;
            }
        }

        // 找父类
        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null || superclass == Object.class) {
            return null;
        }
        return findPostConstructMethod(superclass);
    }

    private static void closeQuietly(InputStream obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (Exception ignored) {

            }
        }
    }

}
