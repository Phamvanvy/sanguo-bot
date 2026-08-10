package peony.patchs;

import java.lang.reflect.Field;
import java.util.Map;

import peony.db.PlayerRelationDAO;
import peony.game.Server;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationService;

public class LoadRelation2 implements Runnable {

	public void run() {
		PlayerRelation rel = Server.server.getServiceRegistry().getRelationService().get(62274);
		if(rel==null){
			PlayerRelationDAO dao = Server.server.getServiceRegistry().getDbService().playerRelationDAO;
			PlayerRelation relation = dao.findPlayerRelation(62274);
			try{
				Field field = RelationService.class.getDeclaredField("relations");
				field.setAccessible(true);
				Map map = (Map)field.get(Server.server.getServiceRegistry().getRelationService());
				map.put(62274, relation);
				System.out.println("62274ok");
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}else{
			System.out.println("62274notfound");
		}
	}

}
