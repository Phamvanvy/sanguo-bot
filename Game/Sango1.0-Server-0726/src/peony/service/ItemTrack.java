package peony.service;

import java.util.HashMap;
import java.util.Map;

/**
 * ÎïÆ·¸ú×Ù
 * @author dchen
 */
public class ItemTrack {

	public int id;
	public Map<Integer, Integer> map = new HashMap<Integer, Integer>();
	
	public ItemTrack(int id){
		this.id = id;
	}
	
	public void addIT(int itemId, int totle){
		map.put(itemId, totle);
	}
	
	public int getTotleByItemId(int itemId){
		return map.get(itemId);
	}
	
}
