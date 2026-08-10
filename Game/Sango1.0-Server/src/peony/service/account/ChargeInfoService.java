package peony.service.account;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import peony.game.Server;
import peony.service.Service;

public class ChargeInfoService implements Service,Runnable{

	private static final Logger log = Logger.getLogger(ChargeInfoService.class);
	
	private String info = "";
	
	public void shutdown() {
		
	}

	public void startup() throws Exception {
		new Thread(this, "ChargeInfoService").start();
	}
	
	public String getInfo(){
		return info;
	}
	
	private void query(){
		synchronized(this){
			GetMethod method = new GetMethod(Server.server.billingURL + "yeepay_charge_rate");
			method.addRequestHeader( "Connection", "close");
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
			try {
	            HttpClient httpclient = new HttpClient();
	            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
	            httpclient.getParams().setSoTimeout(30000);
	            int code = httpclient.executeMethod(method);
	            if (code == 200) {
	                InputStream result = method.getResponseBodyAsStream();
	                ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
	                byte[] str_b = new byte[1024];
	                int i = -1;
	                while ((i=result.read(str_b)) > 0) {
	                   outputstream.write(str_b,0,i);
	                }
	                byte[] bytes = outputstream.toByteArray();
	                info = new String(bytes,"UTF-8").trim();
	                outputstream.close();
	            } else {
	                throw new Exception(peony.Messages.STRING_01866);
	            }
			} catch (Exception ex) {
				log.error(ex,ex);
	        } finally {
	            method.releaseConnection();
	        }
		}
	}

	public void run() {
		while(true){
			try{
			    query();
			} catch (Exception ex){
				ex.printStackTrace();
			}
			try {
				Thread.sleep(3*60*1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		ChargeInfoService cis = new ChargeInfoService();
		cis.query();
		System.out.println(cis.getInfo());
	}
}
