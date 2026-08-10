package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.exp.ExpService;
import peony.net.Packet;

/**
 * 获得坐骑经验。
 * @author lighthu
 */
public class GetHorseExpEffect implements ItemEffect {
    protected float amount;
    protected int[] valueTable;
	
	public GetHorseExpEffect(float amount, int[] valueTable) {
	    this.amount = amount;
	    this.valueTable = valueTable;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)source;
		if (p.horse == null) {
		    throw new UseItemException(peony.Messages.STRING_00527);
		}
		if(p.horse.level >= p.level){
			throw new UseItemException(peony.Messages.STRING_00528);
		}
		if(item.template.id == 797 || item.template.id == 984){ //增加豆包和蛮荒驯兽铃全部使用功能
			ExpService expService = Server.server.getServiceRegistry().getExpService();
			if(item.template.id == 797){
			    expService.doubaohorseexp = valueTable;
			} else if(item.template.id == 984){
				expService.xunshoulinghorseexp = valueTable;
			}
			Packet pt = new Packet(OpCode.OPENUI_SERVER);
			pt.putString("ui_npc_dialog");
			pt.putString("GETHORSEEXP|"+item.template.id+"|"+item.instanceId);
			p.send(pt);
		} else {
			int addValue;
			if (valueTable != null) {
		        addValue = valueTable[p.horse.level - 1];
		    } else if (amount >= 0) {
			    addValue = (int)amount;
			} else {
			    addValue = (int)(-(amount * p.horse.level));
			}
			p.horse.setExp(p.horse.exp + addValue, p, "ITE");
		}
	}
	
	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
