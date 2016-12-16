package com.anbaoxing.autoble4_0;

import android.content.Context;

import java.math.BigInteger;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 小小工具类
 * (String.valueOf(j /(float) (mBloodDatasBeens.size() / 7))).length() == 3
 */
public class BleUtils {

    /*
    判断一个字符串是不是整数
     */
    public static boolean isNumeric(String string) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(string.trim());
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /*
     Android开发中程序判断手机操作系统版本
     */
    public static int getAndroidSDKVersion() {
        int version = 0;
        try {
            version = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
        }
        return version;
    }

    /*
     * 判断当前系统的语言环境是否为中文
     *
     */
    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    /*
    * 判断当前系统的语言环境是否为英文
    *
    */
    public static boolean isEN(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("en"))
            return true;
        else
            return false;
    }

    /**
     * 将16进制 转换成10进制
     *
     * @param str
     * @return
     */
    public static String print10(String str) {

        StringBuffer buff = new StringBuffer();
        String array[] = str.split(" ");
        for (int i = 0; i < array.length; i++) {
            int num = Integer.parseInt(array[i], 16);
            buff.append(String.valueOf((char) num));
        }
        return buff.toString();
    }

    /*
   * 字节数组转字符串
   */
    public static String bytes2String(byte[] b) throws Exception {
        String r = new String(b, "UTF-8");
        return r;
    }

    /*
     * 字符串转字节数组
     */
    public static byte[] string2Bytes(String s) {
        byte[] r = s.getBytes();
        return r;
    }

    /*
     * 字符串转16进制字符串
     */
    public static String string2HexString(String s) throws Exception {
        String r = byte2HexStr(string2Bytes(s));
        return r;
    }

    /**
     * byte转16进制字符串
     *
     * @param b
     * @return
     */
    public static String byte2HexStr(byte[] b) {

        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * byte转二进制字符串
     *
     * @param bytes
     * @return
     */
    public static String byte2BinStr(byte[] bytes) {
        return new BigInteger(1, bytes).toString(2);// 这里的1代表正数
    }

    /**
     * 将16进制的字符串转换为字节数组
     *
     * @param message * @return 字节数组
     */
    public static byte[] getHexBytes(String message) {
        int len = message.length() / 2;
        char[] chars = message.toCharArray();
        String[] hexStr = new String[len];
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; j < len; i += 2, j++) {
            hexStr[j] = "" + chars[i] + chars[i + 1];
            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
        }
        return bytes;
    }

}
