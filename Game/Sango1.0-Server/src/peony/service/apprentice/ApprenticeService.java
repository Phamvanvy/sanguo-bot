package peony.service.apprentice;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import ch.javasoft.util.intcoll.IntHashMap;
import peony.db.PlayerRelationDAO;
import peony.game.Actor;
import peony.game.ChatOption;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationList;
import peony.service.friend.RelationService;

public class ApprenticeService implements Service, ServiceEventListener {

	RelationService relationService = Server.server.getServiceRegistry()
			.getRelationService();
	private PlayerRelationDAO dao = Server.server.getServiceRegistry()
			.getDbService().playerRelationDAO;
	protected static int[] rewardLevel = {54,58,62,66,70};
	protected static int[] teacherRewards = {2686,2687,2688,4452,4453};
	protected static int[] apprenticeReward = {4454,4455,4456,4457,4458};
	protected int[] teacherTimes = { 1, 5, 10 };
	public static int[] teacherReward = { 3687, 3688, 3689 };
	public static String[] titlesName = { peony.Messages.STRING_01029, peony.Messages.STRING_01030, peony.Messages.STRING_01031 };
//	public static int atitle = 3716;
	public static int atitle = 3699;
//	public static int creditBag =3715 ;
	protected AtomicInteger ids = new AtomicInteger(0);
	protected IntHashMap<ApprenticeRequest> id2request = new IntHashMap<ApprenticeRequest>();
	
	
	public ApprenticeRequest newAppRequest(Player player,Player target){
		ApprenticeRequest request = new ApprenticeRequest(ids.incrementAndGet(),Time.currTime,player.ref(),target.ref());
		id2request.put(request.id, request);
		return request;
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);

	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);

	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_PLAYER_LEVELUP, 
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_LEVELUP:
			playerGraduation((Player) event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			processPlayerLoad((Player)event.param1);
			break;
		}
	}
	
	public void processPlayerLoad(Player player){
		Server.server.getServiceRegistry().getDbService().
        schedule(new ApprenticePlayerLoadCall(player==null ? null : player.session, player));
	}
	
	public void playerLoad(Player p){
       if(p!=null){
    	   if(p.level>=70){
    		  PlayerRelation r = relationService.get(p.id);
    		   if(r!=null){
    			  RelationList list = r.apprenticeList;
    			  if(list!=null){
    				  List<Actor> apps = list.players;
    				  Iterator<Actor> it = apps.iterator();
    				  while(it.hasNext()){
    					  Actor a = it.next();
    					  PlayerRelation relation = relationService.get(a.id);
       					  if(relation == null){
       						  relation = dao.findPlayerRelation(a.id);
       					  }
       					  if(relation!=null){
       						  if(relation.teacherId != p.id){
       							  r.removeApprentice(a.id);
       							  dao.updateEntity(r);
       						  }
       					  }
    				   }
    			   }
    		   }
    	   }
       }
	}
	
	/** 请求拜师 */
	public void apprenticeInvite(ClientSession session,Packet packet){
		int serial = packet.getInt();
		Player p = (Player)session.getClient();
		if(p!=null){
		    if(p.level<70){
		    	ErrorHandler.sendErrorMessage(session, serial,
						OpCode.APPRENTICE_INVIT_CLIENT,peony.Messages.STRING_01032 );
		    	return;
		    }
			if (p.party == null) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.APPRENTICE_INVIT_CLIENT,peony.Messages.STRING_01033 );
				return;
			}
			if(p.party.members!=null && p.party.members.size()>2){
				sendPartyChat(p,peony.Messages.STRING_01034);
				return;
			}
			Player targetPlayer = p.party.getPartyFriend(p.id);
			if(targetPlayer != null && checkPartyMember(p,targetPlayer)){
				ApprenticeRequest request = newAppRequest(p, targetPlayer);
				Packet pt = new Packet(OpCode.APPRENTICE_INVIT_SERVER);
				pt.putInt(serial);
				pt.putInt(request.id);
				pt.putInt(p.id);
				pt.putString(p.name);
				pt.put(p.level);
				pt.put(p.clazz);
				pt.put(p.sex);
				targetPlayer.send(pt);
			}
	    } 
	}
	
	/** 玩家拒绝请求 */
	public void apprenticeInvitReject(Packet packet, ClientSession session) {
		int requestId = packet.getInt();
		Player player = (Player) session.getClient();
		if (player != null) {
			ApprenticeRequest request = getAndRemoveRequest(requestId);
			if (request != null) {
				Player source = (Player) ObjectAccessor
						.getGameObject(request.ref);
				if (source != null) {
					ErrorHandler.sendErrorMessage(source.session, -1,
							OpCode.APPRENTICE_INVIT_REJECT_CLIENT,MessageFormat.format(peony.Messages.STRING_01035,
									player.name));
				}
			}
		}
	}
	
	/** 玩家接受请求 */
	public void apprenticeInvitOk(Packet packet, ClientSession session) {
		int requestId = packet.getInt();
		Player player = (Player) session.getClient();
		ApprenticeRequest request = getAndRemoveRequest(requestId);
		if (request != null && (Time.currTime - request.time) < 60000) { // 一分钟之内有效
			Player source = (Player) ObjectAccessor
					.getGameObject(request.ref);
			if (source != null) {
				Player teacher = source;
				Player apprentice = source;
				if(source.level >=70){
					apprentice = player;
				} else {
					teacher = player;
				}
				createTeaAndApp(teacher,apprentice);
			}	
		} else {
			ErrorHandler.sendErrorMessage(session, -1,
					OpCode.PARTY_INVIT_OK_CLIENT, peony.Messages.STRING_01036);
		}
	}

	/**
	 * 建立师徒关系
	 * 
	 * @param session
	 * @param packet
	 */
	public synchronized void createTeaAndApp(Player teacher,Player apprentice) {
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		String msg = MessageFormat.format(peony.Messages.STRING_01037, apprentice.name);
		chatService.sendPrivateMessage(teacher.id, msg);
		msg = MessageFormat.format(peony.Messages.STRING_01038,
				teacher.name);
		chatService.sendPrivateMessage(apprentice.id, msg);
		saveAdd(teacher, apprentice);
		LogUtil.addApprentice(teacher, apprentice);
	}

	/**
	 * 玩家出师
	 * 
	 * @param p
	 */
	public void playerGraduation(Player player) {
		Server.server.getServiceRegistry().getDbService().
        schedule(new PlayerGraduationCall(player==null ? null : player.session, player));
	}
	
	public String getRewardProperty(int level){
		return "PROPERTY_APPRENTICE_REWARDLEVEL"+String.valueOf(level);
	}
	
	public int getRewardIndex(Player player){
		for(int i=rewardLevel.length-1;i>=0;i--){
			if(i==rewardLevel.length-1){
				if(player.level>=rewardLevel[i]){
//					player.pool.setInt(getRewardProperty(rewardLevel[i]), 1);
					return i;
				}
			}else{
				if(player.level>= rewardLevel[i] && player.level<rewardLevel[i+1]){
//					player.pool.setInt(getRewardProperty(rewardLevel[i]), 1);
					return i;
				}
			}
		}
		return -1;
		
	}
	
	public void removeProperty(Player player){
		for(int i=0;i<rewardLevel.length;i++){
			player.pool.remove(getRewardProperty(rewardLevel[i]));
		}
	}

	/**
	 * 解除师徒关系是保存信息
	 * 
	 * @param apprentice   徒弟
	 * @param teacher      师傅
	 * @throws Exception
	 */
	public synchronized void saveRemove(int apprenticeId, int teacherId) throws Exception {
		if (apprenticeId != -1 && teacherId != -1) {
			PlayerRelation entity;
			if (relationService.get(apprenticeId) == null) {
				entity = dao.findPlayerRelation(apprenticeId);
				entity.removeTeacher();
				dao.updateEntity(entity);
			} else {
				entity = Server.server.getServiceRegistry()
						.getRelationService().get(apprenticeId);
				entity.removeTeacher();
				dao.updateEntity(entity);
			}
			if (relationService.get(teacherId) != null) {
				if (relationService.get(teacherId).apprenticeList
						.exists(apprenticeId)) {
//					relationService.get(teacherId).removeApprentice(
//							apprenticeId);
					entity = relationService.get(teacherId);
					entity.removeApprentice(apprenticeId);
					dao.updateEntity(entity);
				}
			} else {
				entity = dao.findPlayerRelation(teacherId);
				if(entity.apprenticeList.exists(apprenticeId)){
					entity.removeApprentice(apprenticeId);
					dao.updateEntity(entity);
				}
			}
		}
	}

	/**
	 * 增加师徒关系时保存
	 * 
	 * @param apprentice
	 * @param teacher
	 */
	public void saveAdd(Player teacher, Player apprentice) {
		if (apprentice != null && teacher != null) {
			PlayerRelation entity;
			if (relationService.get(apprentice.id) == null) {
				entity = dao.findPlayerRelation(apprentice.id);
				entity.addTeacher(teacher.id);
				dao.updateEntity(entity);
			} else {
				entity = Server.server.getServiceRegistry()
						.getRelationService().get(apprentice.id);
				entity.addTeacher(teacher.id);
				if(entity.apprenticeList == null){
					entity.apprenticeList = new RelationList();
				}
				dao.updateEntity(entity);
			}
			PlayerRelation relation = relationService.get(teacher.id);
			if (relation != null) {
				if(relation.apprenticeList == null){
					relation.apprenticeList = new RelationList();
				}
				if (!relation.apprenticeList
						.exists(apprentice.id)) {
					Actor actor = Server.server.getServiceRegistry()
							.getActorCacheService().find(apprentice.id);
					relationService.get(teacher.id).addApprentice(teacher,
							actor);
				}
			}
		}
	}

	public Player getTeacherByApp(Player p) {
		Player teacher = null;
		if (p != null) {
			int teacherId = p.getTeacherId();
			if (teacherId != -1) {
				teacher = ObjectAccessor.getPlayer(teacherId);
				if (teacher == null) {
					teacher = Server.server.getServiceRegistry().getDbService().playerDAO
							.getPlayerById(teacherId);
				}
			}
		}
		return teacher;
	}

	public int getIndex(int times) {
		for (int i = 0; i < teacherTimes.length; i++) {
			if (times == teacherTimes[i])
				return i;
		}
		return -1;
	}

	public void sendPartyChat(Player p, String msg) {
		ChatMessage cm = new ChatMessage(ChatOption.PARTY, -1, -1, peony.Messages.STRING_00004, p.id,
				msg, null);
		cm.sessions = p.party.getSessions();
		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
	}
	
	public ApprenticeRequest getAndRemoveRequest(int id){
		return id2request.remove(id);
	}
	
	public boolean checkPartyMember(Player teacher,Player apprentice){
		if (System.currentTimeMillis()
				- teacher.pool.getLong(
						Player.PROPERTY_TEACHER_LASTTIME, 0L) < 24 * 60 * 60 * 1000l) {
			sendPartyChat(teacher,MessageFormat.format(peony.Messages.STRING_01049,
					teacher.name));
			return false;
		}
		int old = 0;
		if (teacher.relations != null) {
			if (teacher.relations.apprenticeList != null) {
				if(teacher.relations.apprenticeList.exists(apprentice.id)){
					sendPartyChat(teacher,MessageFormat.format(peony.Messages.STRING_01050,
							teacher.name,apprentice.name));
					return false;
				}
				old = teacher.relations.apprenticeList.getCount();
			}
			if (old >= PlayerRelation.MAX_APPRENTICE) {
				sendPartyChat(teacher,MessageFormat.format(peony.Messages.STRING_01051,
						teacher.name));
		        return false;
			}
		}
		if(apprentice.level < 25){
			sendPartyChat(apprentice,MessageFormat.format(peony.Messages.STRING_01052,
					apprentice.name));
			return false;
		}
		if(apprentice.level >=70){
			sendPartyChat(apprentice,MessageFormat.format(peony.Messages.STRING_01053,
					apprentice.name));
			return false;
		}
		if (System.currentTimeMillis()
				- apprentice.pool
						.getLong(
								Player.PROPERTY_APPRENTICE_LASTTIME,
								0L) < 24 * 60 * 60 * 1000l) {
			sendPartyChat(apprentice,MessageFormat.format(peony.Messages.STRING_01054,
					apprentice.name));
			return false;
		}
		if (apprentice.relations != null) {
			if (apprentice.getTeacherId() != -1) {
				sendPartyChat(apprentice,MessageFormat.format(peony.Messages.STRING_01055,
						apprentice.name));
		        return false;
			}
		}
		return true;
	}
	
	public void playerDeleteRemove(Player p){
		if(p!=null){
			try{
				if(p.getTeacherId() != -1){
				   saveRemove(p.getTeacherId(),p.id);
				   Player teacher = ObjectAccessor.getPlayer(p.getTeacherId());
				   if(teacher != null){
					   ChatService chatService = Server.server.getServiceRegistry().getChatService();
					   chatService.sendPrivateMessage(teacher.id, MessageFormat.format(peony.Messages.STRING_01056,
								p.name));
				   } else {
					   Server.server.getServiceRegistry().getMailService().sendSystemMail(p.getTeacherId(), peony.Messages.STRING_00004,peony.Messages.STRING_00087,MessageFormat.format(peony.Messages.STRING_01056,
								p.name), 0,null, 0,"APPRENTICEDELETE");
				   }
				}else if(p.relations!=null && p.relations.apprenticeList!=null){
					int count = p.relations.apprenticeList.getCount();
					if(count > 0){
						for(int i=0;i<count;i++){
							Actor actor = p.relations.apprenticeList.getPlayerAt(i);
							if(actor != null){
								saveRemove(p.id,actor.id);
							}
						}
					}
				}
			} catch(Exception e){
				
			}
		}
	}
	
	
	
	public static boolean inPartyTogether(Player p){
		boolean in = true;
		if(p!=null && p.relations!=null && p.party!=null && p.party.members!=null && p.party.members.size()==2){
			Player targetPlayer =p. party.getPartyFriend(p.id);
			if(p.level<70){
				if(p.getTeacherId()!=targetPlayer.id){
					in = false;
				}
			}else{
				if(p.relations.apprenticeList==null || !p.relations.apprenticeList.exists(targetPlayer.id)){
					in = false;
		        }
			}
		}else{
			in = false;
		}
		return in;
	}
	
}
