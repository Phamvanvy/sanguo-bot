package peony.service.activity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import peony.common.SyncIbuyCall;
import peony.game.ChatOption;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.chat.ChatMessage;
import peony.game.chat.ItemChatAttachment;
import peony.game.drop.DropGroupUtil;
import peony.game.drop.GroupDrop;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ranking.RankingService;

public class VowIbuyCall extends SyncIbuyCall {

	protected VowActivity activity;
	protected Player player;
	protected int serial;
	protected int limit;
	protected List<GameItem> gainitems = new ArrayList<GameItem>();
	//祈福获得的物品数量
	protected List<Integer> gainitemCount=new ArrayList<Integer>();
	
	public VowIbuyCall(ClientSession session, VowActivity activity, Player player, int serial, int limit) {
		super(session, null);
		this.activity = activity;
		this.player = player;
		this.serial = serial;
		this.limit = limit;
	}

	public void callFinish() throws Exception {
		if(success){
			processGain();
			Packet pt = new Packet(OpCode.VOW_SERVER);
			pt.putInt(serial);
			pt.put(gainitems.size());
			for(int i=0;i<gainitems.size();i++){
				GameItem item=gainitems.get(i);
				pt.putInt(item.template.id);
				pt.put((byte)item.template.showImage);
				pt.putShort(((item.template.showType<<2|item.template.bindType)&0xFFFF));
				pt.put(item.template.quality);
				pt.put(gainitemCount.get(i));
			}
			player.send(pt);
		}else{
//			ErrorHandler.sendErrorMessage(session, 0, OpCode.VOW_CLIENT, errorMessage);
		}
	}

	public void run() {
		synchronized (player.ref()) {
			try {
				int shopId = Server.server.getServiceRegistry().getShopService().getShopByItemId(activity.prayIMoneyItem).id;
				if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_CMCC))
					waitBuy(player, 0, shopId, activity.prayIMoneyItem, limit*activity.prayIMoneyItemCount, this);
				else
					waitBuy(player, 0, shopId, activity.prayIMoneyItem, limit, this);			
			} catch (Exception e) {
			}
			addToClientSession();
		}
	}
	
	public void processGain(){
		GroupDrop drop = DropGroupUtil.getGroupDrop(activity.getImoneyItem());
		Gain[] gains = new Gain[limit];
		
		for(int i=0; i<gains.length; i++){
			gains[i] = new Gain(player);
			drop.calc(VowActivity.random, gains[i]);
		}
		Player p = ObjectAccessor.getPlayer(player.id);
		for(Gain _gain : gains){
			GainItem[] gainItems = _gain.getGainItems();
			if(p!=null){
				for(GainItem gainItem : gainItems){
					if(gainItem==null)
						continue;
					GameItem item = gainItem.getItem();
					PlayerTransaction tx = p.newTransaction("VOWPRAY");
					try {
						if(item!=null){
							p.bag.addGameItemComplete(item, gainItem.getCount(), tx, true);
							tx.commit();
						}
					} catch (Exception e) {
						tx.rollback();
						int count = gainItem.getCount();
						Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(player.id, 
								peony.Messages.STRING_00004, peony.Messages.STRING_00820, "", 0, item, count, "VOWPRAY");
					}
					if(item!=null){
						gainitems.add(item);
						gainitemCount.add(gainItem.getCount());
					}
					Integer value = activity.noticeItems.get(item.template.id);
					if(value != null){
						int count = value;
						if(count > 0 && gainItem.getCount() >= count){
							//世界喊话
							ItemChatAttachment attItem = new ItemChatAttachment(item);
							String message = MessageFormat.format("{0}向许愿树祈福得到了{1}个/-1!", player.name, gainItem.getCount());
							ChatMessage cm = new ChatMessage(ChatOption.WORLD, player.id, -1, peony.Messages.STRING_00004, message, attItem);
							Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
						}
					}
				}
			}else{
				for(GainItem gainItem : gainItems){
					if(gainItem==null)
						continue;
					GameItem item = gainItem.getItem();
					int count = gainItem.getCount();
					Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(player.id, 
							peony.Messages.STRING_00004, peony.Messages.STRING_00820, "", 0, item, count, "VOWPRAY");
				}
			}
		}
		if(p!=null){
			p.message(-1, peony.Messages.STRING_00821, -1, -1);
		}
		RankingService rankingService = Server.server.getServiceRegistry().getRankingService();
		if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_CMCC))
			rankingService.processPrayRanking(player,limit*activity.prayIMoneyItemCount,activity.prayIMoneyItem);
		else
			rankingService.processPrayRanking(player,limit,activity.prayIMoneyItem);
		rankingService.prayCountReward(player);//祈福满次数送奖励
	}

}
