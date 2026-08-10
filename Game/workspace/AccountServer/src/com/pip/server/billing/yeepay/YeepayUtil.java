package com.pip.server.billing.yeepay;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

public class YeepayUtil extends Thread {
	private static Logger log = Logger.getLogger(YeepayUtil.class);
	public static class ChargeRequest {
		int id;
		String cardno;
		String cardpass;
		String amount;
		int accountID;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(60000L);
			} catch (Exception e) {
			}
			try {
				List<ChargeRequest> reqs = getChargeRequest();
				log.info("[qq_charge] found " + reqs.size() + " requests");
				
				for (ChargeRequest req : reqs) {
					try {
						String returnURL = "qqhttp://119.147.16.18:8080/itimesipd/ci?cmd=ntf&pwd=reportengine&id=" + req.id + "&cn=" + req.cardno + "&ret=";
						YeepayOrderServlet2.instance.placeOrder(req.accountID, Integer.parseInt(req.amount) * 100, 6, 
								ConstYeepay.CARD_TYPE_SZX, req.cardno, req.cardpass, returnURL, "qq");
					} catch (Exception e1) {
						log.error(e1, e1);
					}
				}
			} catch (Exception e) {
			}
		}
	}
	
	private List<ChargeRequest> getChargeRequest() {
		List<ChargeRequest> ret = new ArrayList<ChargeRequest>();
		String getURL = "http://119.147.16.18:8080/itimesipd/ci?cmd=lst&pwd=reportengine";
        GetMethod method = new GetMethod(getURL);
        method.addRequestHeader( "Connection", "close");
        try {
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
            if (code == 200) {
                String result = method.getResponseBodyAsString();
                BufferedReader br = new BufferedReader(new StringReader(result));
                String line;
                while ((line = br.readLine()) != null) {
                	String[] secs = line.split(" ");
                	if (secs.length >= 5) {
                		ChargeRequest newReq = new ChargeRequest();
                		newReq.id = Integer.parseInt(secs[0]);
                		newReq.cardno = secs[1];
                		newReq.cardpass = secs[2];
                		newReq.amount = secs[3];
                		newReq.accountID = Integer.parseInt(secs[4]);
                		ret.add(newReq);
                	}
                }
            }
        } catch (Exception ex1) {
            log.error(ex1, ex1);
        } finally {
            method.releaseConnection();
        }
        return ret;
	}
}
