package peony.db;

import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationList;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

/**
 * 玩家关系数据库访问对象。
 * @author lighthu
 */
public class PlayerRelationDAO extends GenericHibernateDAO<PlayerRelation, Integer> {
	/**
	 * 通过玩家ID查找玩家的关系信息。
	 * @param playerID
	 * @return
	 */
	public PlayerRelation findPlayerRelation(int playerID) {
		PlayerRelation ret = (PlayerRelation)uniqueResult("from PlayerRelation r where r.id=?",playerID);
		if (ret == null) {
			ret = new PlayerRelation();
			ret.id = playerID;
			ret.friends = new RelationList();
			ret.blackList = new RelationList();
			ret.enemies = new RelationList();
			ret.tempList = new RelationList();
			newEntity(ret);
		}
		return ret;
	}
}
