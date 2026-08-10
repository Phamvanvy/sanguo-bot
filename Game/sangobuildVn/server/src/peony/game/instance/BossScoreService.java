package peony.game.instance;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.Creature;
import peony.game.GameObject;
import peony.game.Instance;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

public class BossScoreService implements Service, ServiceEventListener {

	/** 排行榜 **/
	public Map<Integer, Score> bossScores = new HashMap<Integer, Score>();
	
	/** 被替换出来的最快击杀BOSS排行榜 **/
	public List<BossTimeScore> removedBossTimeScores = new ArrayList<BossTimeScore>();
	
	/** 所有BOSS的定义 */
	public Map<Integer, BossDef> bossDefs = new HashMap<Integer, BossDef>();

	public void shutdown() {
		BossScoreDao bossScoreDao = Server.server.getServiceRegistry().getDbService().bossScoreDao;
		BossTimeScoreDao bossTimeScoreDao = Server.server.getServiceRegistry().getDbService().bossTimeScoreDao;
		Set<Integer> keys = bossScores.keySet();
		for(int key : keys){
			Score score = bossScores.get(key);
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
			
			// 删除所有过期的击杀记录
			Set<Integer> needRemoveIDs = new HashSet<Integer>();
			for (BossTimeScore bossTimeScore : removedBossTimeScores) {
				needRemoveIDs.add(bossTimeScore.id);
			}
			if (needRemoveIDs.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("delete from BossTimeScore o where o.id in (");
				int passed = 0;
				for (int id : needRemoveIDs) {
					if (passed > 0) {
						sb.append(",");
					}
					sb.append(id);
					passed++;
				}
				sb.append(")");
				bossTimeScoreDao.delete(sb.toString());
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
		}
	}
	
	public void removeAllData(){
		Set<Integer> keys = bossScores.keySet();
		for(int key : keys){
			bossScores.put(key, new Score(key));
		}
		BossScoreDao bossScoreDao = Server.server.getServiceRegistry().getDbService().bossScoreDao;
		BossTimeScoreDao bossTimeScoreDao = Server.server.getServiceRegistry().getDbService().bossTimeScoreDao;
		bossScoreDao.delete("delete from BossScore");
		bossTimeScoreDao.delete("delete from BossTimeScore");
	}

	@SuppressWarnings("unchecked")
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("boss.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		BossScoreDao bossScoreDao = Server.server.getServiceRegistry().getDbService().bossScoreDao;
		BossTimeScoreDao bossTimeScoreDao = Server.server.getServiceRegistry().getDbService().bossTimeScoreDao;
		List<BossScore> list = bossScoreDao.list("from BossScore");
		for(BossScore bossScore : list){
			int bossId = bossScore.bossId;
			Score score = bossScores.get(bossId);
			score.bossScores[bossScore.score-1] = bossScore;
		}
		// 最快排行榜取数据库数据无需考虑顺序，因为每次客户端请求之后都会进行排序
		List<BossTimeScore> list1 = bossTimeScoreDao.list("from BossTimeScore");
		for(int i=0;i<list1.size();i++){
			BossTimeScore bossTimeScore = list1.get(i);
			int bossId = bossTimeScore.bossId;
			Score score = bossScores.get(bossId);
			score.addBossTimeScore(bossTimeScore);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		List list = root.elements("instance");
		if(list.size()==0)
			throw new IllegalArgumentException();
		for(int i=0;i<list.size();i++){
			Element map = (Element) list.get(i);
			List ll = map.elements("boss");
			for(int j=0;j<ll.size();j++){
				Element boss = (Element)ll.get(j);
				int bossId = Integer.parseInt(boss.attributeValue("id"));
				Score score = new Score(bossId);
				bossScores.put(bossId, score);
				BossDef def = new BossDef();
				def.id = bossId;
				def.name = boss.attributeValue("name");
				bossDefs.put(def.id, def);
			}
		}
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_UNIT_DIE };
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_UNIT_DIE:
			unitDie((Unit) event.param1, (Unit) event.param2);
			break;
		}
	}

	protected void unitDie(Unit u1, Unit u2) {
		if (isBoss(u1.id) && u2.type == GameObject.TYPE_PLAYER) {
			Instance instance = u2.map.map.instance;
			if(instance==null || !(instance instanceof NormalInstance))
				return;
			NormalInstance normalInstance = (NormalInstance)instance;
			Date date = new Date();
			Score score = bossScores.get(u1.id);
			if(((Creature)u1).battleContribList==null || ((Creature)u1).battleContribList.checkOwners()==null)
				return;
			List<Player> owneres = ((Creature)u1).battleContribList.checkOwners(); // 收益表
			if(score.checkLevel(owneres, u1.level))
				return;
			if(score.getBossScoresSize()<11 && !score.hasOnScoreBoard(owneres)){
				BossScore bossScore = new BossScore(u1.id);
				bossScore.date = date;
				for(Player member : owneres){
					int id = member.id;
					int level = member.level;
					int faction = member.faction;
					String name = member.name;
					int sex = member.sex;
					int clazz = member.clazz;
					Member member2 = new Member(faction,id,level,name,sex,clazz);
					bossScore.members.addMember(member2);
				}
				bossScore.score = score.getMaxScore()+1;
				score.bossScores[bossScore.score-1] = bossScore;
			}
			// 当前击杀BOSS的队伍
			BossScore bossScore2 = score.bossScores[10]==null ? new BossScore(u1.id) : score.bossScores[10];
			bossScore2.date = date;
			bossScore2.members.clearMemeber();
			for(Player member : owneres){
				int id = member.id;
				int level = member.level;
				int faction = member.faction;
				String name = member.name;
				int sex = member.sex;
				int clazz = member.clazz;
				Member member2 = new Member(faction,id,level,name,sex,clazz);
				bossScore2.members.addMember(member2);
			}
			bossScore2.score = 11;
			score.bossScores[10] = bossScore2;
			
			// 最快击杀BOSS
			int dieTime = Time.currTime; //怪物死亡时间
			int lastBossDieTime = normalInstance.lastBossDieTime;
			int createTime = normalInstance.createTime;
			if(lastBossDieTime>0)
				createTime = lastBossDieTime;
			BossTimeScore bossTimeScore = new BossTimeScore();
			bossTimeScore.bossId = u1.id;
			if(score.getBossTimeScoresSize()<10 && score.partyHasOnTimeScoreBoard(owneres)==null){
				bossTimeScore.time = dieTime - createTime;
				bossTimeScore.date = date;
				for(Player member : owneres){
					int id = member.id;
					int level = member.level;
					int faction = member.faction;
					String name = member.name;
					int sex = member.sex;
					int clazz = member.clazz;
					Member member2 = new Member(faction,id,level,name,sex,clazz);
					bossTimeScore.members.addMember(member2);
				}
				score.timeScores[score.getBossTimeScoresSize()] = bossTimeScore;
			}else{
				if(score.partyHasOnTimeScoreBoard(owneres)==null){
					int index = score.getLongestTimeScore();
					bossTimeScore.time = dieTime - createTime;
					bossTimeScore.date = date;
					for(Player member : owneres){
						int id = member.id;
						int level = member.level;
						int faction = member.faction;
						String name = member.name;
						int sex = member.sex;
						int clazz = member.clazz;
						Member member2 = new Member(faction,id,level,name,sex,clazz);
						bossTimeScore.members.addMember(member2);
					}
					removedBossTimeScores.add(score.timeScores[index]);
					score.timeScores[index] = bossTimeScore;
				}else{
					if(score.partyHasOnTimeScoreBoard(owneres).time>=(dieTime - createTime)){
						int index = score.getPartyIndex(owneres);
						bossTimeScore.time = dieTime - createTime;
						bossTimeScore.date = date;
						for(Player member : owneres){
							int id = member.id;
							int level = member.level;
							int faction = member.faction;
							String name = member.name;
							int sex = member.sex;
							int clazz = member.clazz;
							Member member2 = new Member(faction,id,level,name,sex,clazz);
							bossTimeScore.members.addMember(member2);
						}
						removedBossTimeScores.add(score.timeScores[index]);
						score.timeScores[index] = bossTimeScore;
					}
				}
			}
			normalInstance.lastBossDieTime = Time.currTime;
		}
	}

	/**
	 * 根据id判断是否为boss
	 */
	protected boolean isBoss(int id) {
		return bossScores.keySet().contains(id);
	}

	/**
	 * 取得BOSS的定义，如果指定的ID不是BOSS，返回null。
	 */
	public BossDef getBossDef(int id) {
		return bossDefs.get(id);
	}
}

