package com.pip.itimes.server.stage;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

public class TwelfthLunarLoader {
	public TwelfthLunarLoader(File shoutFile) throws Exception{
        XMLConfiguration config = new XMLConfiguration(shoutFile);

        loadTwelfthLunar(config);
        loadTop(config);
        loadActivity(config);
    }

    private void loadTwelfthLunar (XMLConfiguration config) {
    	SubnodeConfiguration auctionConfig = config.configurationAt("Start");
    	TwelfthLunarConfig.startYear = auctionConfig.getInt("Year");
    	TwelfthLunarConfig.startMonth = auctionConfig.getInt("Month");
    	TwelfthLunarConfig.startDay = auctionConfig.getInt("Day");
        auctionConfig = config.configurationAt("End");
        TwelfthLunarConfig.endYear = auctionConfig.getInt("Year");
        TwelfthLunarConfig.endMonth = auctionConfig.getInt("Month");
        TwelfthLunarConfig.endDay = auctionConfig.getInt("Day");
        TwelfthLunarConfig.endHour = auctionConfig.getInt("Hour");
        TwelfthLunarConfig.endMinute = auctionConfig.getInt("Minute");
        auctionConfig = config.configurationAt("Donate");
        TwelfthLunarConfig.donateItemId = auctionConfig.getInt("ItemId");
        TwelfthLunarConfig.donateGiftId = auctionConfig.getInt("GiftId");
        TwelfthLunarConfig.donateConsumer = auctionConfig.getInt("Consumer");
        TwelfthLunarConfig.donateBeanCount = auctionConfig.getInt("BeansCount");
    	
        TwelfthLunarConfig.segment = config.getInt("Segment");
        
        TwelfthLunarConfig.twelfthLunarActivityConfig = new TwelfthLunarActivityConfig[TwelfthLunarConfig.segment];
        for (int i = 0; i < TwelfthLunarConfig.segment; i++) {
        	TwelfthLunarConfig.twelfthLunarActivityConfig[i] = new TwelfthLunarActivityConfig();
        }
    }
    
    private void loadTop (XMLConfiguration config) {
    	SubnodeConfiguration auctionConfig = config.configurationAt("Top");
    	
    	SubnodeConfiguration topStartConfig = auctionConfig.configurationAt("TopStart");
        TwelfthLunarConfig.topStartYear = topStartConfig.getInt("Year");
    	TwelfthLunarConfig.topStartMonth = topStartConfig.getInt("Month");
    	TwelfthLunarConfig.topStartDay = topStartConfig.getInt("Day");
    	
    	SubnodeConfiguration topEndConfig = auctionConfig.configurationAt("TopEnd");
        TwelfthLunarConfig.topEndYear = topEndConfig.getInt("Year");
        TwelfthLunarConfig.topEndMonth = topEndConfig.getInt("Month");
        TwelfthLunarConfig.topEndDay = topEndConfig.getInt("Day");
        
        TwelfthLunarConfig.resetActTime();
        
        TwelfthLunarConfig.topCount = auctionConfig.getInt("TopCount");
        TwelfthLunarConfig.topPraise = auctionConfig.getInt("TopPraise");
        
        SubnodeConfiguration topGiftsConfig = auctionConfig.configurationAt("TopGifts");
        List<SubnodeConfiguration> topGiftsList = topGiftsConfig.configurationsAt("TopGift");
        for (SubnodeConfiguration topGiftsNode : topGiftsList) {
        	int rank = topGiftsNode.getInt("Rank");
        	int itemId = topGiftsNode.getInt("Giftid");
        	TwelfthLunarConfig.setTwelfthLunarConfig(rank, itemId);
        }
    }

    private void loadActivity (XMLConfiguration config) {
    	for (int i = 0; i < TwelfthLunarConfig.segment; i++) {
	        SubnodeConfiguration activityConfig = config.configurationAt("Activity" + (i + 1));
	        TwelfthLunarConfig.twelfthLunarActivityConfig[i].setStratMessage(activityConfig.getString("AdMessage"));
	        TwelfthLunarConfig.twelfthLunarActivityConfig[i].setEndMessage(activityConfig.getString("EdMessage"));
	        
	        SubnodeConfiguration auctionStartConfig = activityConfig.configurationAt("Start");
	        TwelfthLunarConfig.twelfthLunarActivityConfig[i].setStartHour(auctionStartConfig.getInt("Hour"));
	        TwelfthLunarConfig.twelfthLunarActivityConfig[i].setStartMinute(auctionStartConfig.getInt("Minute"));
	        SubnodeConfiguration auctionEndConfig = activityConfig.configurationAt("End");
	        TwelfthLunarConfig.twelfthLunarActivityConfig[i].setEndHour(auctionEndConfig.getInt("Hour"));
	        TwelfthLunarConfig.twelfthLunarActivityConfig[i].setEndMinute(auctionEndConfig.getInt("Minute"));
	        TwelfthLunarConfig.twelfthLunarActivityConfig[i].resetActivityTime();
	        SubnodeConfiguration CharityPorridgeConfig = activityConfig.configurationAt("CharityPorridge");
	        TwelfthLunar twelfthLunar = new TwelfthLunar();
            twelfthLunar.setCount(CharityPorridgeConfig.getInt("Count"));
            twelfthLunar.setGiftId(CharityPorridgeConfig.getInt("GiftId"));
            twelfthLunar.setLevel(CharityPorridgeConfig.getInt("Level"));
            TwelfthLunarConfig.twelfthLunarActivityConfig[i].setTwelfthLunar(twelfthLunar);
    	}
    }
}
