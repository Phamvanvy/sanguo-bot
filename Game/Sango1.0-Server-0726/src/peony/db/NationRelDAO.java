package peony.db;

import java.util.List;

import peony.game.nation.NationRel;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class NationRelDAO extends GenericHibernateDAO<NationRel, Integer> {
	
	@SuppressWarnings("unchecked")
	public List<NationRel> getRels(){
		return super.list("from NationRel");
	}
}
