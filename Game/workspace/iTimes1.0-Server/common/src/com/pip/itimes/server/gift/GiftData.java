package com.pip.itimes.server.gift;


import com.pip.itimes.server.bean.Gift;


public class GiftData{
    private Gift gift;
    private int giftGroupId;

    public GiftData(Gift gift, int giftGroupId){
        this.gift = gift;
        this.giftGroupId = giftGroupId;
    }

    public Gift getGift(){
        return gift;
    }

    public GiftGroup getGiftGroup(){
        return GiftGroups.getGiftGroup(giftGroupId);
    }
    public OnlyGiftGroup getOnlyGiftGroup(){
    	return OnlyGiftGroups.getOnlyGiftGroup(giftGroupId);
    }
    public int getGiftGroupId(){
    	return giftGroupId;
    }
}
