package com.pip.itimes.server.world.battle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.pip.itimes.net.*;
import com.pip.itimes.server.bean.Master;
import com.pip.itimes.server.camp.CampConfig;
import com.pip.itimes.server.camp.CampData;
import com.pip.itimes.server.camp.CampSkillData;
import com.pip.itimes.server.camp.CampSkillLevel;
import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.util.IDGenerator;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.Discount;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.MercenaryService;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.PositionService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.ItemGroup.BossBattlePlayer;
import com.pip.itimes.server.world.ItemGroup.BossBattleTop;
import com.pip.itimes.server.world.battle.Battle2.STATUS;
import com.pip.itimes.server.world.boss.BossDefine;
import com.pip.itimes.server.world.boss.BossDefineLoader;
import com.pip.itimes.server.world.boss.WorldBoss;
import com.pip.itimes.server.world.game.*;
import com.pip.itimes.server.world.worldboss.WorldBossConfig;

import org.apache.log4j.Logger;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ClientBattle2 extends Battle2 implements ILockOwner {

    private Logger log = Logger.getLogger(ClientBattle2.class);

    private int mgId;
    private int teamId;
    private int serial;
    private Monster[] monsters;
    private ILock lock;
    private static final int MAX_IDLE = 3;
    private PlayerService playerService;
    private PositionService positionService;
    private boolean isBossRush;
    private boolean isWorldBoss;

    public ClientBattle2(int id, BattleService2 service,
                         BattleStrategy strategy, IPlayerData[] players,
                         Monster[] monsters, int mgId, int teamId, int serial,
                         PlayerService playerService, PositionService positionService,boolean isBossRush, boolean isWorldBoss) {
        super(id,service,strategy);
        this.isBossRush = isBossRush;
        this.isWorldBoss = isWorldBoss;
        init(players,monsters);
        this.playerService = playerService;
        this.positionService = positionService;
        this.monsters = monsters;
        this.mgId = mgId;
        this.teamId = teamId;
        this.serial = serial;
        for(int i = 0; i < side2.length; i++){
        	if(side2[i] != null){
        		side2[i].setGroupId(mgId);
        	}
        }
    }

    protected void init(IPlayerData[] players,Monster[] monsters){
        side1 = new BattleSprite[players.length];
        pet1 = new BattleSprite[players.length];
        for(int i = 0; i < players.length; i++){
            side1[i] = initPlayer(players[i],isBossRush, isWorldBoss);
            pet1[i] = initPet(players[i], side1[i],0,isBossRush, isWorldBoss);
        }
        side2 = new BattleSprite[monsters.length];
        pet2 = new BattleSprite[monsters.length];
        for(int i = 0; i < monsters.length; i++){
            side2[i] = initMonster(monsters[i],isBossRush, isWorldBoss);
        }
    }

    public IPlayerData[] getPlayers(){
    	IPlayerData[] ret = new IPlayerData[side1.length];
        for(int i=0;i<side1.length;i++){
            ret[i] = side1[i].player;
        }
        return ret;
    }


    public BattleSprite getSprite(int spriteType, int spriteIndex) {
        BattleSprite result = null;

        switch (spriteType) {
            case BattleSprite.TYPE_PLAYER:
                result = side1[spriteIndex];

                break;
            case BattleSprite.TYPE_PLAYER_PET:
                result = pet1[spriteIndex];

                break;
            case BattleSprite.TYPE_MONSTER:
                result = side2[spriteIndex];

                break;
            case BattleSprite.TYPE_MONSTER_PET:
                result = pet2[spriteIndex];

                break;
        }

        return result;

    }


    public void abort() {
        for (int i = 0; i < side1.length; i++) {
            sendAbort(side1[i], pet1[i], serial);
        }
        battleOver = false;
        cancel();
        status = STATUS.end;
        service.removeBattle(this);
    }

    public void cancel(){
        if(lock!=null)
            lock.cancel(this);
    }

    public void release() throws LockException{
        if(lock!=null){
            lock.release(this,true);
        }
    }

    public synchronized void doTime(long time) {
        if(status==STATUS.end||status==STATUS.init)
            return;
        long t = time - lastTime;
        
        boolean side2Error = true;
        for(int i = 0; i < side2.length; i++){
        	if(side2[i] != null){
        		side2Error = false;
        		break;
        	}
        }
        if(side2Error){
        	for (int i = 0; i < side1.length; i++) {
                sendAbort(side1[i], pet1[i],serial);
                log.info("ID[" + side1[i].id +
                        "] name[" + side1[i].name + "]ClientBattle2-abort");
            }
            abort();
            return;
        }
        
        if(status == STATUS.wait_start && t >= 60 * 1000L){
            for (int i = 0; i < side1.length; i++) {
                sendAbort(side1[i], pet1[i],serial);
            }
            abort();
        }else if(status == STATUS.wait_fight && t >= 70 * 1000L){
        	boolean success = true;
            for(int i = 0; i < side1.length; i++){
            	
                if(!side1[i].ready){
                    side1[i].idleRound++;
                    if(side1[i].idleRound>=MAX_IDLE){
                        if(!side1[i].testCannotBattle()){
                            side1[i].setSkill(Skill.STAY_SKILL);
                            side1[i].changeHp( -side1[i].attributes[BattleSprite.
                                            ATTR_HPMAX], battleMovie, this);
                        }
                        if(pet1[i]!=null&&!pet1[i].testCannotBattle()){
                            pet1[i].setSkill(Skill.STAY_SKILL);
                            pet1[i].changeHp( -pet1[i].attributes[BattleSprite.
                                            ATTR_HPMAX], battleMovie, this);
                        }
                        success = false;
                        break;
                    }else{
                        if(side1[i].canAction()){
                            side1[i].setSkill(Skill.ATTACK_SKILL);
                            side1[i].setTarget(side2[0], 0);
                        }
                        if(pet1[i]!=null&&pet1[i].canAction()){
                            pet1[i].setSkill(Skill.ATTACK_SKILL);
                            pet1[i].setTarget(side2[0], 0);
                        }
                    }

                    side1[i].ready = true;
                }
            }
            if(!success){  //将所有玩家的技能置为带着，并全部血量为0
            	 for(int i = 0; i < side1.length; i++){
		             if(!side1[i].testCannotBattle()){
		                 side1[i].setSkill(Skill.STAY_SKILL);
		                 side1[i].changeHp( -side1[i].attributes[BattleSprite.
		                                 ATTR_HPMAX], battleMovie, this);
		             }
		             if(pet1[i]!=null&&!pet1[i].testCannotBattle()){
		                 pet1[i].setSkill(Skill.STAY_SKILL);
		                 pet1[i].changeHp( -pet1[i].attributes[BattleSprite.
		                                 ATTR_HPMAX], battleMovie, this);
		             }
            	 }
            }
          //所有玩家置为死亡，直接判负
            try {
                battleBout(side1, side2, pet1, pet2, round);
                roundEnd();
                clearBourt();
            } catch (Exception ex) {
                caughtException(ex);
            }
        }

    }

    public void catchToBattle(int playerId,int serial){
        if(status!=STATUS.end){
            UWAPSegment seg = getCatchBattleSegment(serial);
            service.getConnectService().writeTo(seg,playerId);
        }
    }

    public WorldPlayer[] getAliveTeam(){
        List l = new ArrayList(3);
        for(int i = 0; i < side1.length; i++){
            if(!side1[i].testCannotBattle() && (side1[i].player != null && side1[i].player instanceof WorldPlayer)){
                l.add(side1[i].player);
            }
        }
        WorldPlayer[] ret = new WorldPlayer[l.size()];
        l.toArray(ret);
        return ret;
    }
    
    public WorldPlayer[] getWorldPlayers(){
    	List l = new ArrayList(3);
    	for(int i = 0; i < side1.length; i++){
    		if(side1[i].player != null && side1[i].player instanceof WorldPlayer){
    			l.add(side1[i].player);
    		}
    	}
    	WorldPlayer[] ret = new WorldPlayer[l.size()];
    	l.toArray(ret);
    	return ret;
    }
    
    public int getTeamAverageLevel(){
        int level = 0;
        for(int i = 0; i < side1.length; i++){
            level += side1[i].level;
        }
        return level / side1.length;
    }

    public IPlayerData[] getTeam(){
    	IPlayerData[] ret = new IPlayerData[side1.length];
        for(int i=0;i<ret.length;i++){
            ret[i] = side1[i].player;
        }
        return ret;
    }


    public boolean isPetDied(Pet p){
        for(int i=0;i<pet1.length;i++){
            if(pet1[i]!=null&&pet1[i].pet==p){
                return pet1[i].getDebufStatus()==Skill.STATUS_DIE;
            }
        }
        return false;
    }

    public Monster getCatchedMonster(PlayerData player){
        for(int i=0;i<side1.length;i++){
            if(side1[i]!=null&&side1[i].player==player){
                if(side1[i].catchedPet==null)
                    return null;
                return side1[i].catchedPet.monster;
            }
        }
        return null;
    }


    public void end() {
        checkDie();
        status = STATUS.end;
        lastTime = System.currentTimeMillis();
        
        if(isWorldBoss){
        	int hurt = 0;
            for(int i = 0; i < side2.length; i++){
            	if(side2[i] != null){
            		hurt += side2[i].hurted;
            	}
            }
            WorldBossConfig.addPlayerRoundHurt(getWorldPlayers(), hurt);
        }

        byte  endByte = getRoundStatus();
        if (endByte == 1) { //胜利
            WorldPlayer[] players = getAliveTeam();
            if (players.length > 0) {
                List l = new ArrayList(3);
                FallResult[] fallResults = com.pip.itimes.server.world.game.FallCalculator.getFallItems(
                        players, monsters, side1.length,
                        getTeamAverageLevel(), l);
                
                if (l.size() > 0) {
                	IItem[] item = new IItem[l.size()];
                    l.toArray(item);
//                        for (int i = 0; i < l.size(); i++) {
//                            IEquipment equ = (IEquipment) l.get(i);
                    service.getFallService().addFalls(getTeam(), item);
                }
                
                int monsterLevel = 100;
                for(int i=0; i<monsters.length; i++){
                	if(monsters[i].getLevel() < monsterLevel){
                		monsterLevel = monsters[i].getLevel();
                	}
                }
                
//                        }
                for (int i = 0; i < players.length; i++) {
                    synchronized (players[i]) {
                    	 short mgMapID = (short)(mgId>>16);
                         if(players[i].getMapId() != mgMapID && !isBossRush){
                        	 boolean bReturn = true;
                         	if(service.getBossService().isWorldBoss(mgId)){
                 				bReturn = false;
                 				Utils.log(log, players[i].getId(), ClientConstants.BATTLE_ROUND_END,
             	            			"MgId[" + mgId + "] BattleError playerMapID != mgMapID but is WorldBoss");
                         	}
                         	if(bReturn){
                         		Utils.log(log, players[i].getId(), ClientConstants.BATTLE_ROUND_END,
                                     "MgId[" + mgId + "] BattleError playerMapID != mgMapID");
                         		break;
                         	}
                         }
                    	if(Math.abs(players[i].getLevel() - monsterLevel) < 10){
	                    	//若是他人的徒弟
	                    	if(playerService.getMasetService().isPrentice(players[i])){
	                    		Master mt = playerService.getMasetService().getMasterRelation(players[i]);
	                    		if(mt != null){
	                    			for(int j=0; j<players.length; j++){
	                    				if(players[j].getId() == mt.getMasterId()){
	                    					//每次加一点亲密度
	                    					mt.setIntimacy(mt.getIntimacy() + 1);
	                    				}
	                    			}
	                    		}
	                    	}
                    	}
                    	
                    	Pet petflag = null;
                        Changed changed = players[i].addFallResult(
                                fallResults[
                                i], players[i].getClientDataVersion());

                        if(fallResults[i].getExp() > 0){
                            players[i].addKills(1);
                        }
                        
                        //广东移动 特殊处理 毒瘤！
//                        String cityname = players[i].getcityname();
//                        if ((Server.iMoneyType == Server.IMONEY_TYPE_CMCC) && 
//                        		Server.CMCC_guangdong_cityname.contains(cityname)){
//                        if ((players[i].Cmcc_list.equals("124328141") || players[i].Cmcc_list.equals("138046130") || players[i].Cmcc_list.equals("94034796"))
//                        		&& (Server.iMoneyType == Server.IMONEY_TYPE_CMCC)){
//                        	IItemTemplate itemtemplate = Items.getTemplate(200884);			// 酷夏宝盒
//                    		if (itemtemplate != null) {
//                    			IItem item_tmp = players[i].completeAddItem(itemtemplate.
//        	                            newInstance(), 1, changed, players[i].getClientDataVersion());
//                    		}
//                    		Random rnd = new Random();
//                    		int count = Utils.getCount(rnd,1,7);
//                    		if (count == 1){
//                    			IItemTemplate itemtem1 = Items.getTemplate(200629);//字符“幸”
//                    			if (itemtem1 != null) {
//            	                    IItem item = players[i].completeAddItem(itemtem1.
//            	                            newInstance(), 1, changed, players[i].getClientDataVersion());
//                        		}
//                    		}else if (count == 3){
//                    			IItemTemplate itemtem1 = Items.getTemplate(200630);//字符“运”
//                    			if (itemtem1 != null) {
//            	                    IItem item = players[i].completeAddItem(itemtem1.
//            	                            newInstance(), 1, changed, players[i].getClientDataVersion());
//                        		}
//                    		}else if (count == 5){
//                    			IItemTemplate itemtem1 = Items.getTemplate(200631);//字符“寻”
//                    			if (itemtem1 != null) {
//            	                    IItem item = players[i].completeAddItem(itemtem1.
//            	                            newInstance(), 1, changed, players[i].getClientDataVersion());
//                        		}
//                    		}else if (count == 7){
//                    			IItemTemplate itemtem1 = Items.getTemplate(200632);//字符“宝”
//                    			if (itemtem1 != null) {
//            	                    IItem item = players[i].completeAddItem(itemtem1.
//            	                            newInstance(), 1, changed, players[i].getClientDataVersion());
//                        		}
//                    		}
//                        }
                        
                        Pet p = players[i].getPet();
                        if (p != null && p.getFavor() > 30) {
                            if (!isPetDied(p)) {
                                int exp = Utils.getPetExp(monsters, p,players[i]);
                                int petoldlevel = p.getLevel();
                                //用于附加可配置经验加成
                                players[i].tryAddPetExp(exp * Discount.EXPADDPERCENT/100, changed);
                                if ((petoldlevel != p.getLevel()) && (p.getLevel() == 10)){
                                	service.getChatService().sendPrivateMessage(-1,"系统",p.getId(),
        									"恭喜您的宠物“"+ p.getName() +
        									"”升到10级，它已获得双倍经验时间，直到它健康成长到30级！");
                                }
//                                    players[i].removeFavor(p, 3, changed,
//                                            new Random());
                              //mengjie add 宠物升级
                                if((changed.getPetproperty(Changed.PET_LEVEL) > 0) &&
                                		(players[i].getLevel() <= 25)){
                                	String level_tmp = "";
                                	if (players[i].getPet().getLevel()<=10){
                                		level_tmp = "1";
                                	}else{
                                		level_tmp = String.valueOf(players[i].getPet().getLevel()-10);
                                	}
                                	service.getChatService().sendPrivateMessage(-1,"系统",players[i].getId(),
									"您的宠物现在是"+ players[i].getPet().getLevel() +
									"级，只有与" + level_tmp +
									/*"至" + String.valueOf(players[i].getPet().getLevel()+5) +
									"等级范围内的怪物作战才可以让您的宠物获得经验哦。"*/
									"级以上的怪物作战才可以让您的宠物获得经验哦。");
                                }
                            } else {
								try {
									HouseInstanceModel houseModel = service.getHouseInstanceModel();
									ChatService chatService = service.getChatService();
									HouseData hdwaiterPet = houseModel.getHouseByPlayerId(players[i].getId());
									if (hdwaiterPet == null || hdwaiterPet.isUsedWaiter() == false) {
										//added by Jeremy:无管家或管家过期并无自动续买时
										Utils.removePetFavor(players[i], p, false, changed);
									} else {
    	                   				Random rnd = new Random();
    	                   				Utils.removePetFavor(players[i], p, false, changed);
    	                   				if(p.getFavor() <= 17){
    	                   					if (Utils.hit(rnd, 5000, 10000)) {				//宠物将要逃跑
    	                   						chatService.sendPrivateMessage(-1,
    	                   								"系统", players[i].getId(),
    	                   								"你的管家英勇的把你准备逃跑的宠物"
    	                   								+ p.getName()
    	                   								+ "捉回来了");
    	                   	            	}
    	                   				}
//										Pet[] pets = players[i].removeLimitPetFavor(1, changed, rnd);
//										if (pets != null || pets.length > 0) {
//											for (int petIndex = 0; petIndex < pets.length; petIndex++) {
//												if (pets[petIndex] != null) {
//													Pet pet2 = pets[petIndex];
//													chatService.sendPrivateMessage(-1,
//															"系统", players[i].getId(),
//															"你的管家英勇的把你准备逃跑的宠物"
//															+ pet2.getName()
//															+ "捉回来了");
//												}
//											}
//										}
									}
								} catch (Exception e) {
									//e.printStackTrace();
									Utils.log(log, players[i].getId(), ClientConstants.BATTLE_ROUND_END, "扣除宠物忠诚度出错");
								}
                            }
                        }
                        //ItemUtils.removeDurability(players[i], false,
                                //changed);
                        if (players[i].getMap() != null && players[i].getMap().getInstance() != null
                    			&& players[i].getMap().getInstance() instanceof CampBattlefieldInstance) {
                        } else {
                    		ItemUtils.removeDurability(players[i], false, changed);
                        }
                        /*boolean[] allDownDurability = ItemUtils.getAllDownDuragbility(players[i]);
                        
                        for(int k= 0; k < allDurability.length; k++ ){
                        	if(allDurability[k]){//需要发聊的无论是发一次还是每次都发
                        		if(allDownDurability[k]){//满走先前》5现在《5，或一直是0
                        			Grid grid = players[i].getLimitUsedEquipments(k);
                            		IEquipment iEquipment=(IEquipment) grid.item;
                            		//如果是过期则不发私聊 
                            		if(iEquipment != null && (new Date()).getTime() > iEquipment.getFAILURE_TIME()){//当日已超过过期日期
                            			if (iEquipment.getFAILURE_TIME() != -1){
                            				continue;
                            			}
                            		}
                            		service.getChatService().sendPrivateMessage(-1, "系统", players[i].getId(), "你的装备"+iEquipment.getName()+"耐久度为"
                            				+ iEquipment.getCurrentDurability() + "，为了你的正常使用请拿去修理");
                        		}
                        	}
                        }*/
                        Monster m = getCatchedMonster(players[i]);
                        if (m != null && players[i].getLevel() > 10 &&
                            m.getLevel() > 10 && m.getPetType() != 0)
                            if (!Utils.hit(m.getBabyRate()*4, 1000000)) {
//                        	if (false){
                                Pet pet = new Pet();
                                pet.setId(IDGenerator.getPetId());
                                pet.setPetType(m.getPetType());
                                pet.setBaby(false);
                                Utils.initPet(pet, 4 * m.getLevel(), 0,m.getLevel());
                                pet.setLevel(m.getLevel());
                                pet.setItemId(m.getStageId() << 16 |
                                              m.getIndex());
                                pet.setCurrentPoint(0);
                                pet.setExp(0);
                                pet.setFavor(50);
//                                pet.setPoint(20);
                                pet.setHp(pet.getMaxHp());
                                pet.setMp(pet.getMaxMp());
                                Ability[] abs = Utils.getPetAbilities(
                                        pet.
                                        getPetType());
                                for (int j = 0; j < abs.length; j++) {
                                    pet.addAbility(abs[j]);
                                }
                                players[i].addPet(pet, changed);
                                petflag = pet;
                            } else {
                                Pet pet = new Pet();
                                pet.setId(IDGenerator.getPetId());
                                pet.setPetType(m.getPetType());
                                pet.setBaby(true);
                                Utils.initPet(pet,
                                              Utils.getBabyPetPoint(m.
                                        getLevel()),
                                              Utils.getBabyPetAddedPoint(m.
                                        getLevel()) , 1);
                                pet.setLevel(1);
                                pet.setItemId(m.getStageId() << 16 |
                                              m.getIndex());
                                pet.setCurrentPoint(0);
                                pet.setExp(0);
                                pet.setFavor(50);
                                pet.setHp(pet.getMaxHp());
                                pet.setMp(pet.getMaxMp());
                                Ability[] abs = Utils.getPetAbilities(
                                        pet.
                                        getPetType());
                                for (int j = 0; j < abs.length; j++) {
                                    pet.addAbility(abs[j]);
                                }
                                players[i].addPet(pet, changed);
                                petflag = pet;
                            }
                        if(players.length>1){
//                            int totalLevel = 0;
//                            for (int k = 0; k < monsters.length; k++) {
//                                totalLevel += monsters[k].getLevel();
//                            }
//                            int avgLevel = totalLevel / monsters.length;
//                            for (int j = 0; j < players.length; j++) {
//                                if (i != j) {
//                                    int avgLevel1 = (players[i].getLevel() +
//                                            players[j].getLevel()) / 2;
//                                    if (Math.abs(avgLevel - avgLevel1) <= 5) {
//                                        players[i].tryAddFriendFavorite(players[
//                                                j], 1);
//                                    }
//                                }
//                            }
                            players[i].tryAddFriendFavorite(players,1);
                        }
                        service.getBufService().checkBattleBuff(players[i],changed);
                        service.getConnectService().sendGetItem(changed, players[i].getId(),
                                    (byte) 1);
                        service.getTongService().modifyPlayer(players[i]);
                        service.checkLevelChangedAndSendTips(players[i],changed,players[i].getId());
                        
                        Utils.log(log, players[i].getId(),
                                  ClientConstants.BATTLE_ROUND_END,
                                  "MgId[" + mgId +
                                  "]SubType[end]Changed[" +
                                  Utils.getHexdump(changed.toBytes()) +
                                  "] Total Money[" + players[i].getMoeny() +
                                  "] Total Credit[" + players[i].getCredit() +
                                  "]");
                        //mengjie add
                        if (petflag != null){
                        	//判断新版本客户端，同步宠物装备信息
                            try{
                            	UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
                            	seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
                            	seg.writeInt(petflag.getId());
                            	Grid[] usedEquipmentsTemp = petflag.getUsedEquipments();
                        		if (usedEquipmentsTemp != null){
                        			for (int jj = 0;jj<petflag.getUsedEquipmentinfo().length;jj++){
                        				seg.write((byte) petflag.getUsedEquipmentinfo()[jj]);
                        				if (usedEquipmentsTemp[jj] != null){
                        					if (petflag.getUsedEquipmentinfo()[jj] == 1){
                        						IEquipment equtmp = (IEquipment) usedEquipmentsTemp[jj].item;
                        						equtmp.setDataVersion(players[i].getClientDataVersion());
                        						seg.write(equtmp.toClientBytesWithLevel(players[i].getLevel()));
                        					}
                        				}
                        			}
                        		}else{
                        			for (int jj = 0;jj<petflag.getUsedEquipmentinfo().length;jj++){
                        				seg.write((byte) petflag.getUsedEquipmentinfo()[jj]);
                        			}
                        		}
                        		// 发送宠物升级所需升级经验
                        		seg.writeInt(Utils.getPetUpLevelExp(petflag.getLevel()));
                        		
                        		//发送宠物阵营宝石效果
                        		List petbuf = petflag.getpetBuf();
                        		int value = 0;
                        		if(petbuf != null){
                        			for(int a = 0;a<petbuf.size();a++){
                        				Buf buf = (Buf)petbuf.get(a);
                        				if(buf.getProperty()== Buf.CAMP_STONE){//阵营宝石buff
                        					value = buf.getValue();
                        					break;
                        				}
                        			}
                        		}
                        		seg.writeInt(value);
                        		service.getConnectService().writeTo(seg, players[i].getId());
                            }catch (Exception e) {
                            	log.debug(e, e);
                            }
                        }
                        Grid[] gridchange = changed.getChangedItems();
                        for (int k = 0; k < gridchange.length; k++) {
                        	int item_id = 0;
                            item_id = gridchange[k].item.getItemId();
                            String item_msg = Items.getMessage(item_id,2,players[i].getPlayerName(),gridchange[k].item.getName(),"怪物那里");
                            if (item_msg != null){
                            	service.getChatService().sendWorldMessage(-1, "系统", item_msg);
                            }
                        }
                        //mengjie add end
                        if(isBossRush){
                        	int tmpStage = players[i].getBossRushStage() + 1;
                        	if(tmpStage <= BossRush.getMaxStage()){
                        		players[i].setBossRushStage(tmpStage);
//                        		if(tmpStage == BossRush.getMaxStage() && players[i].checkBossRushLastTime()){
//                        			players[i].setBossRushTime(players[i].getBossRushTime() + 1);
//                        			players[i].setBossRushLastTime(new Date());
//                        		}
                        	}
                        	players[i].addBossRushBout(round, tmpStage-1);
                        	BossBattlePlayer bbp = new BossBattlePlayer(players[i].getId(),players[i].getPlayerName(),players[i].getLevel(),
                        			players[i].getCamp(),players[i].getBossRushStageBest(),players[i].getBossRushTotalBout());
                        	BossBattleTop.addBossBattleInfo(bbp);
   							
                        	log.info("ID[" + players[i].getId() +"] BossRush stage[" + tmpStage + "] MgId[" + mgId + "] round[" + round +"] win");
                        }
                    }
                    service.getPlayerService().checkPlayer(players[i]);
                }
            }
            try {
                release();
            } catch (LockException ex) {
                ex.printStackTrace();
            }

            String bossTip = BossTips.getTip(mgId);
            if(bossTip!=null){
                sendBossTip(bossTip);
            }
            //mengjie add 服务器怪发地区聊
            String bosslocalTip[] = BossLocalTips.getTip(mgId);
            if(bosslocalTip!=null){
            	String mapid[] = Utils.splitString(bosslocalTip[0], ',');
            	for (int i = 0; i < mapid.length; i++) {
            			service.getChatService().sendMapMessage(
            					Short.valueOf(mapid[i]).shortValue(), -1,"系统",
            					bosslocalTip[2]);	
            	}
            }
            //为统计平台计数
            if(monsters != null){
                boolean flag = false;
                
                for(int m = 0; m < monsters.length; m++){
                    if(monsters[m] != null && monsters[m].getAiClass() != null && monsters[m].getAiClass().trim().length() > 0){
                        flag = true;
                        break;
                    }
                }
                
                if(flag){
                    Server.realtimeStatService.bossKillCounter++;
                }
            }

        }else if (endByte == 3){ 		//玩家取胜，但是玩家抓了怪物作为宠物，则玩家的不获得掉落组
        	WorldPlayer[] players = getAliveTeam();
        	for(int i = 0; i < players.length; i++){
        		Changed changed = new Changed();
        		Pet petflag = null;
        		Monster m = getCatchedMonster(players[i]);
                if (m != null && players[i].getLevel() > 10 &&
                    m.getLevel() > 10 && m.getPetType() != 0)
                    if (!Utils.hit(m.getBabyRate()*4, 1000000)) {
//                	if (false){
                        Pet pet = new Pet();
                        pet.setId(IDGenerator.getPetId());
                        pet.setPetType(m.getPetType());
                        pet.setBaby(false);
                        Utils.initPet(pet, 4 * m.getLevel(), 0,m.getLevel());
                        pet.setLevel(m.getLevel());
                        pet.setItemId(m.getStageId() << 16 |
                                      m.getIndex());
                        pet.setCurrentPoint(0);
                        pet.setExp(0);
                        pet.setFavor(50);
//                        pet.setPoint(20);
                        pet.setHp(pet.getMaxHp());
                        pet.setMp(pet.getMaxMp());
                        Ability[] abs = Utils.getPetAbilities(
                                pet.
                                getPetType());
                        for (int j = 0; j < abs.length; j++) {
                            pet.addAbility(abs[j]);
                        }
                        players[i].addPet(pet, changed);
                        petflag = pet;
                    } else {
                        Pet pet = new Pet();
                        pet.setId(IDGenerator.getPetId());
                        pet.setPetType(m.getPetType());
                        pet.setBaby(true);
                        Utils.initPet(pet,
                                      Utils.getBabyPetPoint(m.
                                getLevel()),
                                      Utils.getBabyPetAddedPoint(m.
                                getLevel()) , 1);
                        pet.setLevel(1);
                        pet.setItemId(m.getStageId() << 16 |
                                      m.getIndex());
                        pet.setCurrentPoint(0);
                        pet.setExp(0);
                        pet.setFavor(50);
                        pet.setHp(pet.getMaxHp());
                        pet.setMp(pet.getMaxMp());
                        Ability[] abs = Utils.getPetAbilities(
                                pet.
                                getPetType());
                        for (int j = 0; j < abs.length; j++) {
                            pet.addAbility(abs[j]);
                        }
                        players[i].addPet(pet, changed);
                        petflag = pet;
                    }
                if(players.length>1){
            		players[i].tryAddFriendFavorite(players,1);
            	}
                service.getBufService().checkBattleBuff(players[i],changed);		//战斗中的buff变更
                service.getConnectService().sendGetItem(changed, players[i].getId(),
                        (byte) 1);
                service.getTongService().modifyPlayer(players[i]);
                service.checkLevelChangedAndSendTips(players[i],changed,players[i].getId());
                Utils.log(log, players[i].getId(),
                        ClientConstants.BATTLE_ROUND_END,
                        "MgId[" + mgId +
                        "]SubType[end]Changed[" +
                        Utils.getHexdump(changed.toBytes()) +
                        "]Money[" + players[i].getMoeny() +
                        "]");
                if (petflag != null){
                	//判断新版本客户端，同步宠物装备信息
                    try{
                    	UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
                    	seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
                    	seg.writeInt(petflag.getId());
                    	Grid[] usedEquipmentsTemp = petflag.getUsedEquipments();
                		if (usedEquipmentsTemp != null){
                			for (int jj = 0;jj<petflag.getUsedEquipmentinfo().length;jj++){
                				seg.write((byte) petflag.getUsedEquipmentinfo()[jj]);
                				if (usedEquipmentsTemp[jj] != null){
                					if (petflag.getUsedEquipmentinfo()[jj] == 1){
                						IEquipment equtmp = (IEquipment) usedEquipmentsTemp[jj].item;
                						equtmp.setDataVersion(players[i].getClientDataVersion());
                						seg.write(equtmp.toClientBytesWithLevel(players[i].getLevel()));
                					}
                				}
                			}
                		}else{
                			for (int jj = 0;jj<petflag.getUsedEquipmentinfo().length;jj++){
                				seg.write((byte) petflag.getUsedEquipmentinfo()[jj]);
                			}
                		}
                		// 发送宠物升级所需升级经验
                		seg.writeInt(Utils.getPetUpLevelExp(petflag.getLevel()));
                		service.getConnectService().writeTo(seg, players[i].getId());
                    }catch (Exception e) {
                    	log.debug(e, e);
                    }
                }
                service.getPlayerService().checkPlayer(players[i]);
        	}
        	try {
                release();
            } catch (LockException ex) {
                ex.printStackTrace();
            }
        	 
        }else {
        	IPlayerData[] players = getTeam();
            for (int i = 0; i < players.length; i++) {
                Changed changed = new Changed();
                Pet p = players[i].getPet();
                if (p != null && p.getFavor() > 30) {
					try {
						HouseInstanceModel houseModel = service.getHouseInstanceModel();
						HouseData hdwaiterPet = houseModel.getHouseByPlayerId(players[i].getId());
						if (hdwaiterPet == null || hdwaiterPet.isUsedWaiter() == false) {
							//added by Jeremy:无管家或管家过期并无自动续买时
							if(players[i] instanceof WorldPlayer){
								WorldPlayer wp = (WorldPlayer)players[i];
								Utils.removePetFavor(wp, p, true, changed);
							}
						} else {
							Random rnd = new Random();
							if(players[i] instanceof WorldPlayer){
								WorldPlayer wp = (WorldPlayer)players[i];
								Utils.removePetFavor(wp, p, false, changed);
							}
               				if(p.getFavor() <= 17){
               					if (Utils.hit(rnd, 5000, 10000)) {				//宠物将要逃跑
               						ChatService chatService = service.getChatService();
               						chatService.sendPrivateMessage(-1,
               								"系统", players[i].getId(),
               								"你的管家英勇的把你准备逃跑的宠物"
               								+ p.getName()
               								+ "捉回来了");
               	            	}
               				}
//               				Random rnd = new Random();
//							Pet[] pets = players[i].removeLimitPetFavor(1, changed, rnd);
//							if (pets != null || pets.length > 0) {
//								for (int j = 0; j < pets.length; j++) {
//									if (pets[j] != null) {
//										Pet pet2 = pets[j];
//										ChatService chatService = service.getChatService();
//										chatService.sendPrivateMessage(-1,
//												"系统", players[i].getId(),
//												"你的管家英勇的把你准备逃跑的宠物"
//												+ pet2.getName()
//												+ "捉回来了");
//									}
//								}
//							}
						}
					} catch (Exception e) {
						//e.printStackTrace();
						Utils.log(log, players[i].getId(), ClientConstants.BATTLE_ROUND_END, "扣除宠物忠诚度出错");
					}
                }
                if (players[i].getMap() != null && players[i].getMap().getInstance() != null
            			&& players[i].getMap().getInstance() instanceof CampBattlefieldInstance) {
                } else {
                	if(players[i] instanceof WorldPlayer){
                		WorldPlayer wp = (WorldPlayer)players[i];
                		ItemUtils.removeDurability(wp, true, changed);
                	}
                }
                service.getBufService().checkBattleBuff(players[i],changed);
                service.getConnectService().sendGetItem(changed, players[i].getId(), (byte) 1);
                Utils.log(log, players[i].getId(),
                        ClientConstants.BATTLE_ROUND_END,
                        "MgId[" + mgId +
                        "]SubType[end]Changed[" +
                        Utils.getHexdump(changed.toBytes()) +
                        "]Money[" + players[i].getMoeny() +
                        "] Battle Fail" );
            }
        
            cancel();
        }
        
        //添加防刷CD
        if(endByte == 1 || endByte == 3){
        	try{
	        	WorldPlayer[] players = getAliveTeam();
	        	long now = System.currentTimeMillis();
	        	long datetmp = now;
	        	for(int i=0; i<players.length; i++){
	        		//记录10个mgid校验
	                int counttmp = -1;
	                for(int j = 0 ;j<10;j++){
	                	if (players[i].killmgid[j] == null){
	                		players[i].killmgid[j] = new WorldPlayerKillMg();
	                		counttmp = -1;
	                		players[i].killmgid[j].setKilltime(now);
	                		players[i].killmgid[j].setMgId(mgId);
	                		break;
	                	}
	                	if (players[i].killmgid[j].getMgId() != 0){
	                		if (datetmp > players[i].killmgid[j].getKilltime()){
	                			datetmp = players[i].killmgid[j].getKilltime();
	                			counttmp = j;
	                		}
	                	}else{
	                		counttmp = -1;
	                		players[i].killmgid[j].setKilltime(now);
	                		players[i].killmgid[j].setMgId(mgId);
	                		break;
	                	}
	                }
	                if (counttmp>=0){
	                	players[i].killmgid[counttmp].setKilltime(now);
	                	players[i].killmgid[counttmp].setMgId(mgId);
	                }
	        	}
        	}catch(Exception e){
        		log.error(e, e);
        	}
        }
        
        //世界boss刷新
        if (getRoundStatus() == 1) {	// 玩家获胜，删除世界BOSS
        	WorldPlayer[] players = getAliveTeam();
        	if(players.length > 0){
	        	WorldBoss worldBoss = service.getBossService().getPlayerBoss(players[0].getId());
	        	for(int i = 0; i < players.length; i++){
	        		service.getBossService().deleteBoss(players[i]);
	        		//这里取消胜利玩家的挂盾
	//        		if(players[i].hasBuf(Buf.GUARD)){
	//        			Changed changed = new Changed();
	//        			players[i].removeBuf(Buf.GUARD, changed);
	//        		    changed.setProperty(Changed.GUARDSTATE, 0);
	//        			service.getConnectService().sendGetItem(changed, players[i].getId(), (byte) 1);
	//        		}
	        	}
	        	if(worldBoss != null){
		        	service.getBossService().refreshWorldBossDisable(worldBoss, WorldBoss.STATE_DESTROY);
		        	BossDefineLoader.bossDefineMap.get(worldBoss.getGroupId()).setNeedRecreate(true);
		            //为统计平台计数
	                Server.realtimeStatService.bossKillCounter++;
		        }
        	}
        } else if (getRoundStatus() == 2) {	// BOSS获胜，显示世界BOSSWorldPlayer[] players = getTeam();
        	IPlayerData[] players = getTeam();
        	WorldBoss worldBoss = service.getBossService().getPlayerBoss(players[0].getId());
        	if (worldBoss != null) {
        		BossDefine bossDefine = BossDefineLoader.bossDefineMap.get(worldBoss.getGroupId());
        		if(bossDefine != null && bossDefine.inTime()){
	        		worldBoss.setState(WorldBoss.STATE_SHOW);
	        		service.getBossService().addWaitingBoss(worldBoss);
	        		Vector playerMap = positionService.getPlayer2Position(worldBoss.getMapId());
	            	byte[] refreshData = worldBoss.getRefreshData(worldBoss);
	        	    UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
	        	    seg.writeShort(ClientConstants.EXTEND_PROTOCOL_BOSS_REFRESH);
	        	    seg.write((byte) 1);
	        	    seg.write(refreshData);
	        	    for (int i = 0; i < playerMap.size(); i++) {
	        			service.getConnectService().writeTo(seg, (Integer)playerMap.get(i));
	            	}
        		}
	        }
        } else{
        	IPlayerData[] players = getTeam();
        	WorldBoss worldBoss = service.getBossService().getPlayerBoss(players[0].getId());
        	for(int i = 0; i < players.length; i++){
        		service.getBossService().deleteBoss(players[i]);
        	}
        	if(worldBoss != null){
	        	service.getBossService().refreshWorldBossDisable(worldBoss, WorldBoss.STATE_SHOW);
	        }
        }
        service.removeBattle(this);
        
//        if (isBossRush) {
//			WorldPlayer[] players = getAliveTeam();
//			for (int i = 0; i < players.length; i++) {
//				synchronized (players[i]) {
//					UWAPSegment seg = new UWAPSegment(
//							ClientConstants.EXTEND_PROTOCOL);
//					seg.writeShort(ClientConstants.EXTEND_OPEN_UI);
//					seg.writeString("ui_award_box");
//					seg.writeInt(1);
//					seg.writeString("NewMailOpen");
//					service.getConnectService().writeTo(seg,
//							players[i].getId());
//				}
//			}
//		}
    }


    protected void sendBossTip(String tip){
    	IPlayerData[] players = getPlayers();
        String leaderName = players[0].getPlayerName();
        StringBuilder sb = new StringBuilder(100);
        sb.append(leaderName);
        for(int i=1;i<players.length;i++){
            sb.append(",");
            sb.append(players[i].getPlayerName());
        }
        String t = tip.replace("leader",leaderName).replace("players",sb.toString());
        service.getChatService().sendWorldMessage(-1,"系统",t);
    }

    public void roundEnd() {
        processEnmities();
        checkRunaway();
        for(int i=0;i<side1.length;i++){
            strategy.fillSpriteStatus(side1[i],this);
        }
        for(int i=0;i<pet1.length;i++){
            if(pet1[i]!=null){
                strategy.fillSpriteStatus(pet1[i],this);
            }
        }
        if(!battleOver) {
            for(int i=0;i<side2.length;i++){
                strategy.fillSpriteStatus(side2[i],this);
            }
            for(int i=0;i<pet2.length;i++){
                if(pet2[i]!=null){
                    strategy.fillSpriteStatus(pet2[i],this);
                }
            }
        }
        
        UWAPSegment seg = getRoundEndSegment((byte)0);
        for (int i = 0; i < side1.length; i++) {
            service.getConnectService().writeTo(seg, side1[i].id);
        }
        battleMovie.clear();
        if (battleOver) {
            end();

            //为统计平台计数
            Server.realtimeStatService.fightCounter++;
        } else {
            this.round++;
            this.status = STATUS.wait_fight;
            lastTime = System.currentTimeMillis();
            resetReady();
        }
    }

    protected UWAPSegment getRoundEndSegment(byte type){
        UWAPSegment seg = new UWAPSegment(ClientConstants.BATTLE_ROUND_END);
        seg.write(type);
        seg.writeInt(getId());
        if(type==1){
            seg.writeShort((short)(round-1));
        }else{
            seg.writeShort((short)round);
        }

        if(type==0){
            seg.write((byte) battleMovie.size());
            for (int i = 0; i < battleMovie.size(); i++) {
                seg.writeInts((int[]) battleMovie.get(i));
            }
        }
        for (int i = 0; i < side1.length; i++) {
            seg.write((byte) (i + 1));
            seg.writeInt(side1[i].getAllStatus());
            seg.writeInt(side1[i].hp);
            seg.writeInt(side1[i].mp);
            seg.writeBoolean(side1[i].canAction());
            if (side1[i].used && side1[i].usedItem != null) {
                seg.writeInt(getItemFlag(side1[i]));
            } else {
                seg.writeInt(0);
            }
            seg.writeString(side1[i].getSkillName());
            if (pet1[i] != null) {
                seg.writeInt(pet1[i].hp);
                seg.writeInt(pet1[i].mp);
                seg.writeInt(pet1[i].getAllStatus());
                seg.writeBoolean(pet1[i].canAction());
                seg.writeString(pet1[i].getSkillName());
            } else {
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeBoolean(false);
                seg.writeString("");
            }
        }
        for (int i = 0; i < side2.length; i++) {
            seg.write((byte) ( -i - 1));
            seg.writeInt(side2[i].getAllStatus());
            seg.writeInt(side2[i].hp);
            seg.writeInt(side2[i].mp);
            seg.writeBoolean(true);
            seg.writeInt(0);
            seg.writeString(side2[i].getSkillName());
            if (pet2[i] != null) {
                seg.writeInt(pet2[i].hp);
                seg.writeInt(pet2[i].mp);
                seg.writeInt(pet2[i].getAllStatus());
                seg.writeBoolean(pet2[i].canAction());
                seg.writeString(pet2[i].getSkillName());
            } else {
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeBoolean(false);
                seg.writeString("");
            }
        }
        if(type==0){
            byte roundStatus = getRoundStatus();
            if(roundStatus == 3){			// 玩家抓了怪物作为宠物
            	 seg.write((byte)1);
            }else{
            	seg.write(roundStatus);
            }
            
            if (roundStatus == 1) {
                seg.writeInt(mgId);
            }
        }
        
        syncCDInfo(seg);

        return seg;
    }
    
    //Added by leo for sync CD info
    public void syncCDInfo(UWAPSegment seg){
        seg.write((byte)(side1.length * 2 + side2.length * 2));
        for(int i = 0; i < side1.length; i++){
            seg.writeInt(side1[i].id);
            seg.write(side1[i].getCoolDownInfo());
            if(pet1[i] != null){
                seg.writeInt(pet1[i].id);
                seg.write(pet1[i].getCoolDownInfo());
            }else{
                seg.writeInt(-1);
                seg.write(new byte[0]);
            }
        }
        for(int i = 0; i < side2.length; i++){
            seg.writeInt(side2[i].id);
            seg.write(side2[i].getCoolDownInfo());
            if(pet2[i] != null){
                seg.writeInt(pet2[i].id);
                seg.write(pet2[i].getCoolDownInfo());
            }else{
                seg.writeInt(-1);
                seg.write(new byte[0]);
            }
        }
    }

    protected byte getRoundStatus() {
        if (!battleOver) {
            return 0;
        } else {
            boolean win = true;
            for (int i = 0; i < side2.length; i++) {
                if (!side2[i].testCannotBattle()) {
                    win = false;
                }
            }
            if(win){
            	for(int i = 0; i < side2.length; i ++){
            		if(side2[i].getIsCatch() == 1){
            			// 有怪物被抓
            			return 3;
            		}
            	}
            }
            if (win)
                return 1;			// 玩家获胜
            else
                return 2;			//怪物获胜
        }
    }


    private void checkRunaway(){
        if(side1.length > 1){ //多人组队状态
            for(int i = 0; i < side1.length; i++){
                if(side1[i].getDebufStatus() == Skill.STATUS_RUNAWAY){
                    service.changeTeamStateToNormal(side1[i]);
                }
            }
        }
    }

    private void checkDie(){
        if(side1.length > 1){ //多人组队状态
            for(int i = 0; i < side1.length; i++){
                if(side1[i].getDebufStatus() == Skill.STATUS_DIE){
                    service.changeTeamStateToNormal(side1[i]);
                }
            }
        }
    }
    
    public void processEnmities(){
        if(!battleOver){
            for(int i = 0; i < battleRecorders.size(); i++){
                BattleRecorder recorder = (BattleRecorder)battleRecorders.get(i);
                if(recorder.src.bsType != BattleSprite.TYPE_MONSTER && recorder.src.bsType != BattleSprite.TYPE_MONSTER_PET){
                    int enmityType = recorder.skill.enmityType;
                    BattleSprite[] bss = null;
                    if(enmityType == Skill.ENMITY_ALL){
                        List l = new ArrayList(6);
                        for(int j = 0; j < side2.length; j++){
                            l.add(side2[j]);
                            if(side2[j] != null)
                                l.add(side2[j]);
                        }

                    }else{
                        bss = recorder.getDests();
                    }
                    if(bss != null){
                        for(int j = 0; j < bss.length; j++){
                            if(!bss[j].testCannotBattle()){
                                bss[j].addEnmity(recorder.src, recorder.skill.enmity + recorder.skill.adjust);
                            }
                        }
                    }
                }
            }
            for(int i = 0; i < side1.length; i++){
                if(side1[i].testCannotBattle()){
                    for(int j = 0; j < side2.length; j++){
                        side2[j].clearEnmity(side1[i]);
                        if(pet2[j] != null)
                            pet2[j].clearEnmity(side1[i]);
                    }
                }
                if(pet1[i] != null && pet1[i].testCannotBattle()){
                    for(int j = 0; j < side2.length; j++){
                        side2[j].clearEnmity(pet1[i]);
                        if(pet2[j] != null)
                            pet2[j].clearEnmity(side1[i]);
                    }
                }
            }
        }
    }

    public void start() {
        UWAPSegment seg = getInitSegment(serial);
        broadcast(seg);
        status = STATUS.wait_start;
        lastTime = System.currentTimeMillis();
    }
    
    public void resetWorldBoss(BattleSprite mg){
    	int maxLevel = 1;
    	
    	for(int i = 0; i < side1.length; i++){
    		if(side1[i].level > maxLevel){
    			maxLevel = side1[i].level;
    		}
    	}
    	
    	mg.attributes[BattleSprite.ATTR_HPMAX] = service.getBossService().getWorldBossHp(mgId, mg.attributes[BattleSprite.ATTR_HPMAX], maxLevel);
        mg.attributes[BattleSprite.ATTR_MPMAX] =  service.getBossService().getWorldBossMp(mgId, mg.attributes[BattleSprite.ATTR_MPMAX], maxLevel);
    	mg.hp = mg.attributes[BattleSprite.ATTR_HPMAX];
    	mg.mp = mg.attributes[BattleSprite.ATTR_MPMAX];

    }
    /**
     * @param serial
     * @return 
     */
    protected UWAPSegment getCatchBattleSegment(int serial){
    	 UWAPSegment seg = new UWAPSegment(ClientConstants.BATTLE_INIT, serial);
         seg.writeInt(id);
         seg.writeInt(teamId);
         seg.write((byte) side2.length);
         for (int i = 0; i < side2.length; i++) {
             seg.writeInt(side2[i].hp);
             seg.writeInt(side2[i].mp);
             seg.writeInt(side2[i].getAttribute(BattleSprite.ATTR_HPMAX));
             seg.writeInt(side2[i].getAttribute(BattleSprite.ATTR_MPMAX));
             seg.writeInt(side2[i].monster.getPngId());
             seg.writeString(side2[i].monster.getName() + "(" +
                             side2[i].monster.getLevel() +
                             ")");
             seg.writeShort(side2[i].monster.getLevel());
             seg.write(side2[i].monster.getPetType() != 0 ? (byte) 1 : (byte) 0);
             seg.write((byte) - 1);
             if(pet2[i]!=null){
                 seg.writeInt(pet2[i].hp);
                 seg.writeInt(pet2[i].mp);
                 seg.writeInt(pet2[i].getAttribute(BattleSprite.ATTR_HPMAX));
                 seg.writeInt(pet2[i].getAttribute(BattleSprite.ATTR_MPMAX));
             }else{
                 seg.writeInt(0);
                 seg.writeInt(0);
                 seg.writeInt(0);
                 seg.writeInt(0);
             }
             seg.writeString("");
             seg.writeShort((short) 0);
             seg.write((byte) 0);
             if(pet2[i] != null){
            	 seg.write(pet2[i].pet.getBindType());
             }else{
            	 seg.write((byte)0);
             }
         }
         return seg;
    }
    protected UWAPSegment getInitSegment(int serial){
        UWAPSegment seg = new UWAPSegment(ClientConstants.BATTLE_INIT, serial);
        seg.writeInt(id);
        seg.writeInt(teamId);
        seg.write((byte) side2.length);
        for (int i = 0; i < side2.length; i++) {
        	String tmpai = side2[i].monster.getAiClass();
        	if ( tmpai != null && tmpai.length() > 0 && tmpai.startsWith("Ai90013") ) {
        		//随机修改副本，重置怪物属性 情人节副本
        		resetmg(side2[i]);
        	}else if (tmpai != null && tmpai.length() > 0 && tmpai.startsWith("Ai90001")){
        		//风吼裂谷副本
        		resetmg90001(side2[i]);
        	}else if ( tmpai != null && tmpai.length() > 0 && tmpai.startsWith("Ai90016") ) {
        		//小年AI
        		reset90016(side2[i]);
        	}else{
	        	//世界boss血蓝重置
	        	if(service.getBossService().isWorldBoss(mgId)){
	        		resetWorldBoss(side2[i]);
	        	}
        	}
        		
            seg.writeInt(side2[i].hp);
            seg.writeInt(side2[i].mp);
            seg.writeInt(side2[i].getAttribute(BattleSprite.ATTR_HPMAX));
            seg.writeInt(side2[i].getAttribute(BattleSprite.ATTR_MPMAX));
            seg.writeInt(side2[i].monster.getPngId());
            seg.writeString(side2[i].monster.getName() + "(" +
                            side2[i].monster.getLevel() +
                            ")");
            seg.writeShort(side2[i].monster.getLevel());
            seg.write(side2[i].monster.getPetType() != 0 ? (byte) 1 : (byte) 0);		//canCatch
            seg.write((byte) - 1);
            if(pet2[i]!=null){
                seg.writeInt(pet2[i].hp);
                seg.writeInt(pet2[i].mp);
                seg.writeInt(pet2[i].getAttribute(BattleSprite.ATTR_HPMAX));
                seg.writeInt(pet2[i].getAttribute(BattleSprite.ATTR_MPMAX));
            }else{
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
            }
            seg.writeString("");		//pet name
            seg.writeShort((short) 0);	//pet level
            seg.write((byte) 0);		//can catch:如果是pk的话，敌方的宠无法捕获
            if(pet2[i] != null){
            	seg.write(pet2[i].pet.getBindType());
            }else{
            	seg.write((byte)0);
            }
        }
        return seg;
    }

    public void process(UWAPData data,int playerId) {
//            int pkId = data.readInt();
        byte type = data.getAppType();
        if(type==ClientConstants.BATTLE_JOIN){
            try {
                int teamId = data.readInt();
                int id = data.readInt();		//playerID
                if(playerId==id){
                    receiveJoin(teamId, playerId);
                    resetMercenary();
                }
                else
                    log.info("ID["+playerId+"]BattleJoin Error SendId["+id+"]");
            } catch (IllegalAccessException ex) {
                log.error(ex, ex);
            }
        }
        else if (type == ClientConstants.BATTLE_FIGHT) {
            try {
                short roundId = data.readShort();
                int action = data.readInt();
                byte target = data.readByte();
                int petAction = data.readInt();
                byte petTarget = data.readByte();
                receiveFight(playerId, action, target, petAction, petTarget,
                             roundId);
            } catch (IllegalAccessException ex1) {
                log.error(ex1,ex1);
            }
//            } else {
//                WorldPlayer player = playerService.getWorldPlayer(playerId);
//                if (player != null) {
//                    UWAPSegment seg = new UWAPSegment(ClientConstants.BATTLE_ABORT,
//                                                      data.getSerial(),
//                                                      data.getSessionId());
//                    seg.writeInt(pkId);
//                    seg.writeInt(player.getHp());
//                    seg.writeInt(player.getMp());
//                    Pet pet = player.getPet();
//                    seg.writeInt(pet == null ? -1 : pet.getHp());
//                    seg.writeInt(pet == null ? -1 : pet.getMaxMp());
//                    write(seg);
//                }
//            }
        }
    }
    private synchronized void receiveFight(int id, int action,
                                           byte target, int petAction,
                                           byte petTarget, short roundId) {
        if(status!=STATUS.wait_fight)
            return;
//        if(status!=WAIT_FIGHT)
//            return;
        if(this.round == roundId){
            int index = getIndex(id);
            side1[index].idleRound = 0;
            int realAction = (short)(action & 0xFFFF);
            int itemId = (action >> 16) & 0xFFFF;
            
            if(checkAction(side1[index],realAction,itemId)){
                side1[index].setTarget(getTarget(target), getTargetIndex(target));
            }
            int realPetAction = (short)(petAction & 0xFFFF);
            if(realPetAction == Skill.SKILL_NOT_READY){
                realPetAction = Skill.SKILL_STAY;
            }
            if(pet1[index] != null){
                if(checkPetAction(side1[index],pet1[index],realPetAction)){
                    pet1[index].setTarget(getTarget(petTarget),
                                          getTargetIndex(petTarget));
                }
            }
            try {
	            if(side1[index].player != null){
	            	log.info("ID["+side1[index].player.getId()+"]use realAction["+realAction +"]itemId["+itemId+"]Pet's realAction["+realPetAction +"]");
	            }
            }catch (Exception ex) {
                caughtException(ex);
            }
            side1[index].ready = true;
            resetMercenary();
            if(isReady()){
                try {
                    battleBout(side1, side2, pet1, pet2, roundId);
                    roundEnd();
                    clearBourt();
                } catch (Exception ex) {
                    caughtException(ex);
                }
            }
        }
    }

    private void caughtException(Exception ex){
        log.error(ex,ex);
        abort();
    }

    private int getIndex(int id) {
        for (int i = 0; i < side1.length; i++) {
            if (side1[i].id == id) {
                return i;
            }
        }
        return -1;
    }

    private int getTargetIndex(byte index){
        if(index < -10)
            return (-index) - 10 - 1;
        if(index > 10)
            return index - 10 - 1;
        if(index < 0)
            return (-index) - 1;
        if(index > 0)
            return index - 1;
        return 0;
    }

    private BattleSprite getTarget(byte index){
        try {
            if (index < -10) {
                int ii = ( -index) - 10;
                return pet2[ii - 1];
            }
            if (index > 10) {
                return pet1[index - 10 - 1];
            }
            if (index < 0) {
                return side2[( -index) - 1];
            }
            if (index > 0) {
                return side1[index - 1];
            }
        } catch (Exception ex) {
            log.info("Target Index["+index+"] Error");
        }
        return null;
    }


    protected UWAPSegment getBattleStartSegment(byte type){
        UWAPSegment seg = new UWAPSegment(ClientConstants.
                BATTLE_START, serial);
        seg.writeInt(id);
        seg.writeInt(teamId);
        seg.write((byte) side1.length);
        for (int i = 0; i < side1.length; i++) {		//our team
            seg.writeInt(side1[i].id);
            seg.writeInt(side1[i].hp);
            seg.writeInt(side1[i].mp);
            seg.writeInt(side1[i].attributes[BattleSprite.
                         ATTR_HPMAX]);
            seg.writeInt(side1[i].attributes[BattleSprite.
                         ATTR_MPMAX]);
            IEquipment weapon = side1[i].player.getWeapon();
            if (weapon == null) {
                seg.write((byte) - 1);
            } else {
                seg.write((byte) weapon.getProperty(30, side1[i].level)); //武器类型
            }
            seg.writeInt(side1[i].getAllStatus());
            seg.writeBoolean(side1[i].canAction());
            seg.write(side1[i].player.getLightLevel());
            if (pet1[i] != null) {
                seg.write((byte) pet1[i].pet.getPetType());
                seg.writeInt(pet1[i].hp);
                seg.writeInt(pet1[i].mp);
                seg.writeInt(pet1[i].pet.getMaxHp());
                seg.writeInt(pet1[i].pet.getMaxMp());
                String newName = pet1[i].pet.getName();
        		if(pet1[i].pet.getBindType() > 0){
					newName = newName.concat("(" + (pet1[i].pet.getBindType() + 1) + "代)");
				}
                if(pet1[i].pet.getEnhanceName().equals("")&& pet1[i].pet.getEnhanceName().length()==0){
                	seg.writeString(newName);
                }else{
                	seg.writeString(newName + pet1[i].pet.getEnhanceName());
                }
                
                seg.writeInt(pet1[i].getAllStatus());
                seg.writeBoolean(pet1[i].canAction());
                //2代形象
                seg.write(pet1[i].pet.getBindType());
                seg.writeShort(pet1[i].level);
                seg.writeShort(pet1[i].pet.getColorIndex());
                //3代宠信息
                seg.writeInt(pet1[i].pet.getEvolutionLevel());
                seg.writeInt(pet1[i].pet.getEvolutionType());
            } else {
                seg.write((byte) - 1);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeString("");
                seg.writeInt(0);
                seg.writeBoolean(false);
                seg.write((byte)0);
                seg.writeShort((short)0);
                seg.writeShort((short)0);
                seg.writeInt(0);
                seg.writeInt(0);
            }
        }
        seg.writeShort((short)round);		//回合数
//        if(type==0)
//            seg.writeShort((short) round);
//        else if(type==1)
//            seg.writeShort((short)(round-1));
        return seg;
    }

    private synchronized void receiveJoin(int teamId,int playerId){
//        if(this.teamId==teamId){
            if (status == STATUS.end)
                return;
            else if(status==STATUS.wait_start){		//等待开始战斗
                for (int i = 0; i < side1.length; i++) {
                    if (side1[i].id == playerId || side1[i].id < 0) {
                        side1[i].ready = true;
                    }
                }
                if (isReady()) {
                    UWAPSegment seg = getBattleStartSegment((byte)0);
                    broadcast(seg);		//群发这个消息，使每个队友开始
                    resetReady();
                    status = STATUS.wait_fight;
                    lastTime = System.currentTimeMillis();
                }
            }
            else if(status==STATUS.wait_fight){
                UWAPSegment seg = getBattleStartSegment((byte)1); //如果是这时候收到那么说明是战斗以后掉线重连的		//一般参数为1都是断线重连的
                service.getConnectService().writeTo(seg,playerId);
                seg = getRoundEndSegment((byte)1);		//回合结束信息
                service.getConnectService().writeTo(seg,playerId);		//每个连接上的用户都有1个connect对象
            }
            else{
                log.info("ID["+playerId+"] status["+status+"]");
            }
//        }else{
//            log.info("ID["+playerId+"]TEAMID ERROR");
//        }
    }

    private boolean isReady(){
        for(int i = 0; i < side1.length; i++){
            if(!side1[i].ready)
                return false;
        }
        return true;
    }

    private void resetReady(){
        for(int i = 0; i < side1.length; i++){
        	if(side1[i].id < 0){
        		side1[i].ready = true;
        	}else{
        		side1[i].ready = false;
        	}
        }
    }
    
    private void resetMercenary(){
        for(int i = 0; i < side1.length; i++){
        	if(side1[i].id < 0){
        		side1[i].setSkill(Skill.NOTREADY_SKILL);
        	}
        }
    }

    private void broadcast(UWAPSegment seg) {
    	for (int i = 0; i < side1.length; i++) {
    		if(side1[i].id < 0){
    			side1[i].ready = true;
    		}
    	}
        for (int i = 0; i < side1.length; i++) {
        	if(side1[i].id >= 0){
        		service.getConnectService().writeTo(seg, side1[i].id);
        	}
        }
    }


    public void addLock(ILock lock) {
        this.lock = lock;
    }

    public void removeLock(ILock lock) {
        this.lock = null;
    }
    private void resetmg(BattleSprite mg) {
    	String aistr = mg.monster.getAiClass();
    	int maxlevel = 0;
    	int maxleveli = 0;
        for(int i = 0; i < side1.length; i++){
        	if (maxlevel < side1[i].player.getLevel()){
        		maxlevel = side1[i].player.getLevel();
        		maxleveli = i;
        	}
        }
        if (aistr.equalsIgnoreCase("Ai90013_1")){
        	mg.attributes[BattleSprite.ATTR_HPMAX] = maxlevel * 200;
            mg.attributes[BattleSprite.ATTR_MPMAX] = maxlevel * 200;
        }else if (aistr.equalsIgnoreCase("Ai90013_4")){
        	mg.attributes[BattleSprite.ATTR_HPMAX] = maxlevel * 80;
            mg.attributes[BattleSprite.ATTR_MPMAX] = maxlevel * 80;
        }else if (aistr.equalsIgnoreCase("Ai90013_5")){
        	mg.attributes[BattleSprite.ATTR_HPMAX] = maxlevel * 5000;
            mg.attributes[BattleSprite.ATTR_MPMAX] = maxlevel * 300;
        }else{
        	mg.attributes[BattleSprite.ATTR_HPMAX] = maxlevel * 100;
            mg.attributes[BattleSprite.ATTR_MPMAX] = maxlevel * 100;
        }
        mg.hp = mg.attributes[BattleSprite.ATTR_HPMAX];
        mg.mp = mg.attributes[BattleSprite.ATTR_MPMAX];
        mg.attributes[BattleSprite.ATTR_AGI] = side1[maxleveli].player.getRealAgility() + 1;
        mg.attributes[BattleSprite.ATTR_STR] = side1[maxleveli].player.getRealStrength() + 1;
        mg.attributes[BattleSprite.ATTR_VIT] = side1[maxleveli].player.getRealVitality() + 1;
        mg.attributes[BattleSprite.ATTR_INT] = side1[maxleveli].player.getRealIntelligence() + 1;        
    }
    
    private void reset90016(BattleSprite bs) {
    	int mul = 2500;
    	int levelSum = 0;
    	int apprenticeCount = 0;
    	int percent = 40;
    	for(int i=0; i<side1.length; i++){
    		levelSum += side1[i].player.getLevel();
    		if(playerService.getMasetService().isMaster(side1[i].player)){
    			Master[] master = playerService.getMasetService().getRelation(side1[i].player);
    			for(int j=0; j<master.length; j++){
    				if(j != i && side1[j].player.getId() == master[j].getPrenticeId()){
    					apprenticeCount++;
    				}
    			}
    		}
    	}
    	if(apprenticeCount > 0){
    		mul = 2000;
    		percent = 30;
    	}
    	if(bs == side2[1]){
	    	bs.attributes[BattleSprite.ATTR_HPMAX] = levelSum * mul * 10;
    	}else{
    		bs.attributes[BattleSprite.ATTR_HPMAX] = levelSum * mul * percent / 100;
    	}
    	bs.hp = bs.attributes[BattleSprite.ATTR_HPMAX];
    }
    
    private void resetmg90001(BattleSprite mg) {
    	String aistr = mg.monster.getAiClass();
    	int on = 0;
    	int down = 0;
        if ((side1.length<3) && (side1.length>1)){
        	on = 4;
        	down = 5;
        }else if ((side1.length<2) && (side1.length>0)){
        	on = 2;
        	down = 3;
        }else{
        	on = 1;
        	down = 1;
        }
        if (aistr.equalsIgnoreCase("Ai90001_3")){
        	mg.attributes[BattleSprite.ATTR_HPMAX] = mg.attributes[BattleSprite.ATTR_HPMAX] * on / down;
            mg.attributes[BattleSprite.ATTR_MPMAX] = mg.attributes[BattleSprite.ATTR_MPMAX] * on / down;
        }else if (aistr.equalsIgnoreCase("Ai90001_4")){
        	mg.attributes[BattleSprite.ATTR_HPMAX] = mg.attributes[BattleSprite.ATTR_HPMAX] * on / down;
            mg.attributes[BattleSprite.ATTR_MPMAX] = mg.attributes[BattleSprite.ATTR_MPMAX] * on / down;
        }
        mg.hp = mg.attributes[BattleSprite.ATTR_HPMAX];
        mg.mp = mg.attributes[BattleSprite.ATTR_MPMAX];
//        mg.attributes[BattleSprite.ATTR_AGI] = mg.attributes[BattleSprite.ATTR_AGI] * on / down;
//        mg.attributes[BattleSprite.ATTR_STR] = mg.attributes[BattleSprite.ATTR_STR] * on / down;
//        mg.attributes[BattleSprite.ATTR_VIT] = mg.attributes[BattleSprite.ATTR_VIT] * on / down;
//        mg.attributes[BattleSprite.ATTR_INT] = mg.attributes[BattleSprite.ATTR_INT] * on / down;        
    }
}