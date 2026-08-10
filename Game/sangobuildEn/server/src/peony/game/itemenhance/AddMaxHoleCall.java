package peony.game.itemenhance;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 镶嵌：扩展装备的最大孔数。（需要扣除一个扩展孔物品，暂不开放）
 * serial               int                 序列号
 * itemid               int                 装备的物品id
 * instanceid           int                 装备的instanceid
 * public static final short DECORATE_ADD_MAX_HOLE_CLIENT = 501;
 * 镶嵌：扩展装备的最大孔数返回。
 * serial               int                 序列号
 * itemid               int                 装备的物品id
 * instanceid           int                 装备的instanceid
 * holecount            byte                新附加最大孔数
 * public static final short DECORATE_ADD_MAX_HOLE_SERVER = 502;
 */
public class AddMaxHoleCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(AddMaxHoleCall.class);
	protected int serial;
	protected Player player;
    protected int equItemID;
	protected int equInstanceID;
	protected ItemEnhance itemEnh;
	public static final int ADD_MAX_HOLE_ITEM = 123; 

	public AddMaxHoleCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.player = (Player)session.getClient();
		this.equItemID = packet.getInt();
		this.equInstanceID = packet.getInt();
	}
	
	protected void go() throws Exception {
	    if (true) {
	        throw new Exception("暫時不支持此功能");
	    }
	    
	    // 在背包中查找目标装备
	    Object[] obj = ItemUtil.findPlayerEquipment(player, equItemID, equInstanceID);
	    if (obj == null) {
	        throw new Exception("找不到此裝備");
	    }
	    GameItem gi = (GameItem)obj[0];
	    LogUtil.logAddMaxHoleTry(player, gi);
	    if (gi.template.equipment == null) {
	        throw new Exception("鑲嵌目標必須是一件裝備");
	    }
	    if (gi.object == null) {
	        gi.object = new ItemEnhance();
	    }
	    if (!(gi.object instanceof ItemEnhance)) {
	        throw new Exception("裝備數据錯誤");
	    }
	    
	    // 检查是否已达到最大孔位
	    itemEnh = (ItemEnhance)gi.object;
	    if (itemEnh.addMaxHole + gi.template.equipment.maxHole >= 20) {
	        throw new Exception("已達到最大孔數");
	    }
	    
	    // 从背包中扣除打孔物品，修改孔数
	    PlayerTransaction tx = player.newTransaction("AHL");
	    if (player.bag.removeGameItem(ADD_MAX_HOLE_ITEM, GameItem.GENERAL_INSTANCEID, 1, tx, true) == null) {
	        tx.rollback();
	        ItemTemplate it = ObjectAccessor.getItemTemplate(ADD_MAX_HOLE_ITEM);
            throw new Exception(MessageFormat.format("必須擁有{0}才能打孔", it.name));
	    }
	    tx.commit();
	    itemEnh.addMaxHole++;
	    
	    LogUtil.logAddMaxHoleOK(player, gi);
	}

	public void callFinish() {
	    try {
	        go();
	        
	        // 回送打孔成功的包
	        Packet pt = new Packet(OpCode.DECORATE_ADD_HOLE_SERVER);
            pt.putInt(serial);
            pt.putInt(equItemID);
            pt.putInt(equInstanceID);
            pt.put(itemEnh.addHole);
            session.send(pt);
	    } catch (Exception e) {
	        ErrorHandler.sendErrorMessage(session, serial,
                    OpCode.DECORATE_ADD_HOLE_CLIENT, e.getMessage());
	    }
	}

	public void run() {
		addToClientSession();
	}
}
