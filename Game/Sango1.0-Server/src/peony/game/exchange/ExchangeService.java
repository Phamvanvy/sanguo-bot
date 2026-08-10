package peony.game.exchange;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Time;
import peony.service.Service;

public class ExchangeService implements Service {

	private static final int TIMEOUT = 60 * 1000; 
	
	protected ConcurrentHashMap<Integer,Exchange> exchanges = new ConcurrentHashMap<Integer,Exchange>();
	
	protected int lastUpdateTime = 0;
	
	public void shutdown() {
		
	}

	public void startup() throws Exception {

	}

	public void exchangeCreated(Exchange e){
		exchanges.put(e.id, e);
	}
	
	public void exchangeCompleted(Exchange e){
		exchanges.remove(e.id);
	}
	
	public Exchange getExchange(int id){
		return exchanges.get(id);
	}
	
	public Exchange removeExchange(int id){
		return exchanges.remove(id);
	}
	
	public void update() {
		if (Time.currTime - lastUpdateTime < 1000) {
			return;
		}
		lastUpdateTime = Time.currTime;
		Iterator<Exchange> ite = exchanges.values().iterator();
		while(ite.hasNext()){
			Exchange ex = ite.next();
			if(ex.state==Exchange.STATE_INIT){
				if(Time.currTime-ex.createTime>=TIMEOUT){
					Player p = ObjectAccessor.getPlayer(ex.sourceRef.id);
					if(p!=null){
						p.exchange = null;
						p.exchangeRefuse(ex.serial, peony.Messages.STRING_01542);
					}
					ite.remove();
				}
			}
		}
	}
}
