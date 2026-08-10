package com.pip.itimes.server.camp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Date;

import com.pip.itimes.server.util.PropertyPool;

public class CampOfficial {
	
	public static final byte POST_NULL = 0;					//无职位
	public static final byte POST_KING = 1;					//国王
	public static final byte POST_OTHER = 2;				//其它
	public static final byte POST_DUKE = 3;					//公爵
	public static final byte POST_FINANCIAL = 4;			//财政司
	public static final byte POST_KNIGHT = 5;				//骑士队长
	
	public static final byte POST_COUNT = 3;				//目前职位个数
	
	//pool about
	public static final String STR_OFFICIAL_COUNT = "OfficialCount";
	public static final String STR_OFFICIAL_PLAYERID = "OfficialPlayerID";
	public static final String STR_OFFICIAL_TASKTIME = "OfficialTaskTime";
	public static final String STR_OFFICIAL_POST = "OfficialPost";
	
	private byte post;				//官职
	private int playerid;			//玩家ID
	private long taskTime;			//任务开始时间
	
	public void setPost(byte post){
		this.post = post;
	}
	
	public byte getPost(){
		return post;
	}
	
	public void setPlayerID(int playerid){
		this.playerid = playerid;
	}
	
	public int getPlayerID(){
		return playerid;
	}
	
	public void setTaskTime(long taskTime){
		this.taskTime = taskTime; 
	}
	
	public long getTaskTime(){
		return taskTime;
	}
	
	public String getPostName(){
		return getPostName(post);
	}
	
	static public String getPostName(byte post){
		switch(post){
		case POST_DUKE:
			return "公爵";
		case POST_FINANCIAL:
			return "财政司";
		}
		return "";
	}
}
