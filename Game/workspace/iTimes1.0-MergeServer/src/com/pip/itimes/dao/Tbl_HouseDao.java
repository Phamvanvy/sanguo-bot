package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_House;

public class Tbl_HouseDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_HouseDao.class);

    public static final String SQL_PARA = "id, playerid, playername, createtime, level, style, rule, areaid, gridsize, items, parts, lasttime, title, waiterid, visitedtimes, usedimoney, leavemessagetimes, canusewaitertime, autobuywaiter, addgridsize";

    public Tbl_HouseDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_House> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_House.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_House.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_House> list = new ArrayList<Tbl_House>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_House house = new Tbl_House();

                house.setId(rs.getInt(1));
                house.setPlayerid(rs.getInt(2));
                house.setPlayername(rs.getString(3));
                house.setCreatetime(rs.getTimestamp(4));
                house.setLevel(rs.getInt(5));
                house.setStyle(rs.getInt(6));
                house.setRule(rs.getInt(7));
                house.setAreaid(rs.getInt(8));
                house.setGridsize(rs.getInt(9));
                house.setItems(rs.getBytes(10));
                house.setParts(rs.getBytes(11));
                house.setLasttime(rs.getTimestamp(12));
                house.setTitle(rs.getString(13));
                house.setWaiterid(rs.getInt(14));
                house.setVisitedtimes(rs.getInt(15));
                house.setUsedimoney(rs.getInt(16));
                house.setLeavemessagetimes(rs.getInt(17));
                house.setCanusewaitertime(rs.getTimestamp(18));
                house.setAutobuywaiter(rs.getInt(19));
                house.setAddgridsize(rs.getInt(20));

                list.add(house);
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
