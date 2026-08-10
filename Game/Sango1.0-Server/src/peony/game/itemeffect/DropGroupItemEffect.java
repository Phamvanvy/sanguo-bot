package peony.game.itemeffect;

import java.text.MessageFormat;
import java.util.Random;
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
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.drop.GroupDrop;

public class DropGroupItemEffect implements ItemEffect {
	
    protected String groups;
    protected int useItemID;
    protected int useItemCount;
    protected boolean supportMail;
    
    private static final Random rnd = new Random();
	
	public DropGroupItemEffect(String groups, int useItemID, int useItemCount, boolean supportMail) {
		this.groups = groups;
		this.useItemID = useItemID;
		this.useItemCount = useItemCount;
		this.supportMail = supportMail;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player sp = (Player)source;
        try {
            if (useItemCount > 0 && useItemID >= 0) {
            	if(sp.bag.removeGameItemIngoreInstanceId(useItemID, useItemCount, tx, true)==null){
            		ItemTemplate it = ObjectAccessor.getItemTemplate(useItemID);
            		if(it==null)
            			it = ObjectAccessor.createGameItem(useItemID).template;
                    throw new UseItemException(MessageFormat.format(peony.Messages.STRING_01015, it.name));
            	}
            }
            
            String[] str0 = groups.split(",");
    		for(String str1 : str0){
    			int groupID = Integer.parseInt(str1);
    			Gain gain = new Gain(sp);
        		GroupDrop gd = ObjectAccessor.getGroupDrop(groupID);
        		gd.calc(rnd, gain);
                
                // 把物品一件件加入背包
                for (GainItem gi : gain.getGainItems()) {
                	if(gi.getItem().template!=null&&gi.getItem().template.itemValid!=null&&gi.getItem().template.itemValid.time>0){
        				//装备本身属性
                		gi.getItem().validTime=(int)(System.currentTimeMillis()/60000+gi.getItem().template.itemValid.time);
        			}
                    if (!sp.bag.addGameItem(gi.getItem(), gi.getCount(), tx, true)) {
                        // 包满，分两种情况：发飞鸽或者失败
                        if (supportMail) {
                        	int remain = sp.bag.remainCountOnLastFail;
                        	gi.add(remain - gi.getCount());
                        	tx.addMailItem(gi);
                        } else {
                            throw new NoEnoughSpaceException();
                        }
                    }
                    
                    // 判断是否需要通知
        			if (ItemUtil.getNoticeType(gi.getItem().template.id) != 0 || AddItemEffect.specialNoticeItem(item, gi.getItem().template.id)) {
        				tx.addNoticeItem(gi.getItem());
        			}
                }
                
                // 添加金钱、经验、战功、声望
                if (gain.getMoney() > 0) {
                    sp.addMoney(gain.getMoney(), tx, true);
                }
                if (gain.getExp() > 0 && sp.level < 30) {
                    sp.addExp(gain.getExp(), tx, true);
                }
                if (gain.getCredit() > 0) {
                    sp.addCredit(gain.getCredit(), tx, true);
                }
                if (gain.getHonor() > 0) {
                    sp.addHonor(gain.getHonor(), tx, true);
                }
                if(gain.getSalary() > 0){
                	sp.addSalary(gain.getSalary(), tx, true);
                }
    		}
        } catch (NoEnoughSpaceException e) {
            throw new UseItemException(peony.Messages.STRING_01016);
        }
	}
	
	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
