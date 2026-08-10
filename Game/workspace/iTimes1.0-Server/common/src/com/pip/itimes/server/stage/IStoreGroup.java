package com.pip.itimes.server.stage;

import java.util.List;
import java.util.ArrayList;

public class IStoreGroup {

    private String name;
    private IStoreItem[] items;
    private boolean hide = false;

    public IStoreGroup(String name, boolean hide, IStoreItem[] items) {
        this.name = name;
        this.items = items;
        this.hide = hide;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public IStoreItem[] getItems(){
        return items;
    }
    
    public IStoreItem getItem(int itemId){
    	for(int i=0;i<items.length;i++){
            if(items[i].item.getItemId()==itemId){
                return items[i];
            }
        }
    	return null;
    }


    public void addItem(IStoreItem item){
        for(int i=0;i<items.length;i++){
            if(items[i].item.getItemId()==item.item.getItemId()){
                items[i] = item;
                return;
            }
        }
        IStoreItem[] newItems = new IStoreItem[items.length+1];
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
            IStoreItem[] newItems = new IStoreItem[l.size()];
            l.toArray(newItems);
            items = newItems;
            return true;
        }
        return false;
    }
    
    public void setHide(boolean hide){
    	this.hide = hide;
    }
    
    public boolean getHide(){
    	return hide;
    }
}
