package com.pip.itimes.server.bean;

import java.util.Date;

/**
 * 阵营表
 * @author leo
 *
 */
public class Camp implements java.io.Serializable{
    /**
     * 序号
     */
    private int id;

    /**
     * 阵营
     */
    private int camp;

    /**
     * 领袖id
     */
    private int kingid;

    /**
     * 创建时间
     */
    private Date createtime;
    
    /**
     * 最后操作时间
     */
    private Date lasttime;

    /**
     * 阵营资金
     */
    private long money;

    /**
     * 当前税率
     */
    private int taxrate;

    /**
     * 阵营科技
     */
    private byte[] skills;

    /**
     * 阵营公告
     */
    private String slogan;

    /**
     * 参数池
     */
    private String pool;

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

    public int getCamp(){
        return camp;
    }

    public void setCamp(int camp){
        this.camp = camp;
    }

    public int getKingid(){
        return kingid;
    }

    public void setKingid(int kingid){
        this.kingid = kingid;
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

    public long getMoney(){
        return money;
    }

    public void setMoney(long money){
        this.money = money;
    }

    public int getTaxrate(){
        return taxrate;
    }

    public void setTaxrate(int taxrate){
        this.taxrate = taxrate;
    }

    public byte[] getSkills(){
        return skills;
    }

    public void setSkills(byte[] skills){
        this.skills = skills;
    }

    public String getSlogan(){
        return slogan;
    }

    public void setSlogan(String slogan){
        this.slogan = slogan;
    }

    public String getPool(){
        return pool;
    }

    public void setPool(String pool){
        this.pool = pool;
    }

    public boolean isValid(){
        return valid;
    }

    public void setValid(boolean valid){
        this.valid = valid;
    }
}
