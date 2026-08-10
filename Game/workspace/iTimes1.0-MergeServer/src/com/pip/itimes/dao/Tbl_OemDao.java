package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Oem;

public class Tbl_OemDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_OemDao.class);

    public static final String SQL_PARA = "id, shopid, itemid, total, current, pay, workpoint, createtime, name, type, areaid, state, quality";

    public Tbl_OemDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Oem> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Oem.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Oem.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Oem> list = new ArrayList<Tbl_Oem>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Oem oem = new Tbl_Oem();

                oem.setId(rs.getInt(1));
                oem.setShopid(rs.getInt(2));
                oem.setItemid(rs.getInt(3));
                oem.setTotal(rs.getInt(4));
                oem.setCurrent(rs.getInt(5));
                oem.setPay(rs.getInt(6));
                oem.setWorkpoint(rs.getInt(7));
                oem.setCreatetime(rs.getTimestamp(8));
                oem.setName(rs.getString(9));
                oem.setType(rs.getInt(10));
                oem.setAreaid(rs.getInt(11));
                oem.setState(rs.getInt(12));
                oem.setQuality(rs.getInt(13));

                list.add(oem);
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
