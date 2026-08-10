package peony.game;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import peony.game.ai.KingShoutRule;
import peony.game.ai.StateCreatureAI;
import peony.game.buff.GodBuff;
import peony.game.buff.NationBuff;
import peony.game.drop.CreditDrop;
import peony.game.drop.Drop;
import peony.game.drop.DropGroupUtil;
import peony.game.drop.ExpDrop;
import peony.game.drop.GroupDrop;
import peony.game.drop.HonorDrop;
import peony.game.drop.ItemDrop;
import peony.game.drop.MoneyDrop;
import peony.game.drop.RateDrop;
import ch.javasoft.util.intcoll.IntHashMap;

import com.pip.mapeditor.data.GameMap;
import com.pip.mapeditor.data.MapFile;
import com.pip.sanguo.data.DataChangeHandler;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.GameAreaInfo;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.item.DropNode;
import com.pip.sanguo.data.map.GameMapExit;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.map.Period;

public class VMapUtil implements DataChangeHandler {
	
	private static final Logger log = Logger.getLogger(VMapUtil.class);
	
	public static final IntHashMap<GameMapDefinition> definitions = new IntHashMap<GameMapDefinition>();
	
	// 统计所有场景的出口
	private static final IntHashMap<GameMapExit[]> exits = new IntHashMap<GameMapExit[]>();
	
	public static final NormalGatherEndCall NORMALGATHERENDCALL = new NormalGatherEndCall();
	
	
	public static GameMapDefinition getDefinition(int mapId){
		return definitions.get(mapId);
	}
	
	public static GameMapExit[] getExits(int mapId) {
		return exits.get(mapId);
	}
	
	@SuppressWarnings("unchecked")
	public static void load() throws Exception{
		List gas = Server.server.getServiceRegistry().getDataService().data
				.getDataListByType(GameArea.class);
		Iterator ite = gas.iterator();
		while (ite.hasNext()) {
			GameArea area = (GameArea) ite.next();
			registerGameArea(area);
		}
	}
	
	public static void registerGameArea(GameArea area) {
	    MapFile mapfile = area.getMapFile();
        GameAreaInfo info = area.getAreaInfo();
        int i = 0;
        for (GameMapInfo mi : info.maps) {
            GameMapDefinition definition = new GameMapDefinition(mi,mapfile.getMaps().get(i++));
            definitions.put(mi.getGlobalID(), definition);
            
            // 统计出口
            List<GameMapExit> tmpList = new ArrayList<GameMapExit>();
            for (GameMapObject gmo : mi.objects) {
            	if (gmo instanceof GameMapExit) {
            		GameMapExit gme = (GameMapExit)gmo;
            		if (gme.exitType < GameMapExit.TYPE_INTERNAL) {
            			tmpList.add(gme);
            		}
            	}
            }
            GameMapExit[] arr = new GameMapExit[tmpList.size()];
            tmpList.toArray(arr);
            exits.put(mi.getGlobalID(), arr);
        }
	}
	
	/**
	 * 当关卡变化时，更新GameMapDefinition对象中的GameMap和GameMapInfo引用。
	 * @param area
	 */
	public static void updateGameArea(GameArea area, List<GameMapDefinition> added, 
	        List<GameMapDefinition> changed, List<GameMapDefinition> removed) {
	    int newMapCount = area.getAreaInfo().maps.size();
	    for (int i = 0; i < 16; i++) {
	        int id = (area.id << 4) | i;
	        GameMapDefinition oldDef = definitions.get(id);
	        if (oldDef != null && i < newMapCount) {
	            // 已经有的场景
                GameMap map = area.getMapFile().getMaps().get(i);
                GameMapInfo mapInfo = area.getAreaInfo().maps.get(i);
	            oldDef.map = map;
	            oldDef.mapInfo = mapInfo;
	            changed.add(oldDef);
	        } else if (i < newMapCount) {
	            // 新增的场景
                GameMap map = area.getMapFile().getMaps().get(i);
                GameMapInfo mapInfo = area.getAreaInfo().maps.get(i);
	            GameMapDefinition definition = new GameMapDefinition(mapInfo, map);
	            definitions.put(id, definition);
	            added.add(definition);
	        } else if (oldDef != null) {
	            // 删除的场景
	            definitions.remove(id);
	            removed.add(oldDef);
	        }
	    }
	}
	
	public static List<String> getAllNpcNames() {
		List<String> ret = new LinkedList<String>();
		for (GameMapDefinition def : definitions.values()) {
			for (GameMapObject go : def.mapInfo.objects) {
				if (go instanceof GameMapNPC) {
					GameMapNPC npc = (GameMapNPC) go;
					if (npc.faction.id == GameObject.FACTION_NEUTRAL
							|| npc.faction.id == GameObject.FACTION_WEI
							|| npc.faction.id == GameObject.FACTION_SHU
							|| npc.faction.id == GameObject.FACTION_WU) {
						String[] ss = npc.name.split("\\|");
						ret.add(ss[0]);
					}
				}
			}
		}
		return ret;
	}
	
	public static VMap create(VMapManager manager,World world,int mapId,int cellWidth,int cellHeight,String revision) {
		GameMapDefinition definition = definitions.get(mapId);
		VMap map = new VMap(manager,world,definition,cellWidth,cellHeight);
		addCreatures(map,definition.mapInfo,revision);
		map.addExits();
		return map;
	}
	
	public static VMap create(VMapManager manager,World world,int mapId,String revision) {
		return create(manager,world,mapId,120,120,revision);
	}
	
	@SuppressWarnings("unchecked")
	public static GameObject addCreature(VMap map,GameMapNPC t,boolean refresh,int liveTime,String revision){
	    return addCreature(map, -1, -1, t, refresh, liveTime,revision);
	}

    @SuppressWarnings("unchecked")
    public static GameObject addCreature(VMap map, int x, int y, GameMapNPC npc,boolean refresh,int liveTime,String revision){
		if (x == -1) {
		    x = npc.x;
		}
		if (y == -1) {
		    y = npc.y;
		}
		if(!isCurrentRevision(npc, revision))
			return null;
		NPCTemplate template = npc.template;
		if (template.type.id == 0) {  //人物，怪物
			Creature creature = new Creature(npc.getGlobalID(),
					npc.name, x, y, map);
			creature.template = template;
			creature.updateTemplate(true);

			creature.faction = npc.faction.id;
			creature.lastMoveTime = Time.currTime;
			creature.initPatrolPath(npc.patrolPath);
			creature.canPass = npc.canPass;
			creature.isFunctional = npc.isFunctional;
			creature.functionName = npc.functionName;
			creature.functionScript = npc.functionScript;
			creature.dieRefreshNPC = npc.dieRefresh;
			creature.searchName = npc.searchName;
			if (creature.searchName.length() == 0) {
				creature.searchName = null;
			}
			if(npc.refreshInterval==-1) {
				creature.refreshTime = -1;
			} else {
				creature.refreshTime = npc.refreshInterval*1000;
			}
			if (liveTime > 0) {
			    creature.disappearTime = Time.currTime + liveTime;
			} else if (npc.liveTime > 0) {
			    creature.disappearTime = Time.currTime + npc.liveTime * 1000;
			} else if (npc.liveTime == -1){
				creature.disappearTime = -1;
			}
			creature.dynamicRefresh = npc.dynamicRefresh;
			creature.linkDistance = npc.linkDistance;
			creature.isGuard = npc.isGuard;
			creature.isStaticField = npc.isStatic;
//			creature.skill = null;
			creature.skill = ObjectAccessor.getSkill(1);
			creature.setupTouchAction();
			if(!npc.canAttack){
				creature.buffs.addBuff(new GodBuff());  //免疫所有攻击
				creature.canBeAttacked = false;
			}
//			if(creature.id==0xd1015)
			// 配置怪物AI
//			if(creature.id==0x200002)
			if ("empty".equals(template.aiClass)) {
				creature.setAI(null);
			} else if ("".equals(template.aiClass)) {
			    creature.setAI(new StateCreatureAI(creature));
			} else if ("general".equals(template.aiClass)) {
			    // 带逻辑配置的通用AI
			    StateCreatureAI ai = new StateCreatureAI(creature);
				ai.config(template.aiRules);
			    creature.setAI(ai);
			} else if ("gohome".equals(template.aiClass)) {
			    // 带逻辑配置的护送NPC AI
//				creature.patrolSpeed = 25;
//				creature.speed = 25;
			    StateCreatureAI ai = new StateCreatureAI(creature);
			    ai.setGoHome(true);
                ai.config(template.aiRules);
                creature.setAI(ai);
			} else if ("passive".equals(template.aiClass)) {
			    // 被动挨打的AI
			    StateCreatureAI ai = new StateCreatureAI(creature);
                ai.setPassive(true);
                ai.config(template.aiRules);
                creature.setAI(ai);
			} else {
			    log.error("Invalid ai config: " + template.aiClass);
			}
			if(NPCUtil.getKingFaction(creature)!=0&&creature.ai!=null){
				((StateCreatureAI)creature.ai).addAIRule(new KingShoutRule((StateCreatureAI)creature.ai));
				NationBuff buff = Server.server.getServiceRegistry().getNationService().getNationByFaction(creature.faction).buff;
				if(buff!=null){
					creature.buffs.addBuff(buff);
					creature.refreshProperties(true);
				}
			}
			if(!npc.visible&&!refresh)
				return null;
			if(npc.periods.size()>0){
				creature.refreshPeriods = npc.periods;
				creature.state |= GameObject.STATE_DIE;
				creature.dieTime = Time.elapseTime(Period.getNextTimeInPeriods(Calendar.getInstance(), creature.refreshPeriods).getTimeInMillis());
			}
			map.addCreature(creature);
			ObjectAccessor.addGameObject(creature);
			return creature;
		}else if(template.type.id==3){ //采集资源
			GatherUnit gu = new GatherUnit(NORMALGATHERENDCALL);
			gu.id = npc.getGlobalID();
			gu.x = x;
			gu.y = y;
			gu.name = npc.name;
			gu.refreshTime = npc.refreshInterval*1000;
            gu.isStaticField = npc.isStatic;
            gu.level = template.level;
			gu.template = npc.template;
			gu.updateTemplate();
			if (!npc.visible&&!refresh)
				return null;
			map.addGatherUnit(gu);
			ObjectAccessor.addGameObject(gu);
			return gu;
		}
		return null;
	}
    
    protected static boolean isCurrentRevision(GameMapNPC npc, String revision){
    	if(revision == null)
    		return true;
    	if(npc.revision==null||npc.revision.length()==0)
    		return true;
		if (!npc.revision.startsWith("!")) {
			return npc.revision.equals(revision);
		}else{
			return !npc.revision.substring(1).equals(revision);
		}
    }
	
	
	protected static void addCreatures(VMap map, GameMapInfo info,String revision) {
		for (GameMapObject go : info.objects) {
			if (go instanceof GameMapNPC) {
				addCreature(map, (GameMapNPC)go, false, 0, revision);
			}
		}
	}
	
	public static void createFall(Fall fall, NPCTemplate template) {
		for (DropNode node : template.dropGroups) {
			if (node.type == DropNode.TYPE_ITEM
					|| node.type == DropNode.TYPE_EQUIPMENT) {
				ItemTemplate t = ObjectAccessor.getItemTemplate(node.id);
				if (t == null)
					throw new IllegalArgumentException();
				ItemDrop d = new ItemDrop(t, node.isTask ? node.taskId : -1,
						node.quantityMin, node.quantityMax);
				fall.addDrop(new RateDrop(node.dropRate, d));
			} else if (node.type == DropNode.TYPE_DROPGROUP) {
				GroupDrop d = ObjectAccessor.getGroupDrop(node.id);
				if (d == null)
					throw new IllegalArgumentException();
				fall.addDrop(new RateDrop(node.dropRate, d, node.quantityMin, node.quantityMax));
			} else {
				throw new IllegalArgumentException();
			}
		}
		if (template.money > 0) {
			MoneyDrop d = new MoneyDrop(-1, template.money, template.money);
			fall.addDrop(d);
		}
		if (template.exp > 0) {
			ExpDrop d = new ExpDrop(-1, template.exp, template.exp);
			fall.addDrop(d);
		}
		if (template.credit > 0) {
		    HonorDrop d = new HonorDrop(-1, template.credit, template.credit);
		    fall.addDrop(d);
		}
		if (template.type.id==0){ //只有怪物才加世界掉落,世界掉落率是5%
		    Drop[] worldDrops = DropGroupUtil.getWorldDrop(template.level);
			for (Drop d : worldDrops) {
			    fall.addDrop(d);
			}
		}
		if (template.rank > 0) {
		    CreditDrop d = new CreditDrop(-1, template.rank, template.rank);
		    fall.addDrop(d);
		}
		fall.isPartyModel = template.partyModel;
		fall.partyModelRatio = template.partyModelRatio;
	}

    /**
     * 添加新对象通知。
     * @param obj 新添加的对象
     */
    public void dataObjectAdded(DataObject obj) {
        if (obj instanceof NPCTemplate) {
            // 新增模板，不影响已有数据
        }
    }
    
    /**
     * 对象被删除通知。
     * @param obj 被删除的老对象
     */
    public void dataObjectRemoved(DataObject obj) {
        if (obj instanceof NPCTemplate) {
            // 模板被删除，删除所有使用此模板的NPC
            for (GameObject gobj : ObjectAccessor.instanceid2objects.values()) {
                if (gobj instanceof Creature) {
                    Creature c = (Creature)gobj;
                    if (c.template.id == obj.id) {
                        c.disappearTime = Time.currTime;
                    }
                } else if (gobj instanceof GatherUnit) {
                    GatherUnit gu = (GatherUnit)gobj;
                    if (gu.template.id == obj.id) {
                        gu.disappearTime = Time.currTime;
                    }
                }
            }
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
        if (newobj instanceof NPCTemplate) {
            // 模板被修改，更新所有使用此模板的NPC的属性
            for (GameObject gobj : ObjectAccessor.instanceid2objects.values()) {
                if (gobj instanceof Creature) {
                    Creature c = (Creature)gobj;
                    if (c.template.id == newobj.id) {
                        c.updateTemplate(false);
                    }
                } else if (gobj instanceof GatherUnit) {
                    GatherUnit gu = (GatherUnit)gobj;
                    if (gu.template.id == newobj.id) {
                        gu.updateTemplate();
                    }
                }
            }
        }
    }
}
