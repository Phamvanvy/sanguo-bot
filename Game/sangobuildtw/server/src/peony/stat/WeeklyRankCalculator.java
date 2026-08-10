package peony.stat;

import java.io.*;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import peony.game.CreditUtil;
import peony.game.DataService;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.sanguo.data.ProjectData;

/**
 * 每周战功结算，设置军衔。
 * 首先要按照
 * @author lighthu
 */
public class WeeklyRankCalculator {
	private static final Logger log = Logger.getLogger(WeeklyRankCalculator.class);
	
	XMLConfiguration config;
	ProjectData data;
	
	public static void main(String[] args) throws Exception{
	    try {
	        new WeeklyRankCalculator().launch();
	        log.info("WeeklyRank: Finished");
	    } catch (Exception e) {
	        log.error(e, e);
	    }
	}
	
	public WeeklyRankCalculator() throws Exception {
		config = new XMLConfiguration("peony.xml");
		DataService service = new DataService(new File(config.getString("datadir")),"PIP");
		data = service.data;
	}
	
	/*
	 * 计算可参与统计的人数。
	 */
	private int getTotalCount(int faction) {
	    int minCredit = CreditUtil.getStartCredit(data);
	    String hql = "select count(*) from Player p where p.weekCredit >= " + minCredit + " and p.faction="+faction;
	    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	    Transaction tx = session.beginTransaction();
	    try {
	        Query query = session.createQuery(hql);
	        int ret = ((Long)query.uniqueResult()).intValue();
	        tx.commit();
	        return ret;
	    } catch (HibernateException ex) {
            tx.rollback();
            throw ex;
        }
	}
	
	public void launch() throws Exception {
	    // 计算总数
		FileWriter fw = new FileWriter("weekrank.sql");
		PrintWriter out = new PrintWriter(fw);
		out.println("LOCK TABLES `player` WRITE;");
		for (int faction = 1; faction <= 3; faction++) {
			int total = getTotalCount(faction);
			log.info("WeeklyRank: total " + total);

			// 按战功从高到低排序读取，每批处理一千条数据
			int minCredit = CreditUtil.getStartCredit(data);
			String hql = "select p.id, p.weekCredit from Player p where p.weekCredit >= "
					+ minCredit + "and p.faction="+faction;
			hql += " order by p.weekCredit desc";
			int start = 0;
			int sequence = 0;
			while (start < total) {
				log.info("WeeklyRank: begin " + start);
				Session session = HibernateUtil.getSessionFactory()
						.getCurrentSession();
				Transaction tx = session.beginTransaction();
				try {
					Query query = session.createQuery(hql);
					query.setFirstResult(start);
					query.setMaxResults(10000);
					List list = query.list();
					for (int i = 0; i < list.size(); i++) {
						Object[] row = (Object[]) list.get(i);
						int playerID = ((Integer) row[0]).intValue();
						int credit = ((Integer) row[1]).intValue();
						int newRank = CreditUtil.getRank(data, credit,
								sequence, total).id;
						out.println("update player set rank = " + newRank + " where id = " + playerID + ";");
						sequence++;
					}
					tx.commit();
				} catch (HibernateException ex) {
					tx.rollback();
					throw ex;
				}
				start += 10000;
			}
		}
        
        // 更新全部记录，战功只保留一半
        log.info("WeeklyRank: update all");
        out.println("update player set weekcredit = weekcredit / 2;");
        out.println("UNLOCK TABLES;");
        
        out.close();
	}
}
