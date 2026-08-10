package peony.channel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Player;
import peony.net.ClientSession;

/**
 * 力美下载注册客户端激活消息发送
 * @author dchen
 */
public class LiMeiActivationCall extends ClientSessionAsyncCall {

	private static final Logger log = Logger.getLogger(LiMeiActivationCall.class);
	
	public static String url = "http://211.151.99.71:8102/limei_user_confirm";
	public static String app = "mingzhusanguo";
	
	protected int serial;
	protected String udid;
	protected Player player;
	
	public LiMeiActivationCall(ClientSession session, int serial, String udid) {
		super(session);
		this.player = (Player)session.getClient();
		this.udid = udid;
		this.serial = serial;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		PostMethod method;
		method = new PostMethod(url);
		method.addRequestHeader("Connection", "close");
		method.setParameter("app", app);
		method.setParameter("udid", udid);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
			httpclient.getParams().setContentCharset("UTF-8");
			httpclient.getParams().setSoTimeout(30000);
			int code = httpclient.executeMethod(method);
			InputStream input = method.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String statCode = br.readLine();
			if(statCode.trim().equals("0")){
				//激活成功
				log.info("[LIMEISUCCESS]SERIAL[" + serial + "]CODE[" + code + "]CALLBACK["+url+"]");
			}else{
				//激活失败
				log.info("[LIMEI]SERIAL[" + serial + "]CODE[" + code + "]STATCODE["+statCode.trim()+"]CALLBACK["+url+"]");
			}
		}catch(Exception e){
			
		}
	}

}
