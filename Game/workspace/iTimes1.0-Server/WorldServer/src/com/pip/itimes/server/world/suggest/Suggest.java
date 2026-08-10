package com.pip.itimes.server.world.suggest;

import java.util.Vector;

public class Suggest {

	public static Vector suggest = new Vector();
	
	public int[] map;
	public Vector level = new Vector();
	//游戏建议中的游戏公告
	public static Vector<String> gameNotice = new Vector<String>();
	public static Vector<String> getGameNotice() {
		return gameNotice;
	}
	public static void addGmameNotice(String notice){
		gameNotice.add(notice);
	}
	//游戏建议相关等级的关卡id和名称
	public static Vector levelArea = new Vector();
	
	//游戏小窍门
	public static Vector<String> gameTip = new Vector<String>();
	
	public static void addGmametip(String tip){
		gameTip.add(tip);
	}
	public static Vector<String> getGameTip() {
		return gameTip;
	}
	//游戏玩点及可玩性介绍
	public static Vector<String> gamePlay = new Vector<String>();
	public static Vector getGamePlay() {
		return gamePlay;
	}
	public static void addGamePlay(String gamePlayString){
		gamePlay.add(gamePlayString);
	}
	public static Vector getGameContents() {
		return gameContents;
	}
	public static Vector<String> gameContents = new Vector<String>();
	

	public static void addgameContents(String contents){
		gameContents.add(contents);
	}
	public Suggest(int m[],Vector l) {
		map = m;
		level = l;
	}
	
	//游戏账户提示，1为修改密码提示，2为找回账号提示
	public static   Vector<String> gameSafeInfo = new Vector<String>();
	public static Vector<String> getGameSafeInfo() {
		return gameSafeInfo;
	}
	public static void addGameSafeInfo(String gameInfo){
		gameSafeInfo.add(gameInfo);
	}
	public static void addSuggest(int m[],Vector l) {
		Suggest s = new Suggest(m,l);
		suggest.addElement(s);
	}
	public static void addLevelArea( Object[] l) {
		levelArea.add(l);
	}
	public static Vector getLevelAreaId(int level){
		Vector levelAreaVector = null;
		for(int i = 0; i < levelArea.size(); i++ ){
			Object[] temp = (Object[]) levelArea.get(i);
			int beginLevel =(Integer) temp[0];
			int endLevel = (Integer) temp[1];
			if(level >= beginLevel && level <= endLevel){
				levelAreaVector = (Vector) temp[2];
			}
		}
		return levelAreaVector;
	}
}
