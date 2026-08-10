package peony.clientguid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import peony.game.OpCode;
import peony.game.Player;
import peony.game.skill.Skill;
import peony.net.Packet;
import peony.service.account.Account;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;

import com.pip.sanguo.data.clientEvent.ClientEvent;
import com.pip.sanguo.data.clientEvent.EventItem;

/**
 * 客户端定制引导，每一个条件对应一个函数（函数名跟编辑器条件对应的函数名一致，
 * 系统用反射方法调用对应函数），函数返回true符合条件下发动作序列。
 * @author dchen
 */
public class ClientGuid {
	
	static class Guid{
		/** 引导ID */
		public int id;
		/** 引导类型 */
		public int type;
		/** 引导UI版本类型 */
		public int uiType;
		/** 阵营 */
		public int faction;
		/** 是否重复 */
		public boolean repeat;
		/** 重复时间（秒） */
		public long repeatDuration;
		/** 引导条件集合1 */
		public List<GuidTrigger> triggerList1 = new ArrayList<GuidTrigger>();
		/** 引导条件集合2 */
		public List<GuidTrigger> triggerList2 = new ArrayList<GuidTrigger>();
		/** 动作序列 */
		public List<EventItem> eventItems = new ArrayList<EventItem>();
		
		public int minLevel;
		public int maxLevel;
		
		@SuppressWarnings("unchecked")
		public static Class getParamClass(int paramType) throws Exception{
			switch(paramType){
			case 0:
				return Integer.class;
			case 1:
				return String.class;
			}
			throw new Exception(peony.Messages.STRING_01095);
		}
	}
	
	private static final Logger log = Logger.getLogger(ClientGuid.class);
	
	/** 所有引导的集合 */
	public static Map<Integer, Guid> id2guid = new HashMap<Integer, Guid>();
	public static Guid[] guids;
	
	public static Map<String, Method> methods = new HashMap<String, Method>();
	
	/** 玩家 */
	protected Player player;
	
	/** 引导变量池:key为triggerId或者guidId_triggerId */
	protected Map<String, int[]> varStores = new HashMap<String, int[]>();
	
	/** 记录引导成功次数 */
	public Map<Integer, Integer> guidRecords = new HashMap<Integer, Integer>();
	
	/** 记录引导发送次数 */
	protected Map<Integer, Integer> guidRecords1 = new HashMap<Integer, Integer>();
	
	/** 上次引导时间 */
	protected Map<Integer, Long> lastGuidTime = new HashMap<Integer, Long>();
	
	protected int eventUiType = 0;	//当前客户端的UI类型
	
	public ClientGuid(Player owner){
		this.player = owner;
	}
	
	public void update(int diff){
		if(player.systemState!=Player.SYSTEMSTATE_READY)
			return;
		for(Guid guid : guids){
			if(guidRecords.get(guid.id)!=null && guidRecords.get(guid.id).intValue()>0)
				continue;
			try {
				if(guid.uiType != eventUiType && (guid.uiType != ClientEvent.UI_TYPE_COMMAND || eventUiType == ClientEvent.UI_TYPE_NEWBLUE))
					continue;
				if(guid.faction!=0 && guid.faction!=player.faction)
					continue;
				
				if(player.level<guid.minLevel || player.level>guid.maxLevel)
					continue;
				//trigger1集合进行&&判断
				boolean result1 = false;
				for(GuidTrigger trigger : guid.triggerList1){
					result1 = executeMethod(diff, guid.id, trigger.id, trigger.functionName, trigger.paramType, trigger.paramValue, trigger.paramSign);
					if(!result1)
						break;
				}
				//trigger2集合进行&&判断
				boolean result2 = false;
				for(GuidTrigger trigger : guid.triggerList2){
					result2 = executeMethod(diff, guid.id, trigger.id, trigger.functionName, trigger.paramType, trigger.paramValue, trigger.paramSign);
					if(!result2)
						break;
				}
				//trigger1集合和trigger2集合进行||判断
				if(result1 || result2){
					if(!guid.repeat){
						if(guidRecords1.get(guid.id)==null || guidRecords1.get(guid.id)==0)
							sendClientGuid(guid);
					}else{
						if(lastGuidTime.get(guid.id)==null || System.currentTimeMillis()-lastGuidTime.get(guid.id)>guid.repeatDuration*1000L)
							sendClientGuid(guid);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}
	
	
	
	/** 条件方法执行 */
	private boolean executeMethod(int diff, int guidId, int triggerId, String functionName, int[] paramTypes, Object[] paramValue, int[] paramSign){
		Method method = null;
		boolean result = false;
		try {
			method = methods.get(functionName);
			if(method==null){
				method = ClientGuid.class.getMethod(functionName, Integer.class, Integer.class, Integer.class, int[].class, Object[].class, int[].class);
				methods.put(functionName, method);
			}
			result = (Boolean) method.invoke(this, diff, guidId, triggerId, paramTypes, paramValue, paramSign);
		} catch (Exception e) {
		}
		return result;
	}
	
	/** 往客户端发送引导动作 */
	private void sendClientGuid(Guid guid){
		try {
			if(guidRecords1.get(guid.id)==null)
				guidRecords1.put(guid.id, 1);
			else
				guidRecords1.put(guid.id, guidRecords1.get(guid.id)+1);
			if(guid.repeat)
				lastGuidTime.put(guid.id, System.currentTimeMillis());
			Packet pt = new Packet(OpCode.CLIENT_GUID_ACTION_SERVER);
			pt.putInt(guid.id);
			pt.put(guid.eventItems.size());
			for(EventItem item : guid.eventItems){
				pt.putShort(item.promptType);
				pt.putString(item.promptParam);
				pt.putShort(item.actionType);
				int mouseType = player.getAccount().getMouseType();
				if(mouseType==1){
					pt.putString(item.eventDesTouch);
					pt.putString(item.signDescTouch);
				}else{
					pt.putString(item.eventDesKey);
					pt.putString(item.signDescKey);
				}
			}
			player.send(pt);
		} catch (Exception e) {
		}
	}
	
	protected Guid getGuidById(int guidId){
		return id2guid.get(guidId);
	}
	
	protected void setVarStore(String key, int[] arr){
		varStores.put(key, arr);
	}
	
	protected void removeVarStore(String key){
		varStores.remove(key);
	}
	
	protected int[] getVarStore(String key){
		return varStores.get(key);
	}
	
	public void clearLogOutData(){
		guidRecords1.clear();
	}
	
	public void setEventUiType(){
		Account account = this.player.getAccount();
		if(account!=null){
			String mod = null;
			if(account.getUiModel()!=null)
				mod = account.getUiModel().trim();
			if(mod!=null){
				if(mod.equals("AndroidNew") || mod.equals("AndroidLargeNew") || mod.equals("iOSNewUI") || mod.equals("iOSNewUILarge") || mod.equals("Nokia5800New") || mod.equals("Nokia5800NewC")){
					this.eventUiType = ClientEvent.UI_TYPE_NEWBLUE;
				}else if(mod.equals("NewUI_AndroidLarge") || mod.equals("NewUI_Android") || mod.equals("NewUI_iOS") || mod.equals("NewUI_iOSLarge")){
					this.eventUiType = ClientEvent.UI_TYPE_NEW2_YEELOW;
				}else{
					this.eventUiType = ClientEvent.UI_TYPE_JAVA_OLD;
				}
			}else{
				this.eventUiType = ClientEvent.UI_TYPE_NEWBLUE;
			}
		}
	}
	
	/** 根据事件ID获取引用此事件的所有引导的变量池的key */
	public List<String> getVarKeys(String triggerId){
		List<String> varKeys = new ArrayList<String>();
		for(String key : varStores.keySet()){
			if(key.equals(triggerId))
				varKeys.add(key);
			else{
				String[] keyArr = key.split("_");
				if(keyArr.length>1 && keyArr[1].equals(triggerId))
					varKeys.add(key);
			}
		}
		return varKeys;
	}
	
	public static ClientGuid getFromDBByte(byte[] bytes, Player owner){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		try{
			ClientGuid clientGuid = new ClientGuid(owner);
			dis.read(); //version
			int guidRecordSize = dis.readInt();
			for(int i=0;i<guidRecordSize;i++){
				int guidId = dis.readInt();
				int record = dis.readInt();
				clientGuid.guidRecords.put(guidId, record);
			}
			int lastGuidTimeSize = dis.readInt();
			for(int i=0;i<lastGuidTimeSize;i++){
				int guidId = dis.readInt();
				long lastTime = dis.readLong();
				clientGuid.lastGuidTime.put(guidId, lastTime);
			}
			int varStoreSize = dis.readInt();
			for(int i=0;i<varStoreSize;i++){
				String guidId = dis.readUTF();
				int arrSize = dis.readByte();
				int[] arr = new int[arrSize];
				for(int j=0;j<arrSize;j++){
					arr[j] = dis.readInt();
				}
				clientGuid.setVarStore(guidId, arr);
			}
			return clientGuid;
		}catch(Exception e){
			e.printStackTrace();
			return new ClientGuid(owner);
		}
	}
	
	public byte[] toDBByte(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(1); //version
            dos.writeInt(guidRecords.keySet().size());
            for(int guidId : guidRecords.keySet()){
            	dos.writeInt(guidId);
            	dos.writeInt(guidRecords.get(guidId));
            }
            dos.writeInt(lastGuidTime.keySet().size());
            for(int guidId : lastGuidTime.keySet()){
            	dos.writeInt(guidId);
            	dos.writeLong(lastGuidTime.get(guidId));
            }
            dos.writeInt(varStores.keySet().size());
            for(String guidId : varStores.keySet()){
            	dos.writeUTF(guidId);
            	dos.writeByte(varStores.get(guidId).length);
            	for(int i=0;i<varStores.get(guidId).length;i++){
            		dos.writeInt(varStores.get(guidId)[i]);
            	}
            }
            dos.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
	}
	
	/** 玩家和配偶一起杀死怪物 */
	public boolean g_killWithMate(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int npcId = ((Integer)paramValue[0]).intValue();
		int count = ((Integer)paramValue[1]).intValue();
		int c = player.getKillCreatureCount(npcId, player.getMateID());
		if(c>0){
			int[] store = getVarStore(String.valueOf(triggerId));
			if(store==null){
				store = new int[1];
				store[0] = c;
			}else{
				store[0] = store[0] + c;
			}
			setVarStore(String.valueOf(triggerId), store);
			if(store[0]>=count){
				removeVarStore(String.valueOf(triggerId));
				return true;
			}
		}
		return false;
	}
	
	/** 玩家是否完成某任务 */
	public boolean g_finisnQuest(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int questId = ((Integer)paramValue[0]).intValue();
		int type = ((Integer)paramValue[1]).intValue();
		int ok = player.asmVm.finishConditionOk(questId);
		if(type==1)
			return (ok==1 ? false : true);
		else
			return (ok==1 ? true : false);
	}
	
	/** 玩家升级 */
	public boolean g_playerLevelUp(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		if(getVarStore(String.valueOf(triggerId))!=null && getVarStore(String.valueOf(triggerId))[0]==1){
			if(player.bag.getGameItemCount(1007325)>=1)
				return true;
		}
		return false;
	}
	
	/** 基本属性 */
	public boolean g_checkRoleAttribute(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		boolean ok = false;
		int count = 0;
		//级别
		Object value0 = null;
		try {
			value0 = paramValue[0];
		} catch (Exception e) {}
		if(value0!=null){
			int level = ((Integer)paramValue[0]).intValue();
			int sign0 = paramSign[0];
			if(sign0==0){
				ok = (count==0?true:ok) && player.level<level;
			}else if(sign0==1){
				ok = (count==0?true:ok) && player.level==level;
			}else if(sign0==2){
				ok = (count==0?true:ok) && player.level>level;
			}
			count++;
		}
		//金钱
		Object value1 = null;
		try {
			value1 = paramValue[1];
		} catch (Exception e) {}
		if(value1!=null){
			int money = ((Integer)paramValue[1]).intValue();
			int sign0 = paramSign[1];
			if(sign0==0){
				ok = (count==0?true:ok) && player.money<money;
			}else if(sign0==1){
				ok = (count==0?true:ok) && player.money==money;
			}else if(sign0==2){
				ok = (count==0?true:ok) && player.money>money;
			}
			count++;
		}
		//血
		Object value2 = null;
		try {
			value2 = paramValue[2];
		} catch (Exception e) {}
		if(value2!=null){
			int hp = ((Integer)paramValue[2]).intValue();
			int sign0 = paramSign[2];
			if(sign0==0){
				ok = (count==0?true:ok) && player.hp<hp*player.maxhp/100;
			}else if(sign0==1){
				ok = (count==0?true:ok) && player.hp==hp*player.maxhp/100;
			}else if(sign0==2){
				ok = (count==0?true:ok) && player.hp>hp*player.maxhp/100;
			}
			count++;
		}
		//蓝
		Object value3 = null;
		try {
			value3 = paramValue[3];
		} catch (Exception e) {}
		if(value3!=null){
			int mp = ((Integer)paramValue[3]).intValue();
			int sign0 = paramSign[3];
			if(sign0==0){
				ok = (count==0?true:ok) && player.mp<mp*player.maxmp/100;
			}else if(sign0==1){
				ok = (count==0?true:ok) && player.mp==mp*player.maxmp/100;
			}else if(sign0==2){
				ok = (count==0?true:ok) && player.mp>mp*player.maxmp/100;
			}
			count++;
		}
		//力量
		Object value4 = null;
		try {
			value4 = paramValue[4];
		} catch (Exception e) {}
		if(value4!=null){
			int strength = ((Integer)paramValue[4]).intValue();
			int sign0 = paramSign[4];
			if(sign0==0){
				ok = (count==0?true:ok) && player.strength<strength;
			}else if(sign0==1){
				ok = (count==0?true:ok) && player.strength==strength;
			}else if(sign0==2){
				ok = (count==0?true:ok) && player.strength>strength;
			}
			count++;
		}
		//敏捷
		Object value5 = null;
		try {
			value5 = paramValue[5];
		} catch (Exception e) {}
		if(value5!=null){
			int agility = ((Integer)paramValue[5]).intValue();
			int sign0 = paramSign[5];
			if(sign0==0){
				ok = (count==0?true:ok) && player.agility<agility;
			}else if(sign0==1){
				ok = (count==0?true:ok) && player.agility==agility;
			}else if(sign0==2){
				ok = (count==0?true:ok) && player.agility>agility;
			}
			count++;
		}
		//智力
		Object value6 = null;
		try {
			value6 = paramValue[6];
		} catch (Exception e) {}
		if(value6!=null){
			int intellect = ((Integer)paramValue[6]).intValue();
			int sign0 = paramSign[6];
			if(sign0==0){
				ok = (count==0?true:ok) && player.intellect<intellect;
			}else if(sign0==1){
				ok = (count==0?true:ok) && player.intellect==intellect;
			}else if(sign0==2){
				ok = (count==0?true:ok) && player.intellect>intellect;
			}
			count++;
		}
		//体力
		Object value7 = null;
		try {
			value7 = paramValue[7];
		} catch (Exception e) {}
		if(value7!=null){
			int stamina = ((Integer)paramValue[7]).intValue();
			int sign0 = paramSign[7];
			if(sign0==0){
				ok = (count==0?true:ok) && player.stamina<stamina;
			}else if(sign0==1){
				ok = (count==0?true:ok) && player.stamina==stamina;
			}else if(sign0==2){
				ok = (count==0?true:ok) && player.stamina>stamina;
			}
			count++;
		}
		return ok;
	}
	
	/** 玩家获得某个物品 */
//	public boolean g_getItem(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
//		int itemId = ((Integer)paramValue[0]).intValue();
//		int[] store = getVarStore(String.valueOf(triggerId));
//		if(store!=null && store[0]==itemId)
//			return true;
//		return false;
//	}
	
	/** 玩家背包中拥有某个物品 */
	public boolean g_getItem(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		String items = (String)paramValue[0];
		boolean has = false;
		if(items!=null){
			String[] ss = items.split(",");
			for(String s : ss){
				try {
					int itemId = Integer.parseInt(s);
					int count = player.bag.getGameItemCount(itemId);
					if(count>0){
						has = true;
						break;
					}
				} catch (Exception e) {
				}
			}
		}
		return has;
	}
	
	/** 玩家靠近某位置 */
	public boolean g_closeToNPC(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int mapId = ((Integer)paramValue[0]).intValue();
		int x = ((Integer)paramValue[1]).intValue();
		int y = ((Integer)paramValue[2]).intValue();
		int dis = ((Integer)paramValue[3]).intValue();
		if(player.map.getId()==mapId){
			if(Math.abs(player.x-x)<=dis && Math.abs(player.y-y)<=dis)
				return true;
		}
		return false;
	}
	
	/** 玩家进入场景 */
	public boolean m_enterScene(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int mapId = ((Integer)paramValue[0]).intValue();
		int[] store = getVarStore(String.valueOf(triggerId));
		if(store[0]==mapId)
			return true;
		return false;
	}
	
	/** 玩家杀死怪物 */
	public boolean m_kILLMonsterID(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int npcId = ((Integer)paramValue[0]).intValue();
		int count = ((Integer)paramValue[1]).intValue();
		int c = player.getKillCreatureCount(npcId);
		String varKey = guidId.intValue() + "_" + triggerId;
		int[] store = getVarStore(varKey);
		if(store==null){
			store = new int[]{c};
		}else{
			store[0] = store[0] + c;
		}
		setVarStore(varKey, store);
		if(store[0]>=count) 
			return true;
		return false;
	}
	
	/** 玩家被怪物杀死 */
	public boolean m_monsterKillPlayer(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		return player.dieCause == 2 ? true : false;
	}
	
	/** 玩家杀死其他玩家 */
	public boolean m_playerKillOTHEN(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int[] arr = getVarStore(String.valueOf(triggerId));
		if(arr!=null && arr[0]>0)
			return true;
		return false;
	}
	
	/** 玩家被其他玩家杀死 */
	public boolean m_otherKillPlayer(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int[] arr = getVarStore(String.valueOf(triggerId));
		if(arr!=null && arr[0]>0)
			return true;
		return false;
	}
	
	/** 完成某一事件 */
	public boolean g_finisnClientEvent(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int lis_guidId = ((Integer)paramValue[0]).intValue();
		int recordCount = 0;
		try {
			recordCount = guidRecords.get(lis_guidId);
		} catch (Exception e) {}
		if(recordCount>0)
			return true;
		return false;
	}
	
	/** 玩家职业 */
	public boolean g_roleJob(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int clazz = ((Integer)paramValue[0]).intValue();
		if(player.clazz==clazz)
			return true;
		return false;
	}
	
	/** 未完成某一事件 */
	public boolean g_noFinisnClientEvent(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int lis_guidId = ((Integer)paramValue[0]).intValue();
		int recordCount = 0;
		try {
			recordCount = guidRecords.get(lis_guidId);
		} catch (Exception e) {}
		if(recordCount==0)
			return true;
		return false;
	}
	
	/** 正在进行某一任务 */
	public boolean g_doingQuest(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int questId = ((Integer)paramValue[0]).intValue();
		if(player.asmVm.hasTask(questId)==1)
			return true;
		return false;
	}
	
	/** 玩家有某个装备但未装配 */
	public boolean g_notEquipItem(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		if(diff.intValue()%5==0){
			int equipmentId = ((Integer)paramValue[0]).intValue();
			if(player.equipments.find(equipmentId)==null && player.bag.getGameItemCount(equipmentId)>0)
				return true;
		}
		return false;
	}
	
	/** 玩家阵营 */
	public boolean g_roleCamp(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int factionId = ((Integer)paramValue[0]).intValue();
		if(factionId==0 || player.faction==factionId)
			return true;
		return false;
	}
	
	/** 提交已完成的任务 */
	public boolean g_submitFinisnQuest(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		if(diff.intValue()%5==0){
			int questId = ((Integer)paramValue[0]).intValue();
			ASMQuest quest = ASMQuestUtil.getQuest(questId);
			if(quest==null)
				return false;
			return player.asmVm.hasFinished(quest, new DateTime());
		}
		return false;
	}
	
	/** 玩家touchNpc */
	public boolean g_openQuestGruide(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int npcId = ((Integer)paramValue[0]).intValue();
		int[] arr = getVarStore(String.valueOf(triggerId));
		if(arr!=null && arr[0]==npcId)
			return true;
		return false;
	}
	
	/** 玩家学习了某个技能 */
	public boolean g_studySkill(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int skillId = ((Integer)paramValue[0]).intValue();
		Skill skill = player.skills.getSkillByGroupId(skillId);
		if(skill!=null)
			return true;
		return false;
	}
	
	/** 玩家学习了某个技能 */
	public boolean g_isGuideRole(Integer diff, Integer guidId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int type = ((Integer)paramValue[0]).intValue();
		
		if(type == 0 && player.name.substring(0, 2).equals("游客")){
			return true;
		}else{
			return false;
			
		}
	}
}
