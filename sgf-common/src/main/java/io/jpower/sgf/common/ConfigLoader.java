package io.jpower.sgf.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeBuilder;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jpower.sgf.utils.JavaUtils;

/**
 * 用来加载配置文件
 * <p>
 * <p>
 * 可以从文件系统路径加载或者从classpath加载
 *
 * @author zheng.sun
 */
public class ConfigLoader {

    private static final int CONFIG_TYPE_XML = 1;

    private static final int CONFIG_TYPE_JSON = 2;

    /**
     * 获得配置文件的File
     *
     * @param filePath
     * @return 不能存在返回null
     */
    public static File getConfigFile(String filePath) {
        File cfgFile = new File(filePath);

        if (!cfgFile.exists()) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
            if (url == null) {
                return null;
            }
            cfgFile = new File(url.getPath());
            if (!cfgFile.exists()) {
                return null;
            }
        }
        return cfgFile;
    }

    /**
     * 获得配置文件的InputStream
     *
     * @param filePath
     * @return 不存在返回null
     */
    public static InputStream getConfigInputStream(String filePath) {
        InputStream in = null;
        File cfgFile = new File(filePath);

        try {
            if (!cfgFile.exists()) {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
            } else {
                in = new FileInputStream(cfgFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return in;
    }

    /**
     * 判断配置文件是否存在
     *
     * @param filePath
     * @return
     */
    public static boolean hasConfig(String filePath) {
        File cfgFile = new File(filePath);

        if (!cfgFile.exists()) {
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
     * 加载xml格式的配置
     *
     * @param clazz
     * @param filePath
     * @return
     */
    public static <T> T loadConfigXml(Class<T> clazz, String filePath) {
        return loadConfig(clazz, filePath, CONFIG_TYPE_XML);
    }

    /**
     * 加载json格式的配置
     *
     * @param clazz
     * @param filePath
     * @return
     */
    public static <T> T loadConfigJson(Class<T> clazz, String filePath) {
        return loadConfig(clazz, filePath, CONFIG_TYPE_JSON);
    }

    private static <T> T loadConfig(Class<T> clazz, String filePath, int configType) {
        InputStream in = null;
        File cfgFile = new File(filePath);

        try {
            if (!cfgFile.exists()) {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
                if (in == null) {
                    throw new IllegalArgumentException("Resource not found: " + filePath);
                }
            } else {
                in = new FileInputStream(cfgFile);
            }

            T config = null;
            switch (configType) {
                case CONFIG_TYPE_XML:
                    config = parseXml(clazz, in);
                    break;
                case CONFIG_TYPE_JSON:
                    config = parseJson(clazz, in);
                    break;
                default:
                    throw new IllegalArgumentException("type mismatch: " + configType);
            }
            invokePostConstruct(config);

            return config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {

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
            } catch (Exception e) {

            }
        }
    }

}
