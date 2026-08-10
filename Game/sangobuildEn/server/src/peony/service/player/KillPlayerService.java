package peony.service.player;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import peony.game.Equipments;
import peony.game.GameItem;
import peony.game.GameObjectRef;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.chat.ChatService;
import peony.game.roll.Roll;
import peony.game.roll.RollService;
import peony.net.Packet;
import peony.service.Service;

public class KillPlayerService implements Service {

	private static Random random = new Random();
	
	public void shutdown() {
		
	}

	public void startup() throws Exception {
		
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
		if(ran<3){
			int gainItemId = getPvpDropEquipment(3,list);
			if(gainItemId==-1)
				gainItemId = getPvpDropEquipment(2,list);
			if(gainItemId==-1)
				gainItemId = getPvpDropEquipment(1,list);
			if(gainItemId==-1)
				return -1;
			return gainItemId;
		}else if(ran<30){
			int gainItemId = getPvpDropEquipment(2,list);
			if(gainItemId==-1)
				gainItemId = getPvpDropEquipment(1,list);
			if(gainItemId==-1)
				return -1;
			return gainItemId;
		}else if(ran<300){
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
							.format("{0}擊殺敵將,繳獲{1}一件", p.name, gainGameItem.template.name), p.map.id);
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
	}

}
