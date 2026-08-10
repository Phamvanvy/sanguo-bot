package peony.game;

public class Version {
	
	public static final int STATUS_CURRENT = 0;
	public static final int STATUS_CANCELED = 1;
	public static final int STATUS_OBSOLETE = 2;
	
	public String id;
	public int status;
	public int maxLevel;
	public String desc;
	public String message;
	public String model = "j2me"; 
	public String url; //发现时obsolete状态时需要下发的url，如果没有则不需要下发
	public String script; //发现时canceled状态时需要下发的脚本，如果没有则不需要下发
	
	public Version(String id,int status,int maxLevel,String desc,String message){
		this.id = id;
		this.status = status;
		this.maxLevel = maxLevel;
		this.desc = desc;
		this.message = message;
	}
	
	public Version(String id,int status,int maxLevel,String desc,String message,String model,String url,String script){
		this.id = id;
		this.status = status;
		this.maxLevel = maxLevel;
		this.desc = desc;
		this.message = message;
		this.model = model;
		this.url = url;
		this.script = script;
	}
}
