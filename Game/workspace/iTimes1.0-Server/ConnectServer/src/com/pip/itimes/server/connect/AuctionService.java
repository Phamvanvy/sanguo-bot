package com.pip.itimes.server.connect;

import com.pip.itimes.server.dao.AuctionDao;
import com.pip.itimes.server.bean.Auction;
import com.pip.itimes.server.dao.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class AuctionService {

    private AuctionDao dao;

    public AuctionService(AuctionDao dao) {
        this.dao = dao;
    }

    public Auction[] getAuctions(int shopId, byte type, int begin, int count) throws
            AuctionException {
        try {
            return dao.getAuctionsByShopAndType(shopId, begin, count);
        } catch (DataAccessException ex) {
            throw new AuctionException("查询出售列表出错");
        }
    }

    public int getCount(int shopId) throws AuctionException{
        try {
            return dao.getCount(shopId);
        } catch (DataAccessException ex) {
            throw new AuctionException("查询商铺错误");
        }
    }
}
