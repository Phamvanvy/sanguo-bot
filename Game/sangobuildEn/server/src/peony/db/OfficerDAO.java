package peony.db;

import java.util.List;

import peony.game.nation.Officer;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class OfficerDAO extends GenericHibernateDAO<Officer, Integer> {
	
	@SuppressWarnings("unchecked")
	public List<Officer> getAllOfficers(){
		return list("from Officer");
	}
	
	@Override
	public void clear(){
		update("from Officer");
	}
}
