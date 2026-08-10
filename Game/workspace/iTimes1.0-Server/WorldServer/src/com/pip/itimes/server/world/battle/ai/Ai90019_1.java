package com.pip.itimes.server.world.battle.ai;

import java.util.ArrayList;
import java.util.Vector;

import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.BattleStrategy;
import com.pip.itimes.server.world.battle.Skill;
import com.pip.itimes.server.world.battle.SkillConstants;

/**
 * 缤纷连击7，；群鹰出击7，；群鹰出击8，；缤纷连击8，；天堂之盾8，；致晕攻击8，；霜冻魔法8，；
 * 
 *
 */
public class Ai90019_1 extends BaseMonsterAI{
	
	private Skill ATTACK_SKILL = new Skill(Skill.SKILL_ATTACK, Skill.TYPE_PHY, 0, Skill.ENMITY_ALL, 0, Skill.SPEED_METHOD_ORDER_3,"攻击");
	
	private Skill SPECIAL_SKILL_A = new Skill((short)30001,Skill.TYPE_STAY,0,Skill.ENMITY_ALL,0,Skill.SPEED_METHOD_FIRST,"");
	private Skill SPECIAL_SKILL_B = new Skill((short)30002,Skill.TYPE_STAY,0,Skill.ENMITY_ALL,0,Skill.SPEED_METHOD_FIRST,"");
	private Skill SPECIAL_SKILL_C = new Skill((short)30003,Skill.TYPE_STAY,0,Skill.ENMITY_ALL,0,Skill.SPEED_METHOD_FIRST,"");	
	private int dieIndex = -1;
	private int dieIndex1 = -1;
	private int special_a_round = -1;
	private Skill currentSkill = SPECIAL_SKILL_A;
	private  ArrayList<String> messageList = new ArrayList<String>();
	
	public Ai90019_1(){
	
		
	}
	
	public int getSpecialHp(){
		return 3500000;
	}
	
	public boolean action(BattleSprite bs,int index,BattleSprite[] our,BattleSprite[] them,BattleSprite[] ourPet,
			BattleSprite[] themPet,Vector battleMovie,BattleDataProcess battleDataProcess,int round){
		//3回合内不一起死亡则全部复活并满血
		//boss身边有俩小弟，小弟负责无脑输出，
		//不给boss加血，但是如果小弟死了，一定回合小弟复活，如果boss死了，则小弟不会再复活
		BattleSprite bsTemp = null;
		for(int i=0;i<our.length;i++){
            if(our[i]!=bs&&our[i].getDebufStatus()==Skill.STATUS_DIE){
            	if(our[i].dieRound + 3 < round){
            		bsTemp = our[i];
            		break;
            	}
            }
        }
        if(bs.testDie()){
        	shout("我一定会回来的......",bs,them);
        }else{
            if (bsTemp != null){
            	dieIndex = -1;
            	Skill skill = (Skill)Skill.getSkill(207).clone();
    			skill.parm1 = bsTemp.attributes[BattleSprite.ATTR_HPMAX];
    			skill.mpUse = 0;
            	return useSkillForTeam(bs, bsTemp, skill);
            } 
        }
        if(!bs.canAction()){
        	defaultCannotActionAction(bs);
        	return false;
        }else{
        	
        	 if(special_a_round >= 0){
         
                 if(round > special_a_round + 1){
                	 special_a_round = -1;
                 }
                 
                 switch(currentSkill.id){
                     case 30001:
                         antiDefence(bs);
                         defenceAllDebuff_1_round(bs);
                         Skill skill1 = getSkill();
                         if(skill1.id == 176){
                        	 shout(getMessage(),bs,them);
                             return useSkillForSelf(bs, skill1);
                         }else{
                        	 shout(getMessage(),bs,them);
                             return useSkillToHighestUnit(bs, skill1, them, themPet);
                         }
                     case 30002:  
                    	 antiDefence(bs);
                         defenceAllDebuff_1_round(bs);
                         Skill skill2 = getSkill();
                         if(skill2.id == 176){
                        	 shout(getMessage(),bs,them);
                             return useSkillForSelf(bs, skill2);
                         }else{
                        	 shout(getMessage(),bs,them);
                             return useSkillToHighestUnit(bs, skill2, them, themPet);
                         }
                     case 30003:               
                    	 antiDefence(bs);
                         defenceAllDebuff_1_round(bs);      
                         Skill skill3 = getSkill();
                         if(skill3.id == 176){
                        	 shout(getMessage(),bs,them);
                             return useSkillForSelf(bs, skill3);
                         }else{
                        	 shout(getMessage(),bs,them);
                             return useSkillToHighestUnit(bs, skill3, them, themPet);
                         }
                        
                 }
             }else{
                 Skill stateskSkill = stateChange();
                 currentSkill = stateskSkill;

                 if(stateskSkill.id != 30001){
                	 special_a_round = round;
                 }                
                 switch(stateskSkill.id){
                     case 30001:
                    	 special_a_round = round;
                         shout("圣诞快乐！",bs,them);
                         antiDefence(bs);
                         defenceAllDebuff_1_round(bs);
                         int incHp = Utils.getRandom(6000, 12000);
                         int themIndex = Utils.getRandom(0, (them.length-1));
                         them[themIndex].changeHp(incHp, battleMovie, battleDataProcess);
                    	 int[] movie = BattleStrategy.makeMovieSub(them[themIndex].bsType, themIndex, bs.bsType, bs.index, SkillConstants.SKILL_LIFE_MAGIC, 
                    			 SkillConstants.ANIMATE_NONE, SkillConstants.POSITION_STAY, SkillConstants.OVER_POSITION_BACK,
                    			 SkillConstants.MOVIE_SPEED_FAST, SkillConstants.HIT_HIT, bs.getDebufStatus(), SkillConstants.ATTACK_NO_CRI,
                    			 incHp, 0, 0, 0);
                         battleMovie.addElement(movie);

                        break;
                         
                     case 30002:
                    	 special_a_round = round;
                         shout("走你 ！", bs, them);
                         antiDefence(bs);
                         defenceAllDebuff_1_round(bs);                    
                         int number = Utils.getRandom(0, 1);
                         if(number == 0){
                        	 bs.AddAttrBuf(1, -2000, -2000,0 , 0, 0, 0, 0, 0, 0, 0, 0, 30002);
                        	 return useSkillToHighestUnit(bs, ATTACK_SKILL, them, themPet);
                         }else{
                            bs.AddAttrBuf(1, 50, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30002);
                             return useSkillToHighestUnit(bs, ATTACK_SKILL, them, themPet);
                         }   
                     case 30003:
                    	 special_a_round = round;
                         antiDefence(bs);
                         defenceAllDebuff_1_round(bs);
                         Skill skill2 = getSkill();
                         if(skill2.id == 176){
                        	 shout(getMessage(),bs,them);
                             return useSkillForSelf(bs, skill2);
                         }else{
                        	 shout(getMessage(), bs, them);
                             return useSkillToHighestUnit(bs, skill2, them, themPet);
                         }
                         
                 }
             }
        }
		return false;
	}
    public Skill stateChange(){
        clearSkill();
        addSkill(SPECIAL_SKILL_A, 30, true);
        addSkill(SPECIAL_SKILL_B, 35, true);
        addSkill(SPECIAL_SKILL_C, 35, true);

        return pickSkill();
    }
    public Skill getSkill(){       	
    	clearSkill();
    	addSkill(7,10,true);
		addSkill(8,20,true);
		addSkill(79,20,true);
		addSkill(80,15,true);
		addSkill(176,15,true);
		addSkill(32,10,true);
		addSkill(112, 10, true);
		return pickSkill();
    }
    public void setMessageList(){
    	messageList.add("我最喜欢捏泡泡了，可以抒发心中的抑郁");
    	messageList.add("绿泡泡，嘎嘣脆，地沟油味");
    	messageList.add("当我的泡泡破碎的时候就是你也要破碎的时候");
    	messageList.add("泡泡啊，请赐予我力量吧");
    	messageList.add("放开那泡泡，我把妹子给你");
    	messageList.add("吃饭、睡觉、捏泡泡，这才是人生");
    	messageList.add("你已经知道我藏了很多私房钱了，这个秘密决不能让我老婆知道");
    	messageList.add("这位朋友来俩张泡泡纸么，蓝色品质哦");
    	messageList.add("你来打我啊，打我啊··");
    	messageList.add("喋喋不休！谁也拦不住");
    	messageList.add("哥们，有没有发现我又帅了");
    	messageList.add("你该强化下了，根本穿不透我的脸皮");
    }
    public String getMessage(){
    	if(messageList.size()== 0){
    		setMessageList();
    		int index = Utils.getRandom(0, (messageList.size()-1));
    		String message = messageList.get(index);
        	messageList.remove(index);
        	return message;
    	}else{
	   	   	while(messageList.size()>0){
	    		int index = Utils.getRandom(0, (messageList.size()-1));
	    		String message = messageList.get(index);
	        	messageList.remove(index);
	        	return message;
	    	}
    	}
    	return "";
    	
    	
    }
   /* public int getRandomInt(int...i){
    	return 1;
    }
    public static int getMax(int...arg)
    {
	     if(arg.length==0)
	     {
	      System.out.println("fatal error:maxium of zero values.");
	      System.exit(0);
	     }
	     int largest=arg[0];
	     for(int i=1;i<arg.length;i++)
	     {
	      if(arg[i]>largest)
	       largest=arg[i];
	     }
	     return largest;
	    }
*/
      
}
