package peony.db;

import java.util.List;

import peony.game.nation.Forbid;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class ForbidDAO extends GenericHibernateDAO<Forbid, Integer> {
	@SuppressWarnings("unchecked")
	public List<Forbid> getAllForbids(){
		return list("from Forbid");
	}
	
	@Override
	public void clear(){
		update("from Forbid");
	}
}
