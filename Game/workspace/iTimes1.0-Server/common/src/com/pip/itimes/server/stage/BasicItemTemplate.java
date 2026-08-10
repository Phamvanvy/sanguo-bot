package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class BasicItemTemplate implements IItemTemplate{

    private byte function;
    private String name;
    private int price;
    private int itemId;
    private byte bindType;
    private boolean binded;
    private Effect[] effects;
    private String desc;
    private int credit;

    private BasicItem item;
    
    
    public byte getItemSplitType() {
		return itemType;
	}

	public void setItemType(byte itemType) {
		this.itemType = itemType;
	}

	public byte getQuality(){
	    return quarlity;
	}

	public void setQuarlity(byte quarlity) {
		this.quarlity = quarlity;
	}

	private byte itemType;
    private byte quarlity;
    
    public BasicItemTemplate() {
    }

    public void setFunction(byte function){
        this.function = function;
    }

    public byte getFunction() {
        return function;
    }

    public void setItemId(int itemId){
        this.itemId = itemId;
    }

    public int getItemId(){
        return itemId;
    }


    public int getId() {
        return 0;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }



    public byte getType() {
        return 0;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public byte getBindType(){
        return bindType;
    }

    public void setBindType(byte bindType){
        this.bindType = bindType;
    }

    public boolean isBinded(){
        return binded;
    }

    public void setBinded(boolean binded){
        this.binded = binded;
    }

    public int getPrice() {
        return price;
    }

   

    public Effect[] getEffects(){
        return effects;
    }

    public void setEffects(Effect[] effects){
        this.effects = effects;
    }

    public String getDesc(){
        return desc;
    }

    public int getCredit() {
        return credit;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public short getLevel(){
        return 0;
    }

    public IItem newInstance(){
        if(item==null){
            item = new BasicItem();
            item.setBinded(binded);
            item.setItemId(itemId);
            item.setBindType(bindType);
            item.setDesc(desc);
            item.setEffects(effects);
            item.setFunction(function);
            item.setName(name);
            item.setPrice(price);
            item.setItemShowType(itemType);
            item.setQuarlity(quarlity);
        }
        return item;
    }
}
