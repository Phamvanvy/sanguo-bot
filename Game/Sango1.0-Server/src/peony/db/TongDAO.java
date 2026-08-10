package peony.db;

import java.util.List;
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
	
	/**
	 * 载入开启自动接收新人的军团信息简表
	 */
	public List<Tong> getAutoAcceptTong(){
		return list("from Tong t where t.autoaccept = 1");
	}
	
	public List<Integer> getAutoAcceptTongIds(){
		return list("select id from Tong t where t.autoaccept=1");
	}
	
	/**
	 * 查询所有军团
	 */
	public List<Tong> getAllTong(){
		return list("from Tong");
	}
	
}
