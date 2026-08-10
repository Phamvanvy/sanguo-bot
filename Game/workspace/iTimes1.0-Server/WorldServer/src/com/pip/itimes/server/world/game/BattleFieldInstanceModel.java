package com.pip.itimes.server.world.game;

import java.util.*;

import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.util.IntHashSet;
import com.pip.itimes.server.world.*;
import com.pip.itimes.server.world.battle.BattleService2;
import com.pip.itimes.server.world.battle.CreditPkBattle;
import org.apache.log4j.Logger;
import java.text.SimpleDateFormat;
import com.pip.itimes.server.dao.*;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;

/**

 * @author Jeffrey
 * @version 1.0
 */
public class BattleFieldInstanceModel implements BattleInstanceModel,Runnable{

    private static final Logger log = Logger.getLogger(BattleFieldInstanceModel.class);

    private volatile boolean isValid;

    private WorldService worldService;
    private InstanceService instanceService;
    private PlayerService playerService;
    private BattleService2 battleService;
    private ChatService chatService;
    private BbsService bbsService;
    private MailService mailService;

    private BattleFieldInstance[] instances;

    private Set<CreditPkBattle> battles = new HashSet<CreditPkBattle>();
    private Map<Integer,Long> failures = new HashMap<Integer,Long>();
    private IntHashSet inBattles = new IntHashSet();

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

    public BattleFieldInstanceModel() {
//        this.worldService = worldService;
//        this.instanceService = instanceService;
//        this.playerService = playerService;
//        this.battleService = battleService;
//        createInstances(instanceService.getInstanceDefinition(95001));
        new Thread(this).start();
    }

    private void createInstances(){
        idf = instanceService.getInstanceDefinition(95003);
        instances = createInstances(idf);
    }


    public void start(long forbidEnterTime,long forbidTime,long endTime) throws BattleFieldException{
        if(isValid)
            throw new BattleFieldException("还有战场没有关闭");
        createInstances();
        log.info("BattleField Started");
        isValid = true;
        canEnterfor = true;
        canEnter = true;
        createTime = System.currentTimeMillis();
        this.forbidEnterTime = forbidEnterTime;
        this.forbidTime = forbidTime;
        this.endTime = endTime;
        chatService.sendSystemMessage(format1.format(new Date(createTime))+"~"+format1.format(new Date(endTime))+"的个人试练场已经开放");
    }

    public void forceStop(){
        isValid = false;
        cancel();
    }

    public void cancel(){
        List<CreditPkBattle> cBattles = new ArrayList<CreditPkBattle>(battles);
        synchronized(battles){

        }
        for(CreditPkBattle battle:cBattles){
            battle.cancel();
        }
        for(int i=0;i<instances.length;i++){
        	if(instances[i]!=null){
	           int[] ids = instances[i].getActives();
	           for(int j=0;j<ids.length;j++){
	               WorldPlayer player = playerService.getWorldPlayer(ids[j]);
	               if(player!=null){
	                   GameMap map = player.getMap();
	                   if(map!=null&&map.getInstance()==instances[i]){
	                       player.getMap().removePlayer(player,true);
	                       player.setMap(null);
	                   }
	               }
	           }
        	}
        }
        for(int i=0;i<instances.length;i++){
        	if (instances[i] != null){
        		instanceEnded(instances[i]);
                worldService.instanceRemoved(instances[i]);
        	}
        }
        instances = null;

    }

    protected void instanceEnded(BattleFieldInstance instance){
//        int attendedCount = instance.getAttendedCount();
        IItem item = Items.getTemplate(550016).newInstance();
        byte[] itemBytes = ItemUtils.item2dbAttachment(item,1);
        int battleCount = instance.getBattleCount();
        if(battleCount<=0)
            return;
//        int totalMoney = attendedCount*((instance.getBeginLevel()-1)/10)*10;
        int totalCredit = battleCount*10 * CREDIT_RIGHT;
        List<BattleFieldRecord> l = instance.getOrderedRecords();
        int i = 0;
        for(i=0;i<l.size()&&i<10;i++){
            BattleFieldRecord r = l.get(i);
            if(r.getPoint()<=0)  //只有积分大于0的才参与排名
                break;
            int money = (int)((long)(instance.getMoney() * 60 * MONEY_PERCENTS[i])/10000);
            int credit = totalCredit * CREDIT_PERCENTS[i]/100+r.winTimes*2+r.loseTimes;
            addAward(r.id,money,credit);
            StringBuilder sb = new StringBuilder(100);
            sb.append("你的战场排名为:");
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
            sb.append(".获得金钱:");
            sb.append(money);
            sb.append("J");
            mailService.sendMail(r.id,r.name,-1,"系统","战场结果",sb.toString(),itemBytes,0,true);
            log.info("ID["+r.id+"] WinMoney["+money+"] WinCredit["+credit+"] Order["+i+"]");
        }
        log.info("BattleFieldEnded BeginLevel[" + instance.getBeginLevel() +
                 "] EndLevel[" + instance.getEndLevel() + "]TotalMoney[" +
                 instance.getMoney() + "] TotalCredit[" + totalCredit +
                 "] BattleCount[" + battleCount + "] PlayerCount[" +
                 instance.getAttendedCount() + "]");
        int credit = Math.min(totalCredit / 100, 10);
//        if(credit>0){
            for (; i < l.size(); i++) {
                BattleFieldRecord r = l.get(i);
                if (r.winTimes > 0){ //只有至少获胜过1场的才加荣誉
                    addAward(r.id, 0, credit + r.winTimes * 2 + r.loseTimes);
                    mailService.sendMail(r.id,r.name,-1,"系统","战场结果","战场获胜奖励",itemBytes,0,true);
                }else{
                    addAward(r.id,0,r.winTimes*2+r.loseTimes);
                }
            }
//        }
        try {
            bbsService.addBbs(instance.getDefinition().getBbsId(), -1, "系统",
                              getBbsTitle(instance), getBbsMessage(l), 100);
        } catch (DataAccessException ex) {
            log.error(ex,ex);
        }
    }

    protected String getBbsTitle(BattleFieldInstance instance){
        StringBuilder sb = new StringBuilder(1000);
        sb.append(format.format(new Date(createTime)));
        sb.append(",");
        sb.append(instance.getBeginLevel());
        sb.append("级~");
        sb.append(instance.getEndLevel());
        sb.append("级试练场,");
        sb.append(format1.format(new Date(createTime)));
        sb.append("~");
        sb.append(format1.format(new Date(endTime)));
        sb.append("场次记录");
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

    private void addAward(int id, int money, int credit) {
        WorldPlayer player = playerService.getWorldPlayer(id);
        if (player != null) {
            synchronized (player) {
                Changed changed = new Changed();
                player.addCredit(credit, changed);
                player.addMoney(money, changed);
                battleService.getConnectService().sendGetItem(changed,
                        player.getId(), (byte) 22);
            }
        } else {
            try {
                player = playerService.getWorldPlayerAndCatch(id);
                synchronized (player) {
                    player.addCredit(credit, null);
                    player.addMoney(money, null);
                }
            } catch (Exception ex) {
                log.error(ex, ex);
            } finally {
            	playerService.releasePlayer(player);
            }
        }
    }

    public GameMap getGameMap(WorldPlayer player, short mapId) {
        if(!isValid)
            return null;
        int index = (player.getLevel()-1)/10-1;
        if(instances[index].contains(player.getId())){
            if (failures.containsKey(player.getId())) {
                return instances[index].getEntrance();
            }
            return instances[index].getMap(mapId);
        }else{
            return instances[index].getEntrance();
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

    public Instance tryGotoInstance(int instance, WorldPlayer player, int battleID) throws
            InstanceException {
        if(!isValid){
            throw new InstanceException("战场还没有开放.");
        }
        if(!canEnter||!isValid){
            throw new InstanceException("战场现在不能进入.");
        }
        if(player.getTeam()!=null)
            throw new InstanceException("组队不能进入战场.");
        if(player.getLevel()<21)
            throw new InstanceException("进入战场的等级必须大于20级.");
        if(failures.containsKey(player.getId())){
            if(System.currentTimeMillis()>(failures.get(player.getId())+60*1000L)){
                failures.remove(player.getId());
            }else{
                throw new InstanceException("你现在处于虚弱状态,不能进入战场.");
            }
        }
        int money = player.getLevel()*10;
        if(player.getMoeny()<money){
            throw new InstanceException("没有足够的金钱");
        }
//        if(player.getCredit()<5)
//            throw new InstanceException("没有足够的荣誉");
        int index = (player.getLevel()-1)/10-1;
        instances[index].preAdd(new WorldPlayer[] {player});
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
        instances[index].addMoney(money);
        return instances[index];
//        }else{
//            throw new InstanceException("不能进入战场.");
//        }
    }

    public BattleFieldInstance[] createInstances(InstanceDefinition idf){
        GameMap entrance = worldService.getNoInstanceMap(idf.getEntrance());
        BattleFieldInstance[] ret = new BattleFieldInstance[9];
        for(int i=2;i<10;i++){
            int id = instanceService.getNewInstanceId();
            BattleFieldInstance instance = new BattleFieldInstance(id,idf,instanceService,i*10+1,(i+1)*10);
            instance.setEntrance(entrance);
            short[] maps = idf.getMaps();
            for(int j=0;j<maps.length;j++){
                Scene scene = worldService.getInstanceScene(maps[j]);
                GameMap map = new GameMap(worldService,scene,(short)0,(short)0);
                map.setCanCreateTeam(false);
                map.setCanPk(false);
                instance.addMap(map);
                map.setInstance(instance);
            }
            worldService.instanceCreated(instance);
            ret[i-1] = instance;
        }
        return ret;
    }


    public void enterfor(WorldPlayer player) throws BattleFieldException{
        if(!canEnterfor)
            throw new BattleFieldException("现在不能报名.");
        if(inBattles.contains(player.getId()))
            throw new BattleFieldException("正在战斗中,不能报名.");
//        if(player.getCredit()<5){
//            throw new BattleFieldException("没有足够的荣誉.");
//        }
        int index = (player.getLevel()-1)/10-1;
        if(instances[index].contains(player.getId())){
            instances[index].register(player);
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
                if (instances != null) {
                    for (int i = 0; i < instances.length; i++) {
                    	if(instances[i]!=null)
                    		schedule(instances[i]);
                    }
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

    protected void schedule(BattleFieldInstance instance){
        try {
            int[] ids = instance.getRegisteredPlayers();
            for (; ; ) {
                WorldPlayer p1 = findOnlinePlayer(ids, current, instance);
                if (p1 != null) {
                    WorldPlayer p2 = findOnlinePlayer(ids, current, instance);
                    if (p2 != null) {
                        CreditPkBattle battle = battleService.startCreditBattle(p1, p2,5,instance,this);
                        instance.unRegister(p1.getId());
                        instance.unRegister(p2.getId());
                        inBattles.add(p1.getId());
                        inBattles.add(p2.getId());
                        battles.add(battle);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (Exception ex) {
            log.error(ex,ex);
        }
        finally{
            current = 0;
        }
    }

    public String getBattleFieldInfo(WorldPlayer player){
        if(!isValid){
            return "战场处于关闭状态不能查询";
        }
        if(instances!=null){
            int index = (player.getLevel()-1)/10-1;
            BattleFieldRecord self = instances[index].getRecord(player.getId());
            if(self==null){
                return "你在战场中没有记录";
            }else{
                BattleFieldRecord[] first = instances[index].getFirstRecord();
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
                sb.append("你的成绩:");
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
            int index = (failure.getLevel() - 1) / 10 - 1;
            instances[index].addRecord(battle.getWinner(), failure);
            log.info("CreditBattleEnded Winner[" + battle.getWinner().getId() +
                     "] Level[" + battle.getWinner().getLevel() + "]Failure[" +
                     failure.getId() + "] Level[" + battle.getFailure().getLevel() +
                     "]");
        }
        IPlayerData[] players = battle.getPlayers();
        inBattles.remove(players[0].getId());
        inBattles.remove(players[1].getId());
    }

    private int current = 0;

    protected WorldPlayer findOnlinePlayer(int[] ids,int index,BattleFieldInstance instance){
        if(index>ids.length-1)
            return null;
        for(int i=index;i<ids.length;i++){
            WorldPlayer player = playerService.getWorldPlayer(ids[i]);
            if(player!=null){
                current = i+1;
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
}
