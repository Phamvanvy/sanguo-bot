package com.pip.itimes.server.auth;

import java.io.*;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import java.util.List;
import java.util.ArrayList;

public class CmccService {

    private static final Logger log = Logger.getLogger(CmccService.class);
//    private static final String CMCC_QUERYBALANCE = "http://192.168.0.164:8080/itimesquery/test/QueryBalance.jsp";
//    private static final String CMCC_QUERYBALANCE = "http://218.206.80.185:7070/itimesquery/test/QueryBalance.jsp";
//    private static final String CMCC_QUERYBALANCE = "http://gmpbeta.i139.cn/bizcontrol/QueryBalance";
    private static final String CMCC_QUERYBALANCE = "http://gmp.i139.cn/bizcontrol/QueryBalance";
    private static final String CMCC_CHARGEUP = "http://gmp.i139.cn/bizcontrol/ChargeUp";
//    private static final String CMCC_CHARGEUP = "http://192.168.0.164:8080/itimesquery/test/ChargeUp.jsp";
//    private static final String CMCC_CHARGEUP = "http://218.206.80.185:7070/itimesquery/test/ChargeUp.jsp";

    private static final String CMCC_BUYGAMETOOL = "http://gmp.i139.cn/bizcontrol/BuyGameTool";
//    private static final String CMCC_BUYGAMETOOL = "http://192.168.0.164:8080/itimesquery/test/BuyGameTool.jsp";
//    private static final String CMCC_BUYGAMETOOL = "http://218.206.80.185:7070/itimesquery/test/BuyGameTool.jsp";
    private static final String CMCC_QUERYCHARGE = "http://gmp.i139.cn/bizcontrol/QueryChargeUpRecord";
    private static final String CMCC_QUERYCONSUME = "http://gmp.i139.cn/bizcontrol/QueryConsumeList";

//    private static final String SENDER = "301";
    private static final String USERTYPE = "3";
//    private static final String CHANNELID = "15001001";
//    private static final String CPID = "C00005";
    private static final String CHARGECPID = "701001";
//    private static final String CPSERVICEID = "120120433000";

    private String sender = "";
    private String channelId = "";
    private String cpId = "";
    private String cpServiceId = "";

    private static final String ZERO = "000000000000";

    private static final String[] CHARGE_TABLE = {

                                                 "400120001000",
                                                 "400120002000",
                                                 "400120003000",
                                                 "400120004000",
                                                 "400120005000",
                                                 "400120006000",
                                                 "400120007000",
                                                 "400120008000",
                                                 "400120009000",
                                                 "400120010000",
                                                 "400120011000",
                                                 "400120012000",
                                                 "400120013000",
                                                 "400120014000",
                                                 "400120015000",
                                                 "400120016000",
                                                 "400120017000",
                                                 "400120018000",
                                                 "400120019000",
                                                 "400120020000",
                                                 "400120021000",
                                                 "400120022000",
                                                 "400120023000",
                                                 "400120024000",
                                                 "400120025000",
                                                 "400120026000",
                                                 "400120027000",
                                                 "400120028000",
                                                 "400120029000",
                                                 "400120030000",
                                                 "400120031000",
                                                 "400120032000",
                                                 "400120033000",
                                                 "400120034000",
                                                 "400120035000",
                                                 "400120036000",
                                                 "400120037000",
                                                 "400120038000",
                                                 "400120039000",
                                                 "400120040000",
                                                 "400120041000",
                                                 "400120042000",
                                                 "400120043000",
                                                 "400120044000",
                                                 "400120045000",
                                                 "400120046000",
                                                 "400120047000",
                                                 "400120048000",
                                                 "400120049000",
                                                 "400120050000",
    };


    public CmccService(String sender,String channelId,String cpId,String cpServiceId) {
        this.sender = sender;
        this.channelId = channelId;
        this.cpId = cpId;
        this.cpServiceId = cpServiceId;
    }

    public CmccConsumeRecord queryConsume(String userId, String start,
                                         String end, int startSequence,
                                         int count) throws CmccException {
        PostMethod method = new PostMethod(CMCC_QUERYCONSUME);
        try {
            StringRequestEntity entity = new StringRequestEntity(
                    getQueryConsume(
                            userId, start, end, startSequence, count),
                    "text/xml",
                    "utf-8");
            method.setRequestEntity(entity);

        } catch (UnsupportedEncodingException ex) {
        }
        HttpClient httpclient = new HttpClient();

        try {
            httpclient.getParams().setSoTimeout(10000);
            int code = httpclient.executeMethod(method);
            if (code != 200) {
                throw new CmccException("查询消费历史失败");
            }
            SAXReader reader = new SAXReader();
            String s = method.getResponseBodyAsString().trim();
            log.info("queryconsume String:" + s);
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            Element root = doc.getRootElement();
            int ret = Integer.parseInt(root.elementText(
                    "hRet"));
            if (ret == 0) { //查询成功
                int ss = Integer.parseInt(root.elementText("startSequence"));
                int cc = Integer.parseInt(root.elementText("recordCount"));
                Element list = root.element("consumeList");
                CmccConsumeItem[] items = new CmccConsumeItem[cc];
                int i = 0;
                for (Iterator ite = list.elementIterator("consumerSchema");
                                    ite.hasNext(); ) {
                    Element e = (Element) ite.next();
                    String date = e.elementText("date");
                    String type = e.elementText("consumerType");
                    int point = Integer.parseInt(e.elementText("point"));
                    CmccConsumeItem item = new CmccConsumeItem(date, type,
                            point);
                    items[i++] = item;
                }
                CmccConsumeRecord record = new CmccConsumeRecord(ret, userId,
                        startSequence, items);
                return record;
            }
            throw new CmccException("查询消费历史失败");
        } catch (NumberFormatException ex) {
            log.error(ex, ex);
            throw new CmccException("查询消费历史失败");
        } catch (DocumentException ex) {
            log.error(ex, ex);
            throw new CmccException("查询消费历史失败");
        } catch (IOException ex) {
            log.error(ex, ex);
            throw new CmccException("查询消费历史失败");
        } finally {
            method.releaseConnection();
        }

    }

//        <?xml version="1.0" encoding="UTF-8"?>
//<request>
//<msgType>QueryConsumeListResp</msgType>
//<sender>101</sender>
//<userIdType>1</userIdType>
//<userLabel>13888888888</userLabel>
//<channelId> String</channelId>
//<startDate>String</startDate>
//<endDate>String</endDate>
//<startSequence>1</startSequence>
//<recordCount>20</recordCount>
//</request>
    public String getQueryConsume(String userId,String start,String end,int startSequence,int count){
        StringBuilder sb = new StringBuilder(500);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<msgType>QueryConsumeListResp</msgType>");
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
        sb.append("</request>");
        log.info("Query Consume String:"+sb.toString());
        return sb.toString();
    }

    public CmccChargeRecord queryCharge(String userId,String start,String end,int startSequence,int count) throws CmccException{
        PostMethod method = new PostMethod(CMCC_QUERYCHARGE);
        try {
            StringRequestEntity entity = new StringRequestEntity(getQueryCharge(
                    userId, start, end, startSequence, count), "text/xml",
                    "utf-8");
            method.setRequestEntity(entity);

        } catch (UnsupportedEncodingException ex) {
        }
        HttpClient httpclient = new HttpClient();

        try {
            httpclient.getParams().setSoTimeout(10000);
            int code = httpclient.executeMethod(method);
            if (code != 200) {
                throw new CmccException("查询充值历史失败");
            }
            SAXReader reader = new SAXReader();
            String s = method.getResponseBodyAsString().trim();
            log.info("querycharge String:" + s);
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            Element root = doc.getRootElement();
            int ret = Integer.parseInt(root.elementText(
                    "hRet"));
            if (ret == 0) { //查询成功
                int ss = Integer.parseInt(root.elementText("startSequence"));
                int cc = Integer.parseInt(root.elementText("recordCount"));
                Element list = root.element("chargeList");
//                CmccChargeItem[] items = new CmccChargeItem[cc];
                List l = new ArrayList(cc);
                int i = 0;
                for(Iterator ite = list.elementIterator("consumerSchema");ite.hasNext();){
                    Element e = (Element)ite.next();
                    String date = e.elementText("date");
                    String type = e.elementText("consumerType");
                    int point = Integer.parseInt(e.elementText("point"));
                    CmccChargeItem item = new CmccChargeItem(date,type,point);
                    l.add(item);
//                    items[i++] = item;
                }
                CmccChargeItem[] items = new CmccChargeItem[l.size()];
                l.toArray(items);
                CmccChargeRecord record = new CmccChargeRecord(ret,userId,startSequence,items);
                return record;
            }
            throw new CmccException("查询充值历史失败");
        }catch (NumberFormatException ex) {
            log.error(ex,ex);
            throw new CmccException("查询充值历史失败");
        } catch (DocumentException ex) {
            log.error(ex,ex);
            throw new CmccException("查询充值历史失败");
        } catch (IOException ex) {
            log.error(ex,ex);
            throw new CmccException("查询充值历史失败");
        } finally{
            method.releaseConnection();
        }

    }

//        <?xml version="1.0" encoding="UTF-8"?>
//<request>
//<msgType>QueryChargeUpRecordReq</msgType>
//<sender>101</sender>
//<userIdType>1</userIdType>
//<userLabel>13888888888</userLabel>
//<channelId> String</channelId>
//<startDate>String</startDate>
//<endDate>String</endDate>
//<startSequence>1</startSequence>
//<recordCount>20</recordCount>
//</request>
    public String getQueryCharge(String userId,String start,String end,int startSequence,int count){
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
        sb.append("</request>");
        log.info("Query Charge String:" + sb.toString());
        return sb.toString();
    }

    public int queryBalance(String userId, boolean testMode) throws CmccException{
        if(testMode){
            return 10000;
        }

        PostMethod method = new PostMethod(CMCC_QUERYBALANCE);
        try {
            StringRequestEntity entity = new StringRequestEntity(
                    getQueryBalanceString(userId), "text/xml", "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex1) {
        }
        HttpClient httpclient = new HttpClient();

        try {
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            httpclient.getParams().setSoTimeout(10000);
            int code = httpclient.executeMethod(method);
            if(code!=200)
                throw new CmccException("查询余额失败");
            SAXReader reader = new SAXReader();
            String s = method.getResponseBodyAsString().trim();
            log.info("Balance String:"+s);
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            int ret = Integer.parseInt(doc.getRootElement().elementText(
                    "hRet"));
            if (ret == 0) { //查询成功
                String sValue = doc.getRootElement().elementText("point");
                int index = sValue.indexOf(".");
                if(index>0){
                    sValue = sValue.substring(0,index);
                }
                return Integer.parseInt(sValue);
            }
            throw new CmccException("查询余额失败");
        } catch (NumberFormatException ex) {
            log.error(ex,ex);
            throw new CmccException("查询余额失败");
        } catch (DocumentException ex) {
            log.error(ex,ex);
            throw new CmccException("查询余额失败");
        } catch (IOException ex) {
            log.error(ex,ex);
            throw new CmccException("查询余额失败");
        } finally{
            method.releaseConnection();
        }
    }

    /*
    <?xml version="1.0" encoding="UTF-8"?>
    <request>
    <msgType>QueryBalanceReq</msgType>
    <sender>101</sender>
    <userIdType>1</userIdType>
    <userLabel>13888888888</userLabel>
    <channelId> String</channelId>
    </request>*/
    private String getQueryBalanceString(String userId){
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
        sb.append("</request>");
        log.info("Query Balanace String:"+sb.toString());
        return sb.toString();
    }

    public void chargeUp(String userId,String cpServiceId) throws CmccException{
        PostMethod method = new PostMethod(CMCC_CHARGEUP);
        try {
            StringRequestEntity entity = new StringRequestEntity(
                    getChargeUpString(userId,cpServiceId), "text/xml", "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex1) {
        }
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            httpclient.getParams().setSoTimeout(10000);
            int code = httpclient.executeMethod(method);
            if(code!=200)
                throw new CmccException("充值失败");
            SAXReader reader = new SAXReader();
            String s = method.getResponseBodyAsString().trim();
            log.info("ChargeUp Result:"+s);
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            int ret = Integer.parseInt(doc.getRootElement().elementText(
                    "hRet"));
            if(ret!=0)
                throw new CmccException("充值失败");

        } catch (DocumentException ex) {
            log.error(ex,ex);
            throw new CmccException("充值失败");
        } catch (IOException ex) {
            log.error(ex,ex);
            throw new CmccException("充值失败");
        } catch (NumberFormatException ex) {
            log.error(ex,ex);
            throw new CmccException("充值失败");
        } finally{
            method.releaseConnection();
        }
    }

    public void chargeUp(String userId,int charge) throws CmccException{
        if(charge<=0||charge>CHARGE_TABLE.length){
            throw new CmccException("充值失败");
        }
        String cpServiceId = CHARGE_TABLE[charge-1];
        chargeUp(userId,cpServiceId);
    }
  /**
    <msgType>ChargeUpReq</msgType>
    <sender>101</sender>
    <userIdType>1</userIdType>
    <userLabel>13888888888</userLabel>
    <channelId> String</channelId>
    <cpId>String</cpId>
    <cpServiceId>String</cpServiceId>
    </request>
    */

    private String getChargeUpString(String userId,String cpServiceId){
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
        sb.append(channelId);
        sb.append("</channelId>");
        sb.append("<cpId>");
        sb.append(CHARGECPID);
        sb.append("</cpId>");
        sb.append("<cpServiceId>");
        sb.append(cpServiceId);
        sb.append("</cpServiceId>");
        sb.append("</request>");
        log.info("ChargeUp String:"+sb.toString());
        return sb.toString();
    }

    public void buyGameTool(String userId,String consumeCode) throws CmccException{
        if(consumeCode.equals(ZERO)){
            return;
        }
        PostMethod method = new PostMethod(CMCC_BUYGAMETOOL);
        try {
            StringRequestEntity entity = new StringRequestEntity(
                    getBuyGameToolString(userId,cpServiceId,consumeCode), "text/xml", "utf-8");
            method.setRequestEntity(entity);
        } catch (UnsupportedEncodingException ex1) {
        }
        HttpClient httpclient = new HttpClient();
        try {
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            httpclient.getParams().setSoTimeout(10000);
            int code = httpclient.executeMethod(method);
            if(code!=200)
                throw new CmccException("购买道具失败");
            SAXReader reader = new SAXReader();
            String s = method.getResponseBodyAsString().trim();
            log.info("ChargeUp Result:"+s);
            Reader r = new StringReader(s);
            Document doc = reader.read(r);
            int ret = Integer.parseInt(doc.getRootElement().elementText(
                    "hRet"));
            if(ret==1182)
                throw new NoEnoughBalanceException("没有足够余额");
            if(ret!=0)
                throw new CmccException("购买道具失败");

        } catch (DocumentException ex) {
            log.error(ex,ex);
            throw new CmccException("购买道具失败");
        } catch (IOException ex) {
            log.error(ex,ex);
            throw new CmccException("购买道具失败");
        } catch (NumberFormatException ex) {
            log.error(ex,ex);
            throw new CmccException("购买道具失败");
        } finally{
            method.releaseConnection();
        }
    }

/*
    <request>
    <msgType>BuyGameToolReq</msgType>
    <sender>101</sender>
    <userId>1234567890</userId>
    <channelId> String</channelId>
    <cpId>String</cpId>
    <cpServiceId>String</cpServiceId>
    <consumeCode>String</consumeCode>
    </request>
 */

    private String getBuyGameToolString(String userId,String cpServiceId,String consumeCode){
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
        sb.append("<channelId>");
        sb.append(channelId);
        sb.append("</channelId>");
        sb.append("<cpId>");
        sb.append(cpId);
        sb.append("</cpId>");
        sb.append("<cpServiceId>");
        sb.append(cpServiceId);
        sb.append("</cpServiceId>");
        sb.append("<consumeCode>");
        sb.append(consumeCode);
        sb.append("</consumeCode>");
        sb.append("</request>");
        log.info("BuyGameTool String:"+sb.toString());
        return sb.toString();
    }
}
