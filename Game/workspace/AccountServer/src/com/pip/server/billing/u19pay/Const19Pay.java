package com.pip.server.billing.u19pay;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.pip.server.account.util.Util;

public class Const19Pay {
	public static Logger log = Logger.getLogger(Const19Pay.class);
	
    public static String VERSION = "2.00";
    public static String MERCHANT_ID = "2083";
    public static String MERCHANT_KEY = "zlnaxvc6sfxquakfi6fuurttpn2lemge3lig8xicgxzrz93jfxxikufhivb1ton65gh6qywp3s54y7ia7203y5uksx64ajr3cb9s9d8c4zuugwp5ukcb7gxw1y66bwap";
//    public static String MERCHANT_KEY = "123456789";
    public static String ORDER_URL = "http://pay.19pay.com/page/wap/waporder.do";
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    public static SimpleDateFormat DATE_FORMAT2 = new SimpleDateFormat("yyyyMMddHHmmss");
    public static String CURRENCY_RMB = "RMB";
    public static String METHOD_UNICOM = "LTJFK";
    public static String CALLBACK_URL = "http://218.206.80.188/umpayfee/19pay_result.do";
    
//    测试商户id(merchant_id)：2083 
//    测试密钥(merchant_key):123456789  
//    测试卡生成地址: http://114.255.7.208/page/test/createcard2.jsp
//    用这个生成测试卡，每个卡只能用一次  

    // 直连方式参数
    public static String GET_PARAM_URL = "http://pay.19pay.com/channel.jsp";
    public static String ORDER_URL_D = "http://pay.19pay.com/pgworder/orderdirect.do";
//    public static String GET_PARAM_URL = "http://114.255.7.208/channel.jsp";
    // public static String ORDER_URL_D = "http://114.255.7.208/pgworder/orderdirect.do";
    public static String[][] CARD_TYPES = {
    	{ "LTJFK00020000", "LTJFK", "全国", "全国联通一卡充" },
    	{ "CMJFK00010001", "CMJFK", "全国", "全国移动充值卡" },
    	{ "CMJFK00010102", "CMJFK", "辽宁", "辽宁移动电话交费卡" },
    	{ "CMJFK00010111", "CMJFK", "江苏", "江苏移动充值卡" },
    	{ "CMJFK00010112", "CMJFK", "浙江", "浙江移动缴费券" },
    	{ "CMJFK00010014", "CMJFK", "福建", "福建移动呱呱通充值卡" }
    };
    
    public static HashMap<Integer, Integer> IMONEY_MAP = new HashMap<Integer, Integer>();
    static {
        IMONEY_MAP.put(2000, 7272);
        IMONEY_MAP.put(3000, 10908);
        IMONEY_MAP.put(5000, 18360);
        IMONEY_MAP.put(10000, 37440);
        IMONEY_MAP.put(30000, 116640);
        IMONEY_MAP.put(50000, 198000);
    }
    
    public static String getMD5(String raw) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] output = md5.digest(raw.getBytes("ISO_8859_1"));
            return Util.getHexString(output);
        } catch (Exception e) {
            return "";
        }
    }
    
    private static HashMap<Integer, String> errorMessages = new HashMap<Integer, String>();
    static {
        errorMessages.put(2, "支付失败！您选择的充值卡面值与实际面值不符。");
        errorMessages.put(4, "支付失败！您选择的充值卡面值与实际面值不符。");
        errorMessages.put(10014, "系统忙，请稍后再试");
        errorMessages.put(10016, "该订单支付已成功，不能重复提交");
        errorMessages.put(10029, "运营商系统维护，支付通道暂时关闭");
        errorMessages.put(10030, "运营商系统维护，该面值暂时关闭");
        errorMessages.put(10076, "该订单支付已失败，不能重复提交");
        errorMessages.put(10082, "该卡已被使用，请更换其他充值卡支付");
        errorMessages.put(10083, "很抱歉！该卡已连续二次支付不成功，请更换其他充值卡支付。");
        errorMessages.put(10091, "该卡正在处理中，请不要重复提交");
        errorMessages.put(10110, "系统忙，请稍后再试");
        errorMessages.put(10119, "充值卡面额选择错误");
        errorMessages.put(10120, "该订单正在处理中，不能重复提交");
        errorMessages.put(10123, "系统忙，请稍后再试");
        errorMessages.put(10124, "由于运营商系统临时维护，该省充值卡暂时无法支付，请稍后再试。");
        errorMessages.put(81000, "该卡已失效，请更换其他充值卡支付");
        errorMessages.put(81001, "系统忙，请稍后再试");
        errorMessages.put(81007, "充值卡卡号或密码错误");
        errorMessages.put(82009, "系统忙，请稍后再试");
    }
    
    public static String getErrorMessage(int errorCode) {
        String message = errorMessages.get(errorCode);
        if (message == null) {
            return "未知错误";
        } else {
            return message;
        }
    }
    
    public static long lastCheckParamTime = 0;
    public synchronized static void checkParam() {
    	if (System.currentTimeMillis() < lastCheckParamTime + 60000L) {
    		// 10分钟检查一次
    		return;
    	}
    	lastCheckParamTime = System.currentTimeMillis();
    	
        GetMethod method = new GetMethod(GET_PARAM_URL + "?merchant_id=" + MERCHANT_ID);
        method.addRequestHeader( "Connection", "close");
        try {
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code == 200) {
                String s = method.getResponseBodyAsString().trim();
                String[] arr = s.split("\\|");
                for (int i = 0; i < arr.length; i += 4) {
                	String pc_id = arr[i];
                	String pm_id = arr[i + 1];
                	String province = arr[i + 2];
                	String desc = arr[i + 3];
                	for (int j = 0; j < CARD_TYPES.length; j++) {
                		if (desc.equals(CARD_TYPES[j][3])) {
                			CARD_TYPES[j][0] = pc_id;
                			CARD_TYPES[j][1] = pm_id;
                			CARD_TYPES[j][2] = province;
                			break;
                		}
                	}
                }
            }
        } catch (Exception e) {
        	log.error(e, e);
        }
    }

    /**
     * 检查卡号和密码是否合法。
     * 目前支持的卡号和密码都必须全部是数字，可用的位数包括：
     * 全国移动充值卡	17	18
     * 全国联通一卡充	15	19
     * 全国电信卡	19	18
     * 福建移动呱呱通充值卡	16	17
     * 江苏移动充值卡	16	17
     * 辽宁移动电话交费卡	16	21
     * 浙江移动缴费券	10	8
     * @param cardno 卡号
     * @param cardpass 密码
     * @return 如果不合法，返回false。
     */
    public static boolean checkInput(String cardno, String cardpass) {
    	if (isDigit(cardno) && isDigit(cardpass)) {
    		int cl = cardno.length();
    		int cp = cardpass.length();
    		if (cl == 17 && cp == 18) {
    			return true;
    		}
    		if (cl == 15 && cp == 19) {
    			return true;
    		}
    		if (cl == 19 && cp == 18) {
    			return true;
    		}
    		if (cl == 16 && cp == 17) {
    			return true;
    		}
    		if (cl == 16 && cp == 21) {
    			return true;
    		}
    		if (cl == 10 && cp == 8) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /*
     * 判断一个字符串是否全是数字。
     */
    private static boolean isDigit(String str) {
    	for (int i = str.length() - 1; i >= 0; i--) {
    		char ch = str.charAt(i);
    		if (ch < '0' || ch > '9') {
    			return false;
    		}
    	}
    	return true;
    }
}
