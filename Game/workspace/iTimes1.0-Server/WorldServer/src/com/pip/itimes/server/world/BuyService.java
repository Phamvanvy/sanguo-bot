package com.pip.itimes.server.world;

import com.pip.itimes.server.dao.BuyDao;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.stage.ShopData;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.bean.Buy;
import java.util.Date;
import com.pip.itimes.server.dao.*;
import com.pip.itimes.server.stage.MaterialTypes;
import com.pip.itimes.server.stage.MaterialType;
import com.pip.itimes.server.stage.IItemTemplate;

/**
 * @author Jeffery
 * @version 1.0
 */
public class BuyService {

    private BuyDao dao;

    public BuyService(BuyDao dao) {
        this.dao = dao;
    }

    public void addBuy(ShopData shop, IItemTemplate item, int count,
                       int price,byte quality) throws BuyException{
        Buy buy = new Buy();
        buy.setItemId(item.getItemId());
        buy.setCreateTime(new Date());
        buy.setCurrent(0);
        buy.setPrice(price);
        buy.setShopId(shop.getId());
        buy.setTotal(count);
        buy.setName(item.getName());
        buy.setAreaId((short)shop.getAreaId());
        MaterialType type = MaterialTypes.getMaterialType(item.getItemId());
        buy.setType(type.getType());
        buy.setQuality(quality);
        try {
            dao.makePersistent(buy);
        } catch (DataAccessException ex) {
            throw new BuyException("添加收购物品错误");
        }
    }

    public Buy getBuy(int id){
        try {
            return (Buy) dao.getObject(Buy.class, new Integer(id));
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public boolean removeBuy(Buy buy){
        try {
            dao.makeTransient(buy);
            return true;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    public void saveBuy(Buy buy){
        try {
            dao.makePersistent(buy);
        } catch (DataAccessException ex) {
        }
    }

    public void setState(int shopId,byte state){
        try {
            dao.setState(shopId, state);
        } catch (DataAccessException ex) {
        }
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
