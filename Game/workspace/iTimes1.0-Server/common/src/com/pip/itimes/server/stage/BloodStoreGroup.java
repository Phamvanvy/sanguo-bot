package com.pip.itimes.server.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BloodStoreGroup {
	private int id;
    private String name;
    private BloodStoreItem[] items;
    //private ExChangeItemData [] changitems;
    
    public BloodStoreGroup (String name, BloodStoreItem[] items) {
    	this.name = name;
    	this.items = items;
    }
    
    public void setId (int id) {
    	this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public BloodStoreItem[] getItems() {
        return items;
    }
    
//    public ExChangeItemData[] getchangItems(){
//    	return changitems;
//    }
    
    public void addItem(BloodStoreItem item) {
        for (int i=0; i<items.length; i ++) {
            if (items[i].item.getItemId() == item.item.getItemId()) {
                items[i] = item;
                return;
            }
        }
        BloodStoreItem[] newItems = new BloodStoreItem[items.length+1];
        System.arraycopy(items, 0, newItems, 0, items.length);
        newItems[newItems.length-1] = item;
        items = newItems;
    }

    public boolean removeItem(int itemId) {
        List l = new ArrayList(items.length);
        for (int i = 0; i < items.length; i ++) {
            if (items[i].item.getItemId() != itemId) {
                l.add(items[i]);
            }
        }
        if (l.size() < items.length) {
        	BloodStoreItem[] newItems = new BloodStoreItem[l.size()];
            l.toArray(newItems);
            items = newItems;
            return true;
        }
        return false;
    }
    
    public int getId () {
        return id;
    }

    
}
