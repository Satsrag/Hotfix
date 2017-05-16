package com.zuga.hotfix.hotfix.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Saqrag
 * @version 1.0
 * @see null
 * 2017/3/27
 * @since 1.0
 **/
public class MD5 {
    /**
     * 将字符串转成MD5值
     *
     * @param string
     * @return
     */
    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    public static String toFormatMd5(String string) {
        int i = string.lastIndexOf(".");
        if (i < 0) {
            return MD5.stringToMD5(string);
        }
        String substring = string.substring(i);
        return MD5.stringToMD5(string) + substring;
    }
}
