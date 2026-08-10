package peony.game.drop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.game.CommonUtil;
import peony.game.Gain;
import peony.game.GameItem;
import peony.game.GameObjectRef;
import peony.game.GameQuest;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.roll.Roll;
import peony.game.roll.RollService;
import peony.net.Packet;
import peony.vm.ASMQuestUtil;

public class ItemDrop extends SimpleDrop {
	
	private static final Logger log = Logger.getLogger(ItemDrop.class);

	protected ItemTemplate template;
	public ItemDrop(ItemTemplate template, int questId, int min, int max) {
		super(questId, min, max);
		this.template = template;
	}

	public void calc(Random rnd, Gain gain) {
	    List<Gain> validGains = new ArrayList<Gain>();
	    
	    // 判断哪些人可以获得拾取权
	    if (gain.getGains() == null) {
	        if (canGet(rnd, gain.getPlayer())) {
	            validGains.add(gain);
	        }
	    } else {
	        for (Gain g : gain.getGains()) {
	            if (canGet(rnd, g.getPlayer())) {
	                validGains.add(g);
	            }
	        }
	    }
	    if (validGains.size() == 0) {
	        return;
	    }
	    Gain[] validGainsArr = new Gain[validGains.size()];
	    validGains.toArray(validGainsArr);
	    
	    // 判断谁能获得掉落权，如果是任务复制的物品，所有人都有权获得；绿色以上物品需要ROLL
	    if (questId != -1) {
	        if (!template.isTaskCopy) {
	            // 不复制的任务物品，随机选择一个人掉落
	            internalCalc(rnd, validGainsArr);
	        } else {
	            // 复制的任务物品，每个人都获得掉落机会
	            long l1 = System.currentTimeMillis(); // 保证所有人掉或者不掉
	            for (Gain g : validGains) {
	                internalCalc(new Random(l1), new Gain[] { g });
	            }
	        }
	    } else {
	        internalCalc(rnd, validGainsArr);
	    }
	}

	protected void internalCalc(Random rnd, Gain[] gains) {
		if (template.isEquipment()) {
			GameItem item = ObjectAccessor.createGameItem(template, -1);
			processGainItem(rnd, item, gains, 1);
		} else {
			int c = CommonUtil.getCount(rnd, min, max);
			if (c > 0) {
				if(template.newInstance){
					for(int i=0;i<c;i++){
						GameItem item = ObjectAccessor.createGameItem(template, -1);
						processGainItem(rnd, item, gains, 1);
					}
				}else{
					GameItem item = ObjectAccessor.createGameItem(template, -1);
					processGainItem(rnd, item, gains, c);
				}
			}
		}
	}

	protected void processGainItem(Random rnd, GameItem item, Gain[] gains, int count) {
	    // 如果只有一个玩家候选，直接给
	    if (gains.length == 1) {
	    	if(item.template.id == Player.XUANWUSHI_ITEM){
		    	int addCount= gains[0].getPlayer().addXuanwuItem(count, 1);
		    	if(addCount<=0)
		    		return;
		    	else
	    		    count = addCount;
	    	}
	        addGainItem(gains[0], item, count);
	        return;
	    }
	    
	    // 如果品质绿色以上，ROLL
		if (item.template.quality > 0) {
			GameObjectRef[] refs = new GameObjectRef[gains.length];
			for (int i = 0; i < gains.length; i++) {
				refs[i] = gains[i].getPlayer().ref();
			}
			Roll roll = new Roll(Server.server.getServiceRegistry()
					.getRollService(), refs, item, count, Time.currTime);
			Packet pt = new Packet(OpCode.ROLL_SERVER);
			pt.putInt(roll.id);
			pt.put(GameItem.toClientBytes(item.template));
			pt.putInt(Time.currTime + Roll.TIMEOUT);
			for (Gain g : gains) {
			    g.getPlayer().send(pt);
			}
			LogUtil.logCreateRoll(roll.id, gains, item, count);
		} else {
		    // 随机选择一个玩家给予
		    int index = rnd.nextInt(gains.length);
		    if(item.template.id == Player.XUANWUSHI_ITEM){
		    	int addCount= gains[0].getPlayer().addXuanwuItem(count, 1);
		    	if(addCount<=0)
		    		return;
		    	else
	    		    count = addCount;
	    	}
		    addGainItem(gains[index], item, count);
		}
	}
	
	protected void addGainItem(Gain gain, GameItem item, int count) {
	    if (questId != -1) {
	        int c = gain.getPlayer().bag.getGameItemCount(template.id);
	        int maxCount = Math.max(count, c - template.maxCount);
            if (maxCount > 0) {
                gain.addGainItem(item, maxCount);
            }
	    } else {
	        gain.addGainItem(item, count);
	    }
	}
	
	private boolean canGetSpecialItem(Player p,int itemId){
		if(itemId == Player.XUANWUSHI_ITEM){
			if(p.topBossOfXuanwushi())
				return false;
		}
		return true;
	}

	/*
	 * 判断一个玩家是否能够获得此物品。任务物品只有拥有任务的人才能拾取。
	 * 触发任务的物品只能获得一次（重复任务例外）。
	 */
	private boolean canGet(Random rnd, Player p) {
	    if (questId != -1) {
            if (p.asmVm.hasTask(questId) == 0) {
                return false;
            }
            int c = p.bag.getGameItemCount(template.id);
            if (c >= template.maxCount) {
                return false;
            }
	    }
	    if (template.triggerQuest != null) {
            int qid = template.triggerQuest.getQuestID(p.faction);
            GameQuest quest = ASMQuestUtil.getGameQuest(qid);
            if (quest != null) {
                if (quest.getRepeatType() == 0) {
                    // 不可重复的任务，需要没有完成过，并且背包里没有才能掉落
                    if (p.asmVm.hasTask(qid) == 0 &&
                        p.asmVm.taskFinished(qid) == 0 &&
                        p.bag.getGameItemCount(template.id) == 0) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
        	// 处理防沉迷系统
        	if (p.tirePercent == 0.0f) {
        		return false;
        	} else if (p.tirePercent < 1.0f) {
        		return CommonUtil.hit(rnd, (int)(p.tirePercent * 1000000), 1000000) && canGetSpecialItem(p,template.id);
        	} else {
        		if(canGetSpecialItem(p,template.id))
        		    return true;
        		else
        			return false;
        	}
        }
	}
}
