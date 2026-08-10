package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_ArenateamDao;

public class Tbl_Arenateam extends BaseTable{
    /*
    CREATE TABLE `tbl_arenateam` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `type` int(11) NOT NULL DEFAULT '0',
      `arenaname` varchar(255) NOT NULL DEFAULT '',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `owner` int(11) NOT NULL DEFAULT '0',
      `slogan` text,
      `arenalevel` int(11) NOT NULL DEFAULT '0',
      `lastrepairtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `memebercount` int(11) NOT NULL DEFAULT '0',
      `valid` tinyint(4) NOT NULL DEFAULT '1',
      PRIMARY KEY (`id`),
      KEY `index_arenateam_arenalevel` (`arenalevel`)
    ) ENGINE=MyISAM AUTO_INCREMENT=372 DEFAULT CHARSET=utf8;
    */
    private int id;
    private int type;
    private String arenaname;
    private Date createtime;
    private int owner;
    private String slogan;
    private int arenalevel;
    private Date lastrepairtime;
    private int memebercount;
    private int valid;

    @Override
    public String getColumnNames(){
        return Tbl_ArenateamDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(type));
        sb.append(", ");
        sb.append(Tools.toSqlString(arenaname));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(owner));
        sb.append(", ");
        sb.append(Tools.toSqlString(slogan));
        sb.append(", ");
        sb.append(Tools.toSqlString(arenalevel));
        sb.append(", ");
        sb.append(Tools.toSqlString(lastrepairtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(memebercount));
        sb.append(", ");
        sb.append(Tools.toSqlString(valid));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
        id = mergeData.procArenaTeamId(id);
        
        //处理arenaname
        arenaname = mergeData.procArenaTeamName(arenaname);
        
        //处理owner
        owner = mergeData.procPlayerId(owner);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type = type;
    }

    public String getArenaname(){
        return arenaname;
    }

    public void setArenaname(String arenaname){
        this.arenaname = arenaname;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public int getOwner(){
        return owner;
    }

    public void setOwner(int owner){
        this.owner = owner;
    }

    public String getSlogan(){
        return slogan;
    }

    public void setSlogan(String slogan){
        this.slogan = slogan;
    }

    public int getArenalevel(){
        return arenalevel;
    }

    public void setArenalevel(int arenalevel){
        this.arenalevel = arenalevel;
    }

    public Date getLastrepairtime(){
        return lastrepairtime;
    }

    public void setLastrepairtime(Date lastrepairtime){
        this.lastrepairtime = lastrepairtime;
    }

    public int getMemebercount(){
        return memebercount;
    }

    public void setMemebercount(int memebercount){
        this.memebercount = memebercount;
    }

    public int getValid(){
        return valid;
    }

    public void setValid(int valid){
        this.valid = valid;
    }
}
