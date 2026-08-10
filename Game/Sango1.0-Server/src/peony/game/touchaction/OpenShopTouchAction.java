package peony.game.touchaction;

import com.pip.util.Utils;

import peony.game.Creature;
import peony.game.Player;

/**
 * touchnpc是根据职业打开不同的商店
 * @author pmeng
 *
 */
public class OpenShopTouchAction extends ScriptTouchAction {
	
	protected String script;
	protected String arg;
	
	public OpenShopTouchAction(String script){
		this(script,null);
	}
	
	public OpenShopTouchAction(String script,String arg){
		this.script = script;
		this.arg = arg;
	}

	@Override
	public String getArgument(Player player, Creature npc) {
		if(arg==null)
			return "";
		String[] shopIds = Utils.splitString(arg, ';');
		return shopIds[player.clazz];
	}

	@Override
	public String getScript(Player player, Creature npc) {
		return script;
	}

}
