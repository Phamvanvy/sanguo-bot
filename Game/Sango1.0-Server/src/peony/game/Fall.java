package peony.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import peony.game.drop.CreditDrop;
import peony.game.drop.Drop;
import peony.game.drop.ExpDrop;
import peony.game.drop.MoneyDrop;
import peony.game.drop.RateDrop;
import peony.game.nation.Nation;
import peony.game.nation.NationSkill7;
import peony.service.VIP.VipPrivilegeService;

public class Fall {
	
	protected List<Drop> drops = new ArrayList<Drop>();
	protected GameObject source;
	protected boolean isPartyModel; // 是否是填队模式
	protected long partyModelRatio; // 填队模式增幅
	protected static boolean partyExpMod = true; //是否开启组队经验模式
	
	public Fall(GameObject source){
		this.source = source;
	}
	
	public void addDrop(Drop drop) { //对于金钱以及经验奖励需要特殊处理，因为在组队状态下需要平分经验以及金钱
		drops.add(drop);
	}
	
	/*
	 * Fall被用在多个地方，怪物掉落以及采集的掉落都是用的这个类，但是只有在计算怪物掉落，并且玩家是组队的状况才会传入多个Gain
	 * moneyDrop以及 expDrop不为null的情况只有source是一个Creature
	 */
	public void gain(Random rnd, Gain[] gains) {
		if(isPartyModel){
			processPartyGain(rnd, gains);
		}else{
			processNormalGain(rnd, gains);
		}
	}
	
	/** 普通模式掉落 */
	protected void processNormalGain(Random rnd, Gain[] gains){
		//对于每个掉落需要随即分配一个队员获得
	    // 计算所有的掉落，金钱和经验掉落暂时不分配，累计起来待会儿再分配
	    int money = 0;
	    int exp = 0;
	    for (Drop drop : drops) {
	        if (drop instanceof MoneyDrop) {
	            // 直接金钱掉落
	            money += ((MoneyDrop)drop).calc(rnd);
	            continue;
	        } else if (drop instanceof ExpDrop) {
	            // 直接经验掉落
	            exp += ((ExpDrop)drop).calc(rnd);
	            continue;
	        } else if (drop instanceof RateDrop) {
	            RateDrop rd = (RateDrop)drop;
	            if (rd.getDrop() instanceof MoneyDrop) {
	                // 带几率的金钱掉落
	                if (rd.hit(rnd)) {
	                    money += ((MoneyDrop)rd.getDrop()).calc(rnd);
	                }
	                continue;
	            } else if (rd.getDrop() instanceof ExpDrop) {
	                // 带几率的经验掉落
	                if (rd.hit(rnd)) {
	                    exp += ((ExpDrop)rd.getDrop()).calc(rnd);
	                }
	                continue;
	            }
	        }else if(drop instanceof CreditDrop){
	        	continue;
	        }
	        
	        // 带几率的物品掉落
            Gain gain = getGain(rnd, gains);
            if (gains.length > 1) {
                gain.setGains(gains);
            }
            drop.calc(rnd, gain);
	    }
	    
	    // 金钱平均分配
        if (money > 0) {
            int part1 = money / gains.length;
            int part2 = money % gains.length;
            for (int i = 0; i < gains.length; i++) {
            	//当人物级别是怪物级别2倍的时候，打怪将不会获得金钱；
            	if(source!=null){
            		int playerLevel = gains[i].getPlayer().level;
            		if(playerLevel>=90){
            			playerLevel = 89;
            		}
            		if((source.level+20)<=playerLevel){
                        continue;
                	}
            	}
            	
                int addValue = part1;
                if (i < part2) {
                    addValue++;
                }
//                addValue *= gains[i].getPlayer().moneyRatio;
                addValue *= Server.server.moneyRatio;
                addValue *= gains[i].getPlayer().tirePercent;
                gains[i].addMoney(addValue);
//                processSkillNationMoney(gains[i].getPlayer(), addValue);
            }
        }
        
        // 经验按等级分配，个人经验 = （怪物经验×（0.7 + 0.3×（最终受益人级别和/最终受益人最高级别））×个人级别）/ 最终受益人级别和。
        // 这个经验再经过等级惩罚，成为最终玩家获得的经验。
        if (exp > 0) {
        	int total = gains.length;
    		int totalLevel = 0, maxLevel = 1;
            for (int i = 0; i < gains.length; i++) {
                int lvl = gains[i].getPlayer().level;
                totalLevel += lvl;
                if (lvl > maxLevel) {
                    maxLevel = lvl;
                }
            }
            for (int i = 0; i < gains.length; i++) {
                Player gainPlayer = gains[i].getPlayer();
                int lvl = gainPlayer.level;
                int pexp = (int)(exp * (0.7 + 0.3 * totalLevel / maxLevel) * lvl / totalLevel);
//                pexp = pexp * (10 - Math.abs(lvl - source.level)) / 10;
                //在这里处理坐骑经验提升，按坐骑级别跟怪物级别走
                if(gainPlayer.horse!=null && gainPlayer.horse.level<gainPlayer.level){
                	int hexp = (int)(exp * 0.12f * gainPlayer.getHorseExpRatio());
                	if (gainPlayer.horse.level-source.level>=10)
                		hexp = hexp / 10;
                	else if(gainPlayer.horse.level-source.level>0 && gainPlayer.horse.level-source.level<10){
                		hexp *= (1-(gainPlayer.horse.level-source.level)/10f);
                	}
                	hexp = VipPrivilegeService.promoteHorseExpGet(gainPlayer, hexp);//vip提高坐骑获得经验
                	gainPlayer.horse.setExp(gainPlayer.horse.exp+hexp, gainPlayer, "HORSEEXP");
                }
                
                if (10 - Math.abs(lvl - source.level) < 1) {
                    pexp = pexp / 10;
                }
                pexp *= gains[i].getPlayer().expRatio;
                pexp *= Server.server.expRatio;
                pexp *= gains[i].getPlayer().tirePercent;
                if(partyExpMod)
                	gains[i].addExp(Math.round((pexp * (1 + (total-1)*0.2f))));
                else
                	gains[i].addExp(pexp);
            }
        }
	}
	
	/** 填队模式掉落 */
	protected void processPartyGain(Random rnd, Gain[] gains){
		int money = 0;
	    int exp = 0;
	    int credit = 0;
	    for (Drop drop : drops) {
	        if (drop instanceof MoneyDrop) {
	            money += ((MoneyDrop)drop).calc(rnd);
	            continue;
	        } else if (drop instanceof ExpDrop) {
	            exp += ((ExpDrop)drop).calc(rnd);
	            continue;
	        } else if (drop instanceof RateDrop) {
	            RateDrop rd = (RateDrop)drop;
	            if (rd.getDrop() instanceof MoneyDrop) {
	                if (rd.hit(rnd)) {
	                    money += ((MoneyDrop)rd.getDrop()).calc(rnd);
	                }
	                continue;
	            } else if (rd.getDrop() instanceof ExpDrop) {
	                if (rd.hit(rnd)) {
	                    exp += ((ExpDrop)rd.getDrop()).calc(rnd);
	                }
	                continue;
	            }
	        } else if(drop instanceof CreditDrop){
	        	credit += ((CreditDrop)drop).calc(rnd);
	        	continue;
	        }
	        
            Gain gain = getGain(rnd, gains);
            if (gains.length > 1) {
                gain.setGains(gains);
            }
            drop.calc(rnd, gain);
	    }
	    
        if (money > 0) {
            int part1 = money / gains.length;
            int part2 = money % gains.length;
            for (int i = 0; i < gains.length; i++) {
            	if(source!=null){
            		int playerLevel = gains[i].getPlayer().level;
            		if(playerLevel>=90){
            			playerLevel = 89;
            		}
            		if((source.level+20)<=playerLevel){
                        continue;
                	}
            	}
                int addValue = part1;
                if (i < part2) {
                    addValue++;
                }
//                addValue *= gains[i].getPlayer().moneyRatio;
                addValue *= Server.server.moneyRatio;
                addValue *= gains[i].getPlayer().tirePercent;
                gains[i].addMoney(addValue);
//                processSkillNationMoney(gains[i].getPlayer(), addValue);
            }
        }
        
        if (exp > 0) {
        	int total = gains.length;
    		int totalLevel = 0, maxLevel = 1;
            for (int i = 0; i < gains.length; i++) {
                int lvl = gains[i].getPlayer().level;
                totalLevel += lvl;
                if (lvl > maxLevel) {
                    maxLevel = lvl;
                }
            }
            for (int i = 0; i < gains.length; i++) {
            	Player gainPlayer = gains[i].getPlayer();
                int lvl = gainPlayer.level;
                int pexp = (int)(exp * (0.7 + 0.3 * totalLevel / maxLevel) * lvl / totalLevel);
                
//                pexp = pexp * (10 - Math.abs(lvl - source.level)) / 10;
                //在这里处理坐骑经验提升，按坐骑级别跟怪物级别走
                if(gainPlayer.horse!=null && gainPlayer.horse.level<gainPlayer.level){
                	int hexp = (int)(exp * 0.12f * gainPlayer.getHorseExpRatio());
                	if (gainPlayer.horse.level-source.level>=10)
                		hexp = hexp / 10;
                	else if(gainPlayer.horse.level-source.level>0 && gainPlayer.horse.level-source.level<10){
                		hexp *= (1-(gainPlayer.horse.level-source.level)/10f);
                	}
                	hexp = VipPrivilegeService.promoteHorseExpGet(gainPlayer, hexp);//vip提高坐骑获得经验
                	gainPlayer.horse.setExp(gainPlayer.horse.exp+hexp, gainPlayer, "HORSEEXP");
                }
                if (10 - Math.abs(lvl - source.level) < 1) {
                	pexp = pexp / 10;
                }
                pexp *= gains[i].getPlayer().expRatio;
                pexp *= Server.server.expRatio;
                pexp *= gains[i].getPlayer().tirePercent;
                if(partyExpMod)
                	gains[i].addExp(Math.round((pexp * (1 + (total-1)*0.2f))));
                else
                	gains[i].addExp(pexp);
            }
        }
        
        if(credit>0){
        	credit = (int) (partyModelRatio * gains.length * credit);
        	for(int i = 0; i < gains.length; i++) {
        		gains[i].addCredit(credit);
        	}
        }
	}
	
	protected void processSkillNationMoney(Player player, int addMoney){
		if(player!=null){
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(player.faction);
	        NationSkill7 skill = (NationSkill7) nation.skills.get(7);
	        float ratio = skill.getRatio();
	        int gainNationMoney = Math.round(addMoney * ratio);
	        nation.addMoney(gainNationMoney);
		}
	}
	
	protected Gain getGain(Random rnd,Gain[] gain){
		if(gain.length==1)
			return gain[0];
		else
			return gain[rnd.nextInt(gain.length)];
	}
}
