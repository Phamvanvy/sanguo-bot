package com.pip.itimes.server.world.book;

public class BookAction extends BookNotice{
	private short mapid;
	private short row;
	private short col;
	private int levelmax;
	private int levelmin;
	
	public void setMapID(short mapid){
		this.mapid = mapid;
	}
	public short getMapID(){
		return mapid;
	}
	
	public void setPostion(short row, short col){
		this.row = row;
		this.col = col;
	}
	public void setRow(short row){
		this.row = row;
	}
	public void setCol(short col){
		this.col = col;
	}
	public short getRow(){
		return row;
	}
	public short getCol(){
		return col;
	}
	
	public void setLevel(int levelmin, int levelmax){
		this.levelmin = levelmin;
		this.levelmax = levelmax;
	}
	public void setLevelMax(int levelmax){
		this.levelmax = levelmax;
	}
	public void setLevelMin(int levelmin){
		this.levelmin = levelmin;
	}
	public int getLevelMax(){
		return levelmax;
	}
	public int getLevelMin(){
		return levelmin;
	}
	
}
