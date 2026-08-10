package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Id;

public class Tbl_IdDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_IdDao.class);

    public static final String SQL_PARA = "usedid, id";

    public Tbl_IdDao(ServerConfig config){
        super(config);
    }

    public int getCurrentEquipmentId(){
        return getCurrentId(1);
    }

    public int getCurrentPetId(){
        return getCurrentId(2);
    }

    private int getCurrentId(int type){
        String query = buildQuery("select usedid from tbl_id where id = " + type);
        Statement statement = getStatement();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                return rs.getInt(1);
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

        return 0;
    }

    public ArrayList<Tbl_Id> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Id.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Id.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Id> list = new ArrayList<Tbl_Id>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Id id = new Tbl_Id();

                id.setUsedid(rs.getInt(1));
                id.setId(rs.getInt(2));

                list.add(id);
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
