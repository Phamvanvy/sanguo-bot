package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_VotecampDao;

public class Tbl_Votecamp extends BaseTable{
    /*
    CREATE TABLE `tbl_votecamp` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `playerid` int(11) NOT NULL DEFAULT '0',
      `playername` varchar(255) NOT NULL DEFAULT '',
      `camp` tinyint(4) NOT NULL DEFAULT '1',
      `credit` int(11) NOT NULL DEFAULT '0',
      `creditoffer` int(11) NOT NULL DEFAULT '0',
      `leve` int(11) NOT NULL DEFAULT '0',
      `moeny` int(11) NOT NULL DEFAULT '0',
      `fristtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `endtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `title` varchar(64) NOT NULL DEFAULT '',
      `ticket` int(11) NOT NULL DEFAULT '0',
      `itemcount` int(11) NOT NULL DEFAULT '0',
      `kingflag` tinyint(4) NOT NULL DEFAULT '0',
      `valid` tinyint(4) NOT NULL DEFAULT '1',
      `itemcounttotal` int(11) NOT NULL DEFAULT '0',
      PRIMARY KEY (`id`),
      KEY `index_playerid` (`playerid`)
    ) ENGINE=MyISAM AUTO_INCREMENT=111 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int playerid;
    private String playername;
    private int camp;
    private int credit;
    private int creditoffer;
    private int leve;
    private int moeny;
    private Date fristtime;
    private Date endtime;
    private String title;
    private int ticket;
    private int itemcount;
    private int kingflag;
    private int valid;
    private int itemcounttotal;

    @Override
    public String getColumnNames(){
        return Tbl_VotecampDao.SQL_PARA;
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
        sb.append(Tools.toSqlString(camp));
        sb.append(", ");
        sb.append(Tools.toSqlString(credit));
        sb.append(", ");
        sb.append(Tools.toSqlString(creditoffer));
        sb.append(", ");
        sb.append(Tools.toSqlString(leve));
        sb.append(", ");
        sb.append(Tools.toSqlString(moeny));
        sb.append(", ");
        sb.append(Tools.toSqlString(fristtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(endtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(title));
        sb.append(", ");
        sb.append(Tools.toSqlString(ticket));
        sb.append(", ");
        sb.append(Tools.toSqlString(itemcount));
        sb.append(", ");
        sb.append(Tools.toSqlString(kingflag));
        sb.append(", ");
        sb.append(Tools.toSqlString(valid));
        sb.append(", ");
        sb.append(Tools.toSqlString(itemcounttotal));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procVotecampId(id);
    	
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

    public int getCamp(){
        return camp;
    }

    public void setCamp(int camp){
        this.camp = camp;
    }

    public int getCredit(){
        return credit;
    }

    public void setCredit(int credit){
        this.credit = credit;
    }

    public int getCreditoffer(){
        return creditoffer;
    }

    public void setCreditoffer(int creditoffer){
        this.creditoffer = creditoffer;
    }

    public int getLeve(){
        return leve;
    }

    public void setLeve(int leve){
        this.leve = leve;
    }

    public int getMoeny(){
        return moeny;
    }

    public void setMoeny(int moeny){
        this.moeny = moeny;
    }

    public Date getFristtime(){
        return fristtime;
    }

    public void setFristtime(Date fristtime){
        this.fristtime = fristtime;
    }

    public Date getEndtime(){
        return endtime;
    }

    public void setEndtime(Date endtime){
        this.endtime = endtime;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public int getTicket(){
        return ticket;
    }

    public void setTicket(int ticket){
        this.ticket = ticket;
    }

    public int getItemcount(){
        return itemcount;
    }

    public void setItemcount(int itemcount){
        this.itemcount = itemcount;
    }

    public int getKingflag(){
        return kingflag;
    }

    public void setKingflag(int kingflag){
        this.kingflag = kingflag;
    }

    public int getValid(){
        return valid;
    }

    public void setValid(int valid){
        this.valid = valid;
    }

    public int getItemcounttotal(){
        return itemcounttotal;
    }

    public void setItemcounttotal(int itemcounttotal){
        this.itemcounttotal = itemcounttotal;
    }
}
