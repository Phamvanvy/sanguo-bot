package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Mate;

public class Tbl_MateDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_MateDao.class);

    public static final String SQL_PARA = "id, husbandid, husbandname, wifeid, wifename, createtime";

    public Tbl_MateDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Mate> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Mate.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Mate.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Mate> list = new ArrayList<Tbl_Mate>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Mate mate = new Tbl_Mate();

                mate.setId(rs.getInt(1));
                mate.setHusbandid(rs.getInt(2));
                mate.setHusbandname(rs.getString(3));
                mate.setWifeid(rs.getInt(4));
                mate.setWifename(rs.getString(5));
                mate.setCreatetime(rs.getTimestamp(6));

                list.add(mate);
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
