package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_AuctionDao;
import com.pip.itimes.server.stage.Attachment;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemAttachment;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.MoneyAttachment;

public class Tbl_Auction extends BaseTable{
    /*
    CREATE TABLE `tbl_auction` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `playerid` int(11) NOT NULL DEFAULT '0',
      `shopid` int(11) NOT NULL DEFAULT '0',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `startprice` int(11) NOT NULL DEFAULT '0',
      `currentprice` int(11) NOT NULL DEFAULT '0',
      `endprice` int(11) NOT NULL DEFAULT '0',
      `item` blob NOT NULL,
      `name` varchar(255) NOT NULL DEFAULT '',
      `type` int(11) NOT NULL DEFAULT '0',
      `lastplayerid` int(11) NOT NULL DEFAULT '0',
      `playername` varchar(20) NOT NULL DEFAULT '',
      `quality` int(11) NOT NULL DEFAULT '0',
      `level` int(11) NOT NULL DEFAULT '0',
      `areaid` int(11) NOT NULL DEFAULT '0',
      `state` int(11) NOT NULL DEFAULT '0',
      `validtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=265134 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int playerid;
    private int shopid;
    private Date createtime;
    private int startprice;
    private int currentprice;
    private int endprice;
    private byte[] item;
    private String name;
    private int type;
    private int lastplayerid;
    private String playername;
    private int quality;
    private int level;
    private int areaid;
    private int state;
    private Date validtime;

    @Override
    public String getColumnNames(){
        return Tbl_AuctionDao.SQL_PARA;
    }
    
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(shopid));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(startprice));
        sb.append(", ");
        sb.append(Tools.toSqlString(currentprice));
        sb.append(", ");
        sb.append(Tools.toSqlString(endprice));
        sb.append(", ");
        sb.append(Tools.toSqlString(item));
        sb.append(", ");
        sb.append(Tools.toSqlString(name));
        sb.append(", ");
        sb.append(Tools.toSqlString(type));
        sb.append(", ");
        sb.append(Tools.toSqlString(lastplayerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(playername));
        sb.append(", ");
        sb.append(Tools.toSqlString(quality));
        sb.append(", ");
        sb.append(Tools.toSqlString(level));
        sb.append(", ");
        sb.append(Tools.toSqlString(areaid));
        sb.append(", ");
        sb.append(Tools.toSqlString(state));
        sb.append(", ");
        sb.append(Tools.toSqlString(validtime));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
        id = mergeData.procAuctionId(id);

        //处理shopId
        shopid = mergeData.procShopId(shopid);
        
        //处理playerId, playername
        if(playerid >= 0){
            playerid = mergeData.procPlayerId(playerid);
            playername = mergeData.procPlayerName(playername);
        }
        
        //处理lastplayerid
        lastplayerid = mergeData.procPlayerId(lastplayerid);

        //处理item中的instanceId
        if(item != null){
            Attachment att = ItemUtils.dbBytes2Attachment(item, 0);
            
            if(att instanceof ItemAttachment){
                IItem itm = ((ItemAttachment)att).getItem();

                if(itm instanceof IEquipment){
                    int instanceId = ((IEquipment)itm).getId();
                    instanceId = mergeData.procEquipmentId(instanceId);
                    ((IEquipment)itm).setId(instanceId);
                }
                
                item = att.toDbBytes();
            }
        }
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

    public int getShopid(){
        return shopid;
    }

    public void setShopid(int shopid){
        this.shopid = shopid;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public int getStartprice(){
        return startprice;
    }

    public void setStartprice(int startprice){
        this.startprice = startprice;
    }

    public int getCurrentprice(){
        return currentprice;
    }

    public void setCurrentprice(int currentprice){
        this.currentprice = currentprice;
    }

    public int getEndprice(){
        return endprice;
    }

    public void setEndprice(int endprice){
        this.endprice = endprice;
    }

    public byte[] getItem(){
        return item;
    }

    public void setItem(byte[] item){
        this.item = item;
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

    public int getLastplayerid(){
        return lastplayerid;
    }

    public void setLastplayerid(int lastplayerid){
        this.lastplayerid = lastplayerid;
    }

    public String getPlayername(){
        return playername;
    }

    public void setPlayername(String playername){
        this.playername = playername;
    }

    public int getQuality(){
        return quality;
    }

    public void setQuality(int quality){
        this.quality = quality;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
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

    public Date getValidtime(){
        return validtime;
    }

    public void setValidtime(Date validtime){
        this.validtime = validtime;
    }
}
