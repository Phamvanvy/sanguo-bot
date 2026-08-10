package com.pip.server.billing.alipay;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.apache.log4j.Logger;

public class DoAlipay {
	
	private static Logger logger = Logger.getLogger(DoAlipay.class);
	
	/**
	 * 创建交易接口 step1
	 * @param reqParams
	 * @return
	 */
	public static String TradeCreate(Map<String, String> reqParams)
	{
		logger.info("CreateTrade:req_data[" + reqParams.get("req_data") + "],call_back_url["
				+reqParams.get("call_back_url")+"]");
		
		String sign = Tools.sign(reqParams);
		logger.info("signCreate["+sign+"]");
		reqParams.put("sign", sign);
		ResponseResult resResult = new ResponseResult();
		
		try {
			resResult = Tools.send(reqParams);
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error("Create Trade Exception:"+e1);
			return "";
		}
		String businessResult = "";
		if (resResult !=null && resResult.isSuccess()) {
            businessResult = resResult.getBusinessResult();
        } else {
            return "ERROR";
        }
		return businessResult;
	}
	
	/**
	 * 验证执行接口 step2
	 * @param businessResult
	 * @param reqParams
	 * @return
	 */
	public static String AuthAndExecute(String businessResult,Map<String, String> reqParams){
		logger.info("AuthAndExecute:req_data[" + reqParams.get("req_data") + "],call_back_url["
				+reqParams.get("call_back_url")+"]");
		
		DirectTradeCreateRes directTradeCreateRes = null;
		XMapUtil.register(DirectTradeCreateRes.class);
		
		try {
            directTradeCreateRes = (DirectTradeCreateRes) XMapUtil.load(new ByteArrayInputStream(
                businessResult.getBytes("UTF-8")));
        }  catch (Exception e) {
        	logger.error("XMapUtil.load:"+e);
        }
        String requestToken = directTradeCreateRes.getRequestToken();
        logger.info("requestToken["+requestToken+"]");
        Map<String, String> authParams = Tools.prepareAuthParamsMap(reqParams,requestToken);
        String authSign = Tools.sign(authParams);
        logger.info("signAuthAndExecute["+authSign+"]");
        authParams.put("sign", authSign);
        String redirectURL = "";
        try {
            redirectURL = Tools.getRedirectUrl(authParams);
        } catch (Exception e) {
        	logger.error("MapToUrl:"+e);
        }
		logger.info("URLAuthAndExecute["+redirectURL+"]");
        return redirectURL;
	}
}
