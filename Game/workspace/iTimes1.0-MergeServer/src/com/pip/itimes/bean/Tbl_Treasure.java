package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_TreasureDao;

public class Tbl_Treasure extends BaseTable{
    /*
    CREATE TABLE `tbl_treasure` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `playerid` int(11) NOT NULL DEFAULT '0',
      `x` int(11) NOT NULL DEFAULT '0',
      `y` int(11) NOT NULL DEFAULT '0',
      `mapid` int(11) NOT NULL DEFAULT '0',
      `itemgroupid` int(11) NOT NULL DEFAULT '0',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `keyitemid` int(11) NOT NULL DEFAULT '-1',
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=1515 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int playerid;
    private int x;
    private int y;
    private int mapid;
    private int itemgroupid;
    private Date createtime;
    private int keyitemid;

    @Override
    public String getColumnNames(){
        return Tbl_TreasureDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(x));
        sb.append(", ");
        sb.append(Tools.toSqlString(y));
        sb.append(", ");
        sb.append(Tools.toSqlString(mapid));
        sb.append(", ");
        sb.append(Tools.toSqlString(itemgroupid));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(keyitemid));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procTreasureId(id);
    	
    	//处理playerid
    	playerid = mergeData.procPlayerId(playerid);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getPlayerid(){
        return playerid;
    }

    public void setPlayerid(int playerid){
        this.playerid = playerid;
    }

    public int getX(){
        return x;
    }

    public void setX(int x){
        this.x = x;
    }

    public int getY(){
        return y;
    }

    public void setY(int y){
        this.y = y;
    }

    public int getMapid(){
        return mapid;
    }

    public void setMapid(int mapid){
        this.mapid = mapid;
    }

    public int getItemgroupid(){
        return itemgroupid;
    }

    public void setItemgroupid(int itemgroupid){
        this.itemgroupid = itemgroupid;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }
    
    public int getKeyitemid(){
    	return keyitemid;
    }
    
    public void setKeyitemid(int keyitemid){
    	this.keyitemid = keyitemid;
    }
}
