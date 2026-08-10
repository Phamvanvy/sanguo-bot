package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.attendant.Attendant;
import peony.service.stat.StatService;

/**
 * 获取随从 物品使用效果
 * @author dchen
 */
public class AttendantItemEffect implements ItemEffect {

	public int attendantId;

	public AttendantItemEffect(int attendantId){
		this.attendantId = attendantId;
	}
	
	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player player = (Player)source;
		if(player!=null){
			Attendant attendant = null;
			if(item.object != null && item.object instanceof Attendant){
				attendant = (Attendant) item.object;
				attendant.owner = (Player)target;
			}else{
				attendant = new Attendant(attendantId, player);
			}
			attendant.refreshProperties(false);
			if(player.attendantBag.isFull())
				throw new UseItemException(peony.Messages.STRING_01867);
			player.attendantBag.addAttendant(attendant);
			attendant.setHp(attendant.maxhp, false);
			attendant.setMp(attendant.maxmp, false);
			//统计玩家拥有随从成就
			StatService service = Server.server.getServiceRegistry().getStatService();
			service.playerAddAttendant(player);
		}
	}
	
	public boolean isAsync() {
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}

}
