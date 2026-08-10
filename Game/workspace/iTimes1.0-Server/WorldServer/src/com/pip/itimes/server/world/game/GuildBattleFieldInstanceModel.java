package com.pip.itimes.server.world.game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.log4j.Logger;

import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.util.IntHashSet;
import com.pip.itimes.server.world.BbsService;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.InstanceDefinition;
import com.pip.itimes.server.world.MailService;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.TongData;
import com.pip.itimes.server.world.TongService;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.BattleService2;
import com.pip.itimes.server.world.battle.CreditPkBattle;

public class GuildBattleFieldInstanceModel implements BattleInstanceModel,Runnable{
    private static final Logger log = Logger.getLogger(GuildBattleFieldInstanceModel.class);

    private volatile boolean isValid;

    private WorldService worldService;
    private InstanceService instanceService;
    private PlayerService playerService;
    private BattleService2 battleService;
    private ChatService chatService;
    private BbsService bbsService;
    private MailService mailService;
    private TongService tongService;

    private GuildBattleFieldInstance instance;

    private Set<CreditPkBattle> battles = new HashSet<CreditPkBattle>();
    private Map<Integer,Long> failures = new HashMap<Integer,Long>();
    private IntHashSet inBattles = new IntHashSet();
//    private Map<Integer,Long> waiting = new HashMap<Integer,Long>();



    private volatile boolean canEnterfor = false;
    private volatile boolean canEnter = false;

    private long createTime;
    private long forbidEnterTime;
    private long forbidTime;
    private long endTime;
    private InstanceDefinition idf;

    private static SimpleDateFormat format = new SimpleDateFormat("MM月dd日");
    private static SimpleDateFormat format1 = new SimpleDateFormat("HH:mm");

    private static final int[] CREDIT_PERCENTS = {20,
                                                 16,
                                                 14,
                                                 12,
                                                 10,
                                                 8,
                                                 7,
                                                 6,
                                                 4,
                                                 3
    };

    private static final int[] MONEY_PERCENTS = {20,
                                                16,
                                                14,
                                                12,
                                                10,
                                                8,
                                                7,
                                                6,
                                                4,
                                                3
    };

    private static final int CREDIT_RIGHT = 2;

    public GuildBattleFieldInstanceModel() {
//        this.worldService = worldService;
//        this.instanceService = instanceService;
//        this.playerService = playerService;
//        this.battleService = battleService;
//        createInstances(instanceService.getInstanceDefinition(95001));
        new Thread(this).start();
    }

    private void createInstance(){
        idf = instanceService.getInstanceDefinition(95004);
        instance = createInstance(idf);
    }


    public void start(long forbidEnterTime,long forbidTime,long endTime) throws BattleFieldException{
        if(isValid)
            throw new BattleFieldException("还有战场没有关闭");
        createInstance();
        log.info("GuildBattleField Started");
        isValid = true;
        canEnterfor = true;
        canEnter = true;
        createTime = System.currentTimeMillis();
        this.forbidEnterTime = forbidEnterTime;
        this.forbidTime = forbidTime;
        this.endTime = endTime;
        chatService.sendSystemMessage(format1.format(new Date(createTime))+"~"+format1.format(new Date(endTime))+"的公会试练场已经开放");
    }

    public void forceStop(){
        isValid = false;
        cancel();
    }

    public void cancel(){
        List<CreditPkBattle> cBattles = new ArrayList<CreditPkBattle>(battles);
//        synchronized(battles){
//
//        }
        for(CreditPkBattle battle:cBattles){
            battle.cancel();
        }
        int[] ids = instance.getActives();
        for(int i=0;i<ids.length;i++){
            WorldPlayer player = playerService.getWorldPlayer(ids[i]);
            if(player!=null){
                GameMap map = player.getMap();
                if(map!=null&&map.getInstance()==instance){
                    player.getMap().removePlayer(player,true);
                    player.setMap(null);
                }
               }
        }
        instanceEnded(instance);
        worldService.instanceRemoved(instance);
        instance = null;

    }

    protected void instanceEnded(GuildBattleFieldInstance instance){
        int battleCount = instance.getBattleCount();
        if(battleCount<=0)
            return;
        if(instance.getPlayerCount()<3)
            return;
        int totalCredit = battleCount*6;
        List<BattleFieldRecord> l = instance.getOrderedGuildRecords();
        int i = 0;
        for(i=0;i<l.size()&&i<10;i++){
            BattleFieldRecord r = l.get(i);
            if(r.getPoint()<=0)  //只有积分大于0的才参与排名
                {
                break;
                }
            int money = (int)((long)(instance.getMoney() * 60 * MONEY_PERCENTS[i])/10000);
            //mengjie modify
            int credit = totalCredit * CREDIT_PERCENTS[i]/100 +r.winTimes*2+r.loseTimes;
            addGuildAward(r.id,money,credit);
            StringBuilder sb = new StringBuilder(100);
            sb.append("你所在公会的试练场排名为:");
            sb.append(i+1);
            sb.append("积分为:");
            sb.append(r.winTimes*2-r.loseTimes);
            sb.append(".获胜(");
            sb.append(r.winTimes);
            sb.append(")");
            sb.append("失败(");
            sb.append(r.loseTimes);
            sb.append(")");
            sb.append(".获得排名公会荣誉:");
            sb.append(totalCredit * CREDIT_PERCENTS[i]/100);
            sb.append(".公会成员PK总次数获得荣誉:");
            sb.append(r.winTimes*2+r.loseTimes);
            sb.append(".获得金钱:");
            sb.append(money);
            sb.append("J");
            TongData tong = tongService.getTongData(r.id);
            int owner = tong.getTongOwner();
            mailService.sendMail(owner,"",-1,"系统","战场结果",sb.toString(),ItemUtils.money2dbAttachment(money),0,true);
            chatService.sendTongMessage(tong.getId(),-1,"系统",sb.toString());
            log.info("GuildID["+r.id+"] WinMoney["+money+"] WinCredit["+credit+"] Order["+i+"]");
        }
        log.info("GuildBattleFieldEnded TotalMoney[" +
                 instance.getMoney() + "] TotalCredit[" + totalCredit +
                 "] BattleCount[" + battleCount + "] PlayerCount[" +
                 instance.getAttendedCount() + "]");
        int sCredit = Math.min(totalCredit / 100, 10);
        if(sCredit>0){
            for (; i < l.size(); i++) {
                BattleFieldRecord r = l.get(i);
                if (r.winTimes > 0){ //只有至少获胜过1场的才加荣誉
                    addGuildAward(r.id, 0, sCredit + r.getPoint2());
                    //mengjie add
                    StringBuilder sb = new StringBuilder(100);
                    sb.append("你所在公会的在试练场获胜(");
                    sb.append(r.winTimes);
                    sb.append(")");
                    sb.append("失败(");
                    sb.append(r.loseTimes);
                    sb.append(")");
                    sb.append(".获得试炼场奖励公会荣誉:");
                    sb.append(sCredit);
                    sb.append(".公会成员PK总次数获得荣誉:");
                    sb.append(r.getPoint2());
                    TongData tong = tongService.getTongData(r.id);
                    int owner = tong.getTongOwner();
                    mailService.sendMail(owner,"",-1,"系统","战场结果",sb.toString(),null,0,true);
                    log.info("GuildID["+r.id+"] Credit["+sCredit + r.getPoint2()+"]");
                }else{
                	addGuildAward(r.id, 0, r.getPoint2());
                	//mengjie add
                    StringBuilder sb = new StringBuilder(100);
                    sb.append("你所在公会的在试练场获胜(");
                    sb.append(r.winTimes);
                    sb.append(")");
                    sb.append("失败(");
                    sb.append(r.loseTimes);
                    sb.append(")");
                    sb.append(".获得试炼场奖励公会荣誉:0");
                    sb.append(".公会成员PK总次数获得荣誉:");
                    sb.append(r.getPoint2());
                    TongData tong = tongService.getTongData(r.id);
                    int owner = tong.getTongOwner();
                    mailService.sendMail(owner,"",-1,"系统","战场结果",sb.toString(),null,0,true);
                    log.info("GuildID["+r.id+"] Credit["+sCredit + r.getPoint2()+"]");
                }
            }
        }else{//场次小于100导致战场奖励公会荣誉小于1
        	for (; i < l.size(); i++) {
                BattleFieldRecord r = l.get(i);
                addGuildAward(r.id, 0, r.getPoint2());
            	//mengjie add
                StringBuilder sb = new StringBuilder(100);
                sb.append("你所在公会的在试练场获胜(");
                sb.append(r.winTimes);
                sb.append(")");
                sb.append("失败(");
                sb.append(r.loseTimes);
                sb.append(")");
                sb.append(".获得试炼场奖励公会荣誉:0");
                sb.append(".公会成员PK总次数获得荣誉:");
                sb.append(r.getPoint2());
                TongData tong = tongService.getTongData(r.id);
                int owner = tong.getTongOwner();
                mailService.sendMail(owner,"",-1,"系统","战场结果",sb.toString(),null,0,true);
                log.info("GuildID["+r.id+"] Credit["+sCredit + r.getPoint2()+"]");
            }
        }
        try {
            bbsService.addBbs(instance.getDefinition().getBbsId(), -1, "系统",
                              getBbsTitle(instance), getBbsMessage(l), 100);
        } catch (DataAccessException ex) {
            log.error(ex,ex);
        }
        totalCredit = battleCount*4*CREDIT_RIGHT;
        l = instance.getOrderedRecords();
        i = 0;
        for(i=0;i<l.size()&&i<10;i++){
            BattleFieldRecord r = l.get(i);
            if(r.getPoint()<=0)  //只有积分大于0的才参与排名
            	{
            	break;
            	}
            int money = 0;
            int credit = totalCredit * CREDIT_PERCENTS[i]/100+r.winTimes*2+r.loseTimes;
            addAward(r.id,money,credit,r.getPoint2());
            StringBuilder sb = new StringBuilder(100);
            sb.append("在公会试练场竞技中你的个人排名为:");
            sb.append(i+1);
            sb.append("积分为:");
            sb.append(r.winTimes*2-r.loseTimes);
            sb.append(".获胜(");
            sb.append(r.winTimes);
            sb.append(")");
            sb.append("失败(");
            sb.append(r.loseTimes);
            sb.append(")");
            sb.append(".获得荣誉:");
            sb.append(credit);
            sb.append(".");
            mailService.sendMail(r.id,"",-1,"系统","战场结果",sb.toString(),new byte[0],0,true);
            log.info("ID["+r.id+"] WinMoney["+money+"] WinCredit["+credit+"] Order["+i+"] Guild");
        }
        sCredit = Math.min(totalCredit / 100, 10);
        if(sCredit>0){
            for (; i < l.size(); i++) {
                BattleFieldRecord r = l.get(i);
                if (r.winTimes > 0){ //只有至少获胜过1场的才加荣誉
                    addAward(r.id, 0, sCredit,r.getPoint2());
                }else{
                	addAward(r.id, 0, 0,r.getPoint2());
                }
            }
        }

    }

    protected String getBbsTitle(GuildBattleFieldInstance instance){
        StringBuilder sb = new StringBuilder(1000);
//        sb.append(format.format(new Date(createTime)));
//        sb.append(",");
//        sb.append(instance.getBeginLevel());
//        sb.append("级~");
//        sb.append(instance.getEndLevel());
//        sb.append("级试练场,");
        sb.append(format1.format(new Date(createTime)));
        sb.append("~");
        sb.append(format1.format(new Date(endTime)));
        sb.append("公会试练场记录");
        return sb.toString();
    }

    protected String getBbsMessage(List<BattleFieldRecord> l){
        StringBuilder sb = new StringBuilder(1000);
        sb.append(format1.format(new Date(createTime)));
        sb.append(",");
        sb.append(format1.format(new Date(endTime)));
        sb.append("场次记录:\n");
        for(int i=0;i<l.size()&&i<10;i++){
            BattleFieldRecord record = l.get(i);
            if(record.getPoint()<=0)
                break;
            sb.append("第");
            sb.append(i+1);
            sb.append("名  ");
            sb.append(record.name);
            sb.append(".");
        }
        return sb.toString();
    }

    private void addAward(int id, int money, int credit,int point) {
        WorldPlayer player = playerService.getWorldPlayer(id);
        if (player != null) {
            synchronized (player) {
                Changed changed = new Changed();
                player.addCredit(credit, changed);
                player.addMoney(money, changed);
                if(point>0){
                    player.setContribution(player.getContribution()+point);
                    tongService.modifyPlayer(player);
                }
                battleService.getConnectService().sendGetItem(changed,
                        player.getId(), (byte) 22);
            }
        } else {
            boolean acquired = false;
            try {
                player = playerService.getWorldPlayerAndCatch(id);
//                playerService.acquire(player);
//                acquired = true;
                synchronized (player) {
                    player.addCredit(credit, null);
                    player.addMoney(money, null);
                    if (point > 0) {
                        player.setContribution(player.getContribution() + point);
                        tongService.modifyPlayer(player);
                    }
                }
            } catch (Exception ex) {
                log.error(ex, ex);
            } finally {
//                if (acquired)
                    playerService.releasePlayer(player);
            }
        }
    }

    private void addGuildAward(int id, int money, int credit) {
        TongData tong = tongService.getTongData(id);
        tong.addCredit(credit);
        tongService.saveTongData(tong);
//        WorldPlayer player = playerService.getWorldPlayer(tong.getTongOwner());
//        if (player != null) {
//            synchronized (player) {
//                Changed changed = new Changed();
//                player.addMoney(money, changed);
//                battleService.getConnectService().sendGetItem(changed,
//                        player.getId(), (byte) 22);
//            }
//        } else {
//            boolean acquired = false;
//            try {
//                player = playerService.loadWorldPlayer(tong.getTongOwner());
//                playerService.acquire(player);
//                acquired = true;
//                synchronized (player) {
//                    player.addMoney(money, null);
//                }
//            } catch (Exception ex) {
//                log.error(ex, ex);
//            } finally {
//                if (acquired)
//                    playerService.release(player);
//            }
//        }
    }

    public GameMap getGameMap(WorldPlayer player, short mapId) {
        if(!isValid)
            return null;
//        int index = (player.getLevel()-1)/10-1;
        if(instance.contains(player.getId())){
            if(failures.containsKey(player.getId())){
                return instance.getEntrance();
            }
            int count = instance.getPlayerByOldTong(player);
            //mengjie modify
            if(count==-1||count>10)
                return instance.getEntrance();
            return instance.getMap(mapId);
        }else{
            return instance.getEntrance();
        }
    }

    public GameMap getLoginMap(WorldPlayer player,short mapId){
        return getGameMap(player,mapId);
    }

    public Instance getInstance(IPlayerData player, int instanceId) {
        return null;
    }

    public void playerAddedToInstance(IPlayerData player, Instance instance) {
    }

    public Instance tryGotoInstance(int instanceId, WorldPlayer player, int battleID) throws
            InstanceException {
        if(!isValid){
            throw new InstanceException("战场还没有开放.");
        }
        if(!canEnter||!isValid){
            throw new InstanceException("战场现在不能进入.");
        }
        if(player.getTeam()!=null)
            throw new InstanceException("组队不能进入战场.");
        if(player.getLevel()<20)
            throw new InstanceException("进入战场的等级必须大于20级.");
        if(player.getTongId()<=0)
            throw new InstanceException("没有公会，不能进入战场");
        //mengjie add 入会1天内不允许打公会战场
        if((player.getTonginTime().getTime()+24*3600*1000) > new Date().getTime()){
        	throw new InstanceException("您在加入“"+player.getTongName()+"”24小时后，才能代表该公会出战。");
        }
        if(failures.containsKey(player.getId())){
            if(System.currentTimeMillis()>(failures.get(player.getId())+60*1000L)){
                failures.remove(player.getId());
            }else{
                throw new InstanceException("你现在处于虚弱状态,不能进入战场.");
            }
        }
        int money = player.getLevel()*30;
        if(player.getMoeny()<money){
            throw new InstanceException("没有足够的金钱");
        }
        if(player.getCredit()<50)
            throw new InstanceException("需要50点荣誉才能进入战场");
        instance.preAdd(new WorldPlayer[] {player});
        Changed changed = new Changed();
        player.decMoney(money, changed);
        int hp = player.getHp();
        int mp = player.getMp();
        int maxHp = player.getMaxHp();
        int maxMp = player.getMaxMp();
        player.setHp(maxHp);
        player.setMp(maxMp);
//        changed.addProperty(Changed.HP, maxHp - hp);
//        changed.addProperty(Changed.MP, maxMp - mp);
        changed.addProperty(Changed.HP, maxHp);
        changed.addProperty(Changed.MP, maxMp);
        battleService.getConnectService().sendGetItem(changed, player.getId(), (byte) 0);
        instance.addMoney(money);
//        waiting.put(player.getId(),System.currentTimeMillis());
        return instance;
    }


    public GuildBattleFieldInstance createInstance(InstanceDefinition idf){
        GameMap entrance = worldService.getNoInstanceMap(idf.getEntrance());
        GuildBattleFieldInstance ret = new GuildBattleFieldInstance(instanceService.getNewInstanceId(),idf,instanceService,tongService);
        ret.setEntrance(entrance);
        short[] maps = idf.getMaps();
        for(int j=0;j<maps.length;j++){
            Scene scene = worldService.getInstanceScene(maps[j]);
            GameMap map = new GameMap(worldService,scene,(short)0,(short)0);
            map.setCanCreateTeam(false);
            map.setCanPk(false);
            ret.addMap(map);
            map.setInstance(ret);
        }
        worldService.instanceCreated(ret);
        instance = ret;
        return ret;
    }


    public void enterfor(WorldPlayer player) throws BattleFieldException{
        if(!canEnterfor)
            throw new BattleFieldException("现在不能报名.");
        if(inBattles.contains(player.getId()))
            throw new BattleFieldException("正在战斗中,不能报名.");
        if(player.getCredit()<50){
            throw new BattleFieldException("没有足够的荣誉.");
        }
        if(instance.contains(player.getId())){
        	if(failures.containsKey(player.getId())){
                if(System.currentTimeMillis()>(failures.get(player.getId())+60*1000L)){
                    failures.remove(player.getId());
                }else{
                    throw new BattleFieldException("你现在处于虚弱状态,不能报名.");
                }
            }
            instance.register(player);
        }else
            throw new BattleFieldException("不属于此战场.");
    }

//    public void enter(WorldPlayer player) throws BattleFieldException{
//        int index = (player.getLevel()-1)/10-1;
//        if(instances[index].preAdd(new WorldPlayer[]{player})){
//
//        }
//    }

    public void run(){
        for(;;){
            checkStatus();
            if(isValid){
                if (instance != null) {
                    preSchedule(instance);
                    schedule(instance);
                }
            }
            try {
                Thread.sleep(10 * 1000L);
            } catch (InterruptedException ex1) {
            }
        }
    }

    public void checkStatus(){
        long current = System.currentTimeMillis();
        if(canEnter&&current>=forbidEnterTime){
            canEnter = false;
            chatService.sendMapMessage(idf.getEntrance(),-1,"系统","战场入口已经关闭");
            chatService.sendMapMessage(idf.getMaps()[0],-1,"系统","战场入口已经关闭");
        }
        if(canEnterfor&&current>=forbidTime){
            canEnterfor = false;
            int minutes = (int)(endTime-forbidTime+10*1000L)/(60*1000);
            chatService.sendMapMessage(idf.getMaps()[0],-1,"系统","战场已经停止报名"+minutes+"分钟后战场将关闭!");
        }
        if(isValid&&current>=endTime){
            isValid = false;
            cancel();
        }
    }

    protected void preSchedule(GuildBattleFieldInstance instance){
        long current = System.currentTimeMillis();
        ConcurrentHashMap<Integer,Long> waiting = instance.getWaiting();
        Iterator<Map.Entry<Integer,Long>> ite = waiting.entrySet().iterator();
        while(ite.hasNext()){
            Map.Entry<Integer,Long> entry = ite.next();
            if((current-entry.getValue())>60000L){
            	if(failures.containsKey(entry.getKey())){
                    if(System.currentTimeMillis()>(failures.get(entry.getKey())+60*1000L)){
                        failures.remove(entry.getKey());
                    }else{
                        continue;
                    }
                }
                instance.register(entry.getKey());
            }
        }
    }

    protected void schedule(GuildBattleFieldInstance instance){
        try {
            int[] ids = instance.getRegisteredPlayers();
            IntList l = new ArrayIntList(ids.length);
            for(int i=0;i<ids.length;i++){
                l.add(ids[i]);
            }
            while (l.size()>=2) {
                WorldPlayer p1 = findOnlinePlayer(l, -1, instance);
                if (p1 != null) {
                    WorldPlayer p2 = findOnlinePlayer(l,instance.getGuildId(p1),instance);
                    if (p2 != null) {
                        CreditPkBattle battle = battleService.startCreditBattle(p1, p2,5,instance,this);
                        instance.unRegister(p1.getId());
                        instance.unRegister(p2.getId());
                        inBattles.add(p1.getId());
                        inBattles.add(p2.getId());
                        battles.add(battle);
                    }
                } else {
                    break;
                }
            }
        } catch (Exception ex) {
            log.error(ex,ex);
        }
        finally{
        }
    }

    public String getBattleFieldInfo(WorldPlayer player){
        if(!isValid){
            return "战场处于关闭状态不能查询";
        }
//        return "";
        if(instance!=null){
            BattleFieldRecord self = instance.getGuildRecord(player.getTongId());
            if(self==null){
                return "你在战场中没有记录";
            }else{
                BattleFieldRecord[] first = instance.getFirstGuildRecord();
                StringBuilder sb = new StringBuilder(500);
                for (int i = 0; i < first.length; i++) {
                    if (first[i] == null)
                        break;
                    sb.append(i+1);
                    sb.append(".");
                    sb.append(first[i].name);
                    sb.append(" 胜");
                    sb.append(first[i].winTimes);
                    sb.append(" 负");
                    sb.append(first[i].loseTimes);
                    sb.append(".\n");
                }
                sb.append("所属公会的成绩:");
                sb.append(" 胜");
                sb.append(self.winTimes);
                sb.append(" 负");
                sb.append(self.loseTimes);
                sb.append(".");
                return sb.toString();
            }
        }else{
            return "战场处于关闭状态不能查询";
        }
    }

    public void battleEnded(CreditPkBattle battle) {
        synchronized(battles){
            battles.remove(battle);
        }
        IPlayerData failure = battle.getFailure();
        if (failure != null) {
            failures.put(failure.getId(), System.currentTimeMillis());
//            int index = (failure.getLevel() - 1) / 10 - 1;
            instance.addRecord(battle.getWinner(), failure);
            instance.addWaiting(battle.getWinner(),System.currentTimeMillis());
            log.info("GuildBattleEnded Winner[" + battle.getWinner().getId() +
                     "] Level[" + battle.getWinner().getLevel() + "]Failure[" +
                     failure.getId() + "] Level[" + battle.getFailure().getLevel() +
                     "]");

        }
        IPlayerData[] players = battle.getPlayers();
        inBattles.remove(players[0].getId());
        inBattles.remove(players[1].getId());
    }


    protected WorldPlayer findOnlinePlayer(IntList ids,int guildId,GuildBattleFieldInstance instance){
        for(int i=0;i<ids.size();i++){
            WorldPlayer player = playerService.getWorldPlayer(ids.get(i));
            if(player!=null&&instance.getGuildId(player)!=guildId){
                ids.removeElementAt(i);
                return player;
            }
        }
        return null;
    }

    public WorldService getWorldService() {
        return worldService;
    }

    public PlayerService getPlayerService() {
        return playerService;
    }

    public InstanceService getInstanceService() {
        return instanceService;
    }

    public void setBattleService(BattleService2 battleService) {
        this.battleService = battleService;
    }

    public void setWorldService(WorldService worldService) {
        this.worldService = worldService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setInstanceService(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    public BattleService2 getBattleService() {
        return battleService;
    }

    public void setChatService(ChatService chatService){
        this.chatService = chatService;
    }

    public void setBbsService(BbsService bbsService){
        this.bbsService = bbsService;
    }

    public void setMailService(MailService mailService){
        this.mailService = mailService;
    }

    public void setTongService(TongService tongService){
        this.tongService = tongService;
    }
}
