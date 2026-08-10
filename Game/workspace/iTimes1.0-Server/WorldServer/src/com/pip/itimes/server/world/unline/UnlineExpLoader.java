package com.pip.itimes.server.world.unline;

import java.io.File;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

public class UnlineExpLoader {
	public UnlineExpLoader(File chrFile) throws Exception{
        XMLConfiguration config = new XMLConfiguration(chrFile);

        loadUnlineExp(config);
        loadNews(config);
    }
	
	

    private void loadUnlineExp(XMLConfiguration config){
    	UnlineExpConfig.loginMessage = config.getString("LoginMessage");
    	if(UnlineExpConfig.loginMessage != null && !UnlineExpConfig.loginMessage.equals("")){
    		UnlineExpConfig.loginMessage = "<cff0000>" + UnlineExpConfig.loginMessage + "</c>";
    	}
    	UnlineExpConfig.newCount = config.getInt("NewsCount");
    	UnlineExpConfig.news = new UnlineExpNew[UnlineExpConfig.newCount];
    	for(int i=0; i<UnlineExpConfig.newCount; i++){
    		UnlineExpConfig.news[i] = new UnlineExpNew();
        }
    }

    private void loadNews(XMLConfiguration config){
    	for(int i=0; i<UnlineExpConfig.newCount; i++){
	        SubnodeConfiguration auctionConfig = config.configurationAt("New" + (i+1));
	        SubnodeConfiguration startConfig = auctionConfig.configurationAt("Start");
        	UnlineExpConfig.news[i].setStartYear(startConfig.getInt("Year"));
        	UnlineExpConfig.news[i].setStartMonth(startConfig.getInt("Month") - 1);
        	UnlineExpConfig.news[i].setStartDay(startConfig.getInt("Day"));
        	UnlineExpConfig.news[i].setStartHour(startConfig.getInt("Hour"));
        	UnlineExpConfig.news[i].setStartMinute(startConfig.getInt("Minute"));
	        	
	        SubnodeConfiguration endConfig = auctionConfig.configurationAt("End");
        	UnlineExpConfig.news[i].setEndYear(endConfig.getInt("Year"));
        	UnlineExpConfig.news[i].setEndMonth(endConfig.getInt("Month") - 1);
        	UnlineExpConfig.news[i].setEndDay(endConfig.getInt("Day"));
        	UnlineExpConfig.news[i].setEndHour(endConfig.getInt("Hour"));
        	UnlineExpConfig.news[i].setEndMinute(endConfig.getInt("Minute"));
        	
        	UnlineExpConfig.news[i].setMessage(auctionConfig.getString("Message"));
        	
        	UnlineExpConfig.news[i].resetTime();
    	}
    }
}