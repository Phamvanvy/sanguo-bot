package com.pip.itimes.server.stage;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class SubTaskAward {

    private int sub;
    private int money;
    private int exp;
    private int credit;
    private List removeItems = new ArrayList(5);
    private List addItems = new ArrayList(5);

    public SubTaskAward() {
    }

    public void setSubId(int sub){
        this.sub = sub;
    }

    public int getSubId(){
        return sub;
    }

    public void addMoney(int money){
        this.money += money;
    }

    public int getMoney(){
        return this.money;
    }

    public void addExp(int exp){
        this.exp += exp;
    }

    public int getExp(){
        return exp;
    }

    public int getCredit() {
        return credit;
    }

    public void addCredit(int credit){
        this.credit += credit;
    }

    public void addItem(TemplateGrid grid){
        if(grid.count<0){
            grid.count *= -1;
            removeItems.add(grid);
        }
        else
            addItems.add(grid);
    }

    public TemplateGrid[] getRemoveItems(){
        TemplateGrid[] ret = new TemplateGrid[removeItems.size()];
        removeItems.toArray(ret);
        return ret;
    }

    public TemplateGrid[] getAddItems(){
        TemplateGrid[] ret = new TemplateGrid[addItems.size()];
        addItems.toArray(ret);
        return ret;
    }
}
