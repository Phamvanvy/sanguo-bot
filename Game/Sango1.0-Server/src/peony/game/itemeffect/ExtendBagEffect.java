package peony.game.itemeffect;

import peony.game.Action;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.service.VIP.VipPrivilegeService;

/**
 * 扩展背包。
 * @author lighthu
 */
public class ExtendBagEffect implements ItemEffect {
    protected int count;
	
	public ExtendBagEffect(int count) {
	    this.count = count;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)source;
		//VIP背包扩展与扩展符扩展分开
		VipPrivilegeService vipService = Server.server.getServiceRegistry().getVipPrivilegeService();
		int vipCount = vipService.vipAddBagCount(p);
		int cnt = count + vipCount;
		if(p.bag.getAddedSize()>=cnt)
			throw new UseItemException(peony.Messages.STRING_00994);
		p.bag.extend(cnt, true);
		
		// 记录玩家动作
		p.addAction(Action.EXTEND_BAG);
	}
	
	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
