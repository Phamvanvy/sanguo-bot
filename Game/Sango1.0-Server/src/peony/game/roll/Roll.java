package peony.game.roll;

import java.text.MessageFormat;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import peony.game.Actor;
import peony.game.ChatOption;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.GameObjectRef;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.game.chat.ItemChatAttachment;

public class Roll {

	private static final Logger log = Logger.getLogger(Roll.class);
	
	private static final AtomicInteger ids = new AtomicInteger(1);
	private static final Random rnd = new Random();

	public static final int STATE_STARTED = 0;
	public static final int STATE_END = 1;

	public static final int TIMEOUT = 180 * 1000;

	public int id;
	public RollInfo[] infos;
	public GameItem item;
	public int count;
	public int startTime;
	public int state = 0; // 0 是没有roll 1 是选择了需求 2 是选择了放弃

	protected RollService service;

	public Roll(RollService service, GameObjectRef[] refs, GameItem item,
			int count, int startTime) {
		id = ids.incrementAndGet();
		infos = new RollInfo[refs.length];
		for (int i = 0; i < infos.length; i++) {
			infos[i] = new RollInfo(refs[i]);
		}
		this.item = item;
		this.count = count;
		this.startTime = startTime;
		this.service = service;
		this.service.addRoll(this);
	}

	public void addRollPoint(GameObjectRef ref) {
		if (state != STATE_END) {
			for (RollInfo info : infos) {
				if (info.ref.equals(ref) && info.state == 0) { // 如果没roll过点才让roll
					int point = rnd.nextInt(101);
					info.setPoint(point);
					checkEnd(false);
				}
			}
		}
	}

	public void discard(GameObjectRef ref) {
		if (state != STATE_END) {
			for (RollInfo info : infos) {
				if (info.ref.equals(ref) && info.state == 0) { // 如果没roll过点才让roll
					info.state = 2;
					checkEnd(false);
				}
			}
		}
	}

	/*
	 * 寻找点数最大的，忽略放弃的
	 */
	protected void checkEnd(boolean timeout) {
		RollService rollService = Server.server.getServiceRegistry().getRollService();
		RollInfo max = null;
		boolean allDiscard = true;
		int remainCount = 0;      // 剩余在线人数
		for (RollInfo info : infos) {
		    // 如果玩家下线了，则视为放弃
//		    Player p = (Player) ObjectAccessor.getGameObject(info.ref);
//		    if (p == null) {
//		        info.state = 2;
//		    } else {
		        remainCount++;
//		    }
			if (info.state == 1) {
				if (max == null) {
					max = info;
				} else {
					if (max.compareTo(info) < 0) {
						max = info;
					}
				}
				allDiscard = false;
			} else if (info.state == 0) { // 如果超时，那么默认是需求
				if (!timeout)
					return;
				else {
					int point = rnd.nextInt(101);
					info.setPoint(point);
					allDiscard = false;
					if (max == null) {
						max = info;
					} else {
						if (max.compareTo(info) < 0) {
							max = info;
						}
					}
				}
			} else if (info.state == 2) {
				continue;
			}
		}
		if (allDiscard) { // 如果全部放弃，结束
//		    if (remainCount > 0) {
//    			int index = rnd.nextInt(remainCount);
//    			for (RollInfo info : infos) {
//                    Player p = (Player) ObjectAccessor.getGameObject(info.ref);
////                    if (p == null) {
////                        continue;
////                    }
//                    if (index == 0) {
//                    	sendResult(allDiscard, info.ref.id);
//                    	PlayerTransaction tx = null;
//                    	Gain gain = new Gain(p);
//        				if(p!=null){
//        					tx = p.newTransaction("ROL");
//                            gain.addGainItem(item, count);
//             				p.addGain(gain, tx, true);
//        				}else{
//        					Server.server.getServiceRegistry().getMailService().sendSystemMailAsync
//    							(info.ref.id, peony.Messages.STRING_00004, peony.Messages.STRING_00034, "", 0, item, count, "ROLL");
//        					break;
//        				}
//        				//pvp装备
//        				if(rollService.pvpRollIds.contains(id)){
//        					int copyDay = p.pool.getInt(Player.PROPERTY_COPYEQUIP_DAY, 0);
//        					int copyBlueCount = p.pool.getInt(Player.PROPERTY_COPYEQUIP_COUNT+"2", 0);
//        					int copyPurpleCount = p.pool.getInt(Player.PROPERTY_COPYEQUIP_COUNT+"3", 0);
//        					int copyGreenCount = p.pool.getInt(Player.PROPERTY_COPYEQUIP_COUNT+"1", 0);
//        					if(copyDay==Time.day && (copyBlueCount+copyPurpleCount)>=3){
//        						tx.rollback();
//        						break;
//        					}else if(copyDay==Time.day && copyGreenCount>=20){
//        						tx.rollback();
//        						break;
//        					}
//        					int quality = gain.getGainItems()[0].getItem().template.quality;
//            				if(copyDay==Time.day){
//            					p.pool.setInt(Player.PROPERTY_COPYEQUIP_COUNT+quality, p.pool.getInt(Player.PROPERTY_COPYEQUIP_COUNT+quality, 0)+1);
//            				}else{
//            					p.pool.setInt(Player.PROPERTY_COPYEQUIP_DAY, Time.day);
//            					p.pool.setInt(Player.PROPERTY_COPYEQUIP_COUNT+quality, 1);
//            				}
//            				if(quality>=2){
//            					ChatService chatService = Server.server.getServiceRegistry().getChatService();
//                				chatService.sendAreaSystemMessage(MessageFormat
//                						.format(peony.Messages.STRING_00035, p.name, gain.getGainItems()[0].getItem().template.name), p.map.id);
//            				}
//        				}
//        				tx.commit();
//        				break;
//                    }
//                    index--;
//    			}
//		    }
			this.state = STATE_END;
		} else {
			if (max != null) {
				Player p = (Player) ObjectAccessor.getGameObject(max.ref);
				if (p != null) {
					Gain gain = new Gain(p);
					gain.addGainItem(item, count);
					PlayerTransaction tx = p.newTransaction("ROL");
					p.addGain(gain, tx, true);
					if(gain.getGainItems().length>0){
						for(GainItem gainItem : gain.getGainItems()){
							if(gainItem.getItem().template.id == 3970){
								ItemChatAttachment attItem = new ItemChatAttachment(gainItem.getItem());
								String message = MessageFormat.format("{0}击杀年兽后意外地获得了稀有年兽坐骑/-1!",p.name);
								ChatMessage cm = new ChatMessage(ChatOption.WORLD, p.id, -1, peony.Messages.STRING_00004, message, attItem);
								Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
							}
						}
					}
					//pvp装备
    				if(rollService.pvpRollIds.contains(id)){
    					int copyDay = p.pool.getInt(Player.PROPERTY_COPYEQUIP_DAY, 0);
    					int copyBlueCount = p.pool.getInt(Player.PROPERTY_COPYEQUIP_COUNT+"2", 0);
    					int copyPurpleCount = p.pool.getInt(Player.PROPERTY_COPYEQUIP_COUNT+"3", 0);
    					int copyGreenCount = p.pool.getInt(Player.PROPERTY_COPYEQUIP_COUNT+"1", 0);
    					if(copyDay==Time.day && (copyBlueCount+copyPurpleCount)>=3){
    						tx.rollback();
    						this.state = STATE_END;
    						return;
    					}else if(copyDay==Time.day && copyGreenCount>=20){
    						tx.rollback();
    						this.state = STATE_END;
    						return;
    					}
    					int quality = gain.getGainItems()[0].getItem().template.quality;
        				if(copyDay==Time.day){
        					p.pool.setInt(Player.PROPERTY_COPYEQUIP_COUNT+quality, p.pool.getInt(Player.PROPERTY_COPYEQUIP_COUNT+quality, 0)+1);
        				}else{
        					p.pool.setInt(Player.PROPERTY_COPYEQUIP_DAY, Time.day);
        					p.pool.setInt(Player.PROPERTY_COPYEQUIP_COUNT+quality, 1);
        				}
        				if(quality>=2){
        					ChatService chatService = Server.server.getServiceRegistry().getChatService();
            				chatService.sendAreaSystemMessage(MessageFormat
            						.format(peony.Messages.STRING_00035, p.name, gain.getGainItems()[0].getItem().template.name), p.map.id);
        				}
    				}
					tx.commit();
					sendResult(allDiscard, max.ref.id);
				}else{
					sendResult(allDiscard, max.ref.id);
					Server.server.getServiceRegistry().getMailService().sendSystemMailAsync
						(max.ref.id, peony.Messages.STRING_00004, peony.Messages.STRING_00034, "", 0, item, count, "ROLL");
				}
			}
			this.state = STATE_END;
		}
	}

	/*
	 * 如果超时先检测是否所有人都放弃，如果都放弃，那么随机选一个。如果有人没有选择，那么默认是需求。
	 */
	public void timeOut() {
		log.info("[ROLLTIMEOUT]ROLLID["+id+"]");
		checkEnd(true);
	}

	protected void sendResult(boolean allDiscard, int playerId) {
		Actor p = Server.server.getServiceRegistry().getActorCacheService().find(playerId);
		StringBuilder sb = new StringBuilder(300);
		if (allDiscard) {
			sb.append(MessageFormat.format(peony.Messages.STRING_00036, item.template.name,p.name));
		} else {
			for (RollInfo info : infos) {
				Actor tp = Server.server.getServiceRegistry().getActorCacheService().find(info.ref.id);
				if (tp != null) {
					if (info.state == 1) {
						sb.append(tp.name);
						sb.append(":");
						sb.append(info.point);
						sb.append(",");
					} else if (info.state == 2) {
						sb.append(MessageFormat.format(peony.Messages.STRING_00037, tp.name));
					}
//					sb.append(tp.name);
				}
			}
			sb.append(MessageFormat.format(peony.Messages.STRING_00038, p.name,item.template.name));
		}
		ChatService chatService = Server.server.getServiceRegistry()
				.getChatService();
		Player tp = (Player) ObjectAccessor.getGameObject(playerId);
		if(tp != null){
			ChatMessage cm = new ChatMessage(ChatOption.PARTY, -1, -1,peony.Messages.STRING_00004,
					tp.id, sb.toString(), null);
			if(tp.party != null){
				cm.sessions = tp.party.getSessions();
			}
			chatService.addChatMessage(cm);
		}
	}
}

class RollInfo implements Comparable<RollInfo> {

	public GameObjectRef ref;
	public int point;
	public int state; // 0 表明没有roll点，1表明roll点了, 2 表明放弃了
	public int time;

	public RollInfo(GameObjectRef ref) {
		this.ref = ref;
	}

	public void setPoint(int point) {
		this.point = point;
		this.state = 1;
		this.time = Time.currTime;
	}

	public int compareTo(RollInfo o) {
		if (point > o.point)
			return 1;
		if (point < o.point)
			return -1;
		if (point == o.point) {
			if (time >= o.time)
				return 1;
			return -1;
		} else {
			throw new IllegalStateException();
		}
	}
}
