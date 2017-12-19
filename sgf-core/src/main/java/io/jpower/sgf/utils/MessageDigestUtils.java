package io.jpower.sgf.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 消息摘要工具
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class MessageDigestUtils {

    public static final Charset CHARSET = Charset.forName("UTF-8");

    public static final String ALGORITHM_MD5 = "MD5";

    public static final String ALGORITHM_SHA1 = "SHA-1";

    public static final String ALGORITHM_SHA256 = "SHA-256";

    public static final String ALGORITHM_HMACSHA1 = "HmacSHA1";

    public static final String ALGORITHM_HMACSHA256 = "HmacSHA256";

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
            'b', 'c', 'd', 'e', 'f'};

    public static String md5(byte[] data) {
        return doBytes(ALGORITHM_MD5, data);
    }

    public static String md5(String str) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytes(ALGORITHM_MD5, data);
    }

    public static String md5(InputStream dataIn) {
        return doStream(ALGORITHM_MD5, dataIn);
    }

    public static String sha1(byte[] data) {
        return doBytes(ALGORITHM_SHA1, data);
    }

    public static String sha1(String str) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytes(ALGORITHM_SHA1, data);
    }

    public static String sha1(InputStream dataIn) {
        return doStream(ALGORITHM_SHA1, dataIn);
    }

    public static String sha256(byte[] data) {
        return doBytes(ALGORITHM_SHA256, data);
    }

    public static String sha256(String str) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytes(ALGORITHM_SHA256, data);
    }

    public static String sha256(InputStream dataIn) {
        return doStream(ALGORITHM_SHA256, dataIn);
    }

    private static String doBytes(String algorithm, byte[] data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        md.update(data);
        return toBase16(md.digest());
    }

    private static String toBase16(byte[] output) {
        char[] c = new char[output.length * 2];
        for (int i = 0; i < output.length; i++) {
            byte b = output[i];
            c[i * 2] = DIGITS[b >> 4 & 0xF];
            c[i * 2 + 1] = DIGITS[b & 0xF];
        }
        return new String(c);
    }

    private static String doStream(String algorithm, InputStream dataIn) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw JavaUtils.sneakyThrow(e);
        }

        byte[] buf = new byte[1024];
        int numRead = 0;
        try {
            while ((numRead = dataIn.read(buf)) >= 0) {
                md.update(buf, 0, numRead);
            }
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        return toBase16(md.digest());
    }

    public static String hmacSha1(byte[] data, String key) {
        return doBytesHmac(ALGORITHM_HMACSHA1, data, key);
    }

    public static String hmacSha1(String str, String key) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytesHmac(ALGORITHM_HMACSHA1, data, key);
    }

    public static String hmacSha1(InputStream dataIn, String key) {
        return doStreamHmac(ALGORITHM_HMACSHA1, dataIn, key);
    }

    public static String hmacSha256(byte[] data, String key) {
        return doBytesHmac(ALGORITHM_HMACSHA256, data, key);
    }

    public static String hmacSha256(String str, String key) {
        byte[] data;
        data = str.getBytes(CHARSET);
        return doBytesHmac(ALGORITHM_HMACSHA256, data, key);
    }

    public static String hmacSha256(InputStream dataIn, String key) {
        return doStreamHmac(ALGORITHM_HMACSHA256, dataIn, key);
    }

    private static String doBytesHmac(String algorithm, byte[] data, String key) {
        Mac mac = null;
        try {
            mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(CHARSET), algorithm);
            mac.init(secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        return toBase16(mac.doFinal(data));
    }

    private static String doStreamHmac(String algorithm, InputStream dataIn, String key) {
        Mac mac = null;
        try {
            mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(CHARSET), algorithm);
            mac.init(secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw JavaUtils.sneakyThrow(e);
        }

        byte[] buf = new byte[1024];
        int numRead = 0;
        try {
            while ((numRead = dataIn.read(buf)) >= 0) {
                mac.update(buf, 0, numRead);
            }
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
        return toBase16(mac.doFinal());
    }

    public static void main(String[] args) {
        String m = MessageDigestUtils.md5("abc呵呵");
        System.out.println(m);

        m = MessageDigestUtils.sha1("abc呵呵");
        System.out.println(m);

        m = MessageDigestUtils.sha256("abc呵呵");
        System.out.println(m);

        m = MessageDigestUtils.hmacSha1("abc呵呵", "jalskjf0)(U)(&)&)*(");
        System.out.println(m);

        m = MessageDigestUtils.hmacSha256("abc呵呵", "jalskjf0)(U)(&)&)*(");
        System.out.println(m);
    }

}
