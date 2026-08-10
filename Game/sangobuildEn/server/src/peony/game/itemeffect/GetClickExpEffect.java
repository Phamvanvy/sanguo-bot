package peony.game.itemeffect;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Time;
import peony.game.Unit;
import peony.game.UseItemException;

public class GetClickExpEffect implements ItemEffect {
	
	public static long ONLINE_TIME = 3600 * 1000L;
	public static Random rnd = new Random();
	public static long CD = 20 * 60 * 60 * 1000L;
	

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx_ext)
			throws UseItemException {
		if(item.template.id==1643){
			// 特殊处理福星赐福包使用逻辑
			useItem(source, item, target, tx_ext);
			return;
		}
		Player p = (Player)source;
		long t = p.pool.getLong(Player.PROPERTY_CLICKEXP_START_TIME, 0L);
		if(t==0){
			p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, System.currentTimeMillis());
			p.pool.setLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, 0);
			return;
		}
		long onlineTime = p.pool.getLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, 0L);
		long totalOnlineTime = System.currentTimeMillis() - t + onlineTime;
		if(totalOnlineTime>=ONLINE_TIME){
			int day = p.pool.getInt(Player.PROPERTY_CLICKEXP_DAY);
			if(day != Time.day){
				p.pool.setInt(Player.PROPERTY_CLICKEXP_DAY, Time.day);
				p.pool.setInt(Player.PROPERTY_CLICKEXP_TIMES, 0);
			}
			int times = p.pool.getInt(Player.PROPERTY_CLICKEXP_TIMES,0);
			if(times>12)
				throw new UseItemException("已經達到每天領取的最大次數");
			p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, System.currentTimeMillis());
			p.pool.setLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, 0);
			int r = rnd.nextInt(100);
			if(r == 1){
				GameItem jewel = ObjectAccessor.createGameItem(1310); //宝石袋
				PlayerTransaction tx = p.newTransaction("CEX");
				try {
					p.bag.addGameItemComplete(jewel, 1, tx, true);
					tx.commit();
					p.message(-1, MessageFormat.format("你獲得了{0}", jewel.template.name), -1, -1);
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					int exp = p.level * 100;
					PlayerTransaction tx1 = p.newTransaction("CEX");
					p.addExp(exp, tx1, true);
					tx1.commit();
					p.message(-1, MessageFormat.format("你獲得了{0}在線經驗", exp), -1, -1);
				}
			}else{
				int exp = p.level * 300;
				PlayerTransaction tx = p.newTransaction("CEX");
				p.addExp(exp, tx, true);
				tx.commit();
				p.message(-1, MessageFormat.format("你獲得了{0}在線經驗", exp), -1, -1);
			}
			p.pool.setInt(Player.PROPERTY_CLICKEXP_TIMES, times+1);
		}else{
			long min = (ONLINE_TIME - totalOnlineTime) / (60 * 1000L);
			if(min==0) min = 1;
			p.message(-1, MessageFormat.format("還需要{0}分鐘才能得到在線獎勵", min), -1, -1);
		}
	}
	
	protected void useItem(Unit source, GameItem item, Unit target, PlayerTransaction tx_ext)
				throws UseItemException {
		Player p = (Player)source;
		
		// 特殊处理福星赐福包
		long lastUseTime = p.pool.getLong(Player.PROPERYY_CLICKEXPSUC_TIME, 0);
		long t = p.pool.getLong(Player.PROPERTY_CLICKEXP_START_TIME, 0L);
		if(t==0){
			p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, System.currentTimeMillis());
			p.pool.setLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, 0);
			p.message(-1, MessageFormat.format("還需要{0}分鐘才能得到在線獎勵", ONLINE_TIME/60000), -1, -1);
			return;
		}
		long onlineTime = p.pool.getLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, 0L);
		if(getDayOfYear(t)!=getDayOfYear(System.currentTimeMillis())){
			p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, getMills(0));
			t = p.pool.getLong(Player.PROPERTY_CLICKEXP_START_TIME, 0L);
		}
		long totalOnlineTime = System.currentTimeMillis() - t + onlineTime;
		
		// 特殊处理福星赐福包(跨天使用)
//		if(p.pool.getInt(Player.PROPERTY_CLICKEXP_DAY)!=Time.day){
//			p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, System.currentTimeMillis());
//			p.pool.setLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, 0);
//			p.pool.setInt(Player.PROPERTY_CLICKEXP_DAY, Time.day);
//			p.pool.setInt(Player.PROPERTY_CLICKEXP_TIMES, 0);
//			p.message(-1, MessageFormat.format("还需要{0}分钟才能得到在线奖励", ONLINE_TIME/60000), -1, -1);
//			return;
//		}
		if(System.currentTimeMillis()-lastUseTime>=CD && totalOnlineTime>=ONLINE_TIME){
			int day = p.pool.getInt(Player.PROPERTY_CLICKEXP_DAY);
			if(day != Time.day){
				p.pool.setInt(Player.PROPERTY_CLICKEXP_DAY, Time.day);
				p.pool.setInt(Player.PROPERTY_CLICKEXP_TIMES, 0);
			}
			int times = p.pool.getInt(Player.PROPERTY_CLICKEXP_TIMES,0);
			if(times>12)
				throw new UseItemException("已經達到每天領取的最大次數");
			p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, System.currentTimeMillis());
			p.pool.setLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, 0);
			int r = rnd.nextInt(100);
			if(r == 1){
				GameItem jewel = ObjectAccessor.createGameItem(1310); //宝石袋
				PlayerTransaction tx = p.newTransaction("CEX");
				try {
					p.bag.addGameItemComplete(jewel, 1, tx, true);
					tx.commit();
					p.message(-1, MessageFormat.format("你獲得了{0}", jewel.template.name), -1, -1);
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					int exp = p.level * 100;
					PlayerTransaction tx1 = p.newTransaction("CEX");
					p.addExp(exp, tx1, true);
					tx1.commit();
					p.message(-1, MessageFormat.format("你獲得了{0}在線經驗", exp), -1, -1);
				}
			}else{
				int exp = p.level * 1000;
				PlayerTransaction tx = p.newTransaction("CEX");
				p.addExp(exp, tx, true);
				tx.commit();
				p.message(-1, MessageFormat.format("你獲得了{0}在線經驗", exp), -1, -1);
			}
			p.pool.setInt(Player.PROPERTY_CLICKEXP_TIMES, times+1);
			
			// 特殊处理福星赐福包
			p.pool.setLong(Player.PROPERYY_CLICKEXPSUC_TIME, System.currentTimeMillis());
		}else{
			if(lastUseTime==0 || (System.currentTimeMillis()-lastUseTime>=CD && totalOnlineTime<ONLINE_TIME)){
				long min = (ONLINE_TIME - totalOnlineTime) / (60 * 1000L);
				if(min==0) 
					min = 1;
				p.message(-1, MessageFormat.format("還需要{0}分鐘才能得到在線獎勵", min), -1, -1);
			}else{
				long min = ((lastUseTime+CD)-System.currentTimeMillis()) / (60 * 1000L);
				if(min==0) 
					min = 1;
				long hour = min/60;
				min = min%60;
				if(hour>0 && min==0)
					p.message(-1, MessageFormat.format("還需要{0}小時才能繼續使用", hour), -1, -1);
				else if(hour>0)
					p.message(-1, MessageFormat.format("還需要{0}小時{1}分鐘才能繼續使用", hour, min), -1, -1);
				else
					p.message(-1, MessageFormat.format("還需要{0}分鐘才能繼續使用", min), -1, -1);
			}
		}
	}
	
	protected int getDayOfYear(long times){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(times);
		return cal.get(Calendar.DAY_OF_YEAR);
	}
	
	protected long getMills(int hour){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTimeInMillis();
	}

}
