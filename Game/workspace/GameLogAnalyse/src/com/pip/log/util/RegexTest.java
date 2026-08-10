package com.pip.log.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest{
    public static void go(){
        //        //查找以Java开头,任意结尾的字符串
        //        Pattern pattern = Pattern.compile("^Java.*");
        //        Matcher matcher = pattern.matcher("Java不是人");
        //        boolean b = matcher.matches();
        //        //当条件满足时，将返回true，否则返回false
        //        System.out.println(b);

        //        //以多条件分割字符串时
        //        Pattern pattern = Pattern.compile("[, |]+");
        //        String[] strs = pattern.split("Java Hello World  Java,Hello,,World|Sun");
        //        for(int i = 0; i < strs.length; i++){
        //            System.out.println(strs[i]);
        //        }

        //        //文字替换（首次出现字符）
        //        Pattern pattern = Pattern.compile("正则表达式");
        //        Matcher matcher = pattern.matcher("正则表达式 Hello World,正则表达式 Hello World");
        //        //替换第一个符合正则的数据
        //        System.out.println(matcher.replaceFirst("Java"));

        //        //文字替换（全部）
        //        Pattern pattern = Pattern.compile("正则表达式");
        //        Matcher matcher = pattern.matcher("正则表达式 Hello World,正则表达式 Hello World");
        //        //替换第一个符合正则的数据
        //        System.out.println(matcher.replaceAll("Java"));

        //        //文字替换（置换字符）
        //        Pattern pattern = Pattern.compile("正则表达式");
        //        Matcher matcher = pattern.matcher("正则表达式 Hello World,正则表达式 Hello World ");
        //        StringBuffer sbr = new StringBuffer();
        //        while(matcher.find()){
        //            matcher.appendReplacement(sbr, "Java");
        //        }
        //        matcher.appendTail(sbr);
        //        System.out.println(sbr.toString());

        //        //验证是否为邮箱地址
        //        String str = "ceponline@yahoo.com.cn";
        //        Pattern pattern = Pattern.compile("[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+", Pattern.CASE_INSENSITIVE);
        //        Matcher matcher = pattern.matcher(str);
        //        System.out.println(matcher.matches());

        //        //去除html标记
        //        Pattern pattern = Pattern.compile("<.+?>", Pattern.DOTALL);
        //        Matcher matcher = pattern.matcher("<a href=\"index.html\">主页</a>");
        //        String string = matcher.replaceAll("");
        //        System.out.println(string);

        //        Pattern pattern = Pattern.compile("Changed\\[[0-9A-F ]+\\]");
        //        Matcher matcher = pattern
        //                        .matcher("Changed[01 03 05 00 00 00 2D 06 00 00 00 05 19 00 00 00 01 02 01 00 00 00 00 05 00 0F E5 B0 8F E7 94 9F E5 91 BD E8 8D AF E5 89 82 01 00 00 00 00 32 01 0A 02 00 0F 44 E5 00 61 5A 77 00 77 00 0F 43 E4 00 61 5C B2 00 3B ]");
        //        System.out.println(matcher.find());
        //        String string = matcher.replaceAll("");
        //        System.out.println(string);

        //        //查找html中对应条件字符串
        //        Pattern pattern = Pattern.compile("href=\"(.+?)\"");
        //        Matcher matcher = pattern.matcher("<a href=\"index.html\">主页</a>");
        //        if(matcher.find()){
        //            System.out.println(matcher.group(1));
        //        }

        //        //截取http://地址
        //        //截取url
        //        Pattern pattern = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:]+");
        //        Matcher matcher = pattern.matcher("dsdsds<http://dsds//gfgffdfd>fdf");
        //        StringBuffer buffer = new StringBuffer();
        //        while(matcher.find()){
        //            buffer.append(matcher.group());
        //            buffer.append("\r\n");
        //            System.out.println(buffer.toString());
        //        }

        //        //替换指定{}中文字
        //        String str = "Java目前的发展史是由{0}年-{1}年";
        //        String[][] object = {
        //                        new String[]{
        //                                        "\\{0\\}", "1995"
        //                        }, new String[]{
        //                                        "\\{1\\}", "2007"
        //                        }
        //        };
        //        System.out.println(replace(str, object));

        //        FilesAnalyze.output("C:\\", "[A-z|.]*");

        //        Pattern pattern = Pattern.compile("(?!TRY).{3}$");
        //        Matcher matcher = pattern.matcher("2009-09-01|10:15:56ID[173886],TYPE[33],UseType[1]ItemType[2]TRY1");
        //        if(matcher.find()){
        //            System.out.println(matcher.find());
        //            System.out.println(matcher.group());
        //        }
    }

    private static String replace(final String sourceString, Object[] object){
        String temp = sourceString;
        for(int i = 0; i < object.length; i++){
            String[] result = (String[]) object[i];
            Pattern pattern = Pattern.compile(result[0]);
            Matcher matcher = pattern.matcher(temp);
            temp = matcher.replaceAll(result[1]);
        }
        return temp;
    }
}
