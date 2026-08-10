package com.pip.server.billing.alipay;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;


public class Tools {

	private static Logger logger = Logger.getLogger(Tools.class);
	
	public static ClientConfig clientConfig = new ClientConfig();

	public static SecurityManager securityManager = new SecurityManagerImpl();
	
	public static Map<String, String> params = new HashMap<String, String>();
	
	public static String CALLBACK_URL = "http://218.206.80.188/umpayfee/alipay_result.do";
	
	//Android客户端专用参数，现阶段和通用的支付宝接口账户参数一致
	public static String publicKey_4Client = ""; 
	public static String privateKey_4Client = ""; 
	public static String validateKey_4Client = ""; 
	public static String partner_4Client = ""; 
	public static String seller_4Client = "";	
	
	/**WEB支付方式参数，现阶段和通用的支付宝接口账户参数一致**/
	public static String key_4web = "4tp5k81nj2aogjcxqzrq5vjbe8yi6pyp";
	public static String seller_4web = "";
	public static String partner_4web = "";
	
	public static String show_url_4web = "http://www.pipgame.com";
	public static String mainname_4web = "掌上明珠";
	public static String antiphishing_4web = "0";
	public static String input_charset_4web = "UTF-8";
	public static String sign_type_4web = "MD5";
	public static String transport_4web = "http";
	
	
//	public static HashMap<Integer, Integer> IMONEY_MAP = new HashMap<Integer, Integer>();
//    static {
//    	/*当前优惠3%*/
//        IMONEY_MAP.put(100, 37080);
//        IMONEY_MAP.put(500, 185400);
//        IMONEY_MAP.put(1000, 370800);
//    }
    
    /**
     * 根据充值金额计算对应i币。
     * @param amount 金额（分）
     * @return i币数量
     */
    public static int calcIMoney(int amount) {
  		/*当前优惠3%*/
       	return  amount * 36 * 103 / 1000;
    }
	static{
		params.put("service", clientConfig.getService());//服务addr
		params.put("sellerAccountName", clientConfig.getAcount());//卖家帐号(掌上飞讯)
		params.put("sec_id", clientConfig.getSecId());//安全配置编号
		params.put("partner_id", clientConfig.getPartnerId());//合作伙伴id
		params.put("format", clientConfig.getFormat());
		params.put("version", clientConfig.getVersion());//接口版本号
		
		publicKey_4Client = clientConfig.getPubkey();
		privateKey_4Client = clientConfig.getPrikey();
		validateKey_4Client = clientConfig.getAlipayPubKey();
		
		partner_4Client = clientConfig.getPartnerId();
		seller_4Client = clientConfig.getPartnerId();//和partner一致. 
		
		seller_4web = clientConfig.getAcount();
		partner_4web = clientConfig.getPartnerId();
	}

	/**
	 * 对参数进行签名
	 * 
	 * @param reqParams
	 * @return
	 */
	public static String sign(Map<String, String> reqParams) {
		String signData = ParameterUtil.getSignData(reqParams);
		String sign = "";
		try {
			sign = securityManager.sign(clientConfig.getSignAlgo(), signData,
					clientConfig.getPrikey());
		} catch (Exception e1) {
			logger.error("Sign:"+e1);
			e1.printStackTrace();
		}
		return sign;
	}

	/**
	 * 使用自己的私钥解密返回的结果，只需要对res_data的内容解密
	 * 
	 * @param resData
	 * @return
	 * @throws Exception
	 */
	private static String decryptResData(String resData) throws Exception {
		String data = "";
		data = securityManager.decrypt(clientConfig.getEncryptAlgo(), resData,
				clientConfig.getPrikey());
		return data;
	}

	/**
	 * 准备alipay.wap.trade.create.direct服务的参数
	 * @param Map<String,String>
	 * key:subject,商品名称
	 * key:total_fee,商品总价
	 * key:buyer_account_name,买家帐户
	 * key:call_back_url,回调URL
	 * @return Map<String,String>
	 */
	public static Map<String, String> prepareTradeRequestParamsMap(
			Map<String, String> map) {
		Map<String, String> requestParams = new HashMap<String, String>();

		String reqData = "<direct_trade_create_req><subject>"
				+ map.get("subject") + "</subject><out_trade_no>"
				+ map.get("outTradeNo") + "</out_trade_no><total_fee>"
				+ map.get("totalFee") + "</total_fee><seller_account_name>"
				+ map.get("sellerAccountName")
				+ "</seller_account_name><buyer_account_name>"
				+ map.get("buyerAccountName")
				+ "</buyer_account_name><notify_url>" + map.get("notifyUrl")
				+ "</notify_url>"
				+"<out_user>"+map.get("AccountID")+"</out_user>"+"<zero_pay>"+map.get("zero_pay")+"</zero_pay>"
				+"</direct_trade_create_req>";
		requestParams.put("req_data", reqData);
		requestParams.putAll(prepareCommonParams(map));

		return requestParams;
	}

	/**
	 * 准备alipay.wap.auth.authAndExecute服务的参数
	 * 
	 * @param request
	 * @param requestToken
	 * @return
	 */
	public static Map<String, String> prepareAuthParamsMap(
			Map<String, String> map, String requestToken) {
		Map<String, String> auth = new HashMap<String, String>();
		String reqData = "<auth_and_execute_req><request_token>" + requestToken
				+ "</request_token></auth_and_execute_req>";
		auth.put("req_data", reqData);
		auth.putAll(prepareCommonParams(map));
		String callBackUrl = map.get("call_back_url");
		auth.put("call_back_url", callBackUrl);
		auth.put("service", "alipay.wap.auth.authAndExecute");
		return auth;
	}

	/**
	 * 准备通用参数
	 * 
	 * @param request
	 * @return
	 */
	private static Map<String, String> prepareCommonParams(
			Map<String, String> map) {
		Map<String, String> commonParams = new HashMap<String, String>();
		String service = map.get("service");
		commonParams.put("service", service);
		String secId = map.get("sec_id");
		commonParams.put("sec_id", secId);
		String partner = map.get("partner_id");
		commonParams.put("partner", partner);
		String callBackUrl = map.get("call_back_url");
		commonParams.put("call_back_url", callBackUrl);
		String format = map.get("format");
		commonParams.put("format", format);
		String v = map.get("version");
		commonParams.put("v", v);
		return commonParams;
	}

	/**
	 * 调用alipay.wap.auth.authAndExecute服务的时候需要跳转到支付宝的页面，组装跳转url
	 * 
	 * @param reqParams
	 * @return
	 * @throws Exception
	 */
	public static String getRedirectUrl(Map<String, String> reqParams)
			throws Exception {
		String redirectUrl = clientConfig.getServerUrl() + ":"
				+ clientConfig.getServerPort() + "/service/rest.htm?";
		redirectUrl = redirectUrl + ParameterUtil.mapToUrl(reqParams);
		return redirectUrl;
	}

	/**
	 * 调用支付宝开放平台的服务
	 * 
	 * @param reqParams
	 *            请求参数
	 * @return
	 * @throws Exception
	 */
	public static ResponseResult send(Map<String, String> reqParams)
			throws Exception {
		String response = "";
		String invokeUrl = clientConfig.getServerUrl() + ":"
				+ clientConfig.getServerPort() + "/service/rest.htm?";
		// String invokeUrl = "https://paygw.alipay.com/service/rest.htm?";
		URL serverUrl = new URL(invokeUrl);
		HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();

		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.connect();
		String params = ParameterUtil.mapToUrl(reqParams);
		conn.getOutputStream().write(params.getBytes());

		InputStream is = conn.getInputStream();

		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String line = "";
		while ((line = in.readLine()) != null) {
			buffer.append(line);
		}
		response = URLDecoder.decode(buffer.toString(), "utf-8");
		conn.disconnect();
		return praseResult(response);
	}

	/**
	 * 解析开放平台返回的结果
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private static ResponseResult praseResult(String response) throws Exception {
		// 调用成功
		HashMap<String, String> resMap = new HashMap<String, String>();
		String v = ParameterUtil.getParameter(response, "v");
		String service = ParameterUtil.getParameter(response, "service");
		String partner = ParameterUtil.getParameter(response, "partner");
		String secId = ParameterUtil.getParameter(response, "sec_id");
		String sign = ParameterUtil.getParameter(response, "sign");
		resMap.put("v", v);
		resMap.put("service", service);
		resMap.put("partner", partner);
		resMap.put("sec_id", secId);
		
		String businessResult = "";
		ResponseResult result = new ResponseResult();
		if (response.contains("<err>")) {
			result.setSuccess(false);
			businessResult = ParameterUtil.getParameter(response, "res_error");

			// 转换错误信息
			XMapUtil.register(ErrorCode.class);
			ErrorCode errorCode = (ErrorCode) XMapUtil
					.load(new ByteArrayInputStream(businessResult
							.getBytes("UTF-8")));
			result.setErrorMessage(errorCode);

			resMap.put("res_error", ParameterUtil.getParameter(response,
					"res_error"));
		} else {
			businessResult = ParameterUtil.getParameter(response, "res_data");
			businessResult = decryptResData(businessResult);
			result.setSuccess(true);
			result.setBusinessResult(businessResult);
			resMap.put("res_data", businessResult);
		}
		String verifyData = ParameterUtil.getSignData(resMap);
		boolean verified = Tools.securityManager.verify(clientConfig
				.getSignAlgo(), verifyData, sign, clientConfig
				.getAlipayPubKey());

		if (!verified) {
			throw new Exception("验证签名失败");
		}
		return result;
	}

	/**
	 * 检查字符串是否是空白：<code>null</code>、空字符串<code>""</code>或只有空白字符。
	 * 
	 * <pre>
	 * StringUtil.isBlank(null)      = true
	 * StringUtil.isBlank(&quot;&quot;)        = true
	 * StringUtil.isBlank(&quot; &quot;)       = true
	 * StringUtil.isBlank(&quot;bob&quot;)     = false
	 * StringUtil.isBlank(&quot;  bob  &quot;) = false
	 * </pre>
	 * 
	 * @param str
	 *            要检查的字符串
	 * 
	 * @return 如果为空白, 则返回<code>true</code>
	 */
	public static boolean isBlank(String str) {
		int length;

		if ((str == null) || ((length = str.length()) == 0)) {
			return true;
		}

		for (int i = 0; i < length; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 检查字符串是否不是空白：<code>null</code>、空字符串<code>""</code>或只有空白字符。
	 * 
	 * <pre>
	 * StringUtil.isBlank(null)      = false
	 * StringUtil.isBlank(&quot;&quot;)        = false
	 * StringUtil.isBlank(&quot; &quot;)       = false
	 * StringUtil.isBlank(&quot;bob&quot;)     = true
	 * StringUtil.isBlank(&quot;  bob  &quot;) = true
	 * </pre>
	 * 
	 * @param str
	 *            要检查的字符串
	 * 
	 * @return 如果为空白, 则返回<code>true</code>
	 */
	public static boolean isNotBlank(String str) {
		int length;

		if ((str == null) || ((length = str.length()) == 0)) {
			return false;
		}

		for (int i = 0; i < length; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}

		return false;
	}

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	/**
	 * 从输入流读取内容, 写入到输出流中. 此方法使用大小为4096字符的默认的缓冲区.
	 * 
	 * @param in
	 *            输入流
	 * @param out
	 *            输出流
	 * 
	 * @throws IOException
	 *             输入输出异常
	 */
	public static void io(Reader in, Writer out) throws IOException {
		io(in, out, -1);
	}

	/**
	 * 从输入流读取内容, 写入到输出流中. 使用指定大小的缓冲区.
	 * 
	 * @param in
	 *            输入流
	 * @param out
	 *            输出流
	 * @param bufferSize
	 *            缓冲区大小(字符数)
	 * 
	 * @throws IOException
	 *             输入输出异常
	 */
	public static void io(Reader in, Writer out, int bufferSize)
			throws IOException {
		if (bufferSize == -1) {
			bufferSize = DEFAULT_BUFFER_SIZE >> 1;
		}

		char[] buffer = new char[bufferSize];
		int amount;

		while ((amount = in.read(buffer)) >= 0) {
			out.write(buffer, 0, amount);
		}
	}

	/**
	 * 将指定输入流的所有文本全部读出到一个字符串中.
	 * 
	 * @param in
	 *            要读取的输入流
	 * 
	 * @return 从输入流中取得的文本
	 * 
	 * @throws IOException
	 *             输入输出异常
	 */
	public static String readText(InputStream in) throws IOException {
		return readText(in, null, -1);
	}

	/**
	 * 将指定输入流的所有文本全部读出到一个字符串中.
	 * 
	 * @param in
	 *            要读取的输入流
	 * @param encoding
	 *            文本编码方式
	 * @param bufferSize
	 *            缓冲区大小(字符数)
	 * 
	 * @return 从输入流中取得的文本
	 * 
	 * @throws IOException
	 *             输入输出异常
	 */
	public static String readText(InputStream in, String encoding,
			int bufferSize) throws IOException {
		Reader reader = (encoding == null) ? new InputStreamReader(in)
				: new InputStreamReader(in, encoding);

		return readText(reader, bufferSize);
	}

	/**
	 * 将指定<code>Reader</code>的所有文本全部读出到一个字符串中.
	 * 
	 * @param reader
	 *            要读取的<code>Reader</code>
	 * @param bufferSize
	 *            缓冲区的大小(字符数)
	 * 
	 * @return 从<code>Reader</code>中取得的文本
	 * 
	 * @throws IOException
	 *             输入输出异常
	 */
	public static String readText(Reader reader, int bufferSize)
			throws IOException {
		StringWriter writer = new StringWriter();

		io(reader, writer, bufferSize);
		return writer.toString();
	}
	
	/**序列号生成**/
	
    protected final static char[] NUM = { '0', '1', '2', '3', '4', '5', '6',
		'7', '8', '9' };

	public static String getRandom(Random rnd) {
		char[] ret = new char[4];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = NUM[rnd.nextInt(ret.length)];
		}
		return new String(ret);
	}
}
