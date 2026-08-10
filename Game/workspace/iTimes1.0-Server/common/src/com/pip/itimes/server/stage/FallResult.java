package com.pip.itimes.server.stage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class FallResult {

    private int money;
    private int exp;
    private List templates = new ArrayList();

    public FallResult() {
    }

    public void addMoney(int money){
        this.money += money;
    }

    public void setMoney(int money){
        this.money = money;
    }

    public void addExp(int exp){
        this.exp += exp;
    }

    public void setExp(int exp){
        this.exp = exp;
    }

    public int getMoney(){
        return money;
    }

    public int getExp(){
        return exp;
    }

    public void addItem(int itemId,int count){
        IItemTemplate template = Items.getTemplate(itemId);
        addItem(template,count);
    }

    public void addItem(IItemTemplate template,int count){
        byte type = template.getType();
        if(type==IItem.TYPE_EQU){
            TemplateGrid grid = new TemplateGrid(template,1);
            templates.add(grid);
        }
        else if(type==IItem.TYPE_PET){

        }else{
            boolean added = false;
            for(int i=0;i<templates.size();i++){
                TemplateGrid grid = (TemplateGrid)templates.get(i);
                if(grid.template.getItemId()==template.getItemId()){
                    grid.count += count;
                    added = true;
                }
            }
            if(!added){
                TemplateGrid grid = new TemplateGrid(template,count);
                templates.add(grid);
            }
        }
    }

    public TemplateGrid[] getItems(){
        TemplateGrid[] ret = new TemplateGrid[templates.size()];
        templates.toArray(ret);
        return ret;
    }
    
    public List getTemplates () {
    	return templates;
    }
}
