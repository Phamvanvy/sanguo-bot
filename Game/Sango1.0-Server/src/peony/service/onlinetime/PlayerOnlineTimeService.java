package peony.service.onlinetime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.award.AwardService;

/**
 * 在线时间管理
 * @author pmeng
 */
public class PlayerOnlineTimeService implements Service,ServiceEventListener{
	
	public int day;//服务器模拟天数
	
	public static final int initHour = 3;//初始化时间
	
	public static final int initMinute = 0;
	
	public Long lastCheckTime = 0L;
	
	public static int MINUTE = 60 * 1000 * 10;//十分钟遍历一次
	
	public List<Integer> players = new ArrayList<Integer>();//在线一小时的玩家表
	
	public static final String PROPERTY_ONLLINE_TIME = "onlinetimes";//记录在线时长
	
	public static int HOUR = 60 * 60 * 1000;//一小时
	
	
	public void shutdown() {
		
	}
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		this.day = getDay(Calendar.getInstance());
	}
	
	/**
	 * 获取指定时间的服务器模拟天数
	 */
	public int getDay(Calendar now){
		int day = now.get(Calendar.DAY_OF_YEAR);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int min = now.get(Calendar.MINUTE);
		if(hour >= 0 && initHour > hour){
			if(hour == initHour){
				if(min < initMinute){
					 return day - 1; 
				}
			}else{
				return day - 1;
			}
		}
		return day;
	}
	
	public void update(){
		if((System.currentTimeMillis() - lastCheckTime) >= MINUTE){
			lastCheckTime = System.currentTimeMillis();
			//检测换天
			if(getDay(Calendar.getInstance()) == this.day + 1){
				this.day = getDay(Calendar.getInstance());
				Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_CHANGEDAY_THREE));
				initPlayerOnlineTime();
				players.clear();
			}
		}
	}
	
	/**
	 * 换天时初始化在线玩家的在线时间
	 */
	 public void initPlayerOnlineTime(){
		//统计在线时间
			for(Player p:ObjectAccessor.players.values()){
				if(p==null)
					continue;
				initPlayer(p);
				Packet pt = new Packet(OpCode.GET_AWARDITEM_SERVER);
				pt.putInt(-1);
				pt.putInt(0);
				p.send(pt);
			}
	 }
	
	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_LOADED
		};
	}
	
	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_PLAYER_LOADED:
				processPlayerLoaded((Player)event.param1);
				break;
		}
	}
	
	public void processPlayerLoaded(Player p){
		Calendar lastLogout = Calendar.getInstance();
		lastLogout.setTime(p.lastLogoutTime);
		int day = getDay(lastLogout);
		if(day != this.day){
			initPlayer(p);
		}
	}
	
	//初始化玩家在线时间
	public void initPlayer(Player p){
		p.pool.setLong(PROPERTY_ONLLINE_TIME, 0L);
		p.pool.remove(AwardService.PROPERTY_GETAWARD_NUM);
	}
	
}
