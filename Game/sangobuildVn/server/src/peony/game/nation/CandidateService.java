package peony.game.nation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import peony.db.NationDAO;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapManager;
import peony.game.buff.BuffUtil;
import peony.game.mail.MailService;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.player.ActorCacheService;
import peony.service.stat.Achievement;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;

public class CandidateService implements Service {

	private static final Logger log = Logger.getLogger(CandidateService.class);
	private Map<Integer, Set<Candidate>> candidateRecords = new HashMap<Integer, Set<Candidate>>();
	private Map<Integer, Set<Integer>> votesRecord = new HashMap<Integer, Set<Integer>>();
	protected Map<Integer, Boolean> canSignUpMap = new HashMap<Integer, Boolean>();
	protected Map<Integer, Boolean> canVoteMap = new HashMap<Integer, Boolean>();
	protected Map<Integer, Integer> signUpStartTime = new HashMap<Integer, Integer>();
	protected Map<Integer, Integer> voteStartTime = new HashMap<Integer, Integer>();
	protected Map<Integer, Integer> retireTime = new HashMap<Integer, Integer>();
	protected Map<Integer, Integer> map = new HashMap<Integer, Integer>();
//	protected Timer timer = new Timer();
	protected Map<Integer, Timer> collectTimer = new HashMap<Integer, Timer>();
	ActorCacheService actorCacheService = Server.server.getServiceRegistry().getActorCacheService();
	protected Map<Integer, Integer> votes = new HashMap<Integer, Integer>();
	public int signupFlag = 0; // 标示是否经过了报名阶段（0为没有，1为经过）
	int[][] npc = {{},{272,1114150,1114157},{240,983078,983083},{352,1441827,1441832}}; 
	public static int[] horseItems = {871, 873, 872, 874};
	protected long lastCheckTime;
	
	public CandidateService(){
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				long currentMills = System.currentTimeMillis();
				if(currentMills>lastCheckTime){
					birthKingListener();
					//checkRetireNotify();
					checkKingPower();
					checkNationSloganTime();
					checkKingOnline();
					checkBackCredit();
					lastCheckTime = currentMills;
				}
			}
		}, 60*1000, 60*1000, TimeUnit.MILLISECONDS); // 每隔1分钟执行一次
	}
	
	/**
	 * 国王任期将满的提示
	 */
	protected void checkRetireNotify() {
		Calendar calendar = Calendar.getInstance();
		int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			if(nation.getKingId()>0 && currentDay==6 && (hour*60+min)==21*60){ // 周五进行任期满的提示
				Server.server.getServiceRegistry().getChatService()
				.sendFactionSystemMessage(nation.faction, "Nhiệm kì quốc công đã hết, thứ 2 tuần sau sẽ bầu cử lại. mời đến NPC báo danh tham gia bầu cử");
			}
			if(nation.getKingId()>0 && currentDay==6 && (hour*60+min)==1){ // 周五的开始
				Player p = ObjectAccessor.getPlayer(nation.getKingId());
				if(p!=null){
					p.message(-1, "Nhiệm kì của bạn đã hết, thứ 2 tuần sau sẽ bắt đầu quốc gia đại tuyển chọn", -1, -1);
				}else{
					nation.pool.setInt(((Integer)(nation.faction)).toString(), nation.getKingId());
				}
			}
			if(nation.getKingId()>0 && currentDay==6 && (hour*60+min)>=12*60 && (hour*60+min)<23*60+59){
				if(retireTime.get(nation.faction)==null){
					retireTime.put(nation.faction, hour*60+min);
				}else if((hour*60+min)==retireTime.get(nation.faction)+3*60){// 每3小时提示一次
					Server.server.getServiceRegistry().getChatService()
					.sendFactionSystemMessage(nation.faction, MessageFormat.format("Quốc công {0} nhiệm kì đã hết, vinh quang cách nhiệm, để một vị quốc công khác của chúng ta đến",
							nation.getKingName()));
					retireTime.put(nation.faction, hour*60+min);
				}
			}
		}
	}

	/**
	 * 竞选国王报名
	 */
	public void signUp(Player p, int serial) throws NationVoteException{
		synchronized (this) {
			if(p == null)
				return;
			log.info("[ELECTIONSIGNUP]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]TRY");
			if(canSignUpMap.get(p.faction)==null || !canSignUpMap.get(p.faction)){
				log.info("[ELECTIONSIGNUPFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
				throw new NationVoteException("Hôm nay không phải ngày ghi danh, không thể ghi danh");
			}
			if(p.level < 60){
				log.info("[ELECTIONSIGNUPFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
				throw new NationVoteException("您的等级太低，不能参选国公");
			}
			Set<Candidate> set = candidateRecords.get(p.faction);
			for(Candidate candidate : set){
				if(p.id == candidate.getPlayerId()){
					log.info("[ELECTIONSIGNUPFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
					throw new NationVoteException("Bạn đã báo danh không thể báo danh lại");
				}
			}
			CandidateDao candidateDao = Server.server.getServiceRegistry().getDbService().candidateDao;
			Candidate candidate = new Candidate();
			candidate.setPlayerId(p.id);
			candidate.setFaction(p.faction);
			candidate.setFunds(0);
			candidate.setCreateTime(new Date(System.currentTimeMillis()));
			PlayerTransaction tx = p.newTransaction("NCN");
			try {
				p.decMoney(300000, tx, false);
			} catch (NoEnoughValueException e1) {
				log.info("[ELECTIONSIGNUPFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
				tx.rollback();
				throw new NationVoteException("Cần giao nộp 30 vạn vàng để làm phí báo danh, số vàng của bạn không đủ!");
			} 
			set.add(candidate);
			candidateDao.newEntity(candidate);
			tx.commit();
			if(signupFlag==0)
				signupFlag = 1;
			Nation nation = Server.server.getServiceRegistry().getNationService()
			.getNationByFaction(p.faction);
			if(nation.pool.getInt("SIGNUPFLAG", 0)==0){
				nation.pool.setInt("SIGNUPFLAG", 1);
				Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
			}
			log.info("[ELECTIONSIGNUPSUCCESS]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
		}
	}
	
	/**
	 * 捐献战功
	 */
	public void contributeCredit(Player p, int credit) throws NationVoteException{
		synchronized (this) {
			if(p==null){
				return;
			}else{
				boolean can = true;
				for(Candidate c : candidateRecords.get(p.faction)){
					if(c.getPlayerId()==p.id){
						can = false;
					}
				}
				if(can)
					throw new NationVoteException("Chưa báo danh không thể cống hiến chiến công");
				if(credit<=0){
					throw new NationVoteException("Xin nhập trị giá chiến công đúng");
				}
				log.info("[CONTRIBUTECREDIT]"+LogUtil.getPlayerLogString(p)+"CREDIT["+credit+"]BALANCE["
						+p.getCredit()+"]TRY");
				CandidateDao candidateDao = Server.server.getServiceRegistry().getDbService().candidateDao;
				PlayerTransaction tx = p.newTransaction("NCS");
				try {
					if(canSignUpMap.get(p.faction)!=null && canSignUpMap.get(p.faction)){
						p.decCredit(credit, tx, false);
						Set<Candidate> set = candidateRecords.get(p.faction);
						Iterator<Candidate> iterator = set.iterator();
						while(iterator.hasNext()){
							Candidate candidate = iterator.next();
							if(candidate.getPlayerId() == p.id){
								candidate.setCredit(candidate.getCredit()+credit);
								candidateDao.updateEntity(candidate);
							}
						}
					}else{
						log.info("[CONTRIBUTECREDITFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.getCredit()+"]");
						tx.rollback();
						throw new NationVoteException("Thời gian báo danh đã kết thúc, không thể cống hiến chiến công");
					}
					tx.commit();
				} catch (NoEnoughValueException e) {
					log.info("[CONTRIBUTECREDITFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.getCredit()+"]");
					tx.rollback();
					throw new NationVoteException("您的战功不足");
				}
				log.info("[CONTRIBUTECREDITSUCCESS]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.getCredit()+"]");
			}
		}
	}
	
	/**
	 * 获取候选人
	 * @param playerId,候选人ID
	 * @return
	 */
	public Candidate getCandidate(int playerId, int faction){
		for(Candidate candidate : candidateRecords.get(faction)){
			if(candidate.getPlayerId() == playerId){
				return candidate;
			}
		}
		return null;
	}
	
	/**
	 * 获取前六个候选人,如果报名人数不足六个则返回所有报名者
	 */
	public List<Candidate> getCandidates(int faction){
		NationService nationService = Server.server.getServiceRegistry().getNationService();
		Set<Candidate> set = candidateRecords.get(faction);
		Candidate previousking = null;
		if(nationService.hasKing(faction)){
			for(Candidate candidate : set){
				if(candidate.getPlayerId()==nationService.getNationByFaction(faction).getKingId()){
					previousking = candidate;
					break;
				}
			}
		}
		List<Candidate> list = new ArrayList<Candidate>();
		if(set.size() == 0)
			return null;
		Candidate[] candidates = new Candidate[set.size()];
		int i = 0;
		for(Candidate candidate : set){
			candidates[i] = candidate;
			i++;
		}
		for(int x=0; x<set.size(); x++){ // 按所出战功进行排序
			for(int y=x+1; y<set.size(); y++){
				if(candidates[x].getCredit() < candidates[y].getCredit()){
					Candidate temp = new Candidate();
					temp = candidates[x];
					candidates[x] = candidates[y];
					candidates[y] = temp;
				}
				if(candidates[x].getCredit()==candidates[y].getCredit() 
						&& candidates[x].getCreateTime().after(candidates[y].getCreateTime())){ // 按报名时间进行排序
					Candidate temp = new Candidate();
					temp = candidates[x];
					candidates[x] = candidates[y];
					candidates[y] = temp;
				}
			}
		}
		if(previousking!=null){
			if(candidates.length <= 7){ // 其中肯定包括前一任国王
				for(int j=0; j<candidates.length; j++){
					list.add(candidates[j]);
				}
			}else{
				int flag = 0;
				for(int j=0; j<6; j++){
					if(previousking.getPlayerId()==candidates[j].getPlayerId()){
						flag++;
					}
					list.add(candidates[j]); 
				}
				if(flag==1){ // 前六个人中肯定包含国王,然后在添加第七个候选人
					list.add(candidates[6]);
				}else{
					list.add(previousking);
				}
			}
		}else{
			if(candidates.length < 6){
				for(int j=0; j<candidates.length; j++){
					list.add(candidates[j]);
				}
			}else{
				for(int j=0; j<6; j++){
					list.add(candidates[j]); 
				}
			}
		}
		return list;
	}
	
	/**
	 * 选举投票
	 * @param faction,国家ID
	 * @param candidatePlayerId,被选举人ID
	 */
	public void vote(Player p, int faction, int candidatePlayerId) throws NationVoteException{
		synchronized (this) {
			if(p == null){
				return;
			}else if(canVoteMap.get(p.faction)==null || !canVoteMap.get(p.faction)){
				throw new NationVoteException("今天不是投票日,不能投票");
			}else if(p.level < 60){
				throw new NationVoteException("Đẳng cấp quá thấp, không thể bỏ phiếu");
			}else if(votesRecord.get(faction)!=null && votesRecord.get(faction).contains(p.id)){
				throw new NationVoteException("投票失败，一次选举只能投一票");
			}
			if(candidatePlayerId == p.id){
				throw new NationVoteException("Không thể bỏ phiếu cho mình");
			}
			int day = p.pool.getInt("VOTEDAY", 0);
			if((Time.day-day)<=2){
				throw new NationVoteException("投票失败，一次选举只能投一票");
			}
			log.info("[VOTE]"+LogUtil.getPlayerLogString(p)+"ELECTPLAYERID["+candidatePlayerId+"]TRY");
			Set<Candidate> set = candidateRecords.get(faction);
			Iterator<Candidate> iterator = set.iterator();
			while(iterator.hasNext()){
				Candidate candidate = iterator.next();
				if(candidate.getPlayerId() == candidatePlayerId){
					int votes = candidate.getVotes();
					candidate.setVotes(votes+1);
					CandidateDao dao = Server.server.getServiceRegistry().getDbService().candidateDao;
					dao.updateEntity(candidate);
				}
			}
			Set<Integer> record = votesRecord.get(faction);
			if(record==null){
				record = new HashSet<Integer>();
				record.add(p.id);
				votesRecord.put(faction, record);
			}else{
				record.add(p.id);
			}
			p.pool.setInt("VOTEDAY", Time.day);
			log.info("[VOTESUCCESS]"+LogUtil.getPlayerLogString(p)+"ELECTPLAYERID["+candidatePlayerId+"]");
		}
	}
	
	/**
	 * 获取投票结果
	 * @param faction,国家ID
	 * @param candidatePlayerId,被选举人ID
	 * @return 总票数
	 */
	public int getVoteResult(int faction, int candidatePlayerId){
		Set<Candidate> set = candidateRecords.get(faction);
		for(Candidate candidate : set){
			if(candidate.getPlayerId() == candidatePlayerId){
				return candidate.getVotes();
			}
		}
		return 0;
	}
	
	/**
	 * 统计投票结果,获得票数最多的人的集合(可能是多个人票数一样)
	 * @param faction,国家ID
	 * @return 获得最高票数的集合
	 */
	public List<Candidate> calculateVoteResult(int faction){
		List<Candidate> list = new ArrayList<Candidate>();
		List<Candidate> set = getCandidates(faction);
		if(set==null)
			return null;
		Candidate[] candidates = new Candidate[set.size()];
		int i = 0;
		for(Candidate candidate : set){
			candidates[i] = candidate;
			i++;
		}
		for(int x=0; x<candidates.length; x++){
			for(int y=x+1; y<candidates.length; y++){ // 按最后所得票数进行排序
				if(candidates[x].getVotes() < candidates[y].getVotes()){
					Candidate temp;
					temp = candidates[x];
					candidates[x] = candidates[y];
					candidates[y] = temp;
				}
			}
		}
		for(Candidate candidate : candidates){
			if(candidate.getVotes() == candidates[0].getVotes()){
				list.add(candidate);
			}
		}
		return list;
	}
	
	/**
	 * 产生国王
	 */
	public void birthKing(final int faction) throws NationVoteException{
		synchronized (this) {
			final Timer timer2 = new Timer();
			final Timer timer3 = new Timer();
			List<Candidate> list = calculateVoteResult(faction);
			if(list==null)
				throw new NationVoteException("Không có người chọn");
			final Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(faction);
			Officer preKing = nation.getOfficer(Officer.KING);
			for(Officer officer : nation.officers){
				if(officer!=null){
					officer.setForbidTimes(0);
				}
			}
			for(String name : Officer.NAME){
				nation.pool.remove(name); // 清除大臣修改国家公告条数记录
			}
			nation.clearOfficers();
			signUpStartTime.remove(faction);
			voteStartTime.remove(faction);
			Server.server.getServiceRegistry().getDbService().officerDAO
			.delete("delete from Officer o where o.faction=?", faction);
			final Candidate candidate;
			if(list.size() == 1){
				candidate = list.get(0);
				Officer king = new Officer(candidate.getPlayerId(),Officer.KING,faction
						,actorCacheService.find(candidate.getPlayerId()));
				nation.addOfficer(king);
				if(preKing!=null){
					Server.server.getServiceRegistry().getDbService().officerDAO.makeTransient(preKing);
					Server.server.getServiceRegistry().getDbService().officerDAO.newEntity(king);
					if(candidate.previousKing==1){
						timer2.schedule(new TimerTask(){
							public void run() {
								Server.server.getServiceRegistry().getChatService()
								.sendFactionSystemMessage(faction, MessageFormat.format("giữ nguyên chức vụ,tiếp tục cầm quyền {0}", nation.getKingName()));
							}
						}, 1000, 3*60*60*1000); // 每3小时发一次国家聊
					}else{
						timer2.schedule(new TimerTask(){
							public void run() {
								Server.server.getServiceRegistry().getChatService()
								.sendFactionSystemMessage(faction, MessageFormat.format("thuận ý dân,  {0}được{1}phiếuđược chọn là quốc công", 
										nation.getKingName(),votes.get(faction)));
							}
						}, 1000, 3*60*60*1000); // 每3小时发一次国家聊
					}
					Player previousKing = ObjectAccessor.getPlayer(preKing.id);
					if(previousKing!=null){
						previousKing.unKing();
					}
				}else{
					Server.server.getServiceRegistry().getDbService().officerDAO.newEntity(king);
					timer2.schedule(new TimerTask(){
						public void run() {
							Server.server.getServiceRegistry().getChatService()
							.sendFactionSystemMessage(faction, MessageFormat.format("thuận ý dân,  {0}được{1}phiếuđược chọn là quốc công", 
									nation.getKingName(),votes.get(faction)));
						}
					}, 1000, 3*60*60*1000); // 每3小时发一次国家聊
				}
			}else{
				Candidate[] results = new Candidate[list.size()];
				int i = 0;
				for(Candidate result : list){
					results[i] = result;
					i++;
				}
				// 选出捐献战功最多的一位候选人
				for(int x=0; x<results.length; x++){
					for(int y=x+1; y<results.length; y++){
						if(results[x].getCredit()>results[y].getCredit() || 
								results[x].getCreateTime().before(results[y].getCreateTime())){ 
							Candidate temp;
							temp = results[x];
							results[x] = results[y];
							results[y] = temp;
						}
					}
				}
				candidate = results[results.length-1];
				Officer king = new Officer(candidate.getPlayerId(),Officer.KING,faction
						,actorCacheService.find(candidate.getPlayerId()));
				nation.addOfficer(king);
				if(preKing!=null){
					Server.server.getServiceRegistry().getDbService().officerDAO.makeTransient(preKing);
					Server.server.getServiceRegistry().getDbService().officerDAO.newEntity(king);
					if(candidate.previousKing==1){
						timer2.schedule(new TimerTask(){
							public void run() {
								Server.server.getServiceRegistry().getChatService()
								.sendFactionSystemMessage(faction, MessageFormat.format("giữ nguyên chức vụ,tiếp tục cầm quyền {0}", nation.getKingName()));
							}
						}, 1000, 3*60*60*1000); // 每3小时发一次国家聊
					}else{
						timer2.schedule(new TimerTask(){
							public void run() {
								Server.server.getServiceRegistry().getChatService()
								.sendFactionSystemMessage(faction, MessageFormat.format("thuận ý dân,  {0}được{1}phiếuđược chọn là quốc công", 
										nation.getKingName(),votes.get(faction)));
							}
						}, 1000, 3*60*60*1000); // 每3小时发一次国家聊
					}
				}else{
					Server.server.getServiceRegistry().getDbService().officerDAO.newEntity(king);
					timer2.schedule(new TimerTask(){
						public void run() {
							Server.server.getServiceRegistry().getChatService()
							.sendFactionSystemMessage(faction, MessageFormat.format("thuận ý dân,  {0}được{1}phiếuđược chọn là quốc công", 
									nation.getKingName(),votes.get(faction)));
						}
					}, 1000, 3*60*60*1000); // 每3小时发一次国家聊
				}
			}
			candidate.previousKing = 1;
			candidate.setCredit(0);
			if(candidate.getVotes()==0){
				votes.put(faction, 1);// 如果票数为零,则强制加一票
			}else{
				votes.put(faction, candidate.getVotes());
			}
			candidate.setVotes(0); // 产生国王之后票数恢复为0
			nation.power = 100;
			timer3.schedule(new TimerTask(){
				public void run() {
					timer2.cancel();
					timer3.cancel();
				}
			}, 24*60*60*1000); // 一天后终止发送国家聊
			Set<Candidate> set = candidateRecords.get(faction);
			Iterator<Candidate> iterator = set.iterator();
			while(iterator.hasNext()){
				if(iterator.next().getPlayerId()!=nation.getKingId()){
					iterator.remove();
				}
			}
			Server.server.getServiceRegistry().getDbService()
			.candidateDao.updateEntity(candidate);
			Server.server.getServiceRegistry().getDbService()
			.candidateDao.delete("delete from Candidate o where o.faction=? and o.playerId!=?"
					, faction,nation.getKingId());
			if(votesRecord.get(faction)!=null)
				votesRecord.get(faction).clear();
			if(collectTimer.get(faction)!=null)
				collectTimer.get(faction).cancel();
			int kingId = nation.getKingId();
			Player king = ObjectAccessor.getPlayer(kingId);
			if(king!=null){
				king.message(-1, 
						MessageFormat.format("恭喜你当选了{0}的国公，你的任期为7天，{1}将获得100统帅力，国家的强盛就靠你了，争取当一代明君吧！", 
						nation.getName(),nation.getName()), -1, -1);
				king.setKing();
				king.buffs.addBuff(BuffUtil.createSuiteBuff(216, 1));
			}else{
				nation.pool.setInt(nation.faction+"BIRTHKING", kingId);
			}
			nation.slogan = null;
			Timer t = new Timer();
			t.schedule(new TimerTask(){
				public void run() {
					Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(faction
							, "Quốc công mới nhậm chức, nhận được 100 lực thống soái");
				}
			}, 2*1000);
//			nation.pool.remove("SIGNUPFLAG");
			nation.pool.remove(Officer.PROPERTY_SLOGAN_TIMES); // 清除修改国家公告条数记录
			NationDAO nationDAO = Server.server.getServiceRegistry().getDbService().nationDAO;
			nationDAO.updateEntity(nation);
			MailService mailService = Server.server.getServiceRegistry().getMailService();
			int kingClazz = Server.server.getServiceRegistry().getActorCacheService().find(kingId).clazz;
			GameItem gameItem = ObjectAccessor.createGameItem(horseItems[kingClazz]);
			mailService.sendSystemMail(kingId, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>", "国公专属坐骑", "Quốc công Ngồi thú cưỡi hùng sư, tượng trưng cho quyền lợi vô thượng nước nhà"
					, 0, gameItem, 1, "KING");
			if(preKing!=null){
				int preKingId = preKing.id;
				int currKingId = kingId;
				if(preKingId!=currKingId){
					Player pKing = ObjectAccessor.getPlayer(preKingId);
					if(pKing!=null && pKing.isRide()){
						if(isKingHorse(pKing.horse.itemId)){
							pKing.horse.unRide(pKing);
							pKing.horse = null;
							pKing.unRide();
						}
					}
					if(pKing!=null)
						pKing.buffs.removeBuff(216);
				}
			}
			nation.pool.setLong(Nation.PROPERTY_KINGLOGIN_TIME, 0);
			nation.pool.setLong(Nation.PROPERTY_KINGLOGOUT_TIME, 0);
			
		
			//统计玩家当选国公成就
			try{
				if(nation.getKingId() != -1){
					StatService statService = Server.server.getServiceRegistry().getStatService();
					PvpInfo pvpInfo = statService.getPvpInfo(nation.getKingId(), nation.faction);
					List<Achievement> aList = statService.getAchievementList(StatService.OTHERTYPE_ONCE_ACHIEVETYPE);
					if(aList!=null){
						for(Achievement a:aList){
						    int type = Integer.parseInt(a.param1);
						    if(type == 1){
								if(pvpInfo.pool.getString(StatService.PROPERTY_CANDIDATE_KING) == ""){
									pvpInfo.pool.setString(StatService.PROPERTY_CANDIDATE_KING, statService.getFinishTime(System.currentTimeMillis()));
								    Player p = ObjectAccessor.getPlayer(nation.getKingId());
								    if(p!=null){
									   statService.setMessage(p, a, false);
								    }
								}
						    }
						}
					}
				}
			}catch(Exception e){
				
			}

			log.info("[BIRTHKING]FACTION["+faction+"]KINGID["+kingId+"]");
		}
	}
	
	/**
	 * 定时检测国王统帅力
	 */
	public void checkKingPower(){
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			if(nation.getKingId() > 0 && day!=6 && (hour*60+min)==23*60){
				int power = nation.power;
				if(power >= 20){
					nation.power = power - 20;
				}else{
					nation.power = 0;
				}
				try {
					NationDAO nationDAO = Server.server.getServiceRegistry().getDbService().nationDAO;
					nationDAO.updateEntity(nation);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 检查报名超时
	 */
	protected void checkSignUp(Nation nation) {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		if((cal.get(Calendar.DAY_OF_WEEK)==2 && (hour*60+min)>=(6*60)) 
				|| cal.get(Calendar.DAY_OF_WEEK)==6){ // 每周一、二为竞选报名时间
			if(this.canSignUpMap.get(nation.faction)==null 
					|| (this.canSignUpMap.get(nation.faction)!=null 
					&& this.canSignUpMap.get(nation.faction)==false)){
				this.canSignUpMap.put(nation.faction, true);
				for(int i=1; i<=3; i++){
					VMapManager manager = Server.server.getWorld().getVMapManager(npc[i][0]);
					VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(npc[i][0]);
					for (VMap map : maps) {
						map.refreshNPC(npc[i][2], false);
						map.refreshNPC(npc[i][1], true);
					}
				}
			}
			if(signUpStartTime.get(nation.faction)==null){
				signUpStartTime.put(nation.faction, hour*60+min);
			}else if((hour*60+min)==signUpStartTime.get(nation.faction)+3*60){// 每3小时提示一次
				Server.server.getServiceRegistry().getChatService()
				.sendFactionSystemMessage(nation.faction, "Nước nhà tiến cử ghi danh đã bắt đầu, đề nghị mọi người đến Quốc đô sử quan hăng hái ghi danh");
				signUpStartTime.put(nation.faction, hour*60+min);
			}
		}else{
			if(this.canSignUpMap.get(nation.faction)==null 
					|| (this.canSignUpMap.get(nation.faction)!=null 
					&& this.canSignUpMap.get(nation.faction)==true)){
				this.canSignUpMap.put(nation.faction, false);
			}
		}
	}
	
	/**
	 * 检查投票超时
	 */
	protected void checkVote(Nation nation){
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		if((cal.get(Calendar.DAY_OF_WEEK)==4 && (hour*60+min)>=(9*60)) 
				|| cal.get(Calendar.DAY_OF_WEEK)==5
				|| (cal.get(Calendar.DAY_OF_WEEK)==6 && (hour*60+min)<=(21*60))){// 每周三、四、五为投票时间
			if((this.canVoteMap.get(nation.faction)==null 
					|| (this.canVoteMap.get(nation.faction)!=null 
					&& this.canVoteMap.get(nation.faction)==false)) && signupFlag==1){
				this.canVoteMap.put(nation.faction, true);
				for(int i=1; i<=3; i++){
					VMapManager manager = Server.server.getWorld().getVMapManager(npc[i][0]);
					VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(npc[i][0]);
					for (VMap map : maps) {
						map.refreshNPC(npc[i][1], false);
						map.refreshNPC(npc[i][2], true);
					}
				}
			}
			if(signupFlag==1){
				if(voteStartTime.get(nation.faction)==null){
					voteStartTime.put(nation.faction, hour*60+min);
				}else if((hour*60+min)==voteStartTime.get(nation.faction)+5*60){// 每5小时提示一次
					Server.server.getServiceRegistry().getChatService()
					.sendFactionSystemMessage(nation.faction, "Bây giờ là thời gian nước nhà chính thức tranh cử, xin mời quốc dân đến sử quan bỏ phiếu");
					voteStartTime.put(nation.faction, hour*60+min);
				}
			}
		}else{
			if(this.canVoteMap.get(nation.faction)==null 
					|| (this.canVoteMap.get(nation.faction)!=null 
					&& this.canVoteMap.get(nation.faction)==true)){
				this.canVoteMap.put(nation.faction, false);
			}
		}
	}
	
	/**
	 * 监听产生国王的时间(每分钟监听一次)
	 */
	public void birthKingListener(){
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		NationService nationService = Server.server.getServiceRegistry().getNationService();
		if(nationService==null)
			return;
		for(Nation nation : nationService.getNations()){
			if((cal.get(Calendar.DAY_OF_WEEK)==6 && (hour*60+min)==(14*60+15))){ //每周五23:59产生国王
				try {
					birthKing(nation.faction);
				} catch (NationVoteException e) {
					log.info("[NOCANDIDATES]");
				}
			}
		}
	}
	
	public void shutdown() {
		
	}
	
	public void openSignup(){
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			if(this.canSignUpMap.get(nation.faction)==null 
					|| (this.canSignUpMap.get(nation.faction)!=null 
					&& this.canSignUpMap.get(nation.faction)==false)){
				this.canSignUpMap.put(nation.faction, true);
			}
			if(signUpStartTime.get(nation.faction)==null){
				signUpStartTime.put(nation.faction, hour*60+min);
			}else if((hour*60+min)==signUpStartTime.get(nation.faction)+3*60){// 每3小时提示一次
				Server.server.getServiceRegistry().getChatService()
				.sendFactionSystemMessage(nation.faction, "Nước nhà tiến cử ghi danh đã bắt đầu, đề nghi mọi người đến Quốc đô sử quan hăng hái ghi danh");
				signUpStartTime.put(nation.faction, hour*60+min);
			}
		}
		for(int i=1; i<=3; i++){
			VMapManager manager = Server.server.getWorld().getVMapManager(npc[i][0]);
			VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(npc[i][0]);
			for (VMap map : maps) {
				map.refreshNPC(npc[i][2], false);
				map.refreshNPC(npc[i][1], true);
			}
		}
	}
	
	public void closeSignup(){
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			if((this.canSignUpMap.get(nation.faction)!=null)){
				this.canSignUpMap.put(nation.faction, false);
			}
		}
		backCredit();
	}
	
	public void openVote(){
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			if(this.canVoteMap.get(nation.faction)==null 
					|| (this.canVoteMap.get(nation.faction)!=null 
					&& this.canVoteMap.get(nation.faction)==false)){
				this.signupFlag = 1;
				this.canVoteMap.put(nation.faction, true);
			}
			if(voteStartTime.get(nation.faction)==null){
				voteStartTime.put(nation.faction, hour*60+min);
			}else if((hour*60+min)==voteStartTime.get(nation.faction)+5*60){// 每5小时提示一次
				Server.server.getServiceRegistry().getChatService()
				.sendFactionSystemMessage(nation.faction, "Bây giờ là thời gian nước nhà chính thức tranh cử, xin mời quốc dân đến sử quan bỏ phiếu");
				voteStartTime.put(nation.faction, hour*60+min);
			}
		}
		for(int i=1; i<=3; i++){
			VMapManager manager = Server.server.getWorld().getVMapManager(npc[i][0]);
			VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(npc[i][0]);
			for (VMap map : maps) {
				map.refreshNPC(npc[i][1], false);
				map.refreshNPC(npc[i][2], true);
			}
		}
	}
	
	public void closeVote(){
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			if((this.canVoteMap.get(nation.faction)!=null )){
				this.signupFlag = 0;
				this.canVoteMap.put(nation.faction, false);
			}
		}
	}
	
	public void birthKing(){
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			try {
				this.birthKing(nation.faction);
			} catch (NationVoteException e) {
				
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void startup() throws Exception {
		candidateRecords.put(1, new HashSet<Candidate>()); // 魏
		candidateRecords.put(2, new HashSet<Candidate>()); // 蜀
		candidateRecords.put(3, new HashSet<Candidate>()); // 吴
		CandidateDao candidateDao = Server.server.getServiceRegistry().getDbService().candidateDao;
		List<Candidate> list = candidateDao.list("from Candidate");
		for(Candidate candidate : list){
			Set<Candidate> set = candidateRecords.get(candidate.faction);
			set.add(candidate);
		}
	}
	
	public void update(int diff) {
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			checkSignUp(nation);
			checkVote(nation);
		}
	}
	
	/**
	 * 国王发起募捐
	 */
	public void collect(Player p) throws NationVoteException {
		NationService nationService = Server.server.getServiceRegistry().getNationService();
		if(!nationService.isKing(p)){
			throw new NationVoteException("Bạn không phải là một quốc vương, không có quyền mở mục này!");
		}
		Calendar calendar = Calendar.getInstance();
		int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
		if(map.get(p.faction)!=null && map.get(p.faction)==currentDay){
			// 规定时间内只能发起一次募捐活动(服务器重启后可以跟上次发起募捐同一天内再次发起募捐)
			throw new NationVoteException("Mỗi ngày chỉ có thể phát động quyên góp một lần");
		}else{
			Server.server.getServiceRegistry().getChatService()
			.sendFactionSystemMessage(p.faction, "Hổ lang xâm phạm, trẫm kêu gọi nghĩa sỹ chống lại quân địch, cử quốc vui buồn cùng nhau, trẫm ");
			map.remove(p.faction);
			map.put(p.faction, currentDay);
			// 发起募捐24小时后统计募捐资金
			final Nation nation = nationService.getNationByFaction(p.faction);
			collectTimer.put(p.faction, new Timer());
			Timer timer = collectTimer.get(p.faction);
			timer.schedule(new TimerTask(){
				public void run() {
					Server.server.getServiceRegistry().getNationService().calculateCollection(nation);
				}
			}, 24*60*60*1000); 
		}
	}
	
	/**
	 * 每天更新国家聊次数
	 */
	public void checkNationSloganTime(){
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			if((hour*60+min)==23*60+59){
				nation.pool.remove(Officer.PROPERTY_SLOGAN_TIMES);
				for(String name : Officer.NAME){
					nation.pool.remove(name);
				}
				Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
			}
		}
	}
	
	/**
	 * 国王上线后提示
	 */
	public void checkKingOnline(){
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			int playerId = nation.pool.getInt(((Integer)(nation.faction)).toString(), 0);
			int kingId = nation.pool.getInt(nation.faction+"BIRTHKING");
			if(playerId>0){
				Player player = ObjectAccessor.getPlayer(playerId);
				if(player!=null){
					player.message(-1, "Nhiệm kì của bạn đã hết, ngày mai bắt đầu quốc gia đại tuyển chọn", -1, -1);
					nation.pool.remove(((Integer)(nation.faction)).toString());
					Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
				}
			}
			if(kingId>0){
				Player player = ObjectAccessor.getPlayer(kingId);
				if(player!=null){
					player.message(-1, 
							MessageFormat.format("恭喜你当选了{0}的国公，你的任期为7天，{1}将获得100统帅力，国家的强盛就靠你了，争取当一代明君吧！", 
							nation.getName(),nation.getName()), -1, -1);
					nation.pool.remove(nation.faction+"BIRTHKING");
					Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
				}
			}
			String forbids = nation.pool.getString(nation.faction+"FORBID");
			if(forbids!=""){
				StringBuffer sb = new StringBuffer();
				String[] strs = forbids.split(",");
				if(strs!=null && strs.length>0){
					for(String str : strs){
						try {
							int forbidId = Integer.parseInt(str);
							NationService nationService = Server.server.getServiceRegistry().getNationService();
							Forbid f = nationService.getForbidByTargetId(forbidId);
							if(forbidId>0){
								Player p = ObjectAccessor.getPlayer(forbidId);
								if(p!=null){
									if(f!=null){
										p.message(-1, MessageFormat.format("Bạn đã bị {0} cấm Ngôn", 
												Server.server.getServiceRegistry()
												.getActorCacheService().find(f.id).name), -1, -1);
									}
								}else{
									sb.append(","+str);
								}
							}
						} catch (NumberFormatException e) {
							
						}
					}
				}
				nation.pool.remove(nation.faction+"FORBID");
				nation.pool.setString(nation.faction+"FORBID", sb.toString());
				Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
			}
			String backCredits = nation.pool.getString("BACKCREDIT");
			if(backCredits!=""){
				StringBuffer strb = new StringBuffer();
				String[] backs = backCredits.split(",");
				if(backs.length>0){
					for(String str : backs){
						if(str!=""){
							String[] strs = str.split(" ");
							try {
								int backId = Integer.parseInt(strs[0]);
								int credit = Integer.parseInt(strs[1]);
								Player back = ObjectAccessor.getPlayer(backId);
								if(backId>0 && credit>0 && back!=null){
									log.info("[BACKCREDIT]"+LogUtil.getPlayerLogString(back)
											+"BACK["+credit+"]BALANCE["+back.getCredit()+"]TRY");
									PlayerTransaction tx = back.newTransaction("NCS");
									back.addCredit(credit, tx, false);
									tx.commit();
									back.message(-1, MessageFormat.format("Không trúng cử người chọn, trả về {0} điểm chiến công", credit), -1, -1);
									log.info("[BACKCREDIT]"+LogUtil.getPlayerLogString(back)
											+"BACK["+credit+"]BALANCE["+back.getCredit()+"]");
								}else{
									strb.append(","+strs[0]+" "+strs[1]);
								}
							} catch (NumberFormatException e) {
								
							}
						}
					}
				}
				nation.pool.remove("BACKCREDIT");
				nation.pool.setString("BACKCREDIT", strb.toString());
				Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
			}
		}
	}
	
	/**
	 * 检测战功返还时间
	 */
	public void checkBackCredit(){
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DAY_OF_WEEK);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		if(day==4 && (hour*60+min)==9*60){
			backCredit();
		}
	}
	
	/**
	 * 返还未成为候选人的报名者捐献的战功
	 */
	public void backCredit(){
		for(Nation nation : Server.server.getServiceRegistry().getNationService().getNations()){
			List<Candidate> candidates = getCandidates(nation.faction);
			if(candidates==null || candidateRecords.get(nation.faction)==null){
				return;
			}
			for(Candidate candidate1 : candidateRecords.get(nation.faction)){
				if(!candidates.contains(candidate1)){
					if(candidate1.getCredit()>0){
						Player back = ObjectAccessor.getPlayer(candidate1.getPlayerId());
						if(back!=null){
							log.info("[BACKCREDIT]"+LogUtil.getPlayerLogString(back)
									+"BACK["+candidate1.getCredit()+"]BALANCE["+back.getCredit()+"]TRY");
							PlayerTransaction tx = back.newTransaction("NCS");
							back.addCredit(candidate1.getCredit(), tx, false);
							tx.commit();
							back.message(-1, MessageFormat.format("Không trúng cử người chọn, trả về {0} điểm chiến công", candidate1.getCredit()), -1, -1);
							log.info("[BACKCREDIT]"+LogUtil.getPlayerLogString(back)
									+"BACK["+candidate1.getCredit()+"]BALANCE["+back.getCredit()+"]");
						}else{
							String str = nation.pool.getString("BACKCREDIT");
							StringBuffer sb = new StringBuffer(str);
							nation.pool.setString("BACKCREDIT", sb.append(","+candidate1.getPlayerId()
									+" "+candidate1.getCredit()).toString());
							Server.server.getServiceRegistry().getDbService().nationDAO.updateEntity(nation);
						}
						candidate1.setCredit(0);
						Server.server.getServiceRegistry().getDbService().candidateDao.updateEntity(candidate1);
					}
				}
			}
		}
	}
	
	/**
	 * 对所有报名者进行排序
	 */
	public Candidate[] bubbleCandidates(int faction){
		if(candidateRecords.get(faction)==null || candidateRecords.get(faction).size()==0)
			return null;
		Candidate[] candidates = new Candidate[candidateRecords.get(faction).size()];
		int i = 0;
		for(Candidate candidate : candidateRecords.get(faction)){
			candidates[i] = candidate;
			i++;
		}
		for(int x=0; x<candidates.length; x++){
			for(int y=x+1; y<candidates.length; y++){
				if(candidates[x].getCredit()<candidates[y].getCredit() || 
						candidates[x].getCreateTime().after(candidates[y].getCreateTime())){
					Candidate temp = null;
					temp = candidates[x];
					candidates[x] = candidates[y];
					candidates[y] = temp;
				}
			}
		}
		return candidates;
	}
	
	/**
	 * 获取报名者目前的排名
	 */
	public int getNumInCandidates(Player p){
		Candidate[] candidates = bubbleCandidates(p.faction);
		if(candidates==null)
			return -1;
		int id = p.id;
		for(int x=0; x<candidates.length; x++){
			if(candidates[x].getPlayerId()==id){
				return x+1;
			}
		}
		return -1; // 没有报名返回 -1
	}
	
	/**
	 * 判断是否已经报名
	 */
	public boolean hasCandidate(int id, int faction) {
		for(Candidate candidate : candidateRecords.get(faction)){
			if(candidate.getPlayerId()==id){
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是否是报名时间
	 */
	public boolean canSignup(int faction) {
		if(canSignUpMap.get(faction)!=null){
			return canSignUpMap.get(faction);
		}
		return false;
	}
	
	public static boolean isKingHorse(int itemId){
		for(int id : horseItems){
			if(id==itemId)
				return true;
		}
		if(itemId==798)
			return true;
		return false;
	}
	
}
