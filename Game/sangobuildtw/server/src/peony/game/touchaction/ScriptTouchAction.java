package peony.game.touchaction;

import peony.game.Creature;
import peony.game.OpCode;
import peony.game.Player;
import peony.net.Packet;

/**
 * 带参数的TouchAction，在定义npc的TouchAction时除了指定Action对应的Class还可以带一个字符串参数，
 * 这个参数将会在发送OPENUI_SERVER时附带
 * 
 * @author Jeffrey
 * 
 */
public abstract class ScriptTouchAction implements TouchAction{
	
	abstract public String getScript(Player player,Creature npc);
	abstract public String getArgument(Player player,Creature npc);
	
	public void touch(Player player, Creature npc) {
		Packet pt = new Packet(OpCode.OPENUI_SERVER);
		pt.putString(getScript(player,npc));
		pt.putString(getArgument(player,npc));
		player.send(pt);
	}
}
