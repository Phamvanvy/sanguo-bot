package peony.game.itemeffect;

import java.text.MessageFormat;
import java.util.List;

import com.pip.sanguo.data.item.Item;

import peony.game.ChatOption;
import peony.game.Gain;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.game.chat.ItemChatAttachment;
import peony.net.Packet;
import peony.service.RewardBagService;

public class AddItemEffect implements ItemEffect {

	protected int[] itemIds;
	protected int[] counts;
	
	protected static int[] specialItems = {4740,4047,1615};  //第一个为礼包物品id,后面为掉落物品id
	protected static int[] specialNoticeItems = {4740,4423,2418};
	
	public AddItemEffect(int[] itemIds,int[] counts){
		this.itemIds = itemIds;
		this.counts = counts;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)target;
		Gain gain = new Gain(p);
		for (int i = 0; i < itemIds.length; i++) {
//			GameItem addItem = ObjectAccessor.createGameItem(itemIds[i]);
			//处理特殊物品掉落
			RewardBagService service = Server.server.getServiceRegistry().getRewardBagService();
			GameItem addItem = null;
			if(service.isSpecialItem(item.template.id,itemIds[i])){
				addItem = service.getGameItem(item.template.id, itemIds[i]);
			}else{
				addItem = ObjectAccessor.createGameItem(itemIds[i]);
			}
			if(addItem.template!=null&&addItem.template.itemValid!=null&&addItem.template.itemValid.time>0){
				//装备本身属性 (时效修改成上级包裹的时效)
				addItem.template.itemValid.time=item.validTime;
				addItem.validTime=(int)(/*System.currentTimeMillis()/60000+*/addItem.template.itemValid.time);
				
			}
			gain.addGainItem(addItem, counts[i]);
			
			// 判断是否需要通知
			if (ItemUtil.getNoticeType(itemIds[i]) != 0 || specialNoticeItem(item,itemIds[i])) {
				tx.addNoticeItem(addItem);
			}
		}
		tx.setCause("ITE");
		try {
			p.addGainComplete(gain, tx, true);
		} catch (NoEnoughSpaceException e) {
			throw new UseItemException(peony.Messages.STRING_01016);
		}
	}

	public void bulkUseItem(Player p, GameItem item, int count, PlayerTransaction tx) throws UseItemException {
		Gain gain = new Gain(p);
		for(int i = 0; i < itemIds.length; i++) {
			//处理特殊物品掉落
			RewardBagService service = Server.server.getServiceRegistry().getRewardBagService();
			GameItem addItem = null;
			if(service.isSpecialItem(item.template.id, itemIds[i])){
				addItem = service.getGameItem(item.template.id, itemIds[i]);
			}else{
				addItem = ObjectAccessor.createGameItem(itemIds[i]);
			}
			if(addItem.template!=null&&addItem.template.itemValid!=null&&addItem.template.itemValid.time>0){
				//装备本身属性 (时效修改成上级包裹的时效)
				addItem.template.itemValid.time=item.validTime;
				addItem.validTime=(int)(/*System.currentTimeMillis()/60000+*/addItem.template.itemValid.time);
				
			}
			gain.addGainItem(addItem, counts[i]*count);
			
			// 判断是否需要通知
			if (ItemUtil.getNoticeType(itemIds[i]) != 0 || specialNoticeItem(item,itemIds[i])) {
				for(int j=0; j<count; j++){
					tx.addNoticeItem(addItem);
				}
			}
		}
		tx.setCause("ITE");
		try {//使用成功，请去卡片界面查看。
			p.addGainComplete(gain, tx, true);
			if(item.template.id>=4894&&item.template.id<=4897&&p!=null){
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, "使用成功，请去卡片界面查看。");
			}
		} catch (NoEnoughSpaceException e) {
			throw new UseItemException(peony.Messages.STRING_01016);
		}
	}
	
	/** 特殊不发公告物品 */
	public static boolean isSpecialItem(String item,int itemId) {
		int itId = specialItems[0];
		GameItem gi = ObjectAccessor.createGameItem(itId);
		if(gi!=null){
			if(item.equals(gi.template.name)){
				for(int i=1;i<specialItems.length;i++){
					if(specialItems[i]==itemId){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean specialNoticeItem(GameItem item,int itemId){
		int itId = specialNoticeItems[0];
		if(itId == item.template.id){
			for(int i=1;i<specialNoticeItems.length;i++){
				if(specialNoticeItems[i]==itemId){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isAsync(){
		return false;
	}
	
	/**
	 * 获得特殊物品后，向全世界发送通知。
	 * @param noticeItems 需要通知的物品
	 * @param p 获得的玩家
	 * @param item 使用的物品名称
	 */
	public static void sendItemNotice(List<GameItem> noticeItems, Player p, String item) {
		for (GameItem gi : noticeItems) {
			if(isSpecialItem(item,gi.template.id))
				continue;
			int nt = ItemUtil.getNoticeType(gi.template.id);
			
			// 普通系统广播
			ItemChatAttachment attItem = new ItemChatAttachment(gi);
			String message = MessageFormat.format(peony.Messages.STRING_01127, p.name, item);
			ChatMessage cm = new ChatMessage(ChatOption.WORLD, p.id, -1, peony.Messages.STRING_00004, message, attItem);
			Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
			
			// 狮子吼
			if (nt == 2) {
				Packet pt = new Packet(OpCode.SHOUT_SERVER);
				message = MessageFormat.format(peony.Messages.STRING_01128, p.name, item, gi.template.name);
				pt.putString(message);
				pt.putInt(0xFF0000);
				pt.putInt(10000);
				pt.put(0);
				for (Player p1 : ObjectAccessor.players.values()) {
					p1.send(pt);
				}
			}
		}
	}
	
	public int[] getItemIds(){
		return this.itemIds;
	}
	
	public int[] getItemCount(){
		return this.counts;
	}

	public boolean needRemove() {
		return false;
	}
}
