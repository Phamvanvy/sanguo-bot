package com.pip.itimes.server.connect;

import com.pip.itimes.server.dao.BuyDao;
import com.pip.itimes.server.dao.*;
import com.pip.itimes.server.bean.Buy;

/**
 * @author Jeffery
 * @version 1.0
 */
public class BuyService {

    private BuyDao dao;

    public BuyService(BuyDao dao) {
        this.dao = dao;
    }

    public int getCount(int shopId) throws BuyException{
        try {
            return dao.getCount(shopId);
        } catch (DataAccessException ex) {
            throw new BuyException("查询错误");
        }
    }

    public Buy[] getBuys(int shopId,int begin,int count) throws BuyException{
        try {
            return dao.getBuys(shopId, begin, count);
        } catch (DataAccessException ex) {
            throw new BuyException("查询收购列表出错");
        }
    }

    public Buy[] getBuys(short areaId,byte type,String name,int begin,int count) throws BuyException{
        try {
            return dao.getBuys(areaId, type, name, begin, count);
        } catch (DataAccessException ex) {
            throw new BuyException("查询收购列表出错");
        }
    }

    public int getCount(short areaId,byte type,String name) throws BuyException{
        try {
            return dao.getCount(areaId, type, name);
        } catch (DataAccessException ex) {
            throw new BuyException("查询错误");
        }
    }
}
