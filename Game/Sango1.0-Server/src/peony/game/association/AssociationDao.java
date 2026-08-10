package peony.game.association;

import java.util.List;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class AssociationDao extends GenericHibernateDAO<Association, Integer> {
	
	@SuppressWarnings("unchecked")
	public List<Association> getAllAssociations(){
		return list("from Association");
	}
	
	public Association findByName(String name){
		return (Association) uniqueResult("from Association a where a.name = ?", name);
	}
	
}
