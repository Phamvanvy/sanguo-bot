package peony.test;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import peony.game.Player;
import peony.game.Server;
import com.pip.db.hibernateDAO.HibernateUtil;

/**
 * 处理内测期间坐骑不能保存的问题：删除所有赠送坐骑的任务记录。
 * @author lighthu
 */
public class DataPatch1 {
	private static final Logger log = Logger.getLogger(DataPatch1.class);
	
	public static void main(String[] args) throws Exception{
	    try {
	        new Server();
	        new DataPatch1().launch();
	        log.info("DataPatch1: Finished");
	    } catch (Exception e) {
	        log.error(e, e);
	    }
	}
	
	public DataPatch1() {
	}
	
	public void launch() throws Exception {
	    // 分批处理，每次1000条
	    String hql = "from Player";
        int start = 0;
        while (true) {
            log.info("DataPatch1: begin " + start);
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            Transaction tx = session.beginTransaction();
            try {
                Query query = session.createQuery(hql);
                query.setFirstResult(start);
                query.setMaxResults(1);
                
                List list = null;
				try {
					list = query.list();
					for (int i = 0; i < list.size(); i++) {
					    Player p = (Player)list.get(i);
					    p.asmVm.removeFinished(370);
					    p.asmVm.removeFinished(371);
					    p.asmVm.removeFinished(372);
					    session.saveOrUpdate(p);
					}
				} catch (Exception e) {
					log.error(e,e);
				}
                
                tx.commit();
                
                // 少于1000条记录说明到结尾了
                if (list!=null&&list.size() < 1) {
                    break;
                }
            } catch (HibernateException ex) {
                tx.rollback();
                throw ex;
            }
            start += 1;
        }
	}
}
