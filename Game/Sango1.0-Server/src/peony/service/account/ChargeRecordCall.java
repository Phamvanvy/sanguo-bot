package peony.service.account;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class ChargeRecordCall extends ClientSessionAsyncCall {
	
	private static final Logger log = Logger.getLogger(ChargeRecordCall.class);

	protected int serial;
	
	protected Player player;
	
	protected List<List<String>> result = new ArrayList<List<String>>(); // ²éÑ¯½á¹û
	
	public ChargeRecordCall(ClientSession session, Packet packet) {
		super(session);
		this.player = (Player)session.getClient();
		this.serial = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			if(result.size()==0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CHARGE_RECORD_CLIENT, peony.Messages.STRING_01511);
			}else{
				Packet pt = new Packet(OpCode.CHARGE_RECORD_SERVER);
				pt.putInt(serial);
				pt.putInt(result.size());
				for(List<String> chs : result){
					for(String ch : chs){
						pt.putString(ch);
					}
				}
				session.send(pt);
			}
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.CHARGE_RECORD_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player!=null){
			PostMethod method;
			method = new PostMethod(Server.server.billingURL + "query_charge");
			method.getParams().setContentCharset("utf-8");
			method.addRequestHeader("Connection", "close");
			method.setParameter("accountid", Integer.toString(player.accountId));
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
					while((line=reader.readLine())!=null){
						String[] strs = line.split("	");
						String card = strs[0];
						String password = strs[1];
						String orderTime = strs[2];
						String money = strs[3];
						String state = strs[4];
						List<String> content = new ArrayList<String>();
						content.add(card);
						content.add(password);
						content.add(orderTime);
						content.add(money);
						content.add(state);
						result.add(content);
					}
				}else{
					error(peony.Messages.STRING_01512);
				}
			} catch (Exception ex) {
				log.error(ex, ex);
			} finally {
				method.releaseConnection();
			}
		}
		addToClientSession();
	}

}
