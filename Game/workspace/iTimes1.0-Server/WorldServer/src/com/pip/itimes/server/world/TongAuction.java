package com.pip.itimes.server.world;

public class TongAuction {

    private int tongId;
    private String tongName;
    private int price;
    private int islandId;
    private int priceDiff;

    public TongAuction(int tongId,String tongName,int price,int islandId,int priceDiff) {
        this.tongId = tongId;
        this.tongName = tongName;
        this.price = price;
        this.islandId = islandId;
        this.priceDiff = priceDiff;
    }

    public String getTongName() {
        return tongName;
    }

    public int getTongId() {
        return tongId;
    }

    public int getPrice() {
        return price;
    }

    public int getIslandId(){
        return islandId;
    }

    public int getPriceDiff(){
        return priceDiff;
    }
}
