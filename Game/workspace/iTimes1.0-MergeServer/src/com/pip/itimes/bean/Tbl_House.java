package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_HouseDao;

public class Tbl_House extends BaseTable{
    /*
    CREATE TABLE `tbl_house` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `playerid` int(11) NOT NULL DEFAULT '0',
      `playername` varchar(100) NOT NULL DEFAULT '',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `level` int(11) NOT NULL DEFAULT '0',
      `style` int(11) NOT NULL DEFAULT '0',
      `rule` int(11) NOT NULL DEFAULT '0',
      `areaid` int(11) NOT NULL DEFAULT '0',
      `gridsize` int(11) NOT NULL DEFAULT '0',
      `items` blob,
      `parts` blob,
      `lasttime` datetime DEFAULT NULL,
      `title` mediumtext NOT NULL,
      `waiterid` int(11) NOT NULL DEFAULT '0',
      `visitedtimes` int(11) NOT NULL DEFAULT '0',
      `usedimoney` int(11) NOT NULL DEFAULT '0',
      `leavemessagetimes` int(11) NOT NULL DEFAULT '0',
      `canusewaitertime` datetime DEFAULT NULL,
      `autobuywaiter` int(11) NOT NULL DEFAULT '0',
      `addgridsize` int(11) NOT NULL DEFAULT '0',
      PRIMARY KEY (`id`),
      UNIQUE KEY `playerid` (`playerid`),
      KEY `house_visitedtimes` (`visitedtimes`),
      KEY `house_usedimoney` (`usedimoney`),
      KEY `house_leavemessagetimes` (`leavemessagetimes`)
    ) ENGINE=MyISAM AUTO_INCREMENT=23502 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int playerid;
    private String playername;
    private Date createtime;
    private int level;
    private int style;
    private int rule;
    private int areaid;
    private int gridsize;
    private byte[] items;
    private byte[] parts;
    private Date lasttime;
    private String title;
    private int waiterid;
    private int visitedtimes;
    private int usedimoney;
    private int leavemessagetimes;
    private Date canusewaitertime;
    private int autobuywaiter;
    private int addgridsize;

    @Override
    public String getColumnNames(){
        return Tbl_HouseDao.SQL_PARA;
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
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(level));
        sb.append(", ");
        sb.append(Tools.toSqlString(style));
        sb.append(", ");
        sb.append(Tools.toSqlString(rule));
        sb.append(", ");
        sb.append(Tools.toSqlString(areaid));
        sb.append(", ");
        sb.append(Tools.toSqlString(gridsize));
        sb.append(", ");
        sb.append(Tools.toSqlString(items));
        sb.append(", ");
        sb.append(Tools.toSqlString(parts));
        sb.append(", ");
        sb.append(Tools.toSqlString(lasttime));
        sb.append(", ");
        sb.append(Tools.toSqlString(title));
        sb.append(", ");
        sb.append(Tools.toSqlString(waiterid));
        sb.append(", ");
        sb.append(Tools.toSqlString(visitedtimes));
        sb.append(", ");
        sb.append(Tools.toSqlString(usedimoney));
        sb.append(", ");
        sb.append(Tools.toSqlString(leavemessagetimes));
        sb.append(", ");
        sb.append(Tools.toSqlString(canusewaitertime));
        sb.append(", ");
        sb.append(Tools.toSqlString(autobuywaiter));
        sb.append(", ");
        sb.append(Tools.toSqlString(addgridsize));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procHouseId(id);
    	
    	//处理playerid
    	playerid = mergeData.procPlayerId(playerid);
    	
    	//处理playername
    	playername = mergeData.procPlayerName(playername);
    	
    	//处理items
    	items = Tools.procItems(items, mergeData);
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

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public int getStyle(){
        return style;
    }

    public void setStyle(int style){
        this.style = style;
    }

    public int getRule(){
        return rule;
    }

    public void setRule(int rule){
        this.rule = rule;
    }

    public int getAreaid(){
        return areaid;
    }

    public void setAreaid(int areaid){
        this.areaid = areaid;
    }

    public int getGridsize(){
        return gridsize;
    }

    public void setGridsize(int gridsize){
        this.gridsize = gridsize;
    }

    public byte[] getItems(){
        return items;
    }

    public void setItems(byte[] items){
        this.items = items;
    }

    public byte[] getParts(){
        return parts;
    }

    public void setParts(byte[] parts){
        this.parts = parts;
    }

    public Date getLasttime(){
        return lasttime;
    }

    public void setLasttime(Date lasttime){
        this.lasttime = lasttime;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public int getWaiterid(){
        return waiterid;
    }

    public void setWaiterid(int waiterid){
        this.waiterid = waiterid;
    }

    public int getVisitedtimes(){
        return visitedtimes;
    }

    public void setVisitedtimes(int visitedtimes){
        this.visitedtimes = visitedtimes;
    }

    public int getUsedimoney(){
        return usedimoney;
    }

    public void setUsedimoney(int usedimoney){
        this.usedimoney = usedimoney;
    }

    public int getLeavemessagetimes(){
        return leavemessagetimes;
    }

    public void setLeavemessagetimes(int leavemessagetimes){
        this.leavemessagetimes = leavemessagetimes;
    }

    public Date getCanusewaitertime(){
        return canusewaitertime;
    }

    public void setCanusewaitertime(Date canusewaitertime){
        this.canusewaitertime = canusewaitertime;
    }

    public int getAutobuywaiter(){
        return autobuywaiter;
    }

    public void setAutobuywaiter(int autobuywaiter){
        this.autobuywaiter = autobuywaiter;
    }

    public int getAddgridsize(){
        return addgridsize;
    }

    public void setAddgridsize(int addgridsize){
        this.addgridsize = addgridsize;
    }
}
