package peony.service.player;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import ch.javasoft.util.intcoll.IntHashMap;
import peony.game.ChatOption;
import peony.game.Equipments;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.GameObjectRef;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.game.drop.GroupDrop;
import peony.game.roll.Roll;
import peony.game.roll.RollService;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

public class KillPlayerService implements Service {

	private static Random random = new Random();
	
	public int groupId = -1;
	
	public int ratio = 0;
	
	//三行N列的数据,第一行为被杀死的玩家ID,第二行为被杀死的天,第三行为被杀死次数
	protected IntHashMap<int[][]> killRecords = new IntHashMap<int[][]>(); 
	
	protected static int[] killNotifyNum = {20,40,60,100,150,200,250,300};
	protected static String[] killNotifys = {
		"{0}{1}达成{2}连杀，此人已经遁入魔道！",
		"{0}{1}达成{2}连杀，笑道：神挡杀神，佛挡杀佛！",
		"{0}{1}达成{2}连杀，笑道：命若天定，我就破了你这个天！",
		"{0}{1}达成{2}连杀，笑道：千载霸气，凝而不散，绝代豪雄，睥睨人间。",
		"{0}{1}达成{2}连杀，笑道：神已死，魔已灭，敢问天下谁可以与我一战？",
		"{0}{1}达成{2}连杀，笑道：但求一败！",
		"{0}{1}达成{2}连杀，笑道：待到逆乱阴阳时，以我魔血染青天!",
		"{0}{1}达成{2}连杀，笑道：我不是神。但我做的事。神也未必做得到"
	};
	
//	protected IntHashMap<Integer> notifyRecords = new IntHashMap<Integer>();
	
//	public static int MIN_KILL_NOTIFY_LEVEL = 65;
//	public static int MIN_ANTIKILL_NUM = 20;
	
	public void shutdown() {
//		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
//		Server.server.getEventManager().registerListener(this);
	}
	
	public int getRankPvpDropEquipment(Player p){
		Equipments equipments = p.equipments;
		GameItem[] onGameItems = equipments.equs;
		List<GameItem> list = new ArrayList<GameItem>();
		for(GameItem item : onGameItems){
			if(item!=null && item.template!=null && item.template.isEquipment()){
				if(item.template.equipment.canCopy){
					list.add(item);
				}
			}
		}
		if(p.horse!=null && p.horse.equs!=null && p.horse.equs.equs!=null){
			for(GameItem item : p.horse.equs.equs){
				if(item!=null && item.template!=null && item.template.isEquipment()){
					if(item.template.equipment.canCopy){
						list.add(item);
					}
				}
			}
		}
		int ran = random.nextInt(10000);
//		ran = 1; // 方便测试
		if(ran<18){
			int gainItemId = getPvpDropEquipment(3,list);
			if(gainItemId==-1)
				gainItemId = getPvpDropEquipment(2,list);
			if(gainItemId==-1)
				gainItemId = getPvpDropEquipment(1,list);
			if(gainItemId==-1)
				return -1;
			return gainItemId;
		}else if(ran<150){
			int gainItemId = getPvpDropEquipment(2,list);
			if(gainItemId==-1)
				gainItemId = getPvpDropEquipment(1,list);
			if(gainItemId==-1)
				return -1;
			return gainItemId;
		}else if(ran<1200){
			int gainItemId = getPvpDropEquipment(1,list);
			if(gainItemId==-1)
				return -1;
			return gainItemId;
		}
		return -1;
	}
	
	/** 1绿,2蓝,3紫 */
	protected int getPvpDropEquipment(int type, List<GameItem> items){
		List<Integer> ids = new ArrayList<Integer>();
		for(GameItem item : items){
			if(item.template.quality==type){
				ids.add(item.template.id);
			}
		}
		if(ids.size()==0)
			return -1;
		int index = random.nextInt(ids.size());
		return ids.get(index);
	}
	
	public void rollPvpGainGameItem(List<Player> benefitPlayers, int itemId){
		if(itemId==-1)
			return;
		if(benefitPlayers.size()==1){
			// 如果击杀方只有一人,则直接复制装备
			GameItem gainGameItem = ObjectAccessor.createGameItem(itemId);
			Player p = benefitPlayers.get(0);
			int copyDay = p.pool.getInt(Player.PROPERTY_COPYEQUIP_DAY, 0);
			int copyBlueCount = p.pool.getInt(Player.PROPERTY_COPYEQUIP_COUNT+"2", 0);
			int copyPurpleCount = p.pool.getInt(Player.PROPERTY_COPYEQUIP_COUNT+"3", 0);
			int copyGreenCount = p.pool.getInt(Player.PROPERTY_COPYEQUIP_COUNT+"1", 0);
			if(copyDay==Time.day && (copyBlueCount+copyPurpleCount)>=3){
				return;
			}
			if(copyDay==Time.day && copyGreenCount>=20){
				return;
			}
			PlayerTransaction tx = p.newTransaction("KILLPLAYERROLL");
			try {
				p.bag.addGameItemComplete(gainGameItem, 1, tx, false);
				tx.commit();
				
				int quality = gainGameItem.template.quality;
				if(copyDay==Time.day){
					p.pool.setInt(Player.PROPERTY_COPYEQUIP_COUNT+quality, p.pool.getInt(Player.PROPERTY_COPYEQUIP_COUNT+quality, 0)+1);
				}else{
					p.pool.setInt(Player.PROPERTY_COPYEQUIP_DAY, Time.day);
					p.pool.setInt(Player.PROPERTY_COPYEQUIP_COUNT+quality, 1);
				}
				if(quality>=2){
					ChatService chatService = Server.server.getServiceRegistry().getChatService();
					chatService.sendAreaSystemMessage(MessageFormat
							.format(peony.Messages.STRING_00035, p.name, gainGameItem.template.name), p.map.id);
				}
			} catch (NoEnoughSpaceException e) {
				tx.rollback();
			}
			return;
		}
		// 击杀方为多人的情况，ROLL
		GameObjectRef[] rolls = new GameObjectRef[benefitPlayers.size()];
		for(int i=0;i<benefitPlayers.size();i++){
			rolls[i] = benefitPlayers.get(i).ref();
		}
		GameItem gainGameItem = ObjectAccessor.createGameItem(itemId);
		Roll roll = new Roll(Server.server.getServiceRegistry()
				.getRollService(), rolls, gainGameItem, 1, Time.currTime);
		Packet pt = new Packet(OpCode.ROLL_SERVER);
		pt.putInt(roll.id);
		pt.put(GameItem.toClientBytes(gainGameItem.template));
		pt.putInt(Time.currTime + Roll.TIMEOUT);
		RollService rollService = Server.server.getServiceRegistry().getRollService();
		rollService.pvpRollIds.add(roll.id);
		for(Player p : benefitPlayers){
			if(p!=null && itemId>0){
				p.send(pt);
			}
		}
		LogUtil.logCreateRoll(roll.id, benefitPlayers, gainGameItem, 1);
	}
	
	/**
	 * 杀人掉装备活动
	 * @param benefitPlayers
	 * @param player
	 */
	public void rollPvpActivityDrop(List<Player> benefitPlayers,Player player){
		GainItem[] gainItems = getDropItem(player);
		if(gainItems == null)
			return;
		if(benefitPlayers.size() == 1){
			Player p = benefitPlayers.get(0);
			PlayerTransaction tx = p.newTransaction("KILLPLAYERACTROLL");
			for(GainItem gi : gainItems){
				try{
					p.bag.addGameItemComplete(gi.getItem(), gi.getCount(), tx, true);
					tx.commit();
				} catch (NoEnoughSpaceException e){
					tx.rollback();
				}
			}
			return;
		}
		//击杀方为多人的情况，ROLL
		GameObjectRef[] rolls = new GameObjectRef[benefitPlayers.size()];
		for(int i=0;i<benefitPlayers.size();i++){
			rolls[i] = benefitPlayers.get(i).ref();
		}
		for(GainItem gi : gainItems){
			Roll roll = new Roll(Server.server.getServiceRegistry().getRollService(),rolls,gi.getItem(),gi.getCount(),Time.currTime);
			Packet pt = new Packet(OpCode.ROLL_SERVER);
			pt.putInt(roll.id);
			pt.put(GameItem.toClientBytes(gi.getItem().template));
			pt.putInt(Time.currTime+Roll.TIMEOUT);
			RollService rollService = Server.server.getServiceRegistry().getRollService();
			rollService.pvpRollIds.add(roll.id);
			for(Player p:benefitPlayers){
				if(p!=null){
					p.send(pt);
				}
			}
		}
	}
	
	public GainItem[] getDropItem(Player player){
		int ran = random.nextInt(10000);
		if(ran < ratio){
			Gain gain = new Gain(player);
			GroupDrop gd = ObjectAccessor.getGroupDrop(groupId);
			gd.calc(random, gain);
			return gain.getGainItems();
		}
		return null;
	}

//	public int[] getEventTypes() {
//		return new int[]{
//				ServiceEvent.EVENT_UNIT_DIE
//		};
//	}

//	public void handleEvent(ServiceEvent event) {
//		switch(event.type){
//		case ServiceEvent.EVENT_UNIT_DIE:
//			processKillPlayer((Unit)event.param1, (Unit)event.param2);
//			break;
//		}
//	}
	
	/** 杀人上电视处理方法 */
//	protected void processKillPlayer(Unit died, Unit killer){
//		if(died!=null && killer!=null && died instanceof Player && killer instanceof Player 
//				&& died.level>=MIN_KILL_NOTIFY_LEVEL && killer.level>=MIN_KILL_NOTIFY_LEVEL){
//			Player dPlayer = (Player)died;
//			Player kPlayer = (Player)killer;
//			if(dPlayer.faction!=kPlayer.faction){
//				int currentDay = Time.currentDayOfYear;
//				int[][] arr = killRecords.get(kPlayer.id);
//				if(arr==null){	//第一次杀人的数据存储(新建表增加第一条数据)
//					arr = new int[3][1];
//					arr[0][0] = dPlayer.id;
//					arr[1][0] = currentDay;
//					arr[2][0] = 1;
//				}else{
//					int killIndex = getIndexByDiedId(arr, dPlayer.id);
//					int killDay = getKillDayByIndex(arr, killIndex);
//					if(killDay==-1){ //有杀人记录，但第一次杀死本玩家的数据存储(表中增加一列)
//						int arrLenghth = arr[0].length;
//						int[][] arrTemp = new int[3][arrLenghth+1];
//						copyArr(arr, arrTemp);
//						arr = arrTemp;
//						arr[0][arrLenghth] = dPlayer.id;
//						arr[1][arrLenghth] = currentDay;
//						arr[2][arrLenghth] = 1;
//					}else if(killDay!=currentDay){ //以前杀死过本玩家的数据存储(找到表中对应的列数据进行修改)
//						arr[2][killIndex] += 1;
//						arr[1][killIndex] = currentDay;
//					}
//				}
//				killRecords.put(kPlayer.id, arr);
//				int killCount = getTotalKillCount(arr);
//				int notifyIndex = getNotifyIndex(killCount);
//				if(!hasNotify(kPlayer.id, killCount) && notifyIndex>-1){ //达到本杀人级别且之前没上电视的,上电视
//					String notify = killNotifys[notifyIndex];
//					String mapName = "";
//					try {
//						String tempString = kPlayer.getVMap().mapDef.mapInfo.name;
//						mapName = MessageFormat.format("{0}{1}", "在", tempString.substring(0, 2));
//					} catch (Exception e) {
//					}
//					ChatMessage cm = new ChatMessage(ChatOption.WORLD,kPlayer.id,-1,"系统",MessageFormat.format(notify, kPlayer.name, mapName, killNotifyNum[notifyIndex]),null);
////					Server.server.getServiceRegistry().getChatService().sendWorldMessage(MessageFormat.format(notify, kPlayer.name, killNotifyNum[notifyIndex]));
//					Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
//					recordNotify(kPlayer.id, killNotifyNum[notifyIndex]);
//				}
//				
//				int num = getTotalKillCount(killRecords.get(dPlayer.id));
//				if(num>0){ //杀死有上电视的玩家,取消上电视玩家的记录清空
//					killRecords.remove(dPlayer.id);
//					notifyRecords.remove(dPlayer.id);
//					if(num>=MIN_ANTIKILL_NUM){
//						String mapName = "";
//						try {
//							String tempString = kPlayer.getVMap().mapDef.mapInfo.name;
//							mapName = MessageFormat.format("{0}{1}", "在", tempString.substring(0, 2));
//						} catch (Exception e) {
//						}
//						ChatMessage cm = new ChatMessage(ChatOption.WORLD,kPlayer.id,-1,"系统",MessageFormat.format("{0}{1}终结了{2}的{3}连杀", kPlayer.name, mapName, dPlayer.name, num),null);
////						Server.server.getServiceRegistry().getChatService().sendWorldMessage(MessageFormat.format("{0}终结了{1}的{2}连杀", kPlayer.name, dPlayer.name, num));
//						Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
//					}
//				}
//			}
//		}
//	}
	
	protected int getTotalKillCount(int[][] arr){
		if(arr==null)
			return 0;
		int total = 0;
		for(int i=0;i<arr[0].length;i++){
			total += arr[2][i];
		}
		return total;
	}
	
	private void copyArr(int[][] src, int[][] dest){
		for(int i=0;i<3;i++){
			for(int j=0;j<src[0].length;j++){
				dest[i][j] = src[i][j];
			}
		}
	}
	
	protected int getIndexByDiedId(int[][] arr, int diedId){
		for(int i=0;i<arr[0].length;i++){
			if(arr[0][i]==diedId)
				return i;
		}
		return -1;
	}
	
	protected int getKillDayByIndex(int[][] arr, int index){
		try {
			return arr[1][index];
		} catch (Exception e) {
			return -1;
		}
	}
	
	protected int getNotifyIndex(int killCount){
		int index = -1;
		for(int i=0;i<killNotifyNum.length;i++){
			if(killNotifyNum[i]==killCount)
				index = i;
		}
		return index;
	}
	
//	protected boolean hasNotify(int playerId, int num){
//		if(notifyRecords.get(playerId)==null || notifyRecords.get(playerId)<num)
//			return false;
//		return true;
//	}
//	
//	protected void recordNotify(int playerId, int num){
//		notifyRecords.put(playerId, new Integer(num));
//	}
	
}
