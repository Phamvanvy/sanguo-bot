package peony.game;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import org.apache.log4j.Logger;
import peony.game.drop.GroupDrop;
import peony.game.itemeffect.HorseItemEffect;
import peony.game.skill.Skill;
import peony.game.suite.SuiteEffects;
import ch.javasoft.util.intcoll.*;

import com.pip.sanguo.data.HorseType;

/**
 * 游戏中各种对象的全局存储，用以实现游戏对象的快速搜索。
 * 2010/04/05 lighthu改造以支持多线程同时存取。
 * @author pip
 */
@SuppressWarnings("unchecked")
public class ObjectAccessor {
	private static final Logger log = Logger.getLogger(ObjectAccessor.class);

	/**
	 * 所有内存中GameObject（包括玩家、怪物、采集点）的快速查找表，KEY是instanceID。
	 */
	public static final ConcurrentHashMap<Integer, GameObject> instanceid2objects = new ConcurrentHashMap<Integer, GameObject>();
	/**
	 * 所有内存中角色的快速查找表，KEY是角色ID。
	 */
	public static final ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<Integer, Player>();

	/**
	 * 物品模板索引表。
	 */
	public static final IntHashMap<ItemTemplate> itemTemplates = new IntHashMap<ItemTemplate>();
	/**
	 * 坐骑类型索引表。
	 */
	public static final IntHashMap<HorseType> horseTypes = new IntHashMap<HorseType>();
	/**
	 * 技能索引表。
	 */
	public static final IntHashMap<Skill> skills = new IntHashMap<Skill>();
	/**
	 * 职业技能索引表。
	 */
	public static final List<Skill>[] playerSkills = new List[4];
	/**
	 * 职业初始技能索引表。
	 */
	public static final List<Skill>[] playerInitSkills = new List[4];
	/**
	 * 技能最大级别索引表。
	 */
	public static final IntIntMap skillMaxLevels = new DefaultIntIntMap();
	/**
	 * 技能名称索引表。
	 */
	public static final IntHashMap<String> playerSkillName = new IntHashMap<String>();
	/**
	 * 掉落组索引表。
	 */
	public static final IntHashMap<GroupDrop> groupDrops = new IntHashMap<GroupDrop>();
	/**
	 * 套装效果索引表。
	 */
	public static final IntHashMap<SuiteEffects> suites = new IntHashMap<SuiteEffects>();
	
	/**
	 * 缓存邮件以及聊天系统发布的物品,只存instanceId不为-1的物品，Key是ItemId<<32|instanceId,这里的GameItem只是一个clone版本
	 */
	public static final Map<Long,GameItem> cachedItems = new ConcurrentHashMap<Long,GameItem>();
	
	/*
	 * 自动保存玩家数据的线程。此线程定时按照保存一组玩家数据（按玩家ID分组）。
	 */
	protected static Thread autoSave = null;
	/*
	 * 当前保存分组ID。
	 */
	protected static int round = 0;

	static {
		for (int i = 0; i < playerSkills.length; i++) {
			playerSkills[i] = new ArrayList<Skill>();
		}
		for (int i = 0; i < playerInitSkills.length; i++) {
			playerInitSkills[i] = new ArrayList<Skill>();
		}
	}
	
	/**
	 * 注册一个新的游戏对象。
	 * @param unit
	 */
	public static void addGameObject(GameObject unit) {
		if (unit.type == GameObject.TYPE_PLAYER) {
			players.put(unit.id, (Player)unit);
		}
		instanceid2objects.put(unit.instanceId, unit);
	}
	
	/**
	 * 根据instanceId查找一个游戏对象。
	 * @param instanceId
	 * @return
	 */
	public static GameObject getGameObject(int instanceId){
		return instanceid2objects.get(instanceId);
	}
	
	/**
	 * 根据角色ID查找一个在线角色。
	 * @param id
	 * @return
	 */
	public static Player getPlayer(int id){
		return players.get(id);
	}
	
	/**
	 * 根据先前保存的对象引用查找游戏对象。
	 */
	public static GameObject getGameObject(GameObjectRef ref){
		GameObject gm = instanceid2objects.get(ref.instanceId);
//		if(gm==null || (gm!=null && gm.type!=ref.type && ref.type==GameObject.TYPE_PLAYER))
//			gm = players.get(ref.id);
		return gm;
	}
	
	/**
	 * 从世界中移除一个游戏对象。
	 * @param unit
	 */
	public static void removeGameObject(GameObject unit) {
		instanceid2objects.remove(unit.instanceId);
		if (unit.type == GameObject.TYPE_PLAYER) {
			players.remove(unit.id);
		}
	}

	/**
	 * 启动自动保存玩家数据的线程。
	 */
	public static void startAutoSaveThread(){
		autoSave = new Thread("AutoSave"){
			@Override
			public void run(){
				while (Server.server.running) {
					log.info("[AUTOSAVE]ROUND["+round+"]");
					for (Player p:players.values()) {
						try {
							if (p.id % 40 == round) {
								Server.server.getServiceRegistry().getPlayerService().savePlayer(p);
							}
						} catch (Exception e) {
						}
					}
					round ++;
					if (round >= 40) {
						round = 0;
					}
					try {
						Thread.sleep(60*1000L);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		autoSave.start();
	}
	
	/**
	 * 启动定时报告在线数的线程。
	 */
	public static void startRefreshThread() {
		new Thread("ObjectAccessor"){
			@Override
			public void run(){
				while (Server.server.running) {
					log.info("[ONLINE]COUNT["+players.size()+"]");
					try {
						Thread.sleep(60*1000L);
					} catch (InterruptedException e) {
					}
				}
			}
		}.start();
	}
	
	/**
	 * 注册一个坐骑类型。
	 * @param type
	 */
	public static void addHorseType(HorseType type){
		horseTypes.put(type.getID(), type);
	}
	
	/**
	 * 根据ID查找坐骑类型。
	 * @param id
	 * @return
	 */
	public static HorseType getHorseType(int id){
		return horseTypes.get(id);
	}
	
	/**
	 * 创建一个新的坐骑对象。
	 * @param itemId 坐骑物品ID
	 * @param typeId 坐骑类型ID
	 * @param imageId 坐骑动画ID
	 * @param initLevel 初始等级
	 * @param name 名称
	 * @param iconId 图标ID
	 * @return
	 */
	public static Horse createHorse(int itemId, int typeId, int imageId,
			int initLevel, String name, int iconId) {
		HorseType t = getHorseType(typeId);
		if (t == null) {
			throw new IllegalArgumentException();
		}
		int id = Server.server.getServiceRegistry().getSleepyCatService().generatorHorseId();
		Horse h = new Horse(t, id);
		h.name = name;
		h.imageId = imageId;
		h.iconId = iconId;
		h.initLevel = initLevel;
		h.setLevel(initLevel, null);
		h.itemId = itemId;
		h.degree = Horse.MAXDEGREE;
		return h;
	}
	
	/**
	 * 注册一个物品模板。
	 * @param template
	 */
	public static void addItemTemplate(ItemTemplate template){
		itemTemplates.put(template.id, template);
	}
	
	/**
	 * 根据坐骑类型超找坐骑模板
	 * @param horseType
	 * @return 坐骑模板
	 * @throws Exception
	 */
	public static ItemTemplate getHorseTemplate(int horseType) throws Exception{
		for(int id : itemTemplates.keySet()){
			ItemTemplate it = itemTemplates.get(id);
			if(it.useType.effect instanceof HorseItemEffect){
				HorseItemEffect effect = (HorseItemEffect)it.useType.effect;
				Field field = HorseItemEffect.class.getDeclaredField("horseTypes");
				field.setAccessible(true);
				int[] horseTypes = (int[])field.get(effect);
				for(int i=0;i<horseTypes.length;i++){
					if(horseTypes[i] == horseType){
						return it;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 根据物品ID查找物品模板。
	 * @param id
	 * @return
	 */
	public static ItemTemplate getItemTemplate(int id){
		return itemTemplates.get(id);
	}
	
	/**
	 * 根据ID创建一个物品。
	 * @param id
	 * @return
	 */
	public static GameItem createGameItem(int id) {
		ItemTemplate template = getItemTemplate(id);
		return createGameItem(template, -1);
	}
	
	/**
	 * 根据一个物品模板创建一个物品。
	 * @param template
	 * @param instanceId 指定的实例ID，-1表示新生成
	 * @return
	 */
	public static GameItem createGameItem(ItemTemplate template, int instanceId) {
		if (template == null) {
			throw new IllegalArgumentException();
		}
		if (template.isEquipment() || template.newInstance) {
			if(instanceId==-1){
				instanceId = Server.server.getServiceRegistry().getSleepyCatService().generatorItemId();
			}
			GameItem item = new GameItem(template,instanceId);
			return item;
		} else {
			GameItem item = new GameItem(template, GameItem.GENERAL_INSTANCEID);
			return item;
		}
	}
	
	/**
	 * 注册一个掉落组。
	 * @param groupDrop
	 */
	public static void addGroupDrop(GroupDrop groupDrop){
		groupDrops.put(groupDrop.getId(), groupDrop);
	}

	/**
	 * 根据掉落组ID查找掉落组。
	 * @param groupId
	 * @return
	 */
	public static GroupDrop getGroupDrop(int groupId){
		return groupDrops.get(groupId);
	}
	
	/**
	 * 注册一个技能。
	 * @param skill
	 */
	public static void addSkill(Skill skill) {
		skills.put(skill.getId(), skill);
		if (skillMaxLevels.containsKey(skill.getGroupId())) {
			int maxLevel = skillMaxLevels.getInt(skill.getGroupId());
			if (skill.getLevel() > maxLevel) {
				skillMaxLevels.put(skill.getGroupId(), skill.getLevel());
			}
		} else {
			skillMaxLevels.put(skill.getGroupId(), skill.getLevel());
		}
		if (skill.isPlayerSkill()) {
			boolean found = false;
			for (int i = 0; i < playerSkills[skill.getClazz()].size(); i++) {
				if (playerSkills[skill.getClazz()].get(i).getId() == skill.getId()) {
					playerSkills[skill.getClazz()].set(i, skill);
					found = true;
					break;
				}
			}
			if (!found) {
				playerSkills[skill.getClazz()].add(skill);
			}
			playerSkillName.put(skill.getGroupId(), skill.getName());
		}
	}

	/**
	 * 根据技能组ID查找技能的最高级别。
	 * @param skillGroupId
	 * @return
	 */
	public static int getSkillMaxLevel(int skillGroupId) {
		return skillMaxLevels.getInt(skillGroupId);
	}

	/**
	 * 注册某个职业的初始技能。
	 * @param clazz
	 * @param skill
	 */
	public static void addInitSkill(int clazz, Skill skill) {
		for (int i = 0; i < playerInitSkills[clazz].size(); i++) {
			Skill sk = playerInitSkills[clazz].get(i);
			if (sk.getId() == skill.getId()) {
				playerInitSkills[clazz].set(i, skill);
				return;
			}
		}
		playerInitSkills[clazz].add(skill);
	}

	/**
	 * 根据技能ID查找技能。技能ID=技能组ID<<16|技能等级。
	 * @param id
	 * @return
	 */
	public static Skill getSkill(int id) {
		return skills.get(id);
	}

	/**
	 * 取得某个职业的所有技能。
	 * @param clazz
	 * @return
	 */
	public static List<Skill> getPlayerSkills(int clazz) {
		return playerSkills[clazz];
	}

	/**
	 * 取得所有玩家技能名称。
	 * @return
	 */
	public static IntHashMap<String> getPlayerSkillName() {
		return playerSkillName;
	}

	/**
	 * 取得某个职业的初始技能。
	 * @param clazz
	 * @return
	 */
	public static List<Skill> getPlayerInitSkills(int clazz) {
		return playerInitSkills[clazz];
	}

	/**
	 * 清除技能索引表，等待重新建立。
	 */
	public static void clearSkills() {
		skills.clear();
		for (int i = 0; i < playerSkills.length; i++) {
			playerSkills[i] = new ArrayList<Skill>();
		}
		for (int i = 0; i < playerInitSkills.length; i++) {
			playerInitSkills[i] = new ArrayList<Skill>();
		}
		skillMaxLevels.clear();
		playerSkillName.clear();
	}

	/**
	 * 把一个物品注册到在线物品缓存中去。在线物品缓存用于保证玩家下线后别的玩家依然能查询物品信息。
	 * @param item
	 */
	public static void addGameItemToCached(GameItem item) {
		cachedItems.put(getLong(item.template.id, item.instanceId), item);
	}
	
	/**
	 * 根据物品ID查找缓存的物品信息。
	 * @param itemId
	 * @param instanceId
	 * @return
	 */
	public static GameItem getCachedGameItem(int itemId, int instanceId) {
		return cachedItems.get(getLong(itemId, instanceId));
	}

	/**
	 * 把过期的物品从物品缓存中清除。
	 * @param item
	 */
	public static void removeGameItemFromCache(GameItem item) {
		cachedItems.remove(getLong(item.template.id, item.instanceId));
	}

	/*
	 * 生成物品缓存的KEY = 物品ID << 32 | 物品实例ID。
	 */
	private static long getLong(int i1, int i2) {
		return ((long) i1) << 32 | ( ((long) i2) & 0xFFFFFFFFL);
	}
	
}
