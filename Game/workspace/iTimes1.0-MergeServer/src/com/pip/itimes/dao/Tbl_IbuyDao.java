package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Ibuy;

public class Tbl_IbuyDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_IbuyDao.class);

    public static final String SQL_PARA = "id, accountid, playerid, itemid, itemname, type, imoney, buytime, giftflag, otherplayerid, count, otherplayername, level";

    public Tbl_IbuyDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Ibuy> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Ibuy.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Ibuy.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Ibuy> list = new ArrayList<Tbl_Ibuy>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Ibuy ibuy = new Tbl_Ibuy();

                ibuy.setId(rs.getInt(1));
                ibuy.setAccountid(rs.getInt(2));
                ibuy.setPlayerid(rs.getInt(3));
                ibuy.setItemid(rs.getInt(4));
                ibuy.setItemname(rs.getString(5));
                ibuy.setType(rs.getInt(6));
                ibuy.setImoney(rs.getInt(7));
                ibuy.setBuytime(rs.getTimestamp(8));
                ibuy.setGiftflag(rs.getInt(9));
                ibuy.setOtherplayerid(rs.getInt(10));
                ibuy.setCount(rs.getInt(11));
                ibuy.setOtherplayername(rs.getString(12));
                ibuy.setLevel(rs.getInt(13));

                list.add(ibuy);
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
