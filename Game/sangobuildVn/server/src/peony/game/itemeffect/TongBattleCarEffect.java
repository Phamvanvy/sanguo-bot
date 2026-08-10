package peony.game.itemeffect;

import java.text.MessageFormat;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.Creature;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.VMapUtil;
import peony.service.tong.Tong;
import peony.service.tong.TongService;
import peony.service.tong.TongSkill2;
import peony.service.tong.apply.TongBattleApply;
import peony.service.tong.apply.TongBattleApplyService;
import peony.service.tong.battle.TongBattleFieldInstance;
import peony.service.tong.battle.TongBattleSide;
import peony.service.tong.battle.TongBattleSideDef;

public class TongBattleCarEffect implements ItemEffect {

	private static int CARID1 = 3477521; // 攻城车ID
	private static int CARID2 = 3477522; // 攻城车ID
	public static int MAXUSETIME = 1; // 最多使用次数

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		if (!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if (!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player p = (Player) target;
		if (p != null) {
			if (p.map.map.instance != null
					&& p.map.map.instance instanceof TongBattleFieldInstance 
					&& ((TongBattleFieldInstance)p.map.map.instance).state==TongBattleFieldInstance.STATE_STARTED) {
				TongService tongService = Server.server.getServiceRegistry().getTongService();
				Tong tong = tongService.getPlayerTong(p.id);
				TongSkill2 skill = (TongSkill2) tong.skills.get(2);
				if (skill != null && skill.level > 0) {
					TongBattleApplyService applyService = Server.server.getServiceRegistry().getTongBattleApplyService();
					TongBattleApply apply = applyService.getApplyByTongId(tong.id);
					if(apply.useAccount>=MAXUSETIME){
						throw new UseItemException(MessageFormat.format("最多使用{0}次", MAXUSETIME));
					}
					ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
					TongBattleFieldInstance ins = (TongBattleFieldInstance)p.map.map.instance;
					int type = ins.getSide(tong).def.type;
					int x = 300;
					int y = 300;
					if(type==TongBattleSideDef.TYPE_ATTACK1){
						x = 136;
						y = 596;
						GameMapObject gmo = GameMapObject.findByID(proj, CARID1);
						GameObject npc = VMapUtil.addCreature(p.map.map, x, y,
								(GameMapNPC) gmo, true, 0, Server.server.revision);
						TongBattleSide side = ((TongBattleFieldInstance)p.map.map.instance).getSide(tong);
						((Creature)npc).faction = side.faction | (side.minorFaction << 5);
						((Creature)npc).minorFaction = side.minorFaction;
					}else if(type==TongBattleSideDef.TYPE_ATTACK2){
						x = 825;
						y = 139;
						GameMapObject gmo = GameMapObject.findByID(proj, CARID2);
						GameObject npc = VMapUtil.addCreature(p.map.map, x, y,
								(GameMapNPC) gmo, true, 0, Server.server.revision);
						TongBattleSide side = ((TongBattleFieldInstance)p.map.map.instance).getSide(tong);
						((Creature)npc).faction = side.faction | (side.minorFaction << 5);
						((Creature)npc).minorFaction = side.minorFaction;
					}
					apply.useAccount++;
				} else {
					throw new UseItemException("使用攻城车必须军团秘录中的攻城车建造达到1级");
				}
			}else{
				throw new UseItemException("当前不能使用");
			}
		}
	}

}
