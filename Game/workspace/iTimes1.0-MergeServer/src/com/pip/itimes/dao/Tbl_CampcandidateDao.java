package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Campcandidate;

public class Tbl_CampcandidateDao extends BaseDao{

	private static final Logger log = Logger.getLogger(Tbl_BbsDao.class);

    public static final String SQL_PARA = "id, playerid, createtime, lasttime, camp, preking, totalvote, normalvote, itemvote, ishopitemvote, magicvote, magicremain, eggvote, slogan, valid";

    public Tbl_CampcandidateDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Campcandidate> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Campcandidate.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Campcandidate.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Campcandidate> list = new ArrayList<Tbl_Campcandidate>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
            	Tbl_Campcandidate campcandidate = new Tbl_Campcandidate();

                campcandidate.setId(rs.getInt(1));
                campcandidate.setPlayerid(rs.getInt(2));
                campcandidate.setCreatetime(rs.getTimestamp(3));
                campcandidate.setLasttime(rs.getTimestamp(4));
                campcandidate.setCamp(rs.getInt(5));
                campcandidate.setPreking(rs.getInt(6));
                campcandidate.setTotalvote(rs.getInt(7));
                campcandidate.setNormalvote(rs.getInt(8));
                campcandidate.setItemvote(rs.getInt(9));
                campcandidate.setIshopitemvote(rs.getInt(10));
                campcandidate.setMagicvote(rs.getInt(11));
                campcandidate.setMagicremain(rs.getInt(12));
                campcandidate.setEggvote(rs.getInt(13));
                campcandidate.setSlogan(rs.getString(14));
                campcandidate.setValid(rs.getInt(15));

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
