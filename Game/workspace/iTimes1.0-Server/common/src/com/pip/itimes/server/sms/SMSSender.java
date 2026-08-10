package com.pip.itimes.server.sms;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.concurrent.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class SMSSender {

    private static final Logger log = Logger.getLogger(SMSSender.class);

    private ThreadPoolExecutor executor;

    public SMSSender(int size) {
        executor = new ThreadPoolExecutor(1, size, 60L, TimeUnit.SECONDS,
                                          new LinkedBlockingQueue());
    }

    public void send(String phone, String s,String service) {
        log.info(s);
        Sms sms = new Sms(phone, s,service);
        Sender sender = new Sender(sms);
        executor.execute(sender);
//        log.info("execute");
    }

//    public static void main(String[] args) {
//        SMSSender sender = new SMSSender(5);
////        String msisdn = args[0];
////        String username = args[1];
////        String password = args[2];
////        String msg ="你好";
//        String msg = "恭喜您幻想i时代,帐户名:" + ",密码:" + "0000000" + ".祝您游戏愉快.客服电话：010-64465123.本条免费";
//        sender.send("13501105162", msg,"0738A0000I");
//    }

}


class Sms {

    private String content = null;
    private String phone = null;

    private String service = null;
    private Date date = null;

    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");


    public Sms(String phone, String content,String service) {
        this.phone = phone;
        this.content = content;
        this.service = service;
        this.date = new Date();
    }

    public String getPhone(){
        return phone;
    }

    public String getContent(){
        return content;
    }


    public String getDateString(){
        return format.format(date);
    }


    public String getPostString() {
        StringBuffer buff = new StringBuffer(400);
        buff.append("<linkrich-mt>\n");
        buff.append("<version>200</version>\n");
        buff.append("<customer>CP738</customer>\n");
        buff.append("<pwd>738CP</pwd>\n");
        buff.append("<service>");
        buff.append(service);
        buff.append("</service>\n");
        buff.append("<srcmobile>");
        buff.append(phone);
        buff.append("</srcmobile>\n");
        buff.append("<source>8002738</source>\n");
        buff.append("<destmobile>");
        buff.append(phone);
        buff.append("</destmobile>\n");
        buff.append("<type>16</type>\n");
        buff.append("<pid>0</pid>\n");
        buff.append("<udhi>1</udhi>\n");
        buff.append("<msgid>0000000000</msgid>\n");
        buff.append("<time>");
        buff.append(format.format(date));
        buff.append("</time>\n");
//        buff.append("<time>0701261200</time>\n");
        buff.append("<message>");
        buff.append(content);
        buff.append("</message>\n");
        buff.append("</linkrich-mt>");
        return buff.toString();
    }
}


class Sender implements Runnable {

    private static final Logger log = Logger.getLogger(SMSSender.class);
//    private static final String url = "http://59.151.15.10:16002";
    private static final String url = "http://211.144.155.130/smsclientinterface/send.asp";

    private Sms sms;

    public Sender(Sms sms) {
        this.sms = sms;
    }

    public void run() {

//        GetMethod get = new GetMethod(url+"?circle=zsmz&pwd=123456&mobile="+sms.getPhone()+"&message="+sms.getContent());
        PostMethod post = new PostMethod(url);
        try {
//            log.info("start to send sms");
//            String s = sms.getPostString();
            post.addRequestHeader( "Connection", "close");
            post.addRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=GBK");
            post.addParameter("circle","zsmz");
            post.addParameter("pwd","123456");
            post.addParameter("mobile",sms.getPhone());
            post.addParameter("message",sms.getContent());
//            post.setRequestEntity(new StringRequestEntity(s,
//                    "text/xml", "GBK"));
            log.info("Phone["+sms.getPhone()+"]Content["+sms.getContent()+"]");
            HttpClient httpclient = new HttpClient();
            int result = httpclient.executeMethod(post);
//            int result = httpclient.executeMethod(get);
//            log.info(s);
            log.info("Response status code: " + result);
            log.info("Response body: ");
            log.info(post.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error(ex,ex);
            post.releaseConnection();
        }
    }
}
