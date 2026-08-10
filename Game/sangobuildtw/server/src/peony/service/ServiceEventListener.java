package peony.service;

/**
 * 服务事件监听者。一个监听者可以监听多个类型的事件。
 * @author lighthu
 */
public interface ServiceEventListener {
	/**
	 * 返回此监听者关心的事件类型数组。
	 */
	public int[] getEventTypes();
	
	/**
	 * 处理服务事件。
	 * @param event
	 */
	public void handleEvent(ServiceEvent event);
}
