package peony.game.nation;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.util.StringUtil;

public class PunishCall extends ClientSessionAsyncCall {

	/**
	 * 国家罚款 serial int 目标的名字 string 原因 string 罚款数量 int
	 */

	private static final Logger log = Logger.getLogger(PunishCall.class);
	int serial;
	String targetName;
	String cause;
	int money;
	int flag;

	public PunishCall(Packet packet, ClientSession session) {
		super(session);
		serial = packet.getInt();
		targetName = packet.getString();
		cause = packet.getString();
		money = packet.getInt();
		this.flag = packet.getInt();
	}

	public void callFinish() throws Exception {
		if (success) {
			Packet pt = new Packet(OpCode.NATION_PUNISH_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.NATION_PUNISH_CLIENT, errorMessage);
		}
	}

	public void run() {
		Player p = (Player) session.getClient();
		if (p != null) {
			Nation nation = Server.server.getServiceRegistry()
			.getNationService().getNationByFaction(p.faction);
			if(flag==1 && nation.getKingId()!=p.id){
				error(null, peony.Messages.STRING_01524);
				addToClientSession();
				return;
			}
			if(flag==0 && (nation.getKingId()==p.id || nation.getOfficerByPlayerId(p.id)==null)){
				error(null, peony.Messages.STRING_01525);
				addToClientSession();
				return;
			}
			if (cause.length() == 0) {
				error(null, peony.Messages.STRING_01526);
				addToClientSession();
				return;
			}
			synchronized (nation) {
				Officer officer = nation.getOfficerByPlayerId(p.id);
				if (officer == null) {
					error(null, peony.Messages.STRING_01527);
					addToClientSession();
					return;
				}
				if(officer.level==Officer.LEVEL2 || officer.level==Officer.LEVEL3){
					error(null, peony.Messages.STRING_01528);
					addToClientSession();
					return;
				}
				if (officer.getMaxPunishMoney() == 0) {
					error(null, peony.Messages.STRING_01529);
					addToClientSession();
					return;
				}
				if (officer.getPunishTimes(Time.day) >= officer
						.getMaxPunishTimes()) {
					error(null, peony.Messages.STRING_01530);
					addToClientSession();
					return;
				}
				Actor actor = Server.server.getServiceRegistry()
						.getActorCacheService().find(targetName);
				if (actor == null) {
					error(null, peony.Messages.STRING_01531);
					addToClientSession();
					return;
				}
				if (actor.faction != p.faction) {
					error(null, peony.Messages.STRING_01532);
					addToClientSession();
					return;
				}
				Officer targetOfficer = nation.getOfficerByPlayerId(actor.id);
				if (targetOfficer != null) {
					error(null, peony.Messages.STRING_01533);
					addToClientSession();
					return;
				}
				Player target = ObjectAccessor.getPlayer(actor.id);
				if (target == null) {
					error(null, peony.Messages.STRING_01534);
					addToClientSession();
					return;
				}
				if (money <= 0){
					error(null, peony.Messages.STRING_01535);
					addToClientSession();
					return;
				}
				if (money > officer.getMaxPunishMoney()) {
					error(null, MessageFormat.format(peony.Messages.STRING_01536, officer.getMaxPunishMoney()));
					addToClientSession();
					return;
				}
				cause = StringUtil.filterBadWords(cause);
				PlayerTransaction tx = target.newTransaction("NPU");
				try {
					log.info("[PUNISH]"+LogUtil.getPlayerLogString(p)+"TARGET["+target.id
							+"]MONEY["+money+"]BALANCE["+target.money+"]TRY");
					target.decMoney(money, tx, true);
					tx.commit();
					Server.server.getServiceRegistry().getNationService()
							.punish(officer, actor.id, money);
					nation.addMoney(money);
					Server.server.getServiceRegistry().getChatService()
							.sendFactionSystemMessage(
									target.faction,peony.Messages.STRING_01537,
									MessageFormat.format(peony.Messages.STRING_01538, target.name,cause,officer.getName()));
					Player targetPlayer = ObjectAccessor.getPlayer(actor.id);
					if(targetPlayer!=null){
						targetPlayer.message(-1, MessageFormat.format(peony.Messages.STRING_01539, officer.getName(),p.name,money), -1, -1);
					}
					Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
					log.info("[PUNISHSUCCESS]"+LogUtil.getPlayerLogString(p)+"TARGET["+target.id
							+"]MONEY["+money+"]BALANCE["+target.money+"]");
					addToClientSession();
				} catch (NoEnoughValueException ex) {
					tx.rollback();
					log.info("[PUNISHFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+target.id
							+"]MONEY["+money+"]BALANCE["+target.money+"]");
					error(null, peony.Messages.STRING_01540);
					addToClientSession();
					return;
				}
			}
		}
	}

}
