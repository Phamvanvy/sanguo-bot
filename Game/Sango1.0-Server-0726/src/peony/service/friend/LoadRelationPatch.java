package peony.service.friend;

import peony.db.PlayerRelationDAO;
import peony.game.Server;

public class LoadRelationPatch implements Runnable {

	public void run() {
		PlayerRelation rel = Server.server.getServiceRegistry().getRelationService().get(30900);
		if(rel==null){
			System.out.println("Relation Not Found");
			PlayerRelationDAO dao = Server.server.getServiceRegistry().getDbService().playerRelationDAO;
			PlayerRelation relation = dao.findPlayerRelation(30900);
			Server.server.getServiceRegistry().getRelationService().relations.put(30900, relation);
			System.out.println("Relation LOAD OK");
		}
	}

}
