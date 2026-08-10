package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.VMapException;
import peony.game.battlefield.FlagBattleFieldInstance;
import peony.service.friend.PlayerRelation;

/**
 * 夫妻传送效果。
 * @author lighthu
 */
public class MarriageTeleportEffect implements ItemEffect {
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("錯誤的目標");
		if(!(target instanceof Player))
			throw new UseItemException("錯誤的目標");
		Player p = (Player)source;
		if(p.getVMap().instance instanceof FlagBattleFieldInstance){
			throw new UseItemException("此地不能使用此道具");
		}
		PlayerRelation rel = Server.server.getServiceRegistry().getRelationService().get(p.id);
		if(rel.mateId!=-1){
			Player mate = ObjectAccessor.getPlayer(rel.mateId);
			if(mate!=null&&mate.getVMap()!=null&&mate.getVMap().instance==null){
				try {
					p.goMap(mate.map.id, mate.x, mate.y);
				} catch (VMapException e) {
					throw new UseItemException(e.getMessage());
				}
			}else{
				throw new  UseItemException("不能傳送");
			}
		}else{
			throw new UseItemException("婚后才能使用此物品");
		}
	}
	
	public boolean isAsync(){
		return false;
	}
}
