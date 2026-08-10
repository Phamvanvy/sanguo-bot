package com.pip.itimes.server.dao;

import com.pip.itimes.server.bean.Shop;
import java.util.List;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ShopDao extends BaseDao{
    public ShopDao() {
        super();
    }

    public void addShop(Shop shop) throws DataAccessException{
        makePersistent(shop);
    }

    public Shop[] getShops(int playerId,short areaId) throws DataAccessException{
        List l = getList("from Shop p where p.playerId="+playerId+" and p.areaId="+areaId);
        Shop[] ret = new Shop[l.size()];
        l.toArray(ret);
        return ret;
    }

    public int[] getShopIds(int playerId,short areaId) throws DataAccessException{
        List l = getList("select p.id from Shop p where p.playerId="+playerId+" and p.areaId="+areaId);
        int[] ret = new int[l.size()];
        for(int i=0;i<l.size();i++){
            ret[i] = ((Integer)l.get(i)).intValue();
        }
        return ret;
    }

    public int[] getShopIds(int playerId) throws DataAccessException{
        List l = getList("select p.id from Shop p where p.playerId="+playerId);
        int[] ret = new int[l.size()];
        for(int i=0;i<l.size();i++){
            ret[i] = ((Integer)l.get(i)).intValue();
        }
        return ret;
    }

    public Shop[] getSellShops(int playerId, short areaId) throws
            DataAccessException {
        List l = getList("from Shop p where p.buyPlayerId=" + playerId +
                         " and p.areaId=" + areaId);
        Shop[] ret = new Shop[l.size()];
        l.toArray(ret);
        return ret;
    }

    public boolean hasShop(String shopName) throws DataAccessException{
        return getCount("from Shop p where p.name="+"'"+shopName+"'")>0;
    }

    public int getShopCount(int playerId) throws DataAccessException{
        return super.getCount("from Shop p where p.playerId=" + playerId);
    }

    public Shop[] getShops() throws DataAccessException{
        List l = getList("from Shop");
        Shop[] ret = new Shop[l.size()];
        l.toArray(ret);
        return ret;
    }
}
