package peony.service.shop;

import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.mortbay.log.Log;

import peony.game.Action;
import peony.game.ErrorHandler;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.GameObjectRef;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.mail.MailService;
import peony.net.Packet;

import com.pip.sanguo.data.Shop;

/**
 * 购买一个商店物品的实现。
 * @author lighthu
 */
public class ShopItemBuy implements IBuyObject {
	
	private static final Logger log = Logger.getLogger(ShopItemBuy.class);
    /*
     * 购买玩家
     */
    GameObjectRef player;
    /*
     * 购买请求包的序列号
     */
    int serial;
    /*
     * 商店
     */
    Shop shop;
    /*
     * 购买道具
     */
    Shop.ShopItem buyItem;
    /*
     * 道具模板
     */
    ItemTemplate itemTemplate;
    /*
     * 购买数量
     */
    int count;
    /*
     * 是否已经锁定数量
     */
    boolean locked;
    /*
     * 锁定时的刷新时间
     */
    int lockTime;
    /*
     * 产出物品
     */
    GameItem output;
    
    public ShopItemBuy(Player player, int serial, int shopID, int itemID, int count) throws ShopException {
        this.player = player.ref();
        this.serial = serial;
        shop = Server.server.getServiceRegistry().getShopService().findShop(shopID);
        if (shop == null) {
            throw new ShopException("商店不存在");
        }
        for (Shop.ShopItem item : shop.items) {
            if (item.item.id == itemID) {
                buyItem = item;
                break;
            }
        }
        if (buyItem == null) {
            throw new ShopException("商品不存在");
        }
        itemTemplate = ObjectAccessor.getItemTemplate(buyItem.item.id);
        if (itemTemplate == null) {
            throw new ShopException("商品不存在");
        }
        this.count = count;
    }
    
    public List<Shop.BuyRequirement> getRequirements() {
        return buyItem.requirements;
    }
    
    public void lock() throws ShopException {
        // 锁定数量
        synchronized (buyItem) {
            if(count>1){
                ItemTemplate t = ObjectAccessor.getItemTemplate(buyItem.item.id);
                if(t.isEquipment()||t.newInstance){
                    throw new ShopException("此物品一次只能購買一個");
                }
            }
            if (buyItem.count != 0 && buyItem.remain < count) {
                throw new ShopException("剩余數量不足");
            }
            if (buyItem.buyLimit != 0) {
                int boughtCount = 0;
                Integer existObj = buyItem.buyRecords.get(player.id);
                if (existObj != null) {
                    boughtCount = existObj.intValue();
                }
                if (count + boughtCount > buyItem.buyLimit) {
                    throw new ShopException(MessageFormat.format("購買失敗,每人最多只能購買{0}個", buyItem.buyLimit));
                }
                buyItem.buyRecords.put(player.id, boughtCount + count);
            }
            if (buyItem.count != 0) {
                buyItem.remain -= count;
            }
            lockTime = buyItem.lastRefreshTime;
            locked = true;
        }
    }
    
    public void receive(PlayerTransaction tx, boolean supportMail) throws ShopException {
        Player p = (Player)ObjectAccessor.getGameObject(player);
        if (p == null) {
        	GameItem newItem = ObjectAccessor.createGameItem(buyItem.item.id);
        	if(getShopType().equals("CMCC")){
        		if(newItem.template.bindType==ItemTemplate.BIND_NO){
        			newItem.bindInstance = 0;
        		}
        	}
            MailService ms = Server.server.getServiceRegistry().getMailService();
            String objName = newItem.template.name;
            if (count > 1) {
                objName += " x" + count;
            }
            if (ms.sendSystemMail(player.id, "系統商店", objName, 
            		MessageFormat.format("您購買的{0}因為角色下線,通過系統郵件發送.請註意系統郵件所帶的特殊標志,謹防被騙.", objName),
                    0, newItem, count, "SHOP") == -1) {
                throw new ShopException("背包已滿,發送系統郵件失敗,請撥打客服電話咨詢");
            } 
        	output = newItem;
            return;
        }
        
        // 添加物品
        GameItem newItem = ObjectAccessor.createGameItem(buyItem.item.id);
        if(getShopType().equals("CMCC")){
    		if(newItem.template.bindType==ItemTemplate.BIND_NO){
    			newItem.bindInstance = 0;
    		}
    	}
        if (supportMail) {
            PlayerTransaction newTx = p.newTransaction("BUY");
            try {
                p.bag.addGameItemComplete(newItem, count, newTx, true);
                newTx.commit();
            } catch (NoEnoughSpaceException e) {
                newTx.rollback();
                
                // 发送邮件
                MailService ms = Server.server.getServiceRegistry().getMailService();
                String objName = newItem.template.name;
                if (count > 1) {
                    objName += " x" + count;
                }
                if (ms.sendSystemMail(p.id, "系統商店", objName, 
                		MessageFormat.format("您購買的{0}因為背包已滿,通過系統郵件發送.請註意系統郵件所帶的特殊標志,謹防被騙.", objName),
                        0, newItem, count, "SHOP") == -1) {
                    throw new ShopException("背包已滿,發送系統郵件失敗,請撥打客服電話咨詢");
                } else{
                    p.message(-1, "背包已滿,物品通過系統郵件發送.", -1, -1);
                }
            }
        } else {
            try {
                p.bag.addGameItemComplete(newItem, count, tx, true);
            } catch (NoEnoughSpaceException e) {
                throw new ShopException("沒有足夠的背包格");
            }
        }
        output = newItem;
        
        // 记录玩家动作
        p.addAction(Action.SHOP_BUY);
        boolean useImoney = false;
        boolean useCredit = false;
        for (Shop.BuyRequirement req : buyItem.requirements) {
        	if (req.deduct && req.type == Shop.TYPE_IMONEY) {
        		useImoney = true;
        	}
        	if (req.deduct && req.type == Shop.TYPE_HONOR) {
        		useCredit = true;
        	}
        }
        if (useImoney) {
        	p.addAction(Action.IMONEY_BUY);
        }
        if (useCredit) {
        	p.addAction(Action.CREDIT_BUY);
        }
    }
    
    public void commit() {
        
    }
    
    public void rollback() {
        if (locked) {
            // 恢复剩余数量
            synchronized (buyItem) {
                if (buyItem.lastRefreshTime == lockTime) {
                    if (buyItem.buyRecords.containsKey(player.id)) {
                        buyItem.buyRecords.put(player.id, buyItem.buyRecords.get(player.id) - count);
                    }
                    if (buyItem.count != 0) {
                        buyItem.remain += count;
                    }
                }
            }
        }
    }
    
    public int getDiscount() {
        return buyItem.discount;
    }
    
    public int getCount() {
        return count;
    }
    
    public void notifyClient(boolean succ, String message) {
        Player p = (Player)ObjectAccessor.getGameObject(player);
        if (p == null) {
            return;
        }
        if (succ) {
            // 通知客户端购买成功
            if (p.session != null) {
                Packet pt = new Packet(OpCode.SHOP_BUY_SERVER);
                pt.putInt(serial);
                pt.putInt(itemTemplate.id);
                pt.putShort(count);
                pt.putString(itemTemplate.name);
                pt.put(itemTemplate.quality);
                pt.put(itemTemplate.showType);
                p.session.send(pt);
            }
        } else {
            // 通知客户端购买失败
            if (p.session != null) {
                ErrorHandler.sendErrorMessage(p.session, serial,
                        OpCode.SHOP_BUY_CLIENT, message);
            }
        }
    }
    
    @Override
	public String toString() {
        return "TYPE[SHOP]SHOP[" + shop.id + "]ITEM[" + buyItem.item.id + "]COUNT[" + count + "]";
    }
    
    public void log() {
    	if (output != null) {
    		LogUtil.logShopBuy(player, shop.id, output, count, this);
		}
    }
    
    public String getCause() {
    	return "BUY";
    }
    
    public String getShopType(){
    	if(shop!=null){
    		int shopId = shop.id;
    		if(shopId==47 || shopId==48 || shopId==49 || shopId==50 || shopId==51)
    			 return "CMCC";
    	}
    	return "";
    }
}
