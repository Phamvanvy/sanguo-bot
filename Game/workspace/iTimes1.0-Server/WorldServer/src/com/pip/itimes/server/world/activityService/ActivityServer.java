package com.pip.itimes.server.world.activityService;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.IrechargeService;
import com.pip.itimes.server.world.MailService;
import com.pip.itimes.server.world.PlayerService;

public class ActivityServer implements Runnable {
	private static final Logger log = Logger.getLogger(ActivityServer.class);
	
	public static ActivityServer server;
	private MailService mailService;
	private ActivityService activityService;
	private IrechargeService irechargeService;
	private ConnectService connectService;
	private PlayerService playerService;
	EventManager eventManager;
	
	public static final SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm");
	
	public ActivityServer (ActivityService activityService) {
		this.activityService = activityService;
		eventManager = new EventManager();
		server = this;
		new Thread(this).start();
	}
	
	public void setMailService (MailService mailService) {
		this.mailService = mailService;
	}
	
	public MailService getMailService () {
		return mailService;
	}
	
	public void setIrechargeService (IrechargeService irechargeService) {
		this.irechargeService = irechargeService;
	}
	
	public IrechargeService getIrechargeService () {
		return irechargeService;
	}
	
	public void setConnectService (ConnectService connectService) {
		this.connectService = connectService;
	}
	
	public ConnectService getConnectService () {
		return connectService;
	}
	
	public void setPlayerService (PlayerService playerService) {
		this.playerService = playerService;
	}
	
	public PlayerService getplayerService () {
		return playerService;
	}
	
	public void sendMessage(int playerId, String message) {
        UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
        seg.writeString(message);
        connectService.writeTo(seg, playerId);
    }
	
	public void run () {
		while (true) {
			try {
				long now = System.currentTimeMillis();
				activityService.process(now);
				eventManager.dispatchEvents();
			} catch (Throwable e1) {
				log.error(e1,e1);
			} finally {
                try{
                	Thread.sleep(1000L);
                }catch(Exception e){
                }
            }
		}
	}
	
	public EventManager getEventManager() {
		return eventManager;
	}
}
