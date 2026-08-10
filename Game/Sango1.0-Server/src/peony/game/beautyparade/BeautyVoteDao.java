package peony.game.beautyparade;

import java.util.List;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class BeautyVoteDao extends GenericHibernateDAO<VotePlayer, Integer> {
	
	@SuppressWarnings("unchecked")
	public List<VotePlayer> getVotePlayers(){
		return list("from VotePlayer");
	}
	
}
