package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_BbsDao;

public class Tbl_Bbs extends BaseTable{
    /*
    CREATE TABLE `tbl_bbs` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `bbsid` int(11) NOT NULL DEFAULT '0',
      `playerid` int(11) NOT NULL DEFAULT '0',
      `playername` varchar(255) NOT NULL DEFAULT '',
      `title` varchar(255) NOT NULL DEFAULT '',
      `content` mediumtext,
      `posttime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `priority` int(11) DEFAULT NULL,
      PRIMARY KEY (`id`),
      KEY `index_bbs_bbsid` (`bbsid`),
      KEY `index_bbs_priority` (`priority`),
      KEY `index_bbs_posttime` (`posttime`)
    ) ENGINE=MyISAM AUTO_INCREMENT=12761 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int bbsid;
    private int playerid;
    private String playername;
    private String title;
    private String content;
    private Date posttime;
    private int priority;

    @Override
    public String getColumnNames(){
        return Tbl_BbsDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(bbsid));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(playername));
        sb.append(", ");
        sb.append(Tools.toSqlString(title));
        sb.append(", ");
        sb.append(Tools.toSqlString(content));
        sb.append(", ");
        sb.append(Tools.toSqlString(posttime));
        sb.append(", ");
        sb.append(Tools.toSqlString(priority));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
        id = mergeData.procBbsId(id);
        
        //处理playerid
        playerid = mergeData.procPlayerId(playerid);
        
        //处理playername
        playername = mergeData.procPlayerName(playername);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getBbsid(){
        return bbsid;
    }

    public void setBbsid(int bbsid){
        this.bbsid = bbsid;
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

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }

    public Date getPosttime(){
        return posttime;
    }

    public void setPosttime(Date posttime){
        this.posttime = posttime;
    }

    public int getPriority(){
        return priority;
    }

    public void setPriority(int priority){
        this.priority = priority;
    }
}
