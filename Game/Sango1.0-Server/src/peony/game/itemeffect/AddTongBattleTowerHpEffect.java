package peony.game.itemeffect;

import peony.game.Creature;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.VMap;
import peony.game.changed.ChangedItem;
import peony.service.tong.Tong;
import peony.service.tong.TongService;
import peony.service.tong.battle.TongBattleFieldInstance;
import peony.service.tong.battle.TongBattleSideDef;

public class AddTongBattleTowerHpEffect implements ItemEffect {

	private static int TOWEER1 = 3477507; // ¼ýËþID
	private static int TOWEER2 = 3477508; // ¼ýËþID
	
	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		if (!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if (!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player) target;
		if (p != null) {
			if (p.map.map.instance != null
					&& p.map.map.instance instanceof TongBattleFieldInstance 
					&& ((TongBattleFieldInstance)p.map.map.instance).state==TongBattleFieldInstance.STATE_STARTED) {
				VMap map = p.map.map;
				TongBattleFieldInstance instance = (TongBattleFieldInstance)p.map.map.instance;
				TongService tongService = Server.server.getServiceRegistry().getTongService();
				Tong tong = tongService.getPlayerTong(p.id,false);
				if(!tong.getChairmanName().equals(p.name)){
					throw new UseItemException(peony.Messages.STRING_00678);
				}
				int type = instance.getSide(tong).def.type;
				if(type!=TongBattleSideDef.TYPE_DEFEND){
					throw new UseItemException(peony.Messages.STRING_00679);
				}
				Creature tower1 = map.getCreatureById(TOWEER1);
				Creature tower2 = map.getCreatureById(TOWEER2);
				if((tower1==null && tower2==null)){
					throw new UseItemException(peony.Messages.STRING_00680);
				}else if((tower1!=null && !tower1.isAlive()) && (tower2!=null && !tower2.isAlive())){
					throw new UseItemException(peony.Messages.STRING_00680);
				}
				if(tower1!=null && tower1.isAlive()){
					tower1.setHp(tower1.maxhp, true);
					tower1.addIntPropertyChangedItem(ChangedItem.HP, tower1.maxhp, true);
				}
				if(tower2!=null && tower2.isAlive()){
					tower2.setHp(tower2.maxhp, true);
				}
			}else {
				throw new UseItemException(peony.Messages.STRING_00681);
			}
		}
	}
	
	public boolean needRemove() {
		return false;
	}

}
