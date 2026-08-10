package com.pip.server.util;


import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.log4j.Logger;
import java.io.*;
import java.util.regex.Matcher;
import java.text.*;


public class Utils{
    public static boolean checkString(String s, boolean allowColon){
        if(s == null){
            return false;
        }
        for(int i = 0; i < s.length(); i++){
            char ch = s.charAt(i);
            boolean isValid = false;
            if(ch >= 'a' && ch <= 'z'){
                isValid = true;
            }else if(ch >= 'A' && ch <= 'Z'){
                isValid = true;
            }else if(ch >= '0' && ch <= '9'){
                isValid = true;
            }else if(ch == '_'){
                isValid = true;
            }else if(ch >= 0x4E00 && ch <= 0x9FA5){
                isValid = true;
            }else if(allowColon && ch == ':'){
                isValid = true;
            }
            if(!isValid){
                return false;
            }
        }
        return true;
    }

    public static boolean checkString(String s){
        if(s==null)
            return false;
        for(int i=0;i<s.length();i++){
            char ch = s.charAt(i);
            if (ch == 0x0D || ch == 0x0A)
                continue;
            if(ch>=0x20&&ch<=0x7e)
                continue;
            if(ch>=0x2018&&ch<=0x203B)
                continue;
            if(ch>=0x3001&&ch<=0x3002)
                continue;
            if(ch>=0x3008&&ch<=0x3011)
                continue;
            if(ch>=0x4e00&&ch<=0x9fa5)
                continue;
            if(ch>=0xf92c&&ch<=0xfa29)
                continue;
            if(ch>=0xff01&&ch<=0xffe5)
                continue;
            return false;
        }
        return true;
    }

    static Pattern phonePattern = Pattern.compile("^13[0-9]{1}[0-9]{8}|^15[0-9]{1}[0-9]{8}|^18[0-9]{1}[0-9]{8}");


    public static boolean isValidMobilePhone(String phone){
        return phonePattern.matcher(phone).matches();
    }

    static Pattern midPattern = Pattern.compile("[\\d]{14}");

    public static boolean isValidMID(String mid){
        return midPattern.matcher(mid).matches();
    }

    public static String decodeMid(String s) {
        if(s.length()==0)
            return "";
        sun.misc.BASE64Decoder de64 = new sun.misc.BASE64Decoder();
        try {
            byte[] decData = de64.decodeBuffer(s);
            byte[] keyData1 = "pipitime".getBytes("GBK");
            byte[] realKey1 = new byte[8];
            System.arraycopy(keyData1, 0, realKey1, 0,
                             Math.min(8, keyData1.length));
            com.pip.security.DESKeyGenerator.fixUpStatic(realKey1);
            com.pip.security.DES_CBC_PKCS5 cipher1 = new com.pip.security.
                    DES_CBC_PKCS5();
            cipher1.init(com.pip.security.DES_CBC_PKCS5.DECRYPT_MODE,
                         new com.pip.security.RawSecretKey("RAW", realKey1));
            byte[] decData2 = cipher1.doFinal(decData, 0, decData.length);
            String ret = new String(decData2, "GBK");
            if(isValidMID(ret))
                return ret;
            else
                return "";
        } catch (IOException ex) {
            return "";
        } catch (Exception ex) {
            return "";
        }
    }

    private static final byte[] highDigits;

    private static final byte[] lowDigits;

    // initialize lookup tables
    static{
        final byte[] digits = {
                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };

        int i;
        byte[] high = new byte[256];
        byte[] low = new byte[256];

        for(i = 0; i < 256; i++){
            high[i] = digits[i >>> 4];
            low[i] = digits[i & 0x0F];
        }

        highDigits = high;
        lowDigits = low;
    }
    
    public static String getHexString(byte[] in){
        StringBuffer out = new StringBuffer((in.length * 3));

        for(int i = 0; i < in.length; i++){
            int byteValue = in[i] & 0xFF;
            out.append((char)highDigits[byteValue]);
            out.append((char)lowDigits[byteValue]);
        }
        return out.toString();
    }
}
