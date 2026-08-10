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
					    String title = "亲爱的" + name + "：\n感谢您支持明珠三国，作为对您在内测期间达到满级的奖励，特赠送礼包一个，祝您游戏愉快。";
					    GameItem item = ObjectAccessor.createGameItem(1228);
					    ms.sendSystemMail(id, "系统", "内测满级奖励", title, 0, item, 1, "GM");
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
