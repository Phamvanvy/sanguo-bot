package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_IMoneyCard;

public class Tbl_IMoneyCardDao extends BaseDao{

private static final Logger log = Logger.getLogger(Tbl_BuyDao.class);
	
	public static final String SQL_PARA = "id, createaccountid, createplayerid, createtime, useaccountid, useplayerid, usetime, cardno, password, amount, status";

	public Tbl_IMoneyCardDao(ServerConfig config) {
		super(config);
	}

    public ArrayList<Tbl_IMoneyCard> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_IMoneyCard.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_IMoneyCard.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_IMoneyCard> list = new ArrayList<Tbl_IMoneyCard>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_IMoneyCard imoneyCard = new Tbl_IMoneyCard();

                imoneyCard.setId(rs.getInt(1));
                imoneyCard.setCreateaccountid(rs.getInt(2));
                imoneyCard.setCreateplayerid(rs.getInt(3));
                imoneyCard.setCreatetime(rs.getTimestamp(4));
                imoneyCard.setUseaccountid(rs.getInt(5));
                imoneyCard.setUseplayerid(rs.getInt(6));
                imoneyCard.setUsetime(rs.getTimestamp(7));
                imoneyCard.setCardno(rs.getString(8));
                imoneyCard.setPassword(rs.getString(9));
                imoneyCard.setAmount(rs.getInt(10));
                imoneyCard.setStatus(rs.getInt(11));

                list.add(imoneyCard);
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
