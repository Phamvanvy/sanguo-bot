package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.VMapException;
import peony.game.asyncbattle.AsyncBattleService;
import peony.game.battlefield.FlagBattleFieldInstance;
import peony.service.feast.FeastInstance;
import peony.service.pluginstance.MayDayFestivalService;

/**
 * 物品使用传送效果。
 * @author lighthu
 */
public class TeleportEffect implements ItemEffect {
    protected int[] weiLocation;
    protected int[] shuLocation;
    protected int[] wuLocation;
	
	public TeleportEffect(int[] w, int[] s, int[] wu) {
	    weiLocation = w;
	    shuLocation = s;
	    wuLocation = wu;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)source;
		if(p.getVMap().instance instanceof FlagBattleFieldInstance){
			throw new UseItemException(peony.Messages.STRING_01279);
		}
		
		if(p.getVMap().instance instanceof FeastInstance || p.getVMap().getId()==AsyncBattleService.battleMap){
			throw new UseItemException(peony.Messages.STRING_01279);
		}
		if(!MayDayFestivalService.canEnter){
			throw new UseItemException("活动已关闭");
		}
		
		MayDayFestivalService mdService = Server.server.getServiceRegistry().getMayDayFestivalService();
		if(mdService!=null){
			if(item.template.id == MayDayFestivalService.MAY_ENTERITEM && !mdService.checkMayDayEntertime(p)){
				throw new UseItemException("您今日已经用完了5次进入劳动者的仙园的资格，请明天再来。");
			}
			if(item.template.id == 2760 && p.getVMap().getId() == MayDayFestivalService.MAYDAY_MAP){
				throw new UseItemException("您已在目标场景，不需要再消耗一张通行证");
			}
		}
		if(p.map.id == MayDayFestivalService.MAYDAY_MAP){
			mdService.initState(p);
		}
		int[] t;
	
		if (p.faction == GameObject.FACTION_WEI) {
		    t = weiLocation;
		} else if (p.faction == GameObject.FACTION_SHU) {
		    t = shuLocation;
		} else {
		    t = wuLocation;
		}
		try {
			((Player)source).goMap(t[0], t[1], t[2]);
		} catch (VMapException e) {
			throw new UseItemException(e.getMessage());
		}
	}
	
	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
