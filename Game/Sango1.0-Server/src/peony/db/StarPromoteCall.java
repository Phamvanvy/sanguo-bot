package peony.db;

import java.text.MessageFormat;
import peony.common.ClientSessionAsyncCall;
import peony.game.ChatOption;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.attendant.Attendant;
import peony.game.chat.ChatMessage;
import peony.game.chat.ItemChatAttachment;
import peony.game.itemenhance.ItemEnhance;
import peony.game.itemenhance.NaturalEnhance;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.VIP.VipPrivilegeService;
import peony.service.fiveelement.FiveElementService;

public class StarPromoteCall extends ClientSessionAsyncCall{
	
	protected int serial;
	protected byte type;
	protected int itemId;
	protected int instanceId;
	Player player = null;
	GameItem item = null;
	protected int succRate;
	

	public static int SUCC_TOPRATE = 10;
	public static int SUCC_BASERATE = 5;
	protected static int CREDIT_NORMAL = 1000; //单次升星需要战功
	protected static int CREDIT_ONETIME = 30000;//一键升星需要战功

	public StarPromoteCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		type = packet.get();
		itemId = packet.getInt();
		instanceId = packet.getInt();
		player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
	   if(success){
		   Packet pt = new Packet(OpCode.STAR_PROMOTE_SERVER);
		   pt.putInt(serial);
		   pt.putInt(succRate);
		   pt.putInt(player.getCredit());
		   player.send(pt);
		   Object[] os = ItemUtil.findPlayerEquipment(player, itemId,instanceId);
		   Object owner = os[1];
			if (owner instanceof Player) {
				player.refreshProperties(false);
			} else if (owner instanceof Horse) {
				Horse h = (Horse) owner;
				h.refreshProperties(false, player);
				if (h == player.horse) {
					player.refreshProperties(false);
				}
			} else if(owner instanceof Attendant){
				((Attendant) owner).refreshProperties(false);
			}
			player.refreshStarState();
	   }else {
		   ErrorHandler.sendErrorMessage(session, serial, OpCode.STAR_PROMOTE_CLIENT, errorMessage);
	   }
	}

	public void run() {
		if(player!=null){
			Object[] os = ItemUtil.findPlayerEquipment(player, itemId,instanceId);
			if (os != null) {
				GameItem item = (GameItem) os[0];
				if (!item.template.isEquipment()) {
					return;
				}
				if (!item.template.equipment.canJudgeStar) {
					ErrorHandler.sendErrorMessage(session, serial,
								OpCode.STAR_PROMOTE_CLIENT, peony.Messages.STRING_00193);
					return;
				}
				if(item.object!=null && item.object instanceof ItemEnhance){
					ItemEnhance ie = (ItemEnhance)item.object;
					int star = ie.getStar();
					if(star >= 10){
					     ErrorHandler.sendErrorMessage(session, serial, 
									OpCode.STAR_PROMOTE_CLIENT, "此件装备已经升级到10级，请勿重复升级");
						return;
					}
					if(star < 9){
						 ErrorHandler.sendErrorMessage(session, serial, 
									OpCode.STAR_PROMOTE_CLIENT, "此件装备不为9星，无法进行星级提升");
						 return;
					}
					try {
						processStarPromote(player, type, item,ie);
					} catch (Exception e) {
						ErrorHandler.sendErrorMessage(session, serial, OpCode.STAR_PROMOTE_CLIENT, e.getMessage());
					}
				}
			}	
		}
		addToClientSession();
	}
	
	public static String getProperty(int instanceId){
		return "PROPERTY_STAR_PROMOTE"+String.valueOf(instanceId);
	}
	
	public static int getCredit(byte type){
		if(type == 1){
			return CREDIT_NORMAL;
		}else if(type == 2){
			return CREDIT_ONETIME;
		}
		return -1;
	}
	
	public void processStarPromote(Player player,byte type,GameItem item,ItemEnhance ie)throws Exception{
		PlayerTransaction tx = player.newTransaction("STARPROMOTE");
		int decCredit = getCredit(type);
		try {
			if(player.vipLevel>=7){
				float rate = 1-VipPrivilegeService.STARPROMOTE_DECCREDIT_RATIO;
				decCredit *= rate;
			}
			player.decCredit(decCredit, tx, true);
			succRate = player.pool.getInt(getProperty(item.instanceId), SUCC_BASERATE);
			if(type == 1){
				int rndNum = FiveElementService.rnd.nextInt(1000);
				int realRate = succRate*5;
				if(rndNum<realRate){
					succRate = 100;
					sendMessage(player,item,ie);
				}else {
					if(succRate<SUCC_TOPRATE){
						succRate++;
						player.pool.setInt(getProperty(item.instanceId), succRate);
					}
					player.message(-1, "星级提升失败", -1, -1);
				}
			}else if(type == 2){
				succRate = 100;
				sendMessage(player,item,ie);
			}
			tx.commit();
			LogUtil.logStarPromoteEnd(player, item, type, (succRate==100)?"SUCCESS":"FAIL");
		} catch (NoEnoughValueException e) {
			tx.rollback();
			String msg = MessageFormat.format("此次升级消耗战功为{0}，您当前战功不足，无法升级", decCredit);
			throw new Exception(msg);
		}
	}

	
	public void sendMessage(Player player,GameItem item,ItemEnhance ie){
		ie.setStar(10);
		if(ie.getNaturals()!=null){
			for(NaturalEnhance h : ie.getNaturals()){
				h.value = item.getNatureEnhanceAttribute(h.attType
						, h.percent);
				if(h.value==0)
					h.value = 1;
			}
		}
		
		Packet pt = new Packet(OpCode.STAR_ENHANCE_SERVER);
		pt.putInt(serial);
		pt.put(item.toClientBytes());
		player.send(pt);
		
		player.pool.remove(getProperty(item.instanceId));
		String msg1 = MessageFormat.format("恭喜您，成功地把{0}升级到10星", item.template.name);
		player.message(-1, msg1, -1, -1);
		String msg2 = MessageFormat.format("恭喜玩家{0}成功地把/-1升级到10星，一代战神，横空出世。", player.name);
		ItemChatAttachment attItem = new ItemChatAttachment(item);
    	ChatMessage cm = new ChatMessage(ChatOption.WORLD, player.id, -1,peony.Messages.STRING_00004, msg2, attItem);
		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
	}

}
