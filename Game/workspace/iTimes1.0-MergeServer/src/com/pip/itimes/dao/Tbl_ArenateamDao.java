package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Arenateam;

public class Tbl_ArenateamDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_ArenateamDao.class);

    public static final String SQL_PARA = "id, type, arenaname, createtime, owner, slogan, arenalevel, lastrepairtime, memebercount, valid";

    public Tbl_ArenateamDao(ServerConfig config){
        super(config);
    }

    public ArrayList<String> getAllNames(){
        String query = buildQuery("select arenaname from " + Tbl_Arenateam.class.getSimpleName().toLowerCase() + " where valid = \'1\'");
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

    public ArrayList<Tbl_Arenateam> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Arenateam.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Arenateam.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Arenateam> list = new ArrayList<Tbl_Arenateam>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Arenateam arenateam = new Tbl_Arenateam();

                arenateam.setId(rs.getInt(1));
                arenateam.setType(rs.getInt(2));
                arenateam.setArenaname(rs.getString(3));
                arenateam.setCreatetime(rs.getTimestamp(4));
                arenateam.setOwner(rs.getInt(5));
                arenateam.setSlogan(rs.getString(6));
                arenateam.setArenalevel(rs.getInt(7));
                arenateam.setLastrepairtime(rs.getTimestamp(8));
                arenateam.setMemebercount(rs.getInt(9));
                arenateam.setValid(rs.getInt(10));

                list.add(arenateam);
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
