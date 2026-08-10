package com.pip.itimes.server.stage;

/**
 * @author wpjiang
 * 用于采集点的熟练度提升
 */
public  class GatherAlter{
	//整个地图都提升
	public static final int GATHER_MAP = 1;
	
	//只提升资源点
	public static final int GATHER_RESOURCE = 2;
	
	public GatherAlter(int type, int id, int count){
		this.type = type;
		this.id = id;
		this.count = count;
	}
	
	public GatherAlter() {
		// TODO Auto-generated constructor stub
	}

	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

	int type;
	int id;
	int count;
	
}

