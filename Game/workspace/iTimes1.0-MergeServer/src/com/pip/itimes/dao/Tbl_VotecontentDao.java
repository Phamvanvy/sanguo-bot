package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Votecontent;

public class Tbl_VotecontentDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_VotecontentDao.class);

    public static final String SQL_PARA = "id, votersid, createtime, type, valid, content";

    public Tbl_VotecontentDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Votecontent> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Votecontent.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Votecontent.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Votecontent> list = new ArrayList<Tbl_Votecontent>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Votecontent votecontent = new Tbl_Votecontent();

                votecontent.setId(rs.getInt(1));
                votecontent.setVotersid(rs.getInt(2));
                votecontent.setCreatetime(rs.getTimestamp(3));
                votecontent.setType(rs.getInt(4));
                votecontent.setValid(rs.getInt(5));
                votecontent.setContent(rs.getString(6));

                list.add(votecontent);
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
