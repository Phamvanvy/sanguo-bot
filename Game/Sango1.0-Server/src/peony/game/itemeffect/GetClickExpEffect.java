package peony.game.itemeffect;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.db.SyncExecutorService;
import peony.decimoney.DecImoneyBuy;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.drop.GroupDrop;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.service.account.Account;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopException;
import peony.service.shop.ShopService;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.service.tong.TongSkill5;
import peony.service.tong.TongSkill6;

public class GetClickExpEffect implements ItemEffect, NoItemShopBuyI {
	
	public static long ONLINE_TIME = 2 * 1000L;
	public static Random rnd = new Random();
	public static long CD = 20 * 60 * 60 * 1000L;
	public static int FUXING_DEC_IMONEY = 36;
	public static int FUXING_ID = 1643;
	protected boolean buyOk;
	protected String failMessage;
	public long beginTime;
	private static final Logger log = Logger.getLogger(GetClickExpEffect.class);

	public boolean isAsync() {
		return true;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx_ext)
			throws UseItemException {
//		if(item.template.id==1643){
//			// 特殊处理福星赐福包使用逻辑
//			useItem(source, item, target, tx_ext);
//			return;
//		}
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
				throw new UseItemException(peony.Messages.STRING_01085);
			p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, System.currentTimeMillis());
			p.pool.setLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, 0);
			int r = rnd.nextInt(100);
			if(r == 1){
				GameItem jewel = ObjectAccessor.createGameItem(1310); //宝石袋
				PlayerTransaction tx = p.newTransaction("CEX");
				try {
					p.bag.addGameItemComplete(jewel, 1, tx, true);
					tx.commit();
					p.message(-1, MessageFormat.format(peony.Messages.STRING_01086, jewel.template.name), -1, -1);
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					int exp = p.level * 100;
					PlayerTransaction tx1 = p.newTransaction("CEX");
					p.addExp(exp, tx1, true);
					tx1.commit();
					p.message(-1, MessageFormat.format(peony.Messages.STRING_01087, exp), -1, -1);
				}
			}else{
				int exp = p.level * 300;
				PlayerTransaction tx = p.newTransaction("CEX");
				p.addExp(exp, tx, true);
				tx.commit();
				p.message(-1, MessageFormat.format(peony.Messages.STRING_01087, exp), -1, -1);
			}
			p.pool.setInt(Player.PROPERTY_CLICKEXP_TIMES, times+1);
		}else{
			long min = (ONLINE_TIME - totalOnlineTime) / (60 * 1000L);
			if(min==0) min = 1;
			p.message(-1, MessageFormat.format(peony.Messages.STRING_01088, min), -1, -1);
		}
	}
	
	public void useItem(Unit source, GameItem item, Unit target, int useIB, PlayerTransaction tx_ext ,ClientSession session)
				throws UseItemException {
		Player p = (Player)source;
		
		// 特殊处理福星赐福包
		
		long lastUseTime = p.pool.getLong(Player.PROPERYY_CLICKEXPSUC_TIME, 0);
		long t = p.pool.getLong(Player.PROPERTY_CLICKEXP_START_TIME, 0L);
		if(t==0){
			p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, System.currentTimeMillis());
			p.pool.setLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, 0);
			ErrorHandler.sendErrorMessage(session, OpCode.FUXING_BAG_CLIENT, OpCode.FUXING_BAG_CLIENT, MessageFormat.format(peony.Messages.STRING_01088, ONLINE_TIME/60000));
			throw new UseItemException("");
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
			if(times>12){
				ErrorHandler.sendErrorMessage(session, OpCode.FUXING_BAG_CLIENT, OpCode.FUXING_BAG_CLIENT, peony.Messages.STRING_01085);
				throw new UseItemException("");
			}
			p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, System.currentTimeMillis());
			p.pool.setLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, 0);
			int r = rnd.nextInt(100);
			if(r <= 1 && useIB != 1){
//				GameItem jewel = ObjectAccessor.createGameItem(1310); //宝石袋
//				PlayerTransaction tx = p.newTransaction("CEX");
//				try {
//					p.bag.addGameItemComplete(jewel, 1, tx, true);
//					tx.commit();
//					p.message(-1, MessageFormat.format("你获得了{0}", jewel.template.name), -1, -1);
//				} catch (NoEnoughSpaceException e) {
//					tx.rollback();
//					int exp = p.level * 100;
//					PlayerTransaction tx1 = p.newTransaction("CEX");
//					p.addExp(exp, tx1, true);
//					tx1.commit();
//					p.message(-1, MessageFormat.format("你获得了{0}在线经验", exp), -1, -1);
//				}
				Gain gain = new Gain(p);
				GroupDrop gd = ObjectAccessor.getGroupDrop(780);
				gd.calc(rnd, gain);
				for (GainItem gi : gain.getGainItems()) {
                	MailService mailService = Server.server.getServiceRegistry().getMailService();
                	mailService.sendSystemMail(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01089, "", 0, gi.getItem(), gi.getCount(), "CEX");
	            }
				p.message(-1, peony.Messages.STRING_01090, -1, -1);
			}else{
				synchronized (this) {
					int exp = p.level * 1000;
					//军团专属科技   福星高照
					TongService ts = Server.server.getServiceRegistry().getTongService();
					TongMember tm = ts.getPlayerInfo(p.id);
					if(tm!=null && tm.skills.get(6)!=null){
						TongSkill6 tskill = (TongSkill6)tm.skills.get(6);
						int ratios = tskill.getRatios();
						if(rnd.nextInt(100) >= (100 - ratios)){
							exp *= 2;
						}
					}
					PlayerTransaction tx = p.newTransaction("FUXINBAG");
					if(useIB == 1){
						ShopService service = Server.server.getServiceRegistry().getShopService();
						Account account = p.getAccount();
						long imoney = account.getLongIMoney();
						if(service.getItemPrice(NoItemShopBuy.YIYUANBAO) >imoney){
							tx.rollback();
							ErrorHandler.sendErrorMessage(session, OpCode.FUXING_BAG_CLIENT, OpCode.FUXING_BAG_CLIENT, peony.Messages.STRING_00554);
							throw new UseItemException("");
						}
						if(SyncExecutorService.async==0){
							DecImoneyBuy dib = new DecImoneyBuy(p,FUXING_DEC_IMONEY,"FUXINBAG",new int[]{FUXING_ID});
							try{
								service.buy(p, dib);
							}catch(Exception e){
								tx.rollback();
								ErrorHandler.sendErrorMessage(session, OpCode.FUXING_BAG_CLIENT, OpCode.FUXING_BAG_CLIENT, peony.Messages.STRING_00554);
								throw new UseItemException("");
							}
						}else{
							try {
								int shopId = service.getShopByItemId(NoItemShopBuy.YIYUANBAO).id;
								service.buy(p, new NoItemShopBuy(p,0,shopId,NoItemShopBuy.YIYUANBAO,1,this,null));
								beginTime = System.currentTimeMillis();
								while(true){
									if(buyOk)
										break;
									if(failMessage!=null && !failMessage.equals("")){
										String mess = failMessage;
										clear();
										tx.rollback();
										p.message(-1, mess, -1, -1);
										return;
									}
									if(System.currentTimeMillis()-beginTime>5000){
										clear();
										tx.rollback();
										p.message(-1, peony.Messages.STRING_01091, -1, -1);
										log.info("[DECIMONEYOUT]"+LogUtil.getPlayerLogString(p)+"PRICEITEM["+NoItemShopBuy.YIYUANBAO+"]");
										return;
									}
									try { Thread.sleep(10); } catch (InterruptedException e) {}
								}
								clear();
							} catch (ShopException e) {
								tx.rollback();
//								p.message(-1, peony.Messages.STRING_00405, -1, -1);
								return;
							}
						}
						exp *= 5;
					}
					p.addExp(exp, tx, true);
					tx.commit();
					p.message(-1, MessageFormat.format(peony.Messages.STRING_01087, exp), -1, -1);
				}
			}
			p.pool.setInt(Player.PROPERTY_CLICKEXP_TIMES, times+1);
			
			// 特殊处理福星赐福包
			p.pool.setLong(Player.PROPERYY_CLICKEXPSUC_TIME, System.currentTimeMillis());
		}else{
			if(lastUseTime==0 || (System.currentTimeMillis()-lastUseTime>=CD && totalOnlineTime<ONLINE_TIME)){
				long min = (ONLINE_TIME - totalOnlineTime) / (60 * 1000L);
				if(min==0) 
					min = 1;
				ErrorHandler.sendErrorMessage(session, OpCode.FUXING_BAG_CLIENT, OpCode.FUXING_BAG_CLIENT, MessageFormat.format(peony.Messages.STRING_01088, min));
				throw new UseItemException("");
			}else{
				long m1 = ((lastUseTime+CD)-System.currentTimeMillis()) / (60 * 1000L);
				long m2 = (ONLINE_TIME - totalOnlineTime) / (60 * 1000L);
				long min = Math.max(m1, m2);
				if(min==0) 
					min = 1;
				long hour = min/60;
				min = min%60;
				if(hour>0 && min==0){
					ErrorHandler.sendErrorMessage(session, OpCode.FUXING_BAG_CLIENT, OpCode.FUXING_BAG_CLIENT, MessageFormat.format(peony.Messages.STRING_01092, hour));
					throw new UseItemException("");
				}
				else if(hour>0){
					ErrorHandler.sendErrorMessage(session, OpCode.FUXING_BAG_CLIENT, OpCode.FUXING_BAG_CLIENT, MessageFormat.format(peony.Messages.STRING_01093, hour, min));
					throw new UseItemException("");
				}
				else{
					ErrorHandler.sendErrorMessage(session, OpCode.FUXING_BAG_CLIENT, OpCode.FUXING_BAG_CLIENT, MessageFormat.format(peony.Messages.STRING_01094, min));
					throw new UseItemException("");
				}
			}
		}
	}
	
	protected void clear(){
		buyOk = false;
		failMessage = null;
		beginTime = 0;
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

	public void process(Object[] o) {
		this.buyOk = true;
	}

	public void procssFail(Object[] o) {
		failMessage = peony.Messages.STRING_00405;
	}
	
	public boolean needRemove() {
		return false;
	}

}
