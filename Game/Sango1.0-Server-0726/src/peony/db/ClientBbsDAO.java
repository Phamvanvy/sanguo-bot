package peony.db;

import java.util.List;

import peony.game.clientbbs.ClientBbs;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class ClientBbsDAO extends GenericHibernateDAO<ClientBbs, Integer> {
	@SuppressWarnings("unchecked")
	public List<ClientBbs> getClientBbs(){
		return list(
				"from ClientBbs m where m.enable=1");
	}
}
