package com.pip.log.define;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.SubnodeConfiguration;

import com.pip.log.define.processor.LogProcessor;

public class LogItem{
    private String id;
    private String regex;
    private String replaceBegin;
    private String replaceEnd;
    private String dataMask;
    private LogProcessor processor;

    private Pattern regexPattern;
    private Pattern maskPattern;

    private LogItem(String id, String regex, String replaceBegin, String replaceEnd, String dataMask, LogProcessor processor){
        this.id = id;
        this.regex = regex;
        this.replaceBegin = replaceBegin;
        this.replaceEnd = replaceEnd;
        this.dataMask = dataMask;
        this.processor = processor;

        regexPattern = Pattern.compile(this.regex);
        maskPattern = Pattern.compile(this.dataMask);
    }

    public String getId(){
        return id;
    }

    public String process(String log){
        String result = log;

        Matcher regexMatcher = regexPattern.matcher(log);

        if(regexMatcher.find()){
            String itemData = regexMatcher.group();
            Matcher maskMatcher = maskPattern.matcher(itemData);

            if(maskMatcher.find()){
                itemData = processor.process(maskMatcher.replaceAll(""));
            }

            result = regexMatcher.replaceFirst(replaceBegin + itemData + replaceEnd);
        }

        return result;
    }

    public static final LogItem loadItem(LogDefine define, SubnodeConfiguration node) throws Exception{
        String id = node.getString("id");
        String regex = node.getString("regex");
        String replaceBegin = node.getString("replacebegin");
        String replaceEnd = node.getString("replaceend");
        String dataMask = node.getString("dataMask");
        String pId = node.getString("processor");
        LogProcessor processor = define.getProcessor(pId);

        return new LogItem(id, regex, replaceBegin, replaceEnd, dataMask, processor);
    }
}