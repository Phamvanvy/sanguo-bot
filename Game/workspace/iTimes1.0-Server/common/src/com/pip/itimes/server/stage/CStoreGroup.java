package com.pip.itimes.server.stage;

import java.util.List;
import java.util.ArrayList;

public class CStoreGroup {

	private int id;
    private String name;
    private CStoreItem[] items;

    public CStoreGroup (String name, CStoreItem[] items) {
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

    public CStoreItem[] getItems() {
        return items;
    }

    public void addItem(CStoreItem item) {
        for (int i=0; i<items.length; i ++) {
            if (items[i].item.getItemId() == item.item.getItemId()) {
                items[i] = item;
                return;
            }
        }
        CStoreItem[] newItems = new CStoreItem[items.length+1];
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
            CStoreItem[] newItems = new CStoreItem[l.size()];
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
