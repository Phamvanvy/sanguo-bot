package com.pip.itimes.server.dao;

import java.util.List;

import com.pip.itimes.server.bean.Activity;

public class ActivityDao extends BaseDao  {
	public ActivityDao() {
        super();
    }
	
	/**
	 * 添加活动
	 * @param activity
	 * @throws DataAccessException
	 */
	public void addActivity(Activity activity) throws DataAccessException {
        makePersistent(activity);
    }
	
	/**
	 * 活动失效
	 * @param name
	 * @throws DataAccessException
	 */
	public void updateActivityFailure (Activity activity) throws DataAccessException {
		query("update Activity p set p.enable=false where p.name='" + activity.getName() + "'");
    }
	
	/**
	 * 获得所有活动
	 * @return
	 * @throws DataAccessException
	 */
	public List<Activity> getActivities () throws DataAccessException {
		String sql = "from Activity p";
		List l = getList(sql);
        return l;
	}
	
	/**
	 * 激活/禁止活动
	 * @param activity
	 * @throws DataAccessException
	 */
	public void updateActivity (Activity activity) throws DataAccessException {
		query("update Activity p set p.valid=" + activity.getValid() + " where p.name='" + activity.getName() + "'");
	}
	
	/**
	 * 删除活动
	 * @param activity
	 * @throws DataAccessException
	 */
	public void deleteActivity (Activity activity) throws DataAccessException {
		query("delete Activity p where p.name='" + activity.getName() + "'");
	}
}
