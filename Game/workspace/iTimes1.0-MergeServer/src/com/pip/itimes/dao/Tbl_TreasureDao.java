package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Treasure;

public class Tbl_TreasureDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_TreasureDao.class);

    public static final String SQL_PARA = "id, playerid, x, y, mapid, itemgroupid, createtime, keyitemid";

    public Tbl_TreasureDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Treasure> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Treasure.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Treasure.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Treasure> list = new ArrayList<Tbl_Treasure>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Treasure treasure = new Tbl_Treasure();

                treasure.setId(rs.getInt(1));
                treasure.setPlayerid(rs.getInt(2));
                treasure.setX(rs.getInt(3));
                treasure.setY(rs.getInt(4));
                treasure.setMapid(rs.getInt(5));
                treasure.setItemgroupid(rs.getInt(6));
                treasure.setCreatetime(rs.getTimestamp(7));
                treasure.setKeyitemid(rs.getInt(8));

                list.add(treasure);
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
