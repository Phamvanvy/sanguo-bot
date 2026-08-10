package com.pip.itimes.server.world.lyricsSystem;

public class LyricDataServer {
	private LyricData lyricData;
	private String srcPlayerName;
	private String destPlayerName;
	private int srcServerId;
	private int destServerId;
	private String blessings;
	private int currentIndex;
	
	public void setLyricData(LyricData lyricData){
		this.lyricData = lyricData;
	}
	
	public LyricData getLyricData(){
		return lyricData;
	}
	
	public void setSrcPlayerName(String srcPlayerName){
		this.srcPlayerName = srcPlayerName;
	}
	
	public String getSrcPlayerName(){
		return srcPlayerName;
	}
	
	public void setDestPlayerName(String destPlayerName){
		this.destPlayerName = destPlayerName;
	}
	
	public String getDestPlayerName(){
		return destPlayerName;
	}
	
	public void setSrcServerId(int srcServerId){
		this.srcServerId = srcServerId;
	}
	
	public int getSrcServerId(){
		return srcServerId;
	}
	
	public void setDestServerId(int destServerId){
		this.destServerId = destServerId;
	}
	
	public int getDestServerId(){
		return destServerId;
	}
	
	public void setBlessings(String blessings){
		this.blessings = blessings;
	}
	
	public String getBlessings(){
		return blessings;
	}
	
	public void setCurrentIndex(int currentIndex){
		this.currentIndex = currentIndex;
	}
	
	public int getCurrentIndex(){
		return currentIndex;
	}
	
	public String getNextLyric(){
		String[] lyrics = lyricData.getOtherTip();
		if(lyrics == null){
			return null;
		}
		if(currentIndex < lyrics.length){
			currentIndex++;
			return lyrics[currentIndex - 1];
		}
		return null;
	}
}
