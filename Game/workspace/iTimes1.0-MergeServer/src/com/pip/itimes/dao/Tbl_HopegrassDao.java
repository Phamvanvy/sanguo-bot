package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Hopegrass;

public class Tbl_HopegrassDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_HopegrassDao.class);

    public static final String SQL_PARA = "id, playerid, mapid, x, y, itemgroupid, createtime, validtime, obsoletetime, grasstype, ratio, grouprnd";

    public Tbl_HopegrassDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Hopegrass> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Hopegrass.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Hopegrass.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Hopegrass> list = new ArrayList<Tbl_Hopegrass>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Hopegrass hopegrass = new Tbl_Hopegrass();

                hopegrass.setId(rs.getInt(1));
                hopegrass.setPlayerid(rs.getInt(2));
                hopegrass.setMapid(rs.getInt(3));
                hopegrass.setX(rs.getInt(4));
                hopegrass.setY(rs.getInt(5));
                hopegrass.setItemgroupid(rs.getInt(6));
                hopegrass.setCreatetime(rs.getTimestamp(7));
                hopegrass.setValidtime(rs.getTimestamp(8));
                hopegrass.setObsoletetime(rs.getTimestamp(9));
                hopegrass.setGrasstype(rs.getInt(9));
                hopegrass.setRatio(rs.getInt(10));
                hopegrass.setGrouprnd(rs.getInt(11));

                list.add(hopegrass);
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
