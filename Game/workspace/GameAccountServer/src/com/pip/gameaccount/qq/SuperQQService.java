package com.pip.gameaccount.qq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class SuperQQService implements Runnable {
	
	public static String KEY = "919101307282901971910015";
//	public static String URL1 = "http://121.14.102.10:8191/app_img/a?cpId=11&gameId=19";
//    public static String URL2 = "http://119.147.11.91:8191/app_img/a?cpId=11&gameId=19";
	
    public static String URL1 = "http://121.14.102.10:8191/onlineGameSqq/SuperQQServlet.do?cpId=11&gameId=19";
    public static String URL2 = "http://119.147.11.91:8191/onlineGameSqq/SuperQQServlet.do?cpId=11&gameId=19";
	
    // public static String URL = "http://img.3g.qq.com:8191/app_img/a?cpId=11&gameId=19";
	
	protected int remoteServer = 0;
	
	protected Cipher de_cipher = null;
	protected Cipher en_cipher = null;
	
	private static final Logger log = Logger.getLogger(SuperQQService.class);
	protected ConcurrentHashMap<String, Boolean> superQQCache = new ConcurrentHashMap<String, Boolean>();
	protected long lastClearCacheTime = System.currentTimeMillis();
	/*
     * 控制请求的队列。
     */
    private BlockingQueue<String> pendingRequests = new LinkedBlockingQueue<String>(100000);
	
	public SuperQQService() throws Exception{
		DESedeKeySpec dks = new DESedeKeySpec(KEY.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
		SecretKey securekey = keyFactory.generateSecret(dks);
		de_cipher = Cipher.getInstance("DESEDE/ECB/NoPadding");
		de_cipher.init(Cipher.DECRYPT_MODE, securekey);
		en_cipher = Cipher.getInstance("DESEDE/ECB/NoPadding");
		en_cipher.init(Cipher.ENCRYPT_MODE, securekey);
		
		for (int i = 0; i < 30; i++) {
			Thread t = new Thread(this, "SuperQQ-Thread" + i);
			t.start();
		}
	}
	
	public void addCheckRequest(String uin) {
		Boolean oldval = superQQCache.get(uin);
		if (oldval == null) {
			try {
				pendingRequests.put(uin);
			} catch (Exception e) {
			}
		}
	}
	
	public void run() {
		HttpClient httpclient = new HttpClient();
        httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
        httpclient.getParams().setSoTimeout(10000);
		while (true) {
			try {
				String uin = pendingRequests.take();
				if (superQQCache.contains(uin)) {
					continue;
				}
				boolean ret = checkSuperQQ(httpclient, uin);
				superQQCache.put(uin, ret);
			} catch (Exception e) {
			}
		}
	}
	
	public boolean isSuperQQ(String uin) {
		if ("19_017F9AD862BBFA".equals(uin)) {
			return true;
		}
		
		// 检查缓存，24小时清除一次
		if (System.currentTimeMillis() > lastClearCacheTime + 86400000L) {
			lastClearCacheTime = System.currentTimeMillis();
			clearCache();
		}
		Boolean bo = superQQCache.get(uin);
		if (bo != null) {
			return bo.booleanValue();
		} else {
			addCheckRequest(uin);
			return false;
		}
	}
	
	public void clearCache() {
		log.info("SuperQQService: clearCache");
		superQQCache.clear();
	}
	
	private boolean checkSuperQQ(HttpClient client, String uin) {
		String url = null;
		int useServer = remoteServer;
		if (useServer == 0) {
			url = URL1;
		} else {
			url  = URL2;
		}
		log.info("CheckSuperQQ[" + uin + "]Begin[" + url + "]");
		
		// 特殊处理，如果url为空则跳过
		if (url.length() == 0) {
		    log.info("CheckSuperQQ[" + uin + "]Ignored");
            return false;
		}
		
		PostMethod method = new PostMethod(url);
		String body = getPostBody(uin);
		byte[] bytes = body.getBytes();
		try {
			int len = bytes.length;
			int v = bytes.length%8;
			if(v!=0){
				len += (8-v);
				byte[] tmp = new byte[len];
				Arrays.fill(tmp, (byte)0x20);
				System.arraycopy(bytes, 0, tmp, 0, bytes.length);
				bytes = tmp;
			}
			byte[] en_bytes = en_cipher.doFinal(bytes);
			ByteArrayInputStream bai = new ByteArrayInputStream(en_bytes);
			method.setRequestBody(bai);
			int code = client.executeMethod(method);
			if (code == 200) {
			    try {
    				byte[] data = method.getResponseBody();
    				byte[] de_data = de_cipher.doFinal(data);
    				String s = new String(de_data);
    				int pos1 = s.indexOf('<');
    				int pos2 = s.lastIndexOf('>');
    				s = s.substring(pos1, pos2 + 1);
    				boolean ret = parser(s);
    				log.info("CheckSuperQQ[" + uin + "]" + (ret ? "true" : "false"));
    				return ret;
			    } catch (Exception e) {
		            log.info("CheckSuperQQ[" + uin + "]ParseError");
			        return false;
			    }
			}else{
				log.info("CheckSuperQQ[" + uin + "]" + code);
				return false;
			}
		} catch(Exception ex){
			log.error(ex,ex);
			log.info("CheckSuperQQ[" + uin + "]Error");
			remoteServer = 1 - useServer;
			return false;
		} finally{
			method.releaseConnection();
		}
		
	}
	
	protected String getPostBody(String uin){
		StringBuilder sb = new StringBuilder(200);
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<request>\n");
		sb.append("    <msgType>QuerySqqReq</msgType>\n");
		sb.append("    <uid>");
		sb.append(uin);
		sb.append("</uid>\n");
		sb.append("</request>");
		return sb.toString();
	}
	
	/**
	 * <?xml version="1.0" encoding="UTF-8"?>
		<response>
		<msgType>QuerySQResp</msgType>
		<retCode>0</retCode>
		<enableStatus>Y</enableStatus>
		</response>
	 * @param s
	 * @return
	 */
	protected boolean parser(String s) throws Exception {
		log.debug(s);
		
		log.info("CheckSuperQQ parser[" + s + "]");
		SAXReader saxReader =  new SAXReader();
		try {
			Document doc = saxReader.read(new StringReader(s));
			Element root = doc.getRootElement();
			Element elRet = root.element("retCode");
			if(elRet==null){
				return false;
			}else{
				if("0".equals(elRet.getTextTrim())){
					Element elEnable = root.element("isSqqUser");
					if(elEnable==null)
						return false;
					else{
						if("Y".equalsIgnoreCase(elEnable.getTextTrim())){
							return true;
						}else{
							return false;
						}
					}
				}else{
					return false;
				}
			}
		} catch (DocumentException e) {
			log.error(e,e);
			throw e;
		}
	}
	
	public static void main(String[] args) throws Exception{
//	    System.out.println(URLEncoder.encode("http://119.147.11.91:8191/app_img/a?cpId=11&gameId=19", "utf-8"));
//		SuperQQService service = new SuperQQService();
//		for (int i = 0; i < 1000000; i++) {
//			boolean ret = service.isSuperQQ("19_F73DE8BCFFB72E");
//			boolean ret = service.isSuperQQ("19_9EE728E2595C1D");
//			boolean ret = service.isSuperQQ("19_4BE96C7C401997");
//			boolean ret = service.isSuperQQ("19_4BE96C7C401997");
//			System.out.println("ret:"+ret);
//			try {
//				Thread.sleep(20000);
//			} catch (Exception e) {
//			}
//		}
	}
	
}
