package com.pip.itimes.server.gift;

import java.util.Date;
import java.util.Vector;

import com.pip.itimes.server.bean.Gift;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.stage.TemplateGrid;
import com.pip.itimes.server.util.Utils;


public class OnlyGiftDefine{
    private int id;//给予的 id号
 
    public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	private Vector<OnlyGiftNeedItem> needItems = new Vector<OnlyGiftNeedItem>();//需要的物品类型和要求
    private Vector<TemplateGrid> giveItems = new Vector<TemplateGrid>();//发奖的物品
    
    public void addNeedItem(OnlyGiftNeedItem onlyGiftNeedItem){
        needItems.add(onlyGiftNeedItem);
    }
    
    public void addGiveItems(int itemId, int count){
        TemplateGrid item = new TemplateGrid(Items.getTemplate(itemId), count);
        giveItems.add(item);
    }
    public Vector<OnlyGiftNeedItem> getAllNeedItem(){
    	return needItems;
    }
    
    public TemplateGrid[] getAllGiveItem(){
        TemplateGrid[] result = new TemplateGrid[giveItems.size()];
        giveItems.toArray(result);

        return result;
    }
    public String getGiveItemString(){
        StringBuffer sb = new StringBuffer();
        
        for(int i = 0; i < giveItems.size(); i++){
            TemplateGrid grid = giveItems.get(i);
            sb.append(grid.template.getName());
            
            if(grid.count > 1){
                sb.append(" + ");
                sb.append(grid.count);
            }
            
            if(i < needItems.size() - 1){
                sb.append(", ");
            }
        }
        
        return sb.toString();
    }
 
}