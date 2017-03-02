package com.brok1n.java.fx.serialassistant.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by brok1n on 2017/2/20.
 */
public class Butils {

    // 转化字符串为十六进制编码
    public static String toHexString(String s)
    {
        String str="";
        for (int i=0;i<s.length();i++)
        {
            int ch = (int)s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    public static boolean copyInputStreamToFile(InputStream source, File destination)
    {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream( destination );
            while( source.available() > 0 )
            {
                output.write( source.read() );
            }
            output.flush();

        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally {
            if( output != null )
            {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static void log( String msg )
    {
        System.out.println( msg );
    }


    /**
     * @Title:bytes2HexString
     * @Description:字节数组转16进制字符串
     * @param b
     *            字节数组
     * @return 16进制字符串
     * @throws
     */
    public static String bytes2HexString(byte[] b) {
        StringBuffer result = new StringBuffer();
        String hex;
        for (int i = 0; i < b.length; i++) {
            hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result.append(hex.toUpperCase());
        }
        return result.toString();
    }

    /**
     * @Title:bytes2HexString
     * @Description:字节数组转16进制字符串
     * @param b
     *            字节数组
     * @return 16进制字符串
     * @throws
     */
    public static String bytes2HexString(byte[] b, int start, int len ) {
        StringBuffer result = new StringBuffer();
        String hex;
        for (int i = start; i < start + len; i++) {
            hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result.append(hex.toUpperCase());
        }
        return result.toString();
    }

    /**
     * @Title:hexString2Bytes
     * @Description:16进制字符串转字节数组
     * @param src
     *            16进制字符串
     * @return 字节数组
     * @throws
     */
    public static byte[] hexString2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = (byte) Integer
                    .valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

    /**
     * @Title:string2HexString
     * @Description:字符串转16进制字符串
     * @param strPart
     *            字符串
     * @return 16进制字符串
     * @throws
     */
    public static String string2HexString(String strPart) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < strPart.length(); i++) {
            int ch = (int) strPart.charAt(i);
            String strHex = Integer.toHexString(ch);
            hexString.append(strHex.toUpperCase());
        }
        return hexString.toString();
    }

    /**
     * 16进制字符串转换为字符串
     *
     * @param s
     * @return
     */
    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword);
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    /**
     * @Title:char2Byte
     * @Description:字符转成字节数据char-->integer-->byte
     * @param src
     * @return
     * @throws
     */
    public static Byte char2Byte(Character src) {
        return Integer.valueOf((int)src).byteValue();
    }

    /**
     * @Title:intToHexString
     * @Description:10进制数字转成16进制
     * @param a 转化数据
     * @param len 占用字节数
     * @return
     * @throws
     */
    private static String intToHexString(int a,int len){
        len<<=1;
        String hexString = Integer.toHexString(a);
        int b = len -hexString.length();
        if(b>0){
            for(int i=0;i<b;i++)  {
                hexString = "0" + hexString;
            }
        }
        return hexString;
    }


}
