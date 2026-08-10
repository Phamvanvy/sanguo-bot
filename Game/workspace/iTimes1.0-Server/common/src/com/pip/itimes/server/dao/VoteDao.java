package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.Vote;

import java.util.List;

import org.hibernate.Query;

public class VoteDao extends BaseDao {
    public VoteDao() {
    }

    /**
     * @param voteType
     * @return 按照有效性，选取被选取人的玩家id，和被投票的票数
     * @throws DataAccessException
     */
    public List getAll(int voteType, String start, String end) throws DataAccessException{
        String hql = "select playeridvoters, sum(votepoint) from Vote t where type = " + voteType + " and createtime between '" + start + "' and '" + end + "' group by playeridvoters";
        List l = getList(hql);
        return l;
    }
    /**
     * 保存
     * @param ti
     * @throws DataAccessException
     */
    public void saveVote(Vote ti) throws DataAccessException{
        makePersistent(ti);
    }
    
    public List getVotePlayers (int voteType, int playerId, String start, String end) throws DataAccessException{
    	 String hql = "select votersid, sum(votepoint) from Vote t where type = " + voteType + " and playeridvoters = " + playerId + " and createtime between '" + start + "' and '" + end + "' group by votersid";
         List l = getLimitedList(hql,0,15);
         return l;
    }
    
    public void deleteVote( int playerId)throws DataAccessException{
    	Query query;
		query = getSession().createQuery("update Vote p set p.valid = false where p.votersid="+ playerId+ "or p.playeridvoters = " +  playerId);
		query.executeUpdate();
    	// String hql = "set valid = false from Vote t where playeridvoters = " + playerId;
    	// query(hql);
    }
    
    /**
     * @param voteType
     * @return 按照有效性，选取收费道具投票大王
     * @throws DataAccessException
     */
    public List getVotesKing (int voteType, boolean isImoneyItem, String start, String end) throws DataAccessException {
    	int type = 0;
    	if (isImoneyItem) {
    		type = 1;
    	}
        String hql = "select votersid, sum(votepoint) from Vote t where type = " + voteType + " and isimoneyitem = " + type + " and createtime between '" + start + "' and '" + end + "' group by votersid order by 2 desc";
        List l = getLimitedList(hql,0,1);
        return l;
    }
}
