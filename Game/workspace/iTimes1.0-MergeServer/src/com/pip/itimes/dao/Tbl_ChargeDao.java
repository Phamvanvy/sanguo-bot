package com.pip.itimes.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pip.itimes.ServerConfig;
import com.pip.itimes.bean.Tbl_Charge;

public class Tbl_ChargeDao extends BaseDao{
	private static final Logger log = Logger.getLogger(Tbl_BuyDao.class);
	
	public static final String SQL_PARA = "id, accountid, playerid, playerlevel, money, chargetime";

	public Tbl_ChargeDao(ServerConfig config) {
		super(config);
	}

    public ArrayList<Tbl_Charge> getList(int begin, int count){
        String query = buildQuery("select " + SQL_PARA + " from " + Tbl_Charge.class.getSimpleName().toLowerCase());
        query += " where id >= (select id from " + Tbl_Charge.class.getSimpleName().toLowerCase() + " order by id limit " + begin + ", 1) order by id limit " + count;

        Statement statement = getStatement();
        ArrayList<Tbl_Charge> list = new ArrayList<Tbl_Charge>();
        ResultSet rs = null;

        try{
            rs = statement.executeQuery(query);

            while(rs.next()){
                Tbl_Charge charge = new Tbl_Charge();

                charge.setId(rs.getInt(1));
                charge.setAccountID(rs.getInt(2));
                charge.setPlayerID(rs.getInt(3));
                charge.setPlayerLevel(rs.getInt(4));
                charge.setMoney(rs.getInt(5));
                charge.setChargeTime(rs.getTimestamp(6));

                list.add(charge);
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
