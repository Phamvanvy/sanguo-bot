package peony.db;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopService;

public class RefreshPropertyPointCall extends ClientSessionAsyncCall implements NoItemShopBuyI{
	
	private static final Logger log = Logger.getLogger(RefreshPropertyPointCall.class);
	
	public int ITEMID = 1185;
	Player player = null;
	public int serial;
	protected int oldStrength=0;
	protected int oldAgility=0;
	protected int oldStamina=0;
	protected int oldIntellect=0;
	

	public RefreshPropertyPointCall(Packet packet,ClientSession session) {
		super(session);
		serial = packet.getInt();
		player = (Player)session.getClient();
	}

	public void process(Object[] o) {
		oldStrength=player.strength-player.strengthAdded;
		oldAgility=player.agility-player.agilityAdded;
		oldStamina=player.stamina-player.staminaAdded;
		oldIntellect=player.intellect-player.intellectAdded;
		player.refreshPropertiesPoint();
		player.message(-1, "重置成功", -1, -1);
		addToClientSession();
	}

	public void procssFail(Object[] o) {
		oldStrength=player.strength;
		oldAgility=player.agility;
		oldStamina=player.stamina;
		oldIntellect=player.intellect;
		addToClientSession();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.PLAYER_REBUILDPROPERTY_SERVER);
			pt.putInt(serial);
			pt.putInt(player.propertyPoint);
			pt.putInt(oldStrength);
			pt.putInt(oldIntellect);
			pt.putInt(oldStamina);
			pt.putInt(oldAgility);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.PLAYER_REBUILDPROPERTY_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player!=null){
			if(player.strengthAdded == 0 && player.agilityAdded == 0 && player.staminaAdded == 0 && player.intellectAdded == 0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PLAYER_REBUILDPROPERTY_CLIENT, "没有加点，无需重置 ");
				return;
			}
			PlayerTransaction tx = player.newTransaction("RFP");
			GameItem item = player.bag.removeGameItemIngoreInstanceId(ITEMID, 1, tx, true);
			if(item == null){
				tx.rollback();
				ShopService service = Server.server.getServiceRegistry().getShopService();
				try{
					int shopId = service.getShopByItemId(ITEMID).id;
					NoItemShopBuy ibuy = new NoItemShopBuy(player,serial,shopId,ITEMID,1,this,null);
					service.buy(player, ibuy);
					log.info("[REFRESHPROPERTY]"+LogUtil.getPlayerLogString(player)+"STATE[OK]");
				}catch(Exception e){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.PLAYER_REBUILDPROPERTY_CLIENT, peony.Messages.STRING_00911);
					log.info("[REFRESHPROPERTY]"+LogUtil.getPlayerLogString(player)+"STATE[FAIL]");
					return;
				}
			}else{
				tx.commit();
				oldStrength=player.strength-player.strengthAdded;
				oldAgility=player.agility-player.agilityAdded;
				oldStamina=player.stamina-player.staminaAdded;
				oldIntellect=player.intellect-player.intellectAdded;
				player.refreshPropertiesPoint();
				player.message(-1, "重置成功", -1, -1);
				addToClientSession();
			}
		}
	}
}
