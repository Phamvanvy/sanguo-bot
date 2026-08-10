package com.pip.server.auth.cmcc;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import com.pip.server.auth.NoEnoughBalanceException;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 卓望网游平台接口服务。
 */
public class CmccService implements Runnable {
    private static final Logger log = Logger.getLogger(CmccService.class);

    /*
     * 用户标识类型：固定为3（用户ID）
     */
    private static final String USERTYPE = "3";
    /*
     * 充值CPID：所有游戏都相同
     */
    private static final String CHARGECPID = "701001";
    /*
     * 充值渠道ID：所有游戏都相同
     */
    private static final String CHARGECHANNELID = "15077000";
    /*
     * 1-50元充值代码：所有游戏都相同
     */
    private static final String[] CHARGE_TABLE = { 
            "400120001000", "400120002000", "400120003000", "400120004000", "400120005000", 
            "400120006000", "400120007000", "400120008000", "400120009000", "400120010000",
            "400120011000", "400120012000", "400120013000", "400120014000", "400120015000", 
            "400120016000", "400120017000", "400120018000", "400120019000", "400120020000", 
            "400120021000", "400120022000", "400120023000", "400120024000", "400120025000", 
            "400120026000", "400120027000", "400120028000", "400120029000", "400120030000", 
            "400120031000", "400120032000", "400120033000", "400120034000", "400120035000", 
            "400120036000", "400120037000", "400120038000", "400120039000", "400120040000",
            "400120041000", "400120042000", "400120043000", "400120044000", "400120045000", 
            "400120046000", "400120047000", "400120048000", "400120049000", "400120050000", 
            "400120051000", "400120052000", "400120053000", "400120054000", "400120055000", 
            "400120056000", "400120057000", "400120058000", "400120059000", "400120060000",
            "400120061000", "400120062000", "400120063000", "400120064000", "400120065000", 
            "400120066000", "400120067000", "400120068000", "400120069000", "400120070000", 
            "400120071000", "400120072000", "400120073000", "400120074000", "400120075000", 
            "400120076000", "400120077000", "400120078000", "400120079000", "400120080000", 
            "400120081000", "400120082000", "400120083000", "400120084000", "400120085000", 
            "400120086000", "400120087000", "400120088000", "400120089000", "400120090000",
            "400120091000", "400120092000", "400120093000", "400120094000", "400120095000", 
            "400120096000", "400120097000", "400120098000", "400120099000", "400120100000" 
    };
    /*
     * 幻想武林所有消费代码的配置。
     */
    private String[][] CONSUME_CODES;
    /*
     * 幻想武林所有消费代码及其价格，key是消费代码，value是点数价格
     */
    private Map<String, Integer> ALL_CONSUME_CODES = new HashMap<String, Integer>();
    /*
     * 序列号生成器
     */
    private static AtomicInteger serialGen = new AtomicInteger(1000001);
    /*
     * 时间格式
     */
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    
    /*
     * 平台URL
     */
    private String url;
    /*
     * 平台参数：服务器ID 幻想：301 武林：301
     */
    private String sender = "";
    /*
     * 平台参数：渠道ID 幻想：15001001 武林：15081000
     */
    private String channelId = "";
    /*
     * 平台参数：CPID 幻想：C00005 武林：C00081
     */
    private String cpId = "";
    /*
     * 平台参数：服务ID 幻想：120120433000 武林：120121811000
     */
    private String cpServiceId = "";
    /*
     * 协议版本号
     */
    private String protocolVersion = "1_0_0";
    /*
     * 查询余额接口
     */
    private String CMCC_QUERYBALANCE;
    /*
     * 充值接口
     */
    private String CMCC_CHARGEUP;
    /*
     * 购买接口
     */
    private String CMCC_BUYGAMETOOL;
    /*
     * 查询充值记录接口
     */
    private String CMCC_QUERYCHARGE;
    /*
     * 查询消费记录接口
     */
    private String CMCC_QUERYCONSUME;
    /*
     * 查询充值消费记录新接口
     */
    private String CMCC_QUERY_NEW;
    /*
     * 用户登出通知接口
     */
    private String CMCC_LOGOUT;
    /*
     * 用户推荐接口
     */
    private String CMCC_RECOMMEND;
    /*
     * 订购业务接口
     */
    private String CMCC_SUBSCRIBE = "http://gmc.i139.cn/bizassistant/BusiTransactServlet";
    
    /*
     * 特殊消费代码，用于测试
     */
    private static final String ZERO = "000000000000";
    
    /*
     * 配置自动重载线程。
     */
    private Thread workingThread;
    /*
     * 配置文件。
     */
    private File configFile;
    /*
     * 文件修改时间。
     */
    private long configModifiedTime;
    /*
     * 是否停止运行
     */
    private boolean stopped = false;

    public CmccService(String url, String sender, String channelId, String cpId, 
            String cpServiceId, String ver) {
        
        // 载入配置
        configFile = new File("cmcc_config.xml");
        try {
            loadConfig();
            configModifiedTime = configFile.lastModified();
        } catch (Exception e) {
            log.error(e, e);
        }

        this.url = url;
        this.sender = sender;
        this.channelId = channelId;
        this.cpId = cpId;
        this.cpServiceId = cpServiceId;
        if (ver != null) {
            protocolVersion = ver;
        }
        if (url.contains("test")) {
            CMCC_QUERYBALANCE = url + "QueryBalance.jsp";
            CMCC_CHARGEUP = url + "ChargeUp.jsp";
            CMCC_BUYGAMETOOL = url + "BuyGameTool.jsp";
            CMCC_QUERYCHARGE = url + "QueryChargeUpRecord.jsp";
            CMCC_QUERYCONSUME = url + "QueryConsumeList.jsp";
            CMCC_QUERY_NEW = url + "QueryConsumeRecord.jsp";
            CMCC_LOGOUT = url + "LogoutOnlineGame.jsp";
            CMCC_RECOMMEND = url + "Recommend.jsp";
        } else {
            CMCC_QUERYBALANCE = url + "QueryBalance";
            CMCC_CHARGEUP = url + "ChargeUp";
            CMCC_BUYGAMETOOL = url + "BuyGameTool";
            CMCC_QUERYCHARGE = url + "QueryChargeUpRecord";
            CMCC_QUERYCONSUME = url + "QueryConsumeList";
            CMCC_QUERY_NEW = url + "QueryConsumeRecord";
            CMCC_LOGOUT = url + "LogoutOnlineGame";
            CMCC_RECOMMEND = url + "R";
        }
        log.info(CMCC_QUERYBALANCE);
        log.info(CMCC_CHARGEUP);
        log.info(CMCC_BUYGAMETOOL);
        log.info(CMCC_QUERYCHARGE);
        log.info(CMCC_QUERYCONSUME);
        log.info(CMCC_QUERY_NEW);
        log.info(CMCC_LOGOUT);
        log.info(CMCC_RECOMMEND);
        
        // 启动自动载入配置的线程
        workingThread = new Thread(this);
        workingThread.start();
    }
    
    /*
     * 从配置文件中载入消费代码。
     */
    private void loadConfig() throws Exception {
        SAXReader reader = new SAXReader();
        FileInputStream fis = new FileInputStream(configFile);
        Document doc = reader.read(fis);
        fis.close();
        
        List<String[]> items = new ArrayList<String[]>();
        Iterator itor = doc.getRootElement().elementIterator("item");
        while (itor.hasNext()) {
            Element elem = (Element)itor.next();
            String code = elem.attributeValue("consumecode");
            String name = elem.attributeValue("name");
            String price = elem.attributeValue("price");
            items.add(new String[] { code, name, price });
        }
        CONSUME_CODES = new String[items.size()][];
        items.toArray(CONSUME_CODES);
        
        ALL_CONSUME_CODES.clear();
        ALL_CONSUME_CODES.put("", 0);
        for (int i = 0; i < CONSUME_CODES.length; i++) {
            ALL_CONSUME_CODES.put(CONSUME_CODES[i][0], Integer.parseInt(CONSUME_CODES[i][2]));
        }
    }
    
    /**
     * 线程检查配置文件是否修改。
     */
    public void run() {
        while (!stopped) {
            try {
                Thread.sleep(60000L);
            } catch (Exception e) {
            }
            try {
                if (configFile.lastModified() != configModifiedTime) {
                    loadConfig();
                    configModifiedTime = configFile.lastModified();
                }
            } catch (Exception e) {
                log.error(e, e);
            }
        }
    }
    
    /**
     * 关闭后台线程。
     */
    public void shutdown() {
        stopped = true;
        workingThread.interrupt();
    }
    
    /*
     * 生成交易流水号：7位合作方ID + 14位日期(YYYYMMDDHHMMSS) + 6位自定义数字
     * @return
     */
    private String generateTransID() {
        StringBuilder sb = new StringBuilder();
        sb.append(cpId);
        sb.append("0");
        sb.append(dateFormat.format(new Date()));
        String ser = String.valueOf(serialGen.getAndIncrement());
        sb.append(ser.substring(ser.length() - 6));
        return sb.toString();
    }
    
    /*
     * 向请求字符串中添加transIDO和versionId两个参数。
     */
    private void appendTransID(StringBuilder sb) {
        if ("2_0_0".equals(protocolVersion)) {
            sb.append("<transIDO>");
            sb.append(generateTransID());
            sb.append("</transIDO>");
            sb.append("<versionId>");
            sb.append(protocolVersion);
            sb.append("</versionId>");
        }
    }

    /**
     * 查询消费历史记录（旧版）。
     * @param userId 用户ID
     * @param start 起始日期yyyymmdd
     * @param end 结束日期yyyymmdd
     * @param startSequence 开始记录号
     * @param count 最多返回条目数
     * @return
     * @throws CmccException
     */
    public CmccConsumeRecord queryConsume(String userId, String start, String end, int startSequence, int count)
            throws CmccException {
        // 生成请求包
        PostMethod method = new PostMethod(CMCC_QUERYCONSUME);
        try {
            StringRequestEntity entity = new StringRequestEntity(getQueryConsume(userId, start, end, startSequence,
                    count), "text/xml", "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex) {
        }
        
        // 发送请求
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code != 200) {
                throw new CmccException("平台接口访问错误");
            }
            SAXReader reader = new SAXReader();
            String s = new String(method.getResponseBody(), "UTF-8").trim();
            log.info(s);
            
            /* 返回包格式
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
              <msgType>QueryDetailResp</msgType>
              <hRet>0</hRet>
              <status>1200</status>
              <userIdType>1</userIdType>
              <userLabel>13888888888</userLabel>
              <startSequence>1</startSequence>
              <recordCount>20</recordCount>
              <chargeList>
                <consumerSchema>
                  <date>String</date>
                  <consumerType>C</consumerType>
                  <point>String</point>
                  <cpId>String</cpId>
                  <cpServiceId>String</cpServiceId>
                  <serviceName>String</serviceName>
                </consumerSchema>
                <consumerSchema>
                  <date>String</date>
                  <consumerType>C</consumerType>
                  <point>String</point>
                  <cpId>String</cpId>
                  <cpServiceId>String</cpServiceId>
                  <serviceName>String</serviceName>
                </consumerSchema>
                <consumerSchema>
                  <date>String</date>
                  <consumerType>C</consumerType>
                  <point>String</point>
                  <cpId>String</cpId>
                  <cpServiceId>String</cpServiceId>
                  <serviceName>String</serviceName>
                </consumerSchema>
              </chargeList>
            </response>
             */
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            Element root = doc.getRootElement();
            int ret = Integer.parseInt(root.elementText("hRet"));
            int status = Integer.parseInt(root.elementText("status"));
            if (ret == 0) { // 查询成功
                int ss = Integer.parseInt(root.elementText("startSequence"));
                int cc = Integer.parseInt(root.elementText("recordCount"));
                Element list = root.element("chargeList");
                List<CmccConsumeItem> l = new ArrayList<CmccConsumeItem>(cc);
                for (Iterator ite = list.elementIterator("consumerSchema"); ite.hasNext();) {
                    Element e = (Element) ite.next();
                    String date = e.elementText("date");
                    String type = e.elementText("consumerType");
                    int point = Integer.parseInt(e.elementText("point"));
                    String cpId = e.elementText("cpId");
                    String cpServiceId = e.elementText("cpServiceId");
                    String serviceName = e.elementText("serviceName");
                    CmccConsumeItem item = new CmccConsumeItem(date, type, point, cpId, cpServiceId, serviceName);
                    l.add(item);
                }
                CmccConsumeItem[] items = new CmccConsumeItem[l.size()];
                l.toArray(items);
                CmccConsumeRecord record = new CmccConsumeRecord(ret, userId, ss, items);
                return record;
            } else {
                throw CmccException.create(status);
            }
        } catch (NumberFormatException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (DocumentException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (IOException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } finally {
            method.releaseConnection();
        }
    }

    /*
     * 请求包格式：
     * <?xml version="1.0" encoding="UTF-8"?>
     * <request>
     *   <msgType>QueryConsumeListResp</msgType>
     *   <sender>101</sender>
     *   <userIdType>1</userIdType>
     *   <userLabel>13888888888</userLabel>
     *   <channelId>String</channelId>
     *   <startDate>String</startDate>
     *   <endDate>String</endDate>
     *   <startSequence>1</startSequence>
     *   <recordCount>20</recordCount>
     *   <consumerType>C</consumerType>
     *   <detail>true</detail>
     *   <feeType>1</feeType>
     *   <transIDO>701001020090102142050799929</transIDO>
     *   <versionId>2_0_0</versionId>
     * </request>
     */
    public String getQueryConsume(String userId, String start, String end, int startSequence, int count) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<request>");
        sb.append("<msgType>QueryDetailReq</msgType>");
        sb.append("<sender>");
        sb.append(sender);
        sb.append("</sender>");
        sb.append("<userIdType>");
        sb.append(USERTYPE);
        sb.append("</userIdType>");
        sb.append("<userLabel>");
        sb.append(userId);
        sb.append("</userLabel>");
        sb.append("<channelId>");
        sb.append(channelId);
        sb.append("</channelId>");
        sb.append("<startDate>");
        sb.append(start);
        sb.append("</startDate>");
        sb.append("<endDate>");
        sb.append(end);
        sb.append("</endDate>");
        sb.append("<startSequence>");
        sb.append(startSequence);
        sb.append("</startSequence>");
        sb.append("<recordCount>");
        sb.append(count);
        sb.append("</recordCount>");
        sb.append("<consumerType>C</consumerType><detail>true</detail><feeType>1</feeType>");
        appendTransID(sb);
        sb.append("</request>");
        String ret = sb.toString();
        log.info(ret);
        return ret;
    }

    /**
     * 查询充值记录（旧版）。
     * @param userId 用户ID
     * @param start 起始日期yyyymmdd
     * @param end 结束日期yyyymmdd
     * @param startSequence 起始记录号
     * @param count 最大返回数量
     * @return
     * @throws CmccException
     */
    public CmccChargeRecord queryCharge(String userId, String start, String end, int startSequence, int count)
            throws CmccException {
        // 生成请求包
        PostMethod method = new PostMethod(CMCC_QUERYCHARGE);
        try {
            StringRequestEntity entity = new StringRequestEntity(getQueryCharge(userId, start, end, startSequence,
                    count), "text/xml", "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex) {
        }
        
        // 发送请求
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code != 200) {
                throw new CmccException("平台接口访问错误");
            }
            SAXReader reader = new SAXReader();
            String s = new String(method.getResponseBody(), "UTF-8").trim();
            log.info(s);
            
            /* 返回包格式
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
              <msgType>QueryChargeResp</msgType>
              <hRet>0</hRet>
              <status>1200</status>
              <userIdType>1</userIdType>
              <userLabel>13888888888</userLabel>
              <startSequence>1</startSequence>
              <recordCount>20</recordCount>
              <chargeList>
                <consumerSchema>
                  <date>String</date>
                  <consumerType>A</consumerType>
                  <point>String</point>
                </consumerSchema>
                <consumerSchema>
                  <date>String</date>
                  <consumerType>A</consumerType>
                  <point>String</point>
                </consumerSchema>
                <consumerSchema>
                  <date>String</date>
                  <consumerType>A</consumerType>
                  <point>String</point>
                </consumerSchema>
              </chargeList>
            </response>
             */
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            Element root = doc.getRootElement();
            int ret = Integer.parseInt(root.elementText("hRet"));
            int status = Integer.parseInt(root.elementText("status"));
            if (ret == 0) { // 查询成功
                int ss = Integer.parseInt(root.elementText("startSequence"));
                int cc = Integer.parseInt(root.elementText("recordCount"));
                Element list = root.element("chargeList");
                List<CmccChargeItem> l = new ArrayList<CmccChargeItem>(cc);
                for (Iterator ite = list.elementIterator("consumerSchema"); ite.hasNext();) {
                    Element e = (Element) ite.next();
                    String date = e.elementText("date");
                    String type = e.elementText("consumerType");
                    int point = Integer.parseInt(e.elementText("point"));
                    CmccChargeItem item = new CmccChargeItem(date, type, point);
                    l.add(item);
                }
                CmccChargeItem[] items = new CmccChargeItem[l.size()];
                l.toArray(items);
                CmccChargeRecord record = new CmccChargeRecord(ret, userId, startSequence, items);
                return record;
            } else {
                throw CmccException.create(status);
            }
        } catch (NumberFormatException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (DocumentException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (IOException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } finally {
            method.releaseConnection();
        }
    }

    /*
     * 请求包格式：
     * <?xml version="1.0" encoding="UTF-8"?>
     * <request>
     *   <msgType>QueryChargeUpRecordReq</msgType>
     *   <sender>101</sender>
     *   <userIdType>1</userIdType>
     *   <userLabel>13888888888</userLabel>
     *   <channelId>String</channelId>
     *   <startDate>String</startDate>
     *   <endDate>String</endDate>
     *   <startSequence>1</startSequence>
     *   <recordCount>20</recordCount>
     *   <transIDO>701001020090102142050799929</transIDO>
     *   <versionId>2_0_0</versionId>
     * </request>
     */
    public String getQueryCharge(String userId, String start, String end, int startSequence, int count) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<request>");
        sb.append("<msgType>QueryChargeUpRecordReq</msgType>");
        sb.append("<sender>");
        sb.append(sender);
        sb.append("</sender>");
        sb.append("<userIdType>");
        sb.append(USERTYPE);
        sb.append("</userIdType>");
        sb.append("<userLabel>");
        sb.append(userId);
        sb.append("</userLabel>");
        sb.append("<channelId>");
        sb.append(channelId);
        sb.append("</channelId>");
        sb.append("<startDate>");
        sb.append(start);
        sb.append("</startDate>");
        sb.append("<endDate>");
        sb.append(end);
        sb.append("</endDate>");
        sb.append("<startSequence>");
        sb.append(startSequence);
        sb.append("</startSequence>");
        sb.append("<recordCount>");
        sb.append(count);
        sb.append("</recordCount>");
        appendTransID(sb);
        sb.append("</request>");
        String ret = sb.toString();
        log.info(ret);
        return ret;
    }
    
    
    
    /**
     * 查询消费历史记录（新版）。
     * @param userId 用户ID
     * @param start 起始日期yyyymmdd
     * @param end 结束日期yyyymmdd
     * @param startSequence 开始记录号
     * @param count 最多返回条目数
     * @param timeType 0 - 当日，1 - 指定月，2 - 10天内
     * @param queryType 0 - 全部；消费历史
     * @return
     * @throws CmccException
     */
    public CmccConsumeRecord queryConsumeNew(String userId, String start, String end, int startSequence, 
            int count, int timeType, int queryType) throws CmccException {
        // 生成请求包
        PostMethod method = new PostMethod(CMCC_QUERY_NEW);
        try {
            if (queryType == 0) {
                queryType = 3;
            } else if (queryType == 1) {
                queryType = 5;
            } else {
                queryType = 13;
            }
            timeType++;
            StringRequestEntity entity = new StringRequestEntity(getQueryNew(userId, queryType, timeType, start), 
                    "text/xml", "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex) {
        }
        
        // 发送请求
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code != 200) {
                throw new CmccException("平台接口访问错误");
            }
            SAXReader reader = new SAXReader();
            String s = new String(method.getResponseBody(), "UTF-8").trim();
            log.info(s);
            
            /* 返回包格式
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
              <msgType>QueryConsumeRecordReq</msgType>
              <queryType>3</queryType>
              <hRet>1</hRet>
              <status>1102</status>
              <userIdType>1</userIdType>
              <userLabel>13888888888</userLabel>
              <recordList>
                <recordSchema>
                    <!--消费类型：A-充值B-套餐O-开户C1-客户端单机C2-客户端网游道具C3-WAP网游道具C4-WAP单机-->
                    <recordType>消费类型</recordType>
                    <cpId>合作方ID</cpId>
                    <cpName>合作方名称</cpName>
                    <channelId>渠道ID</channelId>
                    <channelName>渠道名称</channelName>
                    <cpServiceId>业务ID或充值代码ID</cpServiceId>
                    <cpServiceName>业务名称或充值名称</cpServiceName>
                    <packageId>套餐ID</packageId>
                    <packageName>套餐名称</packageName>
                    <toolsId>道具ID</toolsId>
                    <toolsName>道具名称</toolsName>
                    <date>消费时间(格式20070618 09:58)</date>
                    <payType>计费方式(1-点数 2-话费)</payType>
                    <payValue>支付点数或话费分</payValue>
                </recordSchema>
              </recordList>
            </response>
             */
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            Element root = doc.getRootElement();
            int ret = Integer.parseInt(root.elementText("hRet"));
            int status = Integer.parseInt(root.elementText("status"));
            if (ret == 0) { // 查询成功
                int ss = 1;
                Element list = root.element("recordList");
                List<CmccConsumeItem> l = new ArrayList<CmccConsumeItem>();
                int seq = 0;
                for (Iterator ite = list.elementIterator("recordSchema"); ite.hasNext();) {
                    Element e = (Element) ite.next();
                    String date = e.elementText("date");
                    String type = e.elementText("recordType");
                    int point = Integer.parseInt(e.elementText("payValue"));
                    String cpId = e.elementText("cpId");
                    String cpServiceId = e.elementText("cpServiceId");
                    String serviceName = e.elementText("cpServiceName");
                    String toolName = e.elementText("toolsName");
                    CmccConsumeItem item = new CmccConsumeItem(date, type, point, cpId, cpServiceId, 
                            serviceName + " " + toolName);
                    seq++;
                    if (seq >= startSequence && seq < startSequence + count) {
                        l.add(item);
                    }
                }
                CmccConsumeItem[] items = new CmccConsumeItem[l.size()];
                l.toArray(items);
                CmccConsumeRecord record = new CmccConsumeRecord(ret, userId, ss, items);
                return record;
            } else {
                throw CmccException.create(status);
            }
        } catch (NumberFormatException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (DocumentException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (IOException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * 查询充值记录（新版）。
     * @param userId 用户ID
     * @param start 起始日期yyyymmdd
     * @param end 结束日期yyyymmdd
     * @param startSequence 起始记录号
     * @param count 最大返回数量
     * @param timeType 0 - 当日，1 - 指定月，2 - 10天内
     * @param queryType 0 - 全部；消费历史
     * @return
     * @throws CmccException
     */
    public CmccChargeRecord queryChargeNew(String userId, String start, String end, int startSequence, 
            int count, int timeType, int queryType)
            throws CmccException {
        // 生成请求包
        PostMethod method = new PostMethod(CMCC_QUERY_NEW);
        try {
            queryType = 7;
            timeType++;
            StringRequestEntity entity = new StringRequestEntity(getQueryNew(userId, queryType, timeType, start), 
                    "text/xml", "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex) {
        }
        
        // 发送请求
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code != 200) {
                throw new CmccException("平台接口访问错误");
            }
            SAXReader reader = new SAXReader();
            String s = new String(method.getResponseBody(), "UTF-8").trim();
            log.info(s);
            
            /* 返回包格式
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
              <msgType>QueryConsumeRecordReq</msgType>
              <queryType>3</queryType>
              <hRet>1</hRet>
              <status>1102</status>
              <userIdType>1</userIdType>
              <userLabel>13888888888</userLabel>
              <recordList>
                <recordSchema>
                    <!--消费类型：A-充值B-套餐O-开户C1-客户端单机C2-客户端网游道具C3-WAP网游道具C4-WAP单机-->
                    <recordType>消费类型</recordType>
                    <cpId>合作方ID</cpId>
                    <cpName>合作方名称</cpName>
                    <channelId>渠道ID</channelId>
                    <channelName>渠道名称</channelName>
                    <cpServiceId>业务ID或充值代码ID</cpServiceId>
                    <cpServiceName>业务名称或充值名称</cpServiceName>
                    <packageId>套餐ID</packageId>
                    <packageName>套餐名称</packageName>
                    <toolsId>道具ID</toolsId>
                    <toolsName>道具名称</toolsName>
                    <date>消费时间(格式20070618 09:58)</date>
                    <payType>计费方式(1-点数 2-话费)</payType>
                    <payValue>支付点数或话费分</payValue>
                </recordSchema>
              </recordList>
            </response>
             */
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            Element root = doc.getRootElement();
            int ret = Integer.parseInt(root.elementText("hRet"));
            int status = Integer.parseInt(root.elementText("status"));
            if (ret == 0) { // 查询成功
                int ss = 1;
                Element list = root.element("recordList");
                List<CmccChargeItem> l = new ArrayList<CmccChargeItem>();
                int seq = 0;
                for (Iterator ite = list.elementIterator("recordSchema"); ite.hasNext();) {
                    Element e = (Element) ite.next();
                    String date = e.elementText("date");
                    String type = e.elementText("recordType");
                    int point = Integer.parseInt(e.elementText("payValue"));
                    CmccChargeItem item = new CmccChargeItem(date, type, point);
                    seq++;
                    if (seq >= startSequence && seq < startSequence + count) {
                        l.add(item);
                    }
                }
                CmccChargeItem[] items = new CmccChargeItem[l.size()];
                l.toArray(items);
                CmccChargeRecord record = new CmccChargeRecord(ret, userId, startSequence, items);
                return record;
            } else {
                throw CmccException.create(status);
            }
        } catch (NumberFormatException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (DocumentException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (IOException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } finally {
            method.releaseConnection();
        }
    }

    /*
     * 请求包格式：
     * <?xml version="1.0" encoding="UTF-8"?>
     * <request>
     *   <msgType>QueryConsumeRecordReq</msgType>
     *   <queryType>7,3,5,13</queryType>
     *   <sender>101</sender>
     *   <channelId></channelId>
     *   <userIdType>1</userIdType>
     *   <userLabel>13888888888</userLabel>
     *   <queryMonth>200901</queryMonth>
     *   <queryRange>1</queryRange>
     *   <payType>3</payType>
     *   <cpServiceId></cpServiceId>
     *   <packageId></packageId>
     *   <cpId></cpId>
     * </request>
     */
    public String getQueryNew(String userId, int queryType, int timeType, String month) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<request>");
        sb.append("<msgType>QueryConsumeRecordReq</msgType>");
        sb.append("<queryType>");
        sb.append(queryType);
        sb.append("</queryType>");
        sb.append("<sender>");
        sb.append(sender);
        sb.append("</sender>");
        sb.append("<channelId>");
        sb.append("</channelId>");
        sb.append("<userIdType>");
        sb.append(USERTYPE);
        sb.append("</userIdType>");
        sb.append("<userLabel>");
        sb.append(userId);
        sb.append("</userLabel>");
        sb.append("<queryMonth>");
        if (timeType == 2) {
            sb.append(month);
        }
        sb.append("</queryMonth>");
        sb.append("<queryRange>");
        sb.append(timeType);
        sb.append("</queryRange>");
        sb.append("<payType>3</payType>");
        sb.append("<cpServiceId>");
        if (queryType == 13) {
            sb.append(cpServiceId);
        }
        sb.append("</cpServiceId>");
        sb.append("<packageId></packageId>");
        sb.append("<cpId>");
        sb.append("</cpId>");
        sb.append("</request>");
        String ret = sb.toString();
        log.info(ret);
        return ret;
    }
    
    /**
     * 查询余额。
     * @param userId 用户ID
     * @return
     * @throws CmccException
     */
    public int queryBalance(String userId) throws CmccException {
        // 生成请求包
        PostMethod method = new PostMethod(CMCC_QUERYBALANCE);
        try {
            StringRequestEntity entity = new StringRequestEntity(getQueryBalanceString(userId), "text/xml", "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex1) {
        }
        
        // 发送请求
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code != 200)
                throw new CmccException("平台接口访问错误");
            SAXReader reader = new SAXReader();
            String s = new String(method.getResponseBody(), "UTF-8").trim();
            log.info(s);
            
            /* 返回包格式：
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
              <msgType>QueryBalanceResp</msgType>
              <hRet>0</hRet>
              <status>1200</status>
              <userIdType>1</userIdType>
              <userLabel>13888888888</userLabel>
              <point>String</point>
            </response>
             */
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            int ret = Integer.parseInt(doc.getRootElement().elementText("hRet"));
            int status = Integer.parseInt(doc.getRootElement().elementText("status"));
            if (ret == 0) { // 查询成功
                String sValue = doc.getRootElement().elementText("point");
                int index = sValue.indexOf(".");
                if (index > 0) {
                    sValue = sValue.substring(0, index);
                }
                return Integer.parseInt(sValue);
            } else {
                throw CmccException.create(status);
            }
        } catch (NumberFormatException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (DocumentException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (IOException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } finally {
            method.releaseConnection();
        }
    }

    /*
     * 请求包格式：
     * <?xml version="1.0" encoding="UTF-8"?>
     * <request>
     *   <msgType>QueryBalanceReq</msgType>
     *   <sender>101</sender>
     *   <userIdType>1</userIdType>
     *   <userLabel>13888888888</userLabel>
     *   <channelId>String</channelId>
     *   <transIDO>701001020090102142050799929</transIDO>
     *   <versionId>2_0_0</versionId>
     * </request>
     */
    private String getQueryBalanceString(String userId) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<request>");
        sb.append("<msgType>QueryBalanceReq</msgType>");
        sb.append("<sender>");
        sb.append(sender);
        sb.append("</sender>");
        sb.append("<userIdType>");
        sb.append(USERTYPE);
        sb.append("</userIdType>");
        sb.append("<userLabel>");
        sb.append(userId);
        sb.append("</userLabel>");
        sb.append("<channelId>");
        sb.append(channelId);
        sb.append("</channelId>");
        appendTransID(sb);
        sb.append("</request>");
        String ret = sb.toString();
        log.info(ret);
        return ret;
    }

    /**
     * 请求充值。
     * @param userId 用户ID
     * @param chargeCode 充值代码
     * @return 账户剩余点数
     * @throws CmccException
     */
    public int chargeUp(String userId, String chargeCode) throws CmccException {
        // 生成请求包
        PostMethod method = new PostMethod(CMCC_CHARGEUP);
        try {
            StringRequestEntity entity = new StringRequestEntity(getChargeUpString(userId, chargeCode), "text/xml",
                    "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex1) {
        }
        
        // 发送请求
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code != 200) {
                throw new CmccException("平台接口访问错误");
            }
            SAXReader reader = new SAXReader();
            String s = new String(method.getResponseBody(), "UTF-8").trim();
            log.info(s);
            
            /* 返回包格式：
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
              <msgType>ChargeUpResp</msgType>
              <hRet>0</hRet>
              <status>1200</status>
              <balance>200</balance>
            </response>
             */
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            int ret = Integer.parseInt(doc.getRootElement().elementText("hRet"));
            int status = Integer.parseInt(doc.getRootElement().elementText("status"));
            if (ret != 0) {
                throw CmccException.create(status);
            }
            try {
                int balance = (int)Float.parseFloat(doc.getRootElement().elementText("balance"));
                return balance;
            } catch (Exception e) {
                // 旧版接口，查询余额返回
                return queryBalance(userId);
            }
        } catch (DocumentException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (IOException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (NumberFormatException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * 请求充值。
     * @param userId 用户ID
     * @param charge 充值金额(元) 1-50
     * @return 账户剩余点数
     * @throws CmccException
     */
    public int chargeUp(String userId, int charge) throws CmccException {
        if (charge <= 0 || charge > CHARGE_TABLE.length) {
            throw new CmccException("充值金额错误");
        }
        String cpServiceId = CHARGE_TABLE[charge - 1];
        return chargeUp(userId, cpServiceId);
    }

    /*
     * 请求包格式：
     * <?xml version="1.0" encoding="UTF-8"?>
     * <request>
     *   <msgType>ChargeUpReq</msgType>
     *   <sender>101</sender>
     *   <userIdType>1</userIdType>
     *   <userLabel>13888888888</userLabel>
     *   <channelId>String</channelId>
     *   <cpId>String</cpId>
     *   <cpServiceId>String</cpServiceId>
     *   <transIDO>701001020090102142050799929</transIDO>
     *   <versionId>2_0_0</versionId>
     * </request>
     */
    private String getChargeUpString(String userId, String cpServiceId) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<request>");
        sb.append("<msgType>ChargeUpReq</msgType>");
        sb.append("<sender>");
        sb.append(sender);
        sb.append("</sender>");
        sb.append("<userIdType>");
        sb.append(USERTYPE);
        sb.append("</userIdType>");
        sb.append("<userLabel>");
        sb.append(userId);
        sb.append("</userLabel>");
        sb.append("<channelId>");
        sb.append(CHARGECHANNELID);
        sb.append("</channelId>");
        sb.append("<cpId>");
        sb.append(CHARGECPID);
        sb.append("</cpId>");
        sb.append("<cpServiceId>");
        sb.append(cpServiceId);
        sb.append("</cpServiceId>");
        appendTransID(sb);
        sb.append("</request>");
        String ret = sb.toString();
        log.info(ret);
        return ret;
    }

    /**
     * 购买道具。
     * @param userId 用户ID
     * @param consumeCode 消费代码
     * @param channel 4位渠道代码
     * @return 账户余额
     * @throws CmccException
     */
    public int buyGameTool(String userId, String consumeCode, String channel) throws CmccException {
        // 后门，特殊消费代码用于测试
        if (consumeCode.equals(ZERO)) {
            return queryBalance(userId);
        }
        
        // 临时：消费代码特殊转换。当幻想新代码启用，重启AuthServer后，pip区仍然在用旧代码购买，
        // 这里把旧代码转换为新代码。
        if (cpServiceId.equals("120121972000")) {
            if (consumeCode.startsWith("120120433")) {
                int code = Integer.parseInt(consumeCode.substring(9));
                if (code >= 73) {
                    code++;
                }
                String codeStr = String.valueOf(code);
                while (codeStr.length() < 3) {
                    codeStr = "0" + codeStr;
                }
                consumeCode = "120121972" + codeStr;
            }
        }

        // 生成请求包
        PostMethod method = new PostMethod(CMCC_BUYGAMETOOL);
        try {
            StringRequestEntity entity = new StringRequestEntity(
                    getBuyGameToolString(userId, consumeCode, channel), "text/xml", "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex1) {
        }
        
        // 发送请求
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code != 200) {
                throw new CmccException("平台接口访问错误");
            }
            SAXReader reader = new SAXReader();
            String s = new String(method.getResponseBody(), "UTF-8").trim();
            log.info(s);
            
            /* 返回包格式：
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
              <msgType>BuyGameToolResp</msgType>
              <hRet>0</hRet>
              <status>1800</status>
              <balance>200</balance>
              <point>200</point>
            </response>
             */
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            int ret = Integer.parseInt(doc.getRootElement().elementText("hRet"));
            int status = Integer.parseInt(doc.getRootElement().elementText("status"));
            if (ret != 0) {
                if (status == 1182) {
                    throw new NoEnoughBalanceException("您的点数余额不足（状态码：1182）");
                } else {
                    throw CmccException.create(status);
                }
            }
            try {
                int balance = (int)Float.parseFloat(doc.getRootElement().elementText("balance"));
                return balance;
            } catch (Exception e) {
                // 旧版接口，查询余额返回
                return queryBalance(userId);
            }
        } catch (DocumentException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (IOException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (NumberFormatException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } finally {
            method.releaseConnection();
        }
    }

    /* 
     * 请求包格式：
     * <?xml version="1.0" encoding="UTF-8"?>
     * <request>
     *   <msgType>BuyGameToolReq</msgType>
     *   <sender>101</sender>
     *   <userId>12345678</userId>
     *   <fid>String</fid>
     *   <cpId>String</cpId>
     *   <cpServiceId>String</cpServiceId>
     *   <consumeCode>String</consumeCode>
     *   <transIDO>701001020090102142050799929</transIDO>
     *   <versionId>2_0_0</versionId>
     *</request>
     */
    private String getBuyGameToolString(String userId, String consumeCode, String channel) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<request>");
        sb.append("<msgType>BuyGameToolReq</msgType>");
        sb.append("<userId>");
        sb.append(userId);
        sb.append("</userId>");
        sb.append("<sender>");
        sb.append(sender);
        sb.append("</sender>");
        if ("2_0_0".equals(protocolVersion)) {
            sb.append("<fid>");
            sb.append(channel);
            sb.append("</fid>");
        } else {
            sb.append("<channelId>");
            sb.append(channelId);
            sb.append("</channelId>");
        }
        sb.append("<cpId>");
        sb.append(cpId);
        sb.append("</cpId>");
        sb.append("<cpServiceId>");
        sb.append(cpServiceId);
        sb.append("</cpServiceId>");
        sb.append("<consumeCode>");
        sb.append(consumeCode);
        sb.append("</consumeCode>");
        appendTransID(sb);
        sb.append("</request>");
        String ret = sb.toString();
        log.info(ret);
        return ret;
    }
    
    /**
     * 用户登出。
     * @param userId 用户ID
     * @throws CmccException
     */
    public void logout(String userId) {
        // 旧版本没有这个接口
        if (!"2_0_0".equals(protocolVersion)) {
            return;
        }
        
        // 生成请求包
        PostMethod method = new PostMethod(CMCC_LOGOUT);
        try {
            StringRequestEntity entity = new StringRequestEntity(
                    getLogoutString(userId), "text/xml", "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex1) {
        }
        
        // 发送请求
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code != 200) {
                return;
            }
            String s = new String(method.getResponseBody(), "UTF-8").trim();
            log.info(s);
        } catch (IOException ex) {
            log.error(ex, ex);
        } catch (NumberFormatException ex) {
            log.error(ex, ex);
        } finally {
            method.releaseConnection();
        }
    }

    /* 
     * 请求包格式：
     * <?xml version="1.0" encoding="UTF-8"?>
     * <request>
     *   <msgType>LogoutOnlineGameReq</msgType>
     *   <sender>101</sender>
     *   <userId>12345678</userId>
     *   <channelId>String</channelId>
     *   <cpId>String</cpId>
     *   <cpServiceId>String</cpServiceId>
     *   <transIDO>701001020090102142050799929</transIDO>
     *   <versionId>2_0_0</versionId>
     *</request>
     */
    private String getLogoutString(String userId) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<request>");
        sb.append("<msgType>LogoutOnlineGameReq</msgType>");
        sb.append("<userId>");
        sb.append(userId);
        sb.append("</userId>");
        sb.append("<sender>");
        sb.append(sender);
        sb.append("</sender>");
        sb.append("<channelId>");
        sb.append(channelId);
        sb.append("</channelId>");
        sb.append("<cpId>");
        sb.append(cpId);
        sb.append("</cpId>");
        sb.append("<cpServiceId>");
        sb.append(cpServiceId);
        sb.append("</cpServiceId>");
        appendTransID(sb);
        sb.append("</request>");
        String ret = sb.toString();
        log.info(ret);
        return ret;
    }
    
    /**
     * 查询一个消费代码对应的点数价格。
     * @param consumeCode
     * @return 如果消费代码不存在，返回0.
     */
    public int getPrice(String consumeCode) {
        try {
            return ALL_CONSUME_CODES.get(consumeCode);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 查询一个消费代码对应的道具名称。
     * @param consumeCode
     */
    public String getItemName(String consumeCode) {
        for (int i = 0; i < CONSUME_CODES.length; i++) {
            if (consumeCode.equals(CONSUME_CODES[i][0])) {
                String ret = CONSUME_CODES[i][1];
                if (ret.endsWith("(折)")) {
                    ret = ret.substring(0, ret.length() - 3);
                }
                return ret;
            }
        }
        return null;
    }
    
    /**
     * 推荐用户。
     * @param userId 用户ID
     * @param msisdn 目标手机号
     * @param message PUSH标题
     * @throws CmccException
     */
    public void sendRecommend(String userId, String msisdn, String message) throws CmccException {
        // 生成请求包
        PostMethod method = new PostMethod(CMCC_RECOMMEND);
        try {
            StringRequestEntity entity = new StringRequestEntity(
                    getRecommendString(userId, msisdn, message), "text/xml", "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex1) {
        }
        
        // 发送请求
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code != 200) {
                throw new CmccException("平台接口访问错误");
            }
            SAXReader reader = new SAXReader();
            String s = new String(method.getResponseBody(), "UTF-8").trim();
            log.info(s);
            
            /* 返回包格式：
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
              <ret>0</ret>
              <status>0</status>
              ret   返回码 0-成功  1-失败
              状态码
0 成功
1 XML错误
2 非吉林省用户
3 游戏平台下发短信失败
4 游戏平台内部错误
            </response>
             */
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            int ret = Integer.parseInt(doc.getRootElement().elementText("ret"));
            int status = Integer.parseInt(doc.getRootElement().elementText("status"));
            if (ret == 0 && status == 0) {
                return;  // 成功
            } else {
                if (status == 2) {
                    throw new CmccException("您的手机卡不支持此功能");
                } else if (status == 3) {
                    throw new CmccException("向对方下发短信失败");
                } else {
                    throw new CmccException("平台接口访问错误");
                }
            }
        } catch (DocumentException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (IOException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (NumberFormatException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } finally {
            method.releaseConnection();
        }
    }

    /* 
     * 请求包格式：
     * <?xml version="1.0" encoding="UTF-8"?>
     * <request>
     *   <msgType>1</msgType>
     *   <userId>12345678</userId>
     *   <msisdn>13910191212</msisdn>
     *   <cpId>String</cpId>
     *   <cpServiceId>String</cpServiceId>
     *   <content>标题</content>
     *</request>
     */
    private String getRecommendString(String userId, String msisdn, String title) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<request>");
        sb.append("<msgType>1</msgType>");
        sb.append("<userId>");
        sb.append(userId);
        sb.append("</userId>");
        sb.append("<msisdn>");
        sb.append(msisdn);
        sb.append("</msisdn>");
        sb.append("<cpId>");
        sb.append(cpId);
        sb.append("</cpId>");
        sb.append("<cpServiceId>");
        sb.append(cpServiceId);
        sb.append("</cpServiceId>");
        sb.append("<content>");
        sb.append(title);
        sb.append("</content>");
        sb.append("</request>");
        String ret = sb.toString();
        log.info(ret);
        return ret;
    }

    /**
     * 下发获赠话费通知。
     * @param userId 用户ID
     * @param message 通知内容
     * @throws CmccException
     */
    public void sendRewardMessage(String userId, String message) throws CmccException {
        // 生成请求包
        PostMethod method = new PostMethod(CMCC_RECOMMEND);
        try {
            StringRequestEntity entity = new StringRequestEntity(
                    getSendRewardString(userId, message), "text/xml", "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex1) {
        }
        
        // 发送请求
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code != 200) {
                throw new CmccException("平台接口访问错误");
            }
            SAXReader reader = new SAXReader();
            String s = new String(method.getResponseBody(), "UTF-8").trim();
            log.info(s);
            
            /* 返回包格式：
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
              <ret>0</ret>
              <status>0</status>
              ret   返回码 0-成功  1-失败
              状态码
0 成功
1 XML错误
2 非吉林省用户
3 游戏平台下发短信失败
4 游戏平台内部错误
            </response>
             */
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            int ret = Integer.parseInt(doc.getRootElement().elementText("ret"));
            int status = Integer.parseInt(doc.getRootElement().elementText("status"));
            if (ret == 0 && status == 0) {
                return;  // 成功
            } else {
                if (status == 2) {
                    throw new CmccException("您的手机卡不支持此功能");
                } else if (status == 3) {
                    throw new CmccException("向对方下发短信失败");
                } else {
                    throw new CmccException("平台接口访问错误");
                }
            }
        } catch (DocumentException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (IOException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } catch (NumberFormatException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } finally {
            method.releaseConnection();
        }
    }

    /* 
     * 请求包格式：
     * <?xml version="1.0" encoding="UTF-8"?>
     * <request>
     *   <msgType>2</msgType>
     *   <userId>12345678</userId>
     *   <msisdn></msisdn>
     *   <cpId></cpId>
     *   <cpServiceId></cpServiceId>
     *   <content>标题</content>
     *</request>
     */
    private String getSendRewardString(String userId, String title) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<request>");
        sb.append("<msgType>2</msgType>");
        sb.append("<userId>");
        sb.append(userId);
        sb.append("</userId>");
        sb.append("<msisdn>");
        sb.append("</msisdn>");
        sb.append("<cpId>");
        sb.append(cpId);
        sb.append("</cpId>");
        sb.append("<cpServiceId>");
        sb.append(cpServiceId);
        sb.append("</cpServiceId>");
        sb.append("<content>");
        sb.append(title);
        sb.append("</content>");
        sb.append("</request>");
        String ret = sb.toString();
        log.info(ret);
        return ret;
    }
    
    /**
     * 订购移动业务。
     * @param userId
     * @param subType
     * @throws CmccException
     */
    public boolean subscribe(String userId, int subType) throws CmccException {
        // 生成请求包
        GetMethod method = new GetMethod(CMCC_SUBSCRIBE);
        HttpMethodParams params = new HttpMethodParams();
        params.setParameter("userId", userId);
        params.setParameter("busiType", "0" + subType);
        params.setParameter("cpServiceId", cpServiceId);
        params.setParameter("cpId", cpId);
        method.setParams(params);
        
        // 发送请求
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code != 200) {
                throw new CmccException("平台接口访问错误");
            }
            String s = new String(method.getResponseBody(), "UTF-8").trim();
            return s.startsWith("0");
        } catch (IOException ex) {
            log.error(ex, ex);
            throw new CmccException("平台接口访问错误");
        } finally {
            method.releaseConnection();
        }
    }
}
