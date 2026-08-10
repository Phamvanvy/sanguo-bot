package com.pip.itimes.server.world.book;

/**
 * 公告内容
 * @author zxyu
 *
 */
public class BookNotice {
	private String title;			//标题
	private String context;			//内容
	private long startTime;			//开始时间
	private long endTime;			//结束时间
	
	public void setTilte(String title){
		this.title = title;
	}
	
	public String getTitle(){
		return title;
	}
	
	public void setContext(String context){
		context = context.replaceAll("@c", "<c");
		context = context.replaceAll("@t", "</c");
		context = context.replaceAll("@e", ">");
		if (context.indexOf("\\n") == -1){
			this.context = context;
            return;
		}
		while(true){
			int index = context.indexOf("\\n");
			if(index == -1) break;
			if(index == 0){
				context = "\n" + context.substring(index + 2);
			}else{
				context = context.substring(0, index) + "\n" + context.substring(index + 2, context.length());
			}
		}
		this.context = context;
	}
	
	public String getContext(){
		return context;
	}
	
	public void setStartTime(long startTime){
		this.startTime = startTime;
	}
	
	public long getStartTime(){
		return startTime;
	}
	
	public void setEndTime(long endTime){
		this.endTime = endTime;
	}
	
	public long getEndTime(){
		return endTime;
	}
	
	public boolean isActioning(){
		long now = System.currentTimeMillis();
		return now >= startTime && now <= endTime;
	}
}
