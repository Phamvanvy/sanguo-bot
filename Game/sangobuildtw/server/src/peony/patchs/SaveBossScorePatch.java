package peony.patchs;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import peony.game.Server;
import peony.game.instance.BossScore;
import peony.game.instance.BossScoreDao;
import peony.game.instance.BossScoreService;
import peony.game.instance.BossTimeScore;
import peony.game.instance.BossTimeScoreDao;
import peony.game.instance.Score;
import peony.service.Service;
import peony.service.ServiceRegistry;

public class SaveBossScorePatch implements Runnable {

	public void run() {
		BossScoreDao bossScoreDao = Server.server.getServiceRegistry().getDbService().bossScoreDao;
		BossTimeScoreDao bossTimeScoreDao = Server.server.getServiceRegistry().getDbService().bossTimeScoreDao;
		Set<Integer> keys = Server.server.getServiceRegistry().getBossScoreService().bossScores.keySet();
		for(int key : keys){
			Score score = Server.server.getServiceRegistry().getBossScoreService().bossScores.get(key);
			BossScore[] scores = score.bossScores;
			for(BossScore bossScore : scores){
				if(bossScore!=null){
					if(bossScoreDao.uniqueResult("from BossScore o where o.score=? and o.bossId=?", bossScore.score,bossScore.bossId)!=null){
						bossScoreDao.updateEntity(bossScore);
					}else{
						bossScoreDao.newEntity(bossScore);
					}
				}
			}
			for(BossTimeScore bossTimeScore : Server.server.getServiceRegistry().getBossScoreService().removedBossTimeScores){
				if(bossTimeScoreDao.uniqueResult("from BossTimeScore o where o.id=?", bossTimeScore.id)!=null)
					bossTimeScoreDao.makeTransient(bossTimeScore);
			}
			BossTimeScore[] timeScores = score.timeScores;
			for(BossTimeScore bossTimeScore : timeScores){
				if(bossTimeScore!=null){
					if(bossTimeScoreDao.uniqueResult("from BossTimeScore o where o.id=?", bossTimeScore.id)!=null){
						bossTimeScoreDao.makeTransient(bossTimeScore);
					}
					bossTimeScoreDao.newEntity(bossTimeScore);
				}
			}
			System.out.println("Save Score OK");
		}
		ServiceRegistry sr = Server.server.getServiceRegistry();
		try {
			Field field = ServiceRegistry.class.getDeclaredField("services");
			field.setAccessible(true);
			
			Map<String, Service> services = (Map<String, Service>)field.get(sr);
			
			services.remove(BossScoreService.class.getName());
		} catch (Exception e) {
			System.out.println("Remove Score OK");
		} 
	}

}
