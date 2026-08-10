package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Vote;

public class Tbl_VoteDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_VoteDao.class);

    public static final String SQL_PARA = "id, votersid, playeridvoters, createtime, votepoint, type, valid, isimoneyitem";

    public Tbl_VoteDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Vote> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Vote.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Vote.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Vote> list = new ArrayList<Tbl_Vote>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Vote vote = new Tbl_Vote();

                vote.setId(rs.getInt(1));
                vote.setVotersid(rs.getInt(2));
                vote.setPlayeridvoters(rs.getInt(3));
                vote.setCreatetime(rs.getTimestamp(4));
                vote.setVotepoint(rs.getInt(5));
                vote.setType(rs.getInt(6));
                vote.setValid(rs.getInt(7));
                vote.setIsimoneyitem(rs.getInt(8));

                list.add(vote);
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
