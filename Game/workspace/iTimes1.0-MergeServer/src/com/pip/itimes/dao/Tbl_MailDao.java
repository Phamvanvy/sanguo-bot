package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Mail;

public class Tbl_MailDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_MailDao.class);

    public static final String SQL_PARA = "id, sourceid, sourcename, destid, destname, title, content, attachment, price, posttime, readed, validtime";

    public Tbl_MailDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Mail> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Mail.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Mail.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Mail> list = new ArrayList<Tbl_Mail>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Mail mail = new Tbl_Mail();

                mail.setId(rs.getInt(1));
                mail.setSourceid(rs.getInt(2));
                mail.setSourcename(rs.getString(3));
                mail.setDestid(rs.getInt(4));
                mail.setDestname(rs.getString(5));
                mail.setTitle(rs.getString(6));
                mail.setContent(rs.getString(7));
                mail.setAttachment(rs.getBytes(8));
                mail.setPrice(rs.getInt(9));
                mail.setPosttime(rs.getTimestamp(10));
                mail.setReaded(rs.getInt(11));
                mail.setValidtime(rs.getTimestamp(12));

                list.add(mail);
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
