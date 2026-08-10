package peony.game;

import java.util.HashMap;
import java.util.Map;

import peony.service.Service;

public class PropertyService implements Service{

	public Map<Integer,Property> pros = new HashMap<Integer,Property>();
	
	public void startup() {
//		List l = Server.server.getServiceRegistry().getDbService().propertyDAO.getPropertys();
//		for(Object o:l){
//			Property p = (Property)o;
//			pros.put(p.id, p);
//		}
	}
	
	/**
	 * 
	 * @param id 0  ¿ΩÁ 1 Œ∫ 2  Ò 3 Œ‚
	 * @return
	 */
	public PropertyPool  getPropertyPool(int id){
		Property p = pros.get(id);
		if(p==null)
			return null;
		return p.pool;
	}
	
	public void shutdown() {
		
	}
}
