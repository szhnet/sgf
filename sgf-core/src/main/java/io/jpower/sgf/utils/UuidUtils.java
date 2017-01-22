package io.jpower.sgf.utils;

import java.util.UUID;

/**
 * UUID工具
 *
 * @author zheng.sun
 */
public class UuidUtils {

    /**
     * 随机一个UUID，并以16进制字符串形式返回
     *
     * @return
     */
    public static String uuid() {
        UUID uuid = UUID.randomUUID();
        return toHexString(uuid.getMostSignificantBits(), 16)
                + toHexString(uuid.getLeastSignificantBits(), 16);
    }

    /**
     * 将一个已有的UUID以16进制字符串形式返回
     *
     * @param uuid
     * @return
     */
    public static String uuid(UUID uuid) {
        return toHexString(uuid.getMostSignificantBits(), 16)
                + toHexString(uuid.getLeastSignificantBits(), 16);
    }

    private static String toHexString(long val, int expectLength) {
        String hexStr = Long.toHexString(val);
        int append = expectLength - hexStr.length();
        if (append > 0) {
            StringBuilder sb = new StringBuilder(expectLength);
            for (int i = 0; i < append; i++) {
                sb.append('0');
            }
            sb.append(hexStr);
            return sb.toString();
        } else {
            return hexStr;
        }
    }

    /**
     * 随机一个UUID，并以较短的字符串形式返回
     *
     * @return
     */
    public static String shortUuid() {
        UUID uuid = UUID.randomUUID();
        return to62String(uuid.getMostSignificantBits(), 11)
                + to62String(uuid.getLeastSignificantBits(), 11);
    }

    /**
     * 将一个已有的UUID以较短的字符串形式返回
     *
     * @param uuid
     * @return
     */
    public static String shortUuid(UUID uuid) {
        return to62String(uuid.getMostSignificantBits(), 11)
                + to62String(uuid.getLeastSignificantBits(), 11);
    }

    private static String to62String(long val, int expectLength) {
        String uuidStr = NumberUtils.toBase62String(val);
        int append = expectLength - uuidStr.length();
        if (append > 0) {
            StringBuilder sb = new StringBuilder(expectLength);
            for (int i = 0; i < append; i++) {
                sb.append(NumberUtils.DIGITS_62[0]);
            }
            sb.append(uuidStr);
            return sb.toString();
        } else {
            return uuidStr;
        }
    }

}
