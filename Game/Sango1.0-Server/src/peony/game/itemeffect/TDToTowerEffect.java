package peony.game.itemeffect;

import java.util.List;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.Creature;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.Instance;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.VMapUtil;
import peony.game.buff.BuffUtil;
import peony.service.towerdefend.TDItemToTower;
import peony.service.towerdefend.TowerDefendConfig;
import peony.service.towerdefend.TowerDefendInstance;
import peony.service.towerdefend.TowerDefendService;

public class TDToTowerEffect implements ItemEffect {

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)target;
		if(p!=null){
			if(p.map.map.instance==null || !(p.map.map.instance instanceof TowerDefendInstance))
				throw new UseItemException(peony.Messages.STRING_01279);
			TowerDefendService service = Server.server.getServiceRegistry().getTowerDefendService();
			int towerId = service.getItemToTowerId(item.template.id);
			if(towerId==0)
				throw new UseItemException(peony.Messages.STRING_01280);
			int[] center = service.getNearestCenter(p, service.getItemToTower(item.template.id).type);
			if(center==null)
				throw new UseItemException(peony.Messages.STRING_01279);
			List<Creature> list = p.map.map.getNearestCreatures(p, center[0], center[1], 25);
			for(Creature c : list){
				if(service.isTower(c.id))
					throw new UseItemException(peony.Messages.STRING_01281);
			}
			ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
			GameMapObject gmo = GameMapObject.findByID(proj, towerId);
			GameObject npc0 = VMapUtil.addCreature(p.getVMap(), center[0], center[1], (GameMapNPC) gmo, true, 0, null);
			npc0.faction = p.faction;
			((Creature)npc0).buffs.addBuff(BuffUtil.createBuff(TowerDefendConfig.BUFF, 1, 
					(Creature)npc0, (Creature)npc0, 0));
			if(service.getTowerType(towerId).equals(TDItemToTower.TYPE_ATTACK)){
				Instance instance = p.getVMap().instance;
				if(instance instanceof TowerDefendInstance){
					TowerDefendInstance ins = (TowerDefendInstance)instance;
					int[] npc = new int[5];
					npc[0] = center[2];
					npc[1] = center[0]+30;
					npc[2] = center[1];
					npc[3] = towerId;
					npc[4] = npc0.instanceId;
					ins.npcs.add(npc);
				}
			}
		}
	}
	
	public boolean needRemove() {
		return false;
	}

}
