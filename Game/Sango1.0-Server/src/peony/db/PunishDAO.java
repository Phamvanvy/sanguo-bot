package peony.db;

import java.util.List;

import peony.game.nation.Punish;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class PunishDAO extends GenericHibernateDAO<Punish, Integer> {
	@SuppressWarnings("unchecked")
	public List<Punish> getAllPunishs(){
		return list("from Punish");
	}
	
	@Override
	public void clear(){
		update("from Punish");
	}
}
