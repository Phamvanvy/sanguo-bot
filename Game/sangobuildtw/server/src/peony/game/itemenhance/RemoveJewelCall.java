package peony.game.itemenhance;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;

/**
 * 镶嵌：从装备上取下宝石。
 * serial               int                 序列号
 * itemid               int                 要镶嵌的装备的物品id
 * instanceid           int                 要镶嵌的装备的instanceid
 * hole                 byte                镶孔索引(0表示第一个)
 * public static final short DECORATE_REMOVE_JEWEL_CLIENT = 497;
 * 镶嵌：从装备上取下宝石返回。
 * serial               int                 序列号
 * itemid               int                 要镶嵌的装备的物品id
 * instanceid           int                 要镶嵌的装备的instanceid
 * jewelinfo            byte[]              镶嵌后装备宝石信息（参见后面DECORATION数据结构说明）
 * public static final short DECORATE_REMOVE_JEWEL_SERVER = 498;
 */
public class RemoveJewelCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(RemoveJewelCall.class);
	protected int serial;
	protected Player player;
    protected int equItemID;
	protected int equInstanceID;
	protected int hole;
	protected ItemEnhance itemEnh;

	public RemoveJewelCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.player = (Player)session.getClient();
		this.equItemID = packet.getInt();
		this.equInstanceID = packet.getInt();
		this.hole = packet.getByte() & 0xFF;
	}
	
	protected void go() throws Exception {
	    // 在背包中查找目标装备
        Object[] obj = ItemUtil.findPlayerEquipment(player, equItemID, equInstanceID);
        if (obj == null) {
            throw new Exception("找不到此裝備");
        }
        GameItem gi = (GameItem)obj[0];
        LogUtil.logRemoveJewelTry(player, gi, hole);
        Object giOwner = obj[1];
	    if (gi.template.equipment == null) {
	        throw new Exception("鑲嵌目標必須是一件裝備");
	    }
	    if (gi.object == null) {
	        gi.object = new ItemEnhance();
	    }
	    if (!(gi.object instanceof ItemEnhance)) {
	        throw new Exception("裝備數据錯誤");
	    }
	    
	    // 检查孔位是否合法，宝石是否正确
	    itemEnh = (ItemEnhance)gi.object;
	    if (hole < 0 || hole >= itemEnh.addHole + gi.template.equipment.initHole) {
	        throw new Exception("不存在的鑲孔");
	    }
	    int jewelID = itemEnh.getJewel(hole);
	    if (jewelID == -1) {
	        throw new Exception("這個鑲孔上沒有寶石");
	    }
        ItemTemplate jewelTemp = ObjectAccessor.getItemTemplate(jewelID);
        if (jewelTemp == null) {
            throw new Exception("寶石數据錯誤");
        }
	    
        // 扣除金钱、宝石摘除符
	    PlayerTransaction tx = player.newTransaction("RJE");
	    JewelService js = Server.server.getServiceRegistry().getJewelService();
	    int lvl = jewelTemp.useLevel;
	    int needMoney = js.getRemovePrice(lvl);
	    try {
	        player.decMoney(needMoney, tx, true);
	    } catch (NoEnoughValueException ex) {
	        tx.rollback();
	        throw new Exception("沒有足夠的金錢");
	    }
	    int removeSuccess = 0;
	    for(int i=lvl-1;i<js.removeItemIDs.length;i++){
	    	if (player.bag.removeGameItem(js.removeItemIDs[i], GameItem.GENERAL_INSTANCEID, 1, tx, true) != null) {
	    		removeSuccess = 1;
		        break;
		    }
	    }
	    if(removeSuccess==0){
	    	tx.rollback();
	        throw new Exception(MessageFormat.format("缺少{0}以上的摘除符", js.removeItems[lvl - 1].name));
	    }
	    
	    // 把摘除的宝石加入背包
	    GameItem jewel = ObjectAccessor.createGameItem(jewelTemp, -1);
	    if (!player.bag.addGameItem(jewel, 1, tx, true)) {
	        tx.rollback();
	        throw new Exception("背包已滿");
	    }
	    tx.commit();
        
        // 删除镶嵌信息
	    itemEnh.removeJewel(hole);
	    LogUtil.logRemoveJewelOK(player, gi, hole, true);
	    
        // 如果当前装备的物品被修改，刷新人物属性
        if (giOwner instanceof Player) {
            player.refreshProperties(false);
        } else if (giOwner instanceof Horse) {
            // 如果马的装备物品被修改，刷新马的属性；如果这个马当前被装备，还需要刷新人的属性
            Horse h = (Horse)giOwner;
            h.refreshProperties(false, player);
            if (h == player.horse) {
                player.refreshProperties(false);
            }
        }
	}

	public void callFinish() {
	    try {
	        go();
	        
	        // 回送取下成功的包
	        Packet pt = new Packet(OpCode.DECORATE_REMOVE_JEWEL_SERVER);
            pt.putInt(serial);
            pt.putInt(equItemID);
            pt.putInt(equInstanceID);
            pt.put(itemEnh.toClientBytes());
            session.send(pt);
	    } catch (Exception e) {
	        ErrorHandler.sendErrorMessage(session, serial,
                    OpCode.DECORATE_REMOVE_JEWEL_CLIENT, e.getMessage());
	    }
	}

	public void run() {
		addToClientSession();
	}
}
