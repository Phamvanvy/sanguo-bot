package peony.game.itemeffect;

import java.text.MessageFormat;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;

public class AddCardExpItemEffect implements ItemEffect {

	protected int addExp;
	
	public boolean isAsync() {
		return false;
	}
	
	public AddCardExpItemEffect(int addExp){
		this.addExp = addExp;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if (!ItemUtil.checkUseTarget(source, item, target)) {
			throw new UseItemException(peony.Messages.STRING_00014);
		}
		if(source instanceof Player){
			Player player = (Player)source;
			try {
				player.cards.addExp(addExp);
				if(item.template.id == 2783||item.template.id == 4623){
					 String message = MessageFormat.format("恭喜您获得了{0}点卡片经验。", addExp);
					 Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, message);
				}
			} catch (Exception e) {
				throw new UseItemException(e.getMessage());
			}
		}
	}
	
	/** 批量获得卡片经验 */
	public void bulkUseItem(Player player,int count) throws Exception{
		if(addExp>0){
			try {
			    player.cards.addExp(addExp*count);
			    String message = MessageFormat.format("恭喜您获得了{0}点卡片经验。", addExp*count);
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, message);
			} catch (Exception e) {
				throw new UseItemException(e.getMessage());
			}
		}	
	}

	public boolean needRemove() {
		return false;
	}

}
