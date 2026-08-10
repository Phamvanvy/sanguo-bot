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

	private static int TOWEER1 = 3477507; // 箭塔ID
	private static int TOWEER2 = 3477508; // 箭塔ID
	
	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		if (!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("錯誤的目標");
		if (!(target instanceof Player))
			throw new UseItemException("錯誤的目標");
		Player p = (Player) target;
		if (p != null) {
			if (p.map.map.instance != null
					&& p.map.map.instance instanceof TongBattleFieldInstance 
					&& ((TongBattleFieldInstance)p.map.map.instance).state==TongBattleFieldInstance.STATE_STARTED) {
				VMap map = p.map.map;
				TongBattleFieldInstance instance = (TongBattleFieldInstance)p.map.map.instance;
				TongService tongService = Server.server.getServiceRegistry().getTongService();
				Tong tong = tongService.getPlayerTong(p.id);
				if(!tong.getChairmanName().equals(p.name)){
					throw new UseItemException("您沒有權利使用");
				}
				int type = instance.getSide(tong).def.type;
				if(type!=TongBattleSideDef.TYPE_DEFEND){
					throw new UseItemException("進攻方不可使用");
				}
				Creature tower1 = map.getCreatureById(TOWEER1);
				Creature tower2 = map.getCreatureById(TOWEER2);
				if((tower1==null && tower2==null)){
					throw new UseItemException("不可使用");
				}else if((tower1!=null && !tower1.isAlive()) && (tower2!=null && !tower2.isAlive())){
					throw new UseItemException("不可使用");
				}
				if(tower1!=null && tower1.isAlive()){
					tower1.setHp(tower1.maxhp, true);
					tower1.addIntPropertyChangedItem(ChangedItem.HP, tower1.maxhp, true);
				}
				if(tower2!=null && tower2.isAlive()){
					tower2.setHp(tower2.maxhp, true);
				}
			}else {
				throw new UseItemException("當前不能使用");
			}
		}
	}

}
