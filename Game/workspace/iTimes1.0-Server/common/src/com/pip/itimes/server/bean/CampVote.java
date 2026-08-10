package com.pip.itimes.server.bean;

import java.util.Date;

/**
 * 竞选投票表
 * @author leo
 *
 */
public class CampVote implements java.io.Serializable{
    /**
     * 序号
     */
    private int id;

    /**
     * 角色id
     */
    private int playerid;

    /**
     * 阵营
     */
    private int camp;

    /**
     * 创建时间
     */
    private Date createtime;

    /**
     * 最后操作时间
     */
    private Date lasttime;

    /**
     * 总计票数
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
     * 臭鸡蛋票数
     */
    private int eggvote;

    /**
     * 是否有效
     */
    private boolean valid;

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

    public int getCamp(){
        return camp;
    }

    public void setCamp(int camp){
        this.camp = camp;
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

    public int getEggvote(){
        return eggvote;
    }

    public void setEggvote(int eggvote){
        this.eggvote = eggvote;
    }

    public boolean isValid(){
        return valid;
    }

    public void setValid(boolean valid){
        this.valid = valid;
    }
}
