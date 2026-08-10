package peony.game;

import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;

import peony.game.admin.AdminAccountInfoCall;
import peony.game.admin.AdminAccountStatusCall;
import peony.game.admin.AdminDeletePlayerCall;
import peony.game.admin.AdminGMRequestCall;
import peony.game.admin.AdminGMRequestDeleteCall;
import peony.game.admin.AdminGMRequestSolve;
import peony.game.admin.AdminPlayerInfoCall;
import peony.game.admin.AdminPlayerListCall;
import peony.game.admin.AdminPlayerRelationCall;
import peony.game.admin.AdminRemoveItemCall;
import peony.game.admin.AdminSendMail2Call;
import peony.game.admin.AdminSendMailCall;
import peony.game.admin.AdminService;
import peony.game.admin.AdminWhoCall;
import peony.game.admin.DropTestCall;
import peony.game.admin.FindPlayerByNameCall;
import peony.game.admin.RenewPlayerCall;
import peony.game.bbs.AdminBbsAddCall;
import peony.game.bbs.AdminBbsDeleteCall;
import peony.game.bbs.AdminBbsListCall;
import peony.game.changed.ChangedItem;
import peony.game.chat.ChatMessage;
import peony.game.clientbbs.ClientBbsService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.net.PacketHandler;
import peony.service.account.Account;
import peony.service.accountbinding.AdminAccountBindStatus;
import peony.service.activity.Activity;
import peony.service.activity.ActivitySchedule;
import peony.service.activity.ActivityService;
import peony.service.player.PlayerService;

public class AdminPacketHandler implements PacketHandler{
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(AdminPacketHandler.class);

	
	
	public void disconnected(ClientSession session) {
		Admin admin = (Admin)session.getClient();
		if(admin!=null){
			admin.invalid();
		}
	}

	public void handle(Packet packet, ClientSession session,int diff) throws Exception {
		if (session.getClient() == null && packet.getOpCode() != OpCode.ADMIN_LOGIN_CLIENT) {
			return;
		}
		switch (packet.getOpCode()) {
		case OpCode.ADMIN_LOGIN_CLIENT:
			adminLogin(packet, session);
			break;
		case OpCode.ADMIN_WHO_CLIENT:
			adminWho(packet, session);
			break;
		case OpCode.ADMIN_PLAYER_INFO_CLIENT:
			adminPlayerInfo(packet, session);
			break;
		case OpCode.ADMIN_ACCOUNT_INFO_CLIENT:
			adminAccountInfo(packet, session);
			break;
		case OpCode.ADMIN_PLAYERLIST_CLIENT:
			playerList(packet, session);
			break;
		case OpCode.ADMIN_ACCOUNT_STATUS_CLIENT:
			adminForbid(packet, session);
			break;
		case OpCode.ADMIN_CHAT_FORBID_CLIENT:
			chatForbid(packet, session);
			break;
		case OpCode.ADMIN_MAIL_FORBID_CLIENT:
			mailForbid(packet, session);
			break;
		case OpCode.ADMIN_JOIN_CHATCHANNEL_CLIENT:
			joinChatChannel(packet, session);
			break;
		case OpCode.ADMIN_CHAT_CLIENT:
			chat(packet,session);
			break;
		case OpCode.ADMIN_KICK_CLIENT:
			kick(packet,session);
			break;
		case OpCode.ADMIN_GMREQUEST_LIST_CLIENT:
			gmRequestList(packet,session);
			break;
		case OpCode.ADMIN_GMREQUEST_SOLVE_CLIENT:
			gmRequestSlove(packet,session);
			break;
		case OpCode.ADMIN_GMREQUEST_DELETE_CLIENT:
			gmRequestDelete(packet,session);
			break;
		case OpCode.ADMIN_SENDMAIL_CLIENT:
			sendMail(packet,session);
			break;
		case OpCode.ADMIN_GM_DROP_TEST:
			gmRequestDropTest(packet,session);
			break;
		case OpCode.ADMIN_GM_PLAYER_RELATION:
			gmRequestPlayerRelations(packet, session);
			break;
		case OpCode.ADMIN_EXEC_CLIENT:
			exec(packet,session);
			break;
		case OpCode.ADMIN_EXPRATIO_CLIENT:
			expRatio(packet,session);
			break;
		case OpCode.ADMIN_GOTO_CLIENT:
			gotoMap(packet,session);
			break;
		case OpCode.ADMIN_RELOAD_CLIENT:
			reload(packet,session);
			break;
		case OpCode.ADMIN_ACCOUNTBINDING_STATUS_CLIENT:
			accountBindingStatus(packet,session);
			break;
		case OpCode.ADMIN_BBS_ADD_CLIENT:
			bbsAdd(packet,session);
			break;
		case OpCode.ADMIN_BBS_DELETE_CLIENT:
			bbsDelete(packet,session);
			break;
		case OpCode.ADMIN_BBS_LIST_CLIENT:
			bbsList(packet,session);
			break;
		case OpCode.ADMIN_SENDMAIL2_CLIENT:
			sendMail2(packet,session);
			break;
		case OpCode.ADMIN_LIST_ACTIVITY_CLIENT:
			listActivity(packet, session);
			break;
		case OpCode.ADMIN_ACTIVITY_DETAIL_CLIENT:
			activityDetail(packet, session);
			break;
		case OpCode.ADMIN_NEW_ACTIVITY_CLIENT:
			newActivity(packet, session);
			break;
		case OpCode.ADMIN_ENABLE_ACTIVITY_CLIENT:
			enableActivity(packet, session);
			break;
		case OpCode.ADMIN_DELETE_ACTIVITY_CLIENT:
			deleteActivity(packet, session);
			break;
		case OpCode.ADMIN_REMOVE_ITEM_CLIENT:
			removeItem(packet, session);
			break;
		case OpCode.ADMIN_SAVECLIENTBBS_CLIENT:
			saveBBS(packet,session);
			break;
		case OpCode.ADMIN_DELETEPLAYER_CLIENT:
			deletePlayer(packet, session);
			break;
		case OpCode.ADMIN_PLAYERINFO_CLIENT:
			findPlayerByName(packet, session);
			break;
		case OpCode.ADMIN_RENEWPLAYER_CLIENT:
			renewPlayer(packet, session);
			break;
		}
	}
	
	protected void renewPlayer(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new RenewPlayerCall(packet, session));
	}
	
	protected void findPlayerByName(Packet packet, ClientSession session){
		FindPlayerByNameCall call = new FindPlayerByNameCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	protected void deletePlayer(Packet packet, ClientSession session){
		AdminDeletePlayerCall call = new AdminDeletePlayerCall(session, packet);
		Server.server.getServiceRegistry().getDbService().schedule(call);
	}
	
	/**
	 * 从GM上获得公告
	 * @param packet
	 * @param session
	 */
	protected void saveBBS(Packet packet,ClientSession session){
		ClientBbsService service=Server.server.getServiceRegistry().getClientBbsService();
		service.getClientBbs(packet, session);
	} 
	
	/**
	 * 删除物品。
	 * @param packet
	 * @param session
	 */
	protected void removeItem(Packet packet, ClientSession session) {
		Server.server.getServiceRegistry().getDbService().schedule(new AdminRemoveItemCall(session,packet));
	}
	
	/**
	 * 删除活动。
	 * @param packet
	 * @param session
	 */
	protected void deleteActivity(Packet packet, ClientSession session) {
		ActivityService service = Server.server.getServiceRegistry().getActivityService();
		int serial = packet.getInt();
		String name = packet.getString();
		Activity act = service.getActivityByName(name);
		if (act == null) {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_DELETE_ACTIVITY_CLIENT, "不存在的活動");
			return;
		}
		service.removeActivity(act);
		Packet pt = new Packet(OpCode.ADMIN_DELETE_ACTIVITY_SERVER);
		pt.putInt(serial);
		pt.putInt(act.getId());
		session.send(pt);
	}
	
	/**
	 * 激活/禁止活动。
	 * @param packet
	 * @param session
	 */
	protected void enableActivity(Packet packet, ClientSession session) {
		ActivityService service = Server.server.getServiceRegistry().getActivityService();
		int serial = packet.getInt();
		String name = packet.getString();
		boolean enabled = packet.getByte() == 1;
		Activity act = service.getActivityByName(name);
		if (act == null) {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_ENABLE_ACTIVITY_CLIENT, "不存在的活動");
			return;
		}
		act.setEnabled(enabled);
		service.saveActivity(act);
		Packet pt = new Packet(OpCode.ADMIN_ENABLE_ACTIVITY_SERVER);
		pt.putInt(serial);
		pt.putInt(act.getId());
		pt.put(act.isEnabled() ? 1 : 0);
		session.send(pt);
	}
	
	/**
	 * 创建新活动。
	 * @param packet
	 * @param session
	 */
	protected void newActivity(Packet packet, ClientSession session) {
		ActivityService service = Server.server.getServiceRegistry().getActivityService();
		int serial = packet.getInt();
		String name = packet.getString();
		String title = packet.getString();
		String desc = packet.getString();
		String schedule = packet.getString();
		String implClass = packet.getString();
		String configData = packet.getString();
		boolean visible = packet.get() == 1;
		try {
			if (service.getActivityByName(name) != null) {
				throw new Exception("已經有一個同名的活動了,每個活動必須有一個唯一的內部名稱.");
			}
			Activity act = new Activity();
			act.setName(name);
			act.setTitle(title);
			act.setDescription(desc);
			ActivitySchedule sch = new ActivitySchedule();
			sch.parse(schedule);
			act.setSchedule(sch);
			act.setImplClass(implClass);
			act.setConfigData(configData);
			act.setEnabled(false);
			act.setVisible(visible);
			if (act.getImpl() == null) {
				throw new Exception("錯誤的活動實現類");
			}
			service.addActivity(act);
			
			Packet pt = new Packet(OpCode.ADMIN_NEW_ACTIVITY_SERVER);
			pt.putInt(serial);
			pt.putInt(act.getId());
			session.send(pt);
		} catch (Exception e) {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_ACTIVITY_DETAIL_CLIENT, "創建活動失敗：" + e);
		}
	}
	
	/**
	 * 显示活动详情。
	 * @param packet
	 * @param session
	 */
	protected void activityDetail(Packet packet, ClientSession session) {
		ActivityService service = Server.server.getServiceRegistry().getActivityService();
		int serial = packet.getInt();
		int id = packet.getInt();
		Activity act = service.getActivityByID(id);
		if (act == null) {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_ACTIVITY_DETAIL_CLIENT, "不存在的活動ID");
			return;
		}
		Packet pt = new Packet(OpCode.ADMIN_ACTIVITY_DETAIL_SERVER);
		pt.putInt(serial);
		pt.putInt(act.getId());
		pt.putString(act.getName());
		pt.putString(act.getTitle());
		pt.putString(act.getDescription());
		pt.putString(act.getSchedule().toString());
		pt.putString(act.getImplClass());
		pt.putString(act.getConfigData());
		pt.put(act.isEnabled() ? 1 : 0);
		pt.put(act.isVisible() ? 1 : 0);
		pt.put(act.isActive() ? 1 : 0);
		session.send(pt);
	}
	
	/**
	 * 列出当前系统中所有的活动。
	 * @param packet
	 * @param session
	 */
	protected void listActivity(Packet packet, ClientSession session) {
		ActivityService service = Server.server.getServiceRegistry().getActivityService();
		int serial = packet.getInt();
		Activity[] acts = service.getActivities();
		Packet pt = new Packet(OpCode.ADMIN_LIST_ACTIVITY_SERVER);
		pt.putInt(serial);
		pt.putShort(acts.length);
		for (int i = 0; i < acts.length; i++) {
			pt.putInt(acts[i].getId());
			pt.putString(acts[i].getName());
			pt.putString(acts[i].getTitle());
			pt.put(acts[i].isEnabled() ? 1 : 0);
			pt.put(acts[i].isActive() ? 1 : 0);
		}
		session.send(pt);
	}
	
	protected void sendMail2(Packet packet, ClientSession session){
		 Server.server.getServiceRegistry().getDbService().schedule(new AdminSendMail2Call(session,packet));
	}
	
	protected void bbsList(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminBbsListCall(session,packet));
	}
	
	protected void bbsDelete(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminBbsDeleteCall(session,packet));
	}
	
	protected void bbsAdd(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminBbsAddCall(session,packet));
	}
	
	protected void accountBindingStatus(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminAccountBindStatus(session,packet));
	}
	
	protected void reload(Packet packet, ClientSession session){
		int serial = packet.getInt();
		String param = packet.getString();
		try {
			Server.server.getServiceRegistry().getDataService().reload(param);
			Packet pt = new Packet(OpCode.ADMIN_RELOAD_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} catch (Exception e) {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_RELOAD_CLIENT, "重載服務器信息失敗");
		}
	}
	
	protected void gotoMap(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int playerId = packet.getInt();
		int mapId = packet.getInt();
		int x = packet.getInt();
		int y = packet.getInt();
		Player p = ObjectAccessor.getPlayer(playerId);
		if(p==null){
			ErrorHandler.sendAdminErrorMessage(session, serial,
					OpCode.ADMIN_GOTO_CLIENT, "指定的玩家沒有在線");
			return;
		}
		int[] point = {mapId,x,y};
		if (mapId == -1) {
			point = p.getVMap().getRelivePoint(p.faction);
		}
		try {
			p.goMap(point[0], point[1], point[2]);
			Packet pt = new Packet(OpCode.ADMIN_GOTO_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} catch (VMapException e) {
			ErrorHandler.sendAdminErrorMessage(session, serial,
					OpCode.ADMIN_GOTO_CLIENT, MessageFormat.format("無法傳送{0}", e.getMessage()));
			return;
		}
	}
	
	protected void expRatio(Packet packet, ClientSession session){
		int serial = packet.getInt();
		String s = packet.getString();
		if (s.contains(",")) {
			String[] secs = s.split(",");
			float f = Float.parseFloat(secs[0]);
			Server.server.expRatio = f;
			f = Float.parseFloat(secs[1]);
			Server.server.moneyRatio = f;
		} else {
			float f = Float.parseFloat(s);
			Server.server.expRatio = f;
		}
		log.info("[ADMINEXPRATIO]ADMIN["+((Admin)session.getClient()).name+"]RATIO["+s+"]");
		Packet pt = new Packet(OpCode.ADMIN_EXPRATIO_SERVER);
		pt.putInt(serial);
		session.send(pt);
	}
	
	@SuppressWarnings("unchecked")
	protected void exec(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		String className = packet.getString();
		try {
			Class clazz = Class.forName(className);
			Runnable r = (Runnable) clazz.newInstance();
			r.run();
			Packet pt = new Packet(OpCode.ADMIN_EXEC_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} catch (Exception e) {
			ErrorHandler.sendAdminErrorMessage(session, serial,
					OpCode.ADMIN_EXEC_CLIENT, e.getMessage());
		}
	}
	
	protected void gmRequestPlayerRelations(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminPlayerRelationCall(session,packet));
	}
	
	protected void gmRequestDropTest(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new DropTestCall(session,packet));
	}
	
	protected void sendMail(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminSendMailCall(session,packet));
	}
	
	protected void gmRequestDelete(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminGMRequestDeleteCall(session,packet));
	}
	
	protected void gmRequestSlove(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminGMRequestSolve(session,packet));
	}
	
	protected void gmRequestList(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminGMRequestCall(session,packet));
	}
	
	protected void kick(Packet packet, ClientSession sesion){
		int playerId = packet.getInt();
		long time = packet.getLong();
		if(time==0||time<System.currentTimeMillis()){
			Server.server.getServiceRegistry().getPlayerService().removeMute(playerId);
		}else{
			Server.server.getServiceRegistry().getPlayerService().mute(playerId, time);
		}
	}
	
	protected void chat(Packet packet, ClientSession session) {
		int channel = packet.getInt();
		int targetId = packet.getInt();
		String nativeString = packet.getString();
		String message = packet.getString();
		Admin admin = (Admin) session.getClient();
		if (admin != null) {
			ChatMessage cm = new ChatMessage(channel, -admin.id, -1 , "系統", message, null);
			if (channel == ChatOption.AREA) {
				cm.destId = targetId;
			} else if (channel == ChatOption.NATIVE) {
				cm.destName = nativeString;
			} else if (channel == ChatOption.GUILD) {
				cm.destId = targetId;
			} else if (channel == ChatOption.PRIVATE) {
				cm.destId = targetId;
				cm.setSourceName("GM[" + admin.name + "]");
			}
			Server.server.getServiceRegistry().getChatService().addChatMessage(
					cm);
		}
	}
	
	protected void joinChatChannel(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int channel = packet.getInt();
		int targetId = packet.getInt();
		String nativeString = packet.getString();
		if(channel==ChatOption.FACTION) {
			if ((targetId & 1) != 0) {
				Server.server.getServiceRegistry().getChatService().join(channel, 1, nativeString,session);
			} 
			if ((targetId & 2) != 0) {
				Server.server.getServiceRegistry().getChatService().join(channel, 2, nativeString,session);
			} 
			if ((targetId & 4) != 0) {
				Server.server.getServiceRegistry().getChatService().join(channel, 3, nativeString,session);
			} 
		} else {
			Server.server.getServiceRegistry().getChatService().join(channel, targetId, nativeString,session);
		}
		Packet pt = new Packet(OpCode.ADMIN_JOIN_CHATCHANNEL_SERVER);
		pt.putInt(serial);
		session.send(pt);
	}
	
	protected void mailForbid(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int playerId = packet.getInt();
		long time = packet.getLong();
		if(time==0||time<System.currentTimeMillis()){
			Server.server.getServiceRegistry().getMailService().removeForbid(playerId);
		}else{
			Server.server.getServiceRegistry().getMailService().addForbid(playerId, time);
		}
		Packet pt = new Packet(OpCode.ADMIN_MAIL_FORBID_SERVER);
		pt.putInt(serial);
		session.send(pt);
	}
	
	protected void chatForbid(Packet packet, ClientSession session){
		int serial = packet.getInt();
		int playerId = packet.getInt();
		int flag = packet.getInt();
		long time = packet.getLong();
		Server.server.getServiceRegistry().getChatService().forbid(playerId, flag, time);
		Packet pt = new Packet(OpCode.ADMIN_CHAT_FORBID_SERVER);
		pt.putInt(serial);
		session.send(pt);
	}
	
	protected void adminForbid(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminAccountStatusCall(session,packet));
	}
	
	protected void playerList(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminPlayerListCall(session,packet));
	}
	
	protected void adminAccountInfo(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminAccountInfoCall(session,packet));
	}
	
	protected void adminPlayerInfo(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminPlayerInfoCall(session,packet));
	}
	
	protected void adminWho(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminWhoCall(session,packet));
	}
	
	protected void adminLogin(Packet packet,ClientSession session){
		int serial = packet.getInt();
		String name = packet.getString();
		String password = packet.getString();
		if(session.getClient()==null){
			AdminService service = Server.server.getServiceRegistry().getAdminService();
			if(!service.getPassword().equals(password)){
				ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_LOGIN_CLIENT, "登陸密碼錯誤");
				log.info("[ADMINLOGINFAIL]NAME["+name+"]PASSWORD["+password+"]");
				return;
			}
			Admin oldAdmin = service.getAdmin(name);
			if(oldAdmin!=null&&oldAdmin.session!=null){
				oldAdmin.session.close();
			}
			Admin admin;
			try {
				admin = new Admin(name,password);
				service.addAdmin(admin);
				session.setClient(admin);
				Packet pt = new Packet(OpCode.ADMIN_LOGIN_SERVER);
				pt.putInt(serial);
				admin.send(pt);
				peony.channel.ChannelService cs = Server.server.getServiceRegistry().getChannelService();
				cs.addSessionToChannel("gm", session);
				cs.addSessionToChannel("chat_world", session);
				cs.addSessionToChannel("chat_wei", session);
				cs.addSessionToChannel("chat_shu", session);
				cs.addSessionToChannel("chat_wu", session);
				cs.addSessionToChannel("chat_system", session);
				log.info("[ADMINLOGIN]NAME["+name+"]PASSWORD["+password+"]");
			} catch (Exception e) {
				ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_LOGIN_CLIENT, "沒有可登錄ID");
			}

		}else{
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_LOGIN_CLIENT, "已經登錄");
		}
	}
	
}
