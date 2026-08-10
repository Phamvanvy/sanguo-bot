package com.pip.itimes.server.world.chr;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

public class ChristmasLoader {
	public ChristmasLoader(File chrFile) throws Exception{
        XMLConfiguration config = new XMLConfiguration(chrFile);

        loadChr(config);
        loadAuction(config);
        
        //计算当前时间段 -1为不在任何一个时间段
        ChristmasConfig.setCurrentSegment(ChristmasConfig.calcCurrentSegment());
        ChristmasConfig.setLastSegment(ChristmasConfig.currentSegment);
    }
	
	

    private void loadChr(XMLConfiguration config){
    	SubnodeConfiguration auctionConfig = config.configurationAt("Start");
    	ChristmasConfig.startYear = auctionConfig.getInt("Year");
        ChristmasConfig.startMonth = auctionConfig.getInt("Month");
        ChristmasConfig.startDay = auctionConfig.getInt("Day");
        auctionConfig = config.configurationAt("End");
        ChristmasConfig.endYear = auctionConfig.getInt("Year");
        ChristmasConfig.endMonth = auctionConfig.getInt("Month");
        ChristmasConfig.endDay = auctionConfig.getInt("Day");
        auctionConfig = config.configurationAt("Item");
        ChristmasConfig.itemid = auctionConfig.getInt("ItemId");
        
        //计算下活动时间
        ChristmasConfig.resetActTime();
        
        ChristmasConfig.segment = config.getInt("Segment");
        ChristmasConfig.auctionConfig = new ChristmasAuctionConfig[ChristmasConfig.segment];
        for(int i=0; i<ChristmasConfig.segment; i++){
        	ChristmasConfig.auctionConfig[i] = new ChristmasAuctionConfig();
        }
    }

    private void loadAuction(XMLConfiguration config){
    	for(int i=0; i<ChristmasConfig.segment; i++){
	        SubnodeConfiguration auctionConfig = config.configurationAt("Auction" + (i+1));
	        SubnodeConfiguration auctionStartConfig = auctionConfig.configurationAt("Start");
	        ChristmasConfig.auctionConfig[i].setStartHour(auctionStartConfig.getInt("Hour"));
	        ChristmasConfig.auctionConfig[i].setStartMinute(auctionStartConfig.getInt("Minute"));
	        SubnodeConfiguration auctionEndConfig = auctionConfig.configurationAt("End");
	        ChristmasConfig.auctionConfig[i].setEndHour(auctionEndConfig.getInt("Hour"));
	        ChristmasConfig.auctionConfig[i].setEndMinute(auctionEndConfig.getInt("Minute"));
	        ChristmasConfig.auctionConfig[i].setAdMessage(auctionConfig.getString("AdMessage"));
	        ChristmasConfig.auctionConfig[i].setEndMessage(auctionConfig.getString("EndMessage"));
	        ChristmasConfig.auctionConfig[i].setGiftId(auctionConfig.getInt("GiftId"));
	        ChristmasConfig.auctionConfig[i].resetTime();
    	}
    }
}
