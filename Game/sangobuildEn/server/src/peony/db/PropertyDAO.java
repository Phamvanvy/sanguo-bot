package peony.db;

import java.util.List;

import peony.game.Property;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class PropertyDAO extends GenericHibernateDAO<Property, Integer> {

	public Property getPropertyById(int id) {
		return (Property) uniqueResult("from Property p where p.id=?", id);
	}
	
	@SuppressWarnings("unchecked")
	public List getPropertys(){
		return list("from Property");
	}
}

