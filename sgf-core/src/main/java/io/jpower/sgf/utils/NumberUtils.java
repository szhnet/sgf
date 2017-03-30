package io.jpower.sgf.utils;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class NumberUtils {

    final static char[] DIGITS_64 = {
            'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1',
            '2', '3', '4', '5', '6', '7',
            '8', '9', '-', '_'
    };

    final static char[] DIGITS_62 = {
            'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1',
            '2', '3', '4', '5', '6', '7',
            '8', '9'
    };

    private static final long MAGIC_NUM_64_1 = 8070450532247928832L; // 7 * 64^10 再大就超过Long.MAX_VALUE

    private static final long MAGIC_NUM_64_2 = Long.MAX_VALUE - MAGIC_NUM_64_1;

    private static final long MAGIC_NUM_64_SUB = MAGIC_NUM_64_1 - MAGIC_NUM_64_2;

    private static final long MAGIC_NUM_62_1 = 8392993658683402240L; // 10 * 62^10 再大就超过Long.MAX_VALUE

    private static final long MAGIC_NUM_62_2 = Long.MAX_VALUE - MAGIC_NUM_62_1;

    private static final long MAGIC_NUM_62_SUB = MAGIC_NUM_62_1 - MAGIC_NUM_62_2;

    /**
     * 转换为64进制无符号字符串形式
     *
     * @param num
     * @return
     */
    public static String toBase64String(long num) {
        int c = 11;
        char[] buf = new char[c];
        int charPos = c;
        int shift = 6;
        int radix = 1 << shift;
        long mask = radix - 1;
        do {
            buf[--charPos] = DIGITS_64[(int) (num & mask)];
            num >>>= shift;
        } while (num != 0);
        return new String(buf, charPos, (c - charPos));
    }

    @SuppressWarnings("unused")
    private static String toBase64String1(long num) {
        int c = 11;
        int base = 64;
        if (num < 0) {
            long num1 = 0;
            int _10positionAdd = 7;
            if (num < Long.MIN_VALUE + MAGIC_NUM_64_SUB) {
                num1 = -(Long.MIN_VALUE - num) + MAGIC_NUM_64_2 + 1;
            } else {
                num1 = -(Long.MIN_VALUE + MAGIC_NUM_64_SUB - 1 - num);
                _10positionAdd *= 2;
            }
            char[] buf = new char[c];
            int charPos = c;
            do {
                --charPos;
                if (num1 != 0) {
                    if (buf.length - charPos == 11) {
                        buf[charPos] = DIGITS_64[(int) (num1 % base) + _10positionAdd];
                    } else {
                        buf[charPos] = DIGITS_64[(int) (num1 % base)];
                    }
                    num1 = num1 / base;
                } else {
                    if (buf.length - charPos == 11) {
                        buf[charPos] = DIGITS_64[_10positionAdd];
                    } else {
                        buf[charPos] = DIGITS_64[0];
                    }
                }
            } while (num1 != 0 || buf.length - charPos < 11);
            return new String(buf, charPos, (c - charPos));
        } else {
            char[] buf = new char[c];
            int charPos = c;
            do {
                buf[--charPos] = DIGITS_64[(int) (num % base)];
                num = num / base;
            } while (num != 0);
            return new String(buf, charPos, (c - charPos));
        }
    }

    /**
     * 转换为62进制无符号字符串形式
     *
     * @param num
     * @return
     */
    public static String toBase62String(long num) {
        int c = 11; // 最多11位
        int base = 62;
        if (num < 0) {
            long num1 = 0;
            int _10positionAdd = 10;
            if (num < Long.MIN_VALUE + MAGIC_NUM_62_SUB) { // 2倍的MAGIC_NUM_62_1可以支持的负数，小于此值用不到2倍。
                num1 = -(Long.MIN_VALUE - num) + MAGIC_NUM_62_2 + 1;
            } else {
                num1 = -(Long.MIN_VALUE + MAGIC_NUM_62_SUB - 1 - num);
                _10positionAdd *= 2;
            }
            char[] buf = new char[c];
            int charPos = c;
            do {
                --charPos;
                if (num1 != 0) {
                    if (buf.length - charPos == 11) {
                        buf[charPos] = DIGITS_62[(int) (num1 % base) + _10positionAdd];
                    } else {
                        buf[charPos] = DIGITS_62[(int) (num1 % base)];
                    }
                    num1 = num1 / base;
                } else {
                    if (buf.length - charPos == 11) {
                        buf[charPos] = DIGITS_62[_10positionAdd];
                    } else {
                        buf[charPos] = DIGITS_62[0];
                    }
                }
            } while (num1 != 0 || buf.length - charPos < 11);
            return new String(buf, charPos, (c - charPos));
        } else {
            char[] buf = new char[c];
            int charPos = c;
            do {
                buf[--charPos] = DIGITS_62[(int) (num % base)];
                num = num / base;
            } while (num != 0);
            return new String(buf, charPos, (c - charPos));
        }
    }

}
