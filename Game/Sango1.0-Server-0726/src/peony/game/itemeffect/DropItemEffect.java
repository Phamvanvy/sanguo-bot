package peony.game.itemeffect;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import peony.db.DBService;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.drop.GroupDrop;

public class DropItemEffect implements ItemEffect {
    protected int group1;
    protected int group2;
    protected String varName;
    protected int switchCount;
    protected boolean supportMail;
    protected int useItemID;
    protected int useItemCount;
    private static final Random rnd = new Random();
    protected static int CREATURE_NIANSHOU = 3539125; //年兽ID
	
	public DropItemEffect(int g1, int g2, String varName, int sc, boolean sm, int uid, int uic) {
		group1 = g1;
		group2 = g2;
        this.varName = varName;
		switchCount = sc;
		supportMail = sm;
		useItemID = uid;
		useItemCount = uic;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player) && target.id!=3539125)
			throw new UseItemException("错误的目标");
		
		// 如果设置了控制变量切换2个掉落组，这里判断之
		Player sp = (Player)source;
		int groupID = group1;
		if (varName.length() > 0 && switchCount > 0 && group2 >= 0) {
		    int flag = sp.pool.getInt(varName);
		    if ((flag % (switchCount * 2)) >= switchCount) {
		        groupID = group2;
		    }
		}
		
		// 根据掉落组计算掉落物品
		Player p = null;
	    //特殊处理迎春炮仗，使用目标为年兽，但掉落拥有者为source
		if(target.id==CREATURE_NIANSHOU)
			p = (Player)source;
		else
			p = (Player)target;
		Gain gain = new Gain(p);
		GroupDrop gd = ObjectAccessor.getGroupDrop(groupID);
		gd.calc(rnd, gain);
		
		// 把物品加入背包，并且扣除需求物品
		tx.setCause("ITE");
        try {
            // 把需求的物品从背包中扣除
            if (useItemCount > 0 && useItemID >= 0) {
            	if(p.bag.removeGameItemIngoreInstanceId(useItemID, useItemCount, tx, true)==null){
//                if (p.bag.removeGameItem(useItemID, -1, useItemCount, tx, true) == null) {
            		ItemTemplate it = ObjectAccessor.getItemTemplate(useItemID);
            		if(it==null)
            			it = ObjectAccessor.createGameItem(useItemID).template;
                    throw new UseItemException(MessageFormat.format("您没有{0}，请获得{0}后再试一下", it.name));
            	}
//                }
            }
            
            // 把物品一件件加入背包
            for (GainItem gi : gain.getGainItems()) {
                if (!p.bag.addGameItem(gi.getItem(), gi.getCount(), tx, true)) {
                    // 包满，分两种情况：发飞鸽或者失败
                    if (supportMail) {
                    	int remain = p.bag.remainCountOnLastFail;
                    	gi.add(remain - gi.getCount());
                    	tx.addMailItem(gi);
                    } else {
                        throw new NoEnoughSpaceException();
                    }
                }
                
                // 判断是否需要通知
    			if (ItemUtil.getNoticeType(gi.getItem().template.id) != 0) {
    				tx.addNoticeItem(gi.getItem());
    			}
            }
            
            // 添加金钱、经验、战功、声望
            if (gain.getMoney() > 0) {
                p.addMoney(gain.getMoney(), tx, true);
            }
            if (gain.getExp() > 0 && p.level < 30) {
                p.addExp(gain.getExp(), tx, true);
            }
            if (gain.getCredit() > 0) {
                p.addCredit(gain.getCredit(), tx, true);
            }
            if (gain.getHonor() > 0) {
                p.addHonor(gain.getHonor(), tx, true);
            }
        } catch (NoEnoughSpaceException e) {
            throw new UseItemException("没有足够包格");
        }
        
		// 修改控制变量
		if (varName.length() > 0 && switchCount > 0 && group2 >= 0) {
		    int flag = sp.pool.getInt(varName);
		    sp.pool.setInt(varName, flag + 1);
		}
	}
	
	public boolean isAsync(){
		return false;
	}
	
	public int getGroupIds(){
		return group1;
	}
}
