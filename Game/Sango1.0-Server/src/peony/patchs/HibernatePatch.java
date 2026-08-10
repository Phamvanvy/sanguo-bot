package peony.patchs;

import java.lang.reflect.Field;
import org.hibernate.connection.C3P0ConnectionProvider;
import org.hibernate.impl.SessionFactoryImpl;
import com.mchange.v2.c3p0.PoolBackedDataSource;
import com.pip.db.hibernateDAO.HibernateUtil;

/**
 * session¶Ï¿ªÖØÁ¬patch
 * @author dchen
 */
public class HibernatePatch implements Runnable {

	public void run() {
		try {
			// HibernateUtil hem = HibernateUtil.class;
			// Field f = hem.getClass().getDeclaredField("sessionFactory");
			Field f = null;
			// f.setAccessible(true);
			// SessionFactoryImpl sf = (SessionFactoryImpl)f.get(hem);
			SessionFactoryImpl sf = (SessionFactoryImpl) HibernateUtil.getSessionFactory();
			C3P0ConnectionProvider cp = (C3P0ConnectionProvider) sf.getConnectionProvider();
			f = cp.getClass().getDeclaredField("ds");
			f.setAccessible(true);
			PoolBackedDataSource ds = (PoolBackedDataSource) f.get(cp);
			ds.resetPoolManager(true);
			System.out.println("patch ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
