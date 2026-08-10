package com.pip.itimes.bean;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_MasterDao;

public class Tbl_Master extends BaseTable{
    /*
    CREATE TABLE `tbl_master` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `masterid` int(11) NOT NULL DEFAULT '0',
      `mastername` varchar(255) NOT NULL DEFAULT '',
      `prenticeid` int(11) NOT NULL DEFAULT '0',
      `prenticename` varchar(20) NOT NULL DEFAULT '',
      `beginlevel` int(11) NOT NULL DEFAULT '0',
      `state` tinyint(4) NOT NULL DEFAULT '0',
      `intimacy` int(11) NOT NULL DEFAULT '0',
  	  `fame` int(11) NOT NULL DEFAULT '0',
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=4288 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int masterid;
    private String mastername;
    private int prenticeid;
    private String prenticename;
    private int beginlevel;
    private int state;
    private int intimacy;
    private int fame;

    @Override
    public String getColumnNames(){
        return Tbl_MasterDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(masterid));
        sb.append(", ");
        sb.append(Tools.toSqlString(mastername));
        sb.append(", ");
        sb.append(Tools.toSqlString(prenticeid));
        sb.append(", ");
        sb.append(Tools.toSqlString(prenticename));
        sb.append(", ");
        sb.append(Tools.toSqlString(beginlevel));
        sb.append(", ");
        sb.append(Tools.toSqlString(state));
        sb.append(", ");
        sb.append(Tools.toSqlString(intimacy));
        sb.append(", ");
        sb.append(Tools.toSqlString(fame));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procMasterId(id);
    	
    	//处理masterid
    	masterid = mergeData.procPlayerId(masterid);
    	
    	//处理mastername
    	mastername = mergeData.procPlayerName(mastername);
    	
    	//处理prenticeid
    	prenticeid = mergeData.procPlayerId(prenticeid);
    	
    	//处理prenticename
    	prenticename = mergeData.procPlayerName(prenticename);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getMasterid(){
        return masterid;
    }

    public void setMasterid(int masterid){
        this.masterid = masterid;
    }

    public String getMastername(){
        return mastername;
    }

    public void setMastername(String mastername){
        this.mastername = mastername;
    }

    public int getPrenticeid(){
        return prenticeid;
    }

    public void setPrenticeid(int prenticeid){
        this.prenticeid = prenticeid;
    }

    public String getPrenticename(){
        return prenticename;
    }

    public void setPrenticename(String prenticename){
        this.prenticename = prenticename;
    }

    public int getBeginlevel(){
        return beginlevel;
    }

    public void setBeginlevel(int beginlevel){
        this.beginlevel = beginlevel;
    }

    public int getState(){
        return state;
    }

    public void setState(int state){
        this.state = state;
    }
    
    public int getIntimacy(){
    	return intimacy;
    }
    
    public void setIntimacy(int intimacy){
    	this.intimacy = intimacy;
    }
    
    public int getFame(){
    	return fame;
    }
    
    public void setFame(int fame){
    	this.fame = fame;
    }
}
