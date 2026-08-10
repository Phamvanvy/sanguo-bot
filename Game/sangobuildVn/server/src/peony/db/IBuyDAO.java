package peony.db;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.hibernate.Transaction;
import peony.service.shop.IBuy;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class IBuyDAO extends GenericHibernateDAO<IBuy, Integer> {
	/**
	 * 查询一个玩家在指定日期到现在总共消费金额。
	 * 
	 * @param playerID
	 * @param startTime
	 * @return 单位为1/100分，1/1000元宝
	 */
	public int getTotalConsume(int playerID, Date startTime) {
		String hql = "select sum(imoney) from IBuy where playerid = ? and buytime > ?";
		return ((Long) uniqueResult(hql, playerID, startTime)).intValue();
	}

	@SuppressWarnings("unchecked")
	public List<IBuy> getTopConsume(Date startTime, Date endTime, int max) {
		Transaction tx = getSession().beginTransaction();
		List list = null;
		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = format.format(startTime);
			String time2 = format.format(endTime);
			list = getSession()
					.createSQLQuery(
							"select id,playerid,sum(imoney)imoney,accountid,itemname,itemid,type,buytime,otherplayerid,otherplayername,count,giftflag,level,faction from ibuy where buytime>\'"
									+ time
									+ "\' and buytime<\'"
									+ time2
									+ "\' group by playerid order by imoney desc limit "
									+ max).addEntity(IBuy.class).list();
			tx.commit();
		} catch (Exception ex) {
			tx.rollback();
			throw new RuntimeException(ex);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<IBuy> getTopConsume(Date startTime, Date endTime, int max, int faction) {
		Transaction tx = getSession().beginTransaction();
		List list = null;
		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = format.format(startTime);
			String time2 = format.format(endTime);
			list = getSession()
			.createSQLQuery(
					"select id,playerid,sum(imoney)imoney,accountid,itemname,itemid,type,buytime,otherplayerid,otherplayername,count,giftflag,level,faction from ibuy where buytime>\'"
					+ time
					+ "\' and buytime<\'"
					+ time2
					+ "\' and faction="+faction+" group by playerid order by imoney desc limit "
					+ max).addEntity(IBuy.class).list();
			tx.commit();
		} catch (Exception ex) {
			tx.rollback();
			throw new RuntimeException(ex);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getTopItemConsume(Date startTime, Date endTime, int max) {
		Transaction tx = getSession().beginTransaction();
		List list = null;
		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = format.format(startTime);
			String time2 = format.format(endTime);
			list = getSession()
			.createSQLQuery(
					"select itemid from ibuy where buytime>\'"
					+ time
					+ "\' and buytime<\'"
					+ time2
					+ "\' group by itemid order by count(itemid) desc limit "
					+ max).list();
			tx.commit();
		} catch (Exception ex) {
			tx.rollback();
			throw new RuntimeException(ex);
		}
		return list;
	}
	
	public int getTotalConsumeTillNow(int playerID) {
		String hql = "select sum(imoney) from IBuy where playerid = ?";
		return ((Long)uniqueResult(hql, playerID)).intValue();
	}
}
