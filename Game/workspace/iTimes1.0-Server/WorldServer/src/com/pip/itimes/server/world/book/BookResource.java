package com.pip.itimes.server.world.book;

import java.util.regex.Matcher;

public class BookResource {
	private String title;			//±êÌâ
	private String context;			//ÄÚÈÝ
	
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
}
