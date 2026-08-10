package peony.game;

import java.io.File;
import java.io.FileInputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;

import peony.alchemy.AdminDecAlchemyExpCall;
import peony.game.actlead.ActLeader;
import peony.game.actlead.ActLeaderException;
import peony.game.actlead.ActLeaderService;
import peony.game.admin.AdminAccountInfoCall;
import peony.game.admin.AdminAccountStatusCall;
import peony.game.admin.AdminChangeVIPLevelCall;
import peony.game.admin.AdminChangeViolationParam;
import peony.game.admin.AdminDeletePlayerCall;
import peony.game.admin.AdminGMRequestByDateCall;
import peony.game.admin.AdminGMRequestCall;
import peony.game.admin.AdminGMRequestDeleteCall;
import peony.game.admin.AdminGMRequestMultiSolve;
import peony.game.admin.AdminGMRequestSolve;
import peony.game.admin.AdminPlayerInfoCall;
import peony.game.admin.AdminPlayerListCall;
import peony.game.admin.AdminPlayerRelationCall;
import peony.game.admin.AdminReloadCall;
import peony.game.admin.AdminRemoveItemCall;
import peony.game.admin.AdminSendMail2Call;
import peony.game.admin.AdminSendMailCall;
import peony.game.admin.AdminSendMessMailCall;
import peony.game.admin.AdminService;
import peony.game.admin.AdminWhoCall;
import peony.game.admin.DropTestCall;
import peony.game.admin.FindPlayerByNameCall;
import peony.game.admin.HotSwap;
import peony.game.admin.RenewPlayerCall;
import peony.game.bbs.AdminBbsAddCall;
import peony.game.bbs.AdminBbsDeleteCall;
import peony.game.bbs.AdminBbsListCall;
import peony.game.chat.ChatMessage;
import peony.game.clientbbs.ClientBbsService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.net.PacketHandler;
import peony.service.accountbinding.AdminAccountBindStatus;
import peony.service.activity.Activity;
import peony.service.activity.ActivitySchedule;
import peony.service.activity.ActivityService;

public class AdminPacketHandler implements PacketHandler{
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(AdminPacketHandler.class);

	
	
	public void disconnected(ClientSession session) {
		Admin admin = (Admin)session.getClient();
		if(admin!=null){
			admin.invalid();
			Server.server.getServiceRegistry().getAdminService().removeAdmin(admin.name);
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
		case OpCode.ADMIN_GMREQUEST_LIST_BYDATE_CLIENT:
			gmRequestListByDate(packet,session);
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
		case OpCode.ADMIN_SENDMESSMAIL_CLIENT:
			sendMessageMail(packet, session);
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
		case OpCode.ADMIN_MODIFY_ACTIVITY_CLIENT:
			modifyActivity(packet, session);
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
		case OpCode.ADMIN_NEWACTLEADER_CLIENT:
			addActLeader(packet, session);
			break;
		case OpCode.ADMIN_DELETEACTLEADER_CLIENT:
			deleteActLeader(packet, session);
			break;
		case OpCode.ADMIN_ACTLEADERLIST_CLIENT:
			actLeaderList(packet, session);
			break;
		case OpCode.ADMIN_MULTIGMREQUEST_SOLVE_CLIENT:
			gmRequestMultiSlove(packet,session);
			break;
		case OpCode.ADMIN_CHANGE_VIOLATIONPARAM_CLIENT:
			gmChangeViolationParam(packet,session);
			break;
		case OpCode.ADMIN_SHOW_VIOLATIONPARAM_CLIENT:
			gmShowViolationParam(packet,session);
			break;
		case OpCode.ADMIN_CHANGE_VIPLEVEL_CLIENT:
			adminChangeViplevel(packet, session);
			break;
		case OpCode.ADMIN_HOTSWAP_CLIENT:
			hotswap(packet, session);
			break;
		case OpCode.ADMIN_DEC_ALCHEMYEXP_CLIENT:
			decAlchemyExp(packet,session);
			break;
		}
	}
	
	public void decAlchemyExp(Packet packet, ClientSession session){
		 Server.server.getServiceRegistry().getDbService().schedule(new AdminDecAlchemyExpCall(session,packet));
	}
	
	public void hotswap(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		String name = packet.getString();
		if(session != null) {
			int index = name.lastIndexOf('.');
			String simpleName = null;
			if(index != -1) {
				simpleName = name.substring(index + 1);
			} else {
				simpleName = name;
			}
			File file = new File("./hotfix/"+simpleName+".class");
			if (!file.exists()) {
				ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_HOTSWAP_CLIENT, "补丁文件不存在。");
				return;
			}
			Instrumentation inst = HotSwap.inst;
			if(inst == null) {
				ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_HOTSWAP_CLIENT, "Instrumentation没有初始化。");
				return;
			}
			try {
				FileInputStream is = new FileInputStream(file);
				byte[] data = new byte[(int)file.length()];
				is.read(data);
				ClassDefinition def =  new ClassDefinition(Class.forName(name), data);
				inst.redefineClasses(def);
				System.out.printf("redefine ok.\n");
				Packet pt = new Packet(OpCode.ADMIN_HOTSWAP_SERVER);
				pt.putInt(serial);
				session.send(pt);
			} catch (Exception e) {
				ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_HOTSWAP_CLIENT, e.toString());
				return;				
			}
		}
	}
	
	/** 修改玩家VIP等级*/
	protected void adminChangeViplevel(Packet packet,ClientSession session){
		 Server.server.getServiceRegistry().getDbService().schedule(new AdminChangeVIPLevelCall(session,packet));
	}
	
	/**GM工具显示校验加速参数列表**/
	protected void gmShowViolationParam(Packet packet,ClientSession session){
		int serial = packet.getInt();
		Packet pt = new Packet(OpCode.ADMIN_SHOW_VIOLATIONPARAM_SERVER);
		pt.putInt(serial);				
		pt.putString("初级时间错误扣分值");
		pt.putString(String.valueOf(Player.TIME_ERROR_SCORE_1));
		pt.put(0);
		pt.putString("中级时间错误扣分值");
		pt.putString(String.valueOf(Player.TIME_ERROR_SCORE_2));
		pt.put(1);
		pt.putString("高级时间错误扣分值");
		pt.putString(String.valueOf(Player.TIME_ERROR_SCORE_3));
		pt.put(2);
		pt.putString("单次MOVE最大允许发送间隔（毫秒）");
		pt.putString(String.valueOf(Player.MAX_MOVE_DISTANCE));
		pt.put(3);
		pt.putString("超过单次MOVE最大允许移动距离扣分值");
		pt.putString(String.valueOf(Player.EXCEED_DISTANCE_SCORE));
		pt.put(4);
		pt.putString("移动速度过快初级错误扣分值");
		pt.putString(String.valueOf(Player.POSITION_ERROR_SCORE1));
		pt.put(5);
		pt.putString("移动速度过快高级错误扣分值");
		pt.putString(String.valueOf(Player.POSITION_ERROR_SCORE2));
		pt.put(6);
		pt.putString("累计移动距离超过设定速度扣分值");
		pt.putString(String.valueOf(Player.TOTAL_CHEAT_ERROR_SCORE));
		pt.put(7);
		pt.putString("MOVE包发送过于频繁扣分值");
		pt.putString(String.valueOf(Player.TOO_MUCH_MOVE_ERROR_SCORE));
		pt.put(8);
		pt.putString("移动速度过快初级错误阈值");
		pt.putString(String.valueOf(Player.MOVE_CHEAT_VALVE1));
		pt.put(9);
		pt.putString("移动速度过快高级错误阈值");
		pt.putString(String.valueOf(Player.MOVE_CHEAT_VALVE2));
		pt.put(10);
		pt.putString("累计移动距离错误阈值");
		pt.putString(String.valueOf(Player.TOTAL_CHEAT_VALVE));
		pt.put(11);
		pt.putString("判断MOVE包发送过快的时间阈值");
		pt.putString(String.valueOf(Player.TOO_MUCH_MOVE_VALVE));
		pt.put(12);
		session.send(pt);
	}
	
	/**GM工具控制校验加速参数**/
	protected void gmChangeViolationParam(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminChangeViolationParam(session,packet));
	}
	
	/**批量标注"GM请求"已被处理**/ 
	protected void gmRequestMultiSlove(Packet packet,ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminGMRequestMultiSolve(session,packet));
	}
	
	/** 活动指引项列表 */
	protected void actLeaderList(Packet packet, ClientSession session){
		int serial = packet.getInt();
		ActLeaderService service = Server.server.getServiceRegistry().getActLeaderService();
		List<ActLeader> list = service.getAllActLeader();
		Packet pt = new Packet(OpCode.ADMIN_ACTLEADERLIST_SERVER);
		pt.putInt(serial);
		pt.putInt(list.size());
		for(ActLeader al : list){
			pt.putString(al.name);
			pt.putString(al.type);
			pt.putString(al.createTime);
			pt.putString(al.mapName);
			pt.putInt(al.mapId);
			pt.putInt(al.x);
			pt.putInt(al.y);
			pt.putString(al.rewardType);
			pt.putInt(al.minLevel);
			pt.putInt(al.maxLevel);
			pt.putString(al.faction);
			pt.putString(al.instruction);
		}
		session.send(pt);
	}
	
	/** 用GM删除指定活动指引项 */
	protected void deleteActLeader(Packet packet, ClientSession session){
		int serial = packet.getInt();
		String name = packet.getString();
		ActLeaderService service = Server.server.getServiceRegistry().getActLeaderService();
		try {
			service.removeAct(name);
			Packet pt = new Packet(OpCode.ADMIN_DELETEACTLEADER_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} catch (ActLeaderException e) {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_DELETEACTLEADER_CLIENT, e.getMessage());
		}
	}
	
	/** 用GM工具添加新的活动指引项 */
	protected void addActLeader(Packet packet, ClientSession session){
		int serial = packet.getInt();
		String name = packet.getString();
		String type = packet.getString();
		String createTime = packet.getString();
		String mapName = packet.getString();
		int mapId = packet.getInt();
		int x = packet.getInt();
		int y = packet.getInt();
		String rewardType = packet.getString();
		int minLevel = packet.getInt();
		int maxLevel = packet.getInt();
		String faction = packet.getString();
		String instruction = packet.getString();
		ActLeaderService service = Server.server.getServiceRegistry().getActLeaderService();
		try {
			if(mapId<=0 || x<=0 || y<=0 || minLevel<=0 || minLevel>=100 || maxLevel>100 || maxLevel<=0){
				ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_NEWACTLEADER_CLIENT, peony.Messages.STRING_01933);
				return;
			}
			if(mapName==null || mapName.equals("") || mapName.equals("-")){
				mapName = VMapUtil.getDefinition(mapId).mapInfo.name;
			}
			service.addAct(type, name, createTime, mapName, mapId, x, y, rewardType, minLevel, maxLevel, instruction, faction);
			Packet pt = new Packet(OpCode.ADMIN_NEWACTLEADER_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} catch (Exception e) {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_NEWACTLEADER_CLIENT, e.getMessage());
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
	 * 从GM上保存公告
	 * @param packet
	 * @param session
	 */
	protected void saveBBS(Packet packet,ClientSession session){
		ClientBbsService service=Server.server.getServiceRegistry().getClientBbsService();
		service.saveClientBbs(packet, session);
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
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_DELETE_ACTIVITY_CLIENT, peony.Messages.STRING_01934);
			return;
		}
		service.removeActivity(act);
		Packet pt = new Packet(OpCode.ADMIN_DELETE_ACTIVITY_SERVER);
		pt.putInt(serial);
		pt.putInt(act.getId());
		session.send(pt);
	}

	/**
	 * 修改活动参数。
	 * @param packet
	 * @param session
	 */
	protected void modifyActivity(Packet packet, ClientSession session) {
		ActivityService service = Server.server.getServiceRegistry().getActivityService();
		int serial = packet.getInt();
		String name = packet.getString();
		String title = packet.getString();
		String description = packet.getString();
		String schedule = packet.getString();
		boolean visible = packet.getByte() == 1;
		Activity act = service.getActivityByName(name);
		if (act == null) {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_MODIFY_ACTIVITY_CLIENT, peony.Messages.STRING_01934);
			return;
		}
		
		act.setTitle(title);
		act.setDescription(description);
		ActivitySchedule sch = new ActivitySchedule();
		try {
			sch.parse(schedule);
		} catch (Exception e) {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_MODIFY_ACTIVITY_CLIENT, peony.Messages.STRING_01933);
			return;
		}
		act.setSchedule(sch);
		act.setVisible(visible);
		service.saveActivity(act);
		
		Packet pt = new Packet(OpCode.ADMIN_MODIFY_ACTIVITY_SERVER);
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
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_ENABLE_ACTIVITY_CLIENT, peony.Messages.STRING_01934);
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
				throw new Exception(peony.Messages.STRING_01935);
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
				throw new Exception(peony.Messages.STRING_01936);
			}
			service.addActivity(act);
			
			Packet pt = new Packet(OpCode.ADMIN_NEW_ACTIVITY_SERVER);
			pt.putInt(serial);
			pt.putInt(act.getId());
			session.send(pt);
		} catch (Exception e) {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_ACTIVITY_DETAIL_CLIENT, 
				MessageFormat.format(peony.Messages.STRING_01937, e.toString()));
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
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_ACTIVITY_DETAIL_CLIENT, peony.Messages.STRING_01938);
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
		Server.server.getServiceRegistry().getDbService().schedule(new AdminReloadCall(packet, session));
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
					OpCode.ADMIN_GOTO_CLIENT, peony.Messages.STRING_01939);
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
					OpCode.ADMIN_GOTO_CLIENT, MessageFormat.format(peony.Messages.STRING_01940, e.getMessage()));
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
	
	protected void sendMessageMail(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminSendMessMailCall(session, packet));
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
	
	protected void gmRequestListByDate(Packet packet, ClientSession session){
		Server.server.getServiceRegistry().getDbService().schedule(new AdminGMRequestByDateCall(session,packet));
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
		int serial = 0;
		try {
			serial = packet.getInt();
		} catch (Exception e) {
		}
		Admin admin = (Admin) session.getClient();
		if (admin != null) {
			ChatMessage cm = new ChatMessage(channel, -admin.id, -1 , peony.Messages.STRING_00004, message, null);
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
			Packet pt = new Packet(OpCode.ADMIN_CHAT_SERVER);
			pt.putInt(serial);
			session.send(pt);
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
				ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_LOGIN_CLIENT, peony.Messages.STRING_01941);
				log.info("[ADMINLOGINFAIL]NAME["+name+"]PASSWORD["+password+"]");
				return;
			}
			Admin oldAdmin = service.getAdmin(name);
			if(oldAdmin!=null&&oldAdmin.session!=null){
				oldAdmin.invalid();
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
				ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_LOGIN_CLIENT, peony.Messages.STRING_01942);
			}

		}else{
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_LOGIN_CLIENT, peony.Messages.STRING_01943);
		}
	}
	
}
