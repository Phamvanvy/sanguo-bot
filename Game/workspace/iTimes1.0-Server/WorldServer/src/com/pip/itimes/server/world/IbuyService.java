package com.pip.itimes.server.world;

import com.pip.itimes.server.bean.Ibuy;

import com.pip.itimes.server.dao.*;

/**
 * @author sky
 * @version 1.0
 */
public class IbuyService {

    private IbuyDao dao;

    public static final int ibuytype_shop = 1;
    public static final int ibuytype_face = 2;
    public static final int ibuytype_house = 3;
    public static final int ibuytype_housestyle = 4;
    public static final int ibuytype_part = 5;
    public static final int ibuytype_waiter = 6;
    public static final int ibuytype_imoney_card = 7;
    
    public IbuyService(IbuyDao dao) {
        this.dao = dao;
    }

    public void addIbuy(Ibuy ibuy) throws BuyException{
        try {
            dao.makePersistent(ibuy);
        } catch (DataAccessException ex) {
            throw new BuyException("Ìí¼Ó¹ºÂòi±ÒÉÌÆ·¼ÇÂ¼´íÎó");
        }
    }

    public Ibuy[] getIbuys(int playerId,int accountId){
        try {
            return (Ibuy[]) dao.getItmes(playerId, accountId);
        } catch (DataAccessException ex) {
            return null;
        }
    }
    public int selectmonthibuy(int playerId,int accountId){
        try {
            return dao.getmonthibuy(playerId, accountId);
        } catch (DataAccessException ex) {
            return 0;
        }
    }
}
