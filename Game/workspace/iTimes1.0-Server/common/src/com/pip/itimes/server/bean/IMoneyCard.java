package com.pip.itimes.server.bean;

import java.util.Date;

public class IMoneyCard implements java.io.Serializable{
    private int id;
    private int createaccountid;
    private int createplayerid;
    private Date createtime;
    private int useaccountid;
    private int useplayerid;
    private Date usetime;
    private String cardno;
    private String password;
    private int amount;
    private int status;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getCreateaccountid(){
        return createaccountid;
    }

    public void setCreateaccountid(int createaccountid){
        this.createaccountid = createaccountid;
    }

    public int getCreateplayerid(){
        return createplayerid;
    }

    public void setCreateplayerid(int createplayerid){
        this.createplayerid = createplayerid;
    }

    public Date getCreatetime(){
        return createtime;
    }

    public void setCreatetime(Date createtime){
        this.createtime = createtime;
    }

    public int getUseaccountid(){
        return useaccountid;
    }

    public void setUseaccountid(int useaccountid){
        this.useaccountid = useaccountid;
    }

    public int getUseplayerid(){
        return useplayerid;
    }

    public void setUseplayerid(int useplayerid){
        this.useplayerid = useplayerid;
    }

    public Date getUsetime(){
        return usetime;
    }

    public void setUsetime(Date usetime){
        this.usetime = usetime;
    }

    public String getCardno(){
        return cardno;
    }

    public void setCardno(String cardno){
        this.cardno = cardno;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public int getAmount(){
        return amount;
    }

    public void setAmount(int amount){
        this.amount = amount;
    }

    public int getStatus(){
        return status;
    }

    public void setStatus(int status){
        this.status = status;
    }
}
