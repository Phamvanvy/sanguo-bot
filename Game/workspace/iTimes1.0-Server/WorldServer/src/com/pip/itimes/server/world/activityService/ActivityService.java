package com.pip.itimes.server.world.activityService;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.pip.itimes.server.world.activityService.activity.ActivityData;
import com.pip.itimes.server.world.activityService.activity.Service;
import com.pip.itimes.server.bean.Activity;
import com.pip.itimes.server.dao.ActivityDao;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.PlayerService;

public class ActivityService implements Service {
	private Logger log = Logger.getLogger(ActivityService.class);
	
	protected PlayerService playerService;
	private List<ActivityData> activities = new ArrayList();
	private long lastCheckTime = Utils.getTodayStart();
	private ActivityDao dao = new ActivityDao();
	
	public void setPlayerService (PlayerService playerService) {
		this.playerService = playerService;
	}
	
	public void startup() throws Exception {
		List<Activity> l = dao.getActivities();
		for (Activity a : l) {
			ActivityData ad = new ActivityData(a);
			activities.add(ad);
		}
		for (ActivityData act : activities) {
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
		for (ActivityData act : activities) {
			try {
				act.save();
			} catch (Exception e) {
				log.error(e, e);
			}
			if (act.isEnabled()) {
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
	 * @throws DataAccessException 
	 */
	public synchronized void addActivity(ActivityData act) throws DataAccessException {
		act.reset();
		try {
            dao.makePersistent(act.getActivity());
        } catch (DataAccessException ex) {
        	log.error(ex, ex);
        	throw new DataAccessException(ex + "保存活动失败");
        }
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
	public synchronized ActivityData[] getActivities() {
		ActivityData[] ret = new ActivityData[activities.size()];
		activities.toArray(ret);
		return ret;
	}
	
	/**
	 * 根据ID查找一个活动。
	 * @param id
	 * @return 如果活动找不到，返回null
	 */
	public synchronized ActivityData getActivityByID(int id) {
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
	public boolean saveActivity(ActivityData act) {
		try {
			dao.updateActivity(act.getActivity());
			return true;
		} catch (DataAccessException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 根据名称查找一个活动。
	 * @param String
	 * @return 如果活动找不到，返回null
	 */
	public synchronized ActivityData getActivityByName(String name) {
		for (int i = 0; i < activities.size(); i++) {
			if (activities.get(i).getName().equals(name)) {
				return activities.get(i);
			}
		}
		return null;
	}
	
	/**
	 * 永久移除一个活动。如果次活动正在进行中，会先关闭掉。
	 * @param act
	 */
	public synchronized boolean removeActivity(ActivityData act) {
		if (act.isEnabled()) {
			try {
				act.shutdown();
				act.clear();
			} catch (Exception e) {
				log.error(e, e);
			}
		}
		try {
			dao.deleteActivity(act.getActivity());
		} catch (DataAccessException e) {
			e.printStackTrace();
			return false;
		}
		activities.remove(act);
		return true;
	}
	
	/**
	 * 禁用/启用一个活动。
	 * @param act
	 * @param enabled
	 * @return
	 *//*
	public synchronized void setActivityValid(ActivityData act, boolean valid) {
		act.setValid(valid);
		try {
			dao.updateActivity(act.getActivity());
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}*/

	public synchronized void process(long time) {
		try {
			// 每1分钟检查一次所有活动的时间
			if (time - lastCheckTime > 60000) {
				for (ActivityData act : activities) {
					act.process(time);
				}
				lastCheckTime = time;
			}
		} catch (Exception e) {
            log.error(e, e);
        }
	}
}
