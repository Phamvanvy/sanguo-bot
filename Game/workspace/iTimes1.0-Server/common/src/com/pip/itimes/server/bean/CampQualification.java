package com.pip.itimes.server.bean;

import java.util.Date;

/**
 * 竞选资格表
 * @author leo
 *
 */
public class CampQualification implements java.io.Serializable, Comparable<CampQualification>{
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
     * 总投入荣誉
     */
    private int total;

    /**
     * 追加的荣誉
     */
    private int added;

    /**
     * 追加次数
     */
    private int addcount;

    /**
     * 角色剩余荣誉
     */
    private int remain;

    /**
     * 角色级别
     */
    private int level;
    
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

    public int getTotal(){
        return total;
    }

    public void setTotal(int total){
        this.total = total;
    }

    public int getAdded(){
        return added;
    }

    public void setAdded(int added){
        this.added = added;
    }

    public int getAddcount(){
        return addcount;
    }

    public void setAddcount(int addcount){
        this.addcount = addcount;
    }

    public int getRemain(){
        return remain;
    }

    public void setRemain(int remain){
        this.remain = remain;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }
    public boolean isValid(){
        return valid;
    }

    public void setValid(boolean valid){
        this.valid = valid;
    }

    public int compareTo(CampQualification o){
        if(playerid == o.playerid){
            return 0;
        }else{
            if(total < o.total){
                return 1;
            }else if(total > o.total){
                return -1;
            }else{
                if(createtime.getTime() < o.createtime.getTime()){
                    return 1;
                }else{
                    return -1;
                }
            }
        }
    }
}
