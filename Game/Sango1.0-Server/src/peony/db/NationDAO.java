package peony.db;

import java.util.List;

import peony.game.nation.Nation;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class NationDAO extends GenericHibernateDAO<Nation, Integer> {
	@SuppressWarnings("unchecked")
	public List<Nation> getAllNations(){
		return list("from Nation");
	}
}
