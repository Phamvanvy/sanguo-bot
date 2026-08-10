package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.Auction;
import java.util.List;
import com.pip.itimes.server.bean.Shop;
import org.hibernate.Query;
import java.util.Date;
import com.pip.itimes.server.bean.*;
import org.hibernate.*;
/**
 * @author Jeffrey
 * @version 1.0
 */
public class AuctionDao extends BaseDao{

    public AuctionDao() {
        super();
    }

    public Auction getAuction(int id) throws DataAccessException{
        return (Auction)getObject(Auction.class,new Integer(id));
    }

    public Auction[] getAuctionsByShopAndType(int shopId, int begin,
                                              int count) throws
            DataAccessException {
        Auction[] ret = null;
        try {
            String hql = "from Auction a where a.shopId=" + shopId;
            List l = getLimitedList(hql, begin, count);
            ret = new Auction[l.size()];
            l.toArray(ret);
        } catch (DataAccessException ex) {
            throw ex;
        }
        finally{
            closeSession();
        }
        return ret;
    }

    public Auction[] getTimeoutAuctions() throws DataAccessException{
        try {
            String hql = "from Auction a where a.validTime<:time";
            Query query = getSession().createQuery(hql);
            query.setTimestamp("time", new Date());
            List l = query.list();
            Auction[] ret = new Auction[l.size()];
            l.toArray(ret);
            return ret;
        } catch (HibernateException ex) {
            return new Auction[0];
        }
        finally{
            closeSession();
        }
    }

    public int getCount(int shopId) throws DataAccessException{
        String hql = "from Auction a where a.shopId=" + shopId;
        return getCount(hql);
    }
    
    public int getCountcredit(short areaId) throws DataAccessException{
        String hql = "from Auction a where a.areaId="+areaId;
        return getCount(hql);
    }
    
    public Auction[] getAuctions(int type, int quality, int level, short areaId, String name, int begin,
                                 int count) throws
            DataAccessException {
        String hql = "select a,case when a.currentPrice=-1 then a.startPrice else a.currentPrice end from Auction a where a.state=0 and a.areaId=" + areaId + getClause(type, quality, level, name) + " order by 2";
        List l = getLimitedList(hql, begin, count);
        Auction[] ret = new Auction[l.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (Auction) (((Object[]) l.get(i))[0]);
        }
//        l.toArray(ret);
        return ret;
    }

    public Auction[] getAuctionscredit(short areaId,int begin,int count) throws DataAccessException {
    	String hql = "select a,case when a.currentPrice=-1 then a.startPrice else a.currentPrice end from Auction a where a.state=0 and a.areaId=" + areaId + " order by 2";
    	List l = getLimitedList(hql, begin, count);
    	Auction[] ret = new Auction[l.size()];
    	for (int i = 0; i < ret.length; i++) {
    		ret[i] = (Auction) (((Object[]) l.get(i))[0]);
    	}
    	return ret;
    }
    
    public int getCount(int type, int quality, int level, short areaId,String name) throws
            DataAccessException {
        return getCount("from Auction a where a.state=0 and a.areaId="+areaId + getClause(type, quality, level,name));
    }

    private String getClause(int type, int quality, int level,String name) {
        StringBuffer buff = new StringBuffer();
        if (type != -1) {
            buff.append(" and ");
            buff.append(" a.type=" + type);
        }
        if (quality != -1) {
            if(quality==2){
                buff.append(" and ");
                buff.append(" a.quality>=" + quality);
            }else{
                buff.append(" and ");
                buff.append(" a.quality=" + quality);
            }
        }
        if (level != -1) {
            buff.append(" and ");
            int beginLevel = (level >> 16 & 0xFFFF);
            int endLevel = (level & 0xFFFF);
            if(endLevel==0){
                endLevel = 100;
            }
            buff.append(" a.level between " + beginLevel + " and " + endLevel);
        }
        if(name.length()!=0){
            buff.append(" and ");
            buff.append(" a.name like '%"+name+"%'");
        }
        return buff.toString();
    }

    public void setState(int shopId,byte state) throws DataAccessException{
        String hql = "update Auction a set a.state="+state+" where a.shopId="+shopId;
        query(hql);
    }

    public void setOwner(int shopId, int ownerId, String ownerName) throws
            DataAccessException {
        String hql = "update Auction a set a.shopId=" + shopId +
                     ",a.playerName=" + ownerName + ",a.playerId=" + ownerId +
                     ",a.state=" + Shop.STATE_NORMAL + " where a.shopId=" +
                     shopId; ;
        query(hql);
    }
}
