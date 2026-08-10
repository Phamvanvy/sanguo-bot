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

public class AttendantBookSkillEffect implements ItemEffect {

	protected int skillGroupId;
	protected int skillLevel;
	public static String scriptName = "ui_attendant_list";
	public String script = "";
	
	public AttendantBookSkillEffect(int skillGroupId, int skillLevel){
		this.skillGroupId = skillGroupId;
		this.skillLevel = skillLevel;
	}
	
	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player) target;
		script = this.skillGroupId+" "+this.skillLevel+" "+item.template.id;
		Packet pt = new Packet(OpCode.OPENUI_SERVER);
		pt.putString(scriptName);
		pt.putString(script);
		p.send(pt);
	}

	public int getSkillGroupId() {
		return skillGroupId;
	}
	public int getSkillLevel() {
		return skillLevel;
	}
	
	public boolean needRemove() {
		return false;
	}

}
