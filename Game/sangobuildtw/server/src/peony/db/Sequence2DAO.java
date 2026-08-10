package peony.db;

import java.util.List;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class Sequence2DAO extends GenericHibernateDAO<Sequence2, Integer>{

	private static final String GETSEQUENCEBYNAME  = "from Sequence2 s where s.name=?";
	
//	public Sequence2 getSequence(int id){
//		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//		return (Sequence2)session.get(Sequence2.class, new Integer(id));
//	}
	
	public Sequence2 getSequence(String name){
		return (Sequence2)uniqueResult(GETSEQUENCEBYNAME,name);
//		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//		Query query = session.createQuery(GETSEQUENCEBYNAME);
//		query.setString("name", name);
//		return (Sequence2)query.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public Sequence2[] getSequences(){
//		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//		Query query = session.createQuery("from Sequence2");
		List<Sequence2> l = list("from Sequence2");
		Sequence2[] ret = new Sequence2[l.size()];
		l.toArray(ret);
		return ret;
	}
	
	public void save(Sequence2 sequence){
		updateEntity(sequence);
//		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//		session.update(sequence);
	}
}

