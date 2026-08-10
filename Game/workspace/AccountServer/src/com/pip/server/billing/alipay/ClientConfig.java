package com.pip.server.billing.alipay;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ClientConfig {

	private static Logger logger = Logger.getLogger(ClientConfig.class);
	/**
	 * 支付宝开放平台服务调用的地址 线上环境为https://paygw.alipay.com
	 */
	private String serverUrl = "https://paygw.alipay.com";//"http://121.0.29.39";

	/**
	 * 支付宝开放平台服务调用的端口 在真实的线上环境中 请使用443端口
	 */
//	private String serverPort = "80";
	private String serverPort = "443";

	/**
	 * 商户的partnerId
	 */
	private String partnerId = "";

	/**
	 * 商户的安全配置号
	 */
	private String secId = "";

	/**
	 * 商户的私钥
	 */
	private String prikey = "";

	/**
	 * 商户的公钥
	 */
	private String pubkey = "";

	/**
	 * 支付宝开放平台的公钥
	 */
	private String alipayPubKey = "";

	/**
	 * 签名的算法 本次示例采用的是RSA
	 */
	private String signAlgo = "RSA";

	/**
	 * 加密的算法 本次示例使用的是RSA
	 */
	private String encryptAlgo = "RSA";
	
	/**卖家帐户*/
	private String acount = "";
	
	/**数据格式*/
	private String format = "";
	
	/**连接版本*/
	private String version = "";
	
	private String service = "";

	public ClientConfig() {
		try {

			InputStream iss = this.getClass().getClassLoader()
					.getResourceAsStream("com/pip/server/billing/alipay/config.xml");
			DocumentBuilderFactory domfac = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dombuilder = domfac.newDocumentBuilder();
			Document doc = dombuilder.parse(iss);
			Element rootElement = doc.getDocumentElement();
			
			NodeList paramNode = doc.getElementsByTagName("partnerId");
			partnerId = paramNode.item(0).getFirstChild().getNodeValue().trim();

			NodeList secNode = doc.getElementsByTagName("secId");
			secId = secNode.item(0).getFirstChild().getNodeValue().trim();

			NodeList signAlgoNode = doc.getElementsByTagName("signAlgo");
			this.signAlgo = signAlgoNode.item(0).getFirstChild().getNodeValue()
					.trim();

			NodeList prikeyNode = doc.getElementsByTagName("prikey");
			this.prikey = prikeyNode.item(0).getFirstChild().getNodeValue()
					.trim();

			NodeList pubkeyNode = doc.getElementsByTagName("pubkey");
			this.pubkey = pubkeyNode.item(0).getFirstChild().getNodeValue()
					.trim();

			NodeList alipayPubKeyNode = doc
					.getElementsByTagName("alipayPubKey");
			this.alipayPubKey = alipayPubKeyNode.item(0).getFirstChild()
					.getNodeValue().trim();
			
			NodeList acountNode = doc.getElementsByTagName("acount");
			this.acount = acountNode.item(0).getFirstChild().getNodeValue().trim();
			
			NodeList formatNode = doc.getElementsByTagName("format");
			format = formatNode.item(0).getFirstChild().getNodeValue().trim();
			
			NodeList versionNode = doc.getElementsByTagName("version");
			version = versionNode.item(0).getFirstChild().getNodeValue().trim();
			
			NodeList serviceNode = doc.getElementsByTagName("service");
			service = serviceNode.item(0).getFirstChild().getNodeValue().trim();
			
			NodeList serverUrlNode = doc.getElementsByTagName("serverUrl");
			serverUrl = serverUrlNode.item(0).getFirstChild().getNodeValue().trim();

		} catch (Exception e) {
			//异常处理
			//此处为演示代码 直接输出错误信息
			logger.error("ReadConfig:"+e);
		}
	}
	public String getService(){
		return service;
	}
	
	public void setService(String service){
		this.service = service;
	}
	public String getFormat(){
		return format;
	}
	
	public String getVersion(){
		return version;
	}
	
	public void setFormat(String format){
		this.format = format;
	}
	
	public void setVersion(String version){
		this.version = version;
	}
	
	public String getAcount(){
		return acount;
	}
	
	public void setAcount(String acount){
		this.acount = acount;
	}

	/**
	 * @return Returns the serverUrl.
	 */
	public String getServerUrl() {
		return serverUrl;
	}

	/**
	 * @param serverUrl The serverUrl to set.
	 */
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	/**
	 * @return Returns the serverPort.
	 */
	public String getServerPort() {
		return serverPort;
	}

	/**
	 * @param serverPort The serverPort to set.
	 */
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * @return Returns the partnerId.
	 */
	public String getPartnerId() {
		return partnerId;
	}

	/**
	 * @param partnerId The partnerId to set.
	 */
	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	/**
	 * @return Returns the secId.
	 */
	public String getSecId() {
		return secId;
	}

	/**
	 * @param secId The secId to set.
	 */
	public void setSecId(String secId) {
		this.secId = secId;
	}

	/**
	 * @return Returns the prikey.
	 */
	public String getPrikey() {
		return prikey;
	}

	/**
	 * @param prikey The prikey to set.
	 */
	public void setPrikey(String prikey) {
		this.prikey = prikey;
	}

	/**
	 * @return Returns the pubkey.
	 */
	public String getPubkey() {
		return pubkey;
	}

	/**
	 * @param pubkey The pubkey to set.
	 */
	public void setPubkey(String pubkey) {
		this.pubkey = pubkey;
	}

	/**
	 * @return Returns the alipayPubKey.
	 */
	public String getAlipayPubKey() {
		return alipayPubKey;
	}

	/**
	 * @param alipayPubKey The alipayPubKey to set.
	 */
	public void setAlipayPubKey(String alipayPubKey) {
		this.alipayPubKey = alipayPubKey;
	}

	/**
	 * @return Returns the signAlgo.
	 */
	public String getSignAlgo() {
		return signAlgo;
	}

	/**
	 * @param signAlgo The signAlgo to set.
	 */
	public void setSignAlgo(String signAlgo) {
		this.signAlgo = signAlgo;
	}

	/**
	 * @return Returns the encryptAlgo.
	 */
	public String getEncryptAlgo() {
		return encryptAlgo;
	}

	/**
	 * @param encryptAlgo The encryptAlgo to set.
	 */
	public void setEncryptAlgo(String encryptAlgo) {
		this.encryptAlgo = encryptAlgo;
	}

}
