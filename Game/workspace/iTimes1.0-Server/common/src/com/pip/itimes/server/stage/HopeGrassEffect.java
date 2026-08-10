package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class HopeGrassEffect extends Effect{

    private int itemGroupId;
    private int validTime;
    private int obsoleteTime;
    private int grassType;
    private int ratio;
    private int grouprnd;//type=1时，随机会从itemGroupId中掉落的几率
    
    public HopeGrassEffect(int grassType) {
        this.grassType = grassType;
    }

    public int getGrassType(){
        return grassType;
    }



    public byte getType(){
        return 12;
    }

    public int getValidTime() {
        return validTime;
    }

    public void setObsoleteTime(int obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }

    public void setValidTime(int validTime) {
        this.validTime = validTime;
    }

    public int getObsoleteTime() {
        return obsoleteTime;
    }

    public void setItemGroupId(int itemGroupId){
        this.itemGroupId = itemGroupId;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public int getItemGroupId(){
        return itemGroupId;
    }

    public int getRatio() {
        return ratio;
    }

	public int getGrouprnd() {
		return grouprnd;
	}

	public void setGrouprnd(int grouprnd) {
		this.grouprnd = grouprnd;
	}
    
}
