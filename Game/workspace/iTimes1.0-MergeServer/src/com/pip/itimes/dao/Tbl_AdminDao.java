package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Admin;

public class Tbl_AdminDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_AdminDao.class);

    public static final String SQL_PARA = "id, name, password, auth";

    public Tbl_AdminDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Admin> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Admin.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Admin.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Admin> list = new ArrayList<Tbl_Admin>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Admin admin = new Tbl_Admin();

                admin.setId(rs.getInt(1));
                admin.setName(rs.getString(2));
                admin.setPassword(rs.getString(3));
                admin.setAuth(rs.getString(4));

                list.add(admin);
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
