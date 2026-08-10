package com.pip.itimes.server.stage;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class StoreGroup {

    private int id;
    private String desc;
    private StoreItem[] items;
    private int type;
    public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public StoreGroup(int id) {
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public String getDesc(){
        return desc;
    }

    public void setItems(StoreItem[] items){
        this.items = items;
    }

    public StoreItem[] getItems(){
        return items;
    }

    //如果发现相同的物品就替换，否则就加添
    public void addItem(StoreItem item){
        for(int i=0;i<items.length;i++){
            if(items[i].item.getItemId()==item.item.getItemId()){
                items[i] = item;
                return;
            }
        }
        StoreItem[] newItems = new StoreItem[items.length+1];
        System.arraycopy(items,0,newItems,0,items.length);
        newItems[newItems.length-1] = item;
        items = newItems;
    }

    public boolean removeItem(int itemId){
        List l = new ArrayList(items.length);
        for(int i=0;i<items.length;i++){
            if(items[i].item.getItemId()!=itemId){
                l.add(items[i]);
            }
        }
        if(l.size()<items.length){
            StoreItem[] newItems = new StoreItem[l.size()];
            l.toArray(newItems);
            items = newItems;
            return true;
        }
        return false;
    }
}
