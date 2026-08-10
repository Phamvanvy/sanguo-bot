package com.pip.itimes.server.world;

import com.pip.itimes.server.dao.OemDao;
import com.pip.itimes.server.stage.ShopData;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.bean.Oem;
import java.util.Date;
import com.pip.itimes.server.dao.*;
import com.pip.itimes.server.stage.Recipe;

/**
 * @author Jeffery
 * @version 1.0
 */
public class OemService {

    private OemDao dao;

    public OemService(OemDao dao) {
        this.dao = dao;
    }

    public void addOem(ShopData shop, Recipe recipe, int count, int pay
                       ,byte quality) throws OemException {
        Oem oem = new Oem();
        oem.setCreateTime(new Date());
        oem.setCurrent(0);
        oem.setItemId(recipe.getId());
        oem.setPay(pay);
        oem.setShopId(shop.getId());
        oem.setTotal(count);
        oem.setWorkPoint(recipe.getProductivity());
        oem.setType(recipe.getType());
        oem.setName(recipe.getName());
        oem.setAreaId((short)shop.getAreaId());
        oem.setQuality(quality);
        try {
            dao.makePersistent(oem);
        } catch (DataAccessException ex) {
            throw new OemException("添加求做列表错误");
        }
    }

    public Oem getOem(int id){
        try {
            return (Oem) dao.getObject(Oem.class, new Integer(id));
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public boolean removeOem(Oem oem){
        try {
            dao.makeTransient(oem);
            return true;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    public void saveOem(Oem oem){
        try {
            dao.makePersistent(oem);
        } catch (DataAccessException ex) {
        }
    }

    public void setState(int shopId,byte state){
        try {
            dao.setState(shopId, state);
        } catch (DataAccessException ex) {
        }
    }
    public int getCount(short areaId,byte type,String name) throws OemException{
        try {
            return dao.getCount(areaId, type, name);
        } catch (DataAccessException ex) {
            throw new OemException("查询错误");
        }
    }

    public Oem[] getOems(short areaId, byte type, String name, int begin,
                         int count) throws
            OemException {
        try {
            return  dao.getOems(areaId, type, name, begin, count);
        } catch (DataAccessException ex) {
            throw new OemException("查询求做列表错误");
        }
    }


    public int getCount(int shopId) throws OemException{
        try {
            return dao.getCount(shopId);
        } catch (DataAccessException ex) {
            throw new OemException("查询错误");
        }
    }

    public Oem[] getOems(int shopId,int begin,int count) throws OemException{
        try {
            return dao.getOems(shopId, begin, count);
        } catch (DataAccessException ex) {
            throw new OemException("查询求做列表错误");
        }
    }

//    public int getCount(short areaId,byte type,String name) throws OemException{
//        try {
//            return dao.getCount(areaId, type, name);
//        } catch (DataAccessException ex) {
//            throw new OemException("查询错误");
//        }
//    }
//
//    public Oem[] getOems(short areaId, byte type, String name, int begin,
//                         int count) throws
//            OemException {
//        try {
//            return  dao.getOems(areaId, type, name, begin, count);
//        } catch (DataAccessException ex) {
//            throw new OemException("查询求做列表错误");
//        }
//    }
}
