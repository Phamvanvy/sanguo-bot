package peony.game.beautyparade;

import java.util.List;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class BeautySignDao extends GenericHibernateDAO<Beauty, Integer> {
	
	@SuppressWarnings("unchecked")
	public List<Beauty> getBeautys(){
		return list("from Beauty");
	}
	
}
