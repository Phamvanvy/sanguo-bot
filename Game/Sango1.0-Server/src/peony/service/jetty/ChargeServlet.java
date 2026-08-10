package peony.service.jetty;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.service.ServiceEvent;
import peony.service.account.Account;
import peony.service.account.ChargeRegularCall;
import peony.service.account.RecordChargeCall;
import peony.service.account.adapter.AppStoreService;
import peony.service.account.adapter.QmeAccount;
import sun.misc.BASE64Decoder;

/**
 * 新的充值回调接口。所有的充值都将会回调到这个接口，原来的接口只是给游戏内充值使用的。这个接口的url应该是charge_notify。请在配置文件中配置
 * @author Jeffrey
 *
 */
public class ChargeServlet extends HttpServlet{
	
    private Logger log = Logger.getLogger(ChinarunServlet.class);
    private static BASE64Decoder base64Decoder = new BASE64Decoder();

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

    	String feeid = "1000000071504680";
    	if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW))
    		try{feeid = request.getParameter("feeid");}catch(Exception e){}
        String accountid = request.getParameter("account");
        String successinfo = request.getParameter("succ");
        String channel = request.getParameter("channel");
        accountid = accountid.trim();
        successinfo = successinfo.trim();
        log.info("[CHARGECALLBACK]ACCOUNTID[" + accountid + "]SUCCESSINFO[" + successinfo + "]CHANNEL[" + channel+ "]");
        int tmpint = Integer.valueOf(accountid).intValue();
        ClientSession session = Server.server.getServiceRegistry().getAccountService().getClientSession(tmpint);
        response.setCharacterEncoding("GBK");
        response.setStatus(HttpServletResponse.SC_OK);
        boolean success = "1".equals(successinfo);
        if(session == null){
            sendError(response, peony.Messages.STRING_01818);
        }else{
            Player p = (Player) session.getClient();
            if(p != null){
                if(success){
                    // 充值成功
                    p.message(-1, peony.Messages.STRING_01819, -1, -1);
                }else {
                    // 充值失败
                    p.message(-1, peony.Messages.STRING_01820, -1, -1);
                }
            }
        }

        //为统计平台提供数据
        try{
            int amount = 0;
        	String amountString = request.getParameter("imoney");
        	amount = Integer.valueOf(amountString);
        	int iamount = amount * 100;
            Server.server.getServiceRegistry().getRealtimeStatService().chargeCounter += iamount;
            Server.server.getServiceRegistry().getRealtimeStatService().reportChargeDetail(accountid, successinfo, iamount, channel);
            if(success) {
				// 记录充值成功
            	int money = iamount / 36000;
            	if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
            		money = iamount / 3600;
            	}
            	try {
					Player p = (Player) session.getClient();
					if (p != null) {
						Server.server.getEventManager().fireEvent(
								new ServiceEvent(ServiceEvent.EVENT_CHARGE_SUCCESS, p, money));
					}
				} catch (Exception e) {
				}
				RecordChargeCall call = new RecordChargeCall(null, Integer
						.parseInt(accountid), money);
				Server.server.getServiceRegistry().getDbService().schedule(call);
				ChargeRegularCall call2 = new ChargeRegularCall(session,Integer
						.parseInt(accountid), money);
				Server.server.getServiceRegistry().getDbService().schedule(call2);
				if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW)){
					storeValue(feeid, accountid);
				}
            }
        }catch(Exception e){
            log.error(e, e);
        }
        if(channel.equals("ITUNES")&&successinfo.equals("0")){
        	 Player p = (Player) session.getClient();
             if(p != null){
            	 p.pool.setInt(Player.ISSHOWPIPCHARGEFLAG, 1);//永久显示官网充值
             }
        }
    }
    
    protected void storeValue(String feeid, String acc) {
    	int accountId = Integer.parseInt(acc);
    	Account account = Server.server.getServiceRegistry().getAccountService().getAccount(accountId);
    	if(account!=null){
    		Object mi = account.getMetaInfo();
    		if(mi instanceof QmeAccount){
    			QmeAccount qmeAccount = (QmeAccount)mi;
    			int qmeId = qmeAccount.qmeID;
    			String receipt = account.getReceipt();
    			if(receipt!=null){
    				try {
    					byte[] arr = base64Decoder.decodeBuffer(receipt);
    					receipt = new String(arr, "utf-8");
						JSONObject res = JSONObject.fromString(receipt);
						String purchaseInfo = res.getString("purchase-info");
						byte[] data = base64Decoder.decodeBuffer(purchaseInfo);
						JSONObject res1 = JSONObject.fromString(new String(data, "utf-8"));
						String productId = res1.getString("product-id").trim();
						String paramReceipt = productId.split("_")[1];
						String contentid = AppStoreService.receipt2contentId.get(paramReceipt);
						
						PostMethod method = new PostMethod(AppStoreService.url + feeid + "/");
						method.getParams().setContentCharset("UTF-8");
						method.addRequestHeader("Connection", "close");
						method.setParameter("acc", AppStoreService.acc);
						method.setParameter("pwd", AppStoreService.pwd);
						method.setParameter("qmeid", String.valueOf(qmeId));
	        			method.setParameter("contentid", contentid);
	        			method.setParameter("receipt", paramReceipt);
						method.setParameter("testmode", String.valueOf(0));
						HttpClient httpclient = new HttpClient();
						httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
						httpclient.getParams().setSoTimeout(30000);
						int code = httpclient.executeMethod(method);
						String result = method.getResponseBodyAsString();
//						BufferedReader br = new BufferedReader(new StringReader(result));
//						String line = "";
//						StringBuffer response = new StringBuffer();
//						while((line=br.readLine())!=null){
//							response.append(line);
//							response.append("\r");
//						}
						log.info("[APPSTOREVALUE]FEEID["+feeid+"]QMEID["+qmeId+"]CONTENTID["+contentid+"]RECEIPT["+paramReceipt+"]CODE["+code+"]RESULT["+result+"]");
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						account.setReceipt(null);
					}
    			}
    		}
    	}
    }

    private void sendError(HttpServletResponse response, String error) throws IOException{
        response.getWriter().println("2");
        response.getWriter().print(error);
    }
    
}
