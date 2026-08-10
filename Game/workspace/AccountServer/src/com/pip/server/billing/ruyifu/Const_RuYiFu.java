package com.pip.server.billing.ruyifu;

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class Const_RuYiFu {
    public static String VERSION = "5";
    public static String MERCHANT_USERID = "8265948421";
    public static String MERCHANT_KEY = "b5a144f5";
    public static String MERCHANT_PASS = "";
    public static String ORDER_URL = "http://api.ruyifu.net/Notify5/pay";
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    public static int PAYTYPE_NEWCARD = 1;
    public static int PAYTYPE_SINGLE = 1;
    public static String INTERFACE_PAY = "pay";
    public static int OPERATOR_CMCC = 1;
    public static int OPERATOR_UNICOM = 2;
    public static int OPERATOR_CTEL = 3;
    
    public static HashMap<Integer, Integer> IMONEY_MAP = new HashMap<Integer, Integer>();
    static {
    	IMONEY_MAP.put(2000, 7272);
    	IMONEY_MAP.put(3000, 10908);
        IMONEY_MAP.put(5000, 18360);
        IMONEY_MAP.put(10000, 37440);
    }
    
    private static HashMap<String, String> errorMessages = new HashMap<String, String>();
    static {
        errorMessages.put("ERROR0001", "版本号错误");
        errorMessages.put("ERROR0002", "商户ID不正确");  
        errorMessages.put("ERROR0003", "定单失效");
        errorMessages.put("ERROR0004", "定单号重复");
        errorMessages.put("ERROR0005", "商户密码错误");
        errorMessages.put("ERROR0006", "非法提交");
        errorMessages.put("ERROR0007", "同步或异步地址不正确");
        errorMessages.put("ERROR0008", "玩家已经被锁定");
        errorMessages.put("ERROR0009", "商户已经被禁用");
        errorMessages.put("ERROR0010", "金额不正确");
        errorMessages.put("ERROR0011", "参数不完整");
        errorMessages.put("ERROR0012", "参数不正确");
        errorMessages.put("ERROR0013", "系统错误，请联系管理员");
        errorMessages.put("ERROR0014", "全国移动卡，卡号，密码，金额不正确");
        errorMessages.put("ERROR0015", "广东联通，卡号，密码，金额不正确");
        errorMessages.put("ERROR0016", "江苏移动卡，卡号，密码，金额不正确");
        errorMessages.put("ERROR0017", "辽宁移动卡，卡号，密码，金额不正确");
        errorMessages.put("ERROR0018", "浙江移动卡，卡号，密码，金额不正确");
        errorMessages.put("ERROR0019", "不能用此卡继续做交易");
        errorMessages.put("ERROR0020", "余额卡不存在");
        errorMessages.put("ERROR0021", "定单失效");
        errorMessages.put("ERROR0022", "金额不够，请选择多次支付！");
        errorMessages.put("ERROR0023", "请选择运营商");
        errorMessages.put("ERROR0024", "请选择卡类型");
        errorMessages.put("ERROR0025", "卡金额错误");
        errorMessages.put("ERROR0026", "定单号不正确");   
        errorMessages.put("ERROR0027", "参数为空");
        errorMessages.put("ERROR0028", "用户名不规范");
        errorMessages.put("ERROR0029", "商户ID错误");
        errorMessages.put("ERROR0030", "商户验证错误");
        errorMessages.put("ERROR0031", "商户被锁定");
        errorMessages.put("ERROR0032", "此卡已被使用");
        errorMessages.put("ERROR0033", "系统忙");
        errorMessages.put("ERROR0034", "无效输入");
        errorMessages.put("ERROR0035", "认证失败");
        errorMessages.put("ERROR0036", "保存数据失败");
        errorMessages.put("ERROR0037", "无效卡");
        errorMessages.put("ERROR0038", "处理中...");
        errorMessages.put("ERROR0039", "密码尝试过多");
        errorMessages.put("ERROR0040", "卡密码错误");
        errorMessages.put("ERROR0041", "系统错误");
        errorMessages.put("ERROR0042", "充值卡类型不正确");
        errorMessages.put("ERROR0043", "骏网一卡通卡规则不正确");
        errorMessages.put("ERROR0044", "骏网一卡通余额不足");
        errorMessages.put("ERROR0045", "卡号或密码为空");
        errorMessages.put("ERROR0046", "卡号或密码必须为数字");
        errorMessages.put("ERROR0047", "您使用的充值卡为地方专用卡，不能支付");
        errorMessages.put("ERROR0047", "系统繁忙时，系统将先接受此卡，待处理完成后会通过异步通知结果");
    }
    
    public static String getErrorMessage(String errorCode) {
        String message = errorMessages.get(errorCode);
        if (message == null) {
            return "未知错误";
        } else {
            return message;
        }
    }
}
