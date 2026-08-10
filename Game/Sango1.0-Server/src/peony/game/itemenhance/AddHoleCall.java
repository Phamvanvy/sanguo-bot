package peony.game.itemenhance;

import java.text.MessageFormat;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.nation.Nation;
import peony.game.nation.NationSkill2;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;

/**
 * 镶嵌：在装备上打新孔。（需要扣除一个打孔物品）
 * serial               int                 序列号
 * itemid               int                 装备的物品id
 * instanceid           int                 装备的instanceid
 * public static final short DECORATE_ADD_HOLE_CLIENT = 499;
 * 镶嵌：在装备上打新孔返回。
 * serial               int                 序列号
 * itemid               int                 装备的物品id
 * instanceid           int                 装备的instanceid
 * holecount            byte                新附加孔数
 * public static final short DECORATE_ADD_HOLE_SERVER = 500;
 */
public class AddHoleCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(AddHoleCall.class);
	protected int serial;
	protected Player player;
    protected int equItemID;
	protected int equInstanceID;
	protected ItemEnhance itemEnh;
	protected static Random rand = new Random();

	public AddHoleCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.player = (Player)session.getClient();
		this.equItemID = packet.getInt();
		this.equInstanceID = packet.getInt();
	}
	
	protected void go() throws Exception {
		// 在背包中查找目标装备
		Object[] obj = ItemUtil.findPlayerEquipment(player, equItemID,
				equInstanceID);
		if (obj == null) {
			throw new Exception(peony.Messages.STRING_00015);
		}
		GameItem gi = (GameItem) obj[0];
		LogUtil.logAddHoleTry(player, gi);
		if (gi.template.equipment == null) {
			throw new Exception(peony.Messages.STRING_00016);
		}
		if (gi.object == null) {
			gi.object = new ItemEnhance();
		}
		if (!(gi.object instanceof ItemEnhance)) {
			throw new Exception(peony.Messages.STRING_00017);
		}

		// 检查是否已达到最大孔位
		itemEnh = (ItemEnhance) gi.object;
		if (itemEnh.addHole + gi.template.equipment.initHole >= itemEnh.addMaxHole
				+ gi.template.equipment.maxHole) {
			throw new Exception(peony.Messages.STRING_00019);
		}

		// 从背包中扣除金钱和打孔物品
		PlayerTransaction tx = player.newTransaction("AHL");
		JewelService js = Server.server.getServiceRegistry().getJewelService();
		int needMoney = js.getAddHolePrice(gi.template.useLevel,
				itemEnh.addHole + gi.template.equipment.initHole);
		try {
			player.decMoney(needMoney, tx, true);
		} catch (NoEnoughValueException ex) {
			tx.rollback();
			throw new Exception(peony.Messages.STRING_00020);
		}
		List<ItemTemplate> l = js.getAddHoleItem(gi.template.useLevel);
		if (l.size() == 0) {
			tx.rollback();
			throw new Exception(peony.Messages.STRING_00135);
		}
		boolean ok = false;
		// ItemTemplate it = js.getAddHoleItem(gi.template.useLevel);
		for (ItemTemplate it : l) { //按照顺序寻找可以使用的打孔符，打孔符是向下兼容的
			if (player.bag.removeGameItem(it.id, GameItem.GENERAL_INSTANCEID,
					1, tx, true) != null) {
				ok = true;
				break;
				// tx.rollback();
				// throw new Exception("缺少" + it.name);
			}
		}
		if (ok) {
			tx.commit();

			// 计算成功率
			int rate = js.getAddHoleSuccRate(itemEnh.addHole
					+ gi.template.equipment.initHole);
			Nation nation = Server.server.getServiceRegistry().getNationService().getNationByFaction(player.faction);
			NationSkill2 skill = (NationSkill2)nation.skills.get(2); //国家科技，打孔大师
			if(skill != null){
				float v = skill.getAddHoleAdded();
				if(v != 0f){
					rate *= (1 + v);
					if(rate > 10000){
						rate = 10000;
					}
				}
			}
			if (rand.nextInt(10000) > rate) {
				LogUtil.logAddHoleOK(player, gi, false);
				throw new Exception(peony.Messages.STRING_01827);
			}

			// 修改孔数
			itemEnh.addHole++;
			LogUtil.logAddHoleOK(player, gi, true);
			//打孔成功事件
	        Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_DIG_SUCCESS, player));

		}else{
			throw new Exception(MessageFormat.format(peony.Messages.STRING_01828, l.get(0).name));
		}
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
