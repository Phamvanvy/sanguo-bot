package com.pip.itimes.server.world;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;

import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.Auction;
import com.pip.itimes.server.dao.AuctionDao;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.camp.CampMainService;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class AuctionService implements Runnable{

    private static final Logger log = Logger.getLogger(AuctionService.class);
    private AuctionDao dao;
    private MailService mailService;
    private ShopService shopService;
    private ChatService chatService;
    private PlayerService playerService;
    
    private CampMainService campMainService;
    
    private Random rnd = new Random();
    private Map forbids = new HashMap();

    private static Scheduler scheduler = null;
    private static final ItemType[] types = new ItemType[2];
    
    static{
        types[0] = new ItemType(IItem.TYPE_BASIC,"基本物品");
        types[1] = new ItemType(IItem.TYPE_EQU,"装备");
    }

    public AuctionService(AuctionDao dao) throws Exception {
        this.dao = dao;
    }

    public void setMailService(MailService mailService){
        this.mailService = mailService;
    }

    public void setShopService(ShopService shopService){
        this.shopService = shopService;
    }

    public void setChatService(ChatService chatService){
        this.chatService = chatService;
    }
    
    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }
    
    public void setCampMainService(CampMainService campMainService){
        this.campMainService = campMainService;
    }
    
    public Auction[] getAuction(int type, int quality, int level,short areaId,String name, int begin,
                                 int count) throws AuctionException{
        try {
        	if (areaId == 99){
        		return dao.getAuctionscredit(areaId, begin, count);
        	}else{
        		return dao.getAuctions(type, quality, level,areaId,name, begin, count);
        	}
        } catch (DataAccessException ex) {
            throw new AuctionException("查询拍卖列表出错");
        }
     }

     public int getCount(int type, int quality, int level,short areaId,String name) throws AuctionException{
        try {
        	if(areaId == 99){
        		return dao.getCountcredit(areaId);
        	}else{
        		return dao.getCount(type, quality, level, areaId, name);
        	}
        } catch (DataAccessException ex) {
            throw new AuctionException("查询拍卖列表出错");
        }
     }

     public ItemType[] getAuctionTypes(){
         return types;
     }

     public Auction getAuction(int id) throws AuctionException{
        try {
            return dao.getAuction(id);
        } catch (DataAccessException ex) {
            throw new AuctionException("查询拍卖明细出错");
        }
     }

     public void writeItemInfo(Auction auction, int dataVersion, UWAPSegment seg) {
         ItemAttachment attachment = (ItemAttachment) ItemUtils.
                                     dbBytes2Attachment(auction.getItem(), dataVersion);
         if (attachment.getItem().getType() == IItem.TYPE_EQU) {
        	 seg.writeString(ItemUtils.getEquipmentString((IEquipment) attachment.getItem()));
         } else {
             attachment.getItem().getDesc();
             seg.writeString(auction.getName());
         }
         seg.write(attachment.getItem().getType());
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(bos);
         try{
	         if(attachment.getItem().getType() == IItem.TYPE_EQU){
	        	 dos.write(attachment.getItem().toClientBytesWithLevel(-1));
	         }else{
	        	 dos.write(attachment.getItem().toClientBytes(dataVersion));
	        	 dos.write((byte)attachment.count());
	         }
         }catch(Exception e){
        	 log.info(e, e);
         }
         seg.write(bos.toByteArray());
     }

     public boolean deleteAuction(Auction auction){
        try {
            dao.makeTransient(auction);
            return true;
        } catch (DataAccessException ex) {
            return false;
        }
     }

     public void saveAuction(Auction auction){
        try {
            dao.makePersistent(auction);
        } catch (DataAccessException ex) {
        }
     }

     public void addAuction(int id, String name, IItem item, int count, int startPrice, int endPrice, short areaId) throws
             AuctionException {
         Auction auction = new Auction();
         auction.setAreaId(areaId);
         auction.setCurrentPrice( -1);
         auction.setEndPrice(endPrice);
         auction.setCreateTime(new Date());
         auction.setValidTime(new Date(System.currentTimeMillis() + 8 * 3600 * 1000L));
         auction.setItem(ItemUtils.item2dbAttachment(item, count));
         auction.setLastPlayerId( -1);
         if (item.getType() == IItem.TYPE_EQU) {
             auction.setLevel(((IEquipment) item).getLevel());
             auction.setName(item.getName());
         } else {
             auction.setLevel(1);
             auction.setName(item.getName() + " x " + count);
         }
         auction.setPlayerId(id);
         auction.setPlayerName(name);
         auction.setQuality(item.getQuality());
         auction.setShopId( -1);
         auction.setStartPrice(startPrice);
         if (item.getType() == IItem.TYPE_EQU)
             auction.setType(IItem.TYPE_EQU);
         else
             auction.setType(IItem.TYPE_BASIC);
         auction.setState((byte) 0);
         try {
             dao.makePersistent(auction);
         } catch (DataAccessException ex) {
             throw new AuctionException("添加拍卖出错");
         }
     }

     public Auction addAuction(PlayerData player, IItem item, int count,int startPrice,
                            int endPrice,short areaId) throws AuctionException{
         Auction auction = new Auction();
         auction.setAreaId(areaId);
         auction.setCurrentPrice(-1);
         auction.setEndPrice(endPrice);
         auction.setCreateTime(new Date());
         auction.setValidTime(new Date(System.currentTimeMillis()+8*3600*1000L));
         auction.setItem(ItemUtils.item2dbAttachment(item,count));
         auction.setLastPlayerId(-1);
         if(item.getType()==IItem.TYPE_EQU){
             auction.setLevel(((IEquipment)item).getLevel());
             auction.setName(item.getName());
         }else{
             auction.setLevel(1);
             auction.setName(item.getName() + " x " + count);
         }
         auction.setPlayerId(player.getId());
         auction.setPlayerName(player.getPlayerName());
         auction.setQuality(item.getQuality());
         auction.setShopId(-1);
         auction.setStartPrice(startPrice);
         if(item.getType()==IItem.TYPE_EQU)
             auction.setType(IItem.TYPE_EQU);
         else
             auction.setType(IItem.TYPE_BASIC);
         auction.setState((byte)0);
        try {
            dao.makePersistent(auction);
            return auction;
        } catch (DataAccessException ex) {
            throw new AuctionException("添加拍卖出错");
        }
     }

     public Auction addAuction(PlayerData player, IItem item, int count,int startPrice,
                            int endPrice,ShopData shop) throws AuctionException{
         Auction auction = new Auction();
         auction.setAreaId((short)shop.getAreaId());
         auction.setCurrentPrice(-1);
         auction.setEndPrice(endPrice);
         auction.setCreateTime(new Date());
         auction.setValidTime(new Date(System.currentTimeMillis()+8*3600*1000L));
         auction.setItem(ItemUtils.item2dbAttachment(item,count));
         auction.setLastPlayerId(-1);
         if(item.getType()==IItem.TYPE_EQU){
             auction.setLevel(((IEquipment)item).getLevel());
             auction.setName(item.getName());
         }else{
             auction.setLevel(1);
             auction.setName(item.getName() + " x " + count);

         }
         auction.setPlayerId(player.getId());
         auction.setPlayerName(player.getPlayerName());
         auction.setQuality(item.getQuality());
         auction.setShopId(shop.getId());
         auction.setStartPrice(startPrice);
         if(item.getType()==IItem.TYPE_EQU)
             auction.setType(IItem.TYPE_EQU);
         else
             auction.setType(IItem.TYPE_BASIC);
         auction.setState((byte)0);
        try {
            dao.makePersistent(auction);
            return auction;
        } catch (DataAccessException ex) {
            throw new AuctionException("添加拍卖出错");
        }
     }

     public void priceOk(PlayerData player, Auction auction) {
         if (auction.getShopId() != -1) { //竞标成功如果是商铺卖的东西那么先存钱到商铺
             ShopData shop = shopService.getShopData(auction.getShopId());
             if (shop != null) {
            	 int money = auction.getCurrentPrice();	//真实的价格
            	 WorldPlayer auctionplayer = playerService.getWorldPlayerAndCatch(shop.getPlayerId());
            	 try {
//					 WorldPlayer auctionplayer = playerService.loadWorldPlayer(shop.getPlayerId());
					 int tax = campMainService.getTax(money, auctionplayer.getCamp());
					 //2012年5月7日14:05:37 取消加入阵营金库 by zxyu
//					 campMainService.addCampMoney(auctionplayer.getCamp(), tax);
					 money -= tax;
            	 } catch (Exception e) {
					 log.error("PlayerID["+shop.getPlayerId()+ "]load unRegistry error");
				 }finally{
					 playerService.releasePlayer(auctionplayer);
				 }
                 synchronized (shop) {
                	 byte[] att = new byte[0];
                	 if(shop.getMoney() + money < 0){
                		 att = ItemUtils.money2dbAttachment(money);
                	 }else{
                		 shop.setMoney(shop.getMoney() + money);
                	 }
                     mailService.sendMail(shop.getPlayerId(), "", -1, "系统", "拍卖成功",
                                          auction.getName() + "拍卖成功。" + (att.length > 0 ? "金额在附件中。" : "金额已加入商铺(" + money + "J)"),
                                          att, 0, true);
                     log.info("OnlyPrice LastPlayerId["+player.getId()+ "]Auction["+auction.getId()+"]PlayerId["+auction.getPlayerId()+"]Shop["+ auction.getShopId() 
                    		 +"]oldPrice["+auction.getCurrentPrice()+"]Price["+money+"]Item["+Utils.getHexdump(auction.getItem()) +"]ShopOk");
                 }
             } else {//竞拍后商店不存在的情况
                 log.info("Auction Ok Save To Shop Fail Money[" +
                          auction.getCurrentPrice() + "]Shop["+ auction.getShopId()+"]Item["+Utils.getHexdump(auction.getItem())+"]");
             }
         } else {
        	 int money = auction.getCurrentPrice();		//真实的价格
        	 int temp = money;
        	 int tax = 0;
        	 WorldPlayer auctionplayer = playerService.getWorldPlayerAndCatch(auction.getPlayerId());
        	 try {
//				 WorldPlayer auctionplayer = playerService.loadWorldPlayer(auction.getPlayerId());
				 tax = campMainService.getTax(money, auctionplayer.getCamp());
				//2012年5月7日14:05:37 取消加入阵营金库 by zxyu
//                 campMainService.addCampMoney(auctionplayer.getCamp(), tax);
                 money -= tax;
			 } catch (Exception e) {
				 log.error("PlayerID["+auction.getPlayerId()+ "]load unRegistry error");
			 }
        	 playerService.releasePlayer(auctionplayer);
			 String taxprice = "" + Integer.toString(tax);
             mailService.sendMail(auction.getPlayerId(), auction.getPlayerName(),
                                  -1, "系统", "拍卖返回", "" + auction.getName() + "  收税金额为: " + taxprice + "J",
                                  ItemUtils.
                                  money2dbAttachment(money),
                                  0, false);
             log.info("OnlyPrice LastPlayerId["+player.getId()+ "]Auction["+auction.getId()+"]PlayerId["+auction.getPlayerId()+"]Shop["+ auction.getShopId() 
            		 +"]oldPrice["+auction.getCurrentPrice()+"]Price["+money+"]Item["+Utils.getHexdump(auction.getItem())+"]playerOk");
         }
         mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                              "拍卖", "", auction.getItem(), 0, false);
     }

     public void auctionTimeOut(Auction auction) {
         if ((auction.getLastPlayerId() != -1) && (auction.getAreaId() != 99)) {
        	 //如果有人拍 把东西给人 把钱给商店或人////非荣誉拍卖
             mailService.sendMail(auction.getLastPlayerId(), "", -1, "系统",
                                  "竞拍成功", "", auction.getItem(), 0,false);
             if (auction.getShopId() != -1) {
                 ShopData shop = shopService.getShopData(auction.getShopId());
                 int money = auction.getCurrentPrice();		//真实的价格
                 WorldPlayer auctionplayer = playerService.getWorldPlayerAndCatch(shop.getPlayerId());
            	 try {
//    				 WorldPlayer auctionplayer = playerService.loadWorldPlayer(shop.getPlayerId());
    				 int tax = campMainService.getTax(money, auctionplayer.getCamp());
    				 //2012年5月7日14:05:37 取消加入阵营金库 by zxyu
//    				 campMainService.addCampMoney(auctionplayer.getCamp(), tax);
    				 money -= tax;
    			 } catch (Exception e) {
    				 log.error("PlayerID["+auction.getPlayerId()+ "]load unRegistry error");
    			 }finally{
    				 playerService.releasePlayer(auctionplayer);
    			 }
                 synchronized (shop) {
                     shop.setMoney(shop.getMoney() + money);
                     mailService.sendMail(shop.getPlayerId(), "", -1, "系统", "拍卖成功",
                                          auction.getName() + "拍卖成功。金额已加入商铺(" + money + "J)",
                                          new byte[0], 0, true);
                     //log.info("Auction["+auction.getId()+"]TimeOutShopOk");
                     log.info("LastPlayerId["+auction.getLastPlayerId()+ "]Auction["+auction.getId()+"]PlayerId["+auction.getPlayerId()+"]Shop["+ auction.getShopId() 
                    		 +"]oldPrice["+auction.getCurrentPrice()+"]Price["+money+"]Item["+Utils.getHexdump(auction.getItem())+"]TimeOutShopOk");
                 }
             } else {
            	 int money = auction.getCurrentPrice();		//真实的价格
            	 int temp = money;
            	 int tax = 0;
            	 WorldPlayer auctionplayer = playerService.getWorldPlayerAndCatch(auction.getPlayerId());
            	 try {
//    				 WorldPlayer auctionplayer = playerService.loadWorldPlayer(auction.getPlayerId());
    				 tax = campMainService.getTax(money, auctionplayer.getCamp());
    				 //2012年5月7日14:05:37 取消加入阵营金库 by zxyu
//    				 campMainService.addCampMoney(auctionplayer.getCamp(), tax);
                     money -= tax;
            	 } catch (Exception e) {
    				 log.error("PlayerID["+auction.getPlayerId()+ "]load unRegistry error");
    			 } finally{
    				 playerService.releasePlayer(auctionplayer);
    			 }
            	 String taxprice = Integer.toString(tax);
                 mailService.sendMail(auction.getPlayerId(), "", -1, "系统",
                                      "拍卖成功", "拍卖的物品为:" + auction.getName() + "收税为:" + taxprice + "J",
                                      ItemUtils.money2dbAttachment(money), 0,false);
                 //log.info("Auction["+auction.getId()+"]TimeOutPlayerOk");
                 log.info("LastPlayerId["+auction.getLastPlayerId()+ "]Auction["+auction.getId()+"]PlayerId["+auction.getPlayerId()+"]Shop["+ auction.getShopId() 
                		 +"]oldPrice["+auction.getCurrentPrice()+"]Price["+money+"]Item["+Utils.getHexdump(auction.getItem())+"]TimeOutplayerOk");
                 
             }

         } else if(auction.getAreaId() == 99){//荣誉拍卖结束
        	 if(auction.getLastPlayerId() != -1){//如果是-1的话，说明是系统拍卖的，禁止存入数据库
	        	 mailService.sendMail(auction.getLastPlayerId(), "", -1, "系统",
	                     "竞拍成功", "", auction.getItem(), 0,false);
	        	 log.info("(CREDITSHOP)Auction Ok PLAYERID[" +auction.getLastPlayerId()+ "] credit[" +
	        			 auction.getCurrentPrice()+ "]..Auction["+auction.getId()+"]Item["+Utils.getHexdump(auction.getItem())+"]TimeOutplayerOk");
        	 }
         } else{
             if (auction.getShopId() != -1) {  //如果是商店拍卖的东西就先加到商店中如果不行再发信
//            	 WorldPlayer player = playerService.getWorldPlayer(auction.getLastPlayerId());
            	 WorldPlayer player = playerService.getWorldPlayerAndCatch(auction.getPlayerId());
            	 ItemAttachment attachment = null;
            	 if (player != null){
            		 attachment = (ItemAttachment) ItemUtils.dbBytes2Attachment(auction.getItem(), player.getClientDataVersion());
            	 }else{
            		 attachment = (ItemAttachment) ItemUtils.dbBytes2Attachment(auction.getItem(), 0);
            	 }
            	 playerService.releasePlayer(player);
                 ShopData shop = shopService.getShopData(auction.getShopId());
                 synchronized (shop) {
                     if (!shop.completeAddItem(attachment.getItem(),
                                               attachment.count())) {
                         mailService.sendMail(shop.getPlayerId(), "", -1,
                                              "系统", "拍卖失败", "您的商品在拍卖时间内无人问津，拍卖失败。", auction.getItem(),
                                              0,false);
                         log.info("LastPlayerId["+auction.getLastPlayerId()+ "]Auction["+auction.getId()+"]PlayerId["+auction.getPlayerId()+"]Shop["+ auction.getShopId() 
                        		 +"]Price["+auction.getCurrentPrice()+"]Item["+Utils.getHexdump(auction.getItem())+"]TimeOutShopFaillToShop");
                         //log.info("Auction["+auction.getId()+"]TimeOutShopFaillToShop");
                     }else{
                         mailService.sendMail(shop.getPlayerId(), "", -1, "系统", "拍卖失败",
                                              auction.getName() + "拍卖失败。物品已经加入商铺。",
                                          new byte[0], 0, true);
                         log.info("LastPlayerId["+auction.getLastPlayerId()+ "]Auction["+auction.getId()+"]PlayerId["+auction.getPlayerId()+"]Shop["+ auction.getShopId() 
                        		 +"]Price["+auction.getCurrentPrice()+"]Item["+Utils.getHexdump(auction.getItem())+"]TimeOutShopFaillToMail");
                         //log.info("Auction["+auction.getId()+"]TimeOutShopFaillToMail");
                     }
                 }
             } else {
                 mailService.sendMail(auction.getPlayerId(),
                                      auction.getPlayerName(), -1,
                                      "系统", "拍卖失败", "您的商品在拍卖时间内无人问津，拍卖失败。", auction.getItem(), 0,false);
                 //log.info("Auction["+auction.getId()+"]TimeOutPlayerFaillToMail");
                 log.info("LastPlayerId["+auction.getLastPlayerId()+ "]Auction["+auction.getId()+"]PlayerId["+auction.getPlayerId()+"]Shop["+ auction.getShopId() 
                		 +"]Price["+auction.getCurrentPrice()+"]Item["+Utils.getHexdump(auction.getItem())+"]TimeOutPlayerFaillToMail");
             }

         }

     }

     public void setState(int shopId,byte state){
         try {
             dao.setState(shopId, state);
         } catch (DataAccessException ex) {
         }
    }

    public void setOwner(int shopId,PlayerData player){

    }

    public void start(){
        new Thread(this).start();
    }

    public void run(){
        while(true){
            try {
                Thread.sleep(600 * 1000L);
            } catch (InterruptedException ex) {
            }
            try{
                checkTimeoutAuctions();
            }
            catch(Throwable ex){

            }
        }
    }

    private synchronized void checkTimeoutAuctions() throws DataAccessException{
        Auction[] auctions = dao.getTimeoutAuctions();
        for(int i=0;i<auctions.length;i++){
//            mailService.sendAuctionMail(auctions[i]);
            deleteAuction(auctions[i]);
            auctionTimeOut(auctions[i]);
        }
    }

    public void addForbiden(int id,int second){
        if(second==0){
            forbids.remove(new Integer(id));
        }else{
            AuctionForbiden forbiden = new AuctionForbiden(id,System.currentTimeMillis()+second*1000L);
            forbids.put(new Integer(id),forbiden);
        }
    }

    public boolean isFrobiden(int id){
        AuctionForbiden f = (AuctionForbiden)forbids.get(new Integer(id));
        if(f==null)
            return false;
        return System.currentTimeMillis()<f.validTime;
    }

    public Auction[] getAuctions(int shopId, byte type, int begin, int count) throws
            AuctionException {
        try {
            return dao.getAuctionsByShopAndType(shopId, begin, count);
        } catch (DataAccessException ex) {
            throw new AuctionException("查询出售列表出错");
        }
    }

    public int getCount(int shopId) throws AuctionException{
        try {
            return dao.getCount(shopId);
        } catch (DataAccessException ex) {
            throw new AuctionException("查询商铺错误");
        }
    }

    //mengjie add

    //public void start(long forbidEnterTime,long forbidTime,long endTime) throws AuctionException{
    public synchronized void start(CreditShop creditshop) throws AuctionException{
        //log.info("荣誉拍卖行物品："+creditshop.getTitle()+"登录。物品号：" + creditshop.getItemID());
    	IItem item = null;
    	if (creditshop.getItemID() == -1){//配置掉落组
    		DropGroup group = DropGroups.getDropGroup(creditshop.getGroupID(), 50);
    		if(group != null){
	    		int rate = rnd.nextInt(group.getRate());
	            DropItem dropItem = group.calcDropItem(
	                    rate);
	            //int count = getCount(rnd,
	            //        dropItem.getMin(),
	            //        dropItem.getMax());
	            item = dropItem.getItem().newInstance();
	            log.info("(CREDITSHOP)Auction out itime by group name[" +item.getName()+ "] id[" +
	            		item.getItemId()+ "]");
    		}
    	}else{
    		log.info("(CREDITSHOP)Auction out itime name[" +creditshop.getTitle()+ "] id[" +
        		creditshop.getItemID()+ "]");
    		item = Items.getTemplate(creditshop.getItemID()).newInstance();
    	}

        //登陆
        
        this.addAuctioncredit(item,creditshop.getPrice(),(short)creditshop.getAreaId(),creditshop.getTime());
        //chatService.sendSystemMessage("荣誉拍卖行新物品出现！"+creditshop.getTitle()+"。竞拍时间为"+creditshop.getTime()+"分钟。");
        if ((creditshop.getDesc() == null) || ("".equalsIgnoreCase(creditshop.getDesc()))){

        }else{
        	chatService.sendWorldMessage(-1, "系统",creditshop.getDesc());
        }
    }

    private static int getCount(Random rnd, int min, int max) {
        return rnd.nextInt(max - min + 1) + min;
    }
    
    public void addAuctioncredit(IItem item, int startPrice, short areaId,int time) throws
    AuctionException {
    	Auction auction = new Auction();
    	auction.setAreaId(areaId);
    	auction.setCurrentPrice( -1);
    	auction.setEndPrice(-1);
    	auction.setCreateTime(new Date());
    	auction.setValidTime(new Date(System.currentTimeMillis() + time * 60 * 1000L));
    	auction.setItem(ItemUtils.item2dbAttachment(item, 1));
    	auction.setLastPlayerId( -1);
    	if (item.getType() == IItem.TYPE_EQU) {
    		auction.setLevel(((IEquipment) item).getLevel());
    		auction.setName(item.getName());
    	} else {
    		auction.setLevel(1);
    		auction.setName(item.getName() + " x " + 1);
    	}
    	auction.setPlayerId(-1);
    	auction.setPlayerName("系统");
    	auction.setQuality(item.getQuality());
    	auction.setShopId( -1);
    	auction.setStartPrice(startPrice);
		if (item.getType() == IItem.TYPE_EQU)
			auction.setType(IItem.TYPE_EQU);
		else
			auction.setType(IItem.TYPE_BASIC);
		auction.setState((byte) 0);
		try {
		    dao.makePersistent(auction);
		} catch (DataAccessException ex) {
		    throw new AuctionException("添加荣誉拍卖品出错");
		}
    }
}

class AuctionForbiden{
    int id;
    long validTime;
    public AuctionForbiden(int id,long validTime){
        this.id = id;
        this.validTime = validTime;
    }
}
