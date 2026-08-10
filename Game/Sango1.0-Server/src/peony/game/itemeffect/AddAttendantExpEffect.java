package peony.game.itemeffect;

import java.text.MessageFormat;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.attendant.AttendantFixService;
import peony.game.changed.ChangedItem;
import peony.net.Packet;

public class AddAttendantExpEffect implements ItemEffect{
	
	protected int attExp;
	
	public AddAttendantExpEffect(int attExp){
		this.attExp = attExp;
	}

	public boolean isAsync() {
		return false;
	}

	public boolean needRemove() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		Player p = (Player)source;
		if(p!=null){
			if(attExp>0){
				int oldValue = p.pool.getInt(AttendantFixService.PROPERTY_ATTENDANTEXP,0);
				if(oldValue+attExp>0){
					p.pool.setInt(AttendantFixService.PROPERTY_ATTENDANTEXP, oldValue+attExp);
					p.addIntPropertyChangedItem(ChangedItem.ATTENDANT_LEVEL,oldValue+attExp,false,true);
				}
				LogUtil.logAddAttendantExp(p, oldValue, oldValue+attExp, "USEITEM");
			}
		}
	}
	
	public void bulkUseItem(Player p,int count){
		if(p!=null){
			if(attExp>0&&count>0){
				int oldValue = p.pool.getInt(AttendantFixService.PROPERTY_ATTENDANTEXP,0);
				if(oldValue+attExp*count>0){
					p.pool.setInt(AttendantFixService.PROPERTY_ATTENDANTEXP, oldValue+attExp*count);
					p.addIntPropertyChangedItem(ChangedItem.ATTENDANT_LEVEL,oldValue+attExp*count,false,true);
					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, MessageFormat.format("恭喜您获得了{0}点随从经验，快去升级您的随从吧", attExp*count));
				}
				LogUtil.logAddAttendantExp(p, oldValue, oldValue+attExp*count, "USEITEM");
			}
		}
	}

}
