package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_ChargeDao;

public class Tbl_Charge extends BaseTable{
	/*
	CREATE TABLE `tbl_charge` (
	  `id` int(11) NOT NULL AUTO_INCREMENT,
	  `accountid` int(11) DEFAULT NULL,
	  `playerid` int(11) DEFAULT NULL,
	  `playerlevel` int(11) DEFAULT NULL,
	  `money` int(11) DEFAULT NULL,
	  `chargetime` datetime DEFAULT NULL,
	  PRIMARY KEY (`id`)
	) ENGINE=MyISAM AUTO_INCREMENT=1556 DEFAULT CHARSET=utf8;
	 */
	
	private int id;
	private int accountid;
	private int playerid;
	private int playerlevel;
	private int money;
	private Date chargetime;
	
	@Override
	public String getColumnNames() {
		return Tbl_ChargeDao.SQL_PARA;
	}
	@Override
	public void process(MergeData mergeData, ServerConfig serverConfig) {
        //处理id
        id = mergeData.procChargeId(id);
        
        //处理playerid
        playerid = mergeData.procPlayerId(playerid);
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(accountid));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerlevel));
        sb.append(", ");
        sb.append(Tools.toSqlString(money));
        sb.append(", ");
        sb.append(Tools.toSqlString(chargetime));

        return sb.toString();
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getAccountID(){
		return accountid;
	}
	
	public void setAccountID(int accountid){
		this.accountid = accountid;
	}
	
	public int getPlayerID(){
		return playerid;
	}
	
	public void setPlayerID(int playerid){
		this.playerid = playerid;
	}
	
	public int getPlayerLevel(){
		return playerlevel;
	}
	
	public void setPlayerLevel(int playerlevel){
		this.playerlevel = playerlevel;
	}
	
	public int getMoney(){
		return money;
	}
	
	public void setMoney(int money){
		this.money = money;
	}
	
	public Date getChargeTime(){
		return chargetime;
	}
	
	public void setChargeTime(Date chargetime){
		this.chargetime = chargetime;
	}

}
