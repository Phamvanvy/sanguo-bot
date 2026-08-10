package peony.service.jetty;

import java.io.IOException;
import java.text.MessageFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import peony.game.Server;
import peony.game.chat.ChatService;

/**
 * 充值奖励回调Servlet
 * @author dchen
 */
public class ChargeRewardServlet extends HttpServlet{
	
    private Logger log = Logger.getLogger(ChargeRewardServlet.class);
    
    public static String GAMECODEPREFIX = "sanguo";
    
    public static int NOTIFY_OWN = 1;
    
    public static int NOTIFY_ALL = 2;
    
    public static String replaceStr = "***";
    
    public static int notifyType = NOTIFY_ALL;
    
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        request.setCharacterEncoding("UTF-8");
    	String partition = request.getParameter("partition"); //分区
        String accountId = request.getParameter("account"); //账号ID
        String accountName = request.getParameter("accountname"); //账号名称
        String reward = request.getParameter("reward"); //奖励金额（元宝）
        String cause = request.getParameter("cause"); //原因
        String playerName = request.getParameter("role"); //角色名称
//        if(partition!=null && partition.contains(GAMECODEPREFIX)){
        	String displayName = playerName;
        	if(displayName!=null && !displayName.equals("")){
        		ChatService chatService = Server.server.getServiceRegistry().getChatService();
            	if(notifyType==NOTIFY_ALL){ //通知所有服务器
            		chatService.sendWorldMessage(MessageFormat.format("恭喜{0}在充值时人品爆发,获得双倍元宝,充值就有几率获得双倍元宝活动火热进行中。", displayName));
            	}else if(notifyType==NOTIFY_OWN){ //通知本服务器
//            		String gameCode = Server.server.gameCode;
//            		if(partition.trim().equalsIgnoreCase(gameCode)){
            			chatService.sendWorldMessage(MessageFormat.format("恭喜{0}在充值时人品爆发,获得双倍元宝,充值就有几率获得双倍元宝活动火热进行中。", displayName));
//            		}
            	}
        	}
//        }
        log.info("[CHARGEDOUBLEREWARD]ACCOUNTID[" + accountId + "]ACCOUNTNAME["+accountName
        		+"]PLAYERNAME["+(playerName==null ? "" : playerName)+"]REWARD["+reward+"]PARTITION["+partition+"]CAUSE["+cause+"]");
    }
    
    protected String getDisplayName(String accountName){
    	StringBuilder strb = new StringBuilder();
    	if(accountName.length()==2 || accountName.length()==3 || accountName.length()==4){
    		return strb.append(accountName.substring(0, 1)).append(replaceStr).toString();
    	}else if(accountName.length()>4){
    		return strb.append(accountName.substring(0, 1)).append(replaceStr).append(accountName.substring(4)).toString();
    	}
    	return null;
    }
    
}
