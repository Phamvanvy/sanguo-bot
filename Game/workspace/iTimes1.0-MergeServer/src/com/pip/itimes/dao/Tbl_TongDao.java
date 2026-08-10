package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Tong;

public class Tbl_TongDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_TongDao.class);

    public static final String SQL_PARA = "id, tongname, createtime, owner, slogan, level, money, resource, health, lastrepairtime, memebercount, credit, toplisthot, toplistonline, leastcredit";

    public Tbl_TongDao(ServerConfig config){
        super(config);
    }

    public ArrayList<String> getAllNames(){
        String query = buildQuery("select tongname from " + Tbl_Tong.class.getSimpleName().toLowerCase());
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

    public ArrayList<Tbl_Tong> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Tong.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Tong.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Tong> list = new ArrayList<Tbl_Tong>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Tong tong = new Tbl_Tong();

                tong.setId(rs.getInt(1));
                tong.setTongname(rs.getString(2));
                tong.setCreatetime(rs.getTimestamp(3));
                tong.setOwner(rs.getInt(4));
                tong.setSlogan(rs.getString(5));
                tong.setLevel(rs.getInt(6));
                tong.setMoney(rs.getInt(7));
                tong.setResource(rs.getInt(8));
                tong.setHealth(rs.getInt(9));
                tong.setLastrepairtime(rs.getTimestamp(10));
                tong.setMemebercount(rs.getInt(11));
                tong.setCredit(rs.getInt(12));
                tong.setToplisthot(rs.getInt(13));
                tong.setToplistonline(rs.getInt(14));
                tong.setLeastcredit(rs.getInt(15));

                list.add(tong);
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
