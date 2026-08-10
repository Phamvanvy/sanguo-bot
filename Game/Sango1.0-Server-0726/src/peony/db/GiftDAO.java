package peony.db;

import peony.game.gift.GiftHistory;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class GiftDAO extends GenericHibernateDAO<GiftHistory, Integer> {
	
	public GiftHistory getHistory(int playerId, int groupId) {
		return (GiftHistory) uniqueResult(
				"from GiftHistory t where t.playerId = ? and t.groupId=?",
				playerId, groupId);
	}
}
