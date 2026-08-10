package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Bbs;

public class Tbl_BbsDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_BbsDao.class);

    public static final String SQL_PARA = "id, bbsid, playerid, playername, title, content, posttime, priority";

    public Tbl_BbsDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Bbs> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Bbs.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Bbs.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Bbs> list = new ArrayList<Tbl_Bbs>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Bbs bbs = new Tbl_Bbs();

                bbs.setId(rs.getInt(1));
                bbs.setBbsid(rs.getInt(2));
                bbs.setPlayerid(rs.getInt(3));
                bbs.setPlayername(rs.getString(4));
                bbs.setTitle(rs.getString(5));
                bbs.setContent(rs.getString(6));
                bbs.setPosttime(rs.getTimestamp(7));
                bbs.setPriority(rs.getInt(8));

                list.add(bbs);
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
