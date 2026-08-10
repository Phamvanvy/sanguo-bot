package peony.patchs;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.service.ServiceEvent;
import peony.service.account.RecordChargeCall;

/**
 * 新的充值回调接口。所有的充值都将会回调到这个接口，原来的接口只是给游戏内充值使用的。这个接口的url应该是charge_notify。请在配置文件中配置
 * @author Jeffrey
 *
 */
public class TempChargeServlet extends HttpServlet{
    private Logger log = Logger.getLogger(TempChargeServlet.class);

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

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
				RecordChargeCall call = new RecordChargeCall(null, Integer
						.parseInt(accountid), iamount / 36000);
				Server.server.getServiceRegistry().getDbService()
						.schedule(call);
				Player p = (Player) session.getClient();
				if (p != null) {
					Server.server.getEventManager().fireEvent(
							new ServiceEvent(ServiceEvent.EVENT_CHARGE_SUCCESS,
									p, iamount / 36000));
	                }
            }
        }catch(Exception e){
            log.error(e, e);
        }
    }

    private void sendError(HttpServletResponse response, String error) throws IOException{
        response.getWriter().println("2");
        response.getWriter().print(error);
    }
}
