package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_LeavemessageDao;

public class Tbl_Leavemessage extends BaseTable{
    /*
    CREATE TABLE `tbl_leavemessage` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `sourceid` int(11) NOT NULL DEFAULT '0',
      `sourcename` varchar(100) NOT NULL DEFAULT '',
      `title` varchar(255) NOT NULL DEFAULT '',
      `content` mediumtext NOT NULL,
      `ownerid` int(11) NOT NULL DEFAULT '0',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=7248 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int sourceid;
    private String sourcename;
    private String title;
    private String content;
    private int ownerid;
    private Date createtime;

    @Override
    public String getColumnNames(){
        return Tbl_LeavemessageDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(sourceid));
        sb.append(", ");
        sb.append(Tools.toSqlString(sourcename));
        sb.append(", ");
        sb.append(Tools.toSqlString(title));
        sb.append(", ");
        sb.append(Tools.toSqlString(content));
        sb.append(", ");
        sb.append(Tools.toSqlString(ownerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procLeavemessageId(id);
    	
    	//处理sourceid
    	sourceid = mergeData.procPlayerId(sourceid);
    	
    	//处理sourcename
    	sourcename = mergeData.procPlayerName(sourcename);
    	
    	//处理ownerid
    	ownerid = mergeData.procPlayerId(ownerid);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getSourceid(){
        return sourceid;
    }

    public void setSourceid(int sourceid){
        this.sourceid = sourceid;
    }

    public String getSourcename(){
        return sourcename;
    }

    public void setSourcename(String sourcename){
        this.sourcename = sourcename;
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

    public int getOwnerid(){
        return ownerid;
    }

    public void setOwnerid(int ownerid){
        this.ownerid = ownerid;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }
}
