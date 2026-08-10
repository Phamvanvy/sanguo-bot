package com.pip.itimes.server.world;

import java.util.*;

import com.pip.itimes.server.bean.Shop;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.ShopDao;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.stage.ShopData;
//import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ShopService implements Runnable{

    private static final Logger log = Logger.getLogger(ShopService.class);

    private ShopDao dao;

    private ConcurrentHashMap shops = new ConcurrentHashMap();

    private AuctionService auctionService;
    private BuyService buyService;
    private OemService oemService;

    private static final ItemType[] materialTypes = new ItemType[4];
    static {
        materialTypes[0] = new ItemType((byte)1, "采矿");
        materialTypes[1] = new ItemType((byte)2, "采药");
        materialTypes[2] = new ItemType((byte)3, "打猎");
        materialTypes[3] = new ItemType((byte)4, "钓鱼");
    }

    private static final ItemType[] oemTypes = new ItemType[4];
    static{
        oemTypes[0] = new ItemType((byte)0,"锻造");
        oemTypes[1] = new ItemType((byte)1,"炼金");
        oemTypes[2] = new ItemType((byte)2,"裁缝");
        oemTypes[3] = new ItemType((byte)6,"烹饪");
    }


    public ShopService(ShopDao dao) {
        this.dao = dao;
    }

    public void setAuctionService(AuctionService auctionService){
        this.auctionService = auctionService;
    }

    public void setBuyService(BuyService buyService){
        this.buyService = buyService;
    }

    public void setOemService(OemService oemService){
        this.oemService = oemService;
    }

    public void saveShop(ShopData shop){
        try {
            shop.reset();
            dao.makePersistent(shop.getShop());
        } catch (DataAccessException ex) {
        }
    }

    public void loadAllShops() throws Exception{

        Shop[] ss = dao.getShops();
        for(int i=0;i<ss.length;i++){
            ShopData shop = new ShopData(ss[i]);
            shops.put(new Integer(shop.getId()),shop);
        }
    }

    public Shop createShop(PlayerData player, short areaId,String name) throws
            ShopException {
        synchronized(this){
            if (player.getMoeny() < getCreateShopMoney(player, areaId))
                throw new ShopException("钱不足");
            if (player.getLevel() < 15)
                throw new ShopException("建立商铺需要15级");
            try {
                if (dao.hasShop(name))
                    throw new ShopException("已经有同名商铺存在");
                if (dao.getShopCount(player.getId()) >= 2)
                    throw new ShopException("只能建立2个商铺");
            } catch (DataAccessException ex1) {
                throw new ShopException("建立商铺错误");
            }
            Shop shop = new Shop();
            shop.setLevel(1);
            shop.setName(name);
            shop.setMoney(0);
            shop.setPlayerId(player.getId());
            shop.setAreaId(areaId);
            shop.setCreateTime(new Date());
            shop.setItems(new byte[0]);
            shop.setGridSize((short) 10);
            shop.setState(Shop.STATE_NORMAL);
            shop.setBuyPlayerId( -1);
            shop.setPrice( -1);
            shop.setLevelupTime(new Date());
            try {
                dao.addShop(shop);
                ShopData s = new ShopData(shop);
                shops.put(new Integer(s.getId()), s);
                return shop;
            } catch (Exception ex) {
                throw new ShopException("建立商铺错误");
            }
        }
    }

    public int getCreateShopMoney(PlayerData player,short areaId){
        return 5000;
    }

    public Shop[] getShop(int playerId,short areaId) throws ShopException{
        try {
            return dao.getShops(playerId, areaId);
        } catch (DataAccessException ex) {
            throw new ShopException("查询商铺失败");
        }
    }

    public ShopData[] getShops(int playerId,short areaId){
        int[] ids = getShopIds(playerId,areaId);
        ShopData[] ret = new ShopData[ids.length];
        for(int i=0;i<ret.length;i++){
            ret[i] = getShopData(ids[i]);
        }
        return ret;
    }

    public ShopData[] getShops(int playerId){
        int[] ids = getShopIds(playerId);
        ShopData[] ret = new ShopData[ids.length];
        for(int i=0;i<ret.length;i++){
            ret[i] = getShopData(ids[i]);
        }
        return ret;
    }

    public int[] getShopIds(int playerId,short areaId){
        try {
            return dao.getShopIds(playerId, areaId);
        } catch (DataAccessException ex) {
            return new int[0];
        }
    }

    public int[] getShopIds(int playerId){
        try {
            return dao.getShopIds(playerId);
        } catch (DataAccessException ex) {
            return null;
        }
    }


    public ShopData getShopData(int shopId){
        ShopData shop = (ShopData)shops.get(new Integer(shopId));
        if(shop==null){
            Shop s = getShop(shopId);
            if(s!=null){
                try {
                    shop = new ShopData(s);
                    shops.put(new Integer(shop.getId()), shop);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        return shop;
    }

    private Shop getShop(int shopId){
        try {
            return (Shop)dao.getObject(Shop.class, new Integer(shopId));
        } catch (DataAccessException ex) {
            return null;
        }
    }



    public ItemType[] getMaterialTypes(){
        return materialTypes;
    }

    public ItemType[] getOemTypes(){
        return oemTypes;
    }

    public int setup(ShopData shop) throws ShopException{
        if(shop.getState()==Shop.STATE_NORMAL){
            throw new ShopException("已经处于开业状态");
        }
        int money = getSetupMoney(shop.getLevel());
        if(shop.getMoney()<money)
            throw new ShopException("没有足够的钱");
        shop.setMoney(shop.getMoney()-money);
        shop.setState(Shop.STATE_NORMAL);
        saveShop(shop);
        return money;
    }

    public void shutout(ShopData shop) throws ShopException{
        if(shop.getState()==Shop.STATE_CLOSED){
            throw new ShopException("已经处于停业状态");
        }
        shop.setState(Shop.STATE_CLOSED);
        saveShop(shop);
        auctionService.setState(shop.getId(),
                                Shop.STATE_CLOSED);
        buyService.setState(shop.getId(), Shop.STATE_CLOSED);
        oemService.setState(shop.getId(), Shop.STATE_CLOSED);

    }

    public int getSetupMoney(int level){
        if(level==1){
            return 150;
        }
        else if(level==2){
            return 400;
        }
        else if(level==3){
            return 700;
        }
        else if(level==4){
            return 1100;
        }
        return 0;
    }

    public int levelup(ShopData shop) throws ShopException{
        if(shop.getLevel()>=4)
            throw new ShopException("商店已经是最高级");
        long last = shop.getLevelupTime().getTime();
        long curr = System.currentTimeMillis();
        if(curr-last<getLevelUpTime(shop.getLevel()))
            throw new ShopException("还没达到店铺升级所需的时间");
        int money = getLevelupMoney(shop.getLevel());
        if(money>0){
            if(shop.getMoney()<money)
                throw new ShopException("没有足够金钱");
            shop.setMoney(shop.getMoney()-money);
            shop.setLevel(shop.getLevel()+1);
            shop.setGridSize((short)getGridSize(shop.getLevel()));
            shop.setLevelupTime(new Date());
            saveShop(shop);

        }
        return money;
    }

    public long getLevelUpTime(int level){
        if(level==1)
            return 10*86400000;
        if(level==2)
            return 20*86400000;
        if(level==3)
            return 30*86400000;
        return Long.MAX_VALUE;
    }

    public int getLevelupMoney(int level){
        if(level==1)
            return 40000;
        else if(level==2)
            return 200000;
        else if(level==3)
            return 500000;
        return -1;
    }

    public int getGridSize(int level){
        if(level==1)
            return 10;
        else if(level==2)
            return 20;
        else if(level==3)
            return 30;
        else if(level==4)
            return 40;
        return -1;
    }

    public Shop[] getSellShop(short areaId, PlayerData player) throws
            ShopException {
        try {
            return dao.getSellShops(player.getId(), areaId);
        } catch (DataAccessException ex) {
            throw new ShopException("查询商铺失败");
        }
    }

    public void saveAll(){
        int i = 0;
        Iterator ite = shops.values().iterator();
        while(ite.hasNext()){
            ShopData shop = (ShopData)ite.next();
            saveShop(shop);
            i++;
        }
        log.info("All "+i+" shop saved");
    }
    /**
     * 每次存数据时候的存盘量
     */
    public final static short saveRate = 30;
    
    public void save(int round){
        Iterator ite = shops.values().iterator();
        while(ite.hasNext()){
            ShopData shop = (ShopData)ite.next();
            if(shop.getId()%saveRate==round){
                saveShop(shop);
//                log.info("Shop ["+shop.getId()+"] Saved round["+round+"]");
            }
        }
        log.info("Shop Saved round["+round+"]");
    }

    private Calendar lastCheckTime = Calendar.getInstance();
    private int hours = 3;

    public void run(){
        try {
            Thread.sleep(3600000L);
        } catch (InterruptedException ex) {
        }
        try{
            Calendar current = Calendar.getInstance();
            dailyCheck(current);
            lastCheckTime = current;
        }
        catch(Exception ex){
            log.error(ex,ex);
        }
    }

    public void start(){
        new Thread(this).start();
    }

    private static final int[][] dailyFee = { {50, 30, 30}, {300, 100, 100},
                                            {500, 200, 200}, {800, 300, 300}
    };

    public int getDailyFee(ShopData shop) {
        return dailyFee[shop.getLevel() - 1][shop.getState()];
    }

    public void dailyCheck(Calendar current) throws Exception{
        int lastHour = lastCheckTime.get(Calendar.HOUR_OF_DAY);
        int currHour = current.get(Calendar.HOUR_OF_DAY);
        if(currHour>hours&&lastHour<hours){
            Iterator ite = shops.values().iterator();
            while (ite.hasNext()) {
                ShopData shop = (ShopData) ite.next();
                try {
                    dailyCheck(shop);
                } catch (Exception ex) {
                    throw ex;
                }
            }
        }
    }



    public void dailyCheck(ShopData shop) throws Exception{
        synchronized(shop){
            byte type = shop.getState();
            if(type==Shop.STATE_NORMAL){
                int fee = getDailyFee(shop);
                shop.setMoney(shop.getMoney()-fee);
                if(fee<0){
                    shutout(shop);
                }
            }
            else if(type==Shop.STATE_CLOSED){
                int fee = getDailyFee(shop);
                shop.setMoney(shop.getMoney()-fee);
            }
            else if(type==Shop.STATE_SELL){
                int fee = getDailyFee(shop);
                shop.setMoney(shop.getMoney()-fee);
            }
        }
    }

}
