package com.pip.itimes.server.world.battle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.log4j.Logger;

import com.pip.accountskeleton.AccountSkeleton;
import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.BossRush;
import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.LevelTips;
import com.pip.itimes.server.stage.Monster;
import com.pip.itimes.server.stage.MonsterGroup;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.BufService;
import com.pip.itimes.server.world.CampBattlefieldService;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.Client;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.ConnectSession;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.MercenaryPlayer;
import com.pip.itimes.server.world.MercenaryService;
import com.pip.itimes.server.world.PhizService;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.PositionService;
import com.pip.itimes.server.world.PositionSprite;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.StageService;
import com.pip.itimes.server.world.Team;
import com.pip.itimes.server.world.TongService;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.boss.BossService;
import com.pip.itimes.server.world.camp.CampMainService;
import com.pip.itimes.server.world.game.BattleInstanceModel;
import com.pip.itimes.server.world.game.CampBattlefieldConfig;
import com.pip.itimes.server.world.game.FallService2;
import com.pip.itimes.server.world.game.HouseInstanceModel;
import com.pip.itimes.server.world.game.Instance;
import com.pip.itimes.server.world.game.InstanceService;
import com.pip.itimes.server.world.toplist.TopListService;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class BattleService2 implements Runnable {

    private static final Logger log = Logger.getLogger(BattleService2.class);
    
    public static final int period = 86400000;
    public static long lastMakeTime = Utils.getTodayStart(); //默认要这天的起始时间 最好一次更新
    
    private static int count = 0;

    private Map<Integer,
            Battle2> playerid2battles = new HashMap<Integer, Battle2>();

    private ConcurrentHashMap<Integer,
            Battle2> id2battles = new ConcurrentHashMap<Integer, Battle2>();

    private ConcurrentHashMap<Integer,PkRequester> playerid2requester = new ConcurrentHashMap<Integer,PkRequester>();

    private ConcurrentHashMap<Integer,PkRequester> id2requester = new ConcurrentHashMap<Integer,PkRequester>();
    
    private ConcurrentHashMap<Integer, IPlayerData> waitRealsePlayers = new ConcurrentHashMap<Integer, IPlayerData>();
    
    private ConcurrentHashMap<Integer, byte[]> player2Gem = new ConcurrentHashMap<Integer, byte[]>(); //宣战后，玩家在获得的宝石和掉落的宝石个数

    private ConnectService connectService;

    private PlayerService playerService;

    private PositionService positionService;
    
    private PhizService phizService;
    
    private StageService stageService;

    private TongService tongService;

    private FallService2 fallService;

    private ChatService chatService;
    
    private BossService bossService;
    
    private CampMainService campMainService;
    
    private InstanceService instanceService;
    
    private CampBattlefieldService campBattlefieldService;
    
    private MercenaryService mercenaryService;
    
    public BossService getBossService() {
		return bossService;
	}

	public void setBossService(BossService bossService) {
		this.bossService = bossService;
	}

	public CampMainService getCampMainService() {
		return campMainService;
	}
	
	public InstanceService getInstanceService () {
		return instanceService;
	}
	
	public void setCampBattlefieldService (CampBattlefieldService campBattlefieldService) {
		this.campBattlefieldService = campBattlefieldService;
	}
	
	public MercenaryService getMercenaryService(){
		return mercenaryService;
	}
	
	public void setMercenaryService(MercenaryService mercenaryService){
		this.mercenaryService = mercenaryService;
	}

	public void setCampMainService(CampMainService campMainService) {
		this.campMainService = campMainService;
	}
	
	public void setInstanceService (InstanceService instanceService) {
		this.instanceService = instanceService;
	}
	
	public CampBattlefieldService getCampBattlefieldService () {
		return campBattlefieldService;
	}
	
	private BufService bufService;

    private TopListService topListService;

    private AccountSkeleton accountSkeleton;

    private Random rand = new Random();

    private int id = 1;

    private HouseInstanceModel houseModel;

    private static final BattleStrategy normalClientStrategy = new
            BattleStrategy();
    private static final BattleStrategy normalPkStrategy = new PkBattleStrategy();
    private static final BattleStrategy instanceStrategy = new
            InstanceBattleStrategy();
    private static final BattleStrategy battleFieldStrategy = new BattleFieldBattleStrategy();

//    private ReadWriteLock lock = new ReentrantReadWriteLock();
//    private Lock rLock = lock.readLock();
//    private Lock wLock = lock.writeLock();

    public BattleService2() {
        new Thread(this).start();
    }

    public void setConnectService(ConnectService connectService) {
        this.connectService = connectService;
    }

    public ConnectService getConnectService() {
        return connectService;
    }

    public void setFallService(FallService2 fallService) {
        this.fallService = fallService;
    }

    public FallService2 getFallService() {
        return fallService;
    }

    public void setTongService(TongService tongService) {
        this.tongService = tongService;
    }

    public TongService getTongService() {
        return tongService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public PlayerService getPlayerService() {
        return playerService;
    }
    
    public void setPositionService (PositionService positionService) {
    	this.positionService = positionService;
    }
    
    public PositionService getPositionService () {
    	return positionService;
    }
    
    public void setPhizService (PhizService phizService) {
    	this.phizService = phizService;
    }
    
    public PhizService getPhizService () {
    	return phizService;
    }
    
    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public ChatService getChatService() {
        return chatService;
    }

    public void setStageService(StageService stageService) {
        this.stageService = stageService;
    }

    public StageService getStageService() {
        return stageService;
    }

    public void setBufService(BufService bufService){
        this.bufService = bufService;
    }

    public BufService getBufService(){
        return bufService;
    }

    public TopListService getTopListService() {
        return topListService;
    }

    public void setTopListService(TopListService topListService) {
        this.topListService = topListService;
    }
    
    public void setHouseModel(HouseInstanceModel houseModel) {
        this.houseModel = houseModel;
    }
    
    public HouseInstanceModel getHouseInstanceModel(){
        return this.houseModel;
    }

    public void setAccountSkeleton(AccountSkeleton accountSkeleton){
        this.accountSkeleton = accountSkeleton;
    }

    protected void acqurePlayers(IPlayerData[] players) {
        for (int i = 0; i < players.length; i++) {
            playerService.acquire(players[i]);
        }
    }

    protected void releasePlayers(IPlayerData[] players) {
        for (int i = 0; i < players.length; i++) {
        	if(players[i].getId() >= 0){
        		waitRealsePlayers.put(players[i].getId(), players[i]);
        	}
        }
    }

    protected synchronized void addBattle(Battle2 battle) {
    	IPlayerData[] players = battle.getPlayers();
        acqurePlayers(players);
        for (int i = 0; i < players.length; i++) {
            playerid2battles.put(players[i].getId(), battle);
            players[i].setBattle(true);
            phizService.addChangePhiz((PositionSprite)players[i], PhizService.PHIZ_TYPE_BATTLE, PhizService.PHIZ_STATE_DEFAULT);
        }
        id2battles.put(battle.getId(), battle);
    }

    public synchronized void removeBattle(Battle2 battle) {
        IPlayerData[] players = battle.getPlayers();
        boolean releaseFlag = false;
        
        if (id2battles.remove(battle.getId()) != null) {
            releaseFlag = true;
        }
        
        for (int i = 0; i < players.length; i++) {
            playerid2battles.remove(players[i].getId());
            players[i].setBattle(false);
            phizService.addChangePhiz((PositionSprite)players[i], PhizService.PHIZ_TYPE_BATTLE, PhizService.PHIZ_STATE_DEFAULT);
        }
        
        if(releaseFlag){
            releasePlayers(players);
        }
    }

    public CreditPkBattle startCreditBattle(WorldPlayer p1,
                                            WorldPlayer p2, int credit, Instance instance,
                                            BattleInstanceModel model) throws BattleException {
        CreditPkBattle battle = null;
        synchronized (this) {
            if (inBattle(p1) || inBattle(p2))
                throw new BattleException("已经存在战斗.");
            battle = new CreditPkBattle(id++, this,
                                        battleFieldStrategy, p1, p2, -1, credit, instance, model);
            addBattle(battle);
        }
        if (battle != null)
            battle.start();
        return battle;
    }


//    public synchronized ClientBattle2 createMonsterBattle(WorldPlayer player,
//            MonsterGroup mg,
//            int serial, boolean instance) {
//        if (inBattle(player)) {
//
//        }
//        byte[] mIds = mg.getMonstersId();
//        short[] pros = mg.getProbabilities();
//        List l = new ArrayList(3);
//        Monster[] monsters = stageService.getMonsters(mg.getId());
//        for (int i = 0; i < mIds.length; i++) {
//            if (hit100(rand, pros[i])) {
//                l.add(monsters[i]);
//            }
//        }
//        Monster[] ms = new Monster[l.size()];
//        l.toArray(ms);
//        BattleStrategy strategy = normalClientStrategy;
//        if (instance) {
//            strategy = instanceStrategy;
//        }
//        ClientBattle2 battle = new ClientBattle2(id++, this,
//                                                 strategy,
//                                                 new WorldPlayer[] {player}, ms,
//                                                 mg.getId(), -1, serial);
//        addBattle(battle);
//        battle.start();
//        return battle;
//    }

    public ClientBattle2 startMonsterBattle(IPlayerData[] player,
            MonsterGroup mg, int serial,
            int teamId, boolean instance) throws BattleException {

        byte[] mIds = mg.getMonstersId();
        short[] pros = mg.getProbabilities();
        List l = new ArrayList(3);
        Monster[] monsters = stageService.getMonsters(mg.getId());
        for (int i = 0; i < mIds.length; i++) {
            if (hit100(rand, pros[i])) {
                l.add(monsters[i]);
                
            }
        }
        Monster[] ms = new Monster[l.size()];
        l.toArray(ms);
        BattleStrategy strategy = normalClientStrategy;
        if (instance) {
            strategy = instanceStrategy;
        }
        ClientBattle2  battle = null;
        synchronized(this){
            for (int i = 0; i < player.length; i++) {
                if (inBattle(player[i]))
                    throw new BattleException("已经有战斗存在.");
            }

            battle = new ClientBattle2(id++, this,
                    strategy,
                    player, ms,
                    mg.getId(), teamId, serial, playerService, positionService,false, false);
            addBattle(battle);
        }
//        if(battle!=null)
//            battle.start();
        return battle;
    }
    
    public ClientBattle2 startBossRushBattle(IPlayerData[] player,
            MonsterGroup mg, int serial,
            int teamId, boolean instance,int stage) throws BattleException {
    	byte[] mIds = mg.getMonstersId();
        short[] pros = mg.getProbabilities();
        List l = new ArrayList(3);
        Monster[] monsters = stageService.getMonsters(mg.getId());
        for (int i = 0; i < mIds.length; i++) {
            if (hit100(rand, pros[i])) {
                l.add(monsters[i]);
                
            }
        }
        Monster[] ms = new Monster[l.size()];
        l.toArray(ms);
        for(int i=0;i<ms.length;i++){
        	ms[i].setSpecialHP(BossRush.getBossHP(stage, ms[i].getIndex()));
        	ms[i].setSpecialMP(BossRush.getBossMP(stage, ms[i].getIndex()));
        	int pa = BossRush.getBossPA(stage, ms[i].getIndex());
        	ms[i].setPMinAttack(ms[i].getPMinAttack() + pa);
        	ms[i].setPMaxAttack(ms[i].getPMaxAttack() + pa);
        	int ma = BossRush.getBossMA(stage, ms[i].getIndex());
        	ms[i].setMMinAttack(ms[i].getMMinAttack() + ma);
        	ms[i].setMMaxAttack(ms[i].getMMaxAttack() + ma);
        }
        BattleStrategy strategy = normalClientStrategy;
        if (instance) {
            strategy = instanceStrategy;
        }
    	ClientBattle2  battle = null;
        synchronized(this){
            for (int i = 0; i < player.length; i++) {
                if (inBattle(player[i]))
                    throw new BattleException("已经有战斗存在.");
            }

            battle = new ClientBattle2(id++, this,
                    strategy,
                    player, ms,
                    mg.getId(), teamId, serial, playerService, positionService,true, false);
            addBattle(battle);
        }
//        if(battle!=null)
//            battle.start();
        return battle;
    }
    
    public ClientBattle2 startWorldBossBattle(IPlayerData[] player,
            Monster[] ms, int mgId, int serial,
            int teamId) throws BattleException {
        BattleStrategy strategy = normalClientStrategy;
    	ClientBattle2  battle = null;
        synchronized(this){
            for (int i = 0; i < player.length; i++) {
                if (inBattle(player[i]))
                    throw new BattleException("已经有战斗存在.");
            }

            battle = new ClientBattle2(id++, this,
                    strategy,
                    player, ms,
                    mgId, teamId, serial, playerService, positionService,false, true);
            addBattle(battle);
        }
        return battle;
    }
    
    public synchronized boolean inBattle(IPlayerData player) {
        return playerid2battles.containsKey(player.getId());
    }

    public void addPkRequester(WorldPlayer player,
                                            WorldPlayer dest, int serial,
                                            int wager,boolean isforcepk) throws BattleException {
        int pkId = 0;
        PkRequester requester = null;
        synchronized(this){
            if (inBattle(player) || inPkRequesters(player)) {
                throw new BattleException("现在还不能进行PK");
            }
            if (inBattle(dest) || inPkRequesters(dest)){
                throw new BattleException("对方正在战斗状态");
            }
            pkId = id++;
            requester = new PkRequester(pkId, player, dest, wager,
                    serial,isforcepk);
            playerid2requester.put(player.getId(), requester);
            id2requester.put(pkId, requester);
        }
        if(wager>=0){
            UWAPSegment seg = new UWAPSegment(ClientConstants.PK_CREATED,
                                              serial);
            seg.writeInt(pkId);
            connectService.writeTo(seg, player.getId());
            seg = new UWAPSegment(ClientConstants.PK_REQUEST, serial);
            seg.writeInt(player.getId());
            if (requester.isTeamRequest()) {
                seg.writeString(player.getPlayerName() + "(组队)");
            } else {
                seg.writeString(player.getPlayerName());
            }
            seg.writeInt(dest.getId());
            seg.writeShort((short) player.getLevel());
            seg.writeShort((short) wager);
            seg.writeInt(pkId);
            connectService.writeTo(seg, dest.getId());
        }else{  //偷袭
//            UWAPSegment seg = new UWAPSegment(ClientConstants.SNEAK_ATTACK,
//                                              serial);
//            seg.writeInt(pkId);
//            connectService.writeTo(seg, player.getId());
            UWAPSegment seg = new UWAPSegment(ClientConstants.SNEAK_ATTACK, serial);
//            seg.writeInt(player.getId());
//            if (requester.isTeamRequest()) {
//                seg.writeString(player.getPlayerName() + "(组队)");
//            } else {
//                seg.writeString(player.getPlayerName());
//            }
//            seg.writeInt(dest.getId());
//            seg.writeShort((short) player.getLevel());
//            seg.writeShort((short) wager);
            seg.writeInt(pkId);
            connectService.writeTo(seg, dest.getId());
        }
    }
    public void addPkRequester(Team player,
    		WorldPlayer dest, int serial,boolean isforcepk) throws BattleException {
    	int pkId = 0;
        PkRequester requester = null;
        synchronized(this){
        	WorldPlayer playerLeader = (WorldPlayer)player.getLeader();;
            if (inBattle(playerLeader) || inPkRequesters(playerLeader)) {
                throw new BattleException("现在还不能进行PK");
            }
            if (inBattle(dest) || inPkRequesters(dest)){
                throw new BattleException("对方正在战斗状态");
            }
            
            pkId = id++;
            requester = new PkRequester(pkId, player, dest, -1,
                    serial,isforcepk);
            playerid2requester.put(player.getLeader().getId(), requester);
            id2requester.put(pkId, requester);
            UWAPSegment seg = new UWAPSegment(ClientConstants.SNEAK_ATTACK, serial);
            seg.writeInt(pkId);
            connectService.writeTo(seg, dest.getId());
        }
    }
    public synchronized void pkOk(WorldPlayer player,int pkId){
        PkRequester request = id2requester.get(pkId);
        if(request!=null&&request.getDestId()==player.getId()&&request.close()){
            id2requester.remove(pkId);
            playerid2requester.remove(request.getSourceId());
            WorldPlayer source = playerService.getWorldPlayer(request.getSourceId());
            if(source!=null){
                IPlayerData[] sources = getFollowPlayers(source);
                IPlayerData[] dests = getFollowPlayers(player);
                if(sources.length==0||dests.length==0){  //不是队长了
                    sendRefuse(request.getSourceId(),(byte)1,"组队状态变化,无法启动PK",request.getSerial());
                }else{
                    if(!request.checkNoChanged(sources)){
                        sendRefuse(player.getId(),(byte)1,"组队状态变化,无法启动PK",request.getSerial());
                    }else{
                        if(!sources[0].inPkMap()||!dests[0].inPkMap()){
                            sendRefuse(player.getId(),(byte)1,"现在地图不允许PK",request.getSerial());
                            return;
                        }
                        if(request.getWager()>=0){//pk 靠费用来判定类型的
                            WagerMultiPkBattle battle = new WagerMultiPkBattle(id++, this, normalPkStrategy, true,
                                    sources, dests, request.getSerial(), request.getWager());
                            addBattle(battle);
                            battle.start();
                        }else{//偷袭，现在是阵营
                            for(int i=0;i<sources.length;i++){
                                if(sources[i].hasBuf(Buf.GUARD)){
                                    Changed changed = new Changed();
                                    sources[i].clearDeadTime();
                                    changed.setProperty(Changed.GUARDSTATE,0);
                                    connectService.sendGetItem(changed,sources[i].getId(),(byte)22);
                                }
                            }
                            AbstractMultiPkBattle battle = null;
                            
                            if(source.getTeam() != null){
                            	battle = new WarTeamBattle(id++, this, normalPkStrategy, true,
                                        sources, dests, request.getSerial(),request.getIsForcePk());
                            }else{
                            	battle = new WarPkBattle(id++, this, normalPkStrategy, true,
                                        sources, dests, request.getSerial(),request.getIsForcePk());
                            }
                            addBattle(battle);
                            battle.start();
                        }
                    }
                }
            }
        }else{
            UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
            seg.writeString("PK已经超时");
            connectService.writeTo(seg, player.getId());
        }
    }

    protected IPlayerData[] getFollowPlayers(WorldPlayer player){
        Team team = player.getTeam();
        if(team==null||player.getTeamState()==WorldPlayer.TEAM_NORMAL){
            return new WorldPlayer[]{player};
        }else{
            if(team.getLeader()!=player)
                return new WorldPlayer[0];
            else
                return getTeamMembers(team, WorldPlayer.TEAM_FOLLOW);
        }
    }
    
    public IPlayerData[] getTeamMembers(Team team, int state){
    	return team.getMembers(state);
    }

    public synchronized void pkRefuse(WorldPlayer player, int pkId, byte code,
                                      String cause,int serial) {
        PkRequester request = id2requester.get(pkId);
        if (request != null) {
            if (player.getId() == request.getDestId()) {
                if(request.getWager()>=0){
                    UWAPSegment seg = new UWAPSegment(ClientConstants.PK_REFUSE,
                            serial);
                seg.write(code);
                seg.writeString(cause);
                seg.writeInt(pkId);
                connectService.writeTo(seg, request.getSourceId());
                id2requester.remove(pkId);
                playerid2requester.remove(request.getSourceId());
                }else{
                    connectService.sendMessage(request.getSourceId(),cause);
                    id2requester.remove(pkId);
                    playerid2requester.remove(request.getSourceId());
                }
            }
        }
    }

    public synchronized boolean inPkRequesters(WorldPlayer player){
        return playerid2requester.containsKey(player.getId());
    }

    public Battle2 startPkBattle(WorldPlayer p1, WorldPlayer p2,
                                              int serial,
                                              short wager, int sessionId) throws
            BattleException {

        Team team = p1.getTeam();
        IPlayerData[] players1 = null;
        if (team != null) {
            players1 = getTeamMembers(team, WorldPlayer.TEAM_FOLLOW);
        } else {
            players1 = new WorldPlayer[] {p1};
        }
        IPlayerData[] players2 = null;
        team = p2.getTeam();
        if (team != null) {
            players2 = getTeamMembers(team, WorldPlayer.TEAM_FOLLOW);
        } else {
            players2 = new WorldPlayer[] {p2};
        }
        WagerMultiPkBattle battle = null;
        synchronized(this){
            if (inBattle(p1) || inBattle(p2)) {
                throw new BattleException("对方拒绝");
            }

            battle = new WagerMultiPkBattle(id++, this,
                    normalPkStrategy, true, players1,
                    players2, serial, wager);
            addBattle(battle);
        }
        if(battle!=null)
            battle.start();
        return battle;
    }


    public synchronized Battle2 getBattle(int id) {
        return id2battles.get(id);
    }

    public synchronized Battle2 getBattleByPlayer(int id) {
        return playerid2battles.get(id);
    }


    private static boolean hit100(Random rnd, int chance) {
        int r = rnd.nextInt(100);
        if (r <= chance)
            return true;
        return false;
    }


    public void changeTeamStateToNormal(BattleSprite bs) {
        Team team = bs.player.getTeam();
        if (team != null) {
            if (bs.player == team.getLeader()) {
            	IPlayerData[] players = team.getMembers(WorldPlayer.TEAM_FOLLOW);
                for (int i = 0; i < players.length; i++) {
                    if (players[i] != bs.player && players[i] instanceof WorldPlayer) {
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                TEAM_LEAVE,
                                -1);
                        seg.writeInt(team.getId());
                        seg.writeInt(players[i].getId());
                        PositionSprite[] members = team.getPlayers();
                        seg.write((byte) WorldPlayer.TEAM_NORMAL);
                        players[i].setTeamState(WorldPlayer.TEAM_NORMAL);
                        for (int j = 0; j < members.length; j++) {
                        	if(members[j] instanceof WorldPlayer){
                        		connectService.writeTo(seg, members[j].getId());
                        	}
                        }
                    }
                }
            } else {
            	if(bs.player != null && bs.player instanceof WorldPlayer){
	                UWAPSegment seg = new UWAPSegment(ClientConstants.TEAM_LEAVE,
	                                                  -1);
	                seg.writeInt(team.getId());
	                seg.writeInt(bs.player.getId());
	                seg.write((byte) WorldPlayer.TEAM_NORMAL);
	                PositionSprite[] members = team.getPlayers();
	                bs.player.setTeamState(WorldPlayer.TEAM_NORMAL);
	                for (int i = 0; i < members.length; i++) {
	                	if(members[i] instanceof WorldPlayer){
		                    connectService.writeTo(seg, members[i].getId());
		                }
	                }
            	}
            }
        }
    }

    public void sendGotoMap(int playerId, short mapId, short x, short y) {
        byte[] bytes = stageService.getTaskBytes((short) 31004,
                                                 new String[] {"" + mapId,
                                                 "" + x, "" + y});
        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                          GET_FILE_OK);
        seg.writeShort((short) 31004);
        seg.writeShort((short) 2);
        seg.write(bytes);
        connectService.writeTo(seg, playerId);
    }

    public void sendFailureGotoMap(int playerId,short mapId,short x,short y){
        byte[] bytes = stageService.getTaskBytes((short) 31018,
                                                 new String[] {"" + mapId,
                                                 "" + x, "" + y});
        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                          GET_FILE_OK);
        seg.writeShort((short) 31018);
        seg.writeShort((short) 2);
        seg.write(bytes);
        connectService.writeTo(seg, playerId);
    }

    protected void sendRefuse(int playerId, byte code, String cause,int serial) {
        UWAPSegment seg = new UWAPSegment(ClientConstants.PK_REFUSE, serial);
        seg.write(code);
        seg.writeString(cause);
        seg.writeInt(id);
        connectService.writeTo(seg, playerId);
    }

    public void checkLevelChangedAndSendTips(WorldPlayer player,Changed changed, int playerId) {
        int level = changed.getProperty(Changed.LEVEL);
        if (level != 0) {
            String message = LevelTips.getTip(level);
            if (message != null) {
            	if (message != ""){
	                byte[] bytes = stageService.getTaskBytes((short) 31019,
	                        new String[] {message});
	                UWAPSegment seg = new UWAPSegment(ClientConstants.
	                                                  GET_FILE_OK);
	                seg.writeShort((short) 31019);
	                seg.writeShort((short) 2);
	                seg.write(bytes);
	                connectService.writeTo(seg, playerId);
            	}
            }
            String shout = LevelTips.getShout(level);
            if(shout!=null){
                shout = shout.replace("player",player.getPlayerName());
                chatService.sendWorldMessage(-1,"系统",shout);
            }
            //推荐人通用函数
        	playerService.recommendBalance(player, "Battle");
        	//尝试加到师傅的列表中
        	playerService.addMasterPlayer(player, changed);

            // Light: 卓望版本，每次升级都通知认证服务器
            if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC) {
                ConnectSession[] ss = connectService.getConnectSession();
                Client client = null;
                for (ConnectSession cs : ss) {
                    client = cs.getClientOfPlayer(player.getId());
                    if (client != null) {
                        break;
                    }
                }
                if (client != null) {
                    Server.instance.authSession.sendLevelUpNotify(client.cmccUserId, 
                            player.getAccountId(), player.getId(), level);
                }
            }
        }
    }

    public void run() {
        for (; ; ) {
            Iterator<Battle2> ite = id2battles.values().iterator();
            long current = System.currentTimeMillis();
            while (ite.hasNext()) {
                try {
                    ite.next().doTime(current);
                } catch (Exception ex) {
                    log.error(ex, ex);
                }
            }
            Iterator<PkRequester> ite1 = id2requester.values().iterator();
            current = System.currentTimeMillis();
            while(ite1.hasNext()){
                PkRequester request = ite1.next();
                if((request.getTime()+30*1000L)<current){
                    if(request.close()){
                        ite1.remove();
                        playerid2requester.remove(request.getSourceId());
                        sendRefuse(request.getSourceId(),(byte)1,"对方没有反应",request.getSerial());
                    }
                }
            }
            
            //处理需要移除的player
            for(IPlayerData player : waitRealsePlayers.values()){
                playerService.release(player);
            }
            waitRealsePlayers.clear();
            
            try {
            	if(count == 120 ){
            		count = 0;
            		if(Utils.getTodayStart() >=lastMakeTime + period){
            			clearPlayer2Gem();
            		}
            	}
            	count++;
                Thread.sleep(5 * 1000L);
            } catch (InterruptedException ex1) {
            }
        }
    }
    public  boolean isGetGem(int failid,int winid)
    {
    	boolean ret = false;
    	byte[] failgem = null;
    	byte[] wingem = null;
    	if(player2Gem.containsKey(failid)){
    		failgem = player2Gem.get(failid);;
    	}
    	if(player2Gem.containsKey(winid))
    	{
    		wingem = player2Gem.get(winid);
    	}
    	if(failgem == null && wingem == null ){
    		ret = true;	//第一次战斗
    	}else if(failgem == null){
    		// 判断赢家
    		if(wingem[1] < 3){
    			ret = true;
    		}else{
    			ret = false;
    		}
    	}else if(wingem == null){
    		// 判断输家
    		if(failgem[0] < 3){
    			ret = true;
    		}else{
    			ret = false;
    		}
    	}else{
    		if(failgem[0] < 3 && wingem[1] < 3){
    			ret = true;
    		}else{
    			ret = false;
    		}
    	}
    	
    	return ret;
    }
    public synchronized void setPlayer2Gem(int failid,int winid){
    	// 输家
    	byte[] failByte;
    	if(!player2Gem.containsKey(failid)){
    		failByte = new byte[]{0,0};
    	}else{
    		failByte = player2Gem.get(failid);
    	}
    	int temp = failByte[0];
    	failByte[0] = (byte)(temp + 1);
    	player2Gem.put(failid, failByte);
		
    	// 赢家
    	byte[] winByte;
    	if(!player2Gem.containsKey(winid)){
    		winByte = new byte[]{0,0};;
    	}else{
    		winByte = player2Gem.get(winid);
    	}
    	temp = winByte[1];
    	winByte[1] = (byte)(temp + 1);
    	player2Gem.put(winid, winByte);
    }
    public synchronized void clearPlayer2Gem(){
    	lastMakeTime = Utils.getTodayStart();			// 设置一天的初始时间
    	Iterator keys = (Iterator) player2Gem.keys();
    	while(keys.hasNext()){
    		player2Gem.remove(keys.next());
    	}
    }
    
    public void addBattlefieldPKRequester (WorldPlayer self, WorldPlayer target, int serial,
            									int wager) throws BattleException {
		int pkID = 0;
		PkRequester requester = null;
		synchronized (this) {
			if (inBattle(self) || inPkRequesters(self)) {
				throw new BattleException("现在还不能进行PK");
			}
			if (inBattle(target) || inPkRequesters(target)){
				throw new BattleException("对方正在战斗状态");
			}
			pkID = id++;
			requester = new PkRequester(pkID, self, target, wager,
			serial,false);
			playerid2requester.put(self.getId(), requester);
			id2requester.put(pkID, requester);
		}
		if (wager >= 0) {
			UWAPSegment seg = new UWAPSegment(ClientConstants.PK_CREATED, serial);
			seg.writeInt(pkID);
			connectService.writeTo(seg, self.getId());
			seg = new UWAPSegment(ClientConstants.PK_REQUEST, serial);
			seg.writeInt(self.getId());
			if (requester.isTeamRequest()) {
				seg.writeString(self.getPlayerName() + "(组队)");
			} else {
				seg.writeString(self.getPlayerName());
			}
			seg.writeInt(target.getId());
			seg.writeShort((short) self.getLevel());
			seg.writeShort((short) wager);
			seg.writeInt(pkID);
			connectService.writeTo(seg, target.getId());
		} else {  // 战场宣战
			UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, serial);
			seg.writeShort(ClientConstants.EXTEND_INCAMP_BATTLEFIELD_BATTLEFIELD);
			seg.write(CampBattlefieldConfig.ACTION_BATTLEFIELD_WAR);
			seg.writeInt(pkID);
			connectService.writeTo(seg, target.getId());
		}
    }
    
    public void addBattlefieldPKRequester (Team player,
    				WorldPlayer target, int serial) throws BattleException {
		int pkID = 0;
		PkRequester requester = null;
		synchronized(this){
			WorldPlayer playerLeader = (WorldPlayer)player.getLeader();
			if (inBattle(playerLeader) || inPkRequesters(playerLeader)) {
				throw new BattleException("现在还不能进行PK");
			}
			if (inBattle(target) || inPkRequesters(target)){
				throw new BattleException("对方正在战斗状态");
			}
			
			pkID = id++;
			requester = new PkRequester(pkID, player, target, -1, serial,false);
			playerid2requester.put(player.getLeader().getId(), requester);
			id2requester.put(pkID, requester);
			UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, serial);
			seg.writeShort(ClientConstants.EXTEND_CAMP_BATTLEFIELD_RELATED);
			seg.write(CampBattlefieldConfig.ACTION_BATTLEFIELD_WAR);
			seg.writeInt(pkID);
			connectService.writeTo(seg, target.getId());
		}
    }
    
    public synchronized PkRequester getPKRequester (int pkID) {
    	return id2requester.get(pkID);
    }
    
    public synchronized void battlefieldPKSucceed (WorldPlayer target, int pkID) {
        PkRequester request = id2requester.get(pkID);
        if (request != null && request.getDestId() == target.getId() && request.close()) {
            id2requester.remove(pkID);
            playerid2requester.remove(request.getSourceId());
            WorldPlayer source = playerService.getWorldPlayer(request.getSourceId());
            if (source != null) {
                IPlayerData[] sources = getFollowPlayers(source);
                IPlayerData[] dests = getFollowPlayers(target);
                if (sources.length == 0 || dests.length==0) {  //不是队长了
                    sendRefuse(request.getSourceId(),(byte)1,"组队状态变化,无法启动PK", request.getSerial());
                } else {
                    if (!request.checkNoChanged(sources)) {
                        sendRefuse(target.getId(),(byte)1,"组队状态变化,无法启动PK", request.getSerial());
                    } else {
                        if (request.getWager() >= 0) {//pk 靠费用来判定类型的
                            WagerMultiPkBattle battle = new WagerMultiPkBattle(id++, this, normalPkStrategy, true,
                                    sources, dests, request.getSerial(), request.getWager());
                            addBattle(battle);
                            battle.start();
                        } else {	// 战场中宣战
                            for (int i = 0;i < sources.length; i++) {
                                if(sources[i].hasBuf(Buf.GUARD)){
                                    Changed changed = new Changed();
                                    sources[i].clearDeadTime();
                                    changed.setProperty(Changed.GUARDSTATE,0);
                                    connectService.sendGetItem(changed,sources[i].getId(),(byte)22);
                                }
                            }
                            if (target.getMap() != null && target.getMap().getInstance() != null) {
                            	int battlefieldID = target.getMap().getInstance().getId();
                            	Instance instance = instanceService.getInstance(battlefieldID);
                            	if (instance != null) {
                            		AbstractMultiPkBattle battle = new ResourcesBattlefieldPKBattle(id++,
                            				this, normalPkStrategy, true, sources, dests, request.getSerial(), instance);
                            		addBattle(battle);
                            		battle.start();
                            	} else {
                            		UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
                            		seg.writeString("你不在战场中。");
                            		connectService.writeTo(seg, source.getId());
                            	}
                            } else {
                            	UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
                        		seg.writeString("你不在战场中。");
                        		connectService.writeTo(seg, source.getId());
                            }
                        }
                    }
                }
            }
        } else {
            UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
            seg.writeString("宣战已超时。");
            connectService.writeTo(seg, target.getId());
        }
    }
}


class PkRequester {

    private IntList requesters = new ArrayIntList();
    private int id;
    private int destId;
    private int wager;
    private int serial;
    private long time;
    private boolean closed = false;
    private boolean isforcepk = false;
    
    public PkRequester(int id,WorldPlayer player,WorldPlayer dest,int wager,int serial,boolean forcepk) {
        Team team = player.getTeam();
        if (team == null||player.getTeamState()==WorldPlayer.TEAM_NORMAL) {
            requesters.add(player.getId());
        } else {
        	IPlayerData[] players = team.getMembers(WorldPlayer.TEAM_FOLLOW);
            for (int i = 0; i < players.length; i++) {
                requesters.add(players[i].getId());
            }
        }
        this.id = id;
        this.destId = dest.getId();
        this.wager = wager;
        this.time = System.currentTimeMillis();
        this.serial = serial;
        this.isforcepk = forcepk;
        
    }
    public PkRequester(int id,Team player,WorldPlayer dest,int wager,int serial,boolean forcepk) {

    	IPlayerData[] players = player.getMembers(WorldPlayer.TEAM_FOLLOW);
        for (int i = 0; i < players.length; i++) {
            requesters.add(players[i].getId());
        }
        this.id = id;
        this.destId = dest.getId();
        this.wager = wager;
        this.time = System.currentTimeMillis();
        this.serial = serial;
        this.isforcepk = forcepk;
    }

    public boolean isTeamRequest(){
        return requesters.size()>1;
    }

    public boolean getIsForcePk(){
    	return isforcepk;
    }
    
    public int getId(){
        return id;
    }

    public int getDestId(){
        return destId;
    }

    public int getWager(){
        return wager;
    }

    public int[] getPlayersId() {
        return requesters.toArray();
    }

    public int getSourceId(){
        return requesters.get(0);
    }

    public boolean contains(int playerId) {
        return requesters.contains(playerId);
    }

    public long getTime(){
        return time;
    }

    public boolean checkNoChanged(IPlayerData[] players){
        if(players.length!=requesters.size())
            return false;
        for(int i=0;i<players.length;i++){
            if(!requesters.contains(players[i].getId()))
                return false;
        }
        return true;
    }

    public int getSerial(){
        return serial;
    }

    public synchronized boolean close(){
        if(closed)
            return false;
        closed = true;
        return true;
    }
}
