package com.pip.itimes.server.world.battle.ai;

import com.pip.itimes.server.bean.Master;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.Skill;
import com.pip.itimes.server.world.battle.SkillConstants;
import com.pip.itimes.server.world.game.IServerObject;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.util.Utils;

import java.util.Random;
import java.util.Vector;

public class Ai90016_1 extends BaseMonsterAI{
	//yuzexun add 
    //小boss技能
    public static final Ability[] bossxAbility = {
    	Ability.getAbility(160),	//冰冻之门
    	Ability.getAbility(168),	//图腾之盾
    	Ability.getAbility(192),	//魔法吸收
    	Ability.getAbility(184),	//攻击吸收
    	Ability.getAbility(176),	//天堂圣盾
    	Ability.getAbility(202),	//大地复生
    	Ability.getAbility(210),	//荆棘之墙
    	Ability.getAbility(215),	//魔力镜
    	Ability.getAbility(222),	//强效治疗
    };
    //boss技能
    public static final Ability[] bossAbility = {
    	Ability.getAbility(7),		//缤纷连击7
    	Ability.getAbility(24),		//崩溃一击
    	Ability.getAbility(32),		//致晕攻击
    	Ability.getAbility(40),		//狂暴一击
    	Ability.getAbility(72),		//聚法一击
    	Ability.getAbility(79),		//群鹰出击
    	Ability.getAbility(104),	//石化魔法
    	Ability.getAbility(112),	//霜冻魔法
    };
    
    public int subHurtPercent = 0;			//减少伤害百分比
    public int addHurtPercent = 0;			//增加伤害百分比
    //原先是都是10% 现在改成5% 降低难度
    static public int subRoundMpPercent = 5;		//每回合减少Mp百分比
    static public int subRoundHpPercent = 5;		//蓝低于5%时 每回合减少Hp百分比
    public int hurtToMasterPercent = 95;	//徒弟受到的伤害由师傅承担百分比
    public int bossHpMul = 2500;			//Boss的Hp乘数 默认是没有师徒关系的值
    public static final int BOSS_HP_HASMASTER_MUL = 2000;		//拥有师徒关系时 乘数需要设置为该值
    public int bossxHpInBossPercent = 40;	//小boss的HP为boss的Hp的百分比 没有师徒关系
    public static final int BOSSX_HP_INBOSS_PERCENT = 30;		//拥有师徒关系时 小BOSS的血量为BOSS的30%
    public int addHurtPercentBoss = 0;		//增加伤害百分比
    public int bossxDieRoundRef = 0;		//小Boss死亡回合计数
    public boolean hasRelation = false;		//是否拥有关系
    public int levelSum = 0;				//等级和
    public int dieState = 0;				//死亡状态
    
    public static Random rnd = new Random();	//共享随机数
    
    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                      BattleSprite[] them, BattleSprite[] ourPet,
                      BattleSprite[] themPet, Vector battleMovie,
                      BattleDataProcess battleDataProcess,int round) {
    	//需要初始化
    	if (round == 1) {
    		defenceAllDebuff(bs);
    		//设置免疫控制
    		bs.setImmuneControl(true);
            for(int i=0; i<them.length; i++){
            	levelSum += them[i].player.getLevel();
            	if(playerService.getMasetService().isMaster(them[i].player)){
            		int apprentice = 0;	//徒弟个数
            		Master[] master = playerService.getMasetService().getRelation(them[i].player);
            		for(int j=0; j<master.length; j++){
            			for(int ii=0; ii<them.length; ii++){
            				if(ii != i && them[ii].player.getId() == master[j].getPrenticeId()){
            					apprentice ++;
            					hasRelation = true;
            				}
            			}
            		}
            		//带一个徒弟时 伤害减少50% 带两个时伤害减少75%
            		if(apprentice == 1){
            			subHurtPercent = 50;
            			addHurtPercent = 100;
            		}else if(apprentice == 2){
            			subHurtPercent = 75;
            			addHurtPercent = 200;
            		}
            	}
            }
            if(hasRelation){
            	bossHpMul = BOSS_HP_HASMASTER_MUL;
            	bossxHpInBossPercent = BOSSX_HP_INBOSS_PERCENT;
            }
        }
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
        	if(bs == our[0] || bs == our[2]){
        		int skillindex = Utils.getRandom(rnd, 0, bossxAbility.length - 1);
        		if(bossxAbility[skillindex].getMana() > bs.mp){
        			return useDefaultAttackToHighestLevel(bs, them);
        		}
        		int percent = 50;
        		for(int i=0; i<our.length; i++){
        			if(our[i] != bs && !our[i].testDie()){
        				if(Utils.getRandom(rnd, 0, 100) < percent){
        					return useAbilityForTeam(bs, our[i], bossxAbility[skillindex]);
        				}else{
        					percent += 50;
        				}
        			}
        		}
        		return useAbilityForSelf(bs, bossxAbility[skillindex]);
        	}
        	int skillindex = Utils.getRandom(rnd, 0, bossAbility.length - 1);
        	if(!hasRelation){
        		if(bossAbility[skillindex].getMana() > bs.mp){
        			return useDefaultAttackToHighestLevel(bs, them);
        		}else{
        			return useAbilityToHighestLevelUnit(bs, bossAbility[skillindex], them);
        		}
        	}
        	if(bossAbility[skillindex].getMana() > bs.mp){
    			return useDefaultAttackToHighestUnit(bs, them, themPet);
    		}else{
    			return useAbilityToHighestUnit(bs, bossAbility[skillindex], them, themPet);
    		}
        }
        
    }
}
