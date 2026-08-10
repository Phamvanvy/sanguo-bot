package peony.db;

import java.util.Date;
import java.util.List;
import peony.service.ranking.Ranking;
import peony.service.ranking.RankingService;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class RankingDAO extends GenericHibernateDAO<Ranking, Integer>{
	
	public Ranking findRankingByPlayerId(int playerId){
		return (Ranking)uniqueResult("from Ranking where playerid=? and type = ?",playerId,RankingService.TYPE_RONGYUTA);
	}
	
	@SuppressWarnings("unchecked")
	public List<Ranking> findRanking(){
		return list("from Ranking r where r.type=?",RankingService.TYPE_RONGYUTA);
	}
	
	@SuppressWarnings("unchecked")
	public List<Ranking> findTopTwenty(){
		return limitList(
				"from Ranking r where r.type =? order by r.value desc",0, 20,RankingService.TYPE_RONGYUTA);
	}
	
	public void delete(){
		update("delete Ranking r where r.type = ?",RankingService.TYPE_RONGYUTA);
	}
	
	public int getPlayerGrank(int playerId){
		Long l = (Long) uniqueResult(
				"select count(*) from Ranking a where a.type = ? and exists( select b.id from Ranking b where b.playerId = "+ playerId +" and b.playerId <> a.playerId and ((b.value < a.value) or (b.value = a.value and b.time <= a.time)))",RankingService.TYPE_RONGYUTA);
		return l.intValue() + 1;
	}
	
	public int getPlayerGrank(int playerId, int value){
		Long l = (Long) uniqueResult(
				"select count(*) from Ranking a where a.type = ? and  a.value > ? ",RankingService.TYPE_RONGYUTA, value);
		return l.intValue() + 1;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<Ranking> findCardTopTenOld(Date oldDate,Date newDate){
		return limitList(
				"from Ranking r where r.type =? and r.time>= ? and r.time <? order by r.value desc",0, 10,RankingService.TYPE_ROCKCARD,oldDate,newDate);
	}
	
	@SuppressWarnings("unchecked")
	public List<Ranking> findCardTopTenNew(Date date){
		return limitList(
				"from Ranking r where r.type =? and r.time >=? order by r.value desc",0, 10,RankingService.TYPE_ROCKCARD,date);
	}
	
	@SuppressWarnings("unchecked")
	public List<Ranking> getObsoleteCardRanking(Date date){
		return list("from Ranking r where r.type=? and r.time<?",RankingService.TYPE_ROCKCARD,date);
	}
	
	@SuppressWarnings("unchecked")
	public List<Ranking> findPrayTopTenOld(Date oldDate,Date newDate){
		return limitList(
				"from Ranking r where r.type =? and r.time>= ? and r.time <? order by r.value desc",0, 10,RankingService.TYPE_PRAY,oldDate,newDate);
	}
	
	@SuppressWarnings("unchecked")
	public List<Ranking> findPrayTopTenNew(Date date){
		return limitList(
				"from Ranking r where r.type =? and r.time >=? order by r.value desc",0, 10,RankingService.TYPE_PRAY,date);
	}
	
	@SuppressWarnings("unchecked")
	public List<Ranking> getObsoletePrayRanking(Date date){
		return list("from Ranking r where r.type=? and r.time<?",RankingService.TYPE_PRAY,date);
	}
}
