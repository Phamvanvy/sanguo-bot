/**
 * @author leo
 */
package com.pip.log.define;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.SubnodeConfiguration;

import com.pip.log.define.processor.LogProcessor;

/**
 * @author leo
 *
 */
public class LogDefine{
    private String id;
    private String timeRegex;
    private String playerIdRegex;
    private Pattern timePattern;
    private Pattern playerIdPattern;
    private SimpleDateFormat inputDateFormat;
    private SimpleDateFormat outputDateFormat;

    private ConcurrentHashMap<String, LogProcessor> id2processor = new ConcurrentHashMap<String, LogProcessor>();
    private ConcurrentHashMap<String, LogItem> id2item = new ConcurrentHashMap<String, LogItem>();
    private ConcurrentHashMap<String, LogType> id2logType = new ConcurrentHashMap<String, LogType>();

    public LogDefine(SubnodeConfiguration node){
        id = node.getString("id");
        timeRegex = node.getString("timeRegex");
        inputDateFormat = new SimpleDateFormat(node.getString("timeInputFormat"));
        outputDateFormat = new SimpleDateFormat(node.getString("timeOutputFormat"));
        playerIdRegex = node.getString("playerIdRegex");
        timePattern = Pattern.compile(timeRegex);
        playerIdPattern = Pattern.compile(playerIdRegex);
    }

    public String getId(){
        return id;
    }

    public Matcher getTimeMatcher(String log){
        return timePattern.matcher(log);
    }

    public Matcher getPlayerIdMatcher(String log){
        return playerIdPattern.matcher(log);
    }

    public Date parseLogDate(String dateString){
        try{
            return inputDateFormat.parse(dateString);
        }catch(ParseException e){
            e.printStackTrace();
        }

        return null;
    }

    public String formatLogDate(Date date){
        return outputDateFormat.format(date);
    }

    public void addLogProcessor(String id, LogProcessor processor) throws Exception{
        if(id2processor.containsKey(id)){
            throw new Exception("LogProcessor duplicated : " + id);
        }

        id2processor.put(id, processor);
    }

    public void addLogItem(String id, LogItem item) throws Exception{
        if(id2item.containsKey(id)){
            throw new Exception("LogItem duplicated : " + id);
        }

        id2item.put(id, item);
    }

    public void addLogType(String id, LogType type) throws Exception{
        if(id2logType.containsKey(type)){
            throw new Exception("LogType duplicated : " + id);
        }

        id2logType.put(id, type);
    }

    public LogProcessor getProcessor(String id){
        return id2processor.get(id);
    }

    public LogItem getItem(String id){
        return id2item.get(id);
    }

    public LogType findLogType(String log){
        Iterator<String> it = id2logType.keySet().iterator();

        while(it.hasNext()){
            LogType type = id2logType.get(it.next());

            if(type.bingle(log)){
                return type;
            }
        }

        return null;
    }
}
