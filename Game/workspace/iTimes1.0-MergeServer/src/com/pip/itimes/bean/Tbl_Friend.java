package com.pip.itimes.bean;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_FriendDao;

public class Tbl_Friend extends BaseTable{
    /*
    CREATE TABLE `tbl_friend` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `playerid` int(11) NOT NULL DEFAULT '0',
      `playername` varchar(255) DEFAULT '',
      `friendplayerid` int(11) NOT NULL DEFAULT '0',
      `level` int(11) NOT NULL DEFAULT '0',
      `imoney` int(11) NOT NULL DEFAULT '0',
      `valid` tinyint(4) NOT NULL DEFAULT '0',
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=3056 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int playerid;
    private String playername;
    private int friendplayerid;
    private int level;
    private int imoney;
    private int valid;

    @Override
    public String getColumnNames(){
        return Tbl_FriendDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(playername));
        sb.append(", ");
        sb.append(Tools.toSqlString(friendplayerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(level));
        sb.append(", ");
        sb.append(Tools.toSqlString(imoney));
        sb.append(", ");
        sb.append(Tools.toSqlString(valid));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
        id = mergeData.procFriendId(id);
        
        //处理playerid和friendplayerid
        playerid = mergeData.procPlayerId(playerid);
        friendplayerid = mergeData.procPlayerId(friendplayerid);
        
        //处理playername
        playername = mergeData.procPlayerName(playername);
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

    public String getPlayername(){
        return playername;
    }

    public void setPlayername(String playername){
        this.playername = playername;
    }

    public int getFriendplayerid(){
        return friendplayerid;
    }

    public void setFriendplayerid(int friendplayerid){
        this.friendplayerid = friendplayerid;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public int getImoney(){
        return imoney;
    }

    public void setImoney(int imoney){
        this.imoney = imoney;
    }

    public int getValid(){
        return valid;
    }

    public void setValid(int valid){
        this.valid = valid;
    }
}
