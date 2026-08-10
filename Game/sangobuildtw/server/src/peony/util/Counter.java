package peony.util;

import java.util.HashMap;
import java.util.Map;

public class Counter {
	public Map<Integer,Integer> map;
	
	public Counter(){
		map = new HashMap<Integer,Integer>();
	}
	
	public int add(int key,int value){
		Integer i = map.get(key);
		if(i==null){
			i = new Integer(0);
		}
		i += value;
		map.put(key, i);
		return i;
	}
	
	public int value(int key){
		Integer i = map.get(key);
		if(i==null){
			return 0;
		}
		return i;
	}
}
