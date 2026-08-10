package peony.service.jetty;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import peony.db.PlayerDAO;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.service.VIP.VipPrivilegeService;
import peony.service.account.AccountProperty;

/**
 * 角色查询接口
 * @author dchen
 */
public class ActorQueryServlet extends HttpServlet{
	
    private Logger log = Logger.getLogger(ActorQueryServlet.class);
    
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    	String account = request.getParameter("account").trim();
        int accountId = Integer.parseInt(account);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        Player onlinePlayer = null;
        for(Player p : ObjectAccessor.players.values()){
        	if(p!=null && p.accountId==accountId){
        		onlinePlayer = p;
        		break;
        	}
        }
    	PlayerDAO dao = Server.server.getServiceRegistry().getDbService().playerDAO;
        List<Player> otherActors = dao.getDescActors(accountId, onlinePlayer==null?0:onlinePlayer.id);
        int accountVipLevel = -1;
        if(onlinePlayer!=null){
        	accountVipLevel = onlinePlayer.vipLevel;
        }else{
        	accountVipLevel = getVipLevel(accountId);
        }
        if(onlinePlayer!=null){
        	response.getWriter().println(onlinePlayer.name);
        	response.getWriter().println(onlinePlayer.id);
        	response.getWriter().println(accountVipLevel);
        }
        for(Player actor : otherActors){
        	response.getWriter().println(actor.name);
            response.getWriter().println(actor.id);
            response.getWriter().println(accountVipLevel);
        }
        if(onlinePlayer==null && (otherActors==null || otherActors.size()==0))
        	response.getWriter().println("");
    }
    
    protected int getVipLevel(int accountId){
    	VipPrivilegeService vipService = Server.server.getServiceRegistry().getVipPrivilegeService();
    	AccountProperty ap = vipService.getAccountProperty(accountId);
    	if(ap==null){
    		synchronized (Server.server.getServiceRegistry().getDbService().accountPropertyDao) {
				ap = vipService.getAccountPropertyFromDB(accountId);
			}
    	}
    	if(ap!=null)
    		return ap.pool.getInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL,0);
    	return -1;
    }
    
}
