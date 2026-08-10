package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ExtendedItemTemplate implements IItemTemplate {

    private String name;
    private int price;
    private int itemId;
    private byte bindType;
    private boolean canUse;
    private boolean autoUse;
    private String autoUseMessage;
    private String desc;
    private Effect[] effects;
    
    public byte getItemSplitType() {
		return itemType;
	}

	public void setItemType(byte itemType) {
		this.itemType = itemType;
	}


	public void setQuarlity(byte quarlity) {
		this.quarlity = quarlity;
	}

	private byte itemType;
    private byte quarlity;
    
    
    private ExtendedItem item;

    public ExtendedItemTemplate() {
    }

    public void setItemId(int id){
        this.itemId = id;
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
        return 2;
    }

    public void setPrice(int price){
        this.price = price;
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

    public byte getBindType(){
        return bindType;
    }

    public void setBindType(byte bindType){
        this.bindType = bindType;
    }

    public boolean isBinded(){
        return bindType!=IItem.BIND_NO;
    }

    public void setBinded(boolean bind){
    }

    public void setCanUse(boolean canUse){
        this.canUse = canUse;
    }
    
    public void setAutoUse(boolean autoUse){
        this.autoUse = autoUse;
    }

    public void setAutoUseMessage(String autoUseMessage) {
        this.autoUseMessage = autoUseMessage;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean getCanUse(){
        return canUse;
    }
    
    public boolean getAutoUse(){
        return autoUse;
    }
    
    public String getAutoUseMessage(){
        return autoUseMessage;
    }

    public String getDesc() {
        return desc;
    }

    public byte getQuality(){
        return quarlity;
    }

    public short getLevel(){
        return 0;
    }

    public IItem newInstance() {
        if(item==null){
            item = new ExtendedItem();
            item.setBindType(bindType);
            item.setCanUse(canUse);
            item.setAutoUse(autoUse);
            item.setAutoUseMessage(autoUseMessage);
            item.setDesc(desc);
            item.setEffects(effects);
            item.setItemId(itemId);
            item.setName(name);
            item.setPrice(price);
            item.setQuality(quarlity);
            item.setItemShowType(itemType);
        }
        return item;
    }

}
