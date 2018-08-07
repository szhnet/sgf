package io.jpower.sgf.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

/**
 * java语言和jdk类库层面的一些便捷方法
 *1
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class JavaUtils {

    private static final Logger log = LoggerFactory.getLogger(JavaUtils.class);

    public static final Object[] EMPTY_OBJS = new Object[0];

    public static final Class<?>[] EMPTY_CLASSES = new Class[0];

    private static final boolean IS_WINDOWS = isWindows0();

    private static final Unsafe unsafe;

    static {
        // attempt to access field Unsafe#theUnsafe
        final Object maybeUnsafe = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                    unsafeField.setAccessible(true);
                    // the unsafe instance
                    return unsafeField.get(null);
                } catch (Exception e) {
                    return e;
                }
            }
        });

        if (maybeUnsafe instanceof Exception) {
            unsafe = null;
            log.debug("sun.misc.Unsafe.theUnsafe: unavailable", (Exception) maybeUnsafe);
        } else {
            unsafe = (Unsafe) maybeUnsafe;
            log.debug("sun.misc.Unsafe.theUnsafe: available");
        }
    }

    private static boolean isWindows0() {
        boolean windows = SystemPropertyUtils.get("os.name", "").toLowerCase(Locale.US).contains("win");
        if (windows) {
            log.debug("Platform: Windows");
        }
        return windows;
    }

    /**
     * Return {@code true} if the JVM is running on Windows
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    /**
     * 以字符串形式返回堆栈信息
     *
     * @param t
     * @return
     */
    public static String stackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * 可以将throwable抛出，并且不需要catch它，就算是Checked Exception也不需要。
     * 不是通过用RuntimeException包装实现的，抛出的是原始的异常。
     * <p>
     * <pre>
     * try {
     *     ... // can throw Checked Exception
     * } catch (Exception e) {
     *     throw new RuntimeException(e);
     * }
     * </pre>
     * <p>
     * -->
     * <p>
     * <pre>
     * try {
     *     ... // can throw Checked Exception
     * } catch (Exception e) {
     *     throw sneakyThrow(e);
     * }
     * </pre>
     *
     * @param t
     * @return
     */
    public static RuntimeException sneakyThrow(Throwable t) {
        if (t == null) {
            throw new NullPointerException("t");
        }
        JavaUtils.<RuntimeException>sneakyThrow0(t);
        // Shouldn't reach here.
        return new RuntimeException(t);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
        throw (T) t;
    }

    /**
     * 与{@link Class#getSimpleName()}的区别是
     * <ul>
     * <li>对于内嵌类或者内部类，如果类的{@link Class#getName()}返回
     * <code>com.package.Outter$Inner</code>， 那么此方法返回<code>Outter$Inner</code>，而
     * {@link Class#getSimpleName()}返回的是<code>Inner</code></li>
     * <li>对于匿名类，如果类的{@link Class#getName()}返回 <code>com.package.Outter$1</code>
     * ， 那么此方法返回<code>Outter$1</code>，而 {@link Class#getSimpleName()}返回的是空字符串
     * </li>
     * <li>对于数组也是类似规则，如果类的{@link Class#getName()}返回
     * <code>[Lcom.package.Outter$Inner</code> ， 那么此方法返回
     * <code>Outter$Inner[]</code>，而 {@link Class#getSimpleName()}返回的是
     * <code>Inner[]</code></li>
     * <li>其他的和{@link Class#getSimpleName()}一样</li>
     * </ul>
     *
     * @param clazz
     * @return
     */
    public static String getSimpleName(Class<?> clazz) {
        if (clazz.isArray()) {
            return getSimpleName(clazz.getComponentType()) + "[]";
        }
        String sname = null;
        String className = clazz.getName();
        int idx = className.lastIndexOf('.');
        if (idx != -1) {
            sname = className.substring(idx + 1);
        } else {
            sname = className;
        }
        return sname;
    }

    /**
     * 获得sun.misc.Unsafe的实例
     *
     * @return
     */
    public static Unsafe getUnsafe() {
        return unsafe;
    }

    public static boolean bool(Collection<?> c) {
        return c != null && c.size() > 0;
    }

    public static boolean bool(Map<?, ?> m) {
        return m != null && m.size() > 0;
    }

    public static boolean bool(CharSequence s) {
        return s != null && s.length() > 0;
    }

    public static boolean bool(Object[] objs) {
        return objs != null && objs.length > 0;
    }

}
