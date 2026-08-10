package com.pip.itimes.server.gift;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * @author sky
 *	装备、物品混合兑换
 */
public class ExchangeData {
	
	private Map<Integer, ExchangeGroup> exchangeMap = new HashMap<Integer, ExchangeGroup>();
	private Map<Integer, String> exchangecontentMap = new HashMap<Integer, String>();
	
	public void addExchangeData(int id ,ExchangeGroup exchangeGroup){
		exchangeMap.put(id, exchangeGroup);//id 是 group id
	}
	
	public ExchangeGroup getExchangeGroup(int id){
		return exchangeMap.get(id);
	}
	public void addExchangeContent(int id ,String content){
		exchangecontentMap.put(id, content);//id 是 group id
	}
	
	public String getExchangeContent(int id){
		return exchangecontentMap.get(id);
	}
	public Map<Integer, String> getExchangeContents(){
		return exchangecontentMap;
	}
	public int getExchangeGroupCount(){
		return exchangecontentMap.size();
	}
	public void removeExchangeData(){
		exchangeMap.clear();
		exchangecontentMap.clear();
	}
	
}
