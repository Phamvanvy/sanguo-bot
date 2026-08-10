package peony.db;

import java.util.Date;
import java.util.List;

import peony.game.admin.GMRequest;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class GMRequestDAO extends GenericHibernateDAO<GMRequest, Integer>{
	
	public GMRequest getGMRequestById(int id) {
		return (GMRequest) uniqueResult("from GMRequest m where m.id=?", id);
	}
	
	@SuppressWarnings("unchecked")
	public List<GMRequest> getGMRequest(int begin, int count) {
		return limitList(
				"from GMRequest m order by m.createTime desc",
				begin,count);
	}
	
	public int getGMRequestCount(){
		Long l = (Long) uniqueResult(
				"select count(*) from GMRequest");
		return l.intValue();
	}
	
	@SuppressWarnings("unchecked")
	public List<GMRequest> getGMRequestByPlayerId(int playerId){
		return list("from GMRequest m where m.playerId=?",playerId);
	}
	
	public void deleteGMRequest(Date time){
		update("delete from GMRequest m where m.createTime<?"+time);
	}
	
	
}
