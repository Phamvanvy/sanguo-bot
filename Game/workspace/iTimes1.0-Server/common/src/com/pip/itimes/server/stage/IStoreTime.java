package com.pip.itimes.server.stage;

public class IStoreTime {
	private String start;
	private String end;
	private int count;
	private int price;
	
	public IStoreTime() {
		
	}
	public IStoreTime(String start, String end, int count, int price) {
		
		this.start = start;
		this.end = end;
		this.count = count;
		this.price = price;
	}
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
	public String getEnd() {
		return end;
	}
	public void setEnd(String end) {
		this.end = end;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	
	
}
