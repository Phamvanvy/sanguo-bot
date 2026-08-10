package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_BuyDao;

public class Tbl_Buy extends BaseTable{
    /*
    CREATE TABLE `tbl_buy` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `shopid` int(11) NOT NULL DEFAULT '0',
      `itemid` int(11) NOT NULL DEFAULT '0',
      `total` int(11) NOT NULL DEFAULT '0',
      `current` int(11) NOT NULL DEFAULT '0',
      `price` int(11) NOT NULL DEFAULT '0',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `name` varchar(255) NOT NULL DEFAULT '',
      `type` int(11) NOT NULL DEFAULT '0',
      `areaid` int(11) NOT NULL DEFAULT '0',
      `state` int(11) NOT NULL DEFAULT '0',
      `quality` int(11) NOT NULL DEFAULT '0',
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=3848 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int shopid;
    private int itemid;
    private int total;
    private int current;
    private int price;
    private Date createtime;
    private String name;
    private int type;
    private int areaid;
    private int state;
    private int quality;

    @Override
    public String getColumnNames(){
        return Tbl_BuyDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(shopid));
        sb.append(", ");
        sb.append(Tools.toSqlString(itemid));
        sb.append(", ");
        sb.append(Tools.toSqlString(total));
        sb.append(", ");
        sb.append(Tools.toSqlString(current));
        sb.append(", ");
        sb.append(Tools.toSqlString(price));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(name));
        sb.append(", ");
        sb.append(Tools.toSqlString(type));
        sb.append(", ");
        sb.append(Tools.toSqlString(areaid));
        sb.append(", ");
        sb.append(Tools.toSqlString(state));
        sb.append(", ");
        sb.append(Tools.toSqlString(quality));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
        id = mergeData.procBuyId(id);
        
        //处理shopid
        shopid = mergeData.procShopId(shopid);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getShopid(){
        return shopid;
    }

    public void setShopid(int shopid){
        this.shopid = shopid;
    }

    public int getItemid(){
        return itemid;
    }

    public void setItemid(int itemid){
        this.itemid = itemid;
    }

    public int getTotal(){
        return total;
    }

    public void setTotal(int total){
        this.total = total;
    }

    public int getCurrent(){
        return current;
    }

    public void setCurrent(int current){
        this.current = current;
    }

    public int getPrice(){
        return price;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type = type;
    }

    public int getAreaid(){
        return areaid;
    }

    public void setAreaid(int areaid){
        this.areaid = areaid;
    }

    public int getState(){
        return state;
    }

    public void setState(int state){
        this.state = state;
    }

    public int getQuality(){
        return quality;
    }

    public void setQuality(int quality){
        this.quality = quality;
    }
}
