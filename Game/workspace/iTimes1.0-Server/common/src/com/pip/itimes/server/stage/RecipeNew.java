package com.pip.itimes.server.stage;

import java.util.ArrayList;
import java.util.List;

public class RecipeNew implements Comparable<RecipeNew> {

	 private int id;
	 private byte type;
	 private String name;
	 private short level;
	 private short skillLevel;
	 private String desc;
	 private List resources = new ArrayList();
	 private List products = new ArrayList();
	 private boolean playeGame;
	 private int money;
	 private int productivity;
	 private int equType;						//1:ÎäÆ÷£»2£º·À¾ß ;3:ÊÎÆ·
	 private int color;						//ÑÕÉ«0:°×£»1£ºÂÌ ;2:À¶
	 private int itemId;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public short getLevel() {
		return level;
	}
	public void setLevel(short level) {
		this.level = level;
	}
	public short getSkillLevel() {
		return skillLevel;
	}
	public void setSkillLevel(short skillLevel) {
		this.skillLevel = skillLevel;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	 public void addResource(IItemTemplate resource,byte count,int consumeMode){
		 TemplateGridNew grid = new TemplateGridNew(resource,count,consumeMode);
	    resources.add(grid);
	 }

	 public TemplateGridNew[] getResources(){
		 TemplateGridNew[] ret = new TemplateGridNew[resources.size()];
	     resources.toArray(ret);
	     return ret;
	 }

	 public void addProduct(IItemTemplate product,byte count){
	     TemplateGrid grid = new TemplateGrid(product,count);
	     products.add(grid);
	 }

	 public TemplateGrid[] getProducts(){
	     TemplateGrid[] ret = new TemplateGrid[products.size()];
	     products.toArray(ret);
	     return ret;
	 }
	public boolean isPlayeGame() {
		return playeGame;
	}
	public void setPlayeGame(boolean playeGame) {
		this.playeGame = playeGame;
	}
	public int getMoney() {
		return money;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	public int getProductivity() {
		return productivity;
	}
	public void setProductivity(int productivity) {
		this.productivity = productivity;
	}
	public int getEquType() {
		return equType;
	}
	public void setEquType(int equType) {
		this.equType = equType;
	}
	
	public int getColor() {
		return color;
	}
	public void setColor(int color) {
		this.color = color;
	}
	
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	
	public int compareTo(RecipeNew o) {
		return 0;
	}
	 
}