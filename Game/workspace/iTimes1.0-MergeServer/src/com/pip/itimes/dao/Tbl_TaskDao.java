package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Task;

public class Tbl_TaskDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_TaskDao.class);

    public static final String SQL_PARA = "id, current, finished, savedata";

    public Tbl_TaskDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Task> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Task.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Task.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Task> list = new ArrayList<Tbl_Task>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Task task = new Tbl_Task();

                task.setId(rs.getInt(1));
                task.setCurrent(rs.getBytes(2));
                task.setFinished(rs.getBytes(3));
                task.setSavedata(rs.getBytes(4));

                list.add(task);
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
