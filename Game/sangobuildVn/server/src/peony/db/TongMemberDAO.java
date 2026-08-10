package peony.db;

import java.util.List;

import peony.service.tong.TongMember;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

/**
 * 访问帮派成员。
 * @author lighthu
 */
public class TongMemberDAO extends GenericHibernateDAO<TongMember, Integer> {
	/**
	 * 查找某个帮派内的所有成员。
	 * @param tongID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<TongMember> listTongMembers(int tongID) {
		return list("from TongMember t where t.tongID = ?",tongID);
	}

	/**
	 * 根据玩家ID查找成员记录。
	 * @param id
	 * @return
	 */
	public TongMember findByPlayerID(int id) {
		return (TongMember)uniqueResult("from TongMember t where t.id = ?",id);
	}
}
