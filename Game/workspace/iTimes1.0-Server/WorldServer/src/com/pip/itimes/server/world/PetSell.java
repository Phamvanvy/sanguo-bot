package com.pip.itimes.server.world;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class PetSell {

    private int id;
    private int srcId;
    private int destId;
    private int petId;
    private long sellTime;
    private int money;
    private String petName;

    public PetSell(int id,int srcId,int destId,int petId,int money,long sellTime, String petName) {
        this.id = id;
        this.srcId = srcId;
        this.destId = destId;
        this.petId = petId;
        this.money = money;
        this.sellTime = sellTime;
        this.petName = petName;
    }

    public int getSrcId() {
        return srcId;
    }

    public long getSellTime() {
        return sellTime;
    }

    public void setDestId(int destId) {
        this.destId = destId;
    }

    public void setSrcId(int srcId) {
        this.srcId = srcId;
    }

    public void setSellTime(long sellTime) {
        this.sellTime = sellTime;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getDestId() {
        return destId;
    }

    public int getPetId() {
        return petId;
    }

    public int getId() {
        return id;
    }

    public int getMoney() {
        return money;
    }
    
    public String getPetName(){
    	return petName;
    }
}
