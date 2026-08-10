package com.pip.server.billing.paypal;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.HashMap;

public class ConstPaypal {
	
	static DecimalFormat  df = new DecimalFormat("######0.00");
	
	/** 金额对应的i币额 KEY: 产品基本购买额度(i币数)，VALUES[赠送额度（i币数),人民币,美元,加拿大元,欧元,港元]
	 * double usdRate = 6.68;
	 * double eurRate = 9.33;
	 * double hkdRate = 0.86;
	 * double cadRate = 6.56;
	 * 其中货币单位均为元*100  
	 */
    public static HashMap<Integer, Integer[]> IMONEY_MAP = new HashMap<Integer, Integer[]>();
    static {
    	IMONEY_MAP.put(	3600	, new Integer[]{0,	1000	,	150,	152,	107,	1163});
    	IMONEY_MAP.put(	7200	, new Integer[]{0,	2000	,	299,	305,	214,	2326});
    	IMONEY_MAP.put(	10800	, new Integer[]{0,	3000	,	449,	457,	322,	3488});
    	IMONEY_MAP.put(	18000	, new Integer[]{0,	5000	,	749,	762,	536,	5814});
    	IMONEY_MAP.put(	36000	, new Integer[]{0,	10000	,	1497,	1524,	1072,	11628});
    	IMONEY_MAP.put(	108000	, new Integer[]{0,	30000	,	4491,	4573,	3215,	34884});
    	IMONEY_MAP.put(	180000	, new Integer[]{0,	50000	,	7485,	7622,	5359,	58140});
    	IMONEY_MAP.put(	360000	, new Integer[]{0,	100000	,	14970,	15244,	10718,	116279});
    	IMONEY_MAP.put(	720000	, new Integer[]{0,	200000	,	29940,	30488,	21436,	232558});
    }
    public static String ORDER_URL = "https://www.paypal.com/cgi-bin/webscr";
    
    public static String PAYPAL_ACCOUNT = "paypal@pearlinpalm.com";
	
    static HashMap<String, String[]> orderResults = new HashMap<String, String[]>();//订单结果
    
    //wap方式快速充值参数（Express Checkout on Mobile Devices）
    static String WAP_URL_TOCKEN_REQUEST = "https://api-3t.paypal.com/nvp";
    static String WAP_URL_REDIECT_REQUEST = "https://www.paypal.com/cgi-bin/webscr";
    static String WAP_USER = "pippay_api1.pearlinpalm.com";
    static String WAP_PWD = "NVSCMFFFEHGHM3U8";
    static String WAP_SIGN = "AkHA336vtfcv484ZbL0FjZd-3NPLAuEfpYW.ugG4ys-YMiNBk.sxylSX";
    static String WAP_VERSION = "3.2" ;

//  static String WAP_URL_TOCKEN_REQUEST = "https://api-3t.sandbox.paypal.com/nvp";
//  static String WAP_URL_REDIECT_REQUEST = "https://www.sandbox.paypal.com/cgi-bin/webscr";
//  static String WAP_USER = "sdk-three_api1.sdk.com";
//  static String WAP_PWD = "QFZCWN5HZM8VBG7Q";
//  static String WAP_SIGN = "A-IzJhZZjhg29XQ2qnhapuwxIDzyAZQ92FRP5dqBzVesOkzbdUONzmOU";


	/*通知回调地址*/
	static String callbackURL = "http://218.206.80.188/umpayfee/paypal_query.do?";
//	static String callbackURL = "http://192.168.30.166:8080/umpayfee/paypal_query.do?";
	
    public static String fmtCValue(int value){
        double newvalue = value/100.0;
        return df.format(newvalue).toString();
    }
    
    public static HashMap<String, String> parseParams(String str) {
        HashMap<String, String> params = new HashMap<String, String>();

        if(str!=null && str.length()>0){
           String[] fields = str.split("&");
           for(int i = 0;i< fields.length;i++){
               String[] npv = fields[i].split("=");
               if(npv.length>1){
                try {
                    params.put(npv[0], URLDecoder.decode(npv[1], "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                }
               }else{
                   params.put(npv[0],"");
               }
           }
        }
        return params;
    }
    
    public static String appendUrlParams(String baseUrl, String[]name, String[] values){
        if(name==null||values==null ||values.length<name.length){
        	return baseUrl;
        }
        for(int i =0;i<name.length;i++){
        	
        }
        return null;
    }
}
