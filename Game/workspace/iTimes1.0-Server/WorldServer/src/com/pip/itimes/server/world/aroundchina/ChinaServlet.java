package com.pip.itimes.server.world.aroundchina;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.pip.itimes.server.bean.Account;
import com.pip.itimes.server.bean.Irecharge;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.util.KeywordsUtil;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.IrechargeService;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.activityService.ActivityEvent;
import com.pip.itimes.server.world.activityService.ActivityServer;

import org.apache.log4j.Logger;

public class ChinaServlet extends HttpServlet {

    private Logger log = Logger.getLogger(ChinaServlet.class);

    private ConnectService connectservice;
    private PlayerService playerService;
    private IrechargeService irechargeService;

    public ChinaServlet(ConnectService connectservice, PlayerService playerService, IrechargeService irechargeService) {
        this.connectservice = connectservice;
        this.playerService = playerService;
        this.irechargeService = irechargeService;
    }

    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
    	String accountid = request.getParameter("accountid");
        String successinfo = request.getParameter("success");
        accountid = accountid.trim();
        successinfo = successinfo.trim();
        int tmpint = Integer.valueOf(accountid).intValue();
        WorldPlayer user = playerService.getWorldPlaqerByAccountId(tmpint);
        response.setCharacterEncoding("GBK");
        response.setStatus(HttpServletResponse.SC_OK);
        if (user == null) {
            sendError(response, "对应用户不存在");
            String amount = request.getParameter("amount");
            String channel = request.getParameter("channel");
            log.info("UserID[usernull]" + " accountid[" + tmpint + "] successinfo[" + successinfo + "] amount:" + amount + " channel:" + channel);
            return;
        }else{
        	//发送msg
        	if ("true".equals(successinfo)){
        		//充值成功
        		connectservice.sendMessage(user.getId(),"您已充值成功，请查收您的i币。");
        		log.info("UserID["+Long.valueOf(user.getId()).toString()+"]发送"+successinfo);
        	}else if("false".equals(successinfo)){
        		//充值失败
        		connectservice.sendMessage(user.getId(),"您充值不成功，请核对序列号和密码重新充值。");
        		log.info("UserID["+Long.valueOf(user.getId()).toString()+"]发送"+successinfo);
        		return;
        	}
        }
        
        //为统计平台提供数据
        try{
            String amount = request.getParameter("amount");
            String channel = request.getParameter("channel");
            if(amount != null){
            	// I币 * 100
                int iamount = Integer.parseInt(amount) * 100;
                
                //统计
                Server.realtimeStatService.chargeCounter += iamount;
                Server.realtimeStatService.reportChargeDetail(accountid, successinfo, iamount, channel);
                
                // 保存充值记录
    			Irecharge irecharge = new Irecharge();
    			irecharge.setAccountid(user.getAccountId());
    			Date now = new Date();
    			irecharge.setChargetime(now);
    			irecharge.setMoney(iamount / 36000);
    			irecharge.setPlayerlevel(user.getLevel());
    			irecharge.setPlayerid(user.getId());
    			irechargeService.addIrecharge(irecharge);
    			ActivityEvent event = new ActivityEvent(ActivityEvent.EVENT_RECHARGE, user.getId(), iamount / 36000, now);
                ActivityServer.server.getEventManager().addEvent(event);
                log.info("UserID["+Long.valueOf(user.getId()).toString()+"]amount："+amount+"  channel："+channel);
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
