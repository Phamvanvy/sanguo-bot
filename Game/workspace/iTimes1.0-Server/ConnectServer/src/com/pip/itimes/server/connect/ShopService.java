package com.pip.itimes.server.connect;

import com.pip.itimes.server.bean.Shop;
import com.pip.itimes.server.dao.ShopDao;
import com.pip.itimes.server.dao.*;

/**
 * @author Jeffery
 * @version 1.0
 */
public class ShopService {

    private ShopDao dao;

    public ShopService(ShopDao dao) {
        this.dao = dao;
    }

    public Shop getShop(int shopId){
        try {
            return (Shop) dao.getObject(Shop.class, new Integer(shopId));
        } catch (DataAccessException ex) {
            return null;
        }
    }
}
