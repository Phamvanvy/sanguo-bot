package peony.game.nation;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class OfficerCall extends ClientSessionAsyncCall {

	private static final Logger log = Logger.getLogger(OfficerCall.class);
	int serial;
	String targetName;
	int level;
	String slogan;
	Player p;

	public OfficerCall(Packet packet, ClientSession session) {
		super(session);
		serial = packet.getInt();
		targetName = packet.getString();
		level = packet.getInt();
		this.slogan = packet.getString();
		this.p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.NATION_OFFICER_SERVER);
			pt.putInt(serial);
			if(p!=null)
				p.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_OFFICER_CLIENT, errorMessage);
		}
	}

	public void run() {
		Player p = (Player) session.getClient();
		if (p != null) {
			if (!Officer.checkLevel(level))
				return;
			Nation nation = Server.server.getServiceRegistry()
					.getNationService().getNationByFaction(p.faction);
			synchronized (nation) {
				log.info("[OFFICER]"+LogUtil.getPlayerLogString(p)+"LEVEL["+level+"]TARGET["+targetName+"]TRY");
				Officer officer = nation.getOfficerByPlayerId(p.id);
				if (officer == null || officer.level != Officer.KING) {
					log.info("[OFFICERFAILED]"+LogUtil.getPlayerLogString(p)+"LEVEL["+level+"]TARGET["+targetName+"]");
					error(null, "Không có quyền hạn bổ nhiệm quan viên");
					addToClientSession();
					return;
				}
				Actor actor = Server.server.getServiceRegistry()
						.getActorCacheService().find(targetName);
				if (actor == null) {
					log.info("[OFFICERFAILED]"+LogUtil.getPlayerLogString(p)+"LEVEL["+level+"]TARGET["+targetName+"]");
					error(null, "Người sử dụng đã rời mạng");
					addToClientSession();
					return;
				}
				if (slogan.length() == 0) {
					error(null, "册封失败，未输入册封理由");
					addToClientSession();
					return;
				}
				if (actor.faction != p.faction) {
					log.info("[OFFICERFAILED]"+LogUtil.getPlayerLogString(p)+"LEVEL["+level+"]TARGET["+targetName+"]");
					error(null, "不能任命不同阵营的角色");
					addToClientSession();
					return;
				}
				if (actor.level < 50) {
					log.info("[OFFICERFAILED]"+LogUtil.getPlayerLogString(p)+"LEVEL["+level+"]TARGET["+targetName+"]");
					error(null, "Không thể bổ nhiệm nhân vật cấp dưới 50");
					addToClientSession();
					return;
				}
				Officer targetOfficer = nation.getOfficerByPlayerId(actor.id);
				if (targetOfficer != null) {
					log.info("[OFFICERFAILED]"+LogUtil.getPlayerLogString(p)+"LEVEL["+level+"]TARGET["+targetName+"]");
					error(null, "Người chơi này đã bị bổ nhiệm");
					addToClientSession();
					return;
				}
				targetOfficer = nation.getOfficer(level);
				if (targetOfficer != null) {
					Server.server.getServiceRegistry().getDbService().officerDAO.makeTransient(targetOfficer);
				}
				targetOfficer = new Officer(actor.id, level, actor.faction,actor);
				Server.server.getServiceRegistry().getDbService().officerDAO.newEntity(targetOfficer);
				nation.addOfficer(targetOfficer);
				// Server.server.getServiceRegistry().getNationService()
				// .addOfficer(targetOfficer);
				// XX(玩家名称)XXX(任命公告)，被任命为XX（职位名称）
				Server.server.getServiceRegistry().getChatService()
						.sendFactionSystemMessage(
								actor.faction,
								MessageFormat.format("{0}vì{1}, bị bổ nhiệm thành {2}", actor.name,slogan,targetOfficer.getName()));
//				Server.server.getServiceRegistry().getChatService()
//				.sendFactionSystemMessage(
//						actor.faction,slogan);
				log.info("[OFFICERSUCCESS]"+LogUtil.getPlayerLogString(p)+"LEVEL["+level+"]TARGET["+targetName+"]");
				addToClientSession();
			}
		}
	}

}
