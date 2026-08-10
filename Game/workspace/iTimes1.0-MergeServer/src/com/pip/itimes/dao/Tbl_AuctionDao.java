package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Auction;

public class Tbl_AuctionDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_AuctionDao.class);

    public static final String SQL_PARA = "id, playerid, shopid, createtime, startprice, currentprice, endprice, item, name, type, lastplayerid, playername, quality, level, areaid, state, validtime";

    public Tbl_AuctionDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Auction> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Auction.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Auction.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Auction> list = new ArrayList<Tbl_Auction>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Auction auction = new Tbl_Auction();

                auction.setId(rs.getInt(1));
                auction.setPlayerid(rs.getInt(2));
                auction.setShopid(rs.getInt(3));
                auction.setCreatetime(rs.getTimestamp(4));
                auction.setStartprice(rs.getInt(5));
                auction.setCurrentprice(rs.getInt(6));
                auction.setEndprice(rs.getInt(7));
                auction.setItem(rs.getBytes(8));
                auction.setName(rs.getString(9));
                auction.setType(rs.getInt(10));
                auction.setLastplayerid(rs.getInt(11));
                auction.setPlayername(rs.getString(12));
                auction.setQuality(rs.getInt(13));
                auction.setLevel(rs.getInt(14));
                auction.setAreaid(rs.getInt(15));
                auction.setState(rs.getInt(16));
                auction.setValidtime(rs.getTimestamp(17));

                list.add(auction);
            }
        }catch(Exception e){
            log.error(e, e);
        }finally{
            try{
                statement.close();

                if(rs != null){
                    rs.close();
                }
            }catch(Exception e){
            }
        }

        return list;
    }
}
