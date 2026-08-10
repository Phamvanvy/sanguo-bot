package peony.db;

import java.util.Date;
import java.util.Calendar;
import java.util.List;

import peony.game.Actor;
import peony.game.ActorListActor;
import peony.game.Player;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class PlayerDAO extends GenericHibernateDAO<Player, Integer> {
	
	@SuppressWarnings("unchecked")
	public List<ActorListActor> getActorList(int accountId){
		return list("from ActorListActor a where a.accountId=? and a.exist=1 order by a.createTime",accountId);
	}

	public int getActorCount(int accountId){
		return ((Long)uniqueResult("select count(*) from ActorListActor a where a.accountId=? and a.exist=1", accountId)).intValue();
	}
	
	public Actor getActor(int id) {
		return (Actor)uniqueResult("from Actor a where a.id=? and a.exist=1",id);
	}
	
	public Actor getActor(String name) {
		return (Actor)uniqueResult("from Actor a where a.name =? and a.exist=1",name);
	}
	
	@SuppressWarnings("unchecked")
	public List<Actor> getActors(int[] ids) {
		StringBuffer buf = new StringBuffer();
		buf.append("from Actor a where a.id in (");
		for (int i = ids.length - 1; i >= 0; i--) {
			buf.append(ids[i]);
			if (i > 0) {
				buf.append(",");
			}
		}
		buf.append(") and a.exist=1");
		return list(buf.toString());
	}
	
	public Player getPlayerById(int id){
		return (Player)uniqueResult("from Player p where p.id=? and p.exist=1",id);
	}
	
	public Player getDeletedPlayerById(int id){
		return (Player)uniqueResult("from Player p where p.id=? and p.exist=0",id);
	}
	
	/**
	 * 根据用户名字查询用户Id，如果没有查到此用户，那么返回-1
	 * @param name
	 * @return
	 */
	public int getPlayerIdByName(String name){
		Integer ret = (Integer)uniqueResult("select p.id from Player p where p.name=? and p.exist=1",name);
		if(ret!=null)
			return ret.intValue();
		return -1;
	}
	
	public List<Player> getPlayers(int begin,int count){
		return limitList("from Player", begin, count);
	}
	
	public List<Integer> getTopWeekRanks(int count,int faction){
		return limitList("select id from Player p where p.exist=1 and faction=? order by p.weekCredit desc",0,count,faction);
	}
	
	public List<Integer> getTopLevelRanks(int count,int faction){
		if (faction == 0) {
			// 取全部
			return limitList("select id from Player p where p.exist=1 order by p.level desc",0,count);
		} else {
			return limitList("select id from Player p where p.exist=1 and faction=? order by p.level desc",0,count,faction);
		}
	}
	
	public long countPlayer(int accountid){
		long ret=(Long)uniqueResult("select count(*) from Player p where p.accountId=?",accountid);
		return ret;
	}
	
	public int getMaxLevelOfAccount(int accountid){
		return Integer.parseInt(uniqueResult("select max(level) from Player p where p.accountId=?",accountid).toString());
	}
	
	public List<Actor> getActorsByName(String name){
		return list("form Actor a where a.name=?", name);
	}
	
	@SuppressWarnings("unchecked")
	public List<Player> getPlayerByAcc(int accountid){
		return list("from Player p where p.exist=1 and p.accountId=?",accountid);
	}
	
	public int getMaxId(){
		return Integer.parseInt(uniqueResult("select max(id) from Player p").toString());
	}
	
	public List<Player> getDescActors(int accountId, int exceptActorId){
		List<Player> actors = (List<Player>) list("from Player p where p.accountId = ? and p.id <> ? and p.exist = 1 order by p.lastLoginTime desc", accountId, exceptActorId);
		return actors;
	}
	
}
