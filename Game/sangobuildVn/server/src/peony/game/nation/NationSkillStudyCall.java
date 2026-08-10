package peony.game.nation;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;

public class NationSkillStudyCall extends ClientSessionAsyncCall {
	
	private static final Logger log = Logger.getLogger(NationSkillStudyCall.class);

	int serial;
	int id,level;
	
	public NationSkillStudyCall(ClientSession session,Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.id = packet.getInt();
		this.level = packet.getInt();
	}
	
	public void callFinish() throws Exception {

	}

	public void run() {
		Player p = (Player) session.getClient();
		if (p != null) {
			NationService nationService = Server.server.getServiceRegistry()
					.getNationService();
			if (!nationService.isKing(p)) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATION_SKILL_STUDY_CLIENT, "只有国公才能升级国家科技");
				return;
			}
			Nation nation = nationService.getNationByFaction(p.faction);
			synchronized (nation) {
				NationSkill skill = nation.skills.get(id);
				if (skill == null) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATION_SKILL_STUDY_CLIENT, "没有此项国家科技");
					return;
				}
				if (skill.level >= level) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATION_SKILL_STUDY_CLIENT, "此项国家科技已经升级过了");
					return;
				}
				if ((skill.level+1)!=level){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATION_SKILL_STUDY_CLIENT, MessageFormat.format("需要将国家科技先升级到{0}级", (level -1)));
					return;
				}
				if (level > skill.maxLevel){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATION_SKILL_STUDY_CLIENT, MessageFormat.format("{0}只能升级到{1}级", skill.name,skill.maxLevel));
					return;
				}
				if (skill.upgradeDay == Time.day){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATION_SKILL_STUDY_CLIENT, "每个国家科技每天只能升级一次");
					return;
				}
				if(nation.money<skill.getUpgradeMoney(level)){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATION_SKILL_STUDY_CLIENT, "国库金额不足，快速速发动民众捐献");
					return;
				}
				nation.decMoney(skill.getUpgradeMoney(level));
				skill.level += 1;
				skill.upgradeDay = Time.day;
				skill.maintainDay = Time.day;
				Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
				Packet pt = new Packet(OpCode.NATION_SKILL_STUDY_SERVER);
				pt.putInt(serial);
				session.send(pt);
				Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(p.faction, 
						MessageFormat.format("为抵御外敌，国公{0}消耗国库{1}钱升级科技{2}到{3}级", p.name,skill.getUpgradeMoney(level),skill.name,skill.level));
				log.info("[NATIONSKILLUPGRADE]"+LogUtil.getPlayerLogString(p)+"FACTION["+nation.faction+"]SKILL["+skill.id+"]LEVEL["+skill.level+"]");
			}
		}
	}

}
