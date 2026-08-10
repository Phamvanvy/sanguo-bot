package peony.db;

import java.util.List;

import peony.game.bbs.Bbs;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class BbsDAO extends GenericHibernateDAO<Bbs, Integer> {
	public Bbs find(int id){
		return (Bbs)uniqueResult("from Bbs b where b.id=?", id);
	}
	
	@SuppressWarnings("unchecked")
	public List<Bbs> getBbs(int begin,int count){
		return limitList(
				"from Bbs b order by b.order desc,b.createTime desc",
				begin, count);
	}
	
	public int getBbsCount() {
		Long l = (Long) uniqueResult(
				"select count(*) from Bbs");
		return l.intValue();
	}
	
	@SuppressWarnings("unchecked")
	public List<Bbs> getBbs(){
		return super.list("from Bbs");
	}
}
