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
				error(null, "你不是一国之君，没有权利使用此项功能!");
				addToClientSession();
				return;
			}
			if(flag==0 && (nation.getKingId()==p.id || nation.getOfficerByPlayerId(p.id)==null)){
				error(null, "你不是该国大臣，没有权利使用此项功能！");
				addToClientSession();
				return;
			}
			if (cause.length() == 0) {
				error(null, "没有理由，不能罚款");
				addToClientSession();
				return;
			}
			synchronized (nation) {
				Officer officer = nation.getOfficerByPlayerId(p.id);
				if (officer == null) {
					error(null, "你还没有罚款的权利");
					addToClientSession();
					return;
				}
				if(officer.level==Officer.LEVEL2 || officer.level==Officer.LEVEL3){
					error(null, "你暂时不能使用该项权利");
					addToClientSession();
					return;
				}
				if (officer.getMaxPunishMoney() == 0) {
					error(null, "你的官员等级不能罚款");
					addToClientSession();
					return;
				}
				if (officer.getPunishTimes(Time.day) >= officer
						.getMaxPunishTimes()) {
					error(null, "罚款次数已经用完");
					addToClientSession();
					return;
				}
				Actor actor = Server.server.getServiceRegistry()
						.getActorCacheService().find(targetName);
				if (actor == null) {
					error(null, "未找到指定角色");
					addToClientSession();
					return;
				}
				if (actor.faction != p.faction) {
					error(null, "对方不是该国国民，罚款失败");
					addToClientSession();
					return;
				}
				Officer targetOfficer = nation.getOfficerByPlayerId(actor.id);
				if (targetOfficer != null) {
					error(null, "不能对官员罚款");
					addToClientSession();
					return;
				}
				Player target = ObjectAccessor.getPlayer(actor.id);
				if (target == null) {
					error(null, "罚款失败，对方不在线");
					addToClientSession();
					return;
				}
				if (money <= 0){
					error(null, "请输入罚款金额");
					addToClientSession();
					return;
				}
				if (money > officer.getMaxPunishMoney()) {
					error(null, MessageFormat.format("罚款失败，罚款上限为{0}", officer.getMaxPunishMoney()));
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
									target.faction,"官员",
									MessageFormat.format("{0}因{1}被{2}罚款,大家引以为鉴", target.name,cause,officer.getName()));
					Player targetPlayer = ObjectAccessor.getPlayer(actor.id);
					if(targetPlayer!=null){
						targetPlayer.message(-1, MessageFormat.format("你已被{0}{1}罚款{2}", officer.getName(),p.name,money), -1, -1);
					}
					Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
					log.info("[PUNISHSUCCESS]"+LogUtil.getPlayerLogString(p)+"TARGET["+target.id
							+"]MONEY["+money+"]BALANCE["+target.money+"]");
					addToClientSession();
				} catch (NoEnoughValueException ex) {
					tx.rollback();
					log.info("[PUNISHFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+target.id
							+"]MONEY["+money+"]BALANCE["+target.money+"]");
					error(null, "罚款失败，对方金钱不足");
					addToClientSession();
					return;
				}
			}
		}
	}

}
