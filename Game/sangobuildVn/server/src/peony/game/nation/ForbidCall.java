package peony.game.nation;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.util.StringUtil;

public class ForbidCall extends ClientSessionAsyncCall {
	
	private static final Logger log = Logger.getLogger(ForbidCall.class);
	int serial;
	String targetName;
	String cause;
	int flag;
	
	public ForbidCall(Packet packet, ClientSession session){
		super(session);
		serial = packet.getInt();
		targetName = packet.getString();
		cause = packet.getString();
		this.flag = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.NATION_FORBID_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.NATION_FORBID_CLINET, errorMessage);
		}
	}

	public void run() {
		Player p = (Player)session.getClient();
		NationService nationService = Server.server.getServiceRegistry().getNationService();
		if(p!=null){
			log.info("[FORBID]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]TRY");
			if(flag==1){
				if(nationService.getNationByFaction(p.faction).getKingId()!=p.id){
					log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
					error(null, "你不是一国之君，没有权利使用此项功能!\nNgươi không phải là vua một nước, không có quyền lợi sử dụng chức năng này!");
					addToClientSession();
					return;
				}
			}else if(flag==0){
				if(nationService.getNationByFaction(p.faction).getOfficerByPlayerId(p.id)==null
						 || nationService.getNationByFaction(p.faction).getKingId()==p.id){
					log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
					error(null, "你不是该国大臣，没有权利使用此项功能！\nBạn không phải là quốc vương nước này, không có quyền hạn sử dụng công năng này!");
					addToClientSession();
					return;
				}
			}
			if(cause.length()==0){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"没有理由，不能禁言");
				addToClientSession();
				return;
			}
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(p.faction);
			Officer officer = nation.getOfficerByPlayerId(p.id);
			if(officer==null){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"你不是官员不能禁言");
				addToClientSession();
				return;
			}
			if(officer.level==Officer.LEVEL2 || officer.level==Officer.LEVEL4){
				error(null, "Bạn tạm thời không thể sử dụng hạng mục quyền lợi này");
				addToClientSession();
				return;
			}
			if(officer.getMaxForbidTimes()==0){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"你的官员等级不能禁言");
				addToClientSession();
				return;
			}
			if(officer.getForbidTimes()>=officer.getMaxForbidTimes()){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"禁言次数已用完");
				addToClientSession();
				return;
			}
			Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(targetName);
			if(actor==null){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"未找到指定角色");
				addToClientSession();
				return;
			}
			if(actor.faction!=p.faction){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"不能对其他国家的角色禁言");
				addToClientSession();
				return;
			}
			Officer targetOfficer = nation.getOfficerByPlayerId(actor.id);
			if(targetOfficer!=null){
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(null,"不能对官员禁言");
				addToClientSession();
				return;
			}
			try {
				Server.server.getServiceRegistry().getNationService().forbid(officer, actor.id);
			} catch (NationVoteException e) {
				log.info("[FORBIDFAILED]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
				error(e, e.getMessage());
				addToClientSession();
				return;
			}
			cause = StringUtil.filterBadWords(cause);
			Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(p.faction,"Quan viên"
					, MessageFormat.format("{0}因{1}被禁言", actor.name,cause));
			Player target = ObjectAccessor.getPlayer(actor.id);
			if(target!=null){
				target.message(-1, MessageFormat.format("你已被{0}禁言\nBạn đã bị {0} cấm Ngôn", officer.getName(),p.name), -1, -1);
			}else{
				String forbids = nation.pool.getString(p.faction+"FORBID");
				StringBuffer strb = new StringBuffer(forbids);
				nation.pool.setString(p.faction+"FORBID", strb.append(",")
						.append(((Integer)(actor.id)).toString()).toString());
				Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
			}
			log.info("[FORBIDSUCCESS]"+LogUtil.getPlayerLogString(p)+"TARGET["+targetName+"]");
			addToClientSession();
		}
	}

}
