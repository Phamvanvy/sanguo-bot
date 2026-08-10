package com.pip.itimes.server.dao;


import java.util.List;

import com.pip.itimes.server.bean.Bbs;

public class BbsDao extends BaseDao {

    public BbsDao() {
        super();
    }

    public void addBbs(Bbs bbs) throws DataAccessException {
        makePersistent(bbs);
    }

    public List getBbsList(int bbsId, int begin, int maxCount) throws
            DataAccessException {
        return getLimitedList("from Bbs b where ( b.bbsId=" + bbsId +
                              " or b.bbsId= -1 ) and  b.priority = 100 order by b.priority desc,b.postTime desc",
                              begin, maxCount);
    }
    public List getAdminList(int bbsId, int begin, int maxCount) throws
    		DataAccessException {
    	return getLimitedList("from Bbs b where b.bbsId=" + bbsId +
                      	" or b.bbsId= -1 order by b.priority desc,b.postTime desc",
                      	begin, maxCount);
    }
	public List getSystemBbsList(int bbsId, int begin, int maxCount) throws
		    DataAccessException {
		return getLimitedList("from Bbs b where ( b.bbsId=" + bbsId +
                              " or b.bbsId= -1 ) and b.playerId= -1 order by b.priority desc,b.postTime desc",
		                      begin, maxCount);
	}

    public Bbs getBbs(int id) throws DataAccessException {
        return (Bbs) getObject(Bbs.class, new Integer(id));
    }

    public int getBbsCount(int bbsId) throws DataAccessException {
        return getCount("from Bbs b where  ( b.bbsId=" + bbsId +
                              " or b.bbsId= -1 ) and  b.priority = 100");
    }
    public int getAdminBbsCount(int bbsId) throws DataAccessException {
        return getCount("from Bbs b where  b.bbsId=" + bbsId +
                              " or b.bbsId= -1");
    }
    public int getSystemCount(int bbsId) throws DataAccessException {
        return getCount("from Bbs b where (b.bbsId=" + bbsId +" or b.bbsId=-1) and b.playerId= -1 ");
    }
    public Bbs deleteBbs(int id) throws DataAccessException{
        Bbs bbs = (Bbs)getObject(Bbs.class,new Integer(id));
        if(bbs!=null)
            makeTransient(bbs);
        return bbs;
    }

    public void deleteAllBbs(int bbsId) throws DataAccessException{
        query("delete from Bbs b where b.bbsId="+bbsId);
    }
    //mengjie add
    public List getBbsidList(int playerId,int bbsId, int begin, int maxCount) throws
            DataAccessException {
    	if (bbsId==-3){
    		return getLimitedList("from Bbs b where b.playerId=" + playerId +
                      " order by b.priority desc,b.postTime desc",
                      begin, maxCount);
    	}else{
    		return getLimitedList("from Bbs b where b.playerId=" + playerId +
                    " and b.bbsId="+bbsId+" order by b.priority desc,b.postTime desc",
                    begin, maxCount);
    	}
    }
    public int getBbsidCount(int playerId,int bbsId) throws DataAccessException {
    	if (bbsId==-3){
    		return getCount("from Bbs b where b.playerId=" + playerId);
    	}else{
    		return getCount("from Bbs b where b.playerId=" + playerId +  "and b.bbsId="+bbsId);
    	}
    }
    public void deleteBbsbyid(int bbsId,int begin,int end) throws DataAccessException{
    	if (bbsId == -3){
    		query("delete from Bbs b where b.id>="+begin+" and b.id<="+end);
    	}else{
    		query("delete from Bbs b where b.bbsId="+bbsId+" and b.id>="+begin+" and b.id<="+end);
    	}
    }
    public void deleteBbsbyplayerid(int bbsId,int playerid) throws DataAccessException{
    	if (bbsId == -3){
    		query("delete from Bbs b where b.playerId="+playerid);
    	}else{
    		query("delete from Bbs b where b.bbsId="+bbsId+" and b.playerId="+playerid);
    	}
    }
    //mengjie add end
}
