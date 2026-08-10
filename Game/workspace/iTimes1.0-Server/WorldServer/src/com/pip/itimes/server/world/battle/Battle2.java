package com.pip.itimes.server.world.battle;

import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.MercenaryPlayer;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.ai.Ai90016_1;
import com.pip.itimes.server.world.worldboss.WorldBossConfig;
import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.stage.DiamondShineBuf;
import com.pip.itimes.server.stage.Diamonds;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.Monster;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Pet;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Enumeration;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.Items;
import org.apache.log4j.Logger;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.suit.SuitEffect;
import com.pip.itimes.server.suit.Suits;

/**
 * @author Jeffrey
 * @version 1.0
 */
public abstract class Battle2 implements BattleDataProcess {

    private static final Logger log = Logger.getLogger(Battle2.class);

    protected BattleSprite[] side1;
    protected BattleSprite[] pet1;
    protected BattleSprite[] side2;
    protected BattleSprite[] pet2;

    protected BattleStrategy strategy;

    protected int id;

    protected int round = 1;

    protected BattleService2 service;

    protected Vector battleRecorders;
    protected boolean battleOver = false;

    protected volatile STATUS status = STATUS.init;
    protected long lastTime;
    
    protected int roundHurt = 0;		//每回合对怪物造成的伤害

    protected static enum STATUS {init,wait_start,wait_fight,end};
//    private IBattleHandler handler;

    public void setBattleOver () {
    	battleOver = true;
    }
    
    public Battle2(int id, BattleService2 service, BattleStrategy strategy) {
        this.id = id;
        this.service = service;
//        this.handler = handler;
        this.strategy = strategy;
    }

    public void setSide1(BattleSprite[] side1, BattleSprite[] pet1) {
        this.side1 = side1;
        this.pet1 = pet1;
    }

    public void setSide2(BattleSprite[] side2, BattleSprite[] pet2) {
        this.side2 = side2;
        this.pet2 = pet2;
    }

    protected BattleSprite initPlayer(IPlayerData player,boolean isBossRush, boolean isWorldBoss) {
        BattleSprite sprite = new BattleSprite();
      /*  int vit = player.getRealVitality() ;
        int str = player.getRealStrength();
        int inte = player.getRealIntelligence();
        int agi = player.getRealAgility();*/
        int vit = player.getRealVitality();
        int str = player.getRealStrength();
        int inte = player.getRealIntelligence();
        int agi = player.getRealAgility();
        
        int hp = player.getHp() ;
        int mp = player.getMp() ;
        
        Buf bufEva = player.getCampBuf(Buf.CAMP_EVA);
	    int evaValue = 0;
        if(bufEva != null){
        	evaValue = bufEva.getValue();
        }
        Buf bufStone = player.getCampBuf(Buf.CAMP_STONE);
        int stoneValue = 0;
        if(bufStone != null){
        	stoneValue = bufStone.getValue();
        }
        int [] addpoint = player.getSuitEffectDiamondAddValue();	//各属性宝石加成
        int [] trainpoint= player.getTrainLevel();
        int [] trainlevelstone = player.getTrainAttributeAddValue();
        int []magicposlevel = player.getMagicPosLevel();
	    int []magicposfloor = player.getMagicPosFloor();
        sprite.initBattleData((byte) 0, player.getLevel(), vit, str, inte, agi,
                              player.getLuck(), hp, mp, player.getVianyType(), evaValue, stoneValue, addpoint,trainpoint,trainlevelstone,magicposlevel,magicposfloor);
        //sprite.initIntervene(player);
        sprite.id = player.getId();
        if(player.getBufProperty(Changed.HP)>0){
        	sprite.attributes[sprite.ATTR_HPMAX] +=player.getBufProperty(Changed.HP);
        }
        if(player.getBufProperty(Changed.MP)>0){
        	sprite.attributes[sprite.ATTR_MPMAX] +=player.getBufProperty(Changed.MP);
        }
        IEquipment[] equips = player.getUsedEquipments();
        
        sprite.initEquipData(equips);
        
        //如果采用剥离崇算的方法比较麻烦 ，所以这里采用后加的方法，只增加后获得的属性
        // sprite.addDiamondData(equips, 1);
        
        //mengjie add
        SuitEffect[] effectstmp = Suits.getActualSuitEffect(equips);
        
        //星辉套装效果
        SuitEffect[] effects = null;
        if (player != null){
        	int[] diamondShineLevel = Suits.getActualPointSuitEffect2(player.getUsedEquipments());
    		player.addDiamondShineBuf(diamondShineLevel);
    		if(effectstmp != null){
    			effects = effectstmp;
    		}
        }else{
        	effects = effectstmp;
        }
        ArrayList tempAry = (ArrayList)player.getDiamondShineList();
        for(int i =0; i < tempAry.size();i++){
        	DiamondShineBuf dsBuf = (DiamondShineBuf)tempAry.get(i);
        	switch(dsBuf.getProperty()){
        		case DiamondShineBuf.PHYSIC_ATTC:
        	        int ret = sprite.attributes[sprite.ATTR_PMAX] * player.getDiamondShineBufAttri(DiamondShineBuf.PHYSIC_ATTC) / 100;
        	        sprite.attributes[sprite.ATTR_PMAX]  += ret;
        	        ret = sprite.attributes[sprite.ATTR_PMIN] * player.getDiamondShineBufAttri(DiamondShineBuf.PHYSIC_ATTC) / 100;
        	        sprite.attributes[sprite.ATTR_PMIN]  += ret;
        			break;
        		case DiamondShineBuf.MAGIC_ATTC:
        	        ret = sprite.attributes[sprite.ATTR_MMAX] * player.getDiamondShineBufAttri(DiamondShineBuf.MAGIC_ATTC) / 100;
        	        sprite.attributes[sprite.ATTR_MMAX]  += ret;
        	        ret = sprite.attributes[sprite.ATTR_MMIN] * player.getDiamondShineBufAttri(DiamondShineBuf.MAGIC_ATTC) / 100;
        	        sprite.attributes[sprite.ATTR_MMIN]  += ret;
        	        break;
        		case DiamondShineBuf.NOCRI:
        	        ret = sprite.attributes[sprite.ATTR_NOCRI] * player.getDiamondShineBufAttri(DiamondShineBuf.NOCRI) / 100;
        	        sprite.attributes[sprite.ATTR_NOCRI]  += ret;
        	        break;
        		case DiamondShineBuf.PHYSIC_CRI:
        	        ret = sprite.attributes[sprite.ATTR_PCRI] * player.getDiamondShineBufAttri(DiamondShineBuf.PHYSIC_CRI) / 100;
        	        sprite.attributes[sprite.ATTR_PCRI]  += ret;
        	        break;
        		case DiamondShineBuf.MAGIC_CRI:
        	        ret = sprite.attributes[sprite.ATTR_MCRI] * player.getDiamondShineBufAttri(DiamondShineBuf.MAGIC_CRI)/ 100;
        	        sprite.attributes[sprite.ATTR_MCRI]  += ret;
        	        break;
        		case DiamondShineBuf.AGI:
        	        ret = sprite.attributes[sprite.ATTR_AGI] * player.getDiamondShineBufAttri(DiamondShineBuf.AGI) / 100;
        	        sprite.attributes[sprite.ATTR_AGI]  += ret;
        			break;
        		case DiamondShineBuf.STR:
        	        ret = sprite.attributes[sprite.ATTR_STR] * player.getDiamondShineBufAttri(DiamondShineBuf.STR) / 100;
        	        sprite.attributes[sprite.ATTR_STR]  += ret;
        			break;
        		case DiamondShineBuf.INT:
        	        ret = sprite.attributes[sprite.ATTR_INT] * player.getDiamondShineBufAttri(DiamondShineBuf.INT) / 100;
        	        sprite.attributes[sprite.ATTR_INT]  += ret;
        			break;
        		case DiamondShineBuf.ADD_HPMAX:
        	        ret = sprite.attributes[sprite.ATTR_HPMAX] * player.getDiamondShineBufAttri(DiamondShineBuf.ADD_HPMAX) / 100;
        	        sprite.attributes[sprite.ATTR_HPMAX] += ret;
        			break;
        		case DiamondShineBuf.ADD_MPMAX:
        	        ret = sprite.attributes[sprite.ATTR_MPMAX] * player.getDiamondShineBufAttri(DiamondShineBuf.ADD_MPMAX) / 100;
        	        sprite.attributes[sprite.ATTR_MPMAX] += ret;
        			break;
        		case DiamondShineBuf.STR_VALUE:
        			ret = player.getDiamondShineBufAttri(DiamondShineBuf.STR_VALUE);
        			sprite.attributes[sprite.ATTR_STR]  += ret;
        			break;
        		case DiamondShineBuf.AGI_VALUE:
        			ret = player.getDiamondShineBufAttri(DiamondShineBuf.AGI_VALUE);
        			sprite.attributes[sprite.ATTR_AGI]  += ret;
        			break;
        		case DiamondShineBuf.VIT_VALUE:
        			ret =player.getDiamondShineBufAttri(DiamondShineBuf.VIT_VALUE);
        			sprite.attributes[sprite.ATTR_VIT]  += ret;
        			break;
        		case DiamondShineBuf.INT_VALUE:
        			ret = player.getDiamondShineBufAttri(DiamondShineBuf.INT_VALUE);
        			sprite.attributes[sprite.ATTR_INT]  += ret;
        			break;
        	}
        }
        
        if(effects != null){
            sprite.battleSuitEffect = new BattleSuitEffect[effects.length];

            for(int i = 0; i < effects.length; i++){
                sprite.battleSuitEffect[i] = new BattleSuitEffect(effects[i]);
                sprite.battleSuitEffect[i].clearEffect();
            }
        }

        sprite.skillList = new short[0];
        sprite.player = player;
        sprite.name = player.getPlayerName();
        sprite.face = (byte)player.getFace();
        sprite.level = (short) player.getLevel();
        sprite.setStatus(BattleSprite.SEAL_SKILL_ATTACK,false);
        if(player.getLevel()>10&&!(strategy instanceof InstanceBattleStrategy))
            sprite.setStatus(BattleSprite.SEAL_SKILL_CATCH,false);
        sprite.setStatus(BattleSprite.SEAL_SKILL_ITEM,false);
        if(player.getTeamState() == WorldPlayer.TEAM_FOLLOW){
        	sprite.setStatus(BattleSprite.SEAL_SKILL_RUNAWAY,true);				//组队打怪，服务器怪禁止逃跑
        }else{
        	sprite.setStatus(BattleSprite.SEAL_SKILL_RUNAWAY,false);				//服务器怪禁止逃跑
        }
        sprite.setStatus(BattleSprite.SEAL_SKILL_SKILL,false);
        
        if(sprite.player != null){
        	 if(hp > sprite.attributes[sprite.ATTR_HPMAX])
                 hp = sprite.attributes[sprite.ATTR_HPMAX];
             if(mp > sprite.attributes[sprite.ATTR_MPMAX])
                 mp = sprite.attributes[sprite.ATTR_MPMAX];
        	sprite.attributes[sprite.ATTR_HPMAX] = sprite.player.calculateMaxHp();
        	sprite.attributes[sprite.ATTR_MPMAX] = sprite.player.calculateMaxMp();
        	if(player.getBufProperty(Changed.HP)>0){
            	sprite.attributes[sprite.ATTR_HPMAX] +=player.getBufProperty(Changed.HP);
            }
            if(player.getBufProperty(Changed.MP)>0){
            	sprite.attributes[sprite.ATTR_MPMAX] +=player.getBufProperty(Changed.MP);
            }
            if(sprite.hp >sprite.attributes[sprite.ATTR_HPMAX]){
            	sprite.hp = sprite.attributes[sprite.ATTR_HPMAX];
            }
            if(sprite.mp >sprite.attributes[sprite.ATTR_MPMAX]){
            	sprite.mp = sprite.attributes[sprite.ATTR_MPMAX];
            }
            //百层BOSS挑战，替玩家回满血满蓝
            if(isBossRush || isWorldBoss){ 
            	if(sprite.hp < sprite.attributes[sprite.ATTR_HPMAX]){
                	sprite.hp = sprite.attributes[sprite.ATTR_HPMAX];
                	sprite.player.setHp(sprite.player.getMaxHp());
                }
                if(sprite.mp < sprite.attributes[sprite.ATTR_MPMAX]){
                	sprite.mp = sprite.attributes[sprite.ATTR_MPMAX];
                	sprite.player.setMp(sprite.player.getMaxMp());
                }
            }
            //每次战斗佣兵自动恢复
            if(sprite.id < 0){
            	if(sprite.hp < sprite.attributes[sprite.ATTR_HPMAX]){
                	sprite.hp = sprite.attributes[sprite.ATTR_HPMAX];
                }
                if(sprite.mp < sprite.attributes[sprite.ATTR_MPMAX]){
                	sprite.mp = sprite.attributes[sprite.ATTR_MPMAX];
                }
                //设置技能
                MercenaryPlayer mplayer = (MercenaryPlayer)player;
                sprite.skillList = mplayer.getSkillList();
                sprite.skill = Skill.ATTACK_SKILL;
            }
            if(isWorldBoss){
            	int addAP = WorldBossConfig.getPlayerAPPercent((WorldPlayer)sprite.player);
            	if(addAP > 0){
	            	sprite.attributes[BattleSprite.ATTR_PMIN] += sprite.attributes[BattleSprite.ATTR_PMIN] * addAP / 100;
	            	sprite.attributes[BattleSprite.ATTR_MMIN] += sprite.attributes[BattleSprite.ATTR_MMIN] * addAP / 100;
	            	sprite.attributes[BattleSprite.ATTR_PMAX] += sprite.attributes[BattleSprite.ATTR_PMAX] * addAP / 100;
	            	sprite.attributes[BattleSprite.ATTR_MMAX] += sprite.attributes[BattleSprite.ATTR_MMAX] * addAP / 100;
            	}
            }
        }
        sprite.initIntervene(player,sprite);
        return sprite;
    }

    protected BattleSprite initPet(IPlayerData player, BattleSprite owner,int pkflag,boolean isBossRush, boolean isWorldBoss) {
        Pet pet = player.getPet();
        if (pet != null && pet.getFavor() > 30) {
            BattleSprite sprite = new BattleSprite();
            
            int vit = pet.getRealVitality();
            int str = pet.getRealStrength();
            int inte = pet.getRealIntelligence();
            int agi = pet.getRealAgility();
            sprite.initBattleData(BattleSprite.TYPE_PLAYER_PET, pet.getLevel(),
                    vit, 
                    str,
                    inte, 
                    agi, 0,
                    pet.getHp(), pet.getMp(), player.getVianyType(), 0, 0, null,null,null,null,null);
            sprite.id = pet.getId();
            sprite.pet = pet;
            sprite.skillList = new short[0];
            sprite.setStatus(BattleSprite.SEAL_SKILL_ATTACK, false);
            sprite.setStatus(BattleSprite.SEAL_SKILL_SKILL, false);
            sprite.setStatus(BattleSprite.SEAL_SKILL_DEF,false);
			try{
				
				IEquipment[] equips = new IEquipment[pet.getUsedEquipments().length];
				for(int jj = 0;jj<equips.length;jj++){
					if (pet.getUsedEquipments()[jj] != null){
						equips[jj] = (IEquipment)pet.getUsedEquipments()[jj].item;
					}
				}
		        sprite.initPetEquipData(equips,pkflag,pet.getEvolutionLevel());
		        // sprite.addDiamondData(equips,pkflag);
			}catch (Exception e) {
				
			}
			sprite.level = (short) pet.getLevel();
            sprite.battleSuitEffect = owner.splitePetEffect();
            if(isBossRush || isWorldBoss){
            	if(sprite.hp < sprite.attributes[sprite.ATTR_HPMAX]){
                	sprite.hp = sprite.attributes[sprite.ATTR_HPMAX];
                	sprite.pet.setHp(sprite.pet.getMaxHp());
                }
                if(sprite.mp < sprite.attributes[sprite.ATTR_MPMAX]){
                	sprite.mp = sprite.attributes[sprite.ATTR_MPMAX];
                	sprite.pet.setMp(sprite.pet.getMaxMp());
                }
            }
            return sprite;
        }
        return null;
    }

    protected BattleSprite initMonster(Monster monster,boolean isBossRush, boolean isWorldBoss) {
        BattleSprite sprite = new BattleSprite();
        sprite.initBattleData((byte) 1, monster.getLevel(), monster.getVit(),
                              monster.getStr(), monster.getInt(),
                              monster.getAgi(), 0, 0, 0, 0, 0, 0, null,null,null,null,null);
        sprite.initSpecial(monster.getPMinAttack(), monster.getPMaxAttack(),
                           monster.getPDef(), monster.getMMinAttack(),
                           monster.getMMaxAttack(), monster.getMDef(),
                           monster.getParry(), monster
                           .getHit(), monster.getPCritial(),
                           monster.getMCritial(), monster.getHp(),
                           monster.getMp());
        sprite.skillList = new short[0];
        sprite.ai = AiService.getAi(monster.getAiClass());
        if(isBossRush){
	        if(monster.getSpecialHP() > 0){
	        	sprite.attributes[BattleSprite.ATTR_HPMAX] = monster.getSpecialHP();
	        }
	        
	        if(monster.getSpecialMP() > 0){
	        	sprite.attributes[BattleSprite.ATTR_MPMAX] = monster.getSpecialMP();
	        }
        }else{
	        if(sprite.ai.getSpecialHp() > 0){
	            sprite.attributes[BattleSprite.ATTR_HPMAX] = sprite.ai.getSpecialHp();
	        }
	        
	        if(sprite.ai.getSpecialMp() > 0){
	            sprite.attributes[BattleSprite.ATTR_MPMAX] = sprite.ai.getSpecialMp();
	        }
        }
        if(isWorldBoss){
	        sprite.hp = WorldBossConfig.getBossHp();
	        sprite.mp = WorldBossConfig.getBossMp();
	        sprite.attributes[BattleSprite.ATTR_HPMAX] = WorldBossConfig.getBossMaxHp();
	        sprite.attributes[BattleSprite.ATTR_MPMAX] = WorldBossConfig.getBossMaxMp();
        }else{
	        sprite.hp = sprite.attributes[BattleSprite.ATTR_HPMAX];
	        sprite.mp = sprite.attributes[BattleSprite.ATTR_MPMAX];
        }
        sprite.ai = AiService.getAi(monster.getAiClass());
        monster.setMaxHp(sprite.hp);
        monster.setMaxMp(sprite.mp);
        sprite.monster = monster;
        sprite.skill = Skill.NOTREADY_SKILL;
        sprite.level = monster.getLevel();
        return sprite;
    }

    public abstract void start();

    public abstract void end();

    public abstract void abort();

    public abstract void roundEnd();

    public abstract void doTime(long time);

    public abstract void process(UWAPData data,int playerId);

    public abstract void catchToBattle(int playerId,int serial);

    public abstract IPlayerData[] getPlayers();

    protected void sendAbort(BattleSprite player, BattleSprite pet,int serial) {
        if (player != null) {
            UWAPSegment seg = new UWAPSegment(ClientConstants.BATTLE_ABORT,
                                              serial);
            seg.writeInt(getId());
            seg.writeInt(player.player.getHp());
            seg.writeInt(player.player.getMp());
            seg.writeInt(pet == null ? -1 : pet.pet.getHp());
            seg.writeInt(pet == null ? -1 : pet.pet.getMaxMp());
            service.getConnectService().writeTo(seg, player.player.getId());
        }
    }


    protected void prepareEnmities(BattleSprite[] our, BattleSprite[] ourPet,
                                   BattleSprite[] them, BattleSprite[] themPet) {
        for (int i = 0; i < our.length; i++) {
//            Skill skill = Skill.getSkill(our[i].skillId);
            Skill skill = our[i].skill;
            int enmity = skill == null ? 0 : skill.enmity;
            for (int j = 0; j < them.length; j++) {
                them[j].addEnmity(our[i], enmity);
                if (themPet[j] != null)
                    themPet[j].addEnmity(our[i], enmity);
                if (ourPet[i] != null) {
//                    Skill petSkill = Skill.getSkill(ourPet[i].skillId);
                    Skill petSkill = ourPet[i].skill;
//                    if(petSkill==null){
//                        log.info("ID["+our[i].player.getId()+"]Pet["+ourPet[i].pet.getId()+"]Skill["+ourPet[i].skill.id+"]Error");
//                    }
                    int petEnmity = petSkill == null ? 0: petSkill.enmity;
                    them[j].addEnmity(ourPet[i], petEnmity);
                    if (themPet[j] != null)
                        themPet[j].addEnmity(our[i], enmity);
                }
            }
        }
    }

    protected boolean testOurSideFromOrder(int oppGroup) {
        if (oppGroup == BattleSprite.GROUP_OUR ||
            oppGroup == BattleSprite.GROUP_OUR_PET) {
            return true;
        } else {
            return false;
        }
    }

    protected BattleSprite getSpriteFromOrder(int oppGroup, int oppIndex,
                                              BattleSprite[] our,
                                              BattleSprite[] them,
                                              BattleSprite[] ourPet,
                                              BattleSprite[] themPet) {
        BattleSprite result = null;

        switch (oppGroup) {
            case BattleSprite.GROUP_OUR:
                result = our[oppIndex];
                break;
            case BattleSprite.GROUP_THEM:
                result = them[oppIndex];
                break;
            case BattleSprite.GROUP_OUR_PET:
                result = ourPet[oppIndex];
                break;
            case BattleSprite.GROUP_THEM_PET:
                result = themPet[oppIndex];
                break;
        }
        return result;
    }

    protected boolean battleBout(BattleSprite[] our, BattleSprite[] them,
                                 BattleSprite[] ourPet, BattleSprite[] themPet,
                                 int bout) {
//        throw new NullPointerException();
        try {
            battleMovie.clear();
            spriteDoneSkill.clear();

            prepareEnmities(our, ourPet, them, themPet);
            //神圣宝辉减防
            updateDefEffectTime(our);
            updateDefEffectTime(ourPet);
            updateDefEffectTime(them);
            updateDefEffectTime(themPet);
            
            int[][] battleOrder = new int[our.length + them.length +
                                  ourPet.length + themPet.length][2]; //[0] 0：our，1：them，2: ourPete, 3: themPet, [1] index
            int offset = 0;

            for (int i = 0; i < our.length; i++) {
                battleOrder[i][0] = BattleSprite.GROUP_OUR;
                battleOrder[i][1] = i - offset;

                if (our[i - offset] != null) {
                    our[i - offset].groupIndex = i - offset;
                }
            }

            offset += our.length;

            for (int i = offset; i < offset + them.length; i++) {
                battleOrder[i][0] = BattleSprite.GROUP_THEM;
                battleOrder[i][1] = i - offset;

                if (them[i - offset] != null) {
                    them[i - offset].groupIndex = i - offset;
                }
            }

            offset += them.length;

            for (int i = offset; i < offset + ourPet.length; i++) {
                battleOrder[i][0] = BattleSprite.GROUP_OUR_PET;
                battleOrder[i][1] = i - offset;

                if (ourPet[i - offset] != null) {
                    ourPet[i - offset].groupIndex = i - offset;
                }
            }

            offset += ourPet.length;

            for (int i = offset; i < offset + themPet.length; i++) {
                battleOrder[i][0] = BattleSprite.GROUP_THEM_PET;
                battleOrder[i][1] = i - offset;

                if (themPet[i - offset] != null) {
                    themPet[i - offset].groupIndex = i - offset;
                }
            }

            battleOver = false;

            for (int i = 0; i < battleOrder.length; i++) {
                boolean flag = testOurSideFromOrder(battleOrder[i][0]);

                BattleSprite bs = getSpriteFromOrder(battleOrder[i][0],
                        battleOrder[i][1], our, them, ourPet, themPet);

                if (bs == null) {
                    continue;
                }

                if (bs.skill == Skill.NOTREADY_SKILL && !bs.testCannotBattle()) {
                    if (flag) {
                        battleOver = strategy.chooseSkill(bs, battleOrder[i][1],
                                our, them, ourPet, themPet, battleMovie, this,
                                round);
                    } else {
                        battleOver = strategy.chooseSkill(bs, battleOrder[i][1],
                                them, our, themPet, ourPet, battleMovie, this,
                                round);
                    }
                }

                if (battleOver) {
                    break;
                }
            }

            if (battleOver) {
                return true;
            }

            battleOver = false;

            for (int i = 0; i < battleOrder.length - 1; i++) {
                for (int j = i; j < battleOrder.length; j++) {
                    BattleSprite t1, t2;

                    t1 = getSpriteFromOrder(battleOrder[i][0], battleOrder[i][1],
                                            our, them, ourPet, themPet);
                    t2 = getSpriteFromOrder(battleOrder[j][0], battleOrder[j][1],
                                            our, them, ourPet, themPet);

                    if (t1 == null || t2 == null) {
                        if (t1 == null) {
                            int[] tmp = battleOrder[i];
                            battleOrder[i] = battleOrder[j];
                            battleOrder[j] = tmp;
                        }

                        continue;
                    }

                    int speed1 = t1.getSpeed();
                    int speed2 = t2.getSpeed();

                    if (speed1 < speed2) {
                        if ((t1.canAction() && t2.canAction()) ||
                            (!t1.canAction() && !t2.canAction())) {
                            int[] tmp = battleOrder[i];
                            battleOrder[i] = battleOrder[j];
                            battleOrder[j] = tmp;
                        } else if (!t1.canAction()) {
                            int[] tmp = battleOrder[i];
                            battleOrder[i] = battleOrder[j];
                            battleOrder[j] = tmp;
                        }
                    } else if (speed1 == speed2) {
                        if (Skill.getPercentRate(50)) {
                            int[] tmp = battleOrder[i];
                            battleOrder[i] = battleOrder[j];
                            battleOrder[j] = tmp;
                        }
                    }
                }
            }

            battleOver = false;
            battleRecorders = new Vector();

            for (int i = 0; i < battleOrder.length; i++) {
                boolean flag = testOurSideFromOrder(battleOrder[i][0]);

                BattleSprite bs = getSpriteFromOrder(battleOrder[i][0],
                        battleOrder[i][1], our, them, ourPet, themPet);

                if (bs == null) {
                    continue;
                }

                if (bs.skill != Skill.NOTREADY_SKILL && !bs.testCannotBattle()) {
                    if (flag) {
                        battleOver = strategy.doPoisonFrost(bs,
                                battleOrder[i][1], our, them, ourPet, themPet,
                                battleMovie, this);
                    } else {
                        battleOver = strategy.doPoisonFrost(bs,
                                battleOrder[i][1], them, our, themPet, ourPet,
                                battleMovie, this);
                    }

                    if (bs.testCannotBattle()) {
                        continue;
                    } else {
                        if (!bs.canAction()) {
                            if (flag) {
                                battleOver = strategy.chooseSkill(bs,
                                        battleOrder[i][1], our, them, ourPet,
                                        themPet, battleMovie, this, round);
                            } else {
                                battleOver = strategy.chooseSkill(bs,
                                        battleOrder[i][1], them, our, themPet,
                                        ourPet, battleMovie, this, round);
                            }

                            if (battleOver) {
                                break;
                            }
                        }

                        if (flag) {
                            battleOver = strategy.doSkill(bs, our, them, ourPet,
                                    themPet, battleMovie, bout, battleRecorders, this);
                        } else {
                            battleOver = strategy.doSkill(bs, them, our,
                                    themPet, ourPet, battleMovie, bout,
                                    battleRecorders, this);
                        }

                        if (battleOver) {
                            break;
                        }
                    }
                }

                if (battleOver) {
                    break;
                }
            }

            if (battleOver) {
                return true;
            }

            battleOver = true;

            for (int i = 0; i < our.length; i++) {
                if (our[i] == null) {
                    continue;
                }

                if (!our[i].testCannotBattle()) {
                    battleOver = false;

                    break;
                }
            }

            if (battleOver) {
                return true;
            }

            battleOver = true;

            for (int i = 0; i < them.length; i++) {
                if (them[i] == null) {
                    continue;
                }

                if (!them[i].testCannotBattle()) {
                    battleOver = false;

                    break;
                }
            }
            
          //小年AI系统
            if(them != null && them.length > 1 && them[0].ai != null && them[0].monster != null && them[0].monster.getAiClass().startsWith("Ai90016")){
            	//每回合角色会掉血掉蓝 先掉蓝再掉血
            	battleOver = true;
            	for(int i=0; i<our.length; i++){
            		if(our[i].player != null && !our[i].testDie()){
            			int maxmp = our[i].player.getMaxMp();
            			int submp = maxmp * Ai90016_1.subRoundMpPercent / 100;
            			//有足够的蓝可以扣除时 扣蓝 不然扣血
            			if(submp < our[i].mp){
            				our[i].setTarget(our[i], i, BattleSprite.TYPE_PLAYER);
            				our[i].changeMp(-submp);
            				int[] movie = BattleStrategy.makeMovieSub(our[i].bsType, our[i].groupIndex, our[i].target.bsType, our[i].targetIndex, Skill.SKILL_SUBMAGIC, Skill.ANIMATE_INC_MGC, Skill.POSITION_STAY, Skill.OVER_POSITION_BACK, Skill.MOVIE_SPEED_NORMAL, Skill.HIT_HIT,
            						our[i].target.getDebufStatus(), 0, 0, 0, 0, -submp);
            				battleMovie.addElement(movie);
            			}else{
            				int maxhp = our[i].player.getMaxHp();
            				int subhp = maxhp * Ai90016_1.subRoundHpPercent / 100;
            				our[i].setTarget(our[i], i, BattleSprite.TYPE_PLAYER);
            				our[i].changeHp(-subhp, battleMovie, null);
            				int[] movie = BattleStrategy.makeMovieSub(our[i].bsType, our[i].groupIndex, our[i].target.bsType, our[i].targetIndex, Skill.SKILL_NONE, Skill.ANIMATE_INC_MGC, Skill.POSITION_STAY, Skill.OVER_POSITION_BACK, Skill.MOVIE_SPEED_NORMAL, Skill.HIT_HIT,
            						our[i].target.getDebufStatus(), 0, 0, 0, -subhp, 0);
            				battleMovie.addElement(movie);
            			}
            			if(!our[i].testDie()){
        					battleOver = false;
        				}
            		}
            	}
            	//每个Boss回复所有蓝
            	for(int i=0; i<them.length; i++){
            		if(them[i].monster != null && !them[i].testDie()){
            			BattleSprite bs = them[i];
            			bs.setTarget(bs, i, BattleSprite.TYPE_MONSTER);
            			int mp = bs.monster.getMaxMp() - bs.mp;
            			bs.changeMp(mp);
            			int[] movie = BattleStrategy.makeMovieSub(bs.bsType, bs.groupIndex, bs.target.bsType, bs.targetIndex, Skill.SKILL_NONE, Skill.ANIMATE_INC_MGC, Skill.POSITION_STAY, Skill.OVER_POSITION_BACK, Skill.MOVIE_SPEED_NORMAL, Skill.HIT_HIT,
        						bs.target.getDebufStatus(), 0, 0, 0, 0, mp);
        				battleMovie.addElement(movie);
            		}
            	}
            	if(!battleOver){
            		Ai90016_1 tmpAi = (Ai90016_1)them[0].ai;
            		
            		//第一次小怪全死时 Boss减少当前血量除以10的血
            		if(tmpAi.dieState == 0 && them[0].testDie() && them[2].testDie()){
            			tmpAi.dieState = 1;
            			if(them[1].hp >= 10){
            				int hp = them[1].hp - them[1].hp / 10;
            				them[1].changeHp(-hp, battleMovie, null);
            				int[] movie = BattleStrategy.makeMovieSub(them[1].bsType, them[1].groupIndex, them[1].target.bsType, them[1].targetIndex, Skill.SKILL_SUBMAGIC, Skill.ANIMATE_INC_MGC, Skill.POSITION_STAY, Skill.OVER_POSITION_BACK, Skill.MOVIE_SPEED_NORMAL, Skill.HIT_HIT,
            						them[1].target.getDebufStatus(), 0, 0, 0, -hp, 0);
            				battleMovie.addElement(movie);
            			}
            		}
            		
            		boolean resu = false;
            		int resuindex = 0;
            		//检测是否需要复活
            		if(!them[0].testDie() && them[2].testDie()){
	        			Ai90016_1 ai = (Ai90016_1)them[0].ai;
	        			if(ai.bossxDieRoundRef >= 2){
	        				resu = true;
	        				ai.bossxDieRoundRef = 0;
	        				resuindex = 2;
	        			}else{
	        				ai.bossxDieRoundRef ++;
	        			}
            		}
            		if(!them[2].testDie() && them[0].testDie()){
	        			Ai90016_1 ai = (Ai90016_1)them[2].ai;
	        			if(ai.bossxDieRoundRef >= 2){
	        				resu = true;
	        				ai.bossxDieRoundRef = 0;
	        				resuindex = 0;
	        			}else{
	        				ai.bossxDieRoundRef ++;
	        			}
            		}
            		if(resu){
            			Ai90016_1 ai0 = (Ai90016_1)them[0].ai;
            			Ai90016_1 ai1 = (Ai90016_1)them[1].ai;
            			Ai90016_1 ai2 = (Ai90016_1)them[2].ai;
        				them[0].reLive();
        				them[2].reLive();
        				them[0].changeHp(ai0.levelSum * ai0.bossHpMul * ai0.bossxHpInBossPercent / 100, battleMovie, null);
        				them[2].changeHp(ai2.levelSum * ai2.bossHpMul * ai2.bossxHpInBossPercent / 100, battleMovie, null);
        				ai0.addHurtPercentBoss += 50;
        				ai1.addHurtPercentBoss += 50;
        				ai2.addHurtPercentBoss += 50;
        				BattleSprite target = resuindex == 0 ? them[0] : them[2];
        				BattleSprite bs = them[0];
        				bs.setTarget(target, resuindex, BattleSprite.TYPE_MONSTER);
        				Skill.processSaveLifeMovie(bs, 0, bs.hp, Skill.ATTACK_NO_CRI, battleMovie, null);
        				bs = them[2];
        				bs.setTarget(target, resuindex, BattleSprite.TYPE_MONSTER);
        				Skill.processSaveLifeMovie(bs, 2, bs.hp, Skill.ATTACK_NO_CRI, battleMovie, null);
            		}
            	}
            }

            if (battleOver) {
                return true;
            }
            
            battleOver = checkPlayerDie();
            if(battleOver){
            	return true;
            }

            return false;
        } finally {
            Enumeration emu = spriteDoneSkill.keys();

            while (emu.hasMoreElements()) {
                BattleSprite bs = (BattleSprite) emu.nextElement();
                Integer groupIndex = (Integer) spriteDoneSkill.get(bs);

                bs.processBattleBuf(battleMovie, groupIndex.intValue(), this);
            }
        }
    }

    public void clearBourt() {
        for (int i = 0; i < side1.length; i++) {
            BattleSprite bs = side1[i];

            if (bs == null) {
                continue;
            }
            bs.usedItem = null;
            bs.used = false;
            bs.clearBout(battleMovie, i, this);
        }

        for (int i = 0; i < side2.length; i++) {
            BattleSprite bs = side2[i];

            if (bs == null) {
                continue;
            }
            bs.usedItem = null;
            bs.used = false;
            bs.clearBout(battleMovie, i, this);
        }

        for (int i = 0; i < pet1.length; i++) {
            BattleSprite bs = pet1[i];

            if (bs == null) {
                continue;
            }

            bs.clearBout(battleMovie, i, this);
        }

        for (int i = 0; i < pet2.length; i++) {
            BattleSprite bs = pet2[i];

            if (bs == null) {
                continue;
            }

            bs.clearBout(battleMovie, i, this);
        }
    }

//    public IBattleHandler getHandler(){
//        return handler;
//    }

    public BattleStrategy getStrategy() {
        return strategy;
    }

    public BattleService2 getService() {
        return service;
    }

    public int getId() {
        return id;
    }

    public BattleSprite[] getSide1() {
        return side1;
    }


    public BattleSprite[] getSide2() {
        return side2;
    }

    public int getRound() {
        return round;
    }

    public abstract BattleSprite getSprite(int spriteType, int spriteIndex);

    public void spriteDoneSkill(BattleSprite bs, int index, boolean force) {
        if (force ||
            ((bs.getDebufStatus() != Skill.STATUS_NORMAL ||
              bs.getBufStatus() != Skill.STATUS_NORMAL ||
              bs.bufTable.size() > 2) && !bs.testCannotBattle())) {
            spriteDoneSkill.put(bs, new Integer(index));
        }
    }

    protected int getItemFlag(BattleSprite bs){
        if(bs.used && bs.usedItem != null){
            return (bs.usedItem.getItemId() << 16) | 1;
        }
        return 0;
    }

    public int hashCode(){
        return id;
    }

    protected boolean checkPetAction(BattleSprite owner,BattleSprite bs, int action){
        if(action == Skill.SKILL_NOT_READY){
            bs.setSkill(Skill.STAY_SKILL);
            return false;
        }else{
            if(!bs.canAction()){
                if(action != Skill.SKILL_STAY){
                    bs.setSkill(Skill.ATTACK_SKILL);
                    bs.setTarget(bs,bs.groupIndex);
                    log.info("ID[" + owner.id + "]BattleError PetAction Skill["+action+"]");
                    return false;
                }else{
                    bs.setSkill(Skill.STAY_SKILL);
                    return false;
                }
            }else{
                Skill skill = Skill.getSkill(action);
                if(skill==null){
                    bs.setSkill(Skill.ATTACK_SKILL);
                    bs.setTarget(bs,bs.groupIndex);
                    log.info("ID["+owner.id+"]BattleError PetSkillNotFound Skill["+action+"]");
                    return false;
                }else{
                    if(hasPetSkill(bs,skill)){
                        int[] status = Skill.getSkillStatus(bs, action);
                        if (status[0] != Skill.CAN_SELECT_SKILL || status[1] > 0 || status[2] > 0) {
                            bs.setSkill(Skill.ATTACK_SKILL);
                            bs.setTarget(bs, bs.groupIndex);
                            log.info("ID[" + owner.id +
                                     "]BattleError PetSkillCantSelect Skill[" +
                                     action + "]");
                            return false;
                        }else{
                            bs.setSkill(skill);
                            return true;
                        }
                    } else {
                        bs.setSkill(Skill.ATTACK_SKILL);
                        bs.setTarget(bs, bs.groupIndex);
                        log.info("ID[" + owner.id +
                                 "]BattleError PetSkillNotContain Skill[" +
                                 action + "]");
                        return false;
                    }
                }
            }
        }
    }

    protected boolean checkAction(BattleSprite bs, int action, int itemId) {
        if (action == Skill.SKILL_NOT_READY) {
            bs.setSkill(Skill.STAY_SKILL);
            return false;
        }
        if (!bs.canAction()) {
            if (action != Skill.SKILL_STAY) {
                bs.setSkill(Skill.ATTACK_SKILL);
                bs.setTarget(bs, bs.groupIndex);
                log.info("ID[" + bs.id + "]BattleError Action Skill["+action+"]");
                return false;
            }else{
                bs.setSkill(Skill.STAY_SKILL);
                return false;
            }
        } else {
            if (action == Skill.SKILL_ITEM) {
                IItemTemplate template = Items.getTemplate(itemId);
                if (template == null) {
                    log.info("ID[" + bs.id +
                             "]BattleError UseItemNotFound ItemId[" + itemId+"]");
                    bs.setSkill(Skill.ATTACK_SKILL);
                    bs.setTarget(bs, bs.groupIndex);
                    return false;
                } else {
                    IItem item = template.newInstance();
                    if (bs.player.hasItem(item,1)) {
                        if(bs.getStatus(BattleSprite.SEAL_SKILL_ITEM)){
                            bs.setSkill(Skill.ATTACK_SKILL);
                            bs.setTarget(bs,bs.groupIndex);
                            log.info("ID[" + bs.id +
                                     "]BattleError UseItemCannotUse ItemId[" +
                                 itemId+"]");
                            return false;
                        }else{
                            bs.usedItem = item;
                            bs.setSkill(Skill.getSkill(action));
                            return true;
                        }
                    } else {
                        bs.setSkill(Skill.ATTACK_SKILL);
                        bs.setTarget(bs, bs.groupIndex);
                        log.info("ID[" + bs.id +
                                 "]BattleError UseItemNumber ItemId[" +
                                 itemId+"]");
                        return false;
                    }
                }
            } else {
                Skill skill = Skill.getSkill(action);
                if(skill==null){
                    bs.setSkill(Skill.ATTACK_SKILL);
                    bs.setTarget(bs,bs.groupIndex);
                    log.info("ID["+bs.id+"]BattleError SkillNotFound Skill["+action+"]");
                    return false;
                }else{
                    if(hasSkill(bs,skill)){
                        int[] status = Skill.getSkillStatus(bs, action);
                        if (status[0] != Skill.CAN_SELECT_SKILL || status[1] > 0 || status[2] > 0) {
                            bs.setSkill(Skill.ATTACK_SKILL);
                            bs.setTarget(bs, bs.groupIndex);
                            log.info("ID[" + bs.id +
                                     "]BattleError SkillCantSelect Skill[" +
                                     action + "]");
                            return false;
                        }else{
                            bs.setSkill(skill);
                            return true;
                        }
                    } else {
                        bs.setSkill(Skill.ATTACK_SKILL);
                        bs.setTarget(bs, bs.groupIndex);
                        log.info("ID[" + bs.id +
                                 "]BattleError SkillNotContain Skill[" +
                                 action + "]");
                        return false;
                    }
                }
            }
        }
    }

    protected boolean hasSkill(BattleSprite bs, Skill skill) {
        if (skill == Skill.ATTACK_SKILL || skill == Skill.CATCH_SKILL ||
            skill == Skill.ITEM_SKILL || skill == Skill.NOTREADY_SKILL ||
            skill == Skill.RUNAWAY_SKILL || skill == Skill.STAY_SKILL)
            return true;
        Ability ability = Ability.getAbility(skill.id);
        if(ability==null)
            return false;
        return bs.player.containsAbility(ability);
    }

    protected boolean hasPetSkill(BattleSprite bs,Skill skill){
        if (skill == Skill.ATTACK_SKILL  || skill == Skill.NOTREADY_SKILL || skill == Skill.STAY_SKILL){
            return true;
        }
        Ability ability = Ability.getAbility(skill.id);
        if(ability==null)
            return false;
        return bs.pet.hasAbility(ability);
    }
    
    /**
     * 检测是否玩家都死光了
     * @return
     */
    public boolean checkPlayerDie(){
    	boolean die = true;
    	if(side1[0].player != null && side1[0].player instanceof WorldPlayer){
	        for(int i = 0; i < side1.length; i++){
	            if(side1[i].player != null && side1[i].player instanceof WorldPlayer && !side1[i].testCannotBattle()){
	            	die = false;
	            	break;
	            }
	        }
    	}
        if(!die && side2[0].player != null && side2[0].player instanceof WorldPlayer){
        	die = true;
        	for(int i = 0; i < side2.length; i++){
                if(side2[i].player != null && side2[i].player instanceof WorldPlayer && !side2[i].testCannotBattle()){
                	die = false;
                	break;
                }
            }
        }
    	return die;
    }
    
    public void updateDefEffectTime(BattleSprite[] bs){
    	for(int i = 0; i < bs.length; i++){
    		if(bs[i] != null && !bs[i].testCannotBattle()){
    			bs[i].updateCurDefEffectRate();
    		}
    	}
    }
}