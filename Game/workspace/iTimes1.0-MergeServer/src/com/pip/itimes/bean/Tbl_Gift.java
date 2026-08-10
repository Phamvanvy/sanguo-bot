package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_GiftDao;

public class Tbl_Gift extends BaseTable{
    /*
    CREATE TABLE `tbl_gift` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `groupid` int(11) NOT NULL DEFAULT '0',
      `playerid` int(11) NOT NULL DEFAULT '0',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `modifytime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `rcount` int(11) NOT NULL DEFAULT '0',
      `count` int(11) NOT NULL DEFAULT '0',
      PRIMARY KEY (`id`),
      UNIQUE KEY `gfit_index` (`groupid`,`playerid`),
      KEY `group_index` (`groupid`),
      KEY `player_index` (`playerid`)
    ) ENGINE=MyISAM AUTO_INCREMENT=544602 DEFAULT CHARSET=utf8 DELAY_KEY_WRITE=1;
    */
    private int id;
    private int groupid;
    private int playerid;
    private Date createtime;
    private Date modifytime;
    private int rcount;
    private int count;

    @Override
    public String getColumnNames(){
        return Tbl_GiftDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(groupid));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(modifytime));
        sb.append(", ");
        sb.append(Tools.toSqlString(rcount));
        sb.append(", ");
        sb.append(Tools.toSqlString(count));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procGiftId(id);
    	
    	//处理playerid
    	playerid = mergeData.procPlayerId(playerid);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getGroupid(){
        return groupid;
    }

    public void setGroupid(int groupid){
        this.groupid = groupid;
    }

    public int getPlayerid(){
        return playerid;
    }

    public void setPlayerid(int playerid){
        this.playerid = playerid;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public Date getModifytime(){
        return modifytime;
    }

    public void setModifytime(Date modifytime){
        this.modifytime = modifytime;
    }

    public int getRcount(){
        return rcount;
    }

    public void setRcount(int rcount){
        this.rcount = rcount;
    }

    public int getCount(){
        return count;
    }

    public void setCount(int count){
        this.count = count;
    }
}
