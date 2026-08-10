package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Master;

public class Tbl_MasterDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_MasterDao.class);

    public static final String SQL_PARA = "id, masterid, mastername, prenticeid, prenticename, beginlevel, state, intimacy, fame";

    public Tbl_MasterDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Master> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Master.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Master.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Master> list = new ArrayList<Tbl_Master>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Master master = new Tbl_Master();

                master.setId(rs.getInt(1));
                master.setMasterid(rs.getInt(2));
                master.setMastername(rs.getString(3));
                master.setPrenticeid(rs.getInt(4));
                master.setPrenticename(rs.getString(5));
                master.setBeginlevel(rs.getInt(6));
                master.setState(rs.getInt(7));
                master.setIntimacy(rs.getInt(8));
                master.setFame(rs.getInt(9));

                list.add(master);
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
