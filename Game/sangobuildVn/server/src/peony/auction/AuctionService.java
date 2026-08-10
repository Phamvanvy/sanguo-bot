package peony.auction;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.mail.MailService;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.player.ActorCacheService;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;

public class AuctionService implements Service, Runnable {
	private static final Logger log = Logger.getLogger(AuctionService.class);
	// 存放Auction的缓存，过期时间早的排在前面。
	//public static TreeSet<Auction> cache = new TreeSet<Auction>(new AuctionComparator());
	public List<Auction> cache = new ArrayList<Auction>();
	// 缓存玩家参加竞拍的记录
	protected Map record = new TreeMap();
	private Packet packet = new Packet(OpCode.MAIL_NEW_SERVER); // 新邮件通知
	protected boolean active = false;
	protected int serial;
	protected int auctionId;
	protected int itemID;
	protected int price;
	protected int playerId;
	int[] monLevel = {100,10000,1000000,100000000};
    int[] cnt = {1,10,100};
	protected String[] aucBuyCnt = {"Lần đầu đào bảo","Thu được pha phong","Mãn tải mà về"};
	protected String[] aucSellCnt = {"Sơ thiệp thương đạo","Buôn bán tiểu thành","Nhật tiến đẩu kim"};
	protected String[] aucBuyMon = {"花费过百","Nhất trịch thiên kim","Chỉ túy kim mê","Huy kim như thổ"};
	protected String[] aucSellMon = {"Xuất được tiểu lợi","Sinh tài hữu đạo","Bách vạn thân gia","Phúc giáp thiên hạ"};
	

	public AuctionService() {

	}
	
	private AuctionDAO getAuctionDAO() {
		return Server.server.getServiceRegistry().getDbService().auctionDAO;
	}
	
	private MailService getMailService() {
		return Server.server.getServiceRegistry().getMailService();
	}
	
	/**
	 * 创建一个Auction,并将其放入缓存
	 */
	public int createAuction(Player p, int startPrice, int endPrice,GameItem item, int count, PlayerTransaction tx1)
			throws AuctionException {
		synchronized (this) {
//			if(item.template.bindType != -1){
//				tx1.rollback();
//				throw new AuctionException("物品已绑定，不能拍卖");
//			}
			if (item.isBound()) {
				tx1.rollback();
				throw new AuctionException("Vật phẩm đã khóa, không thể đấu giá");
			}
			
			// 记录日志
			LogUtil.logAuctionCreateTry(p, startPrice, endPrice, item, count);
			
			int fees;
			long currentTime = System.currentTimeMillis();
			Auction auction = new Auction();
			auction.setCreateTime(new Date(currentTime));
			auction.setPlayerId(p.id);
			auction.setValidTime(new Date(currentTime + 1000 * 60 * 60 * 8));
			auction.setCount(count);
			auction.setPlayerName(p.name);
			if (startPrice == 0 && endPrice == 0) {
				// 确认物品价格是否存在0的情况,若物品价格为0，则起始价格也为0，系统不扣除其发起拍卖的手续费
				// 如果拍卖者没有设置起拍价，系统自动设置起拍价为物品价格的150%
//				auction.setStartPrice((int) (item.template.price * count * 1.5));
//				auction.setCurrentPrice((int) (item.template.price * count * 1.5));
				auction.setStartPrice(0);
				auction.setCurrentPrice(0);
			} else if (startPrice != 0 && endPrice == 0) {
				// 创建拍卖行时设置当前价格为起拍价格
				auction.setStartPrice(startPrice);
				auction.setCurrentPrice(startPrice);
			}
			PlayerTransaction tx = tx1;
			try {
				// 扣除拍卖者发布拍卖的手续费用,手续费为物品本身价格标价的8%。如果不足整数，如何取整，如果为0
				p.decMoney((int) (item.template.price * auction.getCount() * 0.08), tx, false);
				fees = (int) (item.template.price * auction.getCount() * 0.08);
				if (endPrice != 0 && startPrice == 0) {
					auction.setStartPrice(0);
					auction.setEndPrice(endPrice);
				} else if (endPrice != 0 && startPrice != 0) {
					auction.setStartPrice(startPrice);
					auction.setCurrentPrice(startPrice);
					auction.setEndPrice(endPrice);
				}
				auction.setItem(item);
				if (item.template.isEquipment()) {
					auction.setType(item.template.equipment.type);
				} else {
					// 3为普通物品类型
					auction.setType(3);
				}
				auction.setQuality(item.template.quality);
				auction.setLevel(item.template.useLevel);
				auction.setName(item.template.name);
				auction.setFaction(p.faction);
				tx.commit();
				getAuctionDAO().newEntity(auction);
//				Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PLAYER_AUCTIONCREATE,p));
				// 将创建好的auction放入TreeSet进行缓存
				cache.add(auction);
				ObjectAccessor.addGameItemToCached(auction.getItem());
				
				// 记录成功日志
				LogUtil.logAuctionCreateOK(p, item, count, auction);
				return fees;
			} catch (NoEnoughValueException e) {
				tx.rollback();
				throw new AuctionException("Số dư không đủ");
			} catch (Exception e) {
				tx.rollback();
				log.error(e, e);
				if (e instanceof AuctionException)
					throw (AuctionException) e;
				throw new AuctionException("Thông báo thông tin đấu giá thất bại");
			}
		}
	}

	/**
	 * 从缓存中根据auction的id取得Auction
	 */
	public Auction getFromCache(int id) {
		synchronized (this) {
			for (Auction auction : cache) {
				if (auction.getId() == id) {
					return auction;
				}
			}
		}
		return null;
	}

	/**
	 * 删除Auction,同时清理缓存
	 */
	public boolean deleteAuction(Auction auction) {
		synchronized (this) {
			LogUtil.logDeleteAuctionTry(auction);
			try {
				cache.remove(auction);
				GameItem item = auction.getItem();
				if(item.instanceId!=-1){
					ObjectAccessor.removeGameItemFromCache(item);
				}
			} catch (Exception e) {
				log.error(e, e);
			}
			try {
				getAuctionDAO().makeTransient(auction);
			} catch (Exception e) {
				log.error(e, e);
			}
			// 拍卖成功后删除对应的玩家最近参加的竞拍信息
			Set<Integer> keys = record.keySet();
			Set set = null;
			if (keys != null) {
				for (int key : keys) {
					Set set1 = ((Set) record.get(key));
					if (set1 != null) {
						Iterator ite = set1.iterator();
						while(ite.hasNext()){
							Auction auction2 = (Auction)ite.next();
							if(auction2==auction){
								ite.remove();
							}
						}
//						for (Object auction2 : set1) {
//							if ((Auction) auction2 == auction) {
//								set = set1;
//								set.remove(auction);
//							}
//						}
					}
				}
			}
			// if(set != null){
			// set.remove(auction);
			// }
			LogUtil.logDeleteAuctionOK(auction);
			return true;
		}
	}

	/**
	 * 拍卖行物品交易
	 */
	public void buy(int auctionId, int price, Player p, GameItem item, int count) throws AuctionException {
		LogUtil.logAuctionBuyTry(auctionId, price, p, item, count);
		synchronized (this) {
			if (getFromCache(auctionId) != null && p.id != getFromCache(auctionId).getPlayerId()) {
				// 缓存玩家的竞拍记录
				int a = 0;
				Set<Integer> keys = record.keySet();
				if(keys != null){
					for (int key : keys) {
						if (key == p.id) {
							((Set) record.get(key)).add(getFromCache(auctionId));
							a++;
							break;
						}
					}
					if (a == 0) {
						Set l = new HashSet<Auction>();
						l.add(getFromCache(auctionId));
						record.put(p.id, l);
					}
				}
				this.auctionId = auctionId;
				this.price = price;
				Auction auction = getFromCache(auctionId);
				// 判断auction是否存在，auction可能因过期而被删除掉
				if (auction == null) {
					throw new AuctionException("Đấu giá này không tồn tại hoặc đã hủy bỏ");
				}
				
				int endPrice = auction.getEndPrice();
				int startPrice = auction.getStartPrice();
				PlayerTransaction tx = p.newTransaction("AUC");
				// 如果拍卖行的一口价不为0，则进行一口价交易
				if (endPrice != 0) {
					if (price >= endPrice) {
						try {
							// 返还上次成功出价的金钱
							if(auction.getLastPlayerId() != -1){
								getMailService().sendSystemMail(auction.getLastPlayerId(), "Bán đấu giá nhà", MessageFormat.format("Canh tranh {0} cái {1} thất bại, trả về giá đưa ra", auction.getCount(),auction.getName()),
										"Giá bạn ra quá thấp, quay lại giá ra lần trước", auction.getCurrentPrice(), null, 0, "AUCFAIL");
								
								if(ObjectAccessor.getPlayer(auction.getLastPlayerId()) != null){
									ObjectAccessor.getPlayer(auction.getLastPlayerId()).send(packet);
								}
							}
							// 扣除竟拍者的游戏币并通过飞鸽发送给拍卖者
							p.decMoney(auction.getEndPrice(), tx, false);
							tx.commit();
							getMailService().sendSystemMail(p.id, "Bán đấu giá nhà", MessageFormat.format("Đấu giá {0}cái{1}thành công", auction.getCount(),auction.getName()), MessageFormat.format("Cuộc đấu giá {0} trả giá thành công, đạt được món đồ ", auction.getName()), 0, item, auction.getCount(), "AUCBUY");
							getMailService().sendSystemMail(auction.getPlayerId(),"Bán đấu giá nhà", MessageFormat.format("Bán đấu giá thành công {0}  {1} cái, nhận được vàng", auction.getCount(),auction.getName()),
									"Bán vật phẩm, nhận được vàng", auction.getEndPrice(), null, 0, "AUCSELL");
							LogUtil.logAuctionBuy(p, auction, price);
							auction.setCurrentPrice(auction.getEndPrice());
							LogUtil.logAuctionSucc(p.id, auction);
							if(ObjectAccessor.getPlayer(auction.getPlayerId()) != null){
								ObjectAccessor.getPlayer(auction.getPlayerId()).send(packet);
							}
//							auction.setCurrentPrice(-1); // 
							// 拍卖成功，删除拍卖信息
							deleteAuction(auction);
							
							// 玩家竞拍得到物品后，物品将通过飞鸽发给购买者
							if(p != null){
								p.send(packet);
							}
							// 统计个人成就
							try {
								StatService service = Server.server.getServiceRegistry().getStatService();
								Player createPlayer = ObjectAccessor.getPlayer(auction.getPlayerId());
								int faction;
								if(createPlayer!=null){
									faction = createPlayer.faction;
								} else {
									ActorCacheService cacheService = Server.server.getServiceRegistry().getActorCacheService();
								    faction = cacheService.find(auction.getPlayerId()).faction;
								}
								PvpInfo pvpInfo=service.getPvpInfo(auction.getPlayerId(),faction); 
								pvpInfo.pool.setInt(StatService.PROPERTY_AUCSELL_COUNT, pvpInfo.pool.getInt(StatService.PROPERTY_AUCSELL_COUNT,0)+auction.getCount());
								pvpInfo.pool.setInt(StatService.PROPERTY_AUCSELLMONEY_COUNT, pvpInfo.pool.getInt(StatService.PROPERTY_AUCSELLMONEY_COUNT, 0)+auction.getEndPrice());
								for(int i = 0;i<cnt.length;i++){
									if(i == 0 && pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_AUCTION)!= "" 
										&& pvpInfo.pool.getString(service.getPropertyOfAuction(cnt[i], 0, true))!=pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_AUCTION)){
										pvpInfo.pool.setString(service.getPropertyOfAuction(cnt[i], 0, true),pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_AUCTION));
										pvpInfo.pool.setInt(StatService.PROPERTY_AUCSELL_COUNT, pvpInfo.pool.getInt(StatService.PROPERTY_AUCSELL_COUNT,0)+1);
									}
									if(pvpInfo.pool.getInt(StatService.PROPERTY_AUCSELL_COUNT,0)>=cnt[i] && pvpInfo.pool.getString(service.getPropertyOfAuction(cnt[i], 0, true))==""){
										pvpInfo.pool.setString(service.getPropertyOfAuction(cnt[i], 0, true),service.getFinishTime(System.currentTimeMillis()));
										sendMessage(createPlayer,cnt[i],0,true);
									}
									if(pvpInfo.pool.getString(service.getPropertyOfAuction(cnt[i], 0, true))=="")
								    	break;
									}
								for(int i=0;i<monLevel.length;i++){
								    if(pvpInfo.pool.getInt(StatService.PROPERTY_AUCSELLMONEY_COUNT, 0)>=monLevel[i] && pvpInfo.pool.getString(service.getPropertyOfAuction(0, monLevel[i], true))==""){
								    	pvpInfo.pool.setString(service.getPropertyOfAuction(0, monLevel[i], true), service.getFinishTime(System.currentTimeMillis()));
								    	  sendMessage(createPlayer,0,monLevel[i],true);
								    }
								    if(pvpInfo.pool.getString(service.getPropertyOfAuction(0, monLevel[i], true))=="")
								    	break;
								    }
					          if(p!=null){
					        	  PvpInfo pvpInfo2 = service.getPvpInfo(p.id, p.faction);
					        	  pvpInfo2.pool.setInt(StatService.PROPERTY_AUCBUY_COUNT, pvpInfo2.pool.getInt(StatService.PROPERTY_AUCBUY_COUNT,0)+auction.getCount());
								  pvpInfo2.pool.setInt(StatService.PROPERTY_AUCBUYMONEY_COUNT, pvpInfo2.pool.getInt(StatService.PROPERTY_AUCBUYMONEY_COUNT, 0)+auction.getEndPrice());
								  for(int i = 0;i<cnt.length;i++){
										if(pvpInfo2.pool.getInt(StatService.PROPERTY_AUCBUY_COUNT,0)>=cnt[i] && pvpInfo2.pool.getString(service.getPropertyOfAuction(cnt[i], 0, false))==""){
											pvpInfo2.pool.setString(service.getPropertyOfAuction(cnt[i], 0, false),service.getFinishTime(System.currentTimeMillis()));
											sendMessage(p,cnt[i],0,false);
										}
										if(pvpInfo2.pool.getString(service.getPropertyOfAuction(cnt[i], 0, false))=="")
								    		break;
									}
								  for(int i=0;i<monLevel.length;i++){
								    if(pvpInfo2.pool.getInt(StatService.PROPERTY_AUCBUYMONEY_COUNT, 0)>=monLevel[i] && pvpInfo2.pool.getString(service.getPropertyOfAuction(0, monLevel[i], false))==""){
								    	pvpInfo2.pool.setString(service.getPropertyOfAuction(0, monLevel[i], false), service.getFinishTime(System.currentTimeMillis()));
								        sendMessage(p,0,monLevel[i],false);
								    }
								    if(pvpInfo2.pool.getString(service.getPropertyOfAuction(0, monLevel[i], false))=="")
								    	break;
								    }
					          }
							} catch (Exception e) {
								log.error(e, e);
							}
						} catch (NoEnoughValueException e) {
							tx.rollback();
						}
					} else if (price < endPrice && price > auction.getCurrentPrice()) {
						try {
							// 暂扣玩家出价
							p.decMoney(price, tx, false);
							tx.commit();
							LogUtil.logAuctionBuy(p, auction, price);
							// 返回上次成功出价玩家的出价
							if (auction.getLastPlayerId() != -1) {
								getMailService().sendSystemMail(auction.getLastPlayerId(), "Bán đấu giá nhà", MessageFormat.format("Canh tranh {0} cái {1} thất bại, trả về giá đưa ra", auction.getCount(),auction.getName()),
										"Giá bạn ra quá thấp, quay lại giá ra lần trước", auction.getCurrentPrice(), null, 0, "AUCFAIL");
								if(ObjectAccessor.getPlayer(auction.getLastPlayerId()) != null){
									ObjectAccessor.getPlayer(auction.getLastPlayerId()).send(packet);
								}
							}
							auction.setCurrentPrice(price);
							auction.setLastPlayerId(p.id);
						} catch (NoEnoughValueException e) {
							tx.rollback();
							throw new AuctionException("Số dư không đủ");
						}
						getAuctionDAO().updateEntity(auction);
					} else {
						// 如果玩家的出价小于当前价
						throw new AuctionException("Bạn trả giá quá thấp, đấu giá không thành công");
					}
				} else {
					// 否则进行竞价交易
					int currentPrice = auction.getCurrentPrice();
					if (price > currentPrice) {
						int lastPlayerId = auction.getLastPlayerId();
						// 如果之前已经有人出价，通过飞鸽将出价返还购买者
						if (auction.getLastPlayerId() != -1) {
							try {
								p.decMoney(price, tx, false);
								tx.commit();
								LogUtil.logAuctionBuy(p, auction, price);
							} catch (NoEnoughValueException e) {
								tx.rollback();
								throw new AuctionException("Số dư không đủ");
							}
							getMailService().sendSystemMail(lastPlayerId, "Bán đấu giá nhà",MessageFormat.format("đầu giá {0}cái{1}thất bại,trở lại giá như lần trước", auction.getCount(),auction.getName()), "Giá cả của người chơi khác đã cao hơn bạn", currentPrice, null,0, "AUCFAIL");
							if(ObjectAccessor.getPlayer(auction.getLastPlayerId()) != null){
								ObjectAccessor.getPlayer(auction.getLastPlayerId()).send(packet);
							}
							auction.setCurrentPrice(price);
							auction.setLastPlayerId(p.id);
							getAuctionDAO().updateEntity(auction);
						} else {
							try {
								p.decMoney(price, tx, false);
								tx.commit();
								LogUtil.logAuctionBuy(p, auction, price);
							} catch (NoEnoughValueException e) {
								tx.rollback();
								throw new AuctionException("Số dư không đủ");
							}
							auction.setCurrentPrice(price);
							auction.setLastPlayerId(p.id);
							getAuctionDAO().updateEntity(auction);
						}
					} else {
						// 如果出价小于当前出价
						throw new AuctionException("Trả giá quá thấp");
					}
				}
			} else if (getFromCache(auctionId) == null) {
				throw new AuctionException("Đấu giá này không tồn tại hoặc đã hủy bỏ");
			} else {
				throw new AuctionException("Không được đấu giá vật phẩm đấu giá của bản thân");
			}
		}
	}
	
	public void sendMessage(Player p,int cnt,int money,boolean sell){
	  if(p!=null){
		  TongService ser = Server.server.getServiceRegistry().getTongService();
		  TongMember tm = ser.getPlayerInfo(p.id);
		  String nameCnt = "";
		  String nameMon = "";
		  if(sell){
			  if(getCnt(cnt)!=-1)
			     nameCnt = aucSellCnt[getCnt(cnt)];
			  if(getMon(money)!=-1)
			     nameMon = aucSellMon[getMon(money)];
		  } else {
			  if(getCnt(cnt)!=-1)
			     nameCnt = aucBuyCnt[getCnt(cnt)];
			  if(getMon(money)!=-1)
			     nameMon = aucBuyMon[getMon(money)];
		  }
		  if(money == 0){
			 String msg2 = MessageFormat.format("Chúc mừng anh hùng <cff0000>{0}</c> đạt được thành tựu <cff0000>{1}</c>", p.name,nameCnt);
			 if(cnt == 100){
				 if(tm!=null){
					Server.server.getServiceRegistry().getChatService()
					.sendGuildSystemMessage(msg2,tm.tongID);
				}
			    Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg2);
			 } else{
				Server.server.getServiceRegistry().getChatService().sendAreaSystemMessage(msg2,p.map.id);
			 }
		  }
		  if(cnt == 0){
			  String msg2 = MessageFormat.format("Chúc mừng anh hùng <cff0000>{0}</c> đạt được thành tựu <cff0000>{1}</c>",p.name,nameMon);
			  if(money == 100000000){
				  Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg2);
				  if(tm!=null){
					 Server.server.getServiceRegistry().getChatService()
					   .sendGuildSystemMessage(msg2,tm.tongID);
				  }
			  }else {
				  Server.server.getServiceRegistry().getChatService().sendAreaSystemMessage(msg2,p.map.id);
			  }
		}
	  }
	}

	public void shutdown() {
		active = false;
	}

	public void startup() throws Exception {
		active = true;
		synchronized (this) {
			List<Auction> auctions = getAuctionDAO().getAuctions();
			if (auctions != null) {
				for (Auction auction : auctions) {
					cache.add(auction);
					ObjectAccessor.addGameItemToCached(auction.getItem());
				}
			}
		}
		if (!Server.containsOption("hack")) {
			new Thread(this).start();
		}
	}

	public void run() {
		while (active) {
			try {
				Thread.sleep(60 * 1000);
			} catch (InterruptedException ex) {
			}
			try {
				checkTimeout();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 判断拍卖行是否过期
	 */
	private void checkTimeout() {
		// cache中遍历
		List<Auction> timeOutAuctions = getTimeOutAuctions();
		if(timeOutAuctions != null){
			for (Auction auction : timeOutAuctions) {
				// 得到的过期拍卖行交给timeoutHandle()去处理
				timeoutHandle(auction);
			}
		}
	}

	protected boolean isTimeOut(Auction auction) {
		return System.currentTimeMillis() >= auction.getValidTime().getTime();
	}

	/**
	 * 取得过期的拍卖行列表
	 */
	protected List<Auction> getTimeOutAuctions() {
		synchronized(this){
			// 从cache中得到过期拍卖行
			List<Auction> timeOutAuctions = new ArrayList<Auction>();
			if(cache.size() != 0 ){
				for (Auction auction : cache) {
					if (isTimeOut(auction)) {
						timeOutAuctions.add(auction);
					}
				}
			}
			return timeOutAuctions;
		}
	}

	/**
	 * 拍卖过程到期处理
	 */
	private void timeoutHandle(Auction auction) {
		synchronized (this) {
			Player lastPlayer = ObjectAccessor.getPlayer(auction.getLastPlayerId());
			GameItem item = auction.getItem();
			Player player = ObjectAccessor.getPlayer(auction.getPlayerId());
			// 如果没有人出价，物品返还给拍卖者
			if(auction.getLastPlayerId() == -1){
				LogUtil.logAuctionFail(auction);
				getMailService().sendSystemMail(auction.getPlayerId(), "Bán đấu giá nhà", MessageFormat.format("Đấu giá thất bại, hoàn trả {0}cái{1}", auction.getCount(),auction.getName()),"Không có người chơi cạnh tranh", 0, item, auction.getCount(), "AUCTOUT");
				if(ObjectAccessor.getPlayer(auction.getPlayerId()) != null){
					ObjectAccessor.getPlayer(auction.getPlayerId()).send(packet);
				}
			} else {
				LogUtil.logAuctionSucc(auction.getLastPlayerId(), auction);
				getMailService().sendSystemMail(auction.getLastPlayerId(), "Bán đấu giá nhà",MessageFormat.format("Trả giá thành công {0} {1} cái ", auction.getCount(),auction.getName()), "竞价成功，得到物品", 0, item, auction.getCount(), "AUCBUY");
				if(ObjectAccessor.getPlayer(auction.getLastPlayerId()) != null){
					ObjectAccessor.getPlayer(auction.getLastPlayerId()).send(packet);
				}
				getMailService().sendSystemMail(auction.getPlayerId(), "Bán đấu giá nhà", MessageFormat.format("Đấu giá {0}cái{1}thành công, giành được tiền vàng", auction.getCount(),auction.getName()), MessageFormat.format("{0}Đấu giá thành công,  nhận được vàng", auction.getName()), auction.getCurrentPrice(), null, 0, "AUCSELL");
				if(ObjectAccessor.getPlayer(auction.getPlayerId()) != null){
					ObjectAccessor.getPlayer(auction.getPlayerId()).send(packet);
				}
				// 统计个人成就
				try {
					StatService service = Server.server.getServiceRegistry().getStatService();
					Player createPlayer = ObjectAccessor.getPlayer(auction.getPlayerId());
				    int faction1,faction2;
					if(createPlayer!=null){
						faction1 = createPlayer.faction;
					} else {
						ActorCacheService cacheService = Server.server.getServiceRegistry().getActorCacheService();
						faction1 = cacheService.find(auction.getPlayerId()).faction;
					}
						PvpInfo pvpInfo=service.getPvpInfo(auction.getPlayerId(),faction1); 
						pvpInfo.pool.setInt(StatService.PROPERTY_AUCSELL_COUNT, pvpInfo.pool.getInt(StatService.PROPERTY_AUCSELL_COUNT,0)+auction.getCount());
						pvpInfo.pool.setInt(StatService.PROPERTY_AUCSELLMONEY_COUNT, pvpInfo.pool.getInt(StatService.PROPERTY_AUCSELLMONEY_COUNT, 0)+auction.getEndPrice());
						for(int i = 0;i<cnt.length;i++){
							if(i == 0 && pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_AUCTION)!= ""
								&& pvpInfo.pool.getString(service.getPropertyOfAuction(cnt[i], 0, true))!=pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_AUCTION)){
								pvpInfo.pool.setString(service.getPropertyOfAuction(cnt[i], 0, true),pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_AUCTION));
								pvpInfo.pool.setInt(StatService.PROPERTY_AUCSELL_COUNT, pvpInfo.pool.getInt(StatService.PROPERTY_AUCSELL_COUNT,0)+1);
								
							}
							if(pvpInfo.pool.getInt(StatService.PROPERTY_AUCSELL_COUNT,0)>=cnt[i] && pvpInfo.pool.getString(service.getPropertyOfAuction(cnt[i], 0, true))==""){
								pvpInfo.pool.setString(service.getPropertyOfAuction(cnt[i], 0, true),service.getFinishTime(System.currentTimeMillis()));
								sendMessage(createPlayer,cnt[i],0,true);
							}
							if(pvpInfo.pool.getString(service.getPropertyOfAuction(cnt[i], 0, true))=="")
					    		break;
						}
					    for(int i=0;i<monLevel.length;i++){
					    	if(pvpInfo.pool.getInt(StatService.PROPERTY_AUCSELLMONEY_COUNT, 0)>=monLevel[i] && pvpInfo.pool.getString(service.getPropertyOfAuction(0, monLevel[i], true))==""){
					    		pvpInfo.pool.setString(service.getPropertyOfAuction(0, monLevel[i], true), service.getFinishTime(System.currentTimeMillis()));
					    		sendMessage(createPlayer,0,monLevel[i],true);
					    	}
					    	if(pvpInfo.pool.getString(service.getPropertyOfAuction(0, monLevel[i], true))=="")
					    		break;
					    }
					if(lastPlayer!=null){
						faction2 = lastPlayer.faction;
					} else {
						ActorCacheService cacheService = Server.server.getServiceRegistry().getActorCacheService();
						faction2 = cacheService.find(auction.getLastPlayerId()).faction;
					}
						PvpInfo pvpInfo2 = service.getPvpInfo(auction.getLastPlayerId(), faction2);
						pvpInfo2.pool.setInt(StatService.PROPERTY_AUCBUY_COUNT, pvpInfo2.pool.getInt(StatService.PROPERTY_AUCBUY_COUNT,0)+auction.getCount());
						pvpInfo2.pool.setInt(StatService.PROPERTY_AUCBUYMONEY_COUNT, pvpInfo2.pool.getInt(StatService.PROPERTY_AUCBUYMONEY_COUNT, 0)+auction.getEndPrice());
						for(int i = 0;i<cnt.length;i++){
						  if(pvpInfo2.pool.getInt(StatService.PROPERTY_AUCBUY_COUNT,0)>=cnt[i] && pvpInfo2.pool.getString(service.getPropertyOfAuction(cnt[i], 0, false))==""){
							pvpInfo2.pool.setString(service.getPropertyOfAuction(cnt[i], 0, false),service.getFinishTime(System.currentTimeMillis()));
							sendMessage(lastPlayer,cnt[i],0,false);
						  }
						    if(pvpInfo2.pool.getString(service.getPropertyOfAuction(cnt[i], 0, false))=="")
						      break;
						}
						for(int i=0;i<monLevel.length;i++){
						  if(pvpInfo2.pool.getInt(StatService.PROPERTY_AUCBUYMONEY_COUNT, 0)>=monLevel[i] && pvpInfo2.pool.getString(service.getPropertyOfAuction(0, monLevel[i], false))==""){
						    pvpInfo2.pool.setString(service.getPropertyOfAuction(0, monLevel[i], false), service.getFinishTime(System.currentTimeMillis()));
						    sendMessage(lastPlayer,0,monLevel[i],false);
						  }
						    if(pvpInfo2.pool.getString(service.getPropertyOfAuction(0, monLevel[i], false))=="")
						       break;
						}
				} catch (Exception e) {
					log.error(e, e);
				}
			}
			deleteAuction(auction);
		}
	}

	// 组合查询(条件查询，分页)
	public AuctionResult getAuctions(int type, int quality, int leveldown, int levelup, String name, int sortfeild, int asc, int pageNum, int amount, int playerId) throws AuctionException {
		AuctionResult result = getAuctionDAO().getAuctions(type, quality, leveldown, levelup, name, sortfeild, asc, pageNum, amount, playerId);
		if (result == null) {
			throw new AuctionException("Không có thông tin bán đầu giá phù hợp điều kiện");
		}
		return result;
	}

	/**
	 * 返回最近本角色在拍卖行发布的依旧在拍卖中拍卖信息
	 */
	public List<Auction> getPublishiedAuctionsByPlayerId(int playerId)
			throws AuctionException {
		synchronized (this) {
			List<Auction> auctions = new ArrayList<Auction>();
			for (Auction auction : cache) {
				if (playerId == auction.getPlayerId() && auction.getValidTime().getTime() > System.currentTimeMillis()) {
					auctions.add(auction);
				}
			}
			return auctions;
		}
	}

	/**
	 * 返回玩家最近竞拍的拍卖信息
	 */
	public List<Auction> getJoinAuctions(Player p) throws AuctionException {
		List list = new ArrayList<Auction>();
		synchronized (this) {
			try {
				Set set = (Set) record.get(p.id);
				for (Object auction : set) {
					list.add((Auction) auction);
				}
			} catch (Exception e) {
				throw new AuctionException("最近没有参加竞拍");
			}
		}
		return list;
	}

	/**
	 * 根据物品id查询拍卖详细信息
	 */
	public List<Auction> getPublishiedAuctionsByItemId(int itemId, Player p)throws AuctionException {
		synchronized (this) {
			List<Auction> auctions = new ArrayList<Auction>();
			for (Auction auction : cache) {
				if (p.id == auction.getPlayerId() && auction.getItem() == p.bag.getGameItem(itemId)
						&& auction.getValidTime().getTime() > System.currentTimeMillis()) {
					auctions.add(auction);
				}
			}
			if (auctions.size() == 0) {
				throw new AuctionException("Gần đây không phát lệnh đấu giá hoặc đấu giá đã hết hạn");
			}
			return auctions;
		}
	}
	
	private int getCnt(int count){
		for(int i=0;i<cnt.length;i++){
			if(cnt[i] == count)
				return i;
		}
		return -1;
	}
	
	private int getMon(int money){
		for(int i=0;i<monLevel.length;i++){
			if(monLevel[i] == money)
				return i;
		}
		return -1;
	}
}
