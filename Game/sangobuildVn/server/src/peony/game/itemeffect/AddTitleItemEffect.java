package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Title;
import peony.game.TitleUtil;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.service.ServiceEvent;

public class AddTitleItemEffect implements ItemEffect {

	protected int titleId;
	
	public AddTitleItemEffect(int titleId){
		this.titleId = titleId;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player p = (Player)target;
		Title t = TitleUtil.getTitle(titleId);
		if(t!=null){
			if(!p.addTitle(t)){
				throw new UseItemException("Danh hiệu này đã tồn tại");
			}
		}else{
			throw new UseItemException("Không tìm thấy danh hiệu chỉ định");
		}
		//增加title事件
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_ADD_TITLE,p));

	}

	public boolean isAsync(){
		return false;
	}
}
