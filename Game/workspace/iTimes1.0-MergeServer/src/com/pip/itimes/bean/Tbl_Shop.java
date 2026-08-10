package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_ShopDao;

public class Tbl_Shop extends BaseTable{
    /*
    CREATE TABLE `tbl_shop` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `name` varchar(255) NOT NULL DEFAULT '',
      `money` int(11) NOT NULL DEFAULT '0',
      `playerid` int(11) NOT NULL DEFAULT '0',
      `level` int(11) NOT NULL DEFAULT '0',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `items` blob NOT NULL,
      `areaid` int(11) NOT NULL DEFAULT '0',
      `gridsize` int(11) NOT NULL DEFAULT '0',
      `state` int(11) NOT NULL DEFAULT '0',
      `buyplayerid` int(11) NOT NULL DEFAULT '0',
      `price` int(11) NOT NULL DEFAULT '0',
      `selltime` datetime DEFAULT NULL,
      `leveluptime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=5267 DEFAULT CHARSET=utf8;
    */
    private int id;
    private String name;
    private int money;
    private int playerid;
    private int level;
    private Date createtime;
    private byte[] items;
    private int areaid;
    private int gridsize;
    private int state;
    private int buyplayerid;
    private int price;
    private Date selltime;
    private Date leveluptime;

    @Override
    public String getColumnNames(){
        return Tbl_ShopDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(name));
        sb.append(", ");
        sb.append(Tools.toSqlString(money));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(level));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(items));
        sb.append(", ");
        sb.append(Tools.toSqlString(areaid));
        sb.append(", ");
        sb.append(Tools.toSqlString(gridsize));
        sb.append(", ");
        sb.append(Tools.toSqlString(state));
        sb.append(", ");
        sb.append(Tools.toSqlString(buyplayerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(price));
        sb.append(", ");
        sb.append(Tools.toSqlString(selltime));
        sb.append(", ");
        sb.append(Tools.toSqlString(leveluptime));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procShopId(id);
    	
    	//处理name
    	name = mergeData.procShopName(name);
    	
    	//处理playerid
    	playerid = mergeData.procPlayerId(playerid);
    	
    	//处理items
    	items = Tools.procItems(items, mergeData);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getMoney(){
        return money;
    }

    public void setMoney(int money){
        this.money = money;
    }

    public int getPlayerid(){
        return playerid;
    }

    public void setPlayerid(int playerid){
        this.playerid = playerid;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public byte[] getItems(){
        return items;
    }

    public void setItems(byte[] items){
        this.items = items;
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

    public int getState(){
        return state;
    }

    public void setState(int state){
        this.state = state;
    }

    public int getBuyplayerid(){
        return buyplayerid;
    }

    public void setBuyplayerid(int buyplayerid){
        this.buyplayerid = buyplayerid;
    }

    public int getPrice(){
        return price;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public Date getSelltime(){
        return selltime;
    }

    public void setSelltime(Date selltime){
        this.selltime = selltime;
    }

    public Date getLeveluptime(){
        return leveluptime;
    }

    public void setLeveluptime(Date leveluptime){
        this.leveluptime = leveluptime;
    }
}
