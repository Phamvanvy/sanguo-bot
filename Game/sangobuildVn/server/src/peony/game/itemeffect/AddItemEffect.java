package peony.game.itemeffect;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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
import peony.game.chat.ItemChatAttachment;
import peony.net.Packet;

public class AddItemEffect implements ItemEffect {

	protected int[] itemIds;
	protected int[] counts;
	
	public AddItemEffect(int[] itemIds,int[] counts){
		this.itemIds = itemIds;
		this.counts = counts;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player p = (Player)target;
		Gain gain = new Gain(p);
		for (int i = 0; i < itemIds.length; i++) {
			GameItem addItem = ObjectAccessor.createGameItem(itemIds[i]);
			gain.addGainItem(addItem, counts[i]);
			
			// 判断是否需要通知
			if (ItemUtil.getNoticeType(itemIds[i]) != 0) {
				tx.addNoticeItem(addItem);
			}
		}
		tx.setCause("ITE");
		try {
			p.addGainComplete(gain, tx, true);
		} catch (NoEnoughSpaceException e) {
			throw new UseItemException("没有足够包格");
		}
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
			int nt = ItemUtil.getNoticeType(gi.template.id);
			
			// 普通系统广播
			ItemChatAttachment attItem = new ItemChatAttachment(gi);
			String message = MessageFormat.format("{0} Từ {1} nhận được /-1 rồi!", p.name, item);
			ChatMessage cm = new ChatMessage(ChatOption.WORLD, -1, -1, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>", message, attItem);
			Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
			
			// 狮子吼
			if (nt == 2) {
				Packet pt = new Packet(OpCode.SHOUT_SERVER);
				message = MessageFormat.format("{0} từ {1} nhận được {2}!", p.name, item, gi.template.name);
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
}
