package peony.vm;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import peony.game.CommonUtil;
import peony.game.GameQuest;
import peony.game.Server;
import peony.game.GameQuest.CycleInfo;
import ch.javasoft.util.intcoll.IntHashMap;

import com.pip.sanguo.data.DataChangeHandler;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestInfo;

public class ASMQuestUtil implements DataChangeHandler {
	//任务Id到任务的映射
	private static final Map<Integer,GameQuest> gameQuests = new HashMap<Integer,GameQuest>();
	//关卡Id到任务的映射
//	private static final Map<Integer,GameQuest> areaGameQuests = new HashMap<Integer,GameQuest>();
	
	private static final QuestClassLoader classLoader = new QuestClassLoader();
	
	private static final Map<Integer,ASMQuest> quests = new HashMap<Integer,ASMQuest>();
	private static final Map<Integer,ASMQuest> areaQuests = new HashMap<Integer,ASMQuest>();
	
	private static final IntHashMap<List<ASMQuest>> mapid2quests = new IntHashMap<List<ASMQuest>>();
	
	private static final Logger log = Logger.getLogger(ASMQuestUtil.class);
	
	@SuppressWarnings("unchecked")
	public static void load() throws Exception{
		gameQuests.clear();
		quests.clear();
		areaQuests.clear();
		mapid2quests.clear();
		List quests = Server.server.getServiceRegistry().getDataService().data.getDataListByType(Quest.class);
		Iterator ite = quests.iterator();
		long start = System.currentTimeMillis();
		while(ite.hasNext()){
			Quest quest = (Quest)ite.next();
			try {
    			QuestInfo info = new QuestInfo(quest);
    			info.load();
    			GameQuest gQuest = new GameQuest(quest,info);
    			QuestClassLoader.addQuest(gQuest);
    			ASMQuestUtil.addGameQuest(gQuest);
			} catch (Throwable e) {
			    log.error("Quest " + quest.id + " contains error: ", e);
			}
		}
		//cycle
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("cycle.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		long used = System.currentTimeMillis() - start;
		System.out.println("quest total used : " + used + "ms");
	}
	
	@SuppressWarnings("unchecked")
	protected static void parse(Document doc){
		Element root = doc.getRootElement();
		Iterator ite = root.elementIterator("cycle");
		while(ite.hasNext()){
			Element el = (Element)ite.next();
			int index = Integer.parseInt(el.attributeValue("id"));
			int level = Integer.parseInt(el.attributeValue("level"));
			int[] quests = parseInts(el.attributeValue("quests"));
			if(quests.length>=3){
				for(int i=0;i<quests.length;i++){
					GameQuest gq = getGameQuest(quests[i]);
					int type = CycleInfo.TYPE_NODE;
					int nextQuestId = 0;
					if(i == 0){
						type = CycleInfo.TYPE_HEAD;
					}else if(i == quests.length-1){
						type = CycleInfo.TYPE_TAIL;
					}
					if(type != CycleInfo.TYPE_TAIL){
						nextQuestId = quests[i+1];
					}
					CycleInfo cycleInfo = new CycleInfo(type,index,level,i+1,quests[i],nextQuestId);
					gq.setCycleInfo(cycleInfo);
				}
			}
		}
	}
	
	protected static int[] parseInts(String value){
		String[] ss = value.split(",");
		int[] ret = new int[ss.length];
		for(int i=0;i<ss.length;i++){
			ret[i] = Integer.parseInt(ss[i]);
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static void addGameQuest(GameQuest gameQuest){
		gameQuests.put(gameQuest.getId(), gameQuest);
		try {
			Class clazz = Class.forName(gameQuest.getClassName(),true,classLoader);
			ASMQuest quest = (ASMQuest)clazz.getConstructors()[0].newInstance(gameQuest);
			quests.put(gameQuest.getId(), quest);
			if(gameQuest.getStartNpc()!=-1){
				addMapQuest(gameQuest.getStartNpc()>>12,quest);
			}
			if(gameQuest.isAreaQuest()){
//				areaGameQuests.put(gameQuest.getAreaId(), gameQuest);
				areaQuests.put(gameQuest.getAreaId(), quest);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	protected static void addMapQuest(int mapId,ASMQuest quest){
		List<ASMQuest> l = mapid2quests.get(mapId);
		if(l==null){
			l = new ArrayList<ASMQuest>();
			mapid2quests.put(mapId, l);
		}
		l.add(quest);
	}
	
	protected static void removeQuest(int id) {
	    GameQuest gameQuest = gameQuests.remove(id);
	    quests.remove(id);
	    if (gameQuest != null && gameQuest.getStartNpc() != -1) {
	        List<ASMQuest> l = mapid2quests.get(gameQuest.getStartNpc()>>12);
	        for (int i = 0; i < l.size(); i++) {
	            if (l.get(i).getId() == gameQuest.getId()) {
	                l.remove(i);
	                break;
	            }
	        }
	    }
	    if (gameQuest.isAreaQuest()) {
	        areaQuests.remove(gameQuest.getAreaId());
	    }
	}
	
	/**
	 * 获得场景所有的任务
	 * @param mapId
	 * @return
	 */
	public static List<ASMQuest> getMapQuest(int mapId){
		return mapid2quests.get(mapId);
	}
	
	/**
	 * 获得所有的场景任务
	 */
//	public static GameQuest getAreaGameQuest(int areaId){
//		return areaGameQuests.get(areaId);
//	}
	
	public static GameQuest getGameQuest(int id){
		return gameQuests.get(id);
	}
	
	public static GameQuest[] getAllQuests() {
		GameQuest[] ret = new GameQuest[gameQuests.size()];
		gameQuests.values().toArray(ret);
		return ret;
	}
	
	public static ASMQuest getQuest(int id){
		return quests.get(id);
	}
	
	public static ASMQuest getAreaQuest(int areaId){
		return areaQuests.get(areaId);
	}

    /**
     * 添加新对象通知。
     * @param obj 新添加的对象
     */
    public void dataObjectAdded(DataObject obj) {
        if (obj instanceof Quest) {
            Quest quest = (Quest)obj;
            try {
                QuestInfo info = new QuestInfo(quest);
                info.load();
                GameQuest gQuest = new GameQuest(quest,info);
                QuestClassLoader.addQuest(gQuest);
                ASMQuestUtil.addGameQuest(gQuest);
            } catch (Exception e) {
                log.error("Quest " + quest.id + " contains error: ", e);
            }
        }
    }
    
    /**
     * 对象被删除通知。
     * @param obj 被删除的老对象
     */
    public void dataObjectRemoved(DataObject obj) {
        if (obj instanceof Quest) {
            removeQuest(obj.id);
        }
    }
    
    /**
     * 对象即将被修改通知。
     * @param obj 修改前的对象
     */
    public void dataObjectChanging(DataObject obj) {
    }
    
    /**
     * 对象被修改通知。
     * @param newobj 修改后的新对象
     */
    public void dataObjectChanged(DataObject newobj) {
        if (newobj instanceof Quest) {
            Quest quest = (Quest)newobj;
            removeQuest(quest.id);
            try {
                QuestInfo info = new QuestInfo(quest);
                info.load();
                GameQuest gQuest = new GameQuest(quest,info);
                QuestClassLoader.addQuest(gQuest);
                ASMQuestUtil.addGameQuest(gQuest);
            } catch (Exception e) {
                log.error("Quest " + quest.id + " contains error: ", e);
            }
        }
    }
}
