package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Shop;

public class Tbl_ShopDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_ShopDao.class);

    public static final String SQL_PARA = "id, name, money, playerid, level, createtime, items, areaid, gridsize, state, buyplayerid, price, selltime, leveluptime";

    public Tbl_ShopDao(ServerConfig config){
        super(config);
    }

    public ArrayList<String> getAllNames(){
        String query = buildQuery("select name from " + Tbl_Shop.class.getSimpleName().toLowerCase());
        Statement statement = getStatement();
        ArrayList<String> list = new ArrayList<String>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                list.add(rs.getString(1));
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
    
    public ArrayList<Tbl_Shop> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Shop.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Shop.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Shop> list = new ArrayList<Tbl_Shop>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Shop shop = new Tbl_Shop();

                shop.setId(rs.getInt(1));
                shop.setName(rs.getString(2));
                shop.setMoney(rs.getInt(3));
                shop.setPlayerid(rs.getInt(4));
                shop.setLevel(rs.getInt(5));
                shop.setCreatetime(rs.getTimestamp(6));
                shop.setItems(rs.getBytes(7));
                shop.setAreaid(rs.getInt(8));
                shop.setGridsize(rs.getInt(9));
                shop.setState(rs.getInt(10));
                shop.setBuyplayerid(rs.getInt(11));
                shop.setPrice(rs.getInt(12));
                shop.setSelltime(rs.getTimestamp(13));
                shop.setLeveluptime(rs.getTimestamp(14));

                list.add(shop);
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
