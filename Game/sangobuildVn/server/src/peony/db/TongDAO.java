package peony.db;

import peony.service.tong.Tong;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

/**
 * 帮派数据库访问对象。
 * @author lighthu
 */
public class TongDAO extends GenericHibernateDAO<Tong, Integer> {
	/**
	 * 根据名字查找帮派。
	 * @param name
	 * @return
	 */
	public Tong findByName(String name) {
		return (Tong)uniqueResult("from Tong t where t.name = ?",name);
	}

	/**
	 * 根据ID查找帮派。
	 * @param id
	 * @return
	 */
	public Tong findByTongID(int id) {
		return (Tong)uniqueResult("from Tong t where t.id = ?",id);
	}
}
