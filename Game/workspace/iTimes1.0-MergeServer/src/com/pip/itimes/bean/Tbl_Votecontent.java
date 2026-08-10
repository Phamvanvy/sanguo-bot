package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_VotecontentDao;

public class Tbl_Votecontent extends BaseTable{
    /*
    CREATE TABLE `tbl_votecontent` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `votersid` int(11) NOT NULL DEFAULT '0',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `type` int(11) NOT NULL DEFAULT '0',
      `valid` tinyint(4) NOT NULL DEFAULT '1',
      `content` text,
      PRIMARY KEY (`id`),
      KEY `group_index` (`votersid`)
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8;
    */
    private int id;
    private int votersid;
    private Date createtime;
    private int type;
    private int valid;
    private String content;

    @Override
    public String getColumnNames(){
        return Tbl_VotecontentDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(votersid));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(type));
        sb.append(", ");
        sb.append(Tools.toSqlString(valid));
        sb.append(", ");
        sb.append(Tools.toSqlString(content));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procVotecontentId(id);
    	
    	//处理votersid
    	votersid = mergeData.procPlayerId(votersid);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getVotersid(){
        return votersid;
    }

    public void setVotersid(int votersid){
        this.votersid = votersid;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type = type;
    }

    public int getValid(){
        return valid;
    }

    public void setValid(int valid){
        this.valid = valid;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }
}
