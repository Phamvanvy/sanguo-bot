package peony.game.itemeffect;

import java.text.MessageFormat;

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
import peony.game.asyncbattle.AsyncBattleService;
import peony.game.battlefield.FlagBattleFieldInstance;
import peony.game.instance.WomenDayInstanceService;
import peony.service.CycleInstanceMapManager;
import peony.service.feast.FeastInstanceService;
import peony.service.friend.PlayerRelation;
import peony.service.pluginstance.ChessInstanceService;
import peony.service.pluginstance.MayDayFestivalService;

/**
 * 夫妻传送效果。
 * @author lighthu
 */
public class MarriageTeleportEffect implements ItemEffect {
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)source;
		if(p.getVMap().instance instanceof FlagBattleFieldInstance || p.getVMap().getId()==AsyncBattleService.battleMap){
			throw new UseItemException(peony.Messages.STRING_01279);
		}
		PlayerRelation rel = Server.server.getServiceRegistry().getRelationService().get(p.id);
		if(rel.mateId!=-1){
			Player mate = ObjectAccessor.getPlayer(rel.mateId);
			if(mate==null){//不在线
				throw new  UseItemException(peony.Messages.STRING_01742);
			}
			if(mate!=null&&mate.getVMap()!=null&&mate.getVMap().instance==null&&mate.getVMap().getId()!=CycleInstanceMapManager.mapId.get(new Integer(mate.clazz))){
				try {
					if(p.map.id == MayDayFestivalService.MAYDAY_MAP){//特殊处理五一活动
						MayDayFestivalService mdService = Server.server.getServiceRegistry().getMayDayFestivalService();
						mdService.initState(p);
					}
					if(mate.map.id == MayDayFestivalService.MAYDAY_MAP || mate.map.id == FeastInstanceService.MAPID 
							|| mate.map.id == ChessInstanceService.MAPID|| mate.map.id == WomenDayInstanceService.MAPID[0] 
							|| mate.map.id==AsyncBattleService.battleMap){
						throw new UseItemException("您的配偶正在特殊场景，无法进行传送，请稍候。");
					}
					if(p.map.id == ChessInstanceService.MAPID){
						throw new UseItemException("您正在特殊场景，无法进行传送，请稍候。");
					}
					if(mate.isInStep){
						throw new UseItemException("您的配偶正在跨服战场，无法进行传送，请稍候。");
					}
					p.goMap(mate.map.id, mate.x, mate.y);
				} catch (VMapException e) {
					throw new UseItemException(e.getMessage());
				}
			}else{
				String sexy="他";
				if(p!=null&&p.sex==1){
					sexy="她";
				}
				throw new  UseItemException(MessageFormat.format("你的配偶现在正在副本中忙碌，无法将您传到{0}的身边。", sexy));
			}
		}else{
			throw new UseItemException(peony.Messages.STRING_01743);
		}
	}
	
	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
