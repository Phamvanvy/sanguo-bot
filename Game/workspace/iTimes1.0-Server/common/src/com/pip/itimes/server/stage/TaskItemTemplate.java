package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TaskItemTemplate implements IItemTemplate {

    private String name;
    private short taskId;
    private int itemId;
    private int max;
    private String desc;

    private TaskItem item;
    
	public byte getItemSplitType() {
		return itemType;
	}

	public void setItemType(byte itemType) {
		this.itemType = itemType;
	}

/*	public byte getQuarlity() {
		return quarlity;
	}*/

	public void setQuarlity(byte quarlity) {
		this.quarlity = quarlity;
	}

	private byte itemType;
    private byte quarlity;
    
    public TaskItemTemplate(){

    }

    public void setTaskId(short taskId){
        this.taskId = taskId;
    }

    public short getTaskId() {
        return taskId;
    }

    public void setItemId(int itemId){
        this.itemId = itemId;
    }

    public int getItemId(){
        return itemId;
    }

    public int getPrice(){
        return 0;
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
        return 1;
    }

    public void setMax(int max){
        this.max = max;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getMax(){
        return max;
    }

    public String getDesc() {
        return desc;
    }

    public byte getQuality(){
    	return quarlity;
    }

    public boolean isBinded(){
        return true;
    }

    public void setBinded(boolean binded){

    }

    public byte getBindType(){
        return IItem.BIND_GET;
    }

    public short getLevel(){
        return 0;
    }

    public IItem newInstance() {
        if(item==null){
            item = new TaskItem();
            item.setDesc(desc);
            item.setItemId(itemId);
            item.setMax(max);
            item.setTaskId(taskId);
            item.setName(name);
            item.setQuality(quarlity);
            item.setItemShowType(itemType);
        }
        return item;
    }

}
