package com.pip.itimes.utils.award;

import com.pip.itimes.utils.IAward;
import com.pip.itimes.server.stage.IItemTemplate;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: </p>
 *
 * <p>Company: </p>
 *
 * @author Jeffrey
 * @version 1.0
 */
public class ItemAward implements IAward{

    public IItemTemplate item;
    public int count;

    public ItemAward(IItemTemplate item,int count) {
        this.item = item;
    }

    public IItemTemplate getItem(){
        return item;
    }

    public int count(){
        return count;
    }

    public String toString(){
        return "{Item["+item.getName()+","+count+"]}";
    }
}
