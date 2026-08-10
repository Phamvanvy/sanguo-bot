package peony.db;

import java.util.List;

import peony.service.stat.PvpInfo;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class PvpInfoDAO extends GenericHibernateDAO<PvpInfo, Integer> {

	public PvpInfo getPvpInfoById(int id) {
		return (PvpInfo) uniqueResult("from PvpInfo m where m.id=?", id);
	}

	@SuppressWarnings("unchecked")
	public List<PvpInfo> getTopPvpInfos(int count,int faction) {
		return super.limitList(
				"from PvpInfo m where m.faction=? and m.yesterdayKillCount>0 order by m.yesterdayKillCount desc", 0, count,faction);
	}

	public void updatePvpInfos() {
		super.bulkUpdate("update PvpInfo m set m.yesterdayKillCount=m.todayKillCount,m.yesterdayDieCount=m.todayDieCount,m.todayDieCount=0,m.todayKillCount=0");
	}
}
