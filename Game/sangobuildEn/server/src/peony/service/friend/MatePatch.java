package peony.service.friend;

import peony.game.Server;

public class MatePatch implements Runnable {

	public void run() {
		RelationService service = Server.server.getServiceRegistry().getRelationService();
		for(PlayerRelation rel:service.relations.values()){
			if(rel.mateId==0){
				rel.mateId = -1;
			}
		}
	}

}
