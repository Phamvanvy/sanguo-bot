package com.pip.itimes.server.world;

import java.util.Date;

import com.pip.itimes.server.bean.Bbs;
import com.pip.itimes.server.dao.BbsDao;
import com.pip.itimes.server.dao.DataAccessException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class BbsService {

    private BbsDao dao;

     private static Set forbidenbbsId = new HashSet();

    public BbsService(BbsDao dao) {
        this.dao = dao;
    }

    public void addBbs(int bbsId, int playerId, String playerName, String title,
                       String content, int priority) throws DataAccessException {
        Bbs bbs = new Bbs();
        bbs.setBbsId(bbsId);
        bbs.setPlayerId(playerId);
        bbs.setPlayerName(playerName);
        bbs.setTitle(title);
        bbs.setContent(content);
        bbs.setPostTime(new Date());
        bbs.setPriority(priority);
        dao.addBbs(bbs);
    }

    public void addBbs(Bbs bbs) throws DataAccessException{
        dao.addBbs(bbs);
    }

    public Bbs deleteBbs(int id) throws DataAccessException{
        return dao.deleteBbs(id);
    }

    public void deleteAllBbs(int bbsId) throws DataAccessException{
        dao.deleteAllBbs(bbsId);
    }

    public Bbs getBbs(int id) throws DataAccessException{
        return dao.getBbs(id);
    }

    public BbsResult getBbsList(int bbsId, int pageSize, int pageNo) throws
            DataAccessException {
        int total = dao.getBbsCount(bbsId);
        if (pageNo * pageSize >= total) {
            BbsResult ret = new BbsResult();
            ret.bbs = new Bbs[0];
            ret.pageCount = 0;
            return ret;
        }
        int pageCount = total / pageSize;
        if (total % pageSize != 0)
            pageCount++;
        List l = dao.getBbsList(bbsId, pageNo * pageSize, pageSize);
//            int retCount = l.size();
        BbsResult ret = new BbsResult();
        Bbs[] bbs = new Bbs[l.size()];
        l.toArray(bbs);
        ret.bbs = bbs;
        ret.pageCount = pageCount;
        return ret;
    }
    
    public BbsResult getAdminBbsList(int bbsId, int pageSize, int pageNo) throws
		    DataAccessException {
		int total = dao.getAdminBbsCount(bbsId);
		if (pageNo * pageSize >= total) {
		    BbsResult ret = new BbsResult();
		    ret.bbs = new Bbs[0];
		    ret.pageCount = 0;
		    return ret;
		}
		int pageCount = total / pageSize;
		if (total % pageSize != 0)
		    pageCount++;
		List l = dao.getAdminList(bbsId, pageNo * pageSize, pageSize);
		//    int retCount = l.size();
		BbsResult ret = new BbsResult();
		Bbs[] bbs = new Bbs[l.size()];
		l.toArray(bbs);
		ret.bbs = bbs;
		ret.pageCount = pageCount;
		return ret;
	}
    //jwp add
    public int getSystemBbsCount(int bbsId) throws DataAccessException{
    	return dao.getSystemCount(bbsId);
    }
    public BbsResult getSystemBbsList(int bbsId, int count) throws
	    DataAccessException {
		List l = dao.getSystemBbsList(bbsId, 0, count);
		//    int retCount = l.size();
		BbsResult ret = new BbsResult();
		Bbs[] bbs = new Bbs[l.size()];
		l.toArray(bbs);
		ret.bbs = bbs;
		return ret;
    }
    //jwp add end
    
    //mengjie add
    public BbsResult getBbsListbyplayerid(int playerId,int bbsId,int pageSize, int pageNo) throws
    DataAccessException {
    	int total = dao.getBbsidCount(playerId,bbsId);
        if (pageNo * pageSize >= total) {
        	BbsResult ret = new BbsResult();
        	ret.bbs = new Bbs[0];
            ret.pageCount = 0;
            return ret;
        }
        int pageCount = total / pageSize;
        if (total % pageSize != 0)
            pageCount++;
        List l = dao.getBbsidList(playerId,bbsId,pageNo * pageSize, pageSize);
        BbsResult ret = new BbsResult();
        Bbs[] bbs = new Bbs[l.size()];
        l.toArray(bbs);
        ret.bbs = bbs;
        ret.pageCount = pageCount;
        return ret;
    }
    public void deleteBbsbyid(int id,int begin,int end) throws DataAccessException{
        dao.deleteBbsbyid(id,begin,end);
    }
    public void deleteBbsbyplayerid(int id,int playerid) throws DataAccessException{
        dao.deleteBbsbyplayerid(id,playerid);
    }
    //mengjie add end

    public static class BbsResult{
        public Bbs[] bbs;
        public int pageCount;
    }

    public static void setForbidenBbs(Set ids){
        forbidenbbsId = ids;
    }

    public static boolean isBbsForbiden(int bbsId){
        return forbidenbbsId.contains(bbsId);
    }
}
