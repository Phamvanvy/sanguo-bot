package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.service.tong.Tong;
import peony.service.tong.TongSkill1;

public class AddMpItemEffect implements ItemEffect {

	protected int value;
	
	public AddMpItemEffect(int value){
		this.value = value;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("錯誤的目標");
		if(target.mp==target.maxmp)
			throw new UseItemException("當前的已經處于滿精力狀態");
		//特殊处理军团科技提升法力
		if(target instanceof Player){
			Tong tong = Server.server.getServiceRegistry().getTongService().getPlayerTong(target.id);
			if(tong!=null && tong.skills.get(1)!=null){
				TongSkill1 skill1 = (TongSkill1) tong.skills.get(1);
				int ratio = skill1.getRatio();
				if(ratio>0){
					target.setMp(Math.min(target.mp+(value*ratio/100), target.maxmp), true);
				}
			}
		}
		target.setMp(Math.min(target.mp+value, target.maxmp), true);
	}

	public boolean isAsync(){
		return false;
	}
}
