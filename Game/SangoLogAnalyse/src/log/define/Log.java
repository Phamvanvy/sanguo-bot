package log.define;

import java.util.Date;
import java.util.regex.Matcher;


public class Log{
    private Date logTime;
    private String originalLog;
    private String printLog;
    private LogDefine define;

    public Log(LogDefine define,String originalLog){
        this.define = define;
        this.originalLog = originalLog;
        this.printLog = originalLog;
    }

    public String getOriginalLog(){
        return originalLog;
    }

    public void process(){
        try{
            //处理日期
            Matcher timeMatcher = define.getTimeMatcher(printLog);
            if(timeMatcher.find()){
                logTime = define.parseLogDate(timeMatcher.group());
            }
            printLog = timeMatcher.replaceFirst("");

            //处理类型
            LogType type = define.findLogType(printLog);
            if(type != null){
                printLog = type.process(printLog);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(define.formatLogDate(logTime));
//        sb.append(" ID[");
//        sb.append(playerId);
//        sb.append("]：");
        sb.append(": ");

        return sb.toString() + printLog.toString();
    }
}
