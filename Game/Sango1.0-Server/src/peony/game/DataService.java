package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import net.sf.ehcache.Cache;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import peony.clientguid.ClientGuidService;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.directory.DirectoryService;
import peony.game.drop.DropGroupUtil;
import peony.game.itemenhance.JewelService;
import peony.game.skill.Skill;
import peony.service.Service;
import peony.service.read.BookUtil;
import peony.vm.ASMQuestUtil;

import com.pip.sanguo.data.Animation;
import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.DataChangeHandler;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.GiftGroup;
import com.pip.sanguo.data.Hint;
import com.pip.sanguo.data.HorseType;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.equipment.EquipmentPrefix;
import com.pip.sanguo.data.equipment.SuiteConfig;
import com.pip.sanguo.data.equipment.SuiteConfig_New;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.util.Utils;

public class DataService implements Service, Runnable {
    private static Logger log = Logger.getLogger(DataService.class);

	public ProjectData data;
	public File source;
	private Map<Integer, List<Hint>> hints;
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
		}else if("UNICOM".equals(revision)){
			this.data.branch = "UNICOM";
		}
		this.data.serverMode = true;
		boolean isFake = false;
		if (Server.server != null){
		    isFake = Server.server.isFake;
		}
		if (isFake) {
			this.data.createPathFinder = false;
		}
		this.data.load(source);
		this.hints = this.data.loadHints();
	}
	
	public void shutdown() {
		
	}

	public void startup() throws Exception {
		new Thread(this, "DataService").start();
	}
	
	/**
	 * 定时从分配器更新channel.data，放到client_res目录下。
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(300000L);
			} catch (InterruptedException e) {
			}
			GetMethod method = null;
			try {
				// 从分配器上下载channel.data文件
				if (Server.server.getServiceRegistry().getIpdService() == null) {
					continue;
				}
				String url = Server.server.getServiceRegistry().getIpdService().getMainURL() + "channel.data";
				method = new GetMethod(url);
				method.addRequestHeader( "Connection", "close");
				method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
	            HttpClient httpclient = new HttpClient();
	            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
	            httpclient.getParams().setSoTimeout(30000);
	            int code = httpclient.executeMethod(method);
	            if (code == 200) {
	                InputStream result = method.getResponseBodyAsStream();
	                ByteArrayOutputStream bos = new ByteArrayOutputStream();
	                byte[] buf = new byte[1024];
	                while (true) {
	                	int len = result.read(buf);
	                	if (len < 0) {
	                		break;
	                	} else if (len > 0) {
	                		bos.write(buf, 0, len);
	                	}
	                }
	                buf = bos.toByteArray();
	                
	                // 保存到channel.data文件，并强制清除缓存
	                Utils.saveFileData(new File(source, "client_res/channel.data"), buf);
	                data.forceReloadFile("client_res/channel.data");
	            } else {
	            	log.error("download channel.data failed: " + code);
	            }
			} catch (Exception ex) {
				log.error(ex, ex);
	        } finally {
	        	if (method != null) {
	        		method.releaseConnection();
	        	}
	        }
		}
	}
	
	public Set<String> getNpcs(int areaId, String model){
		if(data.isUseLarge(model))
			return VMapUtil.map2NpcFileNamesLarge.get(areaId);
		else
			return VMapUtil.map2NpcFileNames.get(areaId);
	}
	
	public synchronized GameFile getGameFile(String name, String model) throws IOException{
        int version = data.getFileVersion(name, model);
        byte[] ret = data.downloadFile(name, model);
        if (ret != null) {
            return new GameFile(version, ret, model);
        }
        return null;
    }
	
	public synchronized int getClientDataVersion(String model){
	    return data.getClientDataVersion(model);
	}
	
	public synchronized byte[] getNewClientData(String model){
	    return data.getClientData(model);
	}
	
	public synchronized FileVersion[] versionCompare(String[] files, int[] versions,
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
                if ((version & 0x7FFFFFFF) != versions[i]) {
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
	public void reload(ProjectData proj, String type) throws Exception {
	    log.info("[RELOAD]TYPE[" + type + "]TRY");
	    
	    // 初始化不同数据类型的处理类
	    Map<Class, DataChangeHandler> handlers = new HashMap<Class, DataChangeHandler>();
	    handlers.put(GameArea.class, Server.server.getWorld());
        handlers.put(NPCTemplate.class, new VMapUtil());
        // handlers.put(Quest.class, new ASMQuestUtil());
        handlers.put(GiftGroup.class, Server.server.getServiceRegistry().getGiftService());
        handlers.put(SuiteConfig.class, new ItemUtil());
        handlers.put(SuiteConfig_New.class, new ItemUtil());
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
        boolean needReloadWorldMessageItems = false;
        boolean needReloadPathFinder = false;
        boolean needReloadFile = false;
        if ("file".equals(type)) {
            types = new Class[0];
            needReloadFile = true;
        } else if ("version".equals(type)) {
            types = new Class[0];
            Server.server.getServiceRegistry().getVersionService().reload();
        } else if ("config".equals(type)) {
            types = new Class[0];
            Server.server.reloadConfig();
            Server.server.getServiceRegistry().getModelService().reload();
        } else if ("map".equals(type)) {
            types = new Class[] { GameArea.class, NPCTemplate.class, Animation.class };
            needReloadPathFinder = true;
        } else if ("quest".equals(type)) {
            types = new Class[] { Quest.class };
            needReloadQuests = true;
        } else if ("giftgroup".equals(type)) {
            types = new Class[] { GiftGroup.class };
        } else if ("shop".equals(type)) {
            types = new Class[] { Shop.class };
        } else if ("item".equals(type)) {
            types = new Class[] { SuiteConfig.class,SuiteConfig_New.class, Item.class, Equipment.class, BuffConfig.class, EquipmentPrefix.class };
            needReloadBuffs = true;
            needReloadJewels = true;
            needReloadNoticeItems = true;
            needReloadWorldMessageItems = true;
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
        } else if ("card".equals(type)) {
            types = new Class[] { Card.class };
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
            needReloadWorldMessageItems = true;
            needReloadFile = true;
            needReloadPathFinder = true;
            Server.server.getServiceRegistry().getVersionService().reload();
            Server.server.reloadConfig();
            Server.server.getServiceRegistry().getModelService().reload();
            hints = data.loadHints();
        } else if("randomfaction".equals(type)){
        	types = new Class[0];
        	PlayerUtil.loadRandomFaction();
        } else if("clientguid".equals(type)){
        	types = new Class[0];
        	ClientGuidService service = Server.server.getServiceRegistry().getClientGuidService();
        	service.clearData();
        	service.loadGuids(proj);
        } else if("clientdirectory".equals(type)){
        	types = new Class[0];
        	DirectoryService directoryService = Server.server.getServiceRegistry().getDirectoryService();
        	directoryService.loadDirectorys(proj);
        } else if("fistcharge".equals(type)){
        	types = new Class[0];
        	Server.server.getServiceRegistry().getChargeActivityService().loadConfigs();
        } else if("books".equals(type)){
        	types = new Class[0];
        	BookUtil.clearBooksData();
        	BookUtil.initBook2(proj);
        }else {
            throw new IllegalArgumentException();
        }
        try {
        	if (types.length > 0) {
        		data.reload(proj, types, handlers);
        	}
        	if (needReloadFile) {
        		data.reloadFile();
        	}
        	if (needReloadPathFinder) {
        		data.reloadPathFinder();
        	}
            if (needReloadBuffs) {
                BuffUtil.initBuffs();
            }
            if (needReloadSkills) {
                SkillUtil.initSkills();
                refreshAllPlayerSkills();
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
            if(needReloadWorldMessageItems){
            	ItemUtil.loadWorldMessageItems();
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
	    List<Hint> hts = hints.get(keyType);
	    for (Hint hint : hts) {
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

	/**
	 * 当技能表变化，重新刷新所有缓存玩家身上的已学习技能。
	 */
	public void refreshAllPlayerSkills() {
		Cache cache = Server.server.getServiceRegistry().getPlayerService().cache;
		for (Object key : cache.getKeys()) {
			net.sf.ehcache.Element target = cache.get(key);
			if (target != null) {
				Player p = (Player)target.getObjectValue();
				try {
					refreshPlayerSkill(p, p.skills);
				} catch (Exception e) {
					log.error(e, e);
				}
			}
		}
		for (Player p : ObjectAccessor.players.values()) {
			try {
				refreshPlayerSkill(p, p.skills);
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}
	
	private void refreshPlayerSkill(Player p, Skills ss) throws Exception {
		Field f = Skills.class.getDeclaredField("skills");
		f.setAccessible(true);
		TreeMap<Integer,Skill> skills = (TreeMap<Integer,Skill>)f.get(ss);
		f = Skills.class.getDeclaredField("bookSkills");
		f.setAccessible(true);
		TreeMap<Integer,Skill> bookSkills = (TreeMap<Integer,Skill>)f.get(ss);
		
		refreshPlayerSkill(p, skills);
		refreshPlayerSkill(p, bookSkills);
	}
	
	private void refreshPlayerSkill(Player p, Map<Integer, Skill> map) {
		Object[] arr = map.keySet().toArray();
		for (Object o : arr) {
			int id = ((Integer)o).intValue();
			Skill newSkill = ObjectAccessor.getSkill(id);
			if (newSkill != null) {
				map.put(id, newSkill);
				Buff buff = newSkill.newBuff();
				if (buff != null) {
					p.buffs.removeBuff(buff.getId());
					p.buffs.addBuff(buff);
				}
			}
		}
	}
}
