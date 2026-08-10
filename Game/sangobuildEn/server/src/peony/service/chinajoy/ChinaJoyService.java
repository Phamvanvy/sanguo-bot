package peony.service.chinajoy;

import java.util.HashMap;
import java.util.Map;

import peony.game.DayListener;
import peony.service.Service;

public class ChinaJoyService implements Service,DayListener {
	
	private Map<Integer,Integer> votes = new HashMap<Integer,Integer>();

	public void shutdown() {

	}

	public void startup() throws Exception {
		
	}
	
	public boolean addVote(int accountId){
		Integer i = votes.get(accountId);
		if(i==null){
			i = 0;
		}
		if(i>=2)
			return false;
		i++;
		votes.put(accountId, i);
		return true;
	}
	
	public int getCount(int accountId){
		Integer i = votes.get(accountId);
		if(i==null){
			return 0;
		}else{
			return i;
		}
	}

	public void dayChanged() {
		votes.clear();
	}
	
	

}
