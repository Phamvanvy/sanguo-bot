package peony.game.itemenhance;

import java.text.MessageFormat;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Action;
import peony.game.ChatOption;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObject;
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
import peony.game.attendant.Attendant;
import peony.game.changed.BindChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;

import com.pip.sanguo.data.item.Item;

/**
 * 镶嵌：向装备上添加宝石。
 * serial               int                 序列号
 * itemid               int                 要镶嵌的装备的物品id
 * instanceid           int                 要镶嵌的装备的instanceid
 * hole                 byte                镶孔索引(0表示第一个)
 * jewelid              int                 宝石物品ID
 * method               byte                镶嵌方法：0 - 无镶嵌符，1 - 用等级镶嵌符，2 - 用高级镶嵌符
 * public static final short DECORATE_ADD_JEWEL_CLIENT = 495;
 * 镶嵌：向装备上添加宝石返回。
 * serial               int                 序列号
 * itemid               int                 镶嵌的装备的物品id
 * instanceid           int                 镶嵌的装备的instanceid
 * jewelinfo            byte[]              镶嵌后装备宝石信息（参见后面DECORATION数据结构说明）
 * public static final short DECORATE_ADD_JEWEL_SERVER = 496;
*/
public class AddJewelCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(AddJewelCall.class);
	protected int serial;
	protected Player player;
    protected int equItemID;
	protected int equInstanceID;
	protected int hole;
	protected int jewelID;
	protected byte method;
	protected ItemEnhance itemEnh;
	protected static Random rand = new Random();
	protected GameItem jewel;

	public AddJewelCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.player = (Player)session.getClient();
		this.equItemID = packet.getInt();
		this.equInstanceID = packet.getInt();
		this.hole = packet.getByte() & 0xFF;
		this.jewelID = packet.getInt();
		this.method = packet.get();
	}
	
	protected void go() throws Exception {
	    // 在背包中查找目标装备
	    Object[] obj = ItemUtil.findPlayerEquipment(player, equItemID, equInstanceID);
	    if (obj == null) {
	        throw new Exception("找不到此装备");
	    }
	    GameItem gi = (GameItem)obj[0];
	    Object giOwner = obj[1];
	    LogUtil.logAddJewelTry(player, gi, jewelID, hole, method);
	    if (gi.template.equipment == null) {
	        throw new Exception("镶嵌目标必须是一件装备");
	    }
	    if (gi.object == null) {
	        gi.object = new ItemEnhance();
	    }
	    if (!(gi.object instanceof ItemEnhance)) {
	        throw new Exception("装备数据错误");
	    }
	    
	    // 检查孔位是否合法，宝石是否正确（同一类的宝石只能镶1个），坐骑宝石不能镶嵌给人物装备
	    itemEnh = (ItemEnhance)gi.object;
	    ItemTemplate jewelTemp = ObjectAccessor.getItemTemplate(jewelID);
	    if (jewelTemp == null || jewelTemp.itemType != Item.TYPE_JEWEL) {
	        throw new Exception("宝石数据错误");
	    }
	    if (hole < 0 || hole >= itemEnh.addHole + gi.template.equipment.initHole) {
	        throw new Exception("不存在的镶孔");
	    }
	    if (itemEnh.getJewel(hole) != -1) {
	        throw new Exception("这个镶孔上已经有宝石了，请先取下再镶嵌");
	    }
	    if (itemEnh.findJewelByType(jewelTemp.jewelType)) {
	        throw new Exception("一件装备上同一类的宝石只能镶嵌一个");
	    }
	    if (jewelTemp.isHorseJewel && !gi.template.isHorseEquipment()) {
	        throw new Exception("这个宝石只能在坐骑装备上镶嵌");
	    }
	    
	    
	    // 扣除宝石，金钱，镶嵌符
	    PlayerTransaction tx = player.newTransaction("AJE");
	    GameItem jewel = null;
	    if ((jewel = player.bag.removeGameItem(jewelID, GameItem.GENERAL_INSTANCEID, 1, tx, true)) == null) {
	        tx.rollback();
	        throw new Exception("你没有这个宝石");
	    }
	    this.jewel = jewel;
	    JewelService js = Server.server.getServiceRegistry().getJewelService();
	    int needMoney = js.getDecoratePrice(jewelTemp.useLevel);
	    try {
	        player.decMoney(needMoney, tx, true);
	    } catch (NoEnoughValueException ex) {
	        tx.rollback();
	        throw new Exception("没有足够的金钱");
	    }
	    int rate = JewelService.DECO_SUCC_RATE1;
	    if (method == 1) {
	        rate = JewelService.DECO_SUCC_RATE2;
	        if (player.bag.removeGameItem(js.decorateItemID1, GameItem.GENERAL_INSTANCEID, 1, tx, true) == null) {
	            tx.rollback();
	            throw new Exception(MessageFormat.format("缺少{0}", js.deocrateItem1.name));
	        }
	    } else if (method == 2) {
	        rate = JewelService.DECO_SUCC_RATE3;
            if (player.bag.removeGameItem(js.decorateItemID2, GameItem.GENERAL_INSTANCEID, 1, tx, true) == null) {
                tx.rollback();
                throw new Exception(MessageFormat.format("缺少{0}", js.deocrateItem2.name));
            }
	    }
	    tx.commit();
	    
	    // 计算成功率
	    if (rand.nextInt(100) > rate) {
	    	LogUtil.logAddJewelOK(player, gi, jewelID, hole, method, false);
	        throw new Exception("很遗憾，镶嵌失败了");
	    }
	    
	    // 修改镶嵌信息
	    itemEnh.addJewel(hole, jewelID);
	    LogUtil.logAddJewelOK(player, gi, jewelID, hole, method, true);
	    player.addAction(Action.JEWEL);
	    // 如果原装备未绑定，且宝石是绑定的，则把装备设置为绑定
	    if (gi.bindInstance == -1 && jewelTemp.bindType == ItemTemplate.BIND_REWARD) {
	        gi.bindInstance = 0;
	        BindChangedItem item = new BindChangedItem(gi);
	        player.changed.addChangedItem(item);
	    }

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
        } else if(giOwner instanceof Attendant){
        	((Attendant) giOwner).refreshProperties(false);
        }
        if(jewel.template.useLevel==5){
        	String s = MessageFormat.format("{0}将一颗闪闪发光的{1}成功的镶在了{2}上！", player.name,jewel.template.name,gi.template.name);
        	Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(player.faction, s);
        }
        else if(jewel.template.useLevel==6){
        	String s = MessageFormat.format("{0}将一颗完美无瑕的{1}成功的镶在了{2}上！", player.name,jewel.template.name,gi.template.name);
        	Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(player.faction, s);
        }
        else if(jewel.template.useLevel==7){
        	String s = MessageFormat.format("{0}的{1}将一颗绝世神石{2}成功的镶在了{3}上！", player.getFactionName(),player.name,jewel.template.name,gi.template.name);
        	Server.server.getServiceRegistry().getChatService().sendSystemMessage(ChatOption.WORLD, "系统", s);
        }
        
	}

	public void callFinish() {
	    try {
	        go();
	        Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_ADDJEWEL_SUCCESS,player,jewel));
	        // 回送镶嵌成功的包
	        Packet pt = new Packet(OpCode.DECORATE_ADD_JEWEL_SERVER);
            pt.putInt(serial);
            pt.putInt(equItemID);
            pt.putInt(equInstanceID);
            pt.put(itemEnh.toClientBytes());
            session.send(pt);
            if(Server.server.revision.equals(Server.REVISION_TYPE_TW) && player!=null){
            	player.refreshStar7Buff();
            }
	    } catch (Exception e) {
	        ErrorHandler.sendErrorMessage(session, serial,
                    OpCode.DECORATE_ADD_JEWEL_CLIENT, e.getMessage());
	    }
	}

	public void run() {
		addToClientSession();
	}
}
