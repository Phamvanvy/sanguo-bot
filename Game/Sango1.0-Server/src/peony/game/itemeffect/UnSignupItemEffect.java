package peony.game.itemeffect;

import java.util.Iterator;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.service.duel.DuelService;

public class UnSignupItemEffect implements ItemEffect {
	
	protected int itemId;
	protected static final int UNSIGNUP_FLAGBATTLE = 495;
	protected static final int UNSIGNUP_FUMA = 4050;
	
	public UnSignupItemEffect(int itemId){
		this.itemId = itemId;
	}

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		Player p = (Player)source;
		if(itemId == UNSIGNUP_FLAGBATTLE){
			if(!Server.server.getServiceRegistry().getFlagBattleFieldVMapManager().removeSignup(p))
				throw new UseItemException(peony.Messages.STRING_00818);
			if(p!=null)
				p.message(-1, peony.Messages.STRING_00819, -1, -1);
		}else if(itemId == UNSIGNUP_FUMA){
			DuelService service = Server.server.getServiceRegistry().getDuelService();
			if(!service.canSignUp()){
				throw new UseItemException("现在不能使用特赦令");
			} 
			if(!service.signUps.contains(p.id)){
				throw new UseItemException("您并未报名参加驸马选举，无需特赦");
			} 
			Iterator<Integer> it = service.signUps.iterator();
			while(it.hasNext()){
				int id = it.next();
				if(p.id==id){
					it.remove();
				}
			}
			if(p!=null)
				p.message(-1, "您的比武招亲排队已经取消，不用参加本次战场了。", -1, -1);
		}
	}
	
	public boolean needRemove() {
		return false;
	}
}
