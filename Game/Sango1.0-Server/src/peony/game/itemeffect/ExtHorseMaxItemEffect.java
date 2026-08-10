package peony.game.itemeffect;

import java.text.MessageFormat;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.Horse;
import peony.game.ItemEffect;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Skills;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.changed.ChangedItem;
import peony.game.chat.ChatService;
import peony.game.nation.CandidateService;
import peony.service.ServiceEvent;

public class ExtHorseMaxItemEffect implements ItemEffect {
	
	private static final Logger log = Logger.getLogger(ExtHorseMaxItemEffect.class);

	/**
	 * 使用该物品后坐骑栏的总数，而不是增量
	 */
	protected int maxCount;
	public ExtHorseMaxItemEffect(int count) {
		this.maxCount = count;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		Player p = (Player) source;
		if (source.type == GameObject.TYPE_PLAYER) {
			if(p.horseBag.maxSize >= maxCount){
				throw new UseItemException(MessageFormat.format(peony.Messages.STRING_01096, maxCount));
			} 
//			else if(p.horseBag.maxSize < 10){
//				throw new UseItemException(peony.Messages.STRING_01097);
//			}
			p.horseBag.maxSize++;
			p.addIntPropertyChangedItem(ChangedItem.MAX_HORSE_CNT,p.horseBag.maxSize,false);
			//发私聊
			ChatService cs = Server.server.getServiceRegistry().getChatService();
			cs.sendPrivateMessage(p.id, 
					MessageFormat.format(peony.Messages.STRING_00157, 
							p.horseBag.maxSize,(maxCount - p.horseBag.maxSize)));
		} else {
			throw new UseItemException(peony.Messages.STRING_00985);
		}
	}

	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
