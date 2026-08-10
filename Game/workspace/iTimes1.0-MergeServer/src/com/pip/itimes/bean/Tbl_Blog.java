package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_BlogDao;

public class Tbl_Blog extends BaseTable{
    /*
    CREATE TABLE `tbl_blog` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `playerid` int(11) NOT NULL DEFAULT '0',
      `playername` varchar(100) NOT NULL DEFAULT '',
      `title` varchar(255) NOT NULL DEFAULT '',
      `content` mediumtext NOT NULL,
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `readedtimes` int(11) NOT NULL DEFAULT '0',
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=508 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int playerid;
    private String playername;
    private String title;
    private String content;
    private Date createtime;
    private int readedtimes;

    @Override
    public String getColumnNames(){
        return Tbl_BlogDao.SQL_PARA;
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
        sb.append(Tools.toSqlString(title));
        sb.append(", ");
        sb.append(Tools.toSqlString(content));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(readedtimes));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
        id = mergeData.procBlogId(id);
        
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

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public int getReadedtimes(){
        return readedtimes;
    }

    public void setReadedtimes(int readedtimes){
        this.readedtimes = readedtimes;
    }
}
