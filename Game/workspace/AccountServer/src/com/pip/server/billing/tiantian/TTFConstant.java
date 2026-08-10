package com.pip.server.billing.tiantian;

import java.util.HashMap;

public class TTFConstant {
	//public static String merId = "supply001";
	public static String merId = "zsmz";
	public static String key = "778899";
	public static String URL="http://pay.tiantianfu.com:8888/site/topup/cardBackPay.php";
	
	public static HashMap<String,String> CODE_MSG = new HashMap<String,String>();
	static {
		CODE_MSG.put("001","成功接收");
		CODE_MSG.put("002","暂不支持此类充值卡");
		CODE_MSG.put("003","卡密区域不一致");
		CODE_MSG.put("004","面值不正确");
		CODE_MSG.put("005","无效的充值卡，支付失败");
		CODE_MSG.put("006","充值卡面额大于实际金额");
		CODE_MSG.put("007","充值卡面额小于实际金额");
		CODE_MSG.put("008","充值金额与交易金额不符");
		CODE_MSG.put("010","重复的订单");
		CODE_MSG.put("011","交易正在处理中");
		CODE_MSG.put("012","签名校验失败");
		CODE_MSG.put("013","提交的商户ID不存在");
		CODE_MSG.put("018","交易信息不存在");
		CODE_MSG.put("019","用户提交的卡密错误率太高，暂停接收该用户信息");
		CODE_MSG.put("020","其他错误");
		CODE_MSG.put("021","提交的充值卡信息有误");
		CODE_MSG.put("023","状态不确定，需客服处理");
		CODE_MSG.put("055","交易成功完成");
		CODE_MSG.put("070","对于余额支付，请求支付的金额大于余额返回该代码");
	}
}
