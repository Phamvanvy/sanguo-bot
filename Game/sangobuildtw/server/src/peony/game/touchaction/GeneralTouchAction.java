package peony.game.touchaction;

import peony.game.Creature;
import peony.game.Player;

/**
 * 通用的脚本控制TouchAction，在编辑器中传入脚本名字以及参数
 * @author Jeffrey
 *
 */
public class GeneralTouchAction extends ScriptTouchAction {
	
	protected String script;
	protected String arg;
	
	public GeneralTouchAction(String script){
		this(script,null);
	}
	
	public GeneralTouchAction(String script,String arg){
		this.script = script;
		this.arg = arg;
	}

	@Override
	public String getArgument(Player player, Creature npc) {
		if(arg==null)
			return "";
		return arg;
	}

	@Override
	public String getScript(Player player, Creature npc) {
		return script;
	}

}
