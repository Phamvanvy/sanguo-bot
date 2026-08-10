package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Buy;

public class Tbl_BuyDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_BuyDao.class);

    public static final String SQL_PARA = "id, shopid, itemid, total, current, price, createtime, name, type, areaid, state, quality";

    public Tbl_BuyDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Buy> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Buy.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Buy.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Buy> list = new ArrayList<Tbl_Buy>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Buy buy = new Tbl_Buy();

                buy.setId(rs.getInt(1));
                buy.setShopid(rs.getInt(2));
                buy.setItemid(rs.getInt(3));
                buy.setTotal(rs.getInt(4));
                buy.setCurrent(rs.getInt(5));
                buy.setPrice(rs.getInt(6));
                buy.setCreatetime(rs.getTimestamp(7));
                buy.setName(rs.getString(8));
                buy.setType(rs.getInt(9));
                buy.setAreaid(rs.getInt(10));
                buy.setState(rs.getInt(11));
                buy.setQuality(rs.getInt(12));

                list.add(buy);
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
