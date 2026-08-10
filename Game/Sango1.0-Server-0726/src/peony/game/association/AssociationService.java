package peony.game.association;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 结义系统
 * @author dchen
 */
public class AssociationService implements Service, ServiceEventListener {
	
	private static final Logger log = Logger.getLogger(AssociationService.class);
	
	/** 入盟的最低级别 */
	public static int MINLEVEL = 30;
	
	public Map<Integer, Association> id2Association = new ConcurrentHashMap<Integer, Association>();
	
	public Map<Integer, Association> player2Association = new ConcurrentHashMap<Integer, Association>();
	
	/** 结义BUFF */
	public static int[] BUFFS = {0, 0, 381, 382, 383, 384, 385};
	
	/** 退出扣除声望点数 */
	public static int DEC_HONOR = 5000;
	
	/** 再次加入血盟时间限制 */
	public static long READDTIMEOUT = 24 * 3600 * 1000L;
	
	public void startup() throws Exception {
		loadAssociations();
		Server.server.getEventManager().registerListener(this);
	}
	
	protected void loadAssociations(){
		AssociationDao dao = Server.server.getServiceRegistry().getDbService().associationDao;
		List<Association> list = dao.getAllAssociations();
		for(Association association : list){
			int associationId = association.id;
			id2Association.put(associationId, association);
			for(AssociationMember member : association.memberList.members){
				player2Association.put(member.playerId, association);
			}
		}
	}
	
	protected void saveAssociations(){
		AssociationDao dao = Server.server.getServiceRegistry().getDbService().associationDao;
		for(Association assocition : id2Association.values()){
			dao.makePersistent(assocition);
		}
	}
	
	/** 根据玩家ID获取血盟 */
	public Association getAssociationByPlayerId(int playerId){
		return player2Association.get(playerId);
	}
	
	/** 根据血盟ID获取血盟 */
	public Association getAssociationById(int associationId){
		return id2Association.get(associationId);
	}

	/** 创建结盟 */
	public Association createAssociation(Player p, String name) throws AssociationException{
		if(p!=null){
			if(player2Association.get(p.id)!=null)
				throw new AssociationException("您已加入了血盟,请先退出原有血盟才可创建新的血盟");
			if(p.level<MINLEVEL)
				throw new AssociationException(MessageFormat.format("与兄弟歃血为盟，需要您的等级达到{0}级可以进行，赶快努力吧！", MINLEVEL));
			if(name.length()>16)
				throw new AssociationException("血盟名称太长");
			AssociationDao dao = Server.server.getServiceRegistry().getDbService().associationDao;
			if(dao.findByName(name)!=null)
				throw new AssociationException("您来晚了，已有壮士与您英雄所见略同，请换个更响亮的名字吧。");
			Association association = new Association(p, name);
			dao.newEntity(association);
			return association;
		}
		throw new AssociationException("对不起，血盟没有创建成功，请与兄弟们一同真诚的默念，兄弟齐心歃血为盟一定成功！，");
	}
	
	private void destroyAssociation(Association association){
		removeAssociation(association);
		Server.server.getServiceRegistry().getDbService().schedule(new AssociationDeleteCall(null, association));
	}
	
	/** 删除结盟 */
	public void destroyAssociation(int associationId) throws AssociationException{
		Association association = id2Association.get(associationId);
		if(association!=null){
			List<AssociationMember> list = association.memberList.members;
			if(list.size()>0){
				for(AssociationMember mem : list){
					player2Association.remove(mem.playerId);
					Player p = ObjectAccessor.getPlayer(mem.playerId);
					if(p!=null){
						p.associationInvite = null;
					}
				}
			}
			destroyAssociation(association);
		}
	}
	
	public void addAssociation(Association association, int playerId){
		if(id2Association.get(association.id)==null)
			id2Association.put(association.id, association);
		if(player2Association.get(playerId)==null || player2Association.get(playerId).id!=association.id)
			player2Association.put(playerId, association);
	}
	
	protected void removeAssociation(Object o){
		int associationId = 0;
		if(o instanceof Integer)
			associationId = (Integer)o;
		else if(o instanceof Association)
			associationId = ((Association)o).id;
		id2Association.remove(associationId);
	}
	
	/** 入盟 */
	public void injoyAssociation(Player p, int associationId, int state) throws AssociationException{
		if(p!=null){
			Association association = id2Association.get(associationId);
			if(association==null)
				throw new AssociationException("对不起，您想加入的血盟中的兄弟已各奔东西，请换一个血盟加入吧！");
			if(p.level<MINLEVEL)
				throw new AssociationException(MessageFormat.format("当您达到{0}级后才能入盟", MINLEVEL));
			association.addMember(p.id, AssociationMember.DUTY_MEM, state);
			addAssociation(association, p.id);
			if(state==AssociationMember.STAT_WORK){
				p.associationInvite = null;
				LogUtil.logInjoyAssociation(p, associationId);
			}
		}
	}
	
	/** 退出结盟 */
	public void removeFromAssociation(int playerId) throws AssociationException{
		Association association = player2Association.get(playerId);
		if(association==null)
			throw new AssociationException("这个血盟中的兄弟已各奔东西了。");
		association.removeMember(playerId);
		player2Association.remove(playerId);
		if(association.getMemberCount()==0)
			destroyAssociation(association);
		LogUtil.logRemoveFromAssociation(playerId, association.id);
	}
	
	/** 结盟成员列表 */
	public List<AssociationMember> getMembers(int associationId) throws AssociationException{
		Association association = id2Association.get(associationId);
		if(association==null)
			throw new AssociationException("您想加入的血盟中的兄弟已各奔东西了。");
		return association.memberList.members;
	}
	
	/** 修改血盟名称 */
	public void renameAssociationName(Player player, String name) throws AssociationException{
		if(player!=null){
			Association association = getAssociationByPlayerId(player.id);
			if(association==null || association.getLeader()==null || association.getLeader().playerId!=player.id)
				throw new AssociationException("只有盟主才能修改血盟名称");
			AssociationDao dao = Server.server.getServiceRegistry().getDbService().associationDao;
			if(dao.findByName(name)!=null)
				throw new AssociationException("您来晚了，已有壮士与您英雄所见略同，请换个更响亮的名字吧。");
			association.name = name;
		}else{
			throw new AssociationException("玩家已下线");
		}
	}
	
	/** 发起结盟邀请 */
	public void invite(Player player, int targetId) throws AssociationException{
		if(player!=null){
			Player target = ObjectAccessor.getPlayer(targetId);
			if(target!=null){
				Association association = player2Association.get(player.id);
				if(association==null || association.getMember(player.id)==null 
						|| association.getMember(player.id).duty!=AssociationMember.DUTY_LEADER)
					throw new AssociationException("请您血盟中的盟主来发起结盟，在斗阵中取得胜利有几率获得结义令，便可发起结盟。");
				if(player.faction!=target.faction)
					throw new AssociationException("敌国之人于我辈乃世仇，怎可结盟？");
				if(target.level<MINLEVEL)
					throw new AssociationException(MessageFormat.format("此人功力尚浅，当其达到{0}级时再与其结盟杀敌吧。", MINLEVEL));
				int minLevel = getMinOrMaxTaskLevel(player.level, true);
				int maxLevel = getMinOrMaxTaskLevel(player.level, false);
				if(target.level<minLevel)
					throw new AssociationException("您所邀请的壮士功力尚浅，待其等级提高的与您相近，再与其结为生死弟兄。");
				if(target.level>=maxLevel)
					throw new AssociationException("您的功力尚浅，尚不能与您所邀请的这位英雄歃血为盟，赶快去努力升级吧！");
				if(target.associationInvite!=null && target.associationInvite.inviter!=player.id)
					throw new AssociationException("为时已晚，此英雄已接受了其他血盟的邀请");
				if(target.associationInvite!=null && target.associationInvite.inviter==player.id)
					throw new AssociationException("您已经邀请过了这位英雄，敬请期待回音。");
				if(association.isFull(targetId))
					throw new AssociationException("歃血为盟意味着生死与共，请您谨慎，不要与过多的人结盟。");
				Association association1 = player2Association.get(targetId);
				if(association1!=null){
					if(association1.getLeader()!=null && association1.getLeader().playerId!=player.id)
						throw new AssociationException("这位英雄已与他人歃血为盟了");
					if(association1.getLeader()!=null && association1.getLeader().playerId==player.id)
						throw new AssociationException("这位英雄已经是您血盟中的结义兄弟");
					throw new AssociationException("这位英雄已经是您血盟中的结义兄弟");
				}
				if(target.pool.getLong(Player.PROPERTY_LAST_REMOVEFROMASSOCIATION_TIME, 0)+READDTIMEOUT>System.currentTimeMillis())
					throw new AssociationException("这位英雄刚刚与一帮兄弟解除了血盟之约，24小时内无法再与新的兄弟歃血为盟。");
				Packet pt = new Packet(OpCode.ASSOCIATION_INVITE_SERVER);
				pt.putInt(player.id);
				pt.putString(player.name);
				pt.putInt(association.id);
				pt.putString(association.name);
				target.send(pt);
				LogUtil.logAssociationInvite(player, targetId, association.id);
			}else{
				throw new AssociationException("这位英雄暂时收刀归隐，下线去了。");
			}
		}
	}
	
	/** 答复结盟邀请 */
	public void answerInvite(Player player, int sourceId, int associationId, int answer) throws AssociationException{
		if(player!=null){
			Association association = id2Association.get(associationId);
			String sourcrMessage = null;
			String targetMessage = null;
			if(association==null){
				targetMessage = "您想加入的血盟中的兄弟已各奔东西了。";
			}else if(answer==0){
				sourcrMessage = MessageFormat.format("{0}不愿与您歃血为盟，可能是你们之间的交情不够。", player.name);
			}else if(answer==1){
				sourcrMessage = MessageFormat.format("{0}接受了您义结金兰邀请,与您歃血为盟。", player.name);
				targetMessage = "成功与兄弟歃血为盟，还不速速去主城外星命道人处完成结义任务，得到大家的认可。";
				player.associationInvite = new AssociationInvite(associationId, sourceId, System.currentTimeMillis());
				injoyAssociation(player, associationId, AssociationMember.STAT_WAIT);
			}
			Player source = ObjectAccessor.getPlayer(sourceId);
			if(sourcrMessage!=null && source!=null){
				source.message(-1, sourcrMessage, -1, -1);
			}
			if(targetMessage!=null)
				player.message(-1, targetMessage, -1, -1);
			LogUtil.logAssociationAnswer(player, associationId, answer);
		}
	}
	
	/** 转让血盟  */
	public void transferAssociation(Player leader, int targetPlayerId) throws AssociationException{
		if(leader!=null){
			Association association = player2Association.get(leader.id);
			if(association==null || association.getMember(leader.id).duty!=AssociationMember.DUTY_LEADER)
				throw new AssociationException("您不是盟主，只有盟主才能进行转让。");
			Association association1 = player2Association.get(targetPlayerId);
			if(association1==null || association1.id!=association.id)
				throw new AssociationException("此玩家还不是您血盟中的兄弟，故而不能转让。");
			if(association.getMember(targetPlayerId).state==AssociationMember.STAT_WAIT)
				throw new AssociationException("不能转让给等待状态的盟友");
			association.getMember(targetPlayerId).duty = AssociationMember.DUTY_LEADER;
			association.getMember(leader.id).duty = AssociationMember.DUTY_MEM;
			LogUtil.logTransferAssociation(leader, association.id, targetPlayerId);
		}
	}
	
	public void update(){
		try {
			Iterator<Association> it = id2Association.values().iterator();
			while(it.hasNext()){
				Association association = it.next();
				association.update();
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
		saveAssociations();
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			processPlayerLoaded((Player)event.param1);
			break;
		}
	}
	
	protected void processPlayerLoaded(Player p){
		if(p!=null){
			Association association = player2Association.get(p.id);
			if(association!=null){
				long inviteTime = association.getMember(p.id).inviteTime;
				if(inviteTime+Association.OUTTIME>System.currentTimeMillis()){
					p.associationInvite = new AssociationInvite(association.id, association.getLeader().playerId, inviteTime);
				}
			} else {
				p.associationInvite = null;
			}
		}
	}
	
	/** 取得与玩家等级匹配的结盟的最低或者最高级别 */
	protected int getMinOrMaxTaskLevel(int level, boolean min){
		if(min){
			if(level>=30 && level<50)
				return 30;
			if(level>=50 && level<64)
				return 50;
			if(level>=65)
				return 65;
		}else{
			if(level>=30 && level<50)
				return 50;
			if(level>=50 && level<64)
				return 64;
			if(level>=65)
				return 100;
		}
		return -1;
	}

}
