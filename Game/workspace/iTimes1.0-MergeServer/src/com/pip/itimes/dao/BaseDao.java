package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;

public abstract class BaseDao{
    private static final Logger log = Logger.getLogger(BaseDao.class);

    private ServerConfig config;

    public BaseDao(ServerConfig config){
        this.config = config;
    }

    public abstract List getList(int begin, int count);

    public int getMaxId(String tableName){
        String query = buildQuery("select max(id) from " + tableName);
        Statement statement = getStatement();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                return rs.getInt(1);
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

        return 0;
    }

    public int getRecordCount(String tableName){
        String query = buildQuery("select count(*) from " + tableName);
        Statement statement = getStatement();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                return rs.getInt(1);
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

        return 0;
    }

    public void commit(){
        if(config.getConnection() != null){
            try{
                config.getConnection().commit();
            }catch(Exception e){
                log.error(e, e);
            }
        }
    }

    public void rollback(){
        if(config.getConnection() != null){
            try{
                config.getConnection().rollback();
            }catch(Exception e){
                log.error(e, e);
            }
        }
    }

    public Statement getStatement(){
        try{
            return config.getConnection().createStatement();
        }catch(Exception e){
            log.error("JDBC Statement Created error", e);
        }

        return null;
    }

    public String buildQuery(String sql, Object... values){
        StringBuffer sb = new StringBuffer();

        char[] ca = sql.toCharArray();
        int vidx = 0;

        for(char c : ca){
            if(c == '?'){
                if(values[vidx] instanceof String){
                    sb.append('\'');
                    sb.append(values[vidx]);
                    sb.append('\'');
                }else{
                    sb.append(values[vidx]);
                }

                vidx++;
            }else{
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
