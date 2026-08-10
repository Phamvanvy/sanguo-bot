package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TemplateGrid {

    public IItemTemplate template;

    public int count;
    
    public int percent = 1;
   

    public TemplateGrid(IItemTemplate template,int count) {
        this.template = template;
        this.count = count;
    }
    
    public void setPercent(int itempercent){
    	percent = itempercent;
    }

    public int getPercent(){
    	return percent;
    }

}
