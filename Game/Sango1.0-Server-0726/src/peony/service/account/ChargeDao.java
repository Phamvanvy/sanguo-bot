package peony.service.account;

import java.util.List;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class ChargeDao extends GenericHibernateDAO<Charge, Integer> {

	@SuppressWarnings("unchecked")
	public List<Charge> getAllCharges(){
		return list("from Charge");
	}
	
}
