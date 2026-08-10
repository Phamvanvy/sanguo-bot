package com.pip.server.billing.security;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailUtil implements Runnable {

    // private BlockingQueue queue = LinkedBlockingQueue<E>

    protected String mailServer;
    protected String account;
    protected String password;

    protected BlockingQueue<Mail> queue = new LinkedBlockingQueue<Mail>();

    protected boolean running;

    private static MailUtil instance;

    protected Properties properties = new Properties();
    protected Authenticator auth = null;

    private MailUtil(String mailServer, String account, String password) {
        this.mailServer = mailServer;
        this.account = account;
        this.password = password;
        properties.put("mail.smtp.host", mailServer);
        properties.put("mail.smtp.auth", "true");
        auth = new DefaultAuthenticator(account, password);

    }

    public static void init(String mailServer, String account, String password) {
        instance = new MailUtil(mailServer, account, password);
    }

    public static void send(Mail mail) {
        instance.queue.add(mail);
    }

    public static void start() {
        instance.running = true;
        new Thread(instance).start();
    }

    public void sendMail(Mail mail) throws Exception {
        Session s = null;
        Transport transport = null;
        try {
            s = Session.getInstance(properties);
            MimeMessage message = new MimeMessage(s);
            message.setFrom(new InternetAddress(mail.getFrom()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.getTo(), false));
            message.setSubject(mail.getSubject(), "gb2312");
            
            if (mail.getHTML() == null) {
                // 纯文本邮件
                message.setText(mail.getMessage(), "gb2312");
            } else {
                // HTML邮件
                MimeBodyPart bodyPart = new MimeBodyPart();
                bodyPart.setContent(mail.getMessage(), "text/plain;charset=gb2312");
                MimeMultipart mp = new MimeMultipart();
                mp.setSubType("alternative");
                mp.addBodyPart(bodyPart);
                bodyPart = new MimeBodyPart();
                bodyPart.setContent(mail.getHTML(), "text/html;charset=gb2312");
                mp.addBodyPart(bodyPart);
                message.setContent(mp);
            }
            
            transport = s.getTransport("smtp");
            transport.connect(mailServer, account, password);
            transport.sendMessage(message, InternetAddress.parse(mail.getTo(), false));
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public void run() {
        while (running) {
            try {
                Mail mail = queue.take();
                sendMail(mail);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // 下面是测试代码

    protected static final String MAIL_BIND = "尊敬的用户，您好，\n\n您已申请把您的明珠通行证%s绑定到邮箱%s,请点击下面的链接完成绑定：\n\nhttp://%s/bindconfirm?uid=%s\n\n如果上面的链接无法点击，您也可以将此链接复制，并粘贴到您浏览器的地址栏内，然后访问该页面完成邮件绑定服务。\n\n本邮件为系统邮件，请勿回复，祝您游戏愉快。\n\n掌上明珠运营中心\n客服电话：010-59787888";
    protected static final String MAIL_BIND_HTML = 
        "<html><head><META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html;charset=gb2312\"></head>" +
        "<body>尊敬的用户，您好，<br><br>您已申请把您的明珠通行证%s绑定到邮箱%s,请点击下面的链接完成绑定：<br><br>" +
        "<a href=\"http://%s/bindconfirm?uid=%s\">http://%s/bindconfirm?uid=%s</a><br><br>" +
        "<br><br>如果上面的链接无法点击，您也可以将此链接复制，并粘贴到您浏览器的地址栏内，然后访问该页面完成邮件绑定服务。<br><br>本邮件为系统邮件，请勿回复，祝您游戏愉快。<br><br>掌上明珠运营中心<br>客服电话：010-59787888<br>本邮件为系统邮件，请勿回复，祝您游戏愉快。</body></html>";

    private static Mail getBindMail(String to, String name, String bindMail,
            String randomString) {
        Mail m = new Mail("bind_" + randomString + "@pipgame.mobi", to, "明珠通行证 - 邮箱绑定", String.format(MAIL_BIND,
                name, bindMail, "http://192.168.0.1" + ":" + 7001,
                randomString));
        String html = String.format(MAIL_BIND_HTML,
                name, bindMail, "http://192.168.0.1" + ":" + 7001,
                randomString, "http://192.168.0.1" + ":" + 7001, randomString);
        m.setHTML(html);
        return m;
    }
    
    public static void main(String args[]) throws Exception {
        Mail m = new Mail("test@pearlinpalm.com", "light.hu@pearlinpalm.com", "测试消息", 
                "恭喜你成功注册，请点击下面的链接登录\nhttp://www.sina.com.cn。");
        m.setHTML("<html><head><META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html;charset=gb2312\"></head>" +
                "<body>恭喜你成功注册，请点击下面的链接登录<br><a href=\"http://www.sina.com.cn\">新浪</a></body></html>");
        Mail mm = getBindMail("lighthu@gmail.com", "测试帐号", "lighthu@gmail.com", "aaa");
        new MailUtil("mails.pearlinpalm.com", "addressbak", "AddressBak071105").sendMail(mm);
    }
}
