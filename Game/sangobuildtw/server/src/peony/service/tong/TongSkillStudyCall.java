package peony.service.tong;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;

public class TongSkillStudyCall extends ClientSessionAsyncCall {

	int serial;
	int id,level;
	
	public TongSkillStudyCall(ClientSession session, Packet pt){
		super(session);
		this.serial = pt.getInt();
		this.id = pt.getInt();
		this.level = pt.getInt();
	}
	
	public void callFinish() throws Exception {

	}

	public void run() {
		Player p = (Player)session.getClient();
		if(p != null){
			TongService service = Server.server.getServiceRegistry().getTongService();
			Tong tong = service.getPlayerTong(p.id);
			if(tong != null){
				TongMember tm = service.getPlayerInfo(p.id);
				if(tm.duty != TongService.CHAIRMAN){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_SKILL_STUDY_CLIENT, "不是軍團都督,不能升級此技能");
					return;
				}else{
					TongSkill skill = tong.skills.get(id);
					if (skill == null) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.TONG_SKILL_STUDY_CLIENT, "沒有此項軍團科技");
						return;
					}
					if (skill.level >= level) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.TONG_SKILL_STUDY_CLIENT, "此項軍團科技已經升級過了");
						return;
					}
					if ((skill.level+1)!=level){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.TONG_SKILL_STUDY_CLIENT, MessageFormat.format("需要將軍團科技先升級到{0}級", (level -1)));
						return;
					}
					if (level > skill.maxLevel){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.TONG_SKILL_STUDY_CLIENT, MessageFormat.format("{0}只能升級到{1}級", skill.name,skill.maxLevel));
						return;
					}
					if (skill.upgradeDay == Time.day){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.TONG_SKILL_STUDY_CLIENT, "每個軍團科技每天只能升級一次");
						return;
					}
					if(tong.money<skill.getUpgradeMoney(level)){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.TONG_SKILL_STUDY_CLIENT, "尊敬的都督,您的軍團資金不足,可發布公告召集軍團民眾捐獻");
						return;
					}
					try {
						tong.decMoney(skill.getUpgradeMoney(level));
						skill.level += 1;
						skill.upgradeDay = Time.day;
						skill.maintainDay = Time.day;
						Server.server.getServiceRegistry().getDbService().tongDAO
								.updateEntity(tong);
						Packet pt = new Packet(OpCode.TONG_SKILL_STUDY_SERVER);
						pt.putInt(serial);
						session.send(pt);
					} catch (NoEnoughValueException ex) {
						ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_SKILL_STUDY_CLIENT, "軍團金庫中金額不足,快速速發動幫眾捐獻");
					}
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_SKILL_STUDY_CLIENT, "沒有所屬的軍團");
			}
		}
	}

}
