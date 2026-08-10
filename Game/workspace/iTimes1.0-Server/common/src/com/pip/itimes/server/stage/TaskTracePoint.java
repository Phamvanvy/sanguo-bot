package com.pip.itimes.server.stage;

/**
 * 任务中的一个寻路点。
 */
public class TaskTracePoint {
	// 寻路点名称
	public String name;
	// 目标地图ID
	public short mapID;
	// 目标X位置（像素）
	public short x;
	// 目标Y位置（像素）
	public short y;
	// 目标X位置（格点）
	public short gridx;
	// 目标Y位置（格点）
	public short gridy;
	
	public TaskTracePoint(String n, short mid, short x, short y, short gx, short gy) {
		this.name = n;
		this.mapID = mid;
		this.x = x;
		this.y = y;
		this.gridx = gx;
		this.gridy = gy;
	}
}
