package com.pip.itimes.bean;

import java.util.Date;

import com.pip.itimes.MergeData;
import com.pip.itimes.ServerConfig;
import com.pip.itimes.Tools;
import com.pip.itimes.dao.Tbl_CampcandidateDao;

public class Tbl_Campcandidate extends BaseTable{
	/*
	 CREATE TABLE `tbl_campcandidate` (
	  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号',
	  `playerid` int(11) NOT NULL COMMENT '角色id',
	  `createtime` datetime NOT NULL COMMENT '创建时间',
	  `lasttime` datetime NOT NULL COMMENT '最后操作时间',
	  `camp` tinyint(4) NOT NULL COMMENT '阵营',
	  `preking` tinyint(4) NOT NULL COMMENT '是否前国王(0: 不是, 1: 是)',
	  `totalvote` int(11) NOT NULL COMMENT '总计票数',
	  `normalvote` int(11) NOT NULL COMMENT '真心支持票数',
	  `itemvote` int(11) NOT NULL COMMENT '鲜花票数',
	  `ishopitemvote` int(11) NOT NULL COMMENT '蓝色妖姬票数',
	  `magicvote` int(11) NOT NULL COMMENT '魔力分享票数',
	  `magicremain` int(11) NOT NULL COMMENT '魔力分享剩余票数',
	  `eggvote` int(11) NOT NULL COMMENT '臭鸡蛋票数',
	  `slogan` text NOT NULL COMMENT '竞选宣言',
	  `valid` tinyint(4) NOT NULL COMMENT '是否有效',
	  PRIMARY KEY (`id`)
	) ENGINE=MyISAM AUTO_INCREMENT=133 DEFAULT CHARSET=utf8 COMMENT='候选人表';
	 */

	@Override
	public String getColumnNames() {
		return Tbl_CampcandidateDao.SQL_PARA;
	}
	
	@Override
    public String toString(){
        StringBuffer sb = new StringBuffer();

        sb.append(Tools.toSqlString(id));
        sb.append(", ");
        sb.append(Tools.toSqlString(playerid));
        sb.append(", ");
        sb.append(Tools.toSqlString(createtime));
        sb.append(", ");
        sb.append(Tools.toSqlString(lasttime));
        sb.append(", ");
        sb.append(Tools.toSqlString(camp));
        sb.append(", ");
        sb.append(Tools.toSqlString(preking));
        sb.append(", ");
        sb.append(Tools.toSqlString(totalvote));
        sb.append(", ");
        sb.append(Tools.toSqlString(normalvote));
        sb.append(", ");
        sb.append(Tools.toSqlString(itemvote));
        sb.append(", ");
        sb.append(Tools.toSqlString(ishopitemvote));
        sb.append(", ");
        sb.append(Tools.toSqlString(magicvote));
        sb.append(", ");
        sb.append(Tools.toSqlString(magicremain));
        sb.append(", ");
        sb.append(Tools.toSqlString(eggvote));
        sb.append(", ");
        sb.append(Tools.toSqlString(slogan));
        sb.append(", ");
        sb.append(Tools.toSqlString(valid));

        return sb.toString();
    }

	@Override
	public void process(MergeData mergeData, ServerConfig serverConfig) {
        //处理id
        id = mergeData.procCampcandidateId(id);
        //处理playerid
        playerid = mergeData.procPlayerId(playerid);
        //合服后从区就再也不是国王了
        preking = 0;
	}

	/**
     * 序号
     */
    private int id;

    /**
     * 角色id
     */
    private int playerid;

    /**
     * 创建时间
     */
    private Date createtime;

    /**
     * 最后操作时间
     */
    private Date lasttime;

    /**
     * 阵营
     */
    private int camp;

    /**
     * 是否前任领袖
     */
    private int preking;

    /**
     * 总票数
     */
    private int totalvote;

    /**
     * 真心支持票数
     */
    private int normalvote;

    /**
     * 鲜花票数
     */
    private int itemvote;

    /**
     * 蓝色妖姬票数
     */
    private int ishopitemvote;

    /**
     * 魔力分享票数
     */
    private int magicvote;

    /**
     * 魔力分享剩余票数
     */
    private int magicremain;

    /**
     * 臭鸡蛋票数
     */
    private int eggvote;

    /**
     * 竞选宣言
     */
    private String slogan;

    /**
     * 是否有效
     */
    private int valid;

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

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public Date getLasttime(){
        return lasttime;
    }

    public void setLasttime(Date lasttime){
        this.lasttime = lasttime;
    }

    public int getCamp(){
        return camp;
    }

    public void setCamp(int camp){
        this.camp = camp;
    }

    public int getPreking(){
        return preking;
    }

    public void setPreking(int preking){
        this.preking = preking;
    }

    public int getTotalvote(){
        return totalvote;
    }

    public void setTotalvote(int totalvote){
        this.totalvote = totalvote;
    }

    public int getNormalvote(){
        return normalvote;
    }

    public void setNormalvote(int normalvote){
        this.normalvote = normalvote;
    }

    public int getItemvote(){
        return itemvote;
    }

    public void setItemvote(int itemvote){
        this.itemvote = itemvote;
    }

    public int getIshopitemvote(){
        return ishopitemvote;
    }

    public void setIshopitemvote(int ishopitemvote){
        this.ishopitemvote = ishopitemvote;
    }

    public int getMagicvote(){
        return magicvote;
    }

    public void setMagicvote(int magicvote){
        this.magicvote = magicvote;
    }

    public int getMagicremain(){
        return magicremain;
    }

    public void setMagicremain(int magicremain){
        this.magicremain = magicremain;
    }

    public int getEggvote(){
        return eggvote;
    }

    public void setEggvote(int eggvote){
        this.eggvote = eggvote;
    }

    public String getSlogan(){
        return slogan;
    }

    public void setSlogan(String slogan){
        this.slogan = slogan;
    }

    public int getValid(){
        return valid;
    }

    public void setValid(int valid){
        this.valid = valid;
    }
}
