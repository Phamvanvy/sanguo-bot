package com.pip.itimes.server.stage;

import java.util.ArrayList;

public class IStoreItem {
    public IItemTemplate item;
    public int price;
    public int count;
    public String desc;
    public String consumeCode;
    public int discount;		//折扣值 百分比
    public int contribute;     //公会贡献值
    /*折扣商店用 */
    public int iPrice; //打折后的i币价格
    public int jPrice; //j币价格
    public int credit; //荣誉
    //限时抢购
    
    public ArrayList<IStoreTime> times = new ArrayList<IStoreTime>();
}
