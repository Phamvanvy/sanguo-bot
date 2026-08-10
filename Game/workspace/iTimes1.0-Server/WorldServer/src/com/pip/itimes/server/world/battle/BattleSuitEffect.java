package com.pip.itimes.server.world.battle;

import com.pip.itimes.server.suit.SuitEffect;

public class BattleSuitEffect{
    private SuitEffect effect;
    
    private boolean effecting = false;
    private int effectedTimes = 0;
    private int restBout = 0;
    
    public BattleSuitEffect(SuitEffect effect){
        this.effect = effect;
    }

    public int getType(){
        return effect.getType();
    }
    
    public int getValue(){
        return effect.getValue();
    }

    public int getWay(){
        return effect.getWay();
    }

    public int getBout(){
        return effect.getBout();
    }

    public int getPercent(){
        return effect.getPercent();
    }

    public int getSkillParm1(){
        return effect.getSkillParm1();
    }

    public int getSkillParm2(){
        return effect.getSkillParm2();
    }

    public int getSkillPercent(){
        return effect.getSkillPercent();
    }
    
    public int getSkillBout(){
        return effect.getSkillBout();
    }

    public int getSkillMpUse(){
        return effect.getSkillMpUse();
    }

    public boolean hasEffectSkill(int skillId){
        return effect.hasEffectSkill(skillId);
    }
    
    public void clearEffect(){
        effecting = false;
        effectedTimes = 0;
        restBout = 0;
    }
    
    public void doEffect(){
        if(!effecting){
            effecting = true;
            restBout = effect.getBout();
        }
    }
    
    public void passBout(){
        if(effecting){
            if(restBout > 0){
                restBout--;
            }
            
            if(restBout == 0){
                effecting = false;
                effectedTimes++;
            }
        }
    }
    
    private boolean isSelf(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet){
        BattleSprite target = self.target;
        
        if(target == null){
            return true;
        }
        
        if(our != null){
            for(int i = 0; i < our.length; i++){
                if(our[i] == null){
                    continue;
                }
                
                if(target == our[i]){
                    return true;
                }
            }
        }
        
        if(ourPet != null){
            for(int i = 0; i < ourPet.length; i++){
                if(ourPet[i] == null){
                    continue;
                }
                
                if(target == ourPet[i]){
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean isEnemy(BattleSprite self, BattleSprite[] them, BattleSprite[] themPet){
        BattleSprite target = self.target;
        
        if(target == null){
            return true;
        }
        
        if(them != null){
            for(int i = 0; i < them.length; i++){
                if(them[i] == null){
                    continue;
                }
                
                if(target == them[i]){
                    return true;
                }
            }
        }
        
        if(themPet != null){
            for(int i = 0; i < themPet.length; i++){
                if(themPet[i] == null){
                    continue;
                }
                
                if(target == themPet[i]){
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean canEffect(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        //星辉套装效果，战斗中无效
    	if (effect == null){
    		return false;
    	}
    	//end
    	if(effecting){
            return true;
        }
        
        if((getWay() == SuitEffect.EFFECT_WAY_PET_ONE || getWay() == SuitEffect.EFFECT_WAY_PET_ALL) && !(self.bsType == BattleSprite.TYPE_INTERVENE || self.bsType == BattleSprite.TYPE_PLAYER_PET  || self.bsType == BattleSprite.TYPE_MONSTER_PET) ){
            return false;
        }
        
        if(!Skill.getPercentRate(getPercent())){
            return false;
        }
        
        if(getWay() == SuitEffect.EFFECT_WAY_SELF_ALL || getWay() == SuitEffect.EFFECT_WAY_PET_ALL || getWay() == SuitEffect.EFFECT_WAY_ENEMY_ALL){
            switch(getWay()){
                case SuitEffect.EFFECT_WAY_SELF_ALL:
                    if(isSelf(self, our, ourPet) || getType() == SuitEffect.EFFECT_TYPE_CHANGE_SKILL){
                        return true;
                    }
                    
                    break;
                case SuitEffect.EFFECT_WAY_ENEMY_ALL:
                    if(isEnemy(self, them, themPet)){
                        return true;
                    }
                    
                    break;
                case SuitEffect.EFFECT_WAY_PET_ALL:
                    
                    return true;
            }
        }
        
        if(effectedTimes == 0 && getWay() == SuitEffect.EFFECT_WAY_SELF_ONE){
            if(isSelf(self, our, ourPet) || getType() == SuitEffect.EFFECT_TYPE_CHANGE_SKILL){
                return true;
            }
        }
        
        if(effectedTimes == 0 && getWay() == SuitEffect.EFFECT_WAY_PET_ONE){
            return true;
        }
        
        if(effectedTimes == 0 && getWay() == SuitEffect.EFFECT_WAY_ENEMY_ONE){
            if(isEnemy(self, them, themPet)){
                return true;
            }
        }
        
        return false;
    }
    
    public boolean effectCatch(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        if(!canEffect(self, our, ourPet, them, themPet)){
            return false;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_ADD_PET_CATCH){
            return true;
        }
        
        return false;
    }
    
    public boolean effectSkill(int skillId, BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        if(!canEffect(self, our, ourPet, them, themPet)){
            return false;
        }
        
        if(hasEffectSkill(skillId)){
            return true;
        }
        
        return false;
    }

    public boolean effectStatus(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        boolean result = false;
        
        if(!canEffect(self, our, ourPet, them, themPet) || effecting){
            return result;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_LET_CONFUSE){
            result = true;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_LET_FAINT){
            result = true;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_LET_FROST){
            result = true;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_LET_POSION){
            result = true;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_LET_SLEEP){
            result = true;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_LET_STONE){
            result = true;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_LET_STOP){
            result = true;
        }
        
        return result;
    }
    
    public boolean effectAttribute(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        if(!canEffect(self, our, ourPet, them, themPet)){
            return false;
        }
        switch (getType()){
        	case SuitEffect.EFFECT_TYPE_ADD_PHY_ACT:
        		return true;
        	case SuitEffect.EFFECT_TYPE_ADD_MGC_ACT:
	    		return true;
			case SuitEffect.EFFECT_TYPE_ADD_PHYMGC_ACT:
				return true;
			case SuitEffect.EFFECT_TYPE_ADD_PHY_DEF:
		    	return true;
			case SuitEffect.EFFECT_TYPE_ADD_MGC_DEF:
		   		return true;
			case SuitEffect.EFFECT_TYPE_ADD_PHYMGC_DEF:
		   		return true;
			case SuitEffect.EFFECT_TYPE_REDUCE_PRI_CRI:
		   		return true;
			case SuitEffect.EFFECT_TYPE_REDUCE_MRI_CRI:
		    	return true;
			case SuitEffect.EFFECT_TYPE_REDUCE_PRI_MRI_CRI:
			    return true;
			case SuitEffect.EFFECT_TYPE_ADD_PRI_CRI:
		   		return true;
			case SuitEffect.EFFECT_TYPE_ADD_MRI_CRI:
		    	return true;
			case SuitEffect.EFFECT_TYPE_ADD_PRI_MRI_CRI:
			    return true;
			case SuitEffect.EFFECT_TYPE_REDUCE_PRI_CRI_DAMAGE:
				return true;
			case SuitEffect.EFFECT_TYPE_REDUCE_MRI_CRI_DAMAGE:
				return true;
			case SuitEffect.EFFECT_TYPE_REDUCE_PRI_MRI_CRI_DAMAGE:
				return true;
			case SuitEffect.EFFECT_TYPE_ADD_PRI_CRI_DAMAGE:
				return true;
			case SuitEffect.EFFECT_TYPE_ADD_MRI_CRI_DAMAGE:
				return true;
			case SuitEffect.EFFECT_TYPE_ADD_PRI_MRI_CRI_DAMAGE:
				return true;
			default:
				return false;
        }
       /* if(getType() == SuitEffect.EFFECT_TYPE_ADD_PHY_ACT){
            return true;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_ADD_MGC_ACT){
            return true;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_ADD_PHYMGC_ACT){
            return true;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_ADD_PHY_DEF){
            return true;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_ADD_MGC_DEF){
            return true;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_ADD_PHYMGC_DEF){
            return true;
        }
        
        return false;*/
    }
    
    public int getPhysicalAttackAdd(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        int result = 0;
        
        if(!effectAttribute(self, our, ourPet, them, themPet)){
            return result;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_ADD_PHY_ACT || getType() == SuitEffect.EFFECT_TYPE_ADD_PHYMGC_ACT){
            result = getValue();
            doEffect();
        }
        
        return result;
    }
    
    public int getMagicAttackAdd(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        int result = 0;
        
        if(!effectAttribute(self, our, ourPet, them, themPet)){
            return result;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_ADD_MGC_ACT || getType() == SuitEffect.EFFECT_TYPE_ADD_PHYMGC_ACT){
            result = getValue();
            doEffect();
        }
        
        return result;
    }
    
    public int getPhysicalDefenceAdd(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        int result = 0;
        
        if(!effectAttribute(self, our, ourPet, them, themPet)){
            return result;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_ADD_PHY_DEF || getType() == SuitEffect.EFFECT_TYPE_ADD_PHYMGC_DEF){
            result = getValue();
            doEffect();
        }
        return result;
    }
    
    public int getMagicDefenceAdd(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        int result = 0;
        
        if(!effectAttribute(self, our, ourPet, them, themPet)){
            return result;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_ADD_MGC_DEF || getType() == SuitEffect.EFFECT_TYPE_ADD_PHYMGC_DEF){
            result = getValue();
            doEffect();
        }
        
        return result;
    }
    /**
     * @param self
     * @param our
     * @param ourPet
     * @param them
     * @param themPet
     * @return魔法暴击减少率
     */
    public int getMagicCriReduceRate(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int result = 0;
        
        if(!effectAttribute(self, our, ourPet, them, themPet)){
            return result;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_REDUCE_MRI_CRI  || getType() == SuitEffect.EFFECT_TYPE_REDUCE_PRI_MRI_CRI ){
            result = getValue();
            doEffect();
        }
        
        return result;
    }
    /**
     * @param self
     * @param our
     * @param ourPet
     * @param them
     * @param themPet
     * @return 物理暴击减少率
     */
    public int getPriCriReduceRate(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int result = 0;
        
        if(!effectAttribute(self, our, ourPet, them, themPet)){
            return result;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_REDUCE_PRI_CRI   || getType() == SuitEffect.EFFECT_TYPE_REDUCE_PRI_MRI_CRI ){
            result = getValue();
            doEffect();
        }
        
        return result;
    }
    /**
     * @param self
     * @param our
     * @param ourPet
     * @param them
     * @param themPet
     * @return魔爆减低伤害几率
     */
    public int getMagicCriReduceAttackRate (BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet) {
    	int result = 0;
        
        if (!effectAttribute(self, our, ourPet, them, themPet)) {
            return result;
        }
        
        if (getType() == SuitEffect.EFFECT_TYPE_REDUCE_MRI_CRI_DAMAGE || getType() == SuitEffect.EFFECT_TYPE_REDUCE_PRI_MRI_CRI_DAMAGE ) {
            //result = getValue()/10000;
        	result = getValue();
            doEffect();
        }
        
        return result;
    }
    /**
     * @param self
     * @param our
     * @param ourPet
     * @param them
     * @param themPet
     * @return物爆减低伤害几率
     */
    public int getPriCriReduceAttackRate (BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet) {
    	int result = 0;
        
        if (!effectAttribute(self, our, ourPet, them, themPet)) {
            return result;
        }
        
        if (getType() == SuitEffect.EFFECT_TYPE_REDUCE_PRI_CRI_DAMAGE || getType() == SuitEffect.EFFECT_TYPE_REDUCE_PRI_MRI_CRI_DAMAGE) {
            //result = getValue()/10000;
        	result = getValue();
            doEffect();
        }
        
        return result;
    }
    //mengjie add
    /**
     * @param self
     * @param our
     * @param ourPet
     * @param them
     * @param themPet
     * @return魔法暴击增加率
     */
    public int getMagicCriAddRate(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int result = 0;
        
        if(!effectAttribute(self, our, ourPet, them, themPet)){
            return result;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_ADD_MRI_CRI  || getType() == SuitEffect.EFFECT_TYPE_ADD_PRI_MRI_CRI ){
            result = getValue();
            doEffect();
        }
        
        return result;
    }
    /**
     * @param self
     * @param our
     * @param ourPet
     * @param them
     * @param themPet
     * @return 物理暴击增加率
     */
    public int getPriCriAddRate(BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int result = 0;
        
        if(!effectAttribute(self, our, ourPet, them, themPet)){
            return result;
        }
        
        if(getType() == SuitEffect.EFFECT_TYPE_ADD_PRI_CRI   || getType() == SuitEffect.EFFECT_TYPE_ADD_PRI_MRI_CRI ){
            result = getValue();
            doEffect();
        }
        
        return result;
    }
    /**
     * @param self
     * @param our
     * @param ourPet
     * @param them
     * @param themPet
     * @return魔爆增加伤害几率
     */
    public int getMagicCriAddAttackRate (BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet) {
    	int result = 0;
        
        if (!effectAttribute(self, our, ourPet, them, themPet)) {
            return result;
        }
        
        if (getType() == SuitEffect.EFFECT_TYPE_ADD_MRI_CRI_DAMAGE || getType() == SuitEffect.EFFECT_TYPE_ADD_PRI_MRI_CRI_DAMAGE) {
            //result = getValue()/10000;
        	result = getValue();
            doEffect();
        }
        
        return result;
    }
    
    /**
     * @param self
     * @param our
     * @param ourPet
     * @param them
     * @param themPet
     * @return物爆增加伤害几率
     */
    public int getPriCriAddAttackRate (BattleSprite self, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet) {
    	int result = 0;
        
        if (!effectAttribute(self, our, ourPet, them, themPet)) {
            return result;
        }
        
        if (getType() == SuitEffect.EFFECT_TYPE_ADD_PRI_CRI_DAMAGE || getType() == SuitEffect.EFFECT_TYPE_ADD_PRI_MRI_CRI_DAMAGE ) {
            //result = getValue()/10000;
        	result = getValue();
            doEffect();
        }
        
        return result;
    }
    
    public int getAntiShineHitAdd(BattleSprite self){
        int result = 0;
//        
//        if(!effectAttribute(self, our, ourPet, them, themPet)){
//            return result;
//        }
//        
        if (effect == null){
        	return result;
        }
        if(getType() == SuitEffect.EFFECT_TYPE_SHINE){
        	if (self.lastmissflag > 0){
        		result = getValue();
                doEffect();
        	}
        }
        return result;
    }
    public int getAntiShineHitAdd(BattleSprite self,int type){
        int result = 0;
        if(getType() == SuitEffect.EFFECT_TYPE_SHINE){
        	if ((self.lastmissflagpet > 0)){
        		result = getValue();
                doEffect();
        	}
        }
        return result;
    }
}
