package com.pip.itimes.server.world.activityService;

/**
 * 服务之间的事件。所有事件被放到一个公共的事件队列中，并由服务器主循环分派给关心此事件的服务。
 * @author hchen
 */
public class ActivityEvent {
	/**
	 * 玩家i币消费成功。消费金额RMB:元
	 */
	public static final int EVENT_CONSUMER = 1001;
	/**
	 * 玩家i币充值成功。消费金额RMB:元
	 */
	public static final int EVENT_RECHARGE = 1002;
	/**
	 * 名人堂配置成功。参数：设置玩家个数count，获得物品giftId，获得物品个数giftCount;
	 */
	public static final int EVENT_HALL_FAME = 1003;
	/**
	 * I币卖场消费成功。参数：设置消费获得荣誉倍数
	 *//*
	public static final int EVENT_ISHOP = 1004;
	*//**
	 * 澡堂配置成功。参数：设置每日澡堂开启次数count，开启时间startTime，关闭时间endTime，经验荣誉倍数
	 *//*
	public static final int EVENT_BATH = 1005;*/
	
	public int type;
	public Object param1;
	public Object param2;
	public Object param3;
	
	public ActivityEvent(int type) {
		this.type = type;
	}
	
	public ActivityEvent(int type, Object param1) {
		this.type = type;
		this.param1 = param1;
	}

	public ActivityEvent(int type, Object param1, Object param2) {
		this.type = type;
		this.param1 = param1;
		this.param2 = param2;
	}
	
	public ActivityEvent(int type, Object param1, Object param2, Object param3) {
		this.type = type;
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
	}
}