package io.jpower.sgf.common.compile;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import io.jpower.sgf.common.tuple.TwoTuple;
import io.jpower.sgf.utils.JavaUtils;

/**
 * 执行java代码
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class DynamicJavaRunner {

    /**
     * skip多行注释
     */
    private static final byte SKIP_TYPE_MCOMM = 1;

    /**
     * skip单行注释
     */
    private static final byte SKIP_TYPE_SCOMM = 2;

    /**
     * skip字符串
     */
    private static final byte SKIP_TYPE_STR = 3;

    private static final String KEY_PACKAGE = "package";

    private static final String KEY_CLAZZ = "public class";

    private static final DynamicJavaCompiler javaCompiler = new DynamicJavaCompiler();

    private String packageName;

    /**
     * @param packageName 包名
     */
    public DynamicJavaRunner(String packageName) {
        this.packageName = packageName;
    }

    /**
     * 执行代码
     *
     * @param code
     */
    public <T> T run(String code) {
        Class<?> clazz = compile(code);
        return run(clazz);
    }

    /**
     * 编译代码
     *
     * @param code
     * @return
     */
    public Class<?> compile(String code) {
        TwoTuple<String, String> parseRst = parseCode(code);
        if (parseRst.getFirst() == null) {
            throw new IllegalArgumentException("Not found class name.");
        }

        String fullClassName = packageName + "." + parseRst.getFirst();
        Class<?> clazz = javaCompiler.compile(fullClassName, parseRst.getSecond());
        return clazz;
    }

    /**
     * 执行
     * <p>
     * <ul>
     * <li>需要类有无参构造方法</li>
     * <li>需要类有public的run，call或者execute方法，方法是无参的，可以有返回值。如果没有这些方法将报错。</li>
     * <li>推荐实现{@link Runnable}或者{@link Callable}</li>
     * </ul>
     *
     * @param clazz
     */
    public <T> T run(Class<?> clazz) {
        Object ins = null;
        try {
            ins = clazz.newInstance();
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        }

        if (ins instanceof Runnable) {
            ((Runnable) ins).run();
            return null;
        } else if (ins instanceof Callable<?>) {
            try {
                @SuppressWarnings("unchecked")
                T ret = ((Callable<T>) ins).call();
                return ret;
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        } else {
            Method runMethod = null;
            try {
                runMethod = clazz.getMethod("run");
            } catch (Exception ignored) {
                // not found
            }
            if (runMethod == null) {
                try {
                    runMethod = clazz.getMethod("call");
                } catch (Exception ignored) {
                    // not found
                }
            }
            if (runMethod == null) {
                try {
                    runMethod = clazz.getMethod("execute");
                } catch (Exception ignored) {
                    // not found
                }
            }

            if (runMethod == null) {
                throw new RuntimeException("Not found run(), call() or execute() method");
            }
            try {
                @SuppressWarnings("unchecked")
                T ret = (T) runMethod.invoke(ins);
                return ret;
            } catch (Exception e) {
                throw JavaUtils.sneakyThrow(e);
            }
        }
    }

    /**
     * @param code
     * @return 1:className 2:code
     */
    private TwoTuple<String, String> parseCode(String code) {
        int len = code.length();
        byte skipType = 0;
        StringBuilder partCode = new StringBuilder();
        int packageBegin = -1, packageEnd = -1;
        boolean classFind = false;
        int classNameBegin = -1, classNameEnd = -1;
        for (int i = 0; i < len; i++) {
            char c = code.charAt(i);
            if (skipType == SKIP_TYPE_MCOMM) {
                if (c == '*') {
                    if (i + 1 < len) {
                        char nc = code.charAt(i + 1);
                        if (nc == '/') {
                            skipType = 0;
                            c = nc;
                            i++;
                            appendWhite(partCode);
                        }
                    }
                }
            } else if (skipType == SKIP_TYPE_SCOMM) {
                if (c == '\n' || c == '\r') {
                    skipType = 0;
                    appendWhite(partCode);
                }
            } else if (skipType == SKIP_TYPE_STR) {
                if (c == '"') {
                    boolean find = false;
                    if (i - 1 >= 0) {
                        char pc = code.charAt(i - 1);
                        if (pc != '\\') {
                            find = true;
                        }
                    } else {
                        find = true;
                    }

                    if (find) {
                        skipType = 0;
                    }
                }
            } else {
                boolean append = true;
                if (c == '/') {
                    if (i + 1 < len) {
                        char nc = code.charAt(i + 1);
                        if (nc == '*') {
                            skipType = SKIP_TYPE_MCOMM;
                            c = nc;
                            i++;
                            append = false;
                        } else if (nc == '/') {
                            skipType = SKIP_TYPE_SCOMM;
                            c = nc;
                            i++;
                            append = false;
                        }
                    }
                } else if (c == '"') {
                    boolean find = false;
                    if (i - 1 >= 0) {
                        char pc = code.charAt(i - 1);
                        if (pc != '\\') { // 不是转义字符
                            find = true;
                        }
                    } else {
                        find = true;
                    }
                    if (find) {
                        skipType = SKIP_TYPE_STR;
                        append = false;
                    }
                }

                if (append) {
                    partCode.append(c);
                } else {
                    continue;
                }
            }

            if (packageEnd == -1) { // 找到package语句
                if (packageBegin == -1) {
                    int partPackageBegin = partCode.indexOf(KEY_PACKAGE);
                    if (partPackageBegin != -1) {
                        packageBegin = i - (KEY_PACKAGE.length() - 1);
                    }
                } else {
                    if (c == ';') {
                        packageEnd = i + 1;
                    }
                }
            }

            if (classNameEnd == -1) {
                if (!classFind) {
                    int clazzBegin = partCode.indexOf(KEY_CLAZZ);
                    if (clazzBegin != -1) {
                        classFind = true;
                    }
                } else {
                    if (classNameBegin == -1) {
                        if (isClassNameChar(c)) {
                            classNameBegin = i;
                        }
                    } else {
                        if (!isClassNameChar(c)) {
                            classNameEnd = i;
                        }
                    }
                }
            }

            if (packageEnd != -1 && classNameEnd != -1) {
                break; // 找到包名和类名就完事了
            }
        }

        String className = code.substring(classNameBegin, classNameEnd);

        StringBuilder finalCode = null;
        if (packageBegin != -1 && packageEnd != -1) {
            finalCode = new StringBuilder(
                    code.substring(0, packageBegin) + code.substring(packageEnd, code.length()));
        } else {
            finalCode = new StringBuilder(code);
        }
        finalCode.insert(0, "package " + packageName + ";");

        return new TwoTuple<String, String>(className, finalCode.toString());
    }

    private void appendWhite(StringBuilder partCode) {
        if (partCode.length() == 0 || partCode.charAt(partCode.length() - 1) != ' ') {
            partCode.append(' ');
        }
    }

    private boolean isClassNameChar(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }
        if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
            return true;
        }
        if (c == '_' || c == '$') {
            return true;
        }
        return false;
    }

}
