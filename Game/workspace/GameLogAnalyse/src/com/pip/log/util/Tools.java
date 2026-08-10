package com.pip.log.util;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools{
    public static final Pattern numberPattern = Pattern.compile("\\d+");

    /**
     * 把一个字符串按指定分隔符分段。
     * @param s 原始字符串
     * @param ch 分隔符
     * @return 分出的段的数组
     */
    public static String[] splitString(String s, char ch){
        int startIndex = 0;
        int endIndex = 0;
        Vector<String> vS = new Vector<String>();

        while(true){
            endIndex = s.indexOf(ch, startIndex);

            if(endIndex == -1){
                String tmp = s.substring(startIndex);

                if(tmp.length() > 0){
                    vS.addElement(tmp);
                }

                break;
            }else{
                vS.addElement(s.substring(startIndex, endIndex));
                startIndex = endIndex + 1;
            }
        }

        String[] strs = new String[vS.size()];
        vS.copyInto(strs);

        return strs;
    }

    public static int getNumber(String input){
        Matcher matcher = numberPattern.matcher(input);

        if(matcher.find()){
            return Integer.parseInt(matcher.group());
        }

        return -1;
    }
}
