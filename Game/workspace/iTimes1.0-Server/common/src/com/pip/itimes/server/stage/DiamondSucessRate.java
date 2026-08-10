package com.pip.itimes.server.stage;

import java.util.HashMap;
import java.util.Map;

public class DiamondSucessRate {
	/**
	 * ¼¸ÂÊ±í
	 */
	public   Map diamondSuccessRateMap = new HashMap();
	
	public void addDiamondSuccessRateMap(int id, int rate){
		diamondSuccessRateMap.put(id, rate);
	}
	
	public Map getDiamondSuccessRateMap(){
		return diamondSuccessRateMap;
	}
	
}
