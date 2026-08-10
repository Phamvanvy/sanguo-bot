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
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_SKILL_STUDY_CLIENT, "不是军团都督，不能升级此技能");
					return;
				}else{
					TongSkill skill = tong.skills.get(id);
					if (skill == null) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.TONG_SKILL_STUDY_CLIENT, "没有此项军团科技");
						return;
					}
					if (skill.level >= level) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.TONG_SKILL_STUDY_CLIENT, "此项军团科技已经升级过了");
						return;
					}
					if ((skill.level+1)!=level){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.TONG_SKILL_STUDY_CLIENT, MessageFormat.format("需要将军团科技先升级到{0}级", (level -1)));
						return;
					}
					if (level > skill.maxLevel){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.TONG_SKILL_STUDY_CLIENT, MessageFormat.format("{0}只能升级到{1}级", skill.name,skill.maxLevel));
						return;
					}
					if (skill.upgradeDay == Time.day){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.TONG_SKILL_STUDY_CLIENT, "每个军团科技每天只能升级一次");
						return;
					}
					if(tong.money<skill.getUpgradeMoney(level)){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.TONG_SKILL_STUDY_CLIENT, "尊敬的都督，您的军团资金不足，可发布公告召集军团民众捐献");
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
						ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_SKILL_STUDY_CLIENT, "军团金库中金额不足，快速速发动帮众捐献");
					}
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_SKILL_STUDY_CLIENT, "没有所属的军团");
			}
		}
	}

}
