package com.pip.itimes.server.stage;

import java.io.File;
import java.util.List;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import com.pip.itimes.server.util.Utils;

public class CampBuffLoader {
	public CampBuffLoader(File CampBuffFile) throws Exception {
        XMLConfiguration config = new XMLConfiguration(CampBuffFile);
        
        loadBuff(config);
    }
	
    private void loadBuff(XMLConfiguration config) {
    	CampBuffConfig.campBrightBuff.clear();
    	CampBuffConfig.campDarkBuff.clear();
    	
    	SubnodeConfiguration victoryConfig = config.configurationAt("Wins");
    	List<SubnodeConfiguration> victoryList = victoryConfig.configurationsAt("Win");
    	for (SubnodeConfiguration victoryNode : victoryList) {
    		
    		int campType = victoryNode.getInt("Camp");
    		List<SubnodeConfiguration> buffList = victoryNode.configurationsAt("Buff");
    		
    		for (SubnodeConfiguration buffNode : buffList) {
    			
    			int day = buffNode.getInt("Week");
    			CampBuff buff = new CampBuff();
    			buff.setProperty(buffNode.getInt("Property"));
    			buff.setValue(buffNode.getInt("Value"));
    			buff.setTime(buffNode.getInt("Second"));
    			buff.setUnit(buffNode.getByte("Unit"));
    			buff.setMessage(buffNode.getString("Message"));
    			
    			if (campType == Utils.CAMP_BRIGHT) {
    				CampBuffConfig.campBrightBuff.put(day, buff);
    			} else if (campType == Utils.CAMP_DARK) {
    				CampBuffConfig.campDarkBuff.put(day, buff);
    			}
    			
    		}
    	}
    }
}
