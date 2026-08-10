package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_VoteDao;

public class Tbl_Vote extends BaseTable{
    /*
    CREATE TABLE `tbl_vote` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `votersid` int(11) NOT NULL DEFAULT '0',
      `playeridvoters` int(11) NOT NULL DEFAULT '0',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `votepoint` int(11) NOT NULL DEFAULT '0',
      `type` int(11) NOT NULL DEFAULT '0',
      `valid` tinyint(4) NOT NULL DEFAULT '1',
      `isimoneyitem` tinyint(4) NOT NULL DEFAULT '0',
      PRIMARY KEY (`id`),
      KEY `type_index` (`type`),
      KEY `votersid_index` (`votersid`),
      KEY `playeridvoters_index` (`playeridvoters`)
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8;
    */
    private int id;
    private int votersid;
    private int playeridvoters;
    private Date createtime;
    private int votepoint;
    private int type;
    private int valid;
    private int isimoneyitem;

    @Override
    public String getColumnNames(){
        return Tbl_VoteDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(votersid));
        sb.append(", ");
        sb.append(Tools.toSqlString(playeridvoters));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(votepoint));
        sb.append(", ");
        sb.append(Tools.toSqlString(type));
        sb.append(", ");
        sb.append(Tools.toSqlString(valid));
        sb.append(", ");
        sb.append(Tools.toSqlString(isimoneyitem));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procVoteId(id);
    	
    	//处理votersid
    	votersid = mergeData.procPlayerId(votersid);
    	
    	//处理playeridvoters
    	playeridvoters = mergeData.procPlayerId(playeridvoters);
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

    public int getPlayeridvoters(){
        return playeridvoters;
    }

    public void setPlayeridvoters(int playeridvoters){
        this.playeridvoters = playeridvoters;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public int getVotepoint(){
        return votepoint;
    }

    public void setVotepoint(int votepoint){
        this.votepoint = votepoint;
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
    
    public int getIsimoneyitem(){
    	return isimoneyitem;
    }
    
    public void setIsimoneyitem(int isimoneyitem){
    	this.isimoneyitem = isimoneyitem;
    }
}
