package com.pip.itimes.server.world.battle;

import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.MonsterGroup;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.world.game.IServerObject;
import com.pip.itimes.server.world.game.MonsterObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Jeffrey
 * @version 1.0
 */
public abstract class BaseMonsterAI implements IMonsterAI{

    private static final Random rnd1 = new Random();
    
    public static PlayerService playerService = null;
    
    private List<AIMonsterSkill> skills = new ArrayList<AIMonsterSkill>();
    private int totalSkillRate = 0;
    
    public int getSpecialHp(){
        return -1;
    }
    
    public int getSpecialMp(){
        return -1;
    }
    
    public boolean killAllPlayer(BattleSprite bs, BattleSprite[] them, BattleSprite[] themPet){
        Skill skill = (Skill) Skill.getSkill(112).clone();
        antiDefence(bs);
        defenceAllDebuff_1_round(bs);
        bs.AddAttrBuf(1, 0, 0, 0, 0, 100, 0, 1000000, 0, 0, 1000000, 1000000, 100003);
        return useSkillToHighestUnit(bs, skill, them, themPet);
    }
    
    public boolean killOneSprite(BattleSprite bs, BattleSprite target){
        Skill skill = (Skill) Skill.ATTACK_SKILL.clone();
        antiDefence(bs);
        defenceAllDebuff_1_round(bs);
        bs.AddAttrBuf(1, 0, 0, 0, 0, 100, 0, 1000000, 0, 0, 1000000, 1000000, 100003);
        bs.setTarget(target, target.groupIndex);
        return useSkillForTeam(bs, target, skill);
    }
    
    public boolean monsterDie(BattleSprite playerSprite, int monsterId){
        if(playerSprite.player != null && playerSprite.player.getMap() != null && playerSprite.player.getMap().getMonsterGroup(monsterId) != null){
            if(playerSprite.player.getMap().getMonsterGroup(monsterId).getStatus() == IServerObject.STATUS_INVISIBLE){
                return true;
            }
        }

        return false;
    }
    
    public void shout(String message, BattleSprite bs, BattleSprite[] them){
        for(int i = 0; i < them.length; i++){
        	if(them[i] != null){
        		Server.instance.chatService.sendPrivateRoarMessage(-10, "狮子吼", message, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short) 0, them[i].id, bs.monster.getName());
        	}
        }
    }
    
    public void clearSkill(){
        skills.clear();
        totalSkillRate = 0;
    }
    
    public int getHealHp(BattleSprite[] our){
        int maxDiffHp = 0;
        
        for(int i = 0; i < our.length; i++){
            //int diffHp = our[i].attributes[BattleSprite.ATTR_HPMAX] - our[i].hp;
            int diffHp = our[i].attributes[BattleSprite.ATTR_HPMAX] / 100 * 10;
            if(maxDiffHp < diffHp){
                maxDiffHp = diffHp;
            }
        }
        
        return maxDiffHp;
    }
    
    public void addSkill(int skillId, int rate, boolean noMpUse){
        addSkill((Skill)Skill.getSkill(skillId).clone(), rate, noMpUse);
    }
    
    public void addSkill(Skill skill, int rate, boolean noMpUse){
        AIMonsterSkill as = new AIMonsterSkill();
        as.skill = skill;
        
        if(noMpUse){
            as.skill.mpUse = 0;
        }
        
        as.rate = rate;
        skills.add(as);
        totalSkillRate += rate;
    }
    
    public Skill pickSkill(){
        int pickRate = rnd1.nextInt(totalSkillRate + 1);
        Skill result = null;
        
        for(AIMonsterSkill as : skills){
            if(pickRate < as.rate){
                result = as.skill;
                
                break;
            }else{
                pickRate -= as.rate;
            }
        }
        
        if(result == null && skills.size() > 0){
            result = skills.get(skills.size() - 1).skill;
        }
        
        return result;
    }
    
    public BattleSprite selectHighestEnmity(BattleSprite src,BattleSprite[] them,BattleSprite[] themPet){
        int max = 0;
        BattleSprite ret = null;
        int tmp = 0;
        for(int i = 0; i < them.length; i++){
            if(them[i]!=null&&!them[i].testCannotBattle()){
                int e = src.getEnmity(them[i]);
                if(e > max){
                    tmp = 1;
                    max = e;
                    ret = them[i];
                }else if(e == max&&Utils.hit(rnd1,100 / (tmp + 1),100)){
                    tmp++;
                    ret = them[i];
                }
            }
        }
        for(int i=0;i<themPet.length;i++){
            if(themPet[i]!=null&&!themPet[i].testCannotBattle()){
                int e = src.getEnmity(themPet[i]);
                if(e>=max){
                    max = e;
                    ret = themPet[i];
                }
                else if(e == max&&Utils.hit(rnd1,50,100)){
                    ret = themPet[i];
                }
            }
        }
        return ret;
    }
    
    public BattleSprite selectHighestLevel(BattleSprite src,BattleSprite[] them){
        int max = 0;
        BattleSprite ret = null;
        int tmp = 0;
        for(int i = 0; i < them.length; i++){
            if(them[i]!=null&&!them[i].testCannotBattle()){
                int e = them[i].player.getLevel();
                if(e > max){
                    tmp = 1;
                    max = e;
                    ret = them[i];
                }else if(e == max&&Utils.hit(rnd1,100 / (tmp + 1),100)){
                    tmp++;
                    ret = them[i];
                }
            }
        }
        return ret;
    }

    public BattleSprite selectHightestHpEnmity(BattleSprite[] them,
                                               BattleSprite[] themPet) {
        BattleSprite ret = null;
        for (int i = 0; i < them.length; i++) {
            if (ret == null && them[i].getDebufStatus() != Skill.STATUS_DIE)
                ret = them[i];
            else {
                if (ret.hp < them[i].hp)
                    ret = them[i];
            }
        }
        if (ret == null) {
            for (int i = 0; i < themPet.length; i++) {
                if (ret == null &&
                    themPet[i].getDebufStatus() != Skill.STATUS_DIE)
                    ret = them[i];
                else {
                    if (ret.hp < themPet[i].hp)
                        ret = themPet[i];
                }
            }
        }
        return null;
    }

    public void defaultCannotActionAction(BattleSprite bs){
        bs.setSkill(Skill.STAY_SKILL);
        bs.setTarget(null, -1);
    }

    public int getPercentValue(int value,int percent){
        return percent*value/100;
    }

    public void useItemForSelf(BattleSprite bs,IItem item){
        bs.setSkill(Skill.ITEM_SKILL);
        bs.setTarget(bs,bs.groupIndex);
        bs.usedItem = item;
    }

    //本回合的任何攻击附带崩溃一击效果
    public void antiDefence(BattleSprite bs){
        bs.AddAttrBuf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, SkillConstants.EFFECT_ANTI_BUF_INC_ATK);
    }
    
    public boolean useAbilityToHighestUnit(BattleSprite bs,Ability ability,BattleSprite[] them,BattleSprite[] themPet){
        bs.setSkill(Skill.getSkill(ability.getId()));
        BattleSprite target = selectHighestEnmity(bs,them,themPet);
        if(target==null)
            return true;
        bs.setTarget(target,target.groupIndex);
        return false;
    }
    
    public boolean useSkillToHighestUnit(BattleSprite bs,Skill skill,BattleSprite[] them,BattleSprite[] themPet){
        bs.setSkill(skill);
        BattleSprite target = selectHighestEnmity(bs,them,themPet);
        if(target==null)
            return true;
        bs.setTarget(target,target.groupIndex);
        return false;
    }

    public boolean useAbilityToHighestHpUnit(BattleSprite bs,Ability ability,BattleSprite[] them,BattleSprite[] themPet){
        bs.setSkill(Skill.getSkill(ability.getId()));
        BattleSprite target = selectHightestHpEnmity(them,themPet);
        if(target==null)
            return true;
        bs.setTarget(target,target.groupIndex);
        return false;
    }
    
    public boolean useAbilityToHighestLevelUnit(BattleSprite bs,Ability ability,BattleSprite[] them){
        bs.setSkill(Skill.getSkill(ability.getId()));
        BattleSprite target = selectHighestLevel(bs, them);
        if(target==null)
            return true;
        bs.setTarget(target,target.groupIndex);
        return false;
    }

    public boolean useAbilityForSelf(BattleSprite bs,Ability ability){
        bs.setSkill(Skill.getSkill(ability.getId()));
        bs.setTarget(bs,bs.groupIndex);
        return false;
    }

    public boolean useSkillForSelf(BattleSprite bs,Skill skill){
        bs.setSkill(skill);
        bs.setTarget(bs,bs.groupIndex);
        return false;
    }

    public boolean useAbilityForTeam(BattleSprite bs,BattleSprite team,Ability ability){
        bs.setSkill(Skill.getSkill(ability.getId()));
        bs.setTarget(team,team.groupIndex);
        return false;
    }

    public boolean useDefaultAttackToHighestUnit(BattleSprite bs,BattleSprite[] them,BattleSprite[] themPet){
        bs.setSkill(Skill.ATTACK_SKILL);
        BattleSprite target = selectHighestEnmity(bs,them,themPet);
        if(target==null)
            return true;
        bs.setTarget(target,target.groupIndex);
        return false;
    }
    
    public boolean useDefaultAttackToHighestLevel(BattleSprite bs, BattleSprite[] them){
        bs.setSkill(Skill.ATTACK_SKILL);
        BattleSprite target = selectHighestLevel(bs,them);
        if(target==null)
            return true;
        bs.setTarget(target,target.groupIndex);
        return false;
    }

    public BattleSprite selectHighestHpTeam(BattleSprite[] them){
        BattleSprite result = null;
        int maxHp = 0;
        
        for(int i=0;i<them.length;i++){
            if(them[i] != null && them[i].hp > maxHp){
                maxHp = them[i].hp;
                result = them[i];
            }
        }
        
        return result;
    }
    
    public boolean hasTeamHpLeastAt(BattleSprite bs,BattleSprite[] them,BattleSprite[] themPet,int percent){
        for(int i=0;i<them.length;i++){
            if(them[i]!=null&&them[i]!=bs){
                if(them[i].hp<=(them[i].attributes[BattleSprite.ATTR_HPMAX]*percent/100))
                    return true;
            }
        }
        return false;
    }

    public int getMaxLostHp(BattleSprite[] our){
        int max = 0;
        for(int i=0;i<our.length;i++){
            int losted = our[i].attributes[BattleSprite.ATTR_HPMAX] - our[i].hp;
            if(losted>max)
                max = losted;
        }
        return max;
    }

    public BattleSprite getTeamHpLeastAt(BattleSprite bs,BattleSprite[] them,BattleSprite themPet,int percent){
        for(int i=0;i<them.length;i++){
            if(them[i]!=null&&them[i]!=bs&&them[i].getDebufStatus()!=Skill.STATUS_DIE)
                if(them[i].hp<=(them[i].attributes[BattleSprite.ATTR_HPMAX]*percent/100))
                    return them[i];
        }
        return null;
    }

    public BattleSprite selectLowestHpTeam(BattleSprite bs,BattleSprite[] them,BattleSprite[] themPet){
        BattleSprite ret = null;
        for(int i=0;i<them.length;i++){
            if(them[i]!=null&&them[i]!=bs&&them[i].getDebufStatus()!=Skill.STATUS_DIE){
                if(ret==null)
                    ret = them[i];
                else if(ret.hp>them[i].hp)
                    ret= them[i];
            }
        }
        return ret;
    }

    public boolean allTeamHpLeastAt(BattleSprite bs,BattleSprite[] them,BattleSprite[] themPet,int percent){
        for(int i=0;i<them.length;i++){
            if(them[i]!=null&&them[i]!=bs){
                if(them[i].hp>(them[i].attributes[BattleSprite.ATTR_HPMAX]*percent/100))
                    return false;
            }
        }
        return true;
    }

    /*免疫所有的状态【昏睡，混乱】*/
    public void defenceAllDebuff(BattleSprite bs){
        bs.setBufStatus(1000, Skill.STATUS_IMMUNITY_STATUS, 1, 0, 0, bs.bsType, bs.groupIndex);
    }
    
    /*免疫所有状态1回合*/
    public void defenceAllDebuff_1_round(BattleSprite bs){
        bs.setBufStatus(1, Skill.STATUS_IMMUNITY_STATUS, 1, 0, 0, bs.bsType, bs.groupIndex);
    }

    public boolean isTeamAllDie(BattleSprite bs,BattleSprite[] them){
        for(int i=0;i<them.length;i++){
            if(them[i]!=null&&them[i]!=bs&&them[i].getDebufStatus()!=Skill.STATUS_DIE)
                return false;
        }
        return true;
    }


    public BattleSprite getDieTeam(BattleSprite bs,BattleSprite[] them){
        for(int i=0;i<them.length;i++){
            if(them[i]!=null&&them[i]!=bs&&them[i].getDebufStatus()==Skill.STATUS_DIE)
                return them[i];
        }
        return null;
    }

    public boolean hasEquipment(BattleSprite[] them,int id){
        for(int i=0;i<them.length;i++){
            if(them[i]!=null){
                if(them[i].player.hasEquipmented(id))
                    return true;
            }
        }
        return false;
    }

    public boolean hasItem(BattleSprite[] them,int id){
        for(int i=0;i<them.length;i++){
            if(them[i]!=null){
                if(them[i].player.hasItem(id))
                    return true;
            }
        }
        return false;
    }
//    public int getSelectedSkillIndex(BattleSprite src,int skill){
//        for(int i=0;i<src.skillList.length;i++){
//            if(src.skillList[i]==skill)
//                return i;
//        }
//
//    }
    //mengjie add
    public boolean useDefaultAttackToHighestUnit_nopet(BattleSprite bs,BattleSprite[] them,BattleSprite[] themPet){
        bs.setSkill(Skill.ATTACK_SKILL);
        BattleSprite target = selectHighestEnmity_nopet(bs,them);
        if(target==null)
            return true;
        bs.setTarget(target,target.groupIndex);
        return false;
    }
    public boolean useAbilityToHighestUnit_nopet(BattleSprite bs,Ability ability,BattleSprite[] them,BattleSprite[] themPet){
        bs.setSkill(Skill.getSkill(ability.getId()));
        BattleSprite target = selectHighestEnmity_nopet(bs,them);
        if(target==null)
            return true;
        bs.setTarget(target,target.groupIndex);
        return false;
    }
    public BattleSprite selectHighestEnmity_nopet(BattleSprite src,BattleSprite[] them){
        int max = 0;
        BattleSprite ret = null;
        int tmp = 0;
        for(int i = 0; i < them.length; i++){
            if(them[i]!=null&&!them[i].testCannotBattle()){
                int e = src.getEnmity(them[i]);
                if(e > max){
                    tmp = 1;
                    max = e;
                    ret = them[i];
                }else if(e == max&&Utils.hit(rnd1,100 / (tmp + 1),100)){
                    tmp++;
                    ret = them[i];
                }
            }
        }
        return ret;
    }
    /**
     * 对两个玩家使用中毒的buf
     * @param bs
     * @param ability
     * @param them
     * @param themPet
     * @return
     */
    public boolean useAbilityToHighestUnitTwo(BattleSprite bs,Ability ability,BattleSprite[] them,BattleSprite[] themPet){
        bs.setSkill(Skill.getSkill(ability.getId()));
        BattleSprite target = selectHighestEnmity(bs,them,themPet);
        if(target==null)
            return true;
        for(int i = 0; i < them.length; i ++){
        	if(them[i] != target){
        		them[i].setDeBufStatus(4, Skill.STATUS_POISON, 0, 0, 0, 0, 0);
        		break;
        	}
        }
        bs.setTarget(target,target.groupIndex);
        return false;
    }
    /**
     * 给队友使用某一个技能
     * @param bs
     * @param team
     * @param skill
     * @return
     */
    public boolean useSkillForTeam(BattleSprite bs,BattleSprite team,Skill skill){
        bs.setSkill(skill);
        bs.setTarget(team,team.groupIndex);
        return false;
    }
    /**
     * 寒冰副本的boss
     */
    public int getMonsterLife(BattleSprite them,int i){
    	int ret = i;
    	
    	if(them != null && them.player != null){
    		GameMap map = them.player.getMap();
    		if(map!=null){
	    		MonsterObject mo = map.getMonsterGroup(0xd442004);
	    		if(mo != null && mo.getStatus() == IServerObject.STATUS_INVISIBLE){
	    			//一王已经死了
	    			ret--;
	    		}
	    		mo = map.getMonsterGroup(0xd44200b);
	    		if(mo != null && mo.getStatus() == IServerObject.STATUS_INVISIBLE && ret > 1){
	    			//二王已经死了
	    			ret--;
	    		}
	    		mo = map.getMonsterGroup(0xd442013);
	    		if(mo != null && mo.getStatus() == IServerObject.STATUS_INVISIBLE && ret > 2){
	    			//三王已经死了
	    			ret--;
	    		}
	    		mo = map.getMonsterGroup(0xd442005);
	    		if(mo != null && mo.getStatus() == IServerObject.STATUS_INVISIBLE && ret > 3){
	    			//四王已经死了
	    			ret--;
	    		}
    		}
    	}
    	return ret;
    }
}

class AIMonsterSkill{
    public Skill skill;
    public int rate;
}
