package peony.depot;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;

public class ExtendDepotEffect implements ItemEffect {

	protected int count;
	
	public ExtendDepotEffect(int count){
		this.count = count;
	}
	
	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)source;
		if(p != null && p.depot.getGrids().size() == 0){
			try {
				DepotService depotService = Server.server.getServiceRegistry().getDepotService();
				depotService.extendTurnOn(p, count+15);
			} catch (DepotException e) {
				throw new UseItemException(e.getMessage());
			}
			return;
		}
			
		if(p.depot.getAddedSize()>=count)
			throw new UseItemException(peony.Messages.STRING_00994);
		p.depot.extendDepot(count, true);
	}

	public boolean needRemove() {
		return false;
	}

}
