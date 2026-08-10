package peony.service.shop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import peony.common.SyncIbuyCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.itemeffect.AddAttendantExpEffect;
import peony.net.ClientSession;
import peony.net.Packet;

public class QuickBuyAndUseCall extends SyncIbuyCall {
	protected final Logger log = Logger.getLogger(QuickBuyAndUseCall.class);
	protected int serial;
	protected int shopID;
	protected int itemID;
	protected short count;
	
	private static final Map<Integer,Integer> cache = new ConcurrentHashMap<Integer,Integer>();

	public QuickBuyAndUseCall(ClientSession session, Packet packet) {
		super(session, null);
		this.serial = packet.getInt();
		this.shopID = packet.getInt();
		this.itemID = packet.getInt();
		this.count = packet.getShort();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.SHOP_QUICK_BUYANDUSE_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			if(!errorMessage.equals(peony.Messages.STRING_00554))
				ErrorHandler.sendErrorMessage(session, serial, OpCode.SHOP_QUICK_BUYANDUSE_CLIENT, errorMessage);
		}
	}

	public void run() {
		// …Í«Îπ∫¬Ú
		Player player = (Player) session.getClient();
		if (player != null) {
			ShopService service = Server.server.getServiceRegistry().getShopService();
//			if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW))
//				decItemId = decImoneyAgentItemOftw;
			int shopId = service.getShopByItemId(itemID).id;
			try {
				waitBuy(player, serial, shopId, itemID, count, this);
				
				GameItem item = ObjectAccessor.createGameItem(itemID);
				if(item!=null){
					ItemEffect effect = item.template.useType.effect;
					if(effect!=null){
						PlayerTransaction tx = player.newTransaction("BULKUSE");
						if(effect instanceof AddAttendantExpEffect){
							AddAttendantExpEffect attendantEffect = (AddAttendantExpEffect)effect;
							attendantEffect.bulkUseItem(player, count);
						}
						tx.commit();
					}
				}
				addToClientSession();
			} catch (Exception e) {
				error(peony.Messages.STRING_00554);
				addToClientSession();
				return;
			}
		}
	}
}
