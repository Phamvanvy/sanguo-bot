package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_TongDao;

public class Tbl_Tong extends BaseTable{
    /*
    CREATE TABLE `tbl_tong` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `tongname` varchar(255) NOT NULL DEFAULT '',
      `createtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `owner` int(11) NOT NULL DEFAULT '0',
      `slogan` mediumtext,
      `level` int(11) NOT NULL DEFAULT '0',
      `money` int(11) NOT NULL DEFAULT '0',
      `resource` int(11) NOT NULL DEFAULT '0',
      `health` int(11) NOT NULL DEFAULT '0',
      `lastrepairtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
      `memebercount` int(11) NOT NULL DEFAULT '0',
      `credit` int(11) NOT NULL DEFAULT '0',
      `toplisthot` int(11) NOT NULL DEFAULT '0',
      `toplistonline` int(11) NOT NULL DEFAULT '0',
      `leastcredit` int(11) NOT NULL DEFAULT '0',
      PRIMARY KEY (`id`),
      KEY `index_tong_toplisthot` (`toplisthot`),
      KEY `index_tong_toplistonline` (`toplistonline`),
      KEY `tongname` (`tongname`)
    ) ENGINE=MyISAM AUTO_INCREMENT=797 DEFAULT CHARSET=utf8;
    */
    private int id;
    private String tongname;
    private Date createtime;
    private int owner;
    private String slogan;
    private int level;
    private int money;
    private int resource;
    private int health;
    private Date lastrepairtime;
    private int memebercount;
    private int credit;
    private int toplisthot;
    private int toplistonline;
    private int leastcredit;

    @Override
    public String getColumnNames(){
        return Tbl_TongDao.SQL_PARA;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(tongname));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(owner));
        sb.append(", ");
        sb.append(Tools.toSqlString(slogan));
        sb.append(", ");
        sb.append(Tools.toSqlString(level));
        sb.append(", ");
        sb.append(Tools.toSqlString(money));
        sb.append(", ");
        sb.append(Tools.toSqlString(resource));
        sb.append(", ");
        sb.append(Tools.toSqlString(health));
        sb.append(", ");
        sb.append(Tools.toSqlString(lastrepairtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(memebercount));
        sb.append(", ");
        sb.append(Tools.toSqlString(credit));
        sb.append(", ");
        sb.append(Tools.toSqlString(toplisthot));
        sb.append(", ");
        sb.append(Tools.toSqlString(toplistonline));
        sb.append(", ");
        sb.append(Tools.toSqlString(leastcredit));

        return sb.toString();
    }

    @Override
    public void process(MergeData mergeData, ServerConfig serverConfig){
        //处理id
    	id = mergeData.procTongId(id);
    	
    	//处理tongname
    	tongname = mergeData.procTongName(tongname);
    	
    	//处理owner
    	owner = mergeData.procPlayerId(owner);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getTongname(){
        return tongname;
    }

    public void setTongname(String tongname){
        this.tongname = tongname;
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

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public int getMoney(){
        return money;
    }

    public void setMoney(int money){
        this.money = money;
    }

    public int getResource(){
        return resource;
    }

    public void setResource(int resource){
        this.resource = resource;
    }

    public int getHealth(){
        return health;
    }

    public void setHealth(int health){
        this.health = health;
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

    public int getCredit(){
        return credit;
    }

    public void setCredit(int credit){
        this.credit = credit;
    }

    public int getToplisthot(){
        return toplisthot;
    }

    public void setToplisthot(int toplisthot){
        this.toplisthot = toplisthot;
    }

    public int getToplistonline(){
        return toplistonline;
    }

    public void setToplistonline(int toplistonline){
        this.toplistonline = toplistonline;
    }

    public int getLeastcredit(){
        return leastcredit;
    }

    public void setLeastcredit(int leastcredit){
        this.leastcredit = leastcredit;
    }
}
