package com.pip.server.auth.cmcc.billing;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.server.auth.AccountState;
import com.pip.server.auth.ConnectService;
import com.pip.server.auth.FeeService;
import com.pip.server.auth.NoEnoughBalanceException;
import com.pip.server.auth.cmcc.CmccBackdoorServlet;
import com.pip.server.auth.cmcc.CmccConsumeItem;
import com.pip.server.auth.cmcc.CmccConsumeRecord;
import com.pip.server.auth.cmcc.CmccException;
import com.pip.server.auth.cmcc.CmccService;
import com.pip.server.auth.cmcc.CmccUserCache;
import com.pip.server.auth.cmcc.CmccUserKey;
import com.pip.server.auth.net.AccountConstants;
import com.pip.server.auth.net.UWAPSegment;

/**
 * 待计费服务。
 * @author lighthu
 */
public class BillingService implements Runnable {
    private static final Logger log = Logger.getLogger(BillingService.class);
    private ConnectService connectService;
    private CmccUserCache cache;
    private CmccService cmccService;
    private FeeService feeService;
    private File configFile;
    private HashMap<String, Channel> channels;
    private long lastModified;
    private boolean stopped = false;
    private Thread workingThread;
    private Thread syncThread;
    
    private ArrayList<String> syncRequests = new ArrayList<String>();
    
    public BillingService(ConnectService connService, CmccService cmccService, 
            CmccUserCache cache, FeeService feeService) throws Exception {
        this.connectService = connService;
        this.cmccService = cmccService;
        this.cache = cache;
        this.feeService = feeService;
        configFile = new File("channel.xml");
        loadConfig();
        workingThread = new Thread(this);
        workingThread.start();
        syncThread = new SyncThread();
        syncThread.start();
    }
    
    public void shutdown() {
        stopped = true;
        workingThread.interrupt();
        syncThread.interrupt();
    }
    
    public void run() {
        while (!stopped) {
            try {
                Thread.sleep(60000L);
            } catch (Exception e) {
            }
            if (configFile.lastModified() != lastModified) {
                try {
                    loadConfig();
                } catch (Exception e) {
                    log.error(e, e);
                }
            }
        }
    }

    /**
     * 支付成功通知。
     */
    private class SyncThread extends Thread {
        public void run() {
           while (!stopped) {
               try {
                   String url = null;
                   synchronized (syncRequests) {
                       if (syncRequests.size() == 0) {
                           syncRequests.wait();
                       }
                       if (syncRequests.size() > 0) {
                           url = syncRequests.remove(0);
                       }
                   }
                   if (url != null) {
                       GetMethod method = new GetMethod(url);
                       method.addRequestHeader( "Connection", "close");
                       try {
                           HttpClient httpclient = new HttpClient();
                           httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
                           httpclient.getParams().setSoTimeout(30000);
                           int code = httpclient.executeMethod(method);
                           log.info("[CHANNEL_BUY_NOTIFY]URL[" + url + "]CODE[" + code + "]");
                       } catch (Exception ex) {
                           log.error(ex, ex);
                       } finally {
                           method.releaseConnection();
                       }
                   }
               } catch (Throwable e) {
               }
            }
        }
    }
    
    /*
     * 载入配置。
     */
    protected void loadConfig() throws Exception {
        SAXReader reader = new SAXReader();
        Document doc = reader.read(configFile);
        Element root = doc.getRootElement();
        Iterator itor = root.elementIterator("channel");
        channels = new HashMap<String, Channel>();
        while (itor.hasNext()) {
            Element elem = (Element)itor.next();
            Channel chan = new Channel();
            chan.id = Integer.parseInt(elem.attributeValue("id"));
            chan.name = elem.attributeValue("name");
            chan.password = elem.attributeValue("password");
            
            // IP列表
            String ipList = elem.attributeValue("ip");
            String[] ips = ipList.split(",");
            for (String ip : ips) {
                ip = ip.trim();
                if (ip.length() > 0) {
                    chan.allowIP.add(ip);
                }
            }
            
            // 消费代码表
            Iterator itor2 = elem.elementIterator("consumecode");
            while (itor2.hasNext()) {
                Element elem2 = (Element)itor2.next();
                String code = elem2.attributeValue("id");
                String realCode = elem2.attributeValue("value");
                chan.allowCode.put(code, realCode);
            }

            chan.syncURL = elem.attributeValue("syncurl");
            channels.put(chan.name, chan);
        }
        lastModified = configFile.lastModified();
    }
    
    /**
     * 渠道购买道具。
     * @param channelName 渠道登录名
     * @param password 渠道密码
     * @param ip 渠道访问IP
     * @param userID 用户ID
     * @param code 渠道消费代码（需映射为实际消费代码）
     * @return 如果成功，返回消费的金额（分）
     * @exception 如果失败，抛出异常。
     */
    public int channelBuy(String channelName, String password, String ip, String userId, String code, String info) throws CmccException {
        // 检查参数
        Channel chan = channels.get(channelName);
        if (chan == null) {
            throw new CmccException("用户名或密码错误。");
        }
        String consumeCode = chan.check(password, ip, code);
        
        try {
            log.info("[CHANNEL_BUY]CHANNEL[" + channelName + "]consumeCode[" + consumeCode + "]TRY");
            
            // 检查是否已登录平台
            CmccUserKey userKey = cache.getUserKey(userId);
            if (userKey == null) {
                throw new CmccException("用户未登录");
            }
            cache.activeUserKey(userKey);
            
            // 检查购买上限
            int price = cmccService.getPrice(consumeCode);
            int monthConsume = cache.getConsumeAmount(userKey.getUserId());
            if (monthConsume + price > 10000) {
                throw new CmccException("已达到本月购买上限");
            }
            int dayConsume = cache.getBuyAmount(userKey.getUserId(), 86400000L);
            if (dayConsume + price > 2000) {
                throw new CmccException("已达到当日购买上限");
            }
            
            // 发起购买请求
            cmccService.buyGameTool(userKey.getUserId(), consumeCode, "1000");
            log.info("[CHANNEL_BUY]CHANNEL[" + channelName + "]consumeCode[" + consumeCode + "]PRICE[" + price + "]OK");
            
            // 购买成功，向Fee表插入一条记录，添加当月消费数据
            feeService.newChargedFee(-chan.id, cmccService.getPrice(consumeCode), "CMCC_" + userKey.getUserId());
            cache.addConsumeAmount(userKey.getUserId(), price);
            
            // 安排同步信息
            if (chan.syncURL != null && chan.syncURL.length() > 0) {
                String fullURL = chan.syncURL + URLEncoder.encode(info, "UTF-8");
                synchronized (syncRequests) {
                    syncRequests.add(fullURL);
                    syncRequests.notify();
                }
            }
            return price;
        } catch (CmccException ex) {
            throw ex;
        } catch (Exception e) {
            log.error(e, e);
            throw new CmccException("系统错误");
        }        
    }
}
