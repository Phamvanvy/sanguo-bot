package peony.stat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.pip.db.hibernateDAO.HibernateUtil;

public class NationBuffWeeklyStatistics {
	
	private static final Logger log = Logger.getLogger(NationBuffWeeklyStatistics.class);
	
	private void getPlayerCount() throws IOException{
		Calendar c = Calendar.getInstance();
		Date e = c.getTime();
		Date d = new Date(e.getTime()-7*24*3600*100L); 
		String hql = "select sum(p.level), p.faction from Player p where p.level>=30 and p.exist=1 and p.lastLoginTime between ? and ? group by faction order by 2 asc";
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tx = session.beginTransaction();
		try {
			Query query = session.createQuery(hql);
			query.setParameter(0, d);
			query.setParameter(1, e);
			List list = query.list();
			int t=list.size();
			int[] o = new int[list.size()];
			for (int i = 0; i < list.size(); i++) {
				Object[] oarr = (Object[])list.get(i);
				o[i] = ((Long) oarr[0]).intValue();
			}
			int min = o[0];int max = o[0];int mid = o[0];
			float rate;
			if (min > o[1]) min = o[1];
			if (min > o[2]) min = o[2];
			if (max < o[1]) max = o[1];
			if (max < o[2]) max = o[2];
			if (o[1] != max && o[1] != min) mid = o[1];
			if (o[2] != max && o[2] != min) mid = o[2];
			ArrayList<Float> list_defense = new ArrayList<Float>();
			ArrayList<Float> list_anticrit = new ArrayList<Float>();
			ArrayList<Float> list_exp = new ArrayList<Float>();
			for (int i = 0; i < list.size(); i++) {
				if (o[i] == max) {
					rate = 0;
				} else if (o[i] == mid) {
					rate = Math.round((float)max / mid);
					if(rate>=1.5f)
						rate = 1.5f;
				} else {
					rate = Math.round((float)max / min);
					if(rate>=2.0f)
						rate = 2.0f;
				}
				if(rate * 0.03<=0.3)
					list_exp.add(Float.valueOf((float) (rate * 0.03)));	
				else
					list_exp.add(Float.valueOf(0.3f));	
				if(rate * 0.04<=0.4)
				list_defense.add(Float.valueOf((float) (rate * 0.04)));
				else
					list_defense.add(Float.valueOf(0.4f));
				if(rate * 0.02<=0.2)
				list_anticrit.add(Float.valueOf((float) (rate * 0.02)));
				else
					list_anticrit.add(Float.valueOf(0.2f));}
			
			
			tx.commit();
			Properties pros = new Properties();
			String weiValue = list_exp.get(0)+","+list_defense.get(0)+","+list_anticrit.get(0);
			String shuValue = list_exp.get(1)+","+list_defense.get(1)+","+list_anticrit.get(1);
			String wuValue = list_exp.get(2)+","+list_defense.get(2)+","+list_anticrit.get(2);
			log.info("[WEI]"+weiValue);
			log.info("[SHU]"+shuValue);
			log.info("[WU]"+wuValue);
			pros.setProperty("wei", weiValue);
			pros.setProperty("shu", shuValue);
			pros.setProperty("wu", wuValue);
			pros.store(new FileOutputStream(new File(System.getProperty("user.dir"),"nationbuff.properties")), null);
			log.info("[NATIONBUFFCOMPLETE]");
		} catch (HibernateException ex) {
			tx.rollback();
			ex.printStackTrace();
			throw ex;
		}

	}
	public static void main(String[] args) throws Exception {
		try {
			new NationBuffWeeklyStatistics().getPlayerCount();
		} catch (Exception e) {

		}
	}

}

