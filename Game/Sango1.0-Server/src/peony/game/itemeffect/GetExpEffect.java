package peony.game.itemeffect;

import java.text.MessageFormat;
import java.util.Random;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.service.tong.TongSkill6;

/**
 * 获得经验。
 * @author lighthu
 */
public class GetExpEffect implements ItemEffect {
    protected float amount;
    protected int[] valueTable;
	public static Random rnd = new Random();
	
	public GetExpEffect(float amount, int[] valueTable) {
	    this.amount = amount;
	    this.valueTable = valueTable;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)source;
		int expLock = p.pool.getInt(Player.PROPERTY_LOCK_EXP, Player.EXP_UNLOCK);  //如果玩家锁住经验，物品不能使用
		if(expLock == Player.EXP_LOCK){
			throw new UseItemException("你已锁定经验，无法再获得经验，如想获得经验请去主城官职管理员处解锁。");
		}
		int addValue;
		if (valueTable != null) {
            addValue = valueTable[source.level - 1];
        } else if (amount >= 0) {
		    addValue = (int)amount;
		} else {
		    addValue = (int)(-(amount * source.level));
		}
		//军团专属科技   福星高照    特殊处理赐禄千石
		if(item.template.id == 1929){
			try{
				TongService ts = Server.server.getServiceRegistry().getTongService();
				TongMember tm = ts.getPlayerInfo(p.id);
				if(tm!=null && tm.skills.get(6)!=null){
					TongSkill6 tskill = (TongSkill6)tm.skills.get(6);
					int ratios = tskill.getRatios();
					if(rnd.nextInt(100) >= (100 - ratios)){
						addValue *= 2;
					}
				}
			}catch(Exception e){
				throw new UseItemException(e.getMessage());
			}
		}
		p.addExp(addValue, tx, true);
	}
	
	public void getExp(Player p,int count,GameItem item) throws Exception{
		int toalValue = 0;
		for(int i=0;i<count;i++){
			int addValue;
			PlayerTransaction tx = p.newTransaction("BULKUSE");
			if (valueTable != null) {
	            addValue = valueTable[p.level - 1];
	        } else if (amount >= 0) {
			    addValue = (int)amount;
			} else {
			    addValue = (int)(-(amount * p.level));
			}
			//军团专属科技   福星高照    特殊处理赐禄千石
			if(item.template.id == 1929){
				try{
					TongService ts = Server.server.getServiceRegistry().getTongService();
					TongMember tm = ts.getPlayerInfo(p.id);
					if(tm!=null && tm.skills.get(6)!=null){
						TongSkill6 tskill = (TongSkill6)tm.skills.get(6);
						int ratios = tskill.getRatios();
						if(rnd.nextInt(100) >= (100 - ratios)){
							addValue *= 2;
						}
					}
				}catch(Exception e){
					tx.rollback();
					throw new Exception(e.getMessage());
				}
			}
		    p.addExp(addValue, tx, true);
		    tx.commit();
		    toalValue+= addValue;
		}
		if(toalValue>0)
		     Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, MessageFormat.format("恭喜您获得了{0}点经验", toalValue));
	}
	
	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
