package peony.service.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import peony.service.Service;

/**
 * 活动管理服务。
 * @author lighthu
 */
public class ActivityService implements Service {
	private Logger log = Logger.getLogger(ActivityService.class); 
	private List<Activity> activities;
	private long lastCheckTime;
	private ActivityDAO dao = new ActivityDAO();
	
	public void startup() throws Exception {
		activities = dao.getActivities();
		for (Activity act : activities) {
			try {
				act.load();
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}
	
	/**
	 * 关闭服务时（服务器关闭），把所有活动的状态保存起来，并关闭所有活动。
	 */
	public void shutdown() {
		for (Activity act : activities) {
			try {
				act.save();
			} catch (Exception e) {
				log.error(e, e);
			}
			if (act.isActive()) {
				try {
					act.shutdown();
				} catch (Exception e) {
					log.error(e, e);
				}
			}
		}
	}
	
	/**
	 * 添加一个活动（通常是服务器启动时）。添加活动的时候会检查这个活动是否有先前保存的数据需要载入。
	 * @param act
	 */
	public synchronized void addActivity(Activity act) {
		dao.newEntity(act);
		activities.add(act);
		try {
			act.load();
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	/**
	 * 列出所有的活动。
	 * @return
	 */
	public synchronized Activity[] getActivities() {
		Activity[] ret = new Activity[activities.size()];
		activities.toArray(ret);
		return ret;
	}
	
	/**
	 * 根据ID查找一个活动。
	 * @param id
	 * @return 如果活动找不到，返回null
	 */
	public synchronized Activity getActivityByID(int id) {
		for (int i = 0; i < activities.size(); i++) {
			if (activities.get(i).getId() == id) {
				return activities.get(i);
			}
		}
		return null;
	}
	
	/**
	 * 修改活动后保存。
	 * @param act
	 */
	public void saveActivity(Activity act) {
		dao.updateEntity(act);
	}
	
	/**
	 * 根据名称查找一个活动。
	 * @param String
	 * @return 如果活动找不到，返回null
	 */
	public synchronized Activity getActivityByName(String name) {
		for (int i = 0; i < activities.size(); i++) {
			if (activities.get(i).getName().equals(name)) {
				return activities.get(i);
			}
		}
		return null;
	}
	
	public synchronized Activity getActivityByImpClass(String impClass){
		for (int i = 0; i < activities.size(); i++) {
			if (activities.get(i).implClass.equals(impClass)) {
				return activities.get(i);
			}
		}
		return null;
	}
	
	public synchronized List<Activity> getActivitysByImpClass(String impClass){
		List<Activity> activitys = new ArrayList<Activity>();
		for (int i = 0; i < activities.size(); i++) {
			if (activities.get(i).implClass.equals(impClass)) {
				activitys.add(activities.get(i));
			}
		}
		return activitys;
	}
	
	/**
	 * 永久移除一个活动。如果次活动正在进行中，会先关闭掉。
	 * @param act
	 */
	public synchronized void removeActivity(Activity act) {
		if (act.isActive()) {
			try {
				act.shutdown();
				act.clear();
			} catch (Exception e) {
				log.error(e, e);
			}
		}
		activities.remove(act);
		dao.makeTransient(act);
	}
	
	/**
	 * 禁用/启用一个活动。
	 * @param act
	 * @param enabled
	 * @return
	 */
	public synchronized void setActivityEnabled(Activity act, boolean enabled) {
		act.enabled = enabled;
		dao.makePersistent(act);
	}
	
	public synchronized void update(int diff) {
		// 每1分钟检查一次所有活动的时间
		if (System.currentTimeMillis() - lastCheckTime > 60000) {
			lastCheckTime = System.currentTimeMillis();
			for (Activity act : activities) {
				if (act.isEnabled()) {
					boolean in = act.getSchedule().in();
					
					// 判断正在进行的活动是否到时间关闭了
					if (act.isActive() && !in) {
						try {
							act.shutdown();
						} catch (Exception e) {
							log.error(e, e);
						}
					}
					
					// 判断不在进行的活动是否到时间开启了
					if (!act.isActive() && in) {
						try {
							act.startup();
						} catch (Exception e) {
							log.error(e, e);
						}
					}
					
					// cycle处理
					if (act.isActive()) {
						act.update(diff);
					}
				} else {
					// 如果活动被禁用了，则关闭之
					if (act.isActive()) {
						try {
							act.shutdown();
						} catch (Exception e) {
							log.error(e, e);
						}
					}
				}
			}
		}
	}
}
