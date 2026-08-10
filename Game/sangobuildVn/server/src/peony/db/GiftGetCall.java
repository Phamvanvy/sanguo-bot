package peony.db;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.gift.GiftHistory;
import peony.game.gift.GiftService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.ShopException;

import com.pip.sanguo.data.GiftGroup;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.GiftGroup.CycleDef;
import com.pip.sanguo.data.GiftGroup.GiftDef;
import com.pip.sanguo.data.Shop.BuyRequirement;

public class GiftGetCall extends ClientSessionAsyncCall {

	private static final Logger log = Logger.getLogger(GiftGetCall.class);

	protected int playerId;
	protected int serial;
	protected int giftGroupId;
	protected int giftId;

	public GiftGetCall(ClientSession session, Packet pt, Player player) {
		super(session);
		this.playerId = player.id;
		this.serial = pt.getInt();
		this.giftGroupId = pt.getInt();
		this.giftId = pt.getInt();
		LogUtil.logGiftGetTry(player, giftGroupId, giftId);
	}

	public void callFinish() throws Exception {
		if(!success){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.GIFT_GET_CLIENT, errorMessage);
		}
	}

	public void run() {
		Player player = ObjectAccessor.getPlayer(playerId);
		if (player != null) {
			GiftService giftService = Server.server.getServiceRegistry()
					.getGiftService();
			synchronized (giftService) {
				Calendar now = Calendar.getInstance();
				GiftGroup g = Server.server.getServiceRegistry()
						.getGiftService().getGiftGroup(giftGroupId);
				if (g == null) {
					return;
				}

				GiftDef def = g.findGift(giftId);
				if (def == null) {
					return;
				}
				if (!g.isValid(new Date())) {
					error(null, g.translateText(g.timeErrorMessage, def, 0, 0));
					addToClientSession();
					return;
				}
				if (player.level < def.beginLevel
						|| player.level > def.endLevel) {
					error(null, g.translateText(g.errorMessage, def, 0, 0));
					addToClientSession();
					return;
				}
				DBService dbService = Server.server.getServiceRegistry()
						.getDbService();
				GiftHistory gh = dbService.giftDAO.getHistory(playerId,
						giftGroupId);
				if (gh == null) {
					gh = new GiftHistory();
					gh.allTimes = 0;
					gh.groupId = giftGroupId;
					gh.playerId = player.id;
					gh.lastTime = new Date();
					gh.repeatTimes = 0;
				}
				Calendar cycleStartTime = getCycleStartTime(g, now);
				if (cycleStartTime == null) {
					error(null, g.translateText(g.timeErrorMessage, def, 0, 0));
					addToClientSession();
					return;
				}
				int repeatTimes = gh.repeatTimes;
				if (gh.startTime == null
						|| gh.startTime.getTime() != cycleStartTime
								.getTimeInMillis()) {
					repeatTimes = 0;

				}
				int retCode = checkTimes(g, gh.allTimes, repeatTimes,
						gh.lastTime, now);
				if (retCode == 0) {
					gh.repeatTimes = 1;
					gh.allTimes++;
					gh.startTime = cycleStartTime.getTime();
					
					// 检查变量需求
					if (!checkVars(def.needItems, player)) {
					    error(null, g.translateText(g.needVarMessage, def, 0, 0));
                        addToClientSession();
                        return;
					}
					PlayerTransaction tx = player.newTransaction("GFT");
					try {
					    // 检查物品，金钱，战功需求
						checkRequirements(def.needItems, player, tx);
						Gain gain = getGain(def.giveItems, player);
						player.addGainComplete(gain, tx, true);
						dbService.giftDAO.makePersistent(gh);
						tx.commit();
						LogUtil.logGiftGetOK(player, giftGroupId, giftId);
						
						Packet pt = new Packet(OpCode.GIFT_GET_SERVER);
						pt.putInt(serial);
						pt.putString(g.translateText(g.giveOKMessage, def, 0, 0));
						player.send(pt);
					} catch (ShopException e) {
						tx.rollback();
						error(null, g.translateText(g.needItemMessage, def, 0,
								0));
						addToClientSession();
						return;
					} catch (NoEnoughSpaceException e) {
						tx.rollback();
						error(null, g.translateText(g.bagFullMessage, def, 0, 0));
						addToClientSession();
						return;
					} catch (Exception e) {
						tx.rollback();
						log.error(e, e);
					}
				} else if (retCode == 1) {
					error(null, g.translateText(g.maxExceedMessage, def,
							gh.allTimes, gh.repeatTimes));
					addToClientSession();
					return;
				} else if (retCode == 2) {
					error(null, g.translateText(g.repeatExceedMessage, def,
							gh.allTimes, gh.repeatTimes));
					addToClientSession();
					return;
				} else if (retCode == 3) {
					error(null, g.translateText(g.timeSpaceMessage, def,
							gh.allTimes, gh.repeatTimes));
					addToClientSession();
					return;
				}
			}
		}
	}
	
	/*
	 * 检查变量需求是否满足。如果任何一个条件不满足，返回false。
	 */
	protected boolean checkVars(List<BuyRequirement> l, Player p) {
	    for (BuyRequirement req : l) {
	        if (req.type == Shop.TYPE_VARIABLE) {
	            if (p.asmVm.getGlobalValue(req.varName) < req.amount) {
	                return false;
	            }
	        }
	    }
	    return true;
	}

	protected void checkRequirements(List<BuyRequirement> l, Player p,
			PlayerTransaction tx) throws ShopException {
		for (BuyRequirement req : l) {
			switch (req.type) {
			case Shop.TYPE_HONOR: {
				try {
					p.decCredit(req.amount, tx, false);
				} catch (NoEnoughValueException e) {
					throw new ShopException("");
				}
				break;
			}
			case Shop.TYPE_ITEM: {
				ItemTemplate it = ObjectAccessor.getItemTemplate(req.item.id);
				if (it == null) {
					throw new ShopException("");
				}
				if (p.bag.removeGameItem(req.item.id, -1, req.amount, tx, true) != null) {
					if (req.amount == 1) {
						throw new ShopException("");
					} else {
						throw new ShopException("");
					}
				}
				break;
			}
			case Shop.TYPE_MONEY:
				try {
					p.decMoney(req.amount, tx, false);
				} catch (NoEnoughValueException e) {
					throw new ShopException("");
				}
				break;
			case Shop.TYPE_RANK:
				// 不可能走到这一步
				break;
			case Shop.TYPE_VARIABLE:
			    // 这里忽略
			    break;
			}
		}
	}

	protected Gain getGain(List<BuyRequirement> l, Player p) {
		Gain g = new Gain(p);
		for (BuyRequirement req : l) {
			switch (req.type) {
			case Shop.TYPE_HONOR: {
				g.addCredit(req.amount);
				break;
			}
			case Shop.TYPE_ITEM: {
				ItemTemplate it = ObjectAccessor.getItemTemplate(req.item.id);
				if (it != null)
					g.addGainItem(ObjectAccessor.createGameItem(req.item.id),
							req.amount);
				break;
			}
			case Shop.TYPE_MONEY:
				g.addMoney(req.amount);
				break;
			}
		}
		return g;
	}

	/**
	 * 检查领取次数以及间隔
	 * 
	 * @param g
	 * @param allTimes
	 * @param repeatTimes
	 * @param lastTime
	 *            最后一次领取时间，可以为null，表示没有领取过
	 * @param now
	 *            当前时间
	 * @return
	 */
	public int checkTimes(GiftGroup g, int allTimes, int repeatTimes,
			Date lastTime, Calendar now) {
		if (g.maxTimes!=-1&&g.maxTimes <= allTimes)
			return 1;
		if (g.repeatTimes!=-1&&g.repeatTimes <= repeatTimes)
			return 2;
		if (g.timeSpace != -1 && lastTime != null) {
			if (g.timeSpace > (now.getTimeInMillis() - lastTime.getTime()) / 1000L) {
				return 3;
			}
		}
		return 0;
	}

	/**
	 * 获取每个周期开始时间，如果传入的time不符合领取时间，那么返回null
	 * 
	 * @return
	 */
	public static Calendar getCycleStartTime(GiftGroup g, Calendar time) {
		if (g.cycle.type == CycleDef.CYCLE_DAY) {
			if (g.cycle.amount == 1) {
				if (inTimeRange(g.beginTime, g.endTime, time))
					return DateUtils.truncate(time, Calendar.DAY_OF_MONTH);
				else
					return null;
			} else {
				if (inTimeRange(g.beginTime, g.endTime, time)) {
					long c = (time.getTimeInMillis() - g.beginDate.getTime())
							/ (24 * 3600 * 1000L * g.cycle.amount);
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(g.beginDate.getTime() + c * 24 * 3600
							* 1000L * g.cycle.amount);
					return cal;
				} else {
					return null;
				}
			}
		} else if (g.cycle.type == CycleDef.CYCLE_HOUR) {
			if (inTimeRange(g.beginTime, g.endTime, time)) {
				long c = (time.getTimeInMillis() - g.beginDate.getTime())
						/ (3600 * 1000L * g.cycle.amount);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(g.beginDate.getTime() + c * 3600 * 1000L
						* g.cycle.amount);
				return cal;
			} else {
				return null;
			}
		} else if (g.cycle.type == CycleDef.CYCLE_MONTH) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(g.beginDate);
			int c = getMonthBetweenDates(cal, time) / g.cycle.amount;
			cal.add(Calendar.MONTH, c * g.cycle.amount);
			if (inTimeAndDayRange(g.beginDay, g.endDay, g.beginTime, g.endTime,
					cal, time)) {
				return cal;
			} else {
				return null;
			}
		} else if (g.cycle.type == CycleDef.CYCLE_WEEK) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(g.beginDate);
			int c = getWeekBetweenDates(cal, time) / g.cycle.amount;
			cal.add(Calendar.WEEK_OF_YEAR, c * g.cycle.amount);
			if (inTimeAndDayRange(g.beginDay, g.endDay, g.beginTime, g.endTime,
					cal, time)) {
				return cal;
			} else {
				return null;
			}
		}
		return null;
	}

	public static int getWeekBetweenDates(Calendar d1, Calendar d2) {
		return (int) ((d2.getTimeInMillis() - d2.getTimeInMillis()) / (7 * 24 * 3600 * 1000L));
	}

	public static int getMonthBetweenDates(Calendar d1, Calendar d2) {
		return d2.get(Calendar.YEAR) * 12 + d2.get(Calendar.MONTH)
				- d1.get(Calendar.YEAR) * 12 + d1.get(Calendar.MONTH);
	}

	public static boolean inTimeRange(int start, int end, Calendar time) {
		if (end == -1) {
			end = Integer.MAX_VALUE;
		}
		int min = time.get(Calendar.HOUR_OF_DAY)*60+time.get(Calendar.MINUTE);
		return min >= start && min < end;
	}

	public static boolean inTimeAndDayRange(int startDay, int endDay,
			int startTime, int endTime, Calendar cycleStartTime, Calendar time) {
		if (!inTimeRange(startTime, endTime, time))
			return false;
		long d = (time.getTimeInMillis() - cycleStartTime.getTimeInMillis())
				/ (24 * 3600 * 1000L);
		if (endDay == -1)
			endDay = Integer.MAX_VALUE;
		return d >= startDay && d <= endDay;
	}

}
