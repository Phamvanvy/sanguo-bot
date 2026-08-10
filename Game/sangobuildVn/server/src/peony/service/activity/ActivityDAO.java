package peony.service.activity;

import java.util.Date;
import java.util.List;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class ActivityDAO extends GenericHibernateDAO<Activity, Integer> {
	/**
	 * 列出所有活动。
	 */
	@SuppressWarnings("unchecked")
	public List<Activity> getActivities() {
		return list("from Activity");
	}
}
