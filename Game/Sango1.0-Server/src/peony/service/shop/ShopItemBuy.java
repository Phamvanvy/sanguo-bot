package peony.service.shop;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.Action;
import peony.game.CommonUtil;
import peony.game.ErrorHandler;
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
import peony.game.Time;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.net.Packet;
import peony.util.TimeUtil;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.item.Item;

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
    
    public static boolean tempFlag = true;
    
    public String playerName;
    
    public static List<ShopMessage>noticeItems = new ArrayList<ShopMessage>();
    
    public ShopItemBuy(Player player, int serial, int shopID, int itemID, int count) throws ShopException {
        this.player = player.ref();
        this.serial = serial;
        this.playerName = player.name;
        shop = Server.server.getServiceRegistry().getShopService().findShop(shopID);
        if (shop == null) {
            throw new ShopException(peony.Messages.STRING_00504);
        }
        for (Shop.ShopItem item : shop.items) {
            if (item.item.id == itemID) {
                buyItem = item;
                break;
            }
        }
        if (buyItem == null) {
            throw new ShopException(peony.Messages.STRING_00505);
        }
        itemTemplate = ObjectAccessor.getItemTemplate(buyItem.item.id);
        if (itemTemplate == null) {
            throw new ShopException(peony.Messages.STRING_00505);
        }
        this.count = count;
    }
    
    public static void loadNoticeItems() {
		try {
			noticeItems.clear();
			byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("Items/shopnoticeitems.xml");
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			Element root = doc.getRootElement();
			Iterator ite = root.elementIterator("item");
			while (ite.hasNext()) {
				Element el = (Element)ite.next();
				int id = Integer.parseInt(el.attributeValue("id"));
				int shout = Integer.parseInt(el.attributeValue("shout"));
				int shop = Integer.parseInt(el.attributeValue("shop"));
				String location = el.attributeValue("location");
				ShopMessage shopMessage = new ShopMessage(shop, id, location, shout);
				noticeItems.add(shopMessage);
			}
		} catch (Exception e) {
		}
	}
    
    public ShopMessage getShopMessage(int itemId, int shopId){
    	for(ShopMessage sm : noticeItems){
    		if(sm.itemId==itemId && sm.shopId==shopId)
    			return sm;
    	}
    	return null;
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
                    throw new ShopException(peony.Messages.STRING_00506);
                }
            }
            if (buyItem.count != 0 && buyItem.remain < count) {
                throw new ShopException(peony.Messages.STRING_00507);
            }
            if (buyItem.buyLimit != 0) {
                int boughtCount = 0;
                Integer existObj = buyItem.buyRecords.get(player.id);
                if (existObj != null) {
                    boughtCount = existObj.intValue();
                }
                if(count>buyItem.buyLimit)
                	throw new ShopException(MessageFormat.format(peony.Messages.STRING_00508, buyItem.buyLimit));
                if (count + boughtCount > buyItem.buyLimit) {
                	if(tempFlag && buyItem.refresh > 0){
                		int timeDis = Time.currTime - buyItem.lastRefreshTime;
                		if(timeDis<buyItem.refresh){
                			String m = TimeUtil.getStringH_M_S((buyItem.refresh - timeDis)/1000);
                			throw new ShopException(MessageFormat.format("还差{0}才可以继续购买", m));
                		}
                	}
                    throw new ShopException(MessageFormat.format(peony.Messages.STRING_00508, buyItem.buyLimit));
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
        	if(getShopType().equals("CMCC") || Server.shopItemBind && newItem.template.itemType==Item.TYPE_JEWEL){
        		if(newItem.template.bindType==ItemTemplate.BIND_NO){
        			newItem.bindInstance = 0;
        		}
        	}
            MailService ms = Server.server.getServiceRegistry().getMailService();
            String objName = newItem.template.name;
            if (count > 1) {
                objName += " x" + count;
            }
            if (ms.sendSystemMail(player.id, peony.Messages.STRING_00509, objName, 
            		MessageFormat.format(peony.Messages.STRING_00510, objName),
                    0, newItem, count, "SHOP") == -1) {
                throw new ShopException(peony.Messages.STRING_00511);
            } 
        	output = newItem;
        	ShopMessage sm = null;
        	if((sm=getShopMessage(buyItem.item.id, shop.id))!=null){
            	sendMessage(newItem.template.name, sm.location);
            }
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
                if (ms.sendSystemMail(p.id, peony.Messages.STRING_00509, objName, 
                		MessageFormat.format(peony.Messages.STRING_00512, objName),
                        0, newItem, count, "SHOP") == -1) {
                    throw new ShopException(peony.Messages.STRING_00511);
                } else{
                    p.message(-1, peony.Messages.STRING_00513, -1, -1);
                }
            }
            ShopMessage sm = null;
        	if((sm=getShopMessage(buyItem.item.id, shop.id))!=null){
            	sendMessage(newItem.template.name, sm.location);
            }
        } else {
            try {
            	if(newItem.template!=null&&newItem.template.itemValid!=null&&newItem.template.itemValid.time>0){
					//装备本身属性
            		newItem.validTime=(int)(System.currentTimeMillis()/60000+newItem.template.itemValid.time);
				}
                p.bag.addGameItemComplete(newItem, count, tx, true);
                ShopMessage sm = null;
            	if((sm=getShopMessage(buyItem.item.id, shop.id))!=null){
                	sendMessage(newItem.template.name, sm.location);
                }
            } catch (NoEnoughSpaceException e) {
                throw new ShopException(peony.Messages.STRING_00514);
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
    
    public void sendMessage(String itemName, String location){
    	ChatService chatService = Server.server.getServiceRegistry().getChatService();
    	chatService.sendWorldMessage(MessageFormat.format("{0}从{1}处的{2}购买了{3}", playerName, location, shop.title, itemName));
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
        Player p = null;
        try {
			p = (Player)ObjectAccessor.getGameObject(player);
		} catch (Exception e) {
			p = ObjectAccessor.getPlayer(player.id);
		}
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
                pt.put(itemTemplate.showImage);
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

    /**
     * 是否允许使用不被信任的元宝购买。
     */
    public boolean allowUntrustIMoney() {
    	if(buyItem!=null && buyItem.item!=null){
    		int bind = buyItem.item.bind;
        	if(bind==Item.BIND_NO)
        		return false;
    	}
    	return true;
    }

	public boolean allowUseBindImoney() {
		return true;
	}
}

class ShopMessage{
	
	int shopId;
	int itemId;
	String location;
	int shout;
	
	public ShopMessage(int shopId, int itemId, String location, int shout) {
		super();
		this.shopId = shopId;
		this.itemId = itemId;
		this.location = location;
		this.shout = shout;
	}
	
}
