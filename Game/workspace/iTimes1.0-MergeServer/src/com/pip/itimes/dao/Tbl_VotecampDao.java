package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Votecamp;

public class Tbl_VotecampDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_VotecampDao.class);

    public static final String SQL_PARA = "id, playerid, playername, camp, credit, creditoffer, leve, moeny, fristtime, endtime, title, ticket, itemcount, kingflag, valid, itemcounttotal";

    public Tbl_VotecampDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Votecamp> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Votecamp.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Votecamp.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Votecamp> list = new ArrayList<Tbl_Votecamp>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Votecamp votecamp = new Tbl_Votecamp();

                votecamp.setId(rs.getInt(1));
                votecamp.setPlayerid(rs.getInt(2));
                votecamp.setPlayername(rs.getString(3));
                votecamp.setCamp(rs.getInt(4));
                votecamp.setCredit(rs.getInt(5));
                votecamp.setCreditoffer(rs.getInt(6));
                votecamp.setLeve(rs.getInt(7));
                votecamp.setMoeny(rs.getInt(8));
                votecamp.setFristtime(rs.getTimestamp(9));
                votecamp.setEndtime(rs.getTimestamp(10));
                votecamp.setTitle(rs.getString(11));
                votecamp.setTicket(rs.getInt(12));
                votecamp.setItemcount(rs.getInt(13));
                votecamp.setKingflag(rs.getInt(14));
                votecamp.setValid(rs.getInt(15));
                votecamp.setItemcounttotal(rs.getInt(16));

                list.add(votecamp);
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
