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
import peony.service.activity.Activity;
import peony.service.activity.ActivityService;
import peony.service.activity.ValentineActivity;

public class ValentineEffect implements ItemEffect {

	protected int count;
	
	public boolean isAsync() {
		return false;
	}
	
	public ValentineEffect(int count){
		this.count = count;
	}

	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player player = (Player)source;
		if(source.id==target.id)
			throw new UseItemException("不能对自己使用");
		if(player!=null){
			if(target.type!=GameObject.TYPE_PLAYER || target.faction!=source.faction)
				throw new UseItemException(peony.Messages.STRING_00014);
			ActivityService service = Server.server.getServiceRegistry().getActivityService();
			Activity act = service.getActivityByImpClass("ValentineActivity");
			if(act==null){
				for(int i=1;i<5;i++){
					act = service.getActivityByImpClass("ValentineActivity" + i);
					if(act!=null)
						break;
				}
			}
			if(act==null || !act.isActive() || !act.isEnabled() /*|| !act.isVisible()*/)
				throw new UseItemException("活动尚未开始");
			ValentineActivity actImp = (ValentineActivity)act.getImpl();
			Player tar = (Player)target;
			try {
				actImp.sendFlowers(player, tar.id, tar.level, tar.name, tar.faction, count);
			} catch (Exception e) {
				throw new UseItemException(e.getMessage());
			}
			player.message(-1, "送花成功", -1, -1);
		}
	}
	
	public boolean needRemove() {
		return false;
	}

}
