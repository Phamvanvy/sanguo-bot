package peony.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

/**
 * 服务事件管理器，负责监听者的管理和服务事件的分发。
 * @author lighthu
 */
public class EventManager {
	// 日志
	protected static Logger log = Logger.getLogger(ServiceRegistry.class);
	
	/*
	 * 监听者队列。
	 */
	protected Map<Integer, List<ServiceEventListener>> listeners = new HashMap<Integer, List<ServiceEventListener>>();
	/*
	 * 事件队列。
	 */
	protected Queue<ServiceEvent> events = new ConcurrentLinkedQueue<ServiceEvent>();
	
	/**
	 * 注册一个事件监听者。
	 * @param listener
	 */
	public void registerListener(ServiceEventListener listener) {
		int[] types = listener.getEventTypes();
		for (int type : types) {
			List<ServiceEventListener> lls = listeners.get(type);
			if (lls == null) {
				lls = new ArrayList<ServiceEventListener>();
				listeners.put(type, lls);
			}
			lls.add(listener);
		}
	}
	
	/**
	 * 取消注册一个事件监听者。
	 * @param listener
	 */
	public void unregisterListener(ServiceEventListener listener) {
		for (List<ServiceEventListener> lls : listeners.values()) {
			lls.remove(listener);
		}
	}
	
	/**
	 * 发布一个新事件。
	 * @param event
	 */
	public void addEvent(ServiceEvent event) {
		events.add(event);
	}
	
	/**
	 * 处理当前事件队列中的所有事件。这个方法应该每个cycle调用一次。
	 */
	public void dispatchEvents() {
		while (!events.isEmpty()) {
			ServiceEvent evt = events.remove();
			List<ServiceEventListener> lls = listeners.get(evt.type);
			if (lls != null) {
				for (ServiceEventListener l : lls) {
					try {
						l.handleEvent(evt);
					} catch (Exception e) {
						log.error(e, e);
					}
				}
			}
		}
	}
	
	/**
	 * 发布一个新事件并立刻处理。
	 * @param event
	 */
	public void fireEvent(ServiceEvent event) {
		List<ServiceEventListener> lls = listeners.get(event.type);
		if (lls != null) {
			for (ServiceEventListener l : lls) {
				try {
					l.handleEvent(event);
				} catch (Exception e) {
					log.error(e, e);
				}
			}
		}
	}
}
