package com.pip.itimes.server.world.battle;

import java.util.Random;
import java.util.Vector;

import com.pip.itimes.net.*;
import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import org.apache.log4j.Logger;

/**
 * @author Jeffrey
 * @version 1.0
 */
public abstract class AbstractMultiPkBattle extends Battle2 {

    static final Logger log = Logger.getLogger(AbstractPkBattle.class);

    protected boolean force = false;

    protected int serial = 0;

    protected IPlayerData winner[] = null;
    protected IPlayerData failure[] = null;
    private static final int MAX_IDLE = 3;
    public AbstractMultiPkBattle(int id, BattleService2 service,
                            BattleStrategy strategy, boolean force,
                            IPlayerData[] players1, IPlayerData[] players2, int serial) {
        super(id, service, strategy);
        this.force = force;
        this.serial = serial;
        init(players1, players2);
    }

    protected void init(IPlayerData[] players1, IPlayerData[] players2) {
        side1 = new BattleSprite[players1.length];
        pet1 = new BattleSprite[players1.length];
        for(int i = 0; i < players1.length; i++){
            side1[i] = initPlayer(players1[i],true);
            pet1[i] = initPet(players1[i], side1[i],true);
        }
        side2 = new BattleSprite[players2.length];
        pet2 = new BattleSprite[players2.length];
        for(int i = 0; i < players2.length; i++){
            side2[i] = initPlayer(players2[i],false);
            pet2[i] = initPet(players2[i], side2[i],false);
        }
    }

    protected BattleSprite initPlayer(IPlayerData p, boolean isPlayer) {
        BattleSprite ret = super.initPlayer(p,false,false);
        if (!isPlayer){
            ret.bsType = BattleSprite.TYPE_MONSTER;
        }
        ret.setStatus(BattleSprite.SEAL_SKILL_ATTACK, false);
        ret.setStatus(BattleSprite.SEAL_SKILL_SKILL, false);
        ret.setStatus(BattleSprite.SEAL_SKILL_ITEM, true);
        ret.setStatus(BattleSprite.SEAL_SKILL_RUNAWAY,true);
        ret.setStatus(BattleSprite.SEAL_SKILL_CATCH,true);
        return ret;
    }

    protected BattleSprite initPet(IPlayerData p, BattleSprite owner, boolean isPlayer) {
    	//pk宠物装备特殊处理-攻击减半 mengjie add
    	BattleSprite ret = initPet(p, owner,1,false, false);
        if (ret != null && !isPlayer)
            ret.bsType = BattleSprite.TYPE_MONSTER_PET;
        if (ret != null && isPlayer) {
            ret.setStatus(BattleSprite.SEAL_SKILL_ATTACK, false);
            ret.setStatus(BattleSprite.SEAL_SKILL_SKILL, false);
            ret.setStatus(BattleSprite.SEAL_SKILL_DEF,false);
            ret.setStatus(BattleSprite.SEAL_SKILL_RUNAWAY,true);
        }
        return ret;
    }

    public IPlayerData[] getWinner() {
        return winner;
    }

    public IPlayerData[] getFailure() {
        return failure;
    }

    public boolean isSide1(IPlayerData[] players){
        return side1[0].player.getId()==players[0].getId();
    }

    public void process(UWAPData data, int playerId) {
        byte type = data.getAppType();
        if (type == ClientConstants.PK_FIGHT) {
            try {
                short roundId = data.readShort();
                int action = data.readInt();
                byte target = data.readByte();
                int petAction = data.readInt();
                byte petTarget = data.readByte();
                fight(playerId, roundId, action, target, petAction, petTarget);
            } catch (IllegalAccessException ex) {
            }
        } else if (type == ClientConstants.PK_OK) {
            ok(playerId);
        } else if (type == ClientConstants.PK_REFUSE) {
            try {
                byte code = data.readByte();
                String cause = data.readString();
                refuse(playerId, code, cause);
            } catch (IllegalAccessException ex1) {
            }
        }
    }

    protected abstract void ok(int playerId);


    public synchronized void doTime(long time) {
        if (status == STATUS.end)
            return;
        long t = time - lastTime;
        if (status == STATUS.wait_start && t > 60 * 1000L) {
            refuse(side2[0].id, (byte) 1, "对方没有回应");
        } else if (status == STATUS.wait_fight && t > Utils.ROUND_TIME_LIMIT) {
        	//mengjie add 三回合自动出手，死亡。
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
        	for(int i = 0; i < side2.length; i++){
                if(!side2[i].ready){
                    side2[i].idleRound++;
                    if(side2[i].idleRound>=MAX_IDLE){
                        if(!side2[i].testCannotBattle()){
                            side2[i].setSkill(Skill.STAY_SKILL);
                            side2[i].changeHp( -side2[i].attributes[BattleSprite.
                                            ATTR_HPMAX], battleMovie, this);
                        }
                        if(pet2[i]!=null&&!pet2[i].testCannotBattle()){
                            pet2[i].setSkill(Skill.STAY_SKILL);
                            pet2[i].changeHp( -pet2[i].attributes[BattleSprite.
                                            ATTR_HPMAX], battleMovie, this);
                        }
                    }else{
                        if(side2[i].canAction()){
                            side2[i].setSkill(Skill.ATTACK_SKILL);
                            side2[i].setTarget(side1[0], 0);
                        }
                        if(pet2[i]!=null&&pet2[i].canAction()){
                            pet2[i].setSkill(Skill.ATTACK_SKILL);
                            pet2[i].setTarget(side1[0], 0);
                        }
                    }

                    side2[i].ready = true;
                }
            }
            setDefaultAction(side1,pet1,side2);
            setDefaultAction(side2,pet2,side1);
            try {
                battleBout(side1, side2, pet1, pet2, round);
                roundEnd();
                clearBourt();
            } catch (Exception ex) {
                caughtException(ex);
            }
        }

    }

    protected void setDefaultAction(BattleSprite[] bs1, BattleSprite[] bsPet,
                                    BattleSprite[] bs2) {
        for(int i=0;i<bs1.length;i++){
            if (!bs1[i].ready) {
                if (bs1[i].canAction()) {
                    bs1[i].setSkill(Skill.ATTACK_SKILL);
                    bs1[i].setTarget(bs2[0], 0);
                    if (bsPet[i] != null && bsPet[i].canAction()) {
                        bsPet[i].setSkill(Skill.ATTACK_SKILL);
                        bsPet[i].setTarget(bs2[0], 0);
                    }
                }
            }
        }
    }

    protected abstract void refuse(int playerId, byte code, String cause);


    protected void sendRefuse(int playerId, byte code, String cause) {
        UWAPSegment seg = new UWAPSegment(ClientConstants.PK_REFUSE, serial);
        seg.write(code);
        seg.writeString(cause);
        seg.writeInt(id);
        service.getConnectService().writeTo(seg, playerId);
    }

    protected BattleSprite getTarget(int playerId, byte index) {
        try {
            if (isSide1(playerId)) {
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
            } else {
                if (index < -10) {
                    int ii = ( -index) - 10;
                    return pet1[ii - 1];
                }
                if (index > 10) {
                    return pet2[index - 10 - 1];
                }
                if (index < 0) {
                    return side1[( -index) - 1];
                }
                if (index > 0) {
                    return side2[index - 1];
                }
            }
        } catch (Exception ex) {
        }
        return null;
    }

    protected boolean isSide1(int playerId){
        for(int i=0;i<side1.length;i++){
            if(side1[i].id==playerId)
                return true;
        }
        return false;
    }

    public synchronized void fight(int playerId, short roundId, int action,
                                   byte target, int petAction, byte petTarget) {
        if (status != STATUS.wait_fight)
            return;
        if(roundId!=round)
            return;
        BattleSprite bs = getBattlePlayer(playerId);
        if(bs!=null){
            BattleSprite pet = getBattlePet(playerId);
            bs.idleRound = 0;
            int realAction = (short) (action & 0xFFFF);
            int itemId = (action >> 16) & 0xFFFF;
            if(checkAction(bs,realAction,itemId)){
                bs.setTarget(getTarget(playerId,target), getTargetIndex(target));
            }
            int realPetAction = (short) (petAction & 0xFFFF);
            if (realPetAction == Skill.SKILL_NOT_READY) {
                realPetAction = Skill.SKILL_STAY;
            }
            if (pet != null) {
                if (checkPetAction(bs,pet, realPetAction)) {
                    pet.setTarget(getTarget(playerId,petTarget),
                                          getTargetIndex(petTarget));
                }
            }

            bs.ready = true;
            try {
                if (isReady()) {
                    battleBout(side1, side2, pet1, pet2, round);
                    roundEnd();
                    clearBourt();
                }
            } catch (Exception ex) {
                caughtException(ex);
            }
        }
    }


    private int getTargetIndex(byte index) {
        if (index < -10)
            return ( -index) - 10 - 1;
        if (index > 10)
            return index - 10 - 1;
        if (index < 0)
            return ( -index) - 1;
        if (index > 0)
            return index - 1;
        return 0;
    }

//    private BattleSprite getTarget(byte index) {
//        try {
//            if (index < -10) {
//                int ii = ( -index) - 10;
//                return pet2[ii - 1];
//            }
//            if (index > 10) {
//                return pet1[index - 10 - 1];
//            }
//            if (index < 0) {
//                return side2[( -index) - 1];
//            }
//            if (index > 0) {
//                return side1[index - 1];
//            }
//        } catch (Exception ex) {
//            log.info("Target Index[" + index + "] Error");
//        }
//        return null;
//    }


    protected boolean isReady() {
        for (int i = 0; i < side1.length; i++) {
            if (!side1[i].ready)
                return false;
        }
        for(int i=0;i<side2.length;i++){
            if(!side2[i].ready)
                return false;
        }
        return true;
    }


    protected BattleSprite getBattlePlayer(int id){
        for(int i=0;i<side1.length;i++){
            if(side1[i].id==id)
                return side1[i];
        }
        for(int i=0;i<side2.length;i++){
            if(side2[i].id==id)
                return side2[i];
        }
        return null;
    }

    protected BattleSprite getDefaultTarget(int id){
        for(int i=0;i<side1.length;i++){
            if(side1[i].id==id)
                return side2[0];
        }
        for (int i = 0; i < side2.length; i++) {
            if (side2[i].id == id)
                return side1[0];
        }
        return null;
    }

    protected BattleSprite getBattlePet(int id){
        for(int i=0;i<side1.length;i++){
            if(side1[i].id==id)
                return pet1[i];
        }
        for(int i=0;i<side2.length;i++){
            if(side2[i].id==id)
                return pet2[i];
        }
        return null;
    }

    protected UWAPSegment getRoundEndSegment(int id,byte type){
        BattleSprite[] p1 = side1;
        BattleSprite[] pe1 = pet1;
        BattleSprite[] p2 = side2;
        BattleSprite[] pe2 = pet2;
        if(id==side2[0].id){
            p1 = side2;
            pe1 = pet2;
            p2 = side1;
            pe2 = pet1;
        }
        UWAPSegment seg = new UWAPSegment(ClientConstants.PK_ROUND_END, serial);
        seg.write(type);
        seg.writeInt(getId());
        if(type==1){
            seg.writeShort((short)(round-1));
        }else
            seg.writeShort((short)round);
        if(type==0){
            if(id==side2[0].id){
                makeMovie(battleMovie);
            }
            seg.write((byte) battleMovie.size());
            for (int i = 0; i < battleMovie.size(); i++) {
                seg.writeInts((int[]) battleMovie.get(i));
            }
        }
        byte index = 1;
        for (int i = 0; i < p1.length; i++) {
            seg.write((byte) index++);
            seg.writeInt(p1[i].getAllStatus());
            seg.writeInt(p1[i].hp);
            seg.writeInt(p1[i].mp);
            seg.writeBoolean(p1[i].canAction());
            seg.writeInt(getItemFlag(p1[i]));
            seg.writeString(p1[i].getSkillName());
            if (pe1[i] != null) {
                seg.writeInt(pe1[i].hp);
                seg.writeInt(pe1[i].mp);
                seg.writeInt(pe1[i].getAllStatus());
                seg.writeBoolean(pe1[i].canAction());
                seg.writeString(pe1[i].getSkillName());
            } else {
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeBoolean(false);
                seg.writeString("");
            }
        }
        index = -1;
        for (int i = 0; i < p2.length; i++) {
            seg.write((byte) index--);
            seg.writeInt(p2[i].getAllStatus());
            seg.writeInt(p2[i].hp);
            seg.writeInt(p2[i].mp);
            seg.writeBoolean(p2[i].canAction());
            seg.writeInt(getItemFlag(p2[i]));
            seg.writeString(p2[i].getSkillName());
            if (pe2[i] != null) {
                seg.writeInt(pe2[i].hp);
                seg.writeInt(pe2[i].mp);
                seg.writeInt(pe2[i].getAllStatus());
                seg.writeBoolean(pe2[i].canAction());
                seg.writeString(pe2[i].getSkillName());
            } else {
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeBoolean(false);
                seg.writeString("");
            }
        }

        if(type==0)
            seg.write(getRoundStatus(p1));
        
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

    protected byte getRoundStatus(BattleSprite[] bs) {
        if (!battleOver) {
            return 0;
        } else {
            boolean win = true;
            for (int i = 0; i < bs.length; i++) {
                if (!bs[i].testCannotBattle()) {
                    win = false;
                }
            }
            if (win)
                return 1;
            else
                return 2;
        }
    }

    public void roundEnd() {
        for (int i = 0; i < side1.length; i++) {
            strategy.fillSpriteStatus(side1[i], this);
        }
        for (int i = 0; i < pet1.length; i++) {
            if (pet1[i] != null) {
                strategy.fillSpriteStatus(pet1[i], this);
            }
        }
        for (int i = 0; i < side2.length; i++) {
            strategy.fillSpriteStatus(side2[i], this);
        }
        for (int i = 0; i < pet2.length; i++) {
            if (pet2[i] != null) {
                strategy.fillSpriteStatus(pet2[i], this);
            }
        }
        UWAPSegment seg = getRoundEndSegment(side1[0].id,(byte)0);
        broadcast(seg,side1);
        seg = getRoundEndSegment(side2[0].id,(byte)0);
        broadcast(seg,side2);
        resetFlag();
        battleMovie.clear();
        if (battleOver) {
            if (isWinner(side1)) {
                winner = getPlayers(side1);
                failure = getPlayers(side2);
            } else {
                winner = getPlayers(side2);
                failure = getPlayers(side1);
            }
            end();
            this.status = STATUS.end;
            lastTime = System.currentTimeMillis();
            service.removeBattle(this);
            
            //为统计平台计数
            Server.realtimeStatService.fightCounter++;
        } else {
            lastTime = System.currentTimeMillis();
            status = STATUS.wait_fight;
            this.round++;
        }
    }

    protected boolean isWinner(BattleSprite[] side){
        for(int i=0;i<side.length;i++){
            if(side[i].player != null && side[i].player instanceof WorldPlayer && (!side[i].testDie()))
                return true;
        }
        return false;
    }

    protected void resetFlag(){
        for(int i=0;i<side1.length;i++){
        	side1[i].used = false;
        	side1[i].usedItem = null;
        	if(side1[i].id < 0){
        		mercenaryReady(side1, side2, pet1, pet2, i);
        	}else{
		        side1[i].ready = false;
        	}
        }
        for(int i=0;i<side2.length;i++){
            side2[i].used = false;
            side2[i].usedItem = null;
            if(side2[i].id < 0){
            	mercenaryReady(side2, side1, pet2, pet1, i);
            }else{
            	side2[i].ready = false;
            }
        }
    }
    
    private void mercenaryReady(BattleSprite side1[], BattleSprite side2[], BattleSprite pet1[], BattleSprite pet2[], int i){
    	side1[i].ready = true;
		BattleSprite target = null;
		Random rnd = new Random();
		int index = -1;
		if(side1[i].skillList.length > 0){
			index = Utils.getRandom(rnd, 0, side1[i].skillList.length - 1);
		}
		Skill skill = null;
		if(index != -1){
			skill = Skill.getSkill(side1[i].skillList[index]);
		}
		if(skill != null && skill.getMpUse(side1[i]) < side1[i].mp){
			side1[i].setSkill(skill);
			int[] ret = strategy.getSkillStatus(side1[i], skill.id);
			if(ret[3] == SkillConstants.CHOOSE_ENEMY){
				target = strategy.selectTargetRandom(side1[i], side2, pet2);
			}else if(ret[3] == SkillConstants.CHOOSE_FRIEND){
				target = strategy.selectTargetRandom(side1[i], side1, pet1);
			}else{
				side1[i].setSkill(Skill.ATTACK_SKILL);
				target = strategy.selectTargetRandom(side1[i], side2, pet2);
			}
		}else{
			side1[i].setSkill(Skill.ATTACK_SKILL);
		}
		if(target != null){
			side1[i].setTarget(target, target.groupIndex);
		}
    }

    private void broadcast(UWAPSegment seg,BattleSprite[] bs) {
        for (int i = 0; i < bs.length; i++) {
        	if(bs[i].id < 0){
        		bs[i].ready = true;
        	}else{
        		service.getConnectService().writeTo(seg, bs[i].id);
        	}
        }
    }


    protected IPlayerData[] getPlayers(BattleSprite[] bs){
    	IPlayerData[] ret = new IPlayerData[bs.length];
        for(int i=0;i<ret.length;i++){
            ret[i] = bs[i].player;
        }
        return ret;
    }

    public void makeMovie(Vector movie) {
        for (int i = 0; i < movie.size(); i++) {
            int[] m = (int[]) movie.get(i);
            if (m[0] == m[2]) {
                if (m[0] == BattleSprite.TYPE_PLAYER) {
                    m[0] = m[2] = BattleSprite.TYPE_MONSTER;
                } else if (m[0] == BattleSprite.TYPE_MONSTER) {
                    m[0] = m[2] = BattleSprite.TYPE_PLAYER;
                } else if (m[0] == BattleSprite.TYPE_PLAYER_PET) {
                    m[0] = m[2] = BattleSprite.TYPE_MONSTER_PET;
                } else if (m[0] == BattleSprite.TYPE_MONSTER_PET) {
                    m[0] = m[2] = BattleSprite.TYPE_PLAYER_PET;
                }
            } else {
                if (m[0] == BattleSprite.TYPE_PLAYER) {
                    m[0] = BattleSprite.TYPE_MONSTER;
                } else if (m[0] == BattleSprite.TYPE_PLAYER_PET) {
                    m[0] = BattleSprite.TYPE_MONSTER_PET;
                } else if (m[0] == BattleSprite.TYPE_MONSTER) {
                    m[0] = BattleSprite.TYPE_PLAYER;
                } else if (m[0] == BattleSprite.TYPE_MONSTER_PET) {
                    m[0] = BattleSprite.TYPE_PLAYER_PET;
                }

                if (m[2] == BattleSprite.TYPE_PLAYER) {
                    m[2] = BattleSprite.TYPE_MONSTER;
                } else if (m[2] == BattleSprite.TYPE_PLAYER_PET) {
                    m[2] = BattleSprite.TYPE_MONSTER_PET;
                } else if (m[2] == BattleSprite.TYPE_MONSTER) {
                    m[2] = BattleSprite.TYPE_PLAYER;
                } else if (m[2] == BattleSprite.TYPE_MONSTER_PET) {
                    m[2] = BattleSprite.TYPE_PLAYER_PET;
                }
            }
        }
    }

    protected byte getRoundStatus(BattleSprite p, BattleSprite pet) {
        if (!battleOver)
            return 0;
        if (p.hp > 0 || (pet != null && pet.hp > 0))
            return 1;
        else
            return 2;
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


    protected void caughtException(Exception ex) {
        log.error(ex, ex);
        abort();
    }

    protected void sendPkStart() {
        UWAPSegment seg = getPkStartSegment(serial);
        broadcast(seg,side1);
        broadcast(seg,side2);
//        service.getConnectService().writeTo(seg, side1[0].id);
//        service.getConnectService().writeTo(seg, side2[0].id);
    }

    protected UWAPSegment getPkStartSegment(int serial) {
        UWAPSegment seg = new UWAPSegment(ClientConstants.PK_START,
                                          serial);
        seg.writeInt(id);

        seg.write((byte)side1.length);
        for(int i=0;i<side1.length;i++){
            seg.writeInt(side1[i].id);
        }

        seg.write((byte)side1.length);
        for(int i=0;i<side1.length;i++){
            seg.writeInt(side1[i].id);
            seg.writeString(side1[i].name);
            seg.writeInt(side1[i].hp);
            seg.writeInt(side1[i].mp);
            seg.writeInt(side1[i].attributes[BattleSprite.ATTR_HPMAX]);
            seg.writeInt(side1[i].attributes[BattleSprite.ATTR_MPMAX]);
            seg.write(side1[i].face);
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
                seg.writeString(pet1[i].pet.getName());
                seg.writeInt(pet1[i].getAllStatus());
                seg.writeBoolean(pet1[i].canAction());
                //2代形象
                seg.write(pet1[i].pet.getBindType());
                seg.writeInt(pet1[i].level);
                seg.writeShort(pet1[i].pet.getColorIndex());
                //3代形象
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
                seg.writeInt(0);
                seg.writeShort((short)0);
                seg.writeInt(0);
                seg.writeInt(0);
            }
        }
        seg.write((byte)side2.length);
        for(int i=0;i<side2.length;i++){
            seg.writeInt(side2[i].id);
            seg.writeString(side2[i].name);
            seg.writeInt(side2[i].hp);
            seg.writeInt(side2[i].mp);
            seg.writeInt(side2[i].attributes[BattleSprite.ATTR_HPMAX]);
            seg.writeInt(side2[i].attributes[BattleSprite.ATTR_MPMAX]);
            seg.write(side2[i].face);
            IEquipment weapon = side2[i].player.getWeapon();
            if (weapon == null) {
                seg.write((byte) - 1);
            } else {
                seg.write((byte) weapon.getProperty(30, side2[i].level));
            }
            seg.writeInt(side2[i].getAllStatus());
            seg.writeBoolean(side2[i].canAction());
            seg.write(side2[i].player.getLightLevel());
            if (pet2[i] != null) {
                seg.write((byte) pet2[i].pet.getPetType());
                seg.writeInt(pet2[i].hp);
                seg.writeInt(pet2[i].mp);
                seg.writeInt(pet2[i].pet.getMaxHp());
                seg.writeInt(pet2[i].pet.getMaxMp());
                seg.writeString(pet2[i].pet.getName());
                seg.writeInt(pet2[i].getAllStatus());
                seg.writeBoolean(pet2[i].canAction());
                //2代形象
                seg.write(pet2[i].pet.getBindType());
                seg.writeInt(pet2[i].level);
                seg.writeShort(pet2[i].pet.getColorIndex());
                //3代形象
                seg.writeInt(pet2[i].pet.getEvolutionLevel());
                seg.writeInt(pet2[i].pet.getEvolutionType());
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
                seg.writeInt(0);
                seg.writeShort((short)0);
                seg.writeInt(0);
                seg.writeInt(0);
            }
        }
        seg.writeShort((short) round);
        
        return seg;
    }

    public synchronized void catchToBattle(int playerId,int serial) {
        if (status == STATUS.wait_fight) {
            UWAPSegment seg = getPkStartSegment(serial);
            service.getConnectService().writeTo(seg, playerId);
            seg = getRoundEndSegment(playerId, (byte) 1);
            service.getConnectService().writeTo(seg, playerId);
        }
    }

}
