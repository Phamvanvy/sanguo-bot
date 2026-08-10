package peony.service.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pip.bubble.PromptBubble;

import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.service.ServiceEvent;
import peony.service.account.RecordChargeCall;

public class ChinarunServlet extends HttpServlet{

    private Logger log = Logger.getLogger(ChinarunServlet.class);

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        String accountid = request.getParameter("accountid");
        String successinfo = request.getParameter("success");
        accountid = accountid.trim();
        successinfo = successinfo.trim();
        log.info("[CHINARUNCALLBACK]ACCOUNTID[" + accountid + "]SUCCESSINFO[" + successinfo + "]");
        int tmpint = Integer.valueOf(accountid).intValue();
        ClientSession session = Server.server.getServiceRegistry().getAccountService().getClientSession(tmpint);
        response.setCharacterEncoding("GBK");
        response.setStatus(HttpServletResponse.SC_OK);
        if(session == null){
            sendError(response, "对应用户不存在");
            return;
        }else{
            Player p = (Player) session.getClient();
            if(p != null){
                if("true".equals(successinfo)){
                    // 充值成功
                    p.message(-1, "您已充值成功。", -1, -1);
                }else if("false".equals(successinfo)){
                    // 充值失败
                    p.message(-1, "您充值不成功，请核对序列号和密码重新充值。", -1, -1);
                }
            }
        }

        //为统计平台提供数据
        try{
            String amount = request.getParameter("amount");
            String channel = request.getParameter("channel");
            
            if(amount != null){
                int iamount = Integer.parseInt(amount) * 100;
                
                //统计
                Server.server.getServiceRegistry().getRealtimeStatService().chargeCounter += iamount;
                Server.server.getServiceRegistry().getRealtimeStatService().reportChargeDetail(accountid, successinfo, iamount, channel);
                
                //记录充值成功
                if("true".equals(successinfo)){
	                RecordChargeCall call = new RecordChargeCall(null, Integer.parseInt(accountid), iamount/36000);
	                Server.server.getServiceRegistry().getDbService().schedule(call);
	                Player p = (Player) session.getClient();
	                if(p != null){
	                	Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_CHARGE_SUCCESS, p, iamount/36000));
	                }
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
