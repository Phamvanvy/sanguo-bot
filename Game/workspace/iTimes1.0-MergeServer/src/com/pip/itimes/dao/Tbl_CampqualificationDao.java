package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Campqualification;

public class Tbl_CampqualificationDao extends BaseDao{
	private static final Logger log = Logger.getLogger(Tbl_BbsDao.class);

    public static final String SQL_PARA = "id, playerid, createtime, lasttime, camp, total, added, addcount, remain, level, valid";

    public Tbl_CampqualificationDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Campqualification> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Campqualification.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Campqualification.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Campqualification> list = new ArrayList<Tbl_Campqualification>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
            	Tbl_Campqualification campcandidate = new Tbl_Campqualification();

                campcandidate.setId(rs.getInt(1));
                campcandidate.setPlayerid(rs.getInt(2));
                campcandidate.setCreatetime(rs.getTimestamp(3));
                campcandidate.setLasttime(rs.getTimestamp(4));
                campcandidate.setCamp(rs.getInt(5));
                campcandidate.setTotal(rs.getInt(6));
                campcandidate.setAdded(rs.getInt(7));
                campcandidate.setAddcount(rs.getInt(8));
                campcandidate.setRemain(rs.getInt(9));
                campcandidate.setLevel(rs.getInt(10));
                campcandidate.setValid(rs.getInt(11));

                list.add(campcandidate);
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
