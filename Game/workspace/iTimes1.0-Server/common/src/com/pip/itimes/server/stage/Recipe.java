package com.pip.itimes.server.stage;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffery
 * @version 1.0
 */
public class Recipe implements Comparable<Recipe>{
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

    public Recipe() {

    }

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setType(byte type){
        this.type = type;
    }

    public byte getType(){
        return type;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setLevel(short level){
        this.level = level;
    }

    public short getLevel(){
        return level;
    }

    public void setSkillLevel(short skillLevel){
        this.skillLevel = skillLevel;
    }

    public short getSkillLevel(){
        return skillLevel;
    }

    public void addResource(IItemTemplate resource,byte count){
        TemplateGrid grid = new TemplateGrid(resource,count);
        resources.add(grid);
    }

    public TemplateGrid[] getResources(){
        TemplateGrid[] ret = new TemplateGrid[resources.size()];
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

    public String getDesc(){
        return desc;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public void setPlayeGame(boolean playGame){
        this.playeGame = playGame;
    }

    public boolean getPlayeGame(){
        return playeGame;
    }

    public int getProductivity(){
        return productivity;
    }

    public void setProducitivity(int productivity){
        this.productivity = productivity;
    }

    public int getMoney(){
        return money;
    }

    public void setMoney(int money){
        this.money = money;
    }

    public int compareTo(Recipe r){
        return skillLevel-r.skillLevel;
    }
}
