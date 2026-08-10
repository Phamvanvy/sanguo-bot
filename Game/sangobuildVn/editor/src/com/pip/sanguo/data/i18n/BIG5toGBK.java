package com.pip.sanguo.data.i18n;

import java.io.*;

/**
 * This class is used to convert BIG5 characters to GBK characters. It uses a
 * data file big5gbk.properties in <classes> directory.
 */
public class BIG5toGBK {
    private static char[] arrDictionary = null;
    private static char[] arrDictionaryGBK2BIG5 = null;
    static {
        loadMapping();
        loadingMappinggbk2big5();
    }

    // Load data file to build the mapping table from BIG5 to GBK
    private static void loadMapping() {
        try {
            InputStream is = BIG5toGBK.class.getResourceAsStream(
                "/com/pip/sanguo/data/i18n/big5gbk.data");
            DataInputStream dis = new DataInputStream(is);
            arrDictionary = new char[65536];
            for (int i = 0; i < 65536; i++) {
                arrDictionary[i] = dis.readChar();
            }
            dis.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load data file to build the mapping table from GBK to BIG5
    private static void loadingMappinggbk2big5() {
        try {
            InputStream is = BIG5toGBK.class.getResourceAsStream(
                "/com/pip/sanguo/data/i18n/gbk2big5.data");
            DataInputStream dis = new DataInputStream(is);
            arrDictionaryGBK2BIG5 = new char[65536];
            for (int i = 0; i < 65536; i++) {
                arrDictionaryGBK2BIG5[i] = dis.readChar();
            }
            dis.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Load data file to build the mapping table from GBK to BIG5
    private static void saveMappinggbk2big5() {
        try {
            OutputStream os = new FileOutputStream(
                "D:\\workspace\\Sanguo-Editor1.0\\src\\com\\pip\\sanguo\\data\\i18n\\gbk2big5.data");
            DataOutputStream dos = new DataOutputStream(os);
            for (int i = 0; i < 65536; i++) {
                dos.writeChar(arrDictionaryGBK2BIG5[i]);
            }
            dos.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }    

    /**
     * Convert a string in BIG5 to GBK. If a character in the string is not in
     * BIG5, it will be kept.
     */
    public static String convert(String big5Text) {
        int big5Char = 0;
        StringBuffer textBuffer = new StringBuffer();
        for (int i = 0; i < big5Text.length(); i++) {
            big5Char = big5Text.charAt(i) & 0xffff;

            textBuffer.append(arrDictionary[big5Char]);
        }
        return textBuffer.toString();
    }

    /**
     * Convert a string in GBK to BIG5. If a character in the string is not in
     * GBK, it will be kept.
     */
    public static String convertGB2BIG5(String gbText) {
        int big5Char = 0;
        StringBuffer textBuffer = new StringBuffer();
        for (int i = 0; i < gbText.length(); i++) {
            big5Char = gbText.charAt(i) & 0xffff;
            textBuffer.append(arrDictionaryGBK2BIG5[big5Char]);
        }
        return textBuffer.toString();
    }
    
    public static void main(String[] args) {
//        arrDictionaryGBK2BIG5['в╒' & 0xFFFF] = 'т]';
//        arrDictionaryGBK2BIG5['сн' & 0xFFFF] = 'ъ[';
//        saveMappinggbk2big5();
        System.out.println(arrDictionaryGBK2BIG5['в╒' & 0xFFFF]);
        System.out.println(arrDictionaryGBK2BIG5['сн' & 0xFFFF]);
    }
}
