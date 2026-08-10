package com.pip.util;

import java.util.Hashtable;
import java.util.Vector;

import com.pip.ui.VMGame;

/**
 * 一个有序的Hashtable
 * @author ybai
 *
 */
public class SortHashtable {
	/**   实际的存储对象   */
    private Hashtable ht = new Hashtable();
    
    /**  key是ht的key，value是数据在vec中索引  */
    private Hashtable ht2 = new Hashtable();

    /**  维护一个Hashtable的顺序，存放hashtable的key  */
    private HVector vecKeys = new HVector();

    /**  维护一个Hashtable的顺序，存放hashtable的value  */
    private HVector vecValues = new HVector();
    
    // 缓存keys和values数组
    private Object[] keys;
    private Object[] values;
    
    public synchronized void clear(){        
        ht.clear();
        ht2.clear();
        vecKeys.removeAllElements();
        vecValues.removeAllElements();
        keys = null;
        values = null;
    }
    
    public synchronized void put(Object key, Object value) {  
    	remove(key);
    	
        vecKeys.addElement(key);
        vecValues.addElement(value);

        ht2.put(key, new Integer(vecKeys.size() - 1));
        ht.put(key, value);
        keys = null;
        values = null;
    }
    
    public synchronized void remove(Object key) {    	
    	Integer _index = (Integer)ht2.get(key);

        if(_index != null){
        	vecKeys.removeElementAt(_index.intValue());
        	vecValues.removeElementAt(_index.intValue());
        	
        	ht.remove(key);
        	ht2.remove(key);
        	
            //重新设定索引
            for(int i = _index.intValue(); i < vecKeys.size(); i++){
            	ht2.put(vecKeys.elementAt(i), new Integer(i));
            }
        }
        keys = null;
        values = null;
    }
    
    public int index(Object key) {
    	Integer _index = (Integer)ht2.get(key);
    	if(_index != null) {
    		return _index.intValue();
    	} else {
    		return -1;
    	}
    }
    
    public Object getKey(int index) {
        return vecKeys.elementAt(index);
    }
    
    public Object getValue(int index) {
    	return vecValues.elementAt(index);
    }
    
    public int size() {
    	return vecKeys.size();
    }
    
    public Object get(Object key) {
    	return ht.get(key);
    }
   
    public Object[] keys() {
        if (keys == null) {
            keys = vecKeys.values();
        }
        return keys;
    }
    
    public Object[] values() {
        if (values == null) {
            values = vecValues.values();
        }
        return values;
    }

    private class HVector extends Vector {
    	public synchronized Object[] values() {
    		Object[] values = new Object[elementCount];
    		System.arraycopy(elementData, 0, values, 0, elementCount);
    		return values;
    	}
    }
}
