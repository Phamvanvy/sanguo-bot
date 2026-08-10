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
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.service.tong.TongSkill1;
import peony.service.tong.TongSkill6;
import peony.service.tong.TongSkill7;

public class AddMpItemEffect implements ItemEffect {

	protected int value;
	
	public AddMpItemEffect(int value){
		this.value = value;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(target.mp==target.maxmp)
			throw new UseItemException(peony.Messages.STRING_01543);
		//军团专属科技  灵丹妙药
		int num = 0;
		if(target instanceof Player){
			try{
				TongService ts = Server.server.getServiceRegistry().getTongService();
				TongMember tm = ts.getPlayerInfo(target.id);
				if(tm!=null && tm.skills.get(7)!=null){
					TongSkill7 tskill = (TongSkill7)tm.skills.get(7);
					int radio = tskill.getRatios();
					num = Math.round(radio * value / 100);
				}
			}catch(Exception e){
				throw new UseItemException(e.getMessage());
			}
		}
		target.setMp(Math.min(target.mp+value+num, target.maxmp), true);
	}

	public boolean isAsync(){
		return false;
	}

	public boolean needRemove() {
		return false;
	}
}
