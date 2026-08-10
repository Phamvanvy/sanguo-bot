package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Tongisland;

public class Tbl_TongislandDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_TongislandDao.class);

    public static final String SQL_PARA = "id, tongid, begintime, endtime";

    public Tbl_TongislandDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Tongisland> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Tongisland.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Tongisland.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Tongisland> list = new ArrayList<Tbl_Tongisland>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Tongisland tongisland = new Tbl_Tongisland();

                tongisland.setId(rs.getInt(1));
                tongisland.setTongid(rs.getInt(2));
                tongisland.setBegintime(rs.getTimestamp(3));
                tongisland.setEndtime(rs.getTimestamp(4));

                list.add(tongisland);
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
