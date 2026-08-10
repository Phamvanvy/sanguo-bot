package peony.vm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import peony.game.Creature;
import peony.game.Gain;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.GameQuest;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.PropertyPool;
import peony.game.PropertyService;
import peony.game.QuestReward;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapUtil;
import peony.game.association.AssociationException;
import peony.game.association.AssociationMember;
import peony.game.association.AssociationService;
import peony.game.buff.BuffUtil;
import peony.game.chat.ChatService;
import peony.game.nation.Nation;
import peony.game.quest.CycleRewardEntry;
import peony.net.Packet;
import peony.script.Script;
import peony.service.ServiceEvent;
import peony.service.account.Account;
import peony.service.tong.Tong;
import peony.service.tong.TongService;
import peony.util.ProvinceUtil;
import ch.javasoft.util.intcoll.IntHashMap;
import ch.javasoft.util.intcoll.IntMap.IntEntry;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.quest.QuestTarget;

/**
 * 实现所有函数库，保存用户的任务信息，没个Player对应存在一个vm
 * 
 * @author Jeffrey
 * 
 */
public class ASMGameVM implements GameVM, Cloneable {

	private static final Logger log = Logger.getLogger(ASMGameVM.class);

	public Player player;
	protected Random rnd = new Random();

	// 将要加入的任务Id
	protected Set<Integer> pending = new TreeSet<Integer>();
	// 正在移除的任务Id
	protected Set<Integer> removing = new TreeSet<Integer>();
	// 已经完成的任务Id
	protected IntHashMap<Finish> finished = new IntHashMap<Finish>();
	// protected Set<Integer> finished = new TreeSet<Integer>();
	// 满足前置条件的任务Id
	protected Set<Integer> preConditionOk = new TreeSet<Integer>();
	// 满足后置条件的任务Id
	protected Set<Integer> finishConditionOk = new TreeSet<Integer>();

	protected Set<Integer> failed = new TreeSet<Integer>();

	// 正在执行的任务
	protected Map<Integer, ASMQuest> quests = new LinkedHashMap<Integer, ASMQuest>();
	// 每个任务的变量池
	protected IntHashMap<VarStore> stores = new IntHashMap<VarStore>();

	protected int currentQuestId; // 当前正在执行的任务

	protected int lastStageQuestId; // 上一个场景任务的Id

	protected List<ChangedVar> changed = new ArrayList<ChangedVar>();

	protected Set<Integer> finishTemps = new TreeSet<Integer>();
	
	protected CycleQuestInfo cycleQuestInfo = new CycleQuestInfo();
	
	protected static CycleRewardEntry CYCLE_REWARD_ENTRY = new CycleRewardEntry();
	
	protected Calendar cachedCal = Calendar.getInstance();
	protected Calendar nowCal = Calendar.getInstance();

	public ASMGameVM(Player player) {
		this.player = player;
	}

	
	
	@Override
	public ASMGameVM clone() {
		ASMGameVM ret = new ASMGameVM(player);
		ret.rnd = rnd;
		ret.pending.addAll(pending);
		ret.removing.addAll(removing);
		ret.finished.putAll(finished);
		ret.preConditionOk.addAll(preConditionOk);
		ret.finishConditionOk.addAll(finishConditionOk);
		ret.quests = new LinkedHashMap<Integer, ASMQuest>(quests);
		ret.stores = new IntHashMap<VarStore>(stores.size());
		Iterator<IntEntry<VarStore>> ite = stores.intEntrySet().iterator();
		while (ite.hasNext()) {
			IntEntry<VarStore> entry = ite.next();
			ret.stores.put(entry.getIntKey(), entry.getValue().clone());
		}
		return ret;
	}

	public void removeFinished(int id) {
		finished.remove(id);
	}

	
	/**
	 * 获取玩家已经完成的任务的总数，不包括每日任务和跑环任务
	 * @param player
	 * @return
	 */
	public int getFinishedQuest(Player p){
		if(p!=null){
			int finish=finished.size();
			for(Integer f:finished.keySet()){
				ASMQuest quest=ASMQuestUtil.getQuest(f);
				Finish fini = finished.get(f);
				//处理2454号任务不存在的bug
				if(fini.questId == 2454){
					continue;
				}
				if(fini!=null){
					if(quest!=null && quest.getGameQuest()!=null){
						if(quest.getGameQuest()!= null && quest.getGameQuest().getCycleInfo()!=null){
							finish--;
						}
						if(quest.getGameQuest()!= null && quest.getGameQuest().getRepeatType()==3){
							finish--;
						}
					}
				}
		}
		return finish;
		}
		return 0;
	}
	
	public long getFinishTime(int questId){
		Finish finishQuest = finished.get(questId);
		if(finishQuest!=null){
			long time = finishQuest.finishTime;
			return time;
		}
		return 0;
	}

	public static ASMGameVM fromDBBytes(Player player, byte[] bytes) {
		ASMGameVM ret = new ASMGameVM(player);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		try {
			byte version = dis.readByte(); // version
			if (version == 1) {
				fromDBBytes1(dis, ret);
			} else if (version == 2) {
				fromDBBytes2(dis, ret);
			} else if (version == 3) {
				fromDBBytes3(dis, ret);
			} else if (version == 4) {
				fromDBBytes4(dis, ret);
			} else if (version == 5){
				fromDBBytes5(dis, ret);
			} else if (version == 6){
				fromDBBytes6(dis, ret);
			}
			
		} catch (IOException e) {
			log.error(e, e);
		}
		return ret;
	}

	protected static void fromDBBytes1(DataInputStream dis, ASMGameVM vm)
			throws IOException {
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.pending.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.removing.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			Finish f = new Finish(dis.readInt(), System.currentTimeMillis());
			vm.finished.put(f.questId, f);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			vm.addScript(id, false);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			GameQuest quest = ASMQuestUtil.getGameQuest(id);
			if (quest != null) {
				Script script = quest.getScript();
				int[] vs = new int[script.getFields().length];
				int len = dis.readInt();
				for (int j = 0; j < len; j++) {
					String fieldName = dis.readUTF();
					int index = script.getFieldIndex(fieldName);
					if (index != -1) {
						vs[script.getFieldIndex(fieldName)] = dis.readInt();
					} else {
						// 变量已被删除
						dis.readInt();
					}
				}
				VarStore store = vm.stores.get(id);
				if (store != null)
					store.values = vs;
				else {
					store = new VarStore(id, vs);
					vm.stores.put(store.questId, store);
				}
			} else {
				// 任务已被删除
				int len = dis.readInt();
				for (int j = 0; j < len; j++) {
					dis.readUTF();
					dis.readInt();
				}
			}
		}
	}

	protected static void fromDBBytes2(DataInputStream dis, ASMGameVM vm)
			throws IOException {
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.pending.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.removing.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			Finish f = new Finish(dis.readInt(), System.currentTimeMillis());
			vm.finished.put(f.questId, f);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			vm.addScript(id, false);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.failed.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			GameQuest quest = ASMQuestUtil.getGameQuest(id);
			if (quest != null) {
				Script script = quest.getScript();
				int[] vs = new int[script.getFields().length];
				int len = dis.readInt();
				for (int j = 0; j < len; j++) {
					String fieldName = dis.readUTF();
					int index = script.getFieldIndex(fieldName);
					if (index != -1) {
						vs[script.getFieldIndex(fieldName)] = dis.readInt();
					} else {
						// 变量已被删除
						dis.readInt();
					}
				}
				VarStore store = vm.stores.get(id);
				if (store != null)
					store.values = vs;
				else {
					store = new VarStore(id, vs);
					vm.stores.put(store.questId, store);
				}
			} else {
				// 任务已被删除
				int len = dis.readInt();
				for (int j = 0; j < len; j++) {
					dis.readUTF();
					dis.readInt();
				}
			}
		}

	}

	protected static void fromDBBytes3(DataInputStream dis, ASMGameVM vm)
			throws IOException {
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.pending.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.removing.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			Finish f = new Finish(dis.readInt(), dis.readLong());
			vm.finished.put(f.questId, f);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			vm.addScript(id, false);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.failed.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			GameQuest quest = ASMQuestUtil.getGameQuest(id);
			if (quest != null) {
				Script script = quest.getScript();
				int[] vs = new int[script.getFields().length];
				int len = dis.readInt();
				for (int j = 0; j < len; j++) {
					String fieldName = dis.readUTF();
					int index = script.getFieldIndex(fieldName);
					if (index != -1) {
						vs[script.getFieldIndex(fieldName)] = dis.readInt();
					} else {
						// 变量已被删除
						dis.readInt();
					}
				}
				VarStore store = vm.stores.get(id);
				if (store != null)
					store.values = vs;
				else {
					store = new VarStore(id, vs);
					vm.stores.put(store.questId, store);
				}
			} else {
				// 任务已被删除
				int len = dis.readInt();
				for (int j = 0; j < len; j++) {
					dis.readUTF();
					dis.readInt();
				}
			}
		}

	}
	
	
	protected static void fromDBBytes4(DataInputStream dis, ASMGameVM vm)
			throws IOException {
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.pending.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.removing.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			Finish f = new Finish(dis.readInt(), dis.readLong());
			vm.finished.put(f.questId, f);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			vm.addScript(id, false);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.failed.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			GameQuest quest = ASMQuestUtil.getGameQuest(id);
			if (quest != null) {
				Script script = quest.getScript();
				int[] vs = new int[script.getFields().length];
				int len = dis.readInt();
				for (int j = 0; j < len; j++) {
					String fieldName = dis.readUTF();
					int index = script.getFieldIndex(fieldName);
					if (index != -1) {
						vs[script.getFieldIndex(fieldName)] = dis.readInt();
					} else {
						// 变量已被删除
						dis.readInt();
					}
				}
				VarStore store = vm.stores.get(id);
				if (store != null)
					store.values = vs;
				else {
					store = new VarStore(id, vs);
					vm.stores.put(store.questId, store);
				}
			} else {
				// 任务已被删除
				int len = dis.readInt();
				for (int j = 0; j < len; j++) {
					dis.readUTF();
					dis.readInt();
				}
			}
		}
		vm.cycleQuestInfo.day = dis.readInt();
		int cycleInfo = dis.read();
		if(vm.cycleQuestInfo.day == Time.day){
			for(int i=0;i<cycleInfo;i++){
				vm.cycleQuestInfo.finished.add(dis.readInt());
			}
		}
	}

	protected static void fromDBBytes5(DataInputStream dis, ASMGameVM vm)
			throws IOException {
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.pending.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.removing.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			Finish f = new Finish(dis.readInt(), dis.readLong());
			vm.finished.put(f.questId, f);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			vm.addScript(id, false);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.failed.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			GameQuest quest = ASMQuestUtil.getGameQuest(id);
			if (quest != null) {
				Script script = quest.getScript();
				int[] vs = new int[script.getFields().length];
				int len = dis.readInt();
				for (int j = 0; j < len; j++) {
					String fieldName = dis.readUTF();
					int index = script.getFieldIndex(fieldName);
					if (index != -1) {
						vs[script.getFieldIndex(fieldName)] = dis.readInt();
					} else {
						// 变量已被删除
						dis.readInt();
					}
				}
				VarStore store = vm.stores.get(id);
				if (store != null)
					store.values = vs;
				else {
					store = new VarStore(id, vs);
					vm.stores.put(store.questId, store);
				}
			} else {
				// 任务已被删除
				int len = dis.readInt();
				for (int j = 0; j < len; j++) {
					dis.readUTF();
					dis.readInt();
				}
			}
		}
		vm.cycleQuestInfo.day = dis.readInt();
		vm.cycleQuestInfo.currentCycle = dis.readInt();
		vm.cycleQuestInfo.currentIndex = dis.readInt();
		vm.cycleQuestInfo.state = dis.readInt();
		int cycleInfo = dis.read();
		if (vm.cycleQuestInfo.day == Time.day) {
			for (int i = 0; i < cycleInfo; i++) {
				vm.cycleQuestInfo.finished.add(dis.readInt());
			}
		}
	}
	
	protected static void fromDBBytes6(DataInputStream dis, ASMGameVM vm)
	throws IOException {
		int size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.pending.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.removing.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			Finish f = new Finish(dis.readInt(), dis.readLong());
			vm.finished.put(f.questId, f);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			vm.addScript(id, false);
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			vm.failed.add(dis.readInt());
		}
		size = dis.readInt();
		for (int i = 0; i < size; i++) {
			int id = dis.readInt();
			GameQuest quest = ASMQuestUtil.getGameQuest(id);
			if (quest != null) {
				Script script = quest.getScript();
				int[] vs = new int[script.getFields().length];
				int len = dis.readInt();
				for (int j = 0; j < len; j++) {
					String fieldName = dis.readUTF();
					int index = script.getFieldIndex(fieldName);
					if (index != -1) {
						vs[script.getFieldIndex(fieldName)] = dis.readInt();
					} else {
						// 变量已被删除
						dis.readInt();
					}
				}
				VarStore store = vm.stores.get(id);
				if (store != null)
					store.values = vs;
				else {
					store = new VarStore(id, vs);
					vm.stores.put(store.questId, store);
				}
			} else {
				// 任务已被删除
				int len = dis.readInt();
				for (int j = 0; j < len; j++) {
					dis.readUTF();
					dis.readInt();
				}
			}
		}
		vm.cycleQuestInfo.day = dis.readInt();
		vm.cycleQuestInfo.finishTime = dis.readLong();
		vm.cycleQuestInfo.currentCycle = dis.readInt();
		vm.cycleQuestInfo.currentIndex = dis.readInt();
		vm.cycleQuestInfo.state = dis.readInt();
		int cycleInfo = dis.read();
		if (vm.cycleQuestInfo.day == Time.day) {
			for (int i = 0; i < cycleInfo; i++) {
				vm.cycleQuestInfo.finished.add(dis.readInt());
			}
		}
		//曾经出现过一个Bug，如果当前用户接的任务的数量超过20个，并且超出的任务里面有跑环任务，
		//那么在从数据库载入的时候会将多接的任务丢弃，这样跑环的数据就不准确了，需要清理
		int cycleCount = 0;
		for(int questId : vm.quests.keySet()){
			ASMQuest quest = vm.quests.get(questId);
			if(quest.getGameQuest().getCycleInfo()!=null){
				cycleCount++;
			}
		}
		if(cycleCount==0)
			vm.cycleQuestInfo.clearCurrentState();
	}

	public void clear() {
		lastStageQuestId = -1;
		preConditionOk.clear();
		finishConditionOk.clear();
	}

	public byte[] toDBBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(6);// version
			dos.writeInt(pending.size());
			if (pending.size() > 0) {
				for (int id : pending)
					dos.writeInt(id);
			}
			dos.writeInt(removing.size());
			if (removing.size() > 0) {
				for (int id : removing)
					dos.writeInt(id);
			}
			dos.writeInt(finished.size());
			if (finished.size() > 0) {
				for (Finish f : finished.values()) {
					dos.writeInt(f.questId);
					dos.writeLong(f.finishTime);
				}
			}
			dos.writeInt(quests.size());
			for (int id : quests.keySet()) { // 当前所有的questId
				dos.writeInt(id);
			}
			dos.writeInt(failed.size());
			for (int id : failed) { // 当前所有的questId
				dos.writeInt(id);
			}
			dos.writeInt(stores.size());
			Iterator<IntEntry<VarStore>> ite = stores.intEntrySet().iterator();
			while (ite.hasNext()) {
				IntEntry<VarStore> entry = ite.next();
				int id = entry.getIntKey();
				int[] vs = entry.getValue().values;
				GameQuest quest = ASMQuestUtil.getGameQuest(id);
				dos.writeInt(id);
				dos.writeInt(vs.length);
				for (int i = 0; i < vs.length; i++) {
					String fieldName = quest.getScript().getField(i).getName();
					dos.writeUTF(fieldName);
					dos.writeInt(vs[i]);
				}
			}
			dos.writeInt(cycleQuestInfo.day);
			dos.writeLong(cycleQuestInfo.finishTime);
			dos.writeInt(cycleQuestInfo.currentCycle);
			dos.writeInt(cycleQuestInfo.currentIndex);
			dos.writeInt(cycleQuestInfo.state);
			dos.write(cycleQuestInfo.finished.size());
			for(int cycle:cycleQuestInfo.finished){
				dos.writeInt(cycle);
			}
		} catch (IOException e) {
			log.error(e, e);
		}
		return baos.toByteArray();
	}

	public boolean isFail(int questId) {
		return failed.contains(questId);
	}

	public void update(int diff) {
		cycleQuestInfo.cycle();
		if (pending.size() > 0) {
			for (int scriptId : pending) {
				addScript(scriptId, true);
				
				if (quests.containsKey(scriptId)) {
					// 记录日志
					LogUtil.logGetQuest(player, scriptId, true);
				}
			}
			pending.clear();
		}
		for (ASMQuest quest : quests.values()) {
			currentQuestId = quest.getId();
			if (!failed.contains(currentQuestId)) {
				quest.execute(this);
				if (!removing.contains(currentQuestId)) {
					boolean cycleCheck = true;
					if(quest.getGameQuest().getCycleInfo() != null){
						GameQuest.CycleInfo ci = quest.getGameQuest().getCycleInfo();
						if(ci.cycle != cycleQuestInfo.currentCycle || ci.index != cycleQuestInfo.currentIndex || cycleQuestInfo.state != 1)
							cycleCheck = false;
					}
					if (cycleCheck && quest.finishCondition(this) == 1) {
						if (!finishConditionOk.contains(currentQuestId))
							addToPostConditionOk(quest.getGameQuest());
					} else {
						if (finishConditionOk.contains(currentQuestId))
							removeFromPostConditionOk(quest.getGameQuest());
					}
				}
			}
		}
		if (player.map != null
				&& player.systemState == Player.SYSTEMSTATE_READY) { // 执行场景任务
			ASMQuest quest = ASMQuestUtil.getAreaQuest(player.map.getStageId());
			if (quest != null) {
				if (stores.get(quest.getId()) == null) {
					stores.put(quest.getId(), new VarStore(quest.getId(), quest
							.getGameQuest().getScript().getFields().length));
				}
				if (lastStageQuestId != quest.getId()) {
					lastStageQuestId = quest.getId();
					Packet pt = new Packet(OpCode.AREAQUEST_INFO_SERVER);
					pt.putInt(quest.getId());
					pt.putString(quest.getGameQuest().getName());
					pt.put(quest.getGameQuest().getClientETF());
					pt.putInts(getQuestStore(quest.getId()));
					player.send(pt);
				}
				currentQuestId = quest.getId();
				quest.execute(this);
			} else {
				lastStageQuestId = -1;
			}

		}
		if (removing.size() > 0) { // 必须放在循环之后执行，不然会照成E_TaskFinish的Trigger无法执行到
			for (int id : removing) {
				quests.remove(id);
				finishConditionOk.remove(id);
			}
			removing.clear();
		}
		syncVariables();
	}

	/**
	 * 0 成功 1 没有此任务
	 * 
	 * @param questId
	 * @return
	 */
	public int abandonQuest(int questId) {
		if (quests.containsKey(questId)) {
			preConditionOk.remove(questId);
			finishConditionOk.remove(questId);
			failed.remove(questId);
			quests.remove(questId);
			stores.remove(questId);
			GameQuest quest = ASMQuestUtil.getGameQuest(questId);
			if(quest != null&&quest.getCycleInfo() != null){
				cycleQuestInfo.abandon(player,quest);
			}
			
			// 记录日志
			LogUtil.logAbandonQuest(player, questId);
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * 0 成功 1 包格不够 2 没有指定分支 3 没有指定任务 4 不能完成任务
	 * 
	 * @param questId
	 * @return
	 */
	public int finishQuest(int questId, int branchId) {
		if (quests.containsKey(questId)) {
			GameQuest quest = quests.get(questId).getGameQuest();
			if (finishConditionOk.contains(questId)) {
				int ret = questFinish(quest, branchId);
				if (ret == 0)
					removeFromPostConditionOk(quest);
				return ret;
			} else {
				return 4;
			}

		} else {
			return 3;
		}
	}

	public void pending(int scriptId) {
		if(quests.size()>=20){
			acceptFail(player, scriptId, "不能再接更多的任务");
			return;
		}
		//如果是军团任务，特殊处理军团任务
		if(TongService.isTongQuest(scriptId)){
			Tong tong = Server.server.getServiceRegistry().getTongService().getPlayerTong(player.id);
			if(tong==null || tong.pool.getInt(Tong.PROPERTY_TONG_QUEST+scriptId,0)!=Time.day){
				acceptFail(player, scriptId, "您没有加入军团,或者所在军团没有发布此任务");
				return;
			}
		}
		if (preConditionOk.contains(scriptId)) {
			// ASMQuest quest = ASMQuestUtil.getQuest(scriptId);
			// removeFromPreConditionOk(quest.getGameQuest());
			pending.add(scriptId);
		} else {
			ASMQuest quest = ASMQuestUtil.getQuest(scriptId);
			try {
				if(quest.getGameQuest().questInfo.owner.type==1){
					acceptFail(player, scriptId, "不能接场景任务");
					return;
				}
			} catch (Exception e) {
			}
			if (quest.getGameQuest().getStartNpc() == -1) { // 如果其实NPC是-1
				int oldCurrentQuestId = currentQuestId;
				currentQuestId = quest.getId();
				if (quest.preCondition(this) == 1) {
					pending.add(scriptId);
				} else {
					acceptFail(player, scriptId, "不能接受此任务");
					// ErrorHandler.sendErrorMessage(player.session, -1,
					// OpCode.QUEST_ACCEPT_CLIENT, "不能接受此任务");
				}
				currentQuestId = oldCurrentQuestId;
			} else {
				acceptFail(player, scriptId, "不能接受此任务");
				// ErrorHandler.sendErrorMessage(player.session, -1,
				// OpCode.QUEST_ACCEPT_CLIENT, "不能接受此任务");
			}
		}
	}

	protected void addToPreConditionOk(GameQuest quest) {
		preConditionOk.add(quest.getId());
		Packet pt = new Packet(OpCode.QUEST_START_ADDED_SERVER);
		pt.putInt(quest.getStartNpc());
		pt.putInt(quest.getFinishNpc());
		pt.putInt(quest.getId());
		pt.put(quest.getLevel());
		pt.putString(quest.getName());
		player.send(pt);
	}

	protected void removeFromPreConditionOk(GameQuest quest) {
		preConditionOk.remove(quest.getId());
		Packet pt = new Packet(OpCode.QUEST_START_REMOVED_SERVER);
		pt.putInt(quest.getStartNpc());
		pt.putInt(quest.getId());
		player.send(pt);
	}

	protected void addToPostConditionOk(GameQuest quest) {
		finishConditionOk.add(quest.getId());
		Packet pt = new Packet(OpCode.QUEST_FINISH_ADDED_SERVER);
		pt.putInt(quest.getFinishNpc());
		pt.putInt(quest.getId());
		pt.put(quest.isNotifyFinish() ? 1 : 0);
		player.schedule(pt);
		//player.send(pt);
		// log.debug(String.format("QuestID[%d]AddFinish", quest.getId()));
	}

	protected void removeFromPostConditionOk(GameQuest quest) {
		if (finishConditionOk.remove(quest.getId())) {
			Packet pt = new Packet(OpCode.QUEST_FINISH_REMOVED_SERVER);
			pt.putInt(quest.getFinishNpc());
			pt.putInt(quest.getId());
			player.send(pt);
		}
	}

	public void addScript(int questId, boolean check) {
		addScript(questId, null, check, false);
	}

	protected void acceptFail(Player player, int questId, String msg) {
		Packet pt = new Packet(OpCode.QUEST_ACCEPT_FAIL_SERVER);
		pt.putInt(questId);
		pt.putString(msg);
		player.send(pt);
		
		// 记录日志
		LogUtil.logGetQuest(player, questId, false);
	}

	public void addScript(int questId, int[] values, boolean check,boolean senddesc) {
		ASMQuest quest = ASMQuestUtil.getQuest(questId);
		if (quest != null) {
			// preConditionOk.remove(quest.getId());
			if (check) {
				if (quest.getGameQuest().getCycleInfo() != null) {
					if (check) {
						GameQuest.CycleInfo info = quest.getGameQuest()
								.getCycleInfo();
						if (info.level > player.level) {
							acceptFail(player, quest.getId(), 
									MessageFormat.format("{0}级才能接此任务", info.level));
							return;
						}
					}
				} else {
					if (player.level < (quest.getGameQuest().getLevel() - 6)) {
						acceptFail(player, quest.getId(), 
								MessageFormat.format("{0}级才能接此任务", (quest.getGameQuest().getLevel() - 6)));
						// ErrorHandler.sendErrorMessage(player.session, -1,
						// OpCode.QUEST_ACCEPT_CLIENT, "等级不够接任务");
						return;
					}
				}
				if (player.bag.getFreeBagCount() < quest.getGameQuest()
						.getRequireFreeBag()) {
					acceptFail(player, quest.getId(), "没有足够包格");
					// ErrorHandler.sendErrorMessage(player.session, -1,
					// OpCode.QUEST_ACCEPT_CLIENT, "没有足够包格");
					return;
				}
			}
			
			quests.put(quest.getGameQuest().getId(), quest);
			removeFromPreConditionOk(quest.getGameQuest());
			if(quest.getGameQuest().getCycleInfo() != null){
				cycleQuestInfo.accept(player, quest.getGameQuest(), check);
			}
			VarStore store = null;
			int len = quest.getGameQuest().getScript().getFields().length;
			if (values == null) {
				store = new VarStore(quest.getGameQuest().getId(), len);
			} else {
				if (len < values.length)
					throw new IllegalArgumentException();
				int[] v = new int[len];
				System.arraycopy(values, 0, v, 0, values.length);
				store = new VarStore(quest.getGameQuest().getId(), v);
			}
			stores.put(store.questId, store);
			if(senddesc){
				Packet pt = new Packet(OpCode.QUEST_START_ADDED_SERVER);
				pt.putInt(quest.getGameQuest().getStartNpc());
				pt.putInt(quest.getGameQuest().getFinishNpc());
				pt.putInt(quest.getGameQuest().getId());
				pt.put(quest.getGameQuest().getLevel());
				pt.putString(quest.getGameQuest().getName());
				player.send(pt);
			}
			Packet pt = new Packet(OpCode.QUEST_ACCEPTED_SERVER);
			pt.putInt(questId);
			pt.put(quest.getGameQuest().getClientETF());
			List<QuestTarget> l = quest.getGameQuest().getTargets();
			pt.put((byte) l.size());
			for (QuestTarget target : l) {
				// pt.putString(target.condition);
				pt.putString(target.description);
			}
			player.send(pt);
		}
	}

	protected ASMQuest getCurrentQuest() {
		return ASMQuestUtil.getQuest(currentQuestId);
	}

	public GameQuest getGameQuest(int id) {
		ASMQuest quest = quests.get(id);
		if (quest != null)
			return quest.getGameQuest();
		return null;
	}

	// public boolean needRunPreCondition(int questId) {
	// if (finished.containsKey(questId) || removing.contains(questId)
	// || quests.containsKey(questId) || pending.contains(questId))
	// return false;
	// return true;
	// }

	protected boolean hasFinished(ASMQuest quest, Calendar now) {
		Finish f = finished.get(quest.getId());
		if (f != null) {
			int repeatType = quest.getGameQuest().getRepeatType();
			/**
			 * 重复类型：0 - 不可重复、1 - 每月可完成1次、2 - 每周可完成1次、3 - 每天可完成1次、4 - 无限重复
			 */
			if (repeatType == 0)
				return true;
			else if (repeatType == 1) {
				cachedCal.setTimeInMillis(f.finishTime);
				if (cachedCal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
						&& cachedCal.get(Calendar.MONTH) == now.get(Calendar.MONTH))
					return true;
			} else if (repeatType == 2) {
				cachedCal.setTimeInMillis(f.finishTime);
				if (cachedCal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
						&& cachedCal.get(Calendar.WEEK_OF_YEAR) == now
								.get(Calendar.WEEK_OF_YEAR))
					return true;
			} else if (repeatType == 3) {
				cachedCal.setTimeInMillis(f.finishTime);
				if (cachedCal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
						&& cachedCal.get(Calendar.DAY_OF_YEAR) == now
								.get(Calendar.DAY_OF_YEAR))
					return true;
			} else if (repeatType == 4) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	public void runPreCondition(ASMQuest quest, Calendar now) {
		if (removing.contains(quest.getId())
				|| quests.containsKey(quest.getId())
				|| pending.contains(quest.getId()))
			return;
		if(!quest.getGameQuest().getIsActive()){
			if (preConditionOk.contains(quest.getId())) { // 如果存在则删除，然后发信息给客户端
				removeFromPreConditionOk(quest.getGameQuest());
			}
			return;
		}
		if(quest.getGameQuest().getCycleInfo() != null){
			if(cycleQuestInfo.preConditionOk(player, quest.getGameQuest())&&checkFaction(player.faction,quest.getGameQuest().getFaction())){
				if (!preConditionOk.contains(quest.getId())) { // 如果不存在则添加，然后发信息给客户端
					addToPreConditionOk(quest.getGameQuest());
				}
			}else{
				if (preConditionOk.contains(quest.getId())) { // 如果存在则删除，然后发信息给客户端
					removeFromPreConditionOk(quest.getGameQuest());
				}
			}
			return;
		}
		
		if (hasFinished(quest, now)) {
			return;
		}
		currentQuestId = quest.getId();
		nowCal = now;
		if ((player.level >= quest.getGameQuest().getLevel() - 10)
				&& quest.preCondition(this) == 1 && checkFaction(player.faction,quest.getGameQuest().getFaction())) {
			if (!preConditionOk.contains(quest.getId())) { // 如果不存在则添加，然后发信息给客户端
				addToPreConditionOk(quest.getGameQuest());
			}
		} else {
			if (preConditionOk.contains(quest.getId())) { // 如果存在则删除，然后发信息给客户端
				removeFromPreConditionOk(quest.getGameQuest());
			}
		}
	}
	
	public boolean checkFaction(int playerFaction,int questFaction){
		if(questFaction==-1||questFaction==GameObject.FACTION_NEUTRAL)
			return true;
		else
			return playerFaction==questFaction;
	}

	public int random() {
		return rnd.nextInt(10000);
	}

	public int assignTask(int taskId) {
		if (quests.containsKey(taskId))
			return 0;
		if (finished.containsKey(taskId))
			return 0;
		pending.add(taskId);
		return 1;
	}

	public int endTask(int groupId) {
		// Drop drop = Drops.getDrop(groupId);
		// Fall fall = drop.getFallItems(rnd);
		// if (player.bag.addFall(fall,true)) {
		// questFinish(currentQuestId);
		// return 1;
		// } else {
		// return 0;
		// }
		return 1;
	}
	
	public void forceAddFinished(int questId) {
		Finish f = new Finish(questId, System.currentTimeMillis());
		finished.put(f.questId, f);
	}

	/**
	 * 0 成功 1 包格不够 2 没有指定分支
	 * 
	 * @param questId
	 * @return
	 */
	protected int questFinish(GameQuest quest, int branchId) {
		QuestReward reward = quest.getReward();
		GameQuest.CycleInfo ci = quest.getCycleInfo();
		if(ci != null && ci.type==GameQuest.CycleInfo.TYPE_TAIL){ //因为跑环最后一个任务的奖励是程序添加的，所以会多出一个branchId
			branchId --;
			if(ci.cycle==28 || ci.cycle==29 || ci.cycle==30)
				branchId = 1;
		}
		//特殊处理活动任务的奖励
		Server.server.getServiceRegistry().getGameCityService().gameCityReward(quest, player);
		if (reward.containsBranch(branchId)) {
			Gain gain = new Gain(player);
			reward.gain(branchId, gain);
			if (branchId != 1 && reward.containsBranch(1)) {// 分支1是金钱和经验奖励，所有分支都要加
				reward.gain(1, gain);
			}
			if(ci != null&&ci.type==GameQuest.CycleInfo.TYPE_TAIL){
				CYCLE_REWARD_ENTRY.gain(gain);
			}
			PlayerTransaction tx = player.newTransaction("QST");
			try {
				player.addGainComplete(gain, tx, true);
				tx.commit();
				Finish f = new Finish(quest.getId(), System.currentTimeMillis());
				finished.put(f.questId, f);
				removing.add(quest.getId());
				finishTemps.add(quest.getId());
				if(quest.getCycleInfo() != null){ //如果是环任务
					cycleQuestInfo.finished(player,quest);
				}
				
				// 生成事件通知
				ServiceEvent evt = new ServiceEvent(ServiceEvent.EVENT_FINISH_QUEST, player, quest.getId(), branchId);
				Server.server.getEventManager().addEvent(evt);
				Packet pt = new Packet(OpCode.QUEST_FINISHED_SERVER);
				pt.putInt(quest.getId());
				player.send(pt);
//				if(ci != null && ci.type!=GameQuest.CycleInfo.TYPE_TAIL){
//					addScript(ci.nextQuestId,null, true, true);
//				}
				return 0;
			} catch (NoEnoughSpaceException e) {
				tx.rollback();
			}
			return 1;
		} else {
			return 2;
		}

	}

	public int hasTask(int id) {
		return quests.containsKey(id) ? 1 : 0;
	}

	public int taskFinished(int id) {
		ASMQuest quest = ASMQuestUtil.getQuest(id);
		if (quest == null) {
			return 0;
		}
		return this.hasFinished(quest, nowCal) ? 1 : 0;
	}

	public int canFinish() {
		return finishConditionOk.contains(currentQuestId) ? 1 : 0;
	}

	public int getReward(int branchId) {
		QuestReward reward = getCurrentQuest().getGameQuest().getReward();
		if (reward.containsBranch(branchId)) {
			Gain gain = new Gain(player);
			reward.gain(branchId, gain);
			PlayerTransaction tx = player.newTransaction("QST");
			try {
				player.addGainComplete(gain, tx, true);
				tx.commit();
				return 1;
			} catch (NoEnoughSpaceException e) {
				tx.rollback();
			}
		}
		return 1;
	}
	
	/**
	 * chat message question方法中的message变量可能是个包含系统变量的表达式，需要先处理
	 * @param message
	 * @return
	 */
	protected String getExpressionStringValue(String message){
		GameQuest.Node[] nodes = GameQuest.parserStringExpression(message);
		StringBuilder sb = new StringBuilder(200);
		for(int i=0;i<nodes.length;i++){
			GameQuest.Node node = nodes[i];
			if(node.type==GameQuest.Node.TYPE_STRING){
				sb.append(node.value);
			}else{
				sb.append(getVariableValue(node.value));
			}
		}
		return sb.toString();
	}
	

	public int chat(int npcId, String message, int notifyId) {
		message = getExpressionStringValue(message);
		player.chat(currentQuestId, npcId, message, notifyId);
		return 1;
	}
	
	public int message(String message, int timeout, int notifyId) {
		message = getExpressionStringValue(message);
		player.message(currentQuestId, message, timeout, notifyId);
		return 1;
	}

	public int question(String message, String options, int notifyId) {
		message = getExpressionStringValue(message);
		player.question(currentQuestId, message, options, notifyId);
		return 1;
	}

	public int setFail() {
		ASMQuest quest = getCurrentQuest();
		if (quest != null) {
			failed.add(quest.getId());
			Packet pt = new Packet(OpCode.QUEST_FAIL_SERVER);
			pt.putInt(quest.getId());
			player.send(pt);
			return 1;
		}
		return 0;
	}

	public int refreshNPC(int npcId, int flag, int index) {
		if (flag == 0) { // 查找NPC
			VMap map = player.getVMap();
			Creature c = map.getCreatureById(npcId);
			if (c != null) {
				setValue(currentQuestId, index, c.id);
				return 1;
			} else {
				return 0;
			}
		} else if (flag == 1) { // 复制NPC
		    ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		    GameMapObject gmo = GameMapObject.findByID(proj, npcId);
		    if (gmo == null || !(gmo instanceof GameMapNPC)) {
		        return 0;
		    }
			Creature npc = (Creature) VMapUtil.addCreature(player.getVMap(),
					(GameMapNPC)gmo, true, 0, Server.server.revision);
			npc.ownerRef = player.ref();
			if (npc != null) {
				setValue(currentQuestId, index, npc.instanceId);
				return 1;
			} else {
				return 0;
			}
		} else
			throw new IllegalArgumentException();
	}

	/**
	 * 在指定位置刷新NPC
	 * 
	 * @param npcId
	 *            NPC ID
	 * @param targetNpc
	 *            用于指定位置的NPC的实例ID
	 * @param index
	 *            刷新出的NPC保存的变量索引
	 * @return 无意义
	 */
	public int refreshNPCAt(int npcId, int targetNpc, int index) {
	    ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
        GameMapObject gmo = GameMapObject.findByID(proj, npcId);
        if (gmo == null || !(gmo instanceof GameMapNPC)) {
            return 0;
        }
		GameObject obj = ObjectAccessor.getGameObject(targetNpc);
		if (obj == null || !(obj instanceof Unit)) {
			return 0;
		}
		Unit unit = (Unit) obj;
		Creature npc = (Creature) VMapUtil.addCreature(unit.getVMap(), unit.x,
				unit.y, (GameMapNPC)gmo, true, 0, null);
		npc.ownerRef = player.ref();
		if (npc != null) {
			setValue(currentQuestId, index, npc.instanceId);
			return 1;
		} else {
			return 0;
		}
	}

	public int removeNPC(int instanceId) {
		Creature c = (Creature) ObjectAccessor.getGameObject(instanceId);
		if (c == null)
			return 0;
		// if (c.ownerRef==null)
		// return 0;
		// if (c.ownerRef.id != player.id)
		// return 0;
//		c.removeFromMap();
//		ObjectAccessor.removeGameObject(c);
//		return 1;
		c.setInvisible();
        return 1;
	}

	public int removeNPCById(int npcId) {
		Creature c = player.getVMap().getCreatureById(npcId);
		if (c == null)
			return 0;
		c.setInvisible();
		return 1;
	}

	public int gotoMap(int mapId, int x, int y) {
		try {
		    player.goMap(mapId, x, y);
//			Server.server.getWorld().addPlayerToMap(player, mapId, x, y, false);
			return 1;
		} catch (VMapException e) {
			player.message(-1, e.getMessage(), -1, -1);
			return 0;
		}
	}

	public int logout() {
		player.logout();
		return 1;
	}
	
	public int injoinAssociation(){
		player.injoinAssociation();
		return 1;
	}

	/**
	 * 在玩家周围指定范围内查找某类型的NPC。
	 * 
	 * @param templateID
	 *            NPC模板ID
	 * @param range
	 *            半径（像素）
	 * @param index
	 *            保存查找结果的变量索引
	 * @return 如果找到，返回1，否则返回0.
	 */
	public int findNPCByType(int templateID, int range, int index) {
		GameObject[] units = player.map.getUnits(player, range);
		for (GameObject obj : units) {
			if (obj.type == GameObject.TYPE_CREATURE
					&& ((Creature) obj).template.id == templateID) {
				// 找到了
				setValue(currentQuestId, index, obj.instanceId);
				return 1;
			}
		}
		return 0;
	}

	public int findNPC(int instanceId, int range) {
		GameObject object = ObjectAccessor.getGameObject(instanceId);
		if (object == null || object.type != GameObject.TYPE_CREATURE)
			return 0;
		if (object.isAlive() && player.inRange(object, range))
			return 1;
		return 0;
	}

	public int findNPCAt(int instanceId, int mapId, int x, int y, int distance) {
		GameObject object = ObjectAccessor.getGameObject(instanceId);
		if (object == null || object.type != GameObject.TYPE_CREATURE)
			return 0;
		if (object.isAlive() && object.map.id == mapId
				&& object.distance(x, y) <= distance * distance) {
			return 1;
		}
		return 0;
	}

	public int findPlayer(int playerId, int range) {
		Player p = ObjectAccessor.getPlayer(playerId);
		if (p != null)
			if (player.isAlive() && player.inRange(p, range))
				return 1;
		return 0;
	}

	public int hasItem(int itemId, int count) {
		PlayerTransaction tx = player.newTransaction("QST");
		if (player.bag.removeGameItemIngoreInstanceId(itemId, count, tx, false) != null) {
			tx.rollback();
			return 1;
		} else {
			tx.rollback();
			return 0;
		}
	}
	
	public int shout(int npcId,String msg,int duration){
		VMap map = player.getVMap();
		Creature c = map.getCreatureById(npcId);
		if(c != null){
			c.shout(msg, 5000, 0xFF0000, duration);
			return 1;
		}
		return 0;
	}
	
	public int lionshout(String msg){
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		chatService.sendWorldShout(player.name,player.id,player.faction, msg, 0xff4700, 11000);
		return 1;
	}
	
	public int bubble(int npcId,String text,int time){
		try {
            player.addBubble(npcId,text,time);
        } catch (Exception e) {
            return 0;
        }
        return 1;
	}

	public int hasTitle(int id) {
		return player.titles.hasTitle(id) ? 1 : 0;
	}
	
	public int e_HasAction(int type){
		return player.hasAction(type)?1:0;
	}

	public int addItem(int itemId, int count) {
		GameItem item = ObjectAccessor.createGameItem(itemId);
		if (item != null) {
			PlayerTransaction tx = player.newTransaction("QST");
			try {
				player.bag.addGameItemComplete(item, count, tx, true);
				tx.commit();
				return 1;
			} catch (Exception e) {
				tx.rollback();
				return 0;
			}

		} else {
			return 0;
		}
	}
	
	public int addBuff(int buffId, int buffLevel){
		try {
			player.buffs.addBuff(BuffUtil.createBuff(buffId, buffLevel, player, player, 0));
		} catch (Exception e) {
			return 0;
		}
		return 1;
	}
	
	public int removeItem(int itemId, int count) {
		PlayerTransaction tx = player.newTransaction("QST");
		if (itemId == -1) {
			try {
				player.decMoney(count, tx, true);
				tx.commit();
				return 1;
			} catch (NoEnoughValueException e) {
				tx.rollback();
				return 0;
			}
		} else {
			if (player.bag.removeGameItemIngoreInstanceId(itemId, count, tx, true) != null) {
				tx.commit();
				return 1;
			} else {
				tx.rollback();
				return 0;
			}
		}
	}

	public int openui(String value) {
		String[] ss = value.split("\\s+");
		Packet pt = new Packet(OpCode.OPENUI_SERVER);
		pt.putString(ss[0]);
		if (ss.length >= 2)
			pt.putString(ss[1]);
		else
			pt.putString("");
		player.send(pt);
		return 1;
	}
	
	public int savePosition(String value) {
		player.savePosition(value);
		return 1;
	}
	
	public int decActivePower(int value) {
		try {
			player.decActivePower(value);
			return 1;
		} catch (NoEnoughValueException e) {
			return 0;
		}
	}

	public int moveNPC(int npcId, int mapId, int x, int y) {
		return 0;
	}
	
	public int isChannel(String channel) {
		if (player.session != null) {
			Account a = (Account) player.session.getIdentity();
			if (a == null) {
				return 0;
			} else {
				if(a.getChannel()==null)
					return 0;
				int index = channel.indexOf(a.getChannel());
				if(index != -1){
					if(index+a.getChannel().length()==channel.length())
						return 1;
					else{
						if(channel.charAt(index+a.getChannel().length())=='|')
							return 1;
					}
				}
				return channel.equals(a.getChannel()) ? 1 : 0;
			}
		} else {
			return 0;
		}
	}
	
	public int joinAssociation(){
		if(player.associationInvite!=null){
			AssociationService service = Server.server.getServiceRegistry().getAssociationService();
			int associationId = player.associationInvite.associationId;
			try {
				service.injoyAssociation(player, associationId, AssociationMember.STAT_WORK);
				return 1;
			} catch (AssociationException e) {
				return 0;
			}
		}
		return 0;
	}
	
//	public static void main(String[] args){
//		String s1 = "ucweb|ucweb03";
//		String s2 = "ucw";
//		int index = s1.indexOf(s2);
//		if(index != -1){
//			if(index+s2.length()==s1.length())
//				System.out.println("ok");
//			else{
//				if(s1.charAt(index+s2.length())=='|')
//					System.out.println("ok");
//			}
//		}
//	}

	public int e_UseSkill(int skillId) {
		return player.lastSkillId == skillId ? 1 : 0;
	}

	public int e_Approach(int mapId, int x, int y) {
		if (player.map.id == mapId) {
			if (Math.abs(player.x - x) <= 80 && Math.abs(player.y - y) <= 80) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
	
	public int e_FirstApproach(int mapId, int x, int y) {
		if (player.map.id == mapId) {
			if (Math.abs(player.x - x) <= 80 && Math.abs(player.y - y) <= 80) {
				if (Math.abs(player.lastX - x) > 80 || Math.abs(player.lastY - y) > 80)
					return 1;
			}
		} 
		return 0;
	}
	
	public int e_ApprochPosition(int mapId, int x, int y, int dist){
		if (player.map.id == mapId) {
			int xdist = Math.abs(player.x - x);
			int ydist = Math.abs(player.y - y);
			if(xdist*xdist+ydist*ydist <= dist*dist)
				return 1;
			return 0;
		} else {
			return 0;
		}
	}

	public int e_EnterMap(int mapId) {
		if (player.map.id == mapId) {
			return 1;
		} else {
			return 0;
		}
	}

	public int e_Kill(int npcId, int index, int max) {
		int c = player.getKillCreatureCount(npcId);
		if (c > 0) {
			return addValue(currentQuestId, index, c, max);
		} else {
			return getValue(currentQuestId, index);
		}
	}
	
	public int e_KillWithMate(int npcId, int index, int max) {
		int c = player.getKillCreatureCount(npcId, player.getMateID());
		if (c > 0) {
			return addValue(currentQuestId, index, c, max);
		} else {
			return getValue(currentQuestId, index);
		}
	}
	
	public int e_killWithAssociation(int npcId, int index, int max) {
		int c = player.getKillCreatureCount(npcId, player.getAssociationLeaderID());
		if (c > 0) {
			return addValue(currentQuestId, index, c, max);
		} else {
			return getValue(currentQuestId, index);
		}
	}
	
	public int e_KillPlayer(int playerid) {
		if (playerid == 0) {
			// 参数为-1表示杀死任何玩家都算
			return player.killPlayers.isEmpty() ? 0 : 1;
		} else if (playerid < 0) {
			// 小于0，表示指定阵营
			for (int killid : player.killPlayers) {
				Player p = ObjectAccessor.getPlayer(killid);
				if (p.faction == -playerid) {
					return 1;
				}
			}
			return 0;
		} else {
			return player.killPlayers.contains(playerid) ? 1 : 0;
		}
	}
	
	public int e_KillPlayers(){
		return player.killPlayers.isEmpty() ? 0 : 1;
	}

	public int e_Killed() {
		return player.dieCause == 2 ? 1 : 0;
	}

	public int e_KilledByPlayer() {
		return player.dieCause == 1 ? 1 : 0;
	}

	public int e_UseItem(int itemid) {
		return player.lastItemId == itemid ? 1 : 0;
	}

	public int e_TouchNPC(int npcId) {
		ASMQuest quest = ASMQuestUtil.getQuest(currentQuestId);
		if (quest == null)
			return 0;
		if (quest.getGameQuest().isAreaQuest())
			return player.hasTouchNpc(npcId, -1) ? 1 : 0;
		return player.hasTouchNpc(npcId, currentQuestId) ? 1 : 0;
	}

	public int e_FinishTask() {
		return player.questToFinishQuestId == currentQuestId ? 1 : 0;
	}

	public int e_AnswerQuestion(int notifyId, int answer) {
		return player.hasUINotify(currentQuestId, notifyId,
				Player.NOTIFY_QUESTION, answer) ? 1 : 0;
	}

	public int e_CloseChat(int notifyId) {
		return player.hasUINotify(currentQuestId, notifyId, Player.NOTIFY_CHAT) ? 1
				: 0;
	}

	public int e_CloseMessage(int notifyId) {
		return player.hasUINotify(currentQuestId, notifyId,
				Player.NOTIFY_MESSAGE) ? 1 : 0;
	}

	protected VarStore getStore() {
		return stores.get(currentQuestId);
	}

	public int e_TaskFinish() {
		return finishTemps.remove(currentQuestId) ? 1 : 0;
		// ASMQuest quest = ASMQuestUtil.getQuest(currentQuestId);
		// return hasFinished(quest, Calendar.getInstance()) ? 1 : 0;
	}
	
	public int e_IsInTongBattle(){
		return Server.server.getServiceRegistry().getTongBattleVMapManager().isInTongBattle(player);
	}
	
	public int e_IsInNationBattle(){
		return Server.server.getServiceRegistry().getNationService().isInNationBattle(player);
	}
	
	public int e_IsInFlagBattle(){
		return Server.server.getServiceRegistry().getFlagBattleFieldVMapManager().isInFlagBattle(player);
	}
	
	public int isCity(String city){
		if(player.getAccount()!=null && player.getAccount().getCity()!=null){
			if(player.getAccount().getCity().equals(city)){
				return 1;
			}else if(ProvinceUtil.getProvinceByCity(player.getAccount().getCity()).equals(city)){
				return 1;
			}
		}
		return 0;
	}

	protected void addVarChanged(int questId, int index, int value) {
		for (ChangedVar cv : changed) {
			if (cv.questId == questId && cv.index == index) {
				cv.value = value;
				return;
			}
		}
		ChangedVar cv = new ChangedVar(questId, index, value);
		changed.add(cv);
	}

	public int setValue(int questId, int index, int value) {
		VarStore store = getStore();
		if (store.values[index] != value) {
			store.values[index] = value;
			addVarChanged(questId, index, value);
		}
		return value;
	}

	public int addValue(int questId, int index, int value) {
		VarStore store = getStore();
		store.values[index] += value;
		addVarChanged(questId, index, store.values[index]);
		return store.values[index];
	}

	public int decValue(int questId, int index, int value) {
		VarStore store = getStore();
		store.values[index] -= value;
		addVarChanged(questId, index, store.values[index]);
		return store.values[index];
	}

	public int getValue(int questId, int index) {
		VarStore store = getStore();
		return store.values[index];
	}

	public int addValue(int questId, int index, int value, int max) {
		VarStore store = getStore();
		int v = store.values[index];
		if (v < max) {
			v = Math.min(v + value, max);
			store.values[index] = v;
			addVarChanged(questId, index, v);
		}
		return v;
	}
	
	/**
	 * 读取一个变量的值（局部、系统、全局）。
	 * @param name
	 * @return 返回整数或字符串。
	 */
	public Object getVariableValue(String name) {
		if (name.startsWith("__")) {
			// 全局变量
			return getGlobalValue(name);
		} else if (name.startsWith("_")) {
			// 系统变量
			String s = name.toUpperCase();
			if ("_LEVEL".equals(s)) {
				return player.level;
			} else if ("_MONEY".equals(s)) {
				return player.getMoney();
			} else if ("_HP".equals(s)) {
				return player.hp;
			} else if ("_MAXHP".equals(s)) {
				return player.maxhp;
			} else if ("_MP".equals(s)) {
				return player.mp;
			} else if ("_MAXMP".equals(s)) {
				return player.maxmp;
			} else if ("_STR".equals(s)) {
				return player.strength;
			} else if ("_STA".equals(s)) {
				return player.stamina;
			} else if ("_INT".equals(s)) {
				return player.intellect;
			} else if ("_AGI".equals(s)) {
				return player.agility;
			} else if ("_SEX".equals(s)) {
				return player.sex;
			} else if ("_SEXNAME".equals(s)) {
				return player.getSexName();
			} else if ("_CLASS".equals(s)) {
				return player.clazz;
			} else if ("_CLASSNAME".equals(s)) {
				return player.getClassName();
	        } else if ("_FACTION".equals(s)) {
	        	return player.faction;
	        } else if ("_FACTIONNAME".equals(s)) {
	        	return player.getFactionName();
			} else if ("_NAME".equals(s)) {
				return player.name;
			} else if ("_MAPID".equals(s)) {
				return player.map.getId();
			} else if ("_X".equals(s)) {
				return player.x;
			} else if ("_Y".equals(s)) {
				return player.y;
			} else if ("_KEYBOARDTYPE".equals(s)) {
				return player.getKeyboardType();
	        } else if ("_MOUSETYPE".equals(s)) {
	        	return player.getMouseType();
	        } else if ("_MATEID".equals(s)) {
	        	return player.getMateID();
			} else if ("_ISKING".equals(s)){
				return player.isKing();
			} else if ("_ISOFFICER".equals(s)){
				return player.isOfficer();
			} else if ("_HASTONG".equals(s)) {
				return player.hasGuild();
			} else if ("_ACTIVEPOWER".equals(s)) {
				return player.activePower;
			} else if ("_ADDEDPROPERTYPOINT".equals(s)) {
				return player.getAddedPropertyPoint();
			} else if ("_ADDEDSKILLPOINT".equals(s)) {
				return player.getAddedSkillPoint();
			} else if ("_RECEIVEASSOINV".equals(s)) {
				return player.hasReceiveAssoInv();
			} else if ("_HAVEASSOCIATION".equals(s)){
				return player.hasAssociation();
			} else if ("_FOLLOWATTENDANT".equals(s)){
                return player.attendantIsFollowing();
            }
		} else if (name.startsWith("v")) {
			// 局部变量
			return getValue(currentQuestId, Integer.parseInt(name.substring(1)));
		}
		return null;
	}

	/**
	 * 取得全局变量的值。全局变量名的格式包括： __PLAYER_xxx 玩家变量 __TONG_xxx 军团变量 __FACTION_xxx 国家变量
	 * __WORLD_xxx 世界变量 __PARTY_xxx 团队变量
	 * 
	 * @param name
	 * @return
	 */
	public int getGlobalValue(String name) {
		if (name.startsWith("__PLAYER_")) {
			return player.pool.getInt(name.substring("__PLAYER_".length()));
		} else if (name.startsWith("__TONG_")) {
			TongService ts = Server.server.getServiceRegistry()
					.getTongService();
			Tong t = ts.getPlayerTong(player.id);
			if (t != null) {
				return t.pool.getInt(name.substring("__TONG_".length()));
			}
		} else if (name.startsWith("__FACTION_")) {
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(player.faction);
			PropertyPool pool = nation.pool;
			if (pool != null) {
				return pool.getInt(name.substring("__FACTION_".length()));
			}
		} else if (name.startsWith("__WORLD_")) {
			String pro = name.substring("__WORLD_".length());
			if (pro.equals("DAY")) {
				return Time.day;
			} else {
				PropertyService ps = Server.server.getServiceRegistry()
						.getPropertyService();
				PropertyPool pool = ps.getPropertyPool(0);
				if (pool != null) {
					return pool.getInt(pro);
				}
			}
		} else if (name.startsWith("__PARTY_")) {
			if (player.party != null) {
				return player.party.pool.getInt(name.substring("__PARTY_"
						.length()));
			}
		}
		return 0;
	}

	/**
	 * 设置全局变量的值。变量名规范参见{{@link #getGlobalValue(String)}。
	 * 
	 * @param name
	 * @param newValue
	 */
	public int setGlobalValue(String name, int newValue) {
		if (name.startsWith("__PLAYER_")) {
			player.pool.setInt(name.substring("__PLAYER_".length()), newValue);
		} else if (name.startsWith("__TONG_")) {
			TongService ts = Server.server.getServiceRegistry()
					.getTongService();
			Tong t = ts.getPlayerTong(player.id);
			if (t != null) {
				t.pool.setInt(name.substring("__TONG_".length()), newValue);
			}
		} else if (name.startsWith("__FACTION_")) {
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(player.faction);
			PropertyPool pool = nation.pool;
			if (pool != null) {
				pool.setInt(name.substring("__FACTION_".length()), newValue);
			}
		} else if (name.startsWith("__WORLD_")) {
			PropertyService ps = Server.server.getServiceRegistry()
					.getPropertyService();
			PropertyPool pool = ps.getPropertyPool(0);
			if (pool != null) {
				pool.setInt(name.substring("__WORLD_".length()), newValue);
			}
		} else if (name.startsWith("__PARTY_")) {
			if (player.party != null) {
				player.party.pool.setInt(name.substring("__PARTY_".length()),
						newValue);
			}
		}
		return 1;
	}

	/**
	 * 修改全局变量的值。变量名规范参见{{@link #getGlobalValue(String)}。
	 * 
	 * @param name
	 * @param newValue
	 */
	public int changeGlobalValue(String name, int add) {
		if (name.startsWith("__PLAYER_")) {
			name = name.substring("__PLAYER_".length());
			return player.pool.setInt(name, player.pool.getInt(name) + add);
		} else if (name.startsWith("__TONG_")) {
			name = name.substring("__TONG_".length());
			TongService ts = Server.server.getServiceRegistry()
					.getTongService();
			Tong t = ts.getPlayerTong(player.id);
			if (t != null) {
				return t.pool.setInt(name, t.pool.getInt(name) + add);
			}else{
				return 0;
			}
		} else if (name.startsWith("__FACTION_")) {
			name = name.substring("__FACTION_".length());
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(player.faction);
			PropertyPool pool = nation.pool;
			if (pool != null) {
				return pool.setInt(name, pool.getInt(name) + add);
			}else
				return 0;
		} else if (name.startsWith("__WORLD_")) {
			name = name.substring("__WORLD_".length());
			PropertyService ps = Server.server.getServiceRegistry()
					.getPropertyService();
			PropertyPool pool = ps.getPropertyPool(0);
			if (pool != null) {
				return pool.setInt(name, pool.getInt(name) + add);
			}else{
				return 0;
			}
		} else if (name.startsWith("__PARTY_")) {
			name = name.substring("__PARTY_".length());
			if (player.party != null) {
				return player.party.pool.setInt(name, player.party.pool.getInt(name)
						+ add);
			}else{
				return 0;
			}
		}else
			return 0;
	}

	public int[] getQuestStore(int questId) {
		VarStore store = stores.get(questId);
		// if (store == null) {
		// ASMQuest quest = ASMQuestUtil.getQuest(questId);
		// store = new VarStore(questId,
		// quest.getGameQuest().getScript().getFields().length);
		// stores.put(questId, store);
		// }
		return store.values;
	}

	public Collection<ASMQuest> getQuests() {
		return new ArrayList<ASMQuest>(quests.values());
	}

	public void clientSyncVariable(int questId, int index, int value) {
		ASMQuest quest = quests.get(questId);
		if (quest != null
				&& quest.getGameQuest().isClientModificationAllowed(index)) {
			VarStore store = stores.get(questId);
			store.values[index] = value;
		}
	}

	public void syncVariables() {
		if (changed.size() > 0) {
			Packet pt = new Packet(OpCode.VM_VARIABLE_SYNC_SERVER);
			pt.put(changed.size());
			for (ChangedVar cv : changed) {
				pt.putInt(cv.questId);
				pt.putInt(cv.index);
				pt.putInt(cv.value);
			}
			player.send(pt);
			changed.clear();
		}
	}
	
	public int getCurrentCycle(){
		return cycleQuestInfo.currentCycle;
	}
	
	public int getCurrentCycleIndex(){
		return cycleQuestInfo.currentIndex;
	}
	
	public int getCurrentCycleState(){
		return cycleQuestInfo.state;
	}
	
	public int getFinishedCycle(){
		return cycleQuestInfo.finished.size();
	}
	
	public ItemTemplate getCycleReward(int cycle){
		return ObjectAccessor.getItemTemplate(CycleRewardEntry.CYCLE_ITEMS[cycle]);
	}
	
	public ItemTemplate getCycleReward(){
		return getCycleReward(getFinishedCycle());
	}
	
	public static class CycleQuestInfo{
		public int currentCycle; //当前环数,0表示没有当前环
		public int currentIndex; //当前节点索引，0表示没有节点,如果currentCycle不为0，此成员的值也应该是不为0的
		public int state; //当前节点状态(正在进行1，完成2)
		public int day; //记录时间
		public long finishTime; //记录任务提交时间
		public Set<Integer> finished = new HashSet<Integer>(); //当前已经完成的环index
		
		public void cycle(){
			if(day != 0 && Time.day != day){
				if(finished.size()!=0){
					finished.clear();  //过了当天清除
				}
			}
			if(Time.day != day && currentCycle!=0 && state==2 
			&& System.currentTimeMillis()-finishTime>60*60*1000L){
				clearCurrentState();
			}
		}
		
		public void abandon(Player p,GameQuest quest){
			GameQuest.CycleInfo ci = quest.getCycleInfo();
			if(ci.type == GameQuest.CycleInfo.TYPE_HEAD){ //如果是每环的第一个任务
				clearCurrentState();
			}else{ //否则回退到上一个节点任务完成前的状态
				currentIndex = ci.index - 1;
				state = 2;
				finishTime = System.currentTimeMillis();
			}
		}
		
		public void accept(Player p,GameQuest quest,boolean updateDay){
			GameQuest.CycleInfo ci = quest.getCycleInfo();
			this.currentCycle = ci.cycle;
			this.currentIndex = ci.index;
			this.state = 1;
			this.day = Time.day;
		}
		
		public void finished(Player p,GameQuest quest){
			GameQuest.CycleInfo ci = quest.getCycleInfo();
			if(ci.cycle == currentCycle&&ci.index == currentIndex){
				state = 2;
				this.finishTime = System.currentTimeMillis();
				if(ci.type == GameQuest.CycleInfo.TYPE_TAIL){
					clearCurrentState();
					finished.add(ci.cycle);
					int cycle = p.asmVm.getFinishedCycle();
					Gain gain = new Gain(p);
					if(cycle/3>0 && cycle%3==0){
						PlayerTransaction tx = p.newTransaction("QST");
						try {
							gain.addGainItem(ObjectAccessor.createGameItem(1311), 1);
							p.addGainComplete(gain, tx, true);
							tx.commit();
						} catch (NoEnoughSpaceException e) {
							tx.rollback();
						}
					}
					if(cycle/7>0 && cycle%7==0){
						PlayerTransaction tx = p.newTransaction("QST");
						try {
							gain.addGainItem(ObjectAccessor.createGameItem(1311), 1);
							p.addGainComplete(gain, tx, true);
							tx.commit();
						} catch (NoEnoughSpaceException e) {
							tx.rollback();
						}
					}
				}
			}else{
				log.error("[CYCLEQUESTERROR]" + LogUtil.getPlayerLogString(p) + "QUEST[" + quest.getId() + "]");
			}
		}
		
		public void clearCurrentState(){
			currentCycle = 0;
			currentIndex = 0;
			state = 0;
			finishTime = 0;
		}
		
		public boolean preConditionOk(Player p,GameQuest quest){
			if(currentCycle != 0){ //如果有当前环数据
				if(state==1) //如果正在进行某个节点，那么所有任务都是不可接的
					return false;
				else{ //如果是当前节点的下一个节点，那么就是可以接的
					GameQuest.CycleInfo ci = quest.getCycleInfo();
					return ci.index == currentIndex+1 && currentCycle == ci.cycle;
				}
			}else{ 
				GameQuest.CycleInfo ci = quest.getCycleInfo();
				//如果是头结点，并且等级符合并且今天没有跑完，那么就是可以接受的
				return ci.type==GameQuest.CycleInfo.TYPE_HEAD&&p.level>=ci.level&&!finished.contains(ci.cycle);
			}
		}
	}
}

/**
 * 任务变量池，每个任务对应一个，所有变量都是int类型，由变量下标来访问
 * 
 * @author Jeffrey
 * 
 */
class VarStore implements Cloneable {

	public int questId;
	public int[] values;

	public VarStore(int questId, int varCount) {
		this.questId = questId;
		this.values = new int[varCount];
	}

	public VarStore(int questId, int[] values) {
		this.questId = questId;
		this.values = values;
	}

	@Override
	public VarStore clone() {
		int[] v = new int[values.length];
		System.arraycopy(values, 0, v, 0, values.length);
		return new VarStore(questId, v);
	}
}

class ChangedVar {
	public int questId;
	public int index;
	public int value;

	public ChangedVar(int questId, int index, int value) {
		super();
		this.questId = questId;
		this.index = index;
		this.value = value;
	}

}

class Finish {
	public int questId;
	public long finishTime;

	public Finish(int questId, long finishTime) {
		this.questId = questId;
		this.finishTime = finishTime;
	}

}
