package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.net.Packet;

public class ScriptEffect implements ItemEffect {
	
	protected String args;
	protected String script;

	public ScriptEffect(String script,String args){
		this.script = script;
		this.args = args;
	}

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("錯誤的目標");
		if(!(target instanceof Player))
			throw new UseItemException("錯誤的目標");
		Player p = (Player)source;
		Packet pt = new Packet(OpCode.OPENUI_SERVER);
		pt.putString(script);
		pt.putString(args);
		p.send(pt);
	}

}
