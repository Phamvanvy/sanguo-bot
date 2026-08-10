package com.pip.itimes.server.auth;

import java.io.*;
import java.util.Calendar;
import java.util.Date;

/**
 * 保存常量的类
 * 
 * @author Frank
 *
 */
public class Const {
   
    /** 在用户续费页面上，续费结果跳转页面 */
    public static final String PORTAL_REDIRECT = 
        "http://wap.pipfit.cn:7070/fantasy4wap/feeresult.do?code=";
    public static final String PORTAL_REDIRECT_UNIPAY = 
        "http://wap.pipfit.cn:7070/fantasy4wap/feeresult_unipay.do?code=";

    /** 返回上面页面的返回码定义 */
    public static final int OK = 0;                    // 续费成功
    public static final int ACCOUNT_NOT_FOUND = 1;     // 帐号不存在
    public static final int ACCOUNT_INPUT_ERROR = 2;   // 帐号两次输入不一致
    public static final int INTERNAL_ERROR = 3;        // 内部错误
    public static final int NOT_ALLOWED = 4;           // 暂时不允许充值
    public static final int INPUT_ERROR = 5;           // 输入错误
    public static final int PLACE_ORDER_OK = 6;        // 续费请求发送成功，请等待短信通知，按短信提示操作续费。
    public static final int BILL_NOT_FOUND = 7;        // 账单不存在或已经续费完成
    
    
    // 下面是和计费相关的一些参数。
    
    /** 每月最大扣费数 */
    public static final int MONTH_MAX = 576000;
    /** 每一元钱对应的i币数量 */
    public static final int IMONEY_RATE = 36000;
    /** 各续费渠道冲值一次对应的i币数量 */
    public static final int LINKRICH_WAP_FEE = 2 * IMONEY_RATE;  // 联丰WAP续费
    public static final int SINA_WAP_FEE = 2 * IMONEY_RATE;      // 新浪WAP续费
    public static final int UNIPAY_FEE_020 = 2 * IMONEY_RATE;    // 手机钱包2元续费
    public static final int UNIPAY_FEE_080 = 8 * IMONEY_RATE;    // 手机钱包8元续费
    public static final int UNIPAY_FEE_150 = 16 * IMONEY_RATE;   // 手机钱包15元续费
    public static final int UNIPAY_FEE_160 = 16 * IMONEY_RATE;    // 手机钱包16元续费，送1.5元
    public static final int UNIPAY_FEE_300 = 303 * IMONEY_RATE / 10; 	// 手机钱包30元续费，送0.3元
    
    /** 续费渠道编码 */
    public static final String LINKRICH = "linkrich";
    public static final String SINA = "sina";
    public static final String UNIPAY = "unipay";
    public static final String UNIPAY_REG = "unipay_reg";

    /** 各渠道最大余额 */
    public static java.util.HashMap BALANCE_MAX = new java.util.HashMap();
    static {
        BALANCE_MAX.put(LINKRICH, new Integer(576000));
        BALANCE_MAX.put(SINA, new Integer(576000));
        BALANCE_MAX.put(UNIPAY, new Integer(57600000));
        BALANCE_MAX.put(UNIPAY_REG, new Integer(57600000));
    }

    /** 联丰WAP续费参数 */
    public static final String LINKRICH_WAP_FEE_URL = 
        "http://wap.monternet.com/reversesubscribe?SPID=900603&ServiceID=31032031&SPURL=http://wap.8002.cn/lr/mot/zzlxy/sfee.jspg.jsp?s=110000&r=110000&url=http://<host>:<port>/WapFeeOkOld?FeeID=";
   
    /** 新浪WAP续费参数 */
    public static final String SINA_WAP_FEE_PRE_URL = 
        "http://wap.sina.com.cn/cgi-bin/ad/service.cgi?id=03202048&from=60648&url=http://<host>:<port>/WapFeePreOld?FeeID=";
    public static final String SINA_WAP_FEE_URL = 
        "http://wap.monternet.com/reversesubscribe?SPID=900501&ServiceID=03202048&SPURL=http://wapdl.sina.com.cn/common/dlf/down.php?bid=5000054&from=charge&chargeurl=http://<host>:<port>/WapFeeOkOld?FeeID=";
//    public static final String SINA_WAP_FEE_PRE_URL_NEW = 
//      "http://wap.sina.com.cn/cgi-bin/ad/service.cgi?id=03202048&from=60648&url=http://<host>:<port>/WapFeePre1?FeeID=";
//    public static final String SINA_WAP_FEE_URL_NEW = 
//      "http://wap.monternet.com/reversesubscribe?SPID=900501&ServiceID=03202048&SPURL=http://wapdl.sina.com.cn/common/dlf/down.php?bid=4&from=wulin&chargeurl=http://<host>:<port>/WapFeeOk1?FeeID=";
    public static final String SINA_WAP_FEE_PRE_URL_NEW = 
        "http://<host>:<port>/WapFeePre1?FeeID=";
    public static final String SINA_WAP_FEE_URL_NEW = 
        "http://wap.monternet.com/reversesubscribe?SPID=900501&ServiceID=06010368&SPURL=http://wapdl.sina.com.cn/common/dlf/down.php?bid=wll&from=game&f=60303&id=101849&u=http://<host>:<port>/WapFeeOk1?FeeID=";
    /** 每个手机每月充值上限。 */
    public static final int MONTH_WAPPAY_MAX = MONTH_MAX;
    
    /** 手机钱包续费参数 */

    /** 向手机钱包平台下单的URL */
    public static final String TAKE_ORDER_URL = "http://211.154.41.244/webpay/spBackPer.do";
    /** 按次计费支付功能码 */
    public static final String UNIPAY_FUNCODE = "8817";
    /** 分配给PiP的SP代码 */
    public static final String UNIPAY_SPID = "5557";
    /** 2元计次产品描述 */
    public static final String UNIPAY_REMARK2 = "您购买的720明珠i币已充入明珠通行证%account%"; //"您购买的明珠幻想720i币已成功充入帐户%account%，请查收";
    /** 2元注册产品描述 */
    public static final String UNIPAY_REMARK2_2 = "您已成功注册明珠幻想帐户%account%，密码%password%";
    /** 2元找回密码产品描述 */
    public static final String UNIPAY_REMARK2_1 = "您的掌上明珠通行证是%account%，密码是%password%"; //"您的明珠幻想帐号是%account%，密码%password%";
    /** 2元修改绑定手机号产品描述 */
    public static final String UNIPAY_REMARK2_3 = "您的明珠幻想帐号%account%已绑定新号码%phone%";
    /** 15元计次产品描述 */
    public static final String UNIPAY_REMARK15 = "您已成为明珠幻想包月用户，当月游戏不再计时收费";
    /** 15元包月产品描述 */
    public static final String UNIPAY_REMARK151 = "恭喜您成为明珠幻想包月优惠卡用户，当月游戏不再收取计时费用"; //"您已成为明珠幻想包月用户，帐号%account%";
    /** 8元计次产品描述 */
    public static final String UNIPAY_REMARK8 = "您购买的2880明珠i币已充入明珠通行证%account%"; //"您购买的明珠幻想2880i币已成功充入帐户%account%，请查收";
    /** 16元计次产品描述 */
    public static final String UNIPAY_REMARK16 = "您购买的5760明珠i币已充入明珠通行证%account%"; //"您购买的明珠幻想6300i币已成功充入帐户%account%，请查收";
    /** 30元计次产品描述 */
    public static final String UNIPAY_REMARK30 = "您购买的10908明珠i币已充入明珠通行证%account%"; //"您购买的明珠幻想6300i币已成功充入帐户%account%，请查收";
    /** 2元计次产品WAP下单地址 */
    public static final String UNIPAY_ORDER_URL_2 = "http://hfwap.umpay.com/r.aspx?id=45x127x7x26x2,020!";
    /** 2元找回密码产品WAP下单地址 */
    public static final String UNIPAY_ORDER_URL_21 = "http://hfwap.umpay.com/r.aspx?id=45x0x7x29x2,021!";
    /** 15元计次产品WAP下单地址 */
    public static final String UNIPAY_ORDER_URL_15 = "http://hfwap.umpay.com/r.aspx?id=45x127x7x27x2,150!";
    /** 15元包月产品WAP下单地址 */
    public static final String UNIPAY_ORDER_URL_151 = "http://hfwap.umpay.com/r.aspx?id=45x0x7x40x3,555701!";
    /** 8元计次产品WAP下单地址 */
    public static final String UNIPAY_ORDER_URL_8 = "http://hfwap.umpay.com/r.aspx?id=45x0x7x41x2,080!";
    /** 16元计次产品WAP下单地址 */
    public static final String UNIPAY_ORDER_URL_16 = "http://hfwap.umpay.com/r.aspx?id=45x0x7x66x2,160!";
    /** 30元计次产品WAP下单地址 */
    public static final String UNIPAY_ORDER_URL_30 = "http://hfwap.umpay.com/r.aspx?id=45x127x7x95x2,300!";
    /** WEB 对账地址 */
    public static final String UNIPAY_REPORT_URL = "http://211.136.93.21/webpay/spDayTransBill.do";
    /** 查询包月订购关系地址 */
    public static final String UNIPAY_QUERY_SUB_URL = "http://211.136.93.21/webpay/spQueryUserRegState.do";
    /** 取消包月订购关系地址 */
    public static final String UNIPAY_CANCEL_SUB_URL = "http://211.136.93.21/webpay/spCancelUserRegInfo.do";
    
    /** 把对象转换为String。如果对象为null，返回空串。 */
    public static String objectToString(Object obj) {
        try {
            if (obj == null) {
                return "";
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            String ret = new sun.misc.BASE64Encoder().encode(bos.toByteArray());
            ret = ret.replace("\n", "");
            ret = ret.replace("\r", "");
            return ret;
        } catch (Exception e) {
            return "";
        }
    }
    
    /** 把String转换为对象。 */
    public static Object stringToObject(String str) {
        try {
            if (str == null || str.length() == 0) {
                return null;
            }
            byte[] data = new sun.misc.BASE64Decoder().decodeBuffer(str);
            return new ObjectInputStream(new ByteArrayInputStream(data)).readObject();
        }catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 检查某个时间是否在本月或者将来的月份。
     */
    public static boolean inLaterMonth(java.util.Date checkDate, java.util.Date now) {
        if (checkDate == null) {
            return false;
        }
        Calendar current = Calendar.getInstance();
        Calendar last = Calendar.getInstance();

        current.setTime(now);
        last.setTime(checkDate);
        int m1 = last.get(Calendar.YEAR) * 100 + last.get(Calendar.MONTH);
        int m2 = current.get(Calendar.YEAR) * 100 + current.get(Calendar.MONTH);
        return m1 >= m2;
    }
}
