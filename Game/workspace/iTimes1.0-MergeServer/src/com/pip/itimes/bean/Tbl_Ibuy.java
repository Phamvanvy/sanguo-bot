package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_IbuyDao;

public class Tbl_Ibuy extends BaseTable{
    /*
    CREATE TABLE `tbl_ibuy` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `accountid` int(11) NOT NULL DEFAULT '0',
      `playerid` int(11) NOT NULL DEFAULT '0',
      `itemid` int(11) NOT NULL DEFAULT '0',
      `itemname` varchar(255) NOT NULL DEFAULT '',
      `type` tinyint(4) NOT NULL DEFAULT '0',
      `imoney` int(11) NOT NULL DEFAULT '0',
      `buytime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `giftflag` tinyint(4) NOT NULL DEFAULT '0',
      `otherplayerid` int(11) NOT NULL DEFAULT '-1',
      `count` int(11) NOT NULL DEFAULT '1',
      `otherplayername` varchar(255) DEFAULT NULL,
      `level` int(11) NOT NULL DEFAULT '0',
      PRIMARY KEY (`id`),
      KEY `ibuy_playerid` (`playerid`)
    ) ENGINE=MyISAM AUTO_INCREMENT=195229 DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;
    */
    private int id;
    private int accountid;
    private int playerid;
    private int itemid;
    private String itemname;
    private int type;
    private int imoney;
    private Date buytime;
    private int giftflag;
    private int otherplayerid;
    private int count;
    private String otherplayername;
    private int level;

    @Override
    public String getColumnNames(){
        return Tbl_IbuyDao.SQL_PARA;
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
        sb.append(Tools.toSqlString(itemid));
        sb.append(", ");
        sb.append(Tools.toSqlString(itemname));
        sb.append(", ");
        sb.append(Tools.toSqlString(type));
        sb.append(", ");
        sb.append(Tools.toSqlString(imoney));
        sb.append(", ");
        sb.append(Tools.toSqlString(buytime));
        sb.append(", ");
        sb.append(Tools.toSqlString(giftflag));
        sb.append(", ");
        sb.append(Tools.toSqlString(otherplayerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(count));
        sb.append(", ");
        sb.append(Tools.toSqlString(otherplayername));
        sb.append(", ");
        sb.append(Tools.toSqlString(level));
        
        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procIbuyId(id);
    	
    	//处理playerid
    	playerid = mergeData.procPlayerId(playerid);
    	
    	//处理otherplayerid
    	otherplayerid = mergeData.procPlayerId(otherplayerid);
    	
    	//处理otherplayername
    	otherplayername = mergeData.procPlayerName(otherplayername);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getAccountid(){
        return accountid;
    }

    public void setAccountid(int accountid){
        this.accountid = accountid;
    }

    public int getPlayerid(){
        return playerid;
    }

    public void setPlayerid(int playerid){
        this.playerid = playerid;
    }

    public int getItemid(){
        return itemid;
    }

    public void setItemid(int itemid){
        this.itemid = itemid;
    }

    public String getItemname(){
        return itemname;
    }

    public void setItemname(String itemname){
        this.itemname = itemname;
    }

    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type = type;
    }

    public int getImoney(){
        return imoney;
    }

    public void setImoney(int imoney){
        this.imoney = imoney;
    }

    public Date getBuytime(){
        return buytime;
    }

    public void setBuytime(Date buytime){
        this.buytime = buytime;
    }

    public int getGiftflag(){
        return giftflag;
    }

    public void setGiftflag(int giftflag){
        this.giftflag = giftflag;
    }

    public int getOtherplayerid(){
        return otherplayerid;
    }

    public void setOtherplayerid(int otherplayerid){
        this.otherplayerid = otherplayerid;
    }

    public int getCount(){
        return count;
    }

    public void setCount(int count){
        this.count = count;
    }

    public String getOtherplayername(){
        return otherplayername;
    }

    public void setOtherplayername(String otherplayername){
        this.otherplayername = otherplayername;
    }
    
    public int getLevel(){
    	return level;
    }
    
    public void setLevel(int level){
    	this.level = level;
    }
}
