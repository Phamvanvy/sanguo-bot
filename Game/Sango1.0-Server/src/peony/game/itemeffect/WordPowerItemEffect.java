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
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.drop.GroupDrop;

public class WordPowerItemEffect implements ItemEffect {
	
    protected int group1;
    protected int mapId;
    protected int mapX;
    protected int mapY;
    protected int distance;
    private boolean isNeedRemove;	//是否删除这个物品
    
    private static final Random rnd = new Random();
	
	public WordPowerItemEffect(int g1, int mapId, int mapX, int mapY, int dis) {
		this.group1 = g1;
        this.mapId = mapId;
        this.mapX = mapX;
        this.mapY = mapY;
        this.distance = dis;
        isNeedRemove = false;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player) && target.id!=8323221)
			throw new UseItemException(peony.Messages.STRING_00014);
		
		// 根据掉落组计算掉落物品
		Player p = (Player)source;
		boolean isRange = false;	//是否在挖宝范围内
		if(p.map.getId() == mapId){
			if(Math.abs(p.x - mapX)<=distance && Math.abs(p.y - mapY)<=distance)
				isRange = true;
		}
		
		if(!isRange){
			throw new UseItemException("元芳禀报大人，我觉得此事必有蹊跷，还请仔细推敲宝图上的线索。");
		}
		int groupID = getGroupIds();
		Gain gain = new Gain(p);
		GroupDrop gd = ObjectAccessor.getGroupDrop(groupID);
		gd.calc(rnd, gain);
		tx.setCause("WORLDPOWERITEM");
		// 把物品一件件加入背包
		for (GainItem gi : gain.getGainItems()) {
			if(item != null){
				PlayerTransaction tx2 = p.newTransaction("WORLDPOWERITEM");
				try {
					p.bag.addGameItemComplete(gi.getItem(), gi.getCount(), tx2, true);
					tx2.commit();
				} catch (Exception e) {
					tx2.rollback();
					String content = MessageFormat.format("{0}奖励", item.template.name);
					Server.server.getServiceRegistry().getMailService()
					.sendSystemMail(p.id, peony.Messages.STRING_00004,content, content, 0,
							gi.getItem(), gi.getCount(), "WORLDPOWERITEM");
				}
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
		if(gain.getSalary() > 0){
        	p.addSalary(gain.getSalary(), tx, true);
        }
	   isNeedRemove = true;
	}
	
	public boolean isAsync(){
		return false;
	}
	
	public int getGroupIds(){
		return group1;
	}
	
	public boolean needRemove() {
		return isNeedRemove;
	}
}
