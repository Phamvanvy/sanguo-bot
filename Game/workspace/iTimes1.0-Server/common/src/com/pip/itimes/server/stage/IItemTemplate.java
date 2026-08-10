package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface IItemTemplate {

	//其中getQuality为所用物品共用的，，非装备代表颜色值
    public int getItemId();
    public String getName();
    public short getLevel();
    public byte getQuality();
    public byte getType();
    public int getPrice();
    public String getDesc();

    public IItem newInstance();
    
    public byte getItemSplitType();
}
