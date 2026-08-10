package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Leavemessage;

public class Tbl_LeavemessageDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_LeavemessageDao.class);

    public static final String SQL_PARA = "id, sourceid, sourcename, title, content, ownerid, createtime";

    public Tbl_LeavemessageDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Leavemessage> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Leavemessage.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Leavemessage.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Leavemessage> list = new ArrayList<Tbl_Leavemessage>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Leavemessage leavemessage = new Tbl_Leavemessage();

                leavemessage.setId(rs.getInt(1));
                leavemessage.setSourceid(rs.getInt(2));
                leavemessage.setSourcename(rs.getString(3));
                leavemessage.setTitle(rs.getString(4));
                leavemessage.setContent(rs.getString(5));
                leavemessage.setOwnerid(rs.getInt(6));
                leavemessage.setCreatetime(rs.getTimestamp(7));

                list.add(leavemessage);
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
