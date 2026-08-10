package com.pip.itimes.server.stage;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

public class ShoutLoader {
	public ShoutLoader(File shoutFile) throws Exception{
        XMLConfiguration config = new XMLConfiguration(shoutFile);

        loadShout(config);
        loadActivity(config);
    }

    private void loadShout (XMLConfiguration config) {
    	SubnodeConfiguration auctionConfig = config.configurationAt("Start");
    	ShoutConfig.startYear = auctionConfig.getInt("Year");
    	ShoutConfig.startMonth = auctionConfig.getInt("Month");
    	ShoutConfig.startDay = auctionConfig.getInt("Day");
        auctionConfig = config.configurationAt("End");
        ShoutConfig.endYear = auctionConfig.getInt("Year");
        ShoutConfig.endMonth = auctionConfig.getInt("Month");
        ShoutConfig.endDay = auctionConfig.getInt("Day");    	
    	
        ShoutConfig.segment = config.getInt("Segment");
        
        ShoutConfig.resetActTime();
        
        ShoutConfig.shoutActivityConfig = new ShoutActivityConfig[ShoutConfig.segment];
        for (int i = 0; i < ShoutConfig.segment; i++) {
        	ShoutConfig.shoutActivityConfig[i] = new ShoutActivityConfig();
        }
    }

    private void loadActivity (XMLConfiguration config) {
    	for (int i = 0; i < ShoutConfig.segment; i++) {
	        SubnodeConfiguration activityConfig = config.configurationAt("Activity" + (i + 1));
	        ShoutConfig.shoutActivityConfig[i].setStratMessage(activityConfig.getString("AdMessage"));
	        ShoutConfig.shoutActivityConfig[i].setEndMessage(activityConfig.getString("EndMessage"));
	        
	        SubnodeConfiguration auctionStartConfig = activityConfig.configurationAt("Start");
	        ShoutConfig.shoutActivityConfig[i].setStartHour(auctionStartConfig.getInt("Hour"));
	        ShoutConfig.shoutActivityConfig[i].setStartMinute(auctionStartConfig.getInt("Minute"));
	        SubnodeConfiguration auctionEndConfig = activityConfig.configurationAt("End");
	        ShoutConfig.shoutActivityConfig[i].setEndHour(auctionEndConfig.getInt("Hour"));
	        ShoutConfig.shoutActivityConfig[i].setEndMinute(auctionEndConfig.getInt("Minute"));
	        ShoutConfig.shoutActivityConfig[i].resetActivityTime();
	        
	        loadChat (activityConfig, i);
    	}
    }
    
    private void loadChat (SubnodeConfiguration config, int index) {
    	SubnodeConfiguration chatConfig = config.configurationAt("Chats");
        List<SubnodeConfiguration> chatList = chatConfig.configurationsAt("Chat");

        for(SubnodeConfiguration chatNode : chatList){
            ShoutChat chat = new ShoutChat();
            chat.setType(chatNode.getString("Type"));
            chat.setMessage(chatNode.getString("MessageContent"));
            chat.setCount(chatNode.getInt("Count"));
            chat.setGiftId(chatNode.getInt("GiftId"));
            chat.setSpecialId(chatNode.getInt("SpecialId"));
            chat.setMapId(chatNode.getInt("MapId"));
            
            ShoutConfig.shoutActivityConfig[index].setShoutChat(chat.getType(), chat);
        }
    }
}
