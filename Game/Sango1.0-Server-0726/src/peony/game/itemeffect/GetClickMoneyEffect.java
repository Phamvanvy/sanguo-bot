package peony.game.itemeffect;

import java.text.MessageFormat;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Time;
import peony.game.Unit;
import peony.game.UseItemException;

public class GetClickMoneyEffect implements ItemEffect {

	public static long ONLINE_TIME = 3600 * 1000L;
	public int money;
	
	public boolean isAsync() {
		return false;
	}

	public GetClickMoneyEffect(int money){
		this.money = money;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		Player p = (Player)source;
		long t = p.pool.getLong(Player.PROPERTY_CLICKMONEY_START_TIME, 0L);
		if(t==0){
			p.pool.setLong(Player.PROPERTY_CLICKMONEY_START_TIME, System.currentTimeMillis());
			p.pool.setLong(Player.PROPERTY_CLICKMONEY_CUMULATE_TIME, 0);
			return;
		}
		long onlineTime = p.pool.getLong(Player.PROPERTY_CLICKMONEY_CUMULATE_TIME, 0L);
		long totalOnlineTime = System.currentTimeMillis() - t + onlineTime;
		if(totalOnlineTime>=ONLINE_TIME){
			int day = p.pool.getInt(Player.PROPERTY_CLICKMONEY_DAY);
			if(day != Time.day){
				p.pool.setInt(Player.PROPERTY_CLICKMONEY_DAY, Time.day);
				p.pool.setInt(Player.PROPERTY_CLICKMONEY_TIMES, 0);
			}
			int times = p.pool.getInt(Player.PROPERTY_CLICKMONEY_TIMES,0);
			p.pool.setLong(Player.PROPERTY_CLICKMONEY_START_TIME, System.currentTimeMillis());
			p.pool.setLong(Player.PROPERTY_CLICKMONEY_CUMULATE_TIME, 0);
			tx.setCause("CMY");
			p.addMoney(money, tx, true);
			p.message(-1, MessageFormat.format("你获得了{0}金钱", money), -1, -1);
			p.pool.setInt(Player.PROPERTY_CLICKMONEY_TIMES, times+1);
		}else{
			long min = (ONLINE_TIME - totalOnlineTime) / (60 * 1000L);
			if(min==0) min = 1;
			p.message(-1, MessageFormat.format("还需要{0}分钟才能得到金钱奖励", min), -1, -1);
		}
	}

}
