package com.pip.itimes.server.world.activityService;

/**
 * 服务事件监听者。一个监听者可以监听多个类型的事件。
 * @author hchen
 */
public interface ActivityEventListener {
	/**
	 * 返回此监听者关心的事件类型数组。
	 */
	public int[] getEventTypes();
	
	/**
	 * 处理服务事件。
	 * @param event
	 */
	public void handleEvent(ActivityEvent event);
}

