package com.pip.itimes.server.gift;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sky
 *	装备、物品混合兑换 数据
 */
public class ExchangeGroup {
	private int id;
	
	private Map<Integer, Integer> needEquMap = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> giveItemMap = new HashMap<Integer, Integer>();
	private int type;
	private int beginlevel;
	private int endlevel;
	private int needitem;//需要兑换的附加物品（此物品唯一种类不可多个）
	private int needitemcount;//需要兑换的附加物品数量
	private String content;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Map<Integer, Integer> getNeedEquMap() {
		return needEquMap;
	}
	public void setNeedEquMap(Map<Integer, Integer> needEquMap) {
		this.needEquMap = needEquMap;
	}
	public Map<Integer, Integer> getGiveItemMap() {
		return giveItemMap;
	}
	public void setGiveItemMap(Map<Integer, Integer> giveItemMap) {
		this.giveItemMap = giveItemMap;
	}
	public int getBeginlevel() {
		return beginlevel;
	}
	public void setBeginlevel(int beginlevel) {
		this.beginlevel = beginlevel;
	}
	public int getEndlevel() {
		return endlevel;
	}
	public void setEndlevel(int endlevel) {
		this.endlevel = endlevel;
	}
	public int getNeeditem() {
		return needitem;
	}
	public void setNeeditem(int needitem) {
		this.needitem = needitem;
	}
	
	public int getNeeditemcount() {
		return needitemcount;
	}
	public void setNeeditemcount(int needitemcount) {
		this.needitemcount = needitemcount;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	
}
