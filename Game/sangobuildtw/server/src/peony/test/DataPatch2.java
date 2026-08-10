package peony.test;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Server;
import peony.game.mail.MailService;
import com.pip.db.hibernateDAO.HibernateUtil;

/**
 * 对所有满级用户，发放礼包。
 * @author lighthu
 */
public class DataPatch2 {
	private static final Logger log = Logger.getLogger(DataPatch2.class);
	
	public static void main(String[] args) throws Exception{
	    try {
	        new Server();
	        new DataPatch2().launch();
	        log.info("DataPatch2: Finished");
	    } catch (Exception e) {
	        log.error(e, e);
	    }
	}
	
	public DataPatch2() {
	}
	
	public void launch() throws Exception {
	    // 分批处理，每次100条
	    String hql = "select p.id, p.name from Player p where p.level = 30";
        int start = 0;
        MailService ms = Server.server.getServiceRegistry().getMailService();
        while (true) {
            log.info("DataPatch1: begin " + start);
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            Transaction tx = session.beginTransaction();
            try {
                Query query = session.createQuery(hql);
                query.setFirstResult(start);
                query.setMaxResults(100);
                
                List list = null;
				try {
					list = query.list();
					tx.commit();
					for (int i = 0; i < list.size(); i++) {
					    Object[] arr = (Object[])list.get(i);
					    int id = ((Integer)arr[0]).intValue();
					    String name = (String)arr[1];
					    String title = "親愛的" + name + "：\n感謝您支持明珠三國,作為對您在內測期間達到滿級的獎勵,特贈送禮包一個,祝您遊戲愉快.";
					    GameItem item = ObjectAccessor.createGameItem(1228);
					    ms.sendSystemMail(id, "系統", "內測滿級獎勵", title, 0, item, 1, "GM");
					}
				} catch (Exception e) {
					log.error(e,e);
				}
                
                // 少于100条记录说明到结尾了
                if (list!=null&&list.size() < 100) {
                    break;
                }
            } catch (HibernateException ex) {
                log.error(ex, ex);
                throw ex;
            }
            start += 100;
        }
	}
}
