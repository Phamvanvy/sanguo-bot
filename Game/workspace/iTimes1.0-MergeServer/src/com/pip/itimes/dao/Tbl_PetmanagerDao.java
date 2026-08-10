package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Petmanager;

public class Tbl_PetmanagerDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_PetmanagerDao.class);

    public static final String SQL_PARA = "id, petid, playerid, pet, stone, eattime, information";

    public Tbl_PetmanagerDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Petmanager> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Petmanager.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Petmanager.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Petmanager> list = new ArrayList<Tbl_Petmanager>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Petmanager petmanager = new Tbl_Petmanager();

                petmanager.setId(rs.getInt(1));
                petmanager.setPetid(rs.getInt(2));
                petmanager.setPlayerid(rs.getInt(3));
                petmanager.setPet(rs.getBytes(4));
                petmanager.setStone(rs.getInt(5));
                petmanager.setEattime(rs.getTimestamp(6));
                petmanager.setInformation(rs.getLong(7));

                list.add(petmanager);
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
