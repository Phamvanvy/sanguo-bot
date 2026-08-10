package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Camp;

public class Tbl_CampDao extends BaseDao{
	private static final Logger log = Logger.getLogger(Tbl_AdminDao.class);

    public static final String SQL_PARA = "id, camp, kingid, createtime, lasttime, money, taxrate, skills, slogan, pool, valid";

    public Tbl_CampDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Camp> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Camp.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Camp.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Camp> list = new ArrayList<Tbl_Camp>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
            	Tbl_Camp camp = new Tbl_Camp();

            	camp.setId(rs.getInt(1));
                camp.setCamp(rs.getInt(2));
                camp.setKingid(rs.getInt(3));
                camp.setCreatetime(rs.getDate(4));
                camp.setLasttime(rs.getTimestamp(5));
                camp.setMoney(rs.getLong(6));
                camp.setTaxrate(rs.getInt(7));
                camp.setSkills(rs.getBytes(8));
                camp.setSlogan(rs.getString(9));
                camp.setPool(rs.getString(10));
                camp.setValid(rs.getInt(11));

                list.add(camp);
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
