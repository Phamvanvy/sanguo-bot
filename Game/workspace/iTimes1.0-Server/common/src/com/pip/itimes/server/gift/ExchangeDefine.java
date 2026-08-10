package com.pip.itimes.server.gift;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author sky
 *	装备、物品混合兑换
 */
public class ExchangeDefine {
	private static final Logger log = Logger.getLogger(ExchangeDefine.class);
	private static Map<Integer, ExchangeData> exchangedataMap = new HashMap<Integer, ExchangeData>();

	public static void addExchangeData(int id ,ExchangeData exchangeData){
		exchangedataMap.put(id, exchangeData);//id是 exchange id
	}
	
	public static ExchangeData getExchangeData(int id){
		return exchangedataMap.get(id);
	}
	
	public static void removeAllExchangeData(){
		exchangedataMap.clear();
	}
	
	
	
	
	
}
