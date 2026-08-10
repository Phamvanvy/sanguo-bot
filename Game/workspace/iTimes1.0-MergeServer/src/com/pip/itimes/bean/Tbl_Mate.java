package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_MateDao;

public class Tbl_Mate extends BaseTable{
    /*
    CREATE TABLE `tbl_mate` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `husbandid` int(11) NOT NULL DEFAULT '0',
      `husbandname` varchar(255) NOT NULL DEFAULT '',
      `wifeid` int(11) NOT NULL DEFAULT '0',
      `wifename` varchar(255) NOT NULL DEFAULT '',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=511 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int husbandid;
    private String husbandname;
    private int wifeid;
    private String wifename;
    private Date createtime;

    @Override
    public String getColumnNames(){
        return Tbl_MateDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(husbandid));
        sb.append(", ");
        sb.append(Tools.toSqlString(husbandname));
        sb.append(", ");
        sb.append(Tools.toSqlString(wifeid));
        sb.append(", ");
        sb.append(Tools.toSqlString(wifename));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procMateId(id);
    	
    	//处理husbandid
    	husbandid = mergeData.procPlayerId(husbandid);
    	
    	//处理husbandname
    	husbandname= mergeData.procPlayerName(husbandname);
    	
    	//处理wifeid
    	wifeid = mergeData.procPlayerId(wifeid);
    	
    	//处理wifename
    	wifename = mergeData.procPlayerName(wifename);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getHusbandid(){
        return husbandid;
    }

    public void setHusbandid(int husbandid){
        this.husbandid = husbandid;
    }

    public String getHusbandname(){
        return husbandname;
    }

    public void setHusbandname(String husbandname){
        this.husbandname = husbandname;
    }

    public int getWifeid(){
        return wifeid;
    }

    public void setWifeid(int wifeid){
        this.wifeid = wifeid;
    }

    public String getWifename(){
        return wifename;
    }

    public void setWifename(String wifename){
        this.wifename = wifename;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }
}
