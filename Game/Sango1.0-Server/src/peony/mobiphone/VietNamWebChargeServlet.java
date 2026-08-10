package peony.mobiphone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import peony.game.Server;
import peony.gatecard.GateCardService;
import peony.net.ClientSession;
import peony.service.account.AccountAsyncCall;

/**
 * 越南web充值servlet
 * @author dchen
 */
@SuppressWarnings("serial")
public class VietNamWebChargeServlet extends HttpServlet {
	
	public static String GETACCID_BYACCNAME_URL = "http://210.211.99.54:8102/CheckAccountName"; //暂时写死
	
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		synchronized (this) {
			String accountName = request.getParameter("accountname");
			String cardNum = request.getParameter("cardid");
			String password = request.getParameter("password");
			String cardType = request.getParameter("cardtype");
			int type = Integer.parseInt(cardType);
			response.setCharacterEncoding("GBK");
	        response.setStatus(HttpServletResponse.SC_OK);
	        VietNamWebChargeCall call  = new VietNamWebChargeCall(null);
	        int ammount = 0;
			if(cardType.equals("0") || cardType.equals("1")){
				//telco_mobi、telco_vina充值
				TelcoChargeService service = Server.server.getServiceRegistry().getTelcoChargeService();
				try {
					int accountId = getAccountIdByAccountName(accountName);
					ammount = service.webCharge(accountName, accountId, type, cardNum, call);
				} catch (Exception e) {
					response.getWriter().println(e.getMessage());
					return;
				}
			}else if(cardType.equals("2")){
				//gateCard充值
				GateCardService service = Server.server.getServiceRegistry().getGateCardService();
				try {
					int accountId = getAccountIdByAccountName(accountName);
					ammount = service.vietNamWebcharge(accountName, accountId, cardNum, password, call);
				} catch (Exception e) {
					response.getWriter().println(e.getMessage());
					return;
				}
			}else{
				response.getWriter().println("0");
				response.getWriter().println(peony.Messages.STRING_00714);
				return;
			}
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(call.success){
				if(ammount>0)
					response.getWriter().println(MessageFormat.format(peony.Messages.STRING_00715, ammount));
				else
					response.getWriter().println(peony.Messages.STRING_00716);
			}else{
				response.getWriter().println(peony.Messages.STRING_00620);
			}
		}
	}
	
	/** 根据账号名取得账号ID */
	protected int getAccountIdByAccountName(String accountName) throws Exception {
		PostMethod method;
		method = new PostMethod(GETACCID_BYACCNAME_URL);
		method.getParams().setContentCharset("utf-8");
		method.addRequestHeader("Connection", "close");
		method.setParameter("name", accountName);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			int code = httpclient.executeMethod(method);
			if(code==200){
				BufferedReader reader = new BufferedReader(new InputStreamReader(method
						.getResponseBodyAsStream(), "UTF-8"));
				String line = "";
				int lineNum = 1;
				while((line=reader.readLine())!=null){
					if(lineNum==1){
						if(line.contains("1")){
							throw new Exception(peony.Messages.STRING_00717);
						}
					}else if(lineNum==2){
						return Integer.parseInt(line);
					}
					lineNum++;
				}
			}else{
				
			}
		} catch (Exception ex) {
			throw new Exception(peony.Messages.STRING_00717);
		} finally {
			method.releaseConnection();
		}
		throw new Exception(peony.Messages.STRING_00717);
	}
	
	class VietNamWebChargeCall extends AccountAsyncCall{
		
		public VietNamWebChargeCall(ClientSession session) {
			super(session);
		}

		public void callFinish() throws Exception {
			synchronized (VietNamWebChargeServlet.this){
				VietNamWebChargeServlet.this.notify();
			}
		}

		public void run() {
			
		}
	}

}
