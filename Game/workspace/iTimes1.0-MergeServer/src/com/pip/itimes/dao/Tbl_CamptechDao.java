package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Camptech;

public class Tbl_CamptechDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_CamptechDao.class);

    public static final String SQL_PARA = "id, kingid, kingname, camp, credit, level, moeny, percent, campmoeny, integral, technology, valid";

    public Tbl_CamptechDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Camptech> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Camptech.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Camptech.class.getSimpleName().toLowerCase() + " where valid = '1' order by id limit " + begin + ", 1) and valid = '1' order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Camptech> list = new ArrayList<Tbl_Camptech>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Camptech camptech = new Tbl_Camptech();

                camptech.setId(rs.getInt(1));
                camptech.setKingid(rs.getInt(2));
                camptech.setKingname(rs.getString(3));
                camptech.setCamp(rs.getInt(4));
                camptech.setCredit(rs.getInt(5));
                camptech.setLevel(rs.getInt(6));
                camptech.setMoeny(rs.getLong(7));
                camptech.setPercent(rs.getInt(8));
                camptech.setCampmoeny(rs.getInt(9));
                camptech.setIntegral(rs.getInt(10));
                camptech.setTechnology(rs.getBytes(11));
                camptech.setValid(rs.getInt(12));

                list.add(camptech);
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
