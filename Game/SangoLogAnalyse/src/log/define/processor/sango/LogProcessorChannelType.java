package log.define.processor.sango;

import log.define.processor.LogProcessor;

public class LogProcessorChannelType extends LogProcessor{

	public LogProcessorChannelType(String id) {
		super(id);
	}

	@Override
	public String process(String data) {
	    int channel = Integer.parseInt(data);
	    String temp = data;
	    if(channel == 0){
	    	temp = "世界聊";
	    } else if(channel == 1){
	    	temp = "国家聊";
	    } else if(channel == 2){
	    	temp = "地区聊";
	    } else if(channel == 3){
	    	temp = "同乡聊";
	    } else if(channel == 4){
	    	temp = "帮派聊";
	    } else if(channel == 5){
	    	temp = "队伍聊";
	    } else if(channel == 6){
	    	temp = "私聊";
	    } else if(channel == 7){
	    	temp = "系统";
	    }
		return temp;
	}

}
