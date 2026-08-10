package com.pip.itimes.utils;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Parser使用ILineDecoder解析log文本，解析时将会依次尝试使用decoders,直到有一个满足要求
 * @author Jeffrey
 * @version 1.0
 */
public class LogParser {


    private ILineDecoder[] decoders = null;
    private IVisitor visitor = null;
    private Reader in;
    private int indent;
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd|HH:mm:ss"); //2007-02-01|16:32:43

    //log文件以及每行跳过的字符数
    public LogParser(Reader in,int indent) {
        this.in = in;
        this.indent = indent;
    }

    public void setILineDecoders(ILineDecoder[] decoders){
        this.decoders = decoders;
    }

    public void setVisitor(IVisitor visitor){
        this.visitor = visitor;
    }

    public void parse() throws Exception{
        BufferedReader br = new BufferedReader(in);
        String line = null;
        while((line=br.readLine())!=null){
            String sTime = line.substring(indent,indent+19);
            Date time = format.parse(sTime);
            String s = line.substring(indent+19);
            for(int i=0;i<decoders.length;i++){
                if(decoders[i].match(s,time,visitor)){
                    break;
                }
            }
        }
    }

}
