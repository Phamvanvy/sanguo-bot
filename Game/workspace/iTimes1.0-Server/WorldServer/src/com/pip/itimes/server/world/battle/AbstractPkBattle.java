package com.pip.itimes.server.world.battle;

import java.util.Vector;

import com.pip.itimes.net.*;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import org.apache.log4j.Logger;

/**
 * @author Jeffrey
 * @version 1.0
 */
public abstract class AbstractPkBattle extends Battle2 {

    static final Logger log = Logger.getLogger(AbstractPkBattle.class);

    protected boolean force = false;

    protected int serial = 0;

    protected IPlayerData winner = null;
    protected IPlayerData failure = null;
    private static final int MAX_IDLE = 3;
    public AbstractPkBattle(int id, BattleService2 service,
                            BattleStrategy strategy, boolean force,
                            WorldPlayer p1, WorldPlayer p2, int serial) {
        super(id, service, strategy);
        this.force = force;
        this.serial = serial;
        init(p1, p2);
    }

    protected void init(WorldPlayer p1, WorldPlayer p2) {
        side1 = new BattleSprite[1];
        side2 = new BattleSprite[1];
        pet1 = new BattleSprite[1];
        pet2 = new BattleSprite[1];
        side1[0] = initPlayer(p1, true);
        side2[0] = initPlayer(p2, false);
        pet1[0] = initPet(p1, side1[0], true);
        pet2[0] = initPet(p2, side2[0], false);
    }

    protected BattleSprite initPlayer(WorldPlayer p, boolean isPlayer) {
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

    protected BattleSprite initPet(WorldPlayer p, BattleSprite owner, boolean isPlayer) {
    	//pk宠物装备特殊处理-攻击减半 mengjie add
        BattleSprite ret = initPet(p, owner,1,false,false);
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

    public IPlayerData getWinner() {
        return winner;
    }

    public IPlayerData getFailure() {
        return failure;
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
            if (!side1[0].ready) {
            	side1[0].idleRound++;
            	if(side1[0].idleRound>=MAX_IDLE){
            		if(!side1[0].testCannotBattle()){
                        side1[0].setSkill(Skill.STAY_SKILL);
                        side1[0].changeHp( -side1[0].attributes[BattleSprite.
                                        ATTR_HPMAX], battleMovie, this);
                    }
                    if(pet1[0]!=null&&!pet1[0].testCannotBattle()){
                        pet1[0].setSkill(Skill.STAY_SKILL);
                        pet1[0].changeHp( -pet1[0].attributes[BattleSprite.
                                        ATTR_HPMAX], battleMovie, this);
                    }
            	}else{
	                if (side1[0].canAction()) {
	                    side1[0].setSkill(Skill.ATTACK_SKILL);
	                    side1[0].setTarget(side2[0], 0);
	                    if (pet1[0] != null && pet1[0].canAction()) {
	                        pet1[0].setSkill(Skill.ATTACK_SKILL);
	                        pet1[0].setTarget(side2[0], 0);
	                    }
	                }
            	}
            }
            if (!side2[0].ready) {
            	side2[0].idleRound++;
            	if(side2[0].idleRound>=MAX_IDLE){
            		if(!side2[0].testCannotBattle()){
                        side2[0].setSkill(Skill.STAY_SKILL);
                        side2[0].changeHp( -side2[0].attributes[BattleSprite.
                                        ATTR_HPMAX], battleMovie, this);
                    }
                    if(pet2[0]!=null&&!pet2[0].testCannotBattle()){
                        pet2[0].setSkill(Skill.STAY_SKILL);
                        pet2[0].changeHp( -pet2[0].attributes[BattleSprite.
                                        ATTR_HPMAX], battleMovie, this);
                    }
            	}else{
            		if (side2[0].canAction()) {
                        side2[0].setSkill(Skill.ATTACK_SKILL);
                        side2[0].setTarget(side1[0], 0);
                        if (pet2[0] != null && pet2[0].canAction()) {
                            pet2[0].setSkill(Skill.ATTACK_SKILL);
                            pet2[0].setTarget(side1[0], 0);
                        }
                    }
            	}
            }
            try {
                battleBout(side1, side2, pet1, pet2, round);
                roundEnd();
                clearBourt();
            } catch (Exception ex) {
                caughtException(ex);
            }
        }

    }

    protected abstract void refuse(int playerId, byte code, String cause);
//        if(playerId == side2[0].id){
//            sendRefuse(side1[0].id,code,cause);
//            service.removeBattle(this);
//        }
//    }

    protected void sendRefuse(int playerId, byte code, String cause) {
        UWAPSegment seg = new UWAPSegment(ClientConstants.PK_REFUSE, serial);
        seg.write(code);
        seg.writeString(cause);
        seg.writeInt(id);
        service.getConnectService().writeTo(seg, playerId);
    }

    protected BattleSprite getTarget(int playerId, byte target) {
        try {
            if (side1[0].id == playerId) {
                if (target < -10) {
                    return pet2[0];
                } else if (target > 10) {
                    return pet1[0];
                } else if (target < 0) {
                    return side2[0];
                } else {
                    return side1[0];
                }
            } else {
                if (target < -10) {
                    return pet1[0];
                } else if (target > 10) {
                    return pet2[0];
                } else if (target < 0) {
                    return side1[0];
                } else {
                    return side2[0];
                }
            }
        } catch (Exception ex) {
        }
        return null;
    }

    public synchronized void fight(int playerId, short roundId, int action,
                                   byte target, int petAction, byte petTarget) {
        if (status != STATUS.wait_fight)
            return;
        if(roundId!=round)
            return;
        if (side1[0].id == playerId) {
            int realAction = (short) (action & 0xFFFF);
            int itemId = (action >> 16) & 0xFFFF;
            if(checkAction(side1[0],realAction,itemId)){
                side1[0].setTarget(getTarget(playerId, target), 0);
            }
            int realPetAction = (short) (petAction & 0xFFFF);
            if (pet1[0] != null) {
                if(checkPetAction(side1[0],pet1[0],realPetAction)){
                    pet1[0].setTarget(getTarget(playerId, petTarget), 0);
                }
            }
            side1[0].ready = true;
        } else if (side2[0].id == playerId) {
            int realAction = (short) (action & 0xFFFF);
            int itemId = (action >> 16) & 0xFFFF;
            if(checkAction(side2[0],realAction,itemId)){
                side2[0].setTarget(getTarget(playerId, target), 0);
            }
            int realPetAction = (short) (petAction & 0xFFFF);
            if (realAction == Skill.SKILL_NOT_READY) {
                realAction = Skill.SKILL_STAY;
            }
            if (pet2[0] != null) {
                if(checkPetAction(side2[0],pet2[0],realPetAction)){
                    pet2[0].setTarget(getTarget(playerId, petTarget), 0);
                }
            }
            side2[0].ready = true;
        }
        try {
            if (side1[0].ready && side2[0].ready) {
                battleBout(side1, side2, pet1, pet2, round);
                roundEnd();
                clearBourt();
            }
        } catch (Exception ex) {
            caughtException(ex);
        }
    }

    protected UWAPSegment getRoundEndSegment(int id,byte type){
        BattleSprite p1 = side1[0];
        BattleSprite pe1 = pet1[0];
        BattleSprite p2 = side2[0];
        BattleSprite pe2 = pet2[0];
        if(id==side2[0].id){
            p1 = side2[0];
            pe1 = pet2[0];
            p2 = side1[0];
            pe2 = pet1[0];
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
        seg.write((byte) 1);
        seg.writeInt(p1.getAllStatus());
        seg.writeInt(p1.hp);
        seg.writeInt(p1.mp);
        seg.writeBoolean(p1.canAction());
        seg.writeInt(getItemFlag(p1));
        seg.writeString(p1.getSkillName());
        if (pe1 != null) {
            seg.writeInt(pe1.hp);
            seg.writeInt(pe1.mp);
            seg.writeInt(pe1.getAllStatus());
            seg.writeBoolean(pe1.canAction());
            seg.writeString(pe1.getSkillName());
        } else {
            seg.writeInt(0);
            seg.writeInt(0);
            seg.writeInt(0);
            seg.writeBoolean(false);
            seg.writeString("");
        }
        seg.write((byte) - 1);
        seg.writeInt(p2.getAllStatus());
        seg.writeInt(p2.hp);
        seg.writeInt(p2.mp);
        seg.writeBoolean(p1.canAction());
        seg.writeInt(getItemFlag(p1));
        seg.writeString(p2.getSkillName());
        if (pe2 != null) {
            seg.writeInt(pe2.hp);
            seg.writeInt(pe2.mp);
            seg.writeInt(pe2.getAllStatus());
            seg.writeBoolean(pe2.canAction());
            seg.writeString(pe2.getSkillName());
        } else {
            seg.writeInt(0);
            seg.writeInt(0);
            seg.writeInt(0);
            seg.writeBoolean(false);
            seg.writeString("");
        }
        if(type==0)
            seg.write(getRoundStatus(p1, pe1));
        
        syncCDInfo(seg);
        
        return seg;
    }
    
    //Added by leo for sync CD info
    public void syncCDInfo(UWAPSegment seg){
        seg.write((byte)4);
        seg.writeInt(side1[0].id);
        seg.write(side1[0].getCoolDownInfo());
        if(pet1[0] != null){
            seg.writeInt(pet1[0].id);
            seg.write(pet1[0].getCoolDownInfo());
        }else{
            seg.writeInt(-1);
            seg.write(new byte[0]);
        }
        seg.writeInt(side2[0].id);
        seg.write(side2[0].getCoolDownInfo());
        if(pet2[0] != null){
            seg.writeInt(pet2[0].id);
            seg.write(pet2[0].getCoolDownInfo());
        }else{
            seg.writeInt(-1);
            seg.write(new byte[0]);
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
//        seg.writeInt(getId());
//        seg.writeShort((short)round);
//        seg.write((byte) battleMovie.size());
//        for (int i = 0; i < battleMovie.size(); i++) {
//            seg.writeInts((int[]) battleMovie.get(i));
//        }
//        seg.write((byte) 1);
//        seg.writeInt(side1[0].getDebufStatus());
//        seg.writeInt(side1[0].hp);
//        seg.writeInt(side1[0].mp);
//        seg.writeBoolean(side1[0].canAction());
//        seg.writeInt(getItemFlag(side1[0]));
//        if (pet1[0] != null) {
//            seg.writeInt(pet1[0].hp);
//            seg.writeInt(pet1[0].mp);
//            seg.writeInt(pet1[0].getDebufStatus());
//            seg.writeBoolean(pet1[0].canAction());
//        } else {
//            seg.writeInt(0);
//            seg.writeInt(0);
//            seg.writeInt(0);
//            seg.writeBoolean(false);
//        }
//        seg.write((byte) - 1);
//        seg.writeInt(side2[0].getDebufStatus());
//        seg.writeInt(side2[0].hp);
//        seg.writeInt(side2[0].mp);
//        seg.writeBoolean(side2[0].canAction());
//        seg.writeInt(getItemFlag(side2[0]));
//        if (pet2[0] != null) {
//            seg.writeInt(pet2[0].hp);
//            seg.writeInt(pet2[0].mp);
//            seg.writeInt(pet2[0].getDebufStatus());
//            seg.writeBoolean(pet2[0].canAction());
//        } else {
//            seg.writeInt(0);
//            seg.writeInt(0);
//            seg.writeInt(0);
//            seg.writeBoolean(false);
//        }
//        seg.write(getRoundStatus(side1[0], pet1[0]));
        service.getConnectService().writeTo(seg, side1[0].id);
        seg = getRoundEndSegment(side2[0].id,(byte)0);
//        seg.writeInt(getId());
//        seg.writeShort((short)round);
//        seg.write((byte) battleMovie.size());
//        makeMovie(battleMovie);
//        for (int i = 0; i < battleMovie.size(); i++) {
//            seg.writeInts((int[]) battleMovie.get(i));
//        }
//        seg.write((byte) 1);
//        seg.writeInt(side2[0].getDebufStatus());
//        seg.writeInt(side2[0].hp);
//        seg.writeInt(side2[0].mp);
//        seg.writeBoolean(side2[0].canAction());
//        seg.writeInt(getItemFlag(side2[0]));
//        if (pet2[0] != null) {
//            seg.writeInt(pet2[0].hp);
//            seg.writeInt(pet2[0].mp);
//            seg.writeInt(pet2[0].getDebufStatus());
//            seg.writeBoolean(pet2[0].canAction());
//        } else {
//            seg.writeInt(0);
//            seg.writeInt(0);
//            seg.writeInt(0);
//            seg.writeBoolean(false);
//        }
//        seg.write((byte) - 1);
//        seg.writeInt(side1[0].getDebufStatus());
//        seg.writeInt(side1[0].hp);
//        seg.writeInt(side1[0].mp);
//        seg.writeBoolean(side1[0].canAction());
//        seg.writeInt(getItemFlag(side1[0]));
//        if (pet1[0] != null) {
//            seg.writeInt(pet1[0].hp);
//            seg.writeInt(pet1[0].mp);
//            seg.writeInt(pet1[0].getDebufStatus());
//            seg.writeBoolean(pet1[0].canAction());
//        } else {
//            seg.writeInt(0);
//            seg.writeInt(0);
//            seg.writeInt(0);
//            seg.writeBoolean(false);
//        }
//        seg.write(getRoundStatus(side2[0], pet2[0]));
        service.getConnectService().writeTo(seg, side2[0].id);
        battleMovie.clear();
        if (battleOver) {
            if (side1[0].getDebufStatus() != Skill.STATUS_DIE) {
                winner = side1[0].player;
                failure = side2[0].player;
            } else {
                winner = side2[0].player;
                failure = side1[0].player;
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
            side1[0].ready = false;
            side2[0].ready = false;
        }
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

                swap(m, 1, 3);
            }
        }
    }

    private void swap(int[] m, int index1, int index2) {
        int temp = m[index1];
        m[index1] = m[index2];
        m[index2] = temp;
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
                result = side1[0];
                break;
            case BattleSprite.TYPE_PLAYER_PET:
                result = pet1[0];
                break;
            case BattleSprite.TYPE_MONSTER:
                result = side2[0];
                break;
            case BattleSprite.TYPE_MONSTER_PET:
                result = pet2[0];
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
        service.getConnectService().writeTo(seg, side1[0].id);
        service.getConnectService().writeTo(seg, side2[0].id);
    }

    protected UWAPSegment getPkStartSegment(int serial) {
        UWAPSegment seg = new UWAPSegment(ClientConstants.PK_START,
                                          serial);
        seg.writeInt(id);
        seg.write((byte) 1);
        seg.writeInt(side1[0].id);
        seg.write((byte)1);
        seg.writeInt(side1[0].id);
        seg.writeString(side1[0].name);
        seg.writeInt(side1[0].hp);
        seg.writeInt(side1[0].mp);
        seg.writeInt(side1[0].attributes[BattleSprite.ATTR_HPMAX]);
        seg.writeInt(side1[0].attributes[BattleSprite.ATTR_MPMAX]);
        seg.write(side1[0].face);
        IEquipment weapon = side1[0].player.getWeapon();
        if (weapon == null) {
            seg.write((byte) - 1);
        } else {
            seg.write((byte) weapon.getProperty(30, side1[0].level)); //武器类型
        }
        seg.writeInt(side1[0].getAllStatus());
        seg.writeBoolean(side1[0].canAction());
        seg.write(side1[0].player.getLightLevel());
        if (pet1[0] != null) {
            seg.write((byte) pet1[0].pet.getPetType());
            seg.writeInt(pet1[0].hp);
            seg.writeInt(pet1[0].mp);
            seg.writeInt(pet1[0].pet.getMaxHp());
            seg.writeInt(pet1[0].pet.getMaxMp());
            seg.writeString(pet1[0].pet.getName());
            seg.writeInt(pet1[0].getAllStatus());
            seg.writeBoolean(pet1[0].canAction());
            //2代形象
            seg.write(pet1[0].pet.getBindType());
            seg.writeInt(pet1[0].level);
            seg.writeShort(pet1[0].pet.getColorIndex());
            //3代形象
            seg.writeInt(pet1[0].pet.getEvolutionLevel());
            seg.writeInt(pet1[0].pet.getEvolutionType());
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
        seg.write((byte)1);
        seg.writeInt(side2[0].id);
        seg.writeString(side2[0].name);
        seg.writeInt(side2[0].hp);
        seg.writeInt(side2[0].mp);
        seg.writeInt(side2[0].attributes[BattleSprite.ATTR_HPMAX]);
        seg.writeInt(side2[0].attributes[BattleSprite.ATTR_MPMAX]);
        seg.write(side2[0].face);
        weapon = side2[0].player.getWeapon();
        if (weapon == null) {
            seg.write((byte) - 1);
        } else {
            seg.write((byte) weapon.getProperty(30, side2[0].level));
        }
        seg.writeInt(side2[0].getAllStatus());
        seg.writeBoolean(side2[0].canAction());
        seg.write(side2[0].player.getLightLevel());
        if (pet2[0] != null) {
            seg.write((byte) pet2[0].pet.getPetType());
            seg.writeInt(pet2[0].hp);
            seg.writeInt(pet2[0].mp);
            seg.writeInt(pet2[0].pet.getMaxHp());
            seg.writeInt(pet2[0].pet.getMaxMp());
            seg.writeString(pet2[0].pet.getName());
            seg.writeInt(pet2[0].getAllStatus());
            seg.writeBoolean(pet2[0].canAction());
            //2代形象
            seg.write(pet2[0].pet.getBindType());
            seg.writeInt(pet2[0].level);
            seg.writeShort(pet2[0].pet.getColorIndex());
            //3代形象
            seg.writeInt(pet2[0].pet.getEvolutionLevel());
            seg.writeInt(pet2[0].pet.getEvolutionType());
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
