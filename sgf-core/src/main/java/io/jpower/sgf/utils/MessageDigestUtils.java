package io.jpower.sgf.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 消息摘要工具
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class MessageDigestUtils {


    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final String ALGORITHM_MD5 = "MD5";

    private static final String ALGORITHM_SHA1 = "SHA-1";

    private static final String ALGORITHM_SHA256 = "SHA-256";

    private static final String ALGORITHM_HMACSHA1 = "HmacSHA1";

    private static final String ALGORITHM_HMACSHA256 = "HmacSHA256";

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
            'b', 'c', 'd', 'e', 'f'};

    private static final int BUFF_SIZE = 1024;

    public static byte[] md5(byte[] data) {
        return doBytes(ALGORITHM_MD5, data);
    }

    public static byte[] md5(String str) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytes(ALGORITHM_MD5, data);
    }

    public static byte[] md5(InputStream dataIn) {
        return doStream(ALGORITHM_MD5, dataIn);
    }

    public static String md5Hex(byte[] data) {
        return doBytesHex(ALGORITHM_MD5, data);
    }

    public static String md5Hex(String str) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytesHex(ALGORITHM_MD5, data);
    }

    public static String md5Hex(InputStream dataIn) {
        return doStreamHex(ALGORITHM_MD5, dataIn);
    }

    public static byte[] sha1(byte[] data) {
        return doBytes(ALGORITHM_SHA1, data);
    }

    public static byte[] sha1(String str) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytes(ALGORITHM_SHA1, data);
    }

    public static byte[] sha1(InputStream dataIn) {
        return doStream(ALGORITHM_SHA1, dataIn);
    }

    public static String sha1Hex(byte[] data) {
        return doBytesHex(ALGORITHM_SHA1, data);
    }

    public static String sha1Hex(String str) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytesHex(ALGORITHM_SHA1, data);
    }

    public static String sha1Hex(InputStream dataIn) {
        return doStreamHex(ALGORITHM_SHA1, dataIn);
    }

    public static byte[] sha256(byte[] data) {
        return doBytes(ALGORITHM_SHA256, data);
    }

    public static byte[] sha256(String str) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytes(ALGORITHM_SHA256, data);
    }

    public static byte[] sha256(InputStream dataIn) {
        return doStream(ALGORITHM_SHA256, dataIn);
    }

    public static String sha256Hex(byte[] data) {
        return doBytesHex(ALGORITHM_SHA256, data);
    }

    public static String sha256Hex(String str) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytesHex(ALGORITHM_SHA256, data);
    }

    public static String sha256Hex(InputStream dataIn) {
        return doStreamHex(ALGORITHM_SHA256, dataIn);
    }

    private static byte[] doBytes(String algorithm, byte[] data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        md.update(data);
        return md.digest();
    }

    private static String doBytesHex(String algorithm, byte[] data) {
        return toHexString(doBytes(algorithm, data));
    }

    private static String toHexString(byte[] output) {
        char[] c = new char[output.length * 2];
        for (int i = 0; i < output.length; i++) {
            byte b = output[i];
            c[i * 2] = DIGITS[b >> 4 & 0xF];
            c[i * 2 + 1] = DIGITS[b & 0xF];
        }
        return new String(c);
    }

    private static byte[] doStream(String algorithm, InputStream dataIn) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw JavaUtils.sneakyThrow(e);
        }

        byte[] buf = new byte[BUFF_SIZE];
        int numRead = 0;
        try {
            while ((numRead = dataIn.read(buf)) >= 0) {
                md.update(buf, 0, numRead);
            }
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        return md.digest();
    }

    private static String doStreamHex(String algorithm, InputStream dataIn) {
        return toHexString(doStream(algorithm, dataIn));
    }

    public static byte[] hmacSha1(byte[] data, String key) {
        return doBytesHmac(ALGORITHM_HMACSHA1, data, key);
    }

    public static byte[] hmacSha1(String str, String key) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytesHmac(ALGORITHM_HMACSHA1, data, key);
    }

    public static byte[] hmacSha1(InputStream dataIn, String key) {
        return doStreamHmac(ALGORITHM_HMACSHA1, dataIn, key);
    }

    public static String hmacSha1Hex(byte[] data, String key) {
        return doBytesHmacHex(ALGORITHM_HMACSHA1, data, key);
    }

    public static String hmacSha1Hex(String str, String key) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytesHmacHex(ALGORITHM_HMACSHA1, data, key);
    }

    public static String hmacSha1Hex(InputStream dataIn, String key) {
        return doStreamHmacHex(ALGORITHM_HMACSHA1, dataIn, key);
    }

    public static byte[] hmacSha256(byte[] data, String key) {
        return doBytesHmac(ALGORITHM_HMACSHA256, data, key);
    }

    public static byte[] hmacSha256(String str, String key) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytesHmac(ALGORITHM_HMACSHA256, data, key);
    }

    public static byte[] hmacSha256(InputStream dataIn, String key) {
        return doStreamHmac(ALGORITHM_HMACSHA256, dataIn, key);
    }

    public static String hmacSha256Hex(byte[] data, String key) {
        return doBytesHmacHex(ALGORITHM_HMACSHA256, data, key);
    }

    public static String hmacSha256Hex(String str, String key) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytesHmacHex(ALGORITHM_HMACSHA256, data, key);
    }

    public static String hmacSha256Hex(InputStream dataIn, String key) {
        return doStreamHmacHex(ALGORITHM_HMACSHA256, dataIn, key);
    }

    private static byte[] doBytesHmac(String algorithm, byte[] data, String key) {
        Mac mac = null;
        try {
            mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(CHARSET), algorithm);
            mac.init(secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        return mac.doFinal(data);
    }

    private static String doBytesHmacHex(String algorithm, byte[] data, String key) {
        return toHexString(doBytesHmac(algorithm, data, key));
    }

    private static byte[] doStreamHmac(String algorithm, InputStream dataIn, String key) {
        Mac mac = null;
        try {
            mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(CHARSET), algorithm);
            mac.init(secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw JavaUtils.sneakyThrow(e);
        }

        byte[] buf = new byte[BUFF_SIZE];
        int numRead = 0;
        try {
            while ((numRead = dataIn.read(buf)) >= 0) {
                mac.update(buf, 0, numRead);
            }
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        return mac.doFinal();
    }

    private static String doStreamHmacHex(String algorithm, InputStream dataIn, String key) {
        return toHexString(doStreamHmac(algorithm, dataIn, key));
    }

    public static void main(String[] args) {
        String m = MessageDigestUtils.md5Hex("abc呵呵");
        System.out.println(m);

        m = MessageDigestUtils.sha1Hex("abc呵呵");
        System.out.println(m);

        m = MessageDigestUtils.sha256Hex("abc呵呵");
        System.out.println(m);

        String key = "jalskjf0)(U)(&)&)*(";
        m = MessageDigestUtils.hmacSha1Hex("abc呵呵", key);
        System.out.println(m);

        m = MessageDigestUtils.hmacSha256Hex("abc呵呵", key);
        System.out.println(m);
    }

}
