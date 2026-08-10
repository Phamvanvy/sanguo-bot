package com.pip.itimes.server.world.taskRequest;

public class TaskRequest {
	public static final byte TYPE_NULL = 0;
	public static final byte TYPE_MAILITEM = 1;		//通过邮件给玩家发送物品
	
	private byte type;
	private boolean endRequest;		//是否任务结束时展示要求 true 结束时 false 接任务后
	private short id;
	
	//邮件发送物品相关
	private String mail_title;
	private String mail_context;
	private int mail_itemid;
	private int mail_itemcount;
	private boolean mail_new;
	private String mail_newText;
	private boolean mail_openui;
	
	public void setType(byte type){
		this.type = type;
	}
	
	public byte getType(){
		return type;
	}
	
	public void setEndRequest(boolean endRequest){
		this.endRequest = endRequest;
	}
	
	public boolean isEndRequest(){
		return endRequest;
	}
	
	public void setID(short id){
		this.id = id;
	}
	
	public short getID(){
		return id;
	}
	
	public void setMailTitle(String mail_title){
		this.mail_title = mail_title;
	}
	
	public String getMailTitle(){
		return mail_title;
	}
	
	public void setMailContext(String mail_context){
		this.mail_context = mail_context;
	}
	
	public String getMailContext(){
		return mail_context;
	}
	
	public void setMailItemID(int mail_itemid){
		this.mail_itemid = mail_itemid;
	}
	
	public int getMailItemId(){
		return mail_itemid;
	}
	
	public void setMailItemCount(int mail_itemcount){
		this.mail_itemcount = mail_itemcount;
	}
	
	public int getMailItemCount(){
		return mail_itemcount;
	}
	
	public void setMailNew(boolean mail_new){
		this.mail_new = mail_new;
	}
	
	public boolean isMailNew(){
		return mail_new;
	}
	
	public void setMailNewText(String mail_newText){
		this.mail_newText = mail_newText;
	}
	
	public String getMailNewText(){
		return mail_newText;
	}
	
	public void setMailOpenUI(boolean mail_openui){
		this.mail_openui = mail_openui;
	}
	
	public boolean isMailOpenUI(){
		return mail_openui;
	}
	
	static public byte getType(String strType){
		if(strType.equals("mailItem")){
			return TYPE_MAILITEM;
		}
		return TYPE_NULL;
	}
	
}
