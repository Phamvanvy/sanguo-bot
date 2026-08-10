package peony.db;

import java.util.List;

import peony.service.fame.Fame;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class FameDAO extends GenericHibernateDAO<Fame, Integer>{
	
	public Fame findFameByPlayerId(int playerId){
		return (Fame)uniqueResult("from Fame where playerid=?",playerId);
	}
	
	@SuppressWarnings("unchecked")
	public List<Fame> findFame(){
		return list("from Fame");
	}

}
