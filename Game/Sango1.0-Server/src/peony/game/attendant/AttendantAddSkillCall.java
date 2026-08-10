package peony.game.attendant;

import peony.common.SyncIbuyCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Skills;
import peony.game.skill.Skill;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.ShopService;

public class AttendantAddSkillCall extends SyncIbuyCall {

	protected Player player;
	protected int serial;
	protected int skillGroupId;
	protected int skillLevel;
	protected int instanceId;
	protected int index;
	protected int itemId;
	protected int type;
	
	public AttendantAddSkillCall(ClientSession session, Packet packet) {
		super(session, packet);
		player = (Player)session.getClient();
		serial = packet.getInt();
		skillGroupId = packet.getInt();
		skillLevel = packet.getInt();
		instanceId = packet.getInt();
		index = packet.getByte();
		itemId = packet.getInt();
		type = 0; //学习随从技能方式（0为普通学习，1为一键学习）
		try {
			type = packet.getByte();
		} catch (Exception e) {}
	}

	public void callFinish() {
		if(success){
			Packet pt = new Packet(OpCode.ATTENDANT_ADDSKILL_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_ADDSKILL_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player!=null){
			Attendant attendant = player.attendantBag.getAttendant(instanceId);
			ShopService service = Server.server.getServiceRegistry().getShopService();
			if(attendant!=null){
				Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(skillGroupId, skillLevel));
				if(attendant.hasSkill(skill.getId())){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_ADDSKILL_CLIENT, peony.Messages.STRING_00403);
					return;
				}
				if(type==0 && attendant.skillSwitchs[index]==false){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_ADDSKILL_CLIENT, peony.Messages.STRING_00404);
					return;
				}else if(type==1 && attendant.skillSwitchs[index]==false){
					if(!attendant.canLight(index)){
						error(peony.Messages.STRING_00101);
						addToClientSession();
						return;
					}
					PlayerTransaction tx = player.newTransaction("LIGHTSKILL");
					GameItem item = player.bag.removeGameItemIngoreInstanceId(ItemUtil.ATTENDANT_LIGHTSKILL_ITEM, 1, tx, false);
					ItemTemplate item0 = ObjectAccessor.getItemTemplate(ItemUtil.ATTENDANT_LIGHTSKILL_ITEM);
					if(item0==null)
						item0 = ObjectAccessor.createGameItem(ItemUtil.ATTENDANT_LIGHTSKILL_ITEM).template;
					if(item!=null){
						tx.commit();
					}else{
						tx.rollback();
						try {
							waitBuy(player, 0, service.getShopByItemId(ItemUtil.ATTENDANT_LIGHTSKILL_ITEM).id, ItemUtil.ATTENDANT_LIGHTSKILL_ITEM, 1, this);
						} catch (Exception e) {
							error(peony.Messages.STRING_00405);
							addToClientSession();
							return;
						}
					}
				}
				PlayerTransaction tx = player.newTransaction("ATTSKILL");
				GameItem item = player.bag.removeGameItemIngoreInstanceId(itemId, 1, tx, false);
				if(item!=null){
					tx.commit();
					attendant.lightSkill(index);
					attendant.addSkill(skill, index);
				}else{
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_ADDSKILL_CLIENT, peony.Messages.STRING_00406);
					return;
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ATTENDANT_ADDSKILL_CLIENT, peony.Messages.STRING_00099);
				return;
			}
		}
		addToClientSession();
	}

}
