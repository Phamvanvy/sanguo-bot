package peony.game.attendant;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Skills;
import peony.game.itemeffect.AttendantBookSkillEffect;
import peony.game.skill.Skill;
import peony.net.ClientSession;
import peony.net.Packet;


public class AttendantLearnSkillCall extends ClientSessionAsyncCall{

	int serial;
	int instanceId;
	byte index;
	int itemId;
	Player player;
	int skillGroupId;
	int skillLevel;
	
	public AttendantLearnSkillCall(ClientSession session,Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.instanceId = packet.getInt();
		this.index = packet.get();
		this.itemId = packet.getInt();
		player = (Player)session.getClient();
		
	}
	
	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.ATTENDANT_LEARNSKILL_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_LEARNSKILL_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player != null){
			Attendant attendant = player.attendantBag.getAttendant(instanceId);
			if(attendant != null){
				getSkillIdAndLevel();
				Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(skillGroupId, skillLevel));
				if(attendant.hasSkill(skill.getId())){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_ADDSKILL_CLIENT, "您已经学习过此技能，无需重复学习");
					return;
				}
				PlayerTransaction tx = player.newTransaction("ATTSKILL");
				GameItem item = player.bag.removeGameItemIngoreInstanceId(itemId, 1, tx, false);
				if(item!=null){
					tx.commit();
					attendant.lightSkill(index);
					attendant.addSkill(skill, index);
				}else{
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_ADDSKILL_CLIENT, "没有随从技能书");
					return;
				}
			}else{
				error("没有找到指定随从");
			}
		}
		addToClientSession();
	}
	
	private void getSkillIdAndLevel(){  
		ItemTemplate template = ObjectAccessor.getItemTemplate(itemId);
		if(template != null){
			AttendantBookSkillEffect itemEffect = (AttendantBookSkillEffect)template.useType.effect;
			if(itemEffect != null){
				skillGroupId = itemEffect.getSkillGroupId();
				skillLevel = itemEffect.getSkillLevel();
			}
		}
	}
	
}
