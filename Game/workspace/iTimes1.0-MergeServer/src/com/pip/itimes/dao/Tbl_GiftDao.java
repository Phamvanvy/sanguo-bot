package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Gift;

public class Tbl_GiftDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_GiftDao.class);

    public static final String SQL_PARA = "id, groupid, playerid, createtime, modifytime, rcount, count";

    public Tbl_GiftDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Gift> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Gift.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Gift.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Gift> list = new ArrayList<Tbl_Gift>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Gift gift = new Tbl_Gift();

                gift.setId(rs.getInt(1));
                gift.setGroupid(rs.getInt(2));
                gift.setPlayerid(rs.getInt(3));
                gift.setCreatetime(rs.getTimestamp(4));
                gift.setModifytime(rs.getTimestamp(5));
                gift.setRcount(rs.getInt(6));
                gift.setCount(rs.getInt(7));

                list.add(gift);
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
