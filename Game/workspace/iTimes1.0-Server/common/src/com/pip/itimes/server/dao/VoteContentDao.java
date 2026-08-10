package com.pip.itimes.server.dao;

import java.util.List;

import org.hibernate.Query;

import com.pip.itimes.server.bean.VoteContent;

public class VoteContentDao extends BaseDao{
    public void addVoteContent(VoteContent voteContent) throws DataAccessException {
        makePersistent(voteContent);
    }
    public List getAll(int voteType, String start, String end) throws DataAccessException{
        String hql = "select votersid, valid, content from VoteContent t where type = " + voteType + " and createtime between '" + start + "' and '" + end + "'";
        List l = getList(hql);
        return l;
    }
    
    public void update(int voteType, int playerId, String voteContent)throws DataAccessException{
    	Query query;
		query = getSession().createQuery("update VoteContent p set p.content = '" + voteContent + "' where p.votersid= "+ playerId+ " and type = " + voteType);
		query.executeUpdate();
    }
    
    public void delete(int playerId)throws DataAccessException{
    	Query query;
		query = getSession().createQuery("update VoteContent p set p.valid = false where p.votersid= "+ playerId);
		query.executeUpdate();
    }
    
    // 更新玩家数据为不可以颁奖状态
    public void updateValid (int type, int playerId) {
    	Query query;
    	query = getSession().createQuery("update VoteContent p set p.valid = false where p.type= " + type + " and p.votersid= " + playerId);
    	query.executeUpdate();
    }
}
