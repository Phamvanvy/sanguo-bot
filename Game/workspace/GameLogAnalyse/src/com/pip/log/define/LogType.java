package com.pip.log.define;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.SubnodeConfiguration;

public class LogType{
    private String id;
    private String regex;
    private String replace;
    private List<LogItem> items;
    private List<ReplaceData> replaceDatas;

    private Pattern regexPattern;

    public LogType(String id, String regex, String replace){
        this.id = id;
        this.regex = regex;
        this.replace = replace;
        this.regexPattern = Pattern.compile(this.regex);
        this.items = new ArrayList<LogItem>();
        this.replaceDatas = new ArrayList<ReplaceData>();
    }

    private void addItem(LogItem item){
        items.add(item);
    }

    public String getId(){
        return id;
    }

    public boolean bingle(String log){
        Matcher matcher = regexPattern.matcher(log);

        return matcher.find();
    }

    public String process(String log){
        String result = log;
        Matcher matcher = regexPattern.matcher(log);

        if(matcher.find()){
            result = matcher.replaceFirst(replace);
        }

        //处理logitem
        for(LogItem item : items){
            result = item.process(result);
        }

        //处理replacedata
        for(ReplaceData replaceData : replaceDatas){
            result = replaceData.process(result);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static LogType loadType(LogDefine define, SubnodeConfiguration node){
        String id = node.getString("id");
        String regex = node.getString("regex");
        String replace = node.getString("replace");
        String[] itemNames = node.getStringArray("item");

        LogType type = new LogType(id, regex, replace);

        for(String itemName : itemNames){
            type.addItem(define.getItem(itemName));
        }

        List<SubnodeConfiguration> list = node.configurationsAt("replacedata");

        for(SubnodeConfiguration node1 : list){
            ReplaceData replaceData = new ReplaceData(node1.getString("regex"), node1.getString("replace"));
            type.replaceDatas.add(replaceData);
        }

        return type;
    }
}

class ReplaceData{
    private String regex;
    private String replace;

    private Pattern regexPattern;

    public ReplaceData(String regex, String replace){
        this.regex = regex;
        this.replace = replace;
        this.regexPattern = Pattern.compile(this.regex);
    }

    public String process(String log){
        String result = log;
        Matcher matcher = regexPattern.matcher(log);

        if(matcher.find()){
            result = matcher.replaceFirst(replace);
        }

        return result;
    }
}
