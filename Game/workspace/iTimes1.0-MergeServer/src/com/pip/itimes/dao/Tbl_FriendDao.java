package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Friend;

public class Tbl_FriendDao extends BaseDao{
    private static final Logger log = Logger.getLogger(Tbl_FriendDao.class);

    public static final String SQL_PARA = "id, playerid, playername, friendplayerid, level, imoney, valid";

    public Tbl_FriendDao(ServerConfig config){
        super(config);
    }

    public ArrayList<Tbl_Friend> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Friend.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Friend.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Friend> list = new ArrayList<Tbl_Friend>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Friend friend = new Tbl_Friend();

                friend.setId(rs.getInt(1));
                friend.setPlayerid(rs.getInt(2));
                friend.setPlayername(rs.getString(3));
                friend.setFriendplayerid(rs.getInt(4));
                friend.setLevel(rs.getInt(5));
                friend.setImoney(rs.getInt(6));
                friend.setValid(rs.getInt(7));

                list.add(friend);
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
