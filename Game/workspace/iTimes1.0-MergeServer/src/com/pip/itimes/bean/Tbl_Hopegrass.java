package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_HopegrassDao;

public class Tbl_Hopegrass extends BaseTable{
    /*
    CREATE TABLE `tbl_hopegrass` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `playerid` int(11) NOT NULL DEFAULT '0',
      `mapid` int(11) NOT NULL DEFAULT '0',
      `x` int(11) NOT NULL DEFAULT '0',
      `y` int(11) NOT NULL DEFAULT '0',
      `itemgroupid` int(11) NOT NULL DEFAULT '0',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `validtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `obsoletetime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `grasstype` int(11) NOT NULL DEFAULT '0',
      `ratio` int(11) NOT NULL DEFAULT '0',
      `grouprnd` int(11) NOT NULL DEFAULT '0',
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=67647 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int playerid;
    private int mapid;
    private int x;
    private int y;
    private int itemgroupid;
    private Date createtime;
    private Date validtime;
    private Date obsoletetime;
    private int grasstype;
    private int ratio;
    private int grouprnd;

    @Override
    public String getColumnNames(){
        return Tbl_HopegrassDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(mapid));
        sb.append(", ");
        sb.append(Tools.toSqlString(x));
        sb.append(", ");
        sb.append(Tools.toSqlString(y));
        sb.append(", ");
        sb.append(Tools.toSqlString(itemgroupid));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(validtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(obsoletetime));
        sb.append(", ");
        sb.append(Tools.toSqlString(grasstype));
        sb.append(", ");
        sb.append(Tools.toSqlString(ratio));
        sb.append(", ");
        sb.append(Tools.toSqlString(grouprnd));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procHopegrassId(id);
    	
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

    public int getMapid(){
        return mapid;
    }

    public void setMapid(int mapid){
        this.mapid = mapid;
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

    public Date getValidtime(){
        return validtime;
    }

    public void setValidtime(Date validtime){
        this.validtime = validtime;
    }

    public Date getObsoletetime(){
        return obsoletetime;
    }

    public void setObsoletetime(Date obsoletetime){
        this.obsoletetime = obsoletetime;
    }

    public int getGrasstype(){
        return grasstype;
    }

    public void setGrasstype(int grasstype){
        this.grasstype = grasstype;
    }

    public int getRatio(){
        return ratio;
    }

    public void setRatio(int ratio){
        this.ratio = ratio;
    }

    public int getGrouprnd(){
        return grouprnd;
    }

    public void setGrouprnd(int grouprnd){
        this.grouprnd = grouprnd;
    }
}
