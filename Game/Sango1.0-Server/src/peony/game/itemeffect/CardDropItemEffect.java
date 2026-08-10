package peony.game.itemeffect;

import java.util.Random;
import peony.game.Creature;
import peony.game.Gain;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.mail.MailService;

/**
 * 对怪物使用物品触发怪物的卡片掉落
 * @author dchen
 */
public class CardDropItemEffect implements ItemEffect {

	public static Random random = new Random();
	protected int ratio;
	protected int activePower;
	
	public CardDropItemEffect(int ratio, int activePower) {
		this.ratio = ratio;
		this.activePower = activePower;
	}

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player player = (Player)source;
		if(player!=null && target!=null && target instanceof Creature){
			try {//使用卡片消耗行动力
				player.decActivePower(activePower);
			} catch (NoEnoughValueException e0) {
				throw new UseItemException(peony.Messages.STRING_01736);
			}
			Creature creature = (Creature)target;
			int cardId = creature.template.cardId;
			int cardBlockRatio = creature.template.cardRatio;
			
			//实际生效概率=物品卡片掉率-怪物卡片阻率
			int effectRatio = ratio-cardBlockRatio>0 ? ratio-cardBlockRatio : 0;
			
			int ranNum = random.nextInt(100);
			if(ranNum<effectRatio){
				PlayerTransaction tx1 = player.newTransaction("CARDDROP");
				try {
					Gain gain = new Gain(player);
					GameItem item1 = ObjectAccessor.createGameItem(cardId);
					gain.addGainItem(item1, 1);
					try {
						player.addGainComplete(gain, tx1, true);
						tx1.commit();
					} catch (NoEnoughSpaceException e) {
						tx1.rollback();
						player.message(-1, peony.Messages.STRING_01737, -1, -1);
						sendMail(player, item1, 1);
					}
				} catch (Exception e1) {
					tx1.rollback();
				}
			}
		}
	}
	
	protected void sendMail(Player player, GameItem item, int count){
		if(player!=null){
			MailService mailService = Server.server.getServiceRegistry().getMailService();
			mailService.sendSystemMail(player.id, peony.Messages.STRING_00004, peony.Messages.STRING_01738, peony.Messages.STRING_01739, 
					0, item, count, "GETCARD");
		}
	}
	
	public boolean needRemove() {
		return false;
	}

}
