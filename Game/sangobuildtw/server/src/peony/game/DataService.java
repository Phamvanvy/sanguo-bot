package peony.game;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.game.buff.BuffUtil;
import peony.game.drop.DropGroupUtil;
import peony.game.itemenhance.JewelService;
import peony.service.Service;
import peony.vm.ASMQuestUtil;

import com.pip.sanguo.data.*;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.equipment.EquipmentPrefix;
import com.pip.sanguo.data.equipment.SuiteConfig;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.SkillConfig;

public class DataService implements Service{
    private static Logger log = Logger.getLogger(DataService.class);

	public ProjectData data;
	public File source;
	private List<Hint> hints;
	protected String revision;
	protected static final Random RND = new Random();
	
	public DataService(File source,String revision) throws Exception{
		this.source = source;
		this.source = source;
		this.data = new ProjectData();
		this.revision = revision;
		if("CMCC".equals(revision)){
			this.data.branch = "CMCC";
		}else if("CHINATEL".equals(revision)){
			this.data.branch = "TELECOM";
		}
		this.data.serverMode = true;
		this.data.load(source);
		this.hints = this.data.loadHints();
	}
	
	public void shutdown() {
		
	}

	public void startup() throws Exception {
		
	}
	
	public GameFile getGameFile(String name, String model) throws IOException{
        int version = data.getFileVersion(name, model);
        byte[] ret = data.downloadFile(name, model);
        if (ret != null) {
            return new GameFile(version, ret, model);
        }
        return null;
    }
	
	public int getClientDataVersion(String model){
	    return data.getClientDataVersion(model);
	}
	
	public byte[] getNewClientData(String model){
	    return data.getClientData(model);
	}
	
	public FileVersion[] versionCompare(String[] files, int[] versions,
            String model) {
		// 过滤掉重复版本号
		HashMap<String, Integer> lastIndex = new HashMap<String, Integer>();
		for (int i = 0; i < files.length; i++) {
			if (lastIndex.containsKey(files[i])) {
				// 出现重复
				int li = lastIndex.get(files[i]);
				int v1 = versions[li] & 0x7FFFFFFF;
				int v2 = versions[i] & 0x7FFFFFFF;
				if (v1 < v2) {
					files[li] = null;
					lastIndex.put(files[i], i);
				} else {
					files[i] = null;
				}
			} else {
				lastIndex.put(files[i], i);
			}
		}
		
        List<FileVersion> l = new LinkedList<FileVersion>();
        Hashtable<String, String> clientTable = new Hashtable<String, String>();
        for (int i = 0; i < files.length; i++) {
        	if (files[i] == null) {
        		continue;
        	}
            int version = data.getFileVersion(files[i], model);
            if (version == 0) { //如果没找到则原样返回，有可能旧版的文件被删
                //l.add(new FileVersion(files[i], versions[i]));
            } else {
                if (version != versions[i]) {
                    if(data.getIsClientNeed(files[i], model)){
                        l.add(new FileVersion(files[i], version | 0x80000000));
                    }else{
                        l.add(new FileVersion(files[i], version));
                    }
                }
            }
            clientTable.put(files[i], files[i]);
        }
        //查找所有服务器上认为需要客户端need，而客户端没有发上来的文件
        Hashtable<String, Boolean> clientNeedTable = data.getAllClientNeedTable(model);
        if(clientNeedTable != null){
            Iterator<String> it = clientNeedTable.keySet().iterator();
            while(it.hasNext()){
                String name = it.next();
                Boolean need = clientNeedTable.get(name);
                if(need && !clientTable.containsKey(name)){
                    l.add(new FileVersion(name, data.getFileVersion(name, model) | 0x80000000));
                }
            }
        }
        FileVersion[] ret = new FileVersion[l.size()];
        l.toArray(ret);
        return ret;
    }
	
	/**
	 * 重载数据。
	 * @param type 重载类型，包括：
	 * version:
	 * config:
	 * file:
	 * map: GameArea, NPCTemplate, Animation
     * quest: Quest
     * giftgroup: GiftGroup
     * shop: Shop
     * item: SuiteConfig, Item, Equipment, BuffConfig, EquipmentPrefix
     * title: Title, BuffConfig
     * skill: SkillConfig, BuffConfig
     * formula: Formula
     * dropgroup: DropGroup
     * horse: HorseType
     * hints: 小提示
     * all: GameArea, NPCTemplate, Animation, Quest, GiftGroup, Shop, SuiteConfig, Title, 
     *      Formula, Item, DropGroup, SkillConfig, Equipment, BuffConfig, HorseType, 
     *      EquipmentPrefix
	 */
	public void reload(String type) throws Exception {
	    log.info("[RELOAD]TYPE[" + type + "]TRY");
	    
	    // 初始化不同数据类型的处理类
	    Map<Class, DataChangeHandler> handlers = new HashMap<Class, DataChangeHandler>();
	    handlers.put(GameArea.class, Server.server.getWorld());
        handlers.put(NPCTemplate.class, new VMapUtil());
        // handlers.put(Quest.class, new ASMQuestUtil());
        handlers.put(GiftGroup.class, Server.server.getServiceRegistry().getGiftService());
        handlers.put(SuiteConfig.class, new ItemUtil());
        handlers.put(Title.class, new TitleUtil());
        handlers.put(Item.class, new ItemUtil());
        handlers.put(DropGroup.class, new DropGroupUtil());
        handlers.put(Equipment.class, new ItemUtil());
        handlers.put(HorseType.class, new HorseUtil());

        // 根据参数设定重载的类
        Class[] types;
        boolean needReloadBuffs = false;
        boolean needReloadSkills = false;
        boolean needReloadJewels = false;
        boolean needReloadQuests = false;
        boolean needReloadNoticeItems = false;
        if ("file".equals(type)) {
            types = new Class[0];
        } else if ("version".equals(type)) {
            types = new Class[0];
            Server.server.getServiceRegistry().getVersionService().reload();
        } else if ("config".equals(type)) {
            types = new Class[0];
            Server.server.reloadConfig();
            Server.server.getServiceRegistry().getModelService().reload();
        } else if ("map".equals(type)) {
            types = new Class[] { GameArea.class, NPCTemplate.class, Animation.class };
        } else if ("quest".equals(type)) {
            types = new Class[] { Quest.class };
            needReloadQuests = true;
        } else if ("giftgroup".equals(type)) {
            types = new Class[] { GiftGroup.class };
        } else if ("shop".equals(type)) {
            types = new Class[] { Shop.class };
        } else if ("item".equals(type)) {
            types = new Class[] { SuiteConfig.class, Item.class, Equipment.class, BuffConfig.class, EquipmentPrefix.class };
            needReloadBuffs = true;
            needReloadJewels = true;
            needReloadNoticeItems = true;
        } else if ("title".equals(type)) {
            types = new Class[] { com.pip.sanguo.data.Title.class, BuffConfig.class };
            needReloadBuffs = true;
        } else if ("skill".equals(type)) {
            types = new Class[] { SkillConfig.class, BuffConfig.class };
            needReloadBuffs = true;
            needReloadSkills = true;
        } else if ("formula".equals(type)) {
            types = new Class[] { Formula.class };
        } else if ("dropgroup".equals(type)) {
            types = new Class[] { DropGroup.class };
        } else if ("horse".equals(type)) {
            types = new Class[] { HorseType.class };
        } else if ("hints".equals(type)) {
            hints = data.loadHints();
            types = new Class[0];
        } else if ("all".equals(type)) {
            types = ProjectData.supportDataClasses;
            needReloadBuffs = true;
            needReloadSkills = true;
            needReloadJewels = true;
            needReloadQuests = true;
            needReloadNoticeItems = true;
            Server.server.getServiceRegistry().getVersionService().reload();
            Server.server.reloadConfig();
            Server.server.getServiceRegistry().getModelService().reload();
            hints = data.loadHints();
        } else {
            throw new IllegalArgumentException();
        }
        try {
            data.reload(types, handlers);
            if (needReloadBuffs) {
                BuffUtil.initBuffs();
            }
            if (needReloadSkills) {
                SkillUtil.initSkills();
            }
            if (needReloadJewels) {
                JewelService js = (JewelService)Server.server.getServiceRegistry().getService(JewelService.class);
                js.reload();
            }
            if (needReloadQuests) {
            	ASMQuestUtil.load();
            }
            if (needReloadNoticeItems) {
            	ItemUtil.loadNoticeItems();
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        
        log.info("[RELOAD]TYPE[" + type + "]OK");
	}
	
	/**
	 * 为指定用户随机挑选一条小提示。
	 * @param p
	 * @return
	 */
	public String getHint(Player p) {
	    List<Hint> candidates = new ArrayList<Hint>();
	    int totalWeight = 0;
	    int keyType = p == null ? -1 : p.getKeyboardType();
	    int mType = p == null ? -1 : p.getMouseType();
	    int level = p == null ? 1 : p.level;
	    for (Hint hint : hints) {
	    	if (hint.keyboardType != -1 && hint.keyboardType != keyType) {
	    		continue;
	    	}
	    	if (hint.mouseType != -1 && hint.mouseType != mType) {
	    		continue;
	    	}
	        if (hint.minLevel <= level && hint.maxLevel >= level) {
	            candidates.add(hint);
	            totalWeight += hint.weight;
	        }
	    }
	    if (totalWeight > 0) {
	        int w = RND.nextInt(totalWeight);
	        for (Hint hint : candidates) {
	            w -= hint.weight;
	            if (w < 0) {
	                return hint.message;
	            }
	        }
	    }
	    return null;
	}
}
