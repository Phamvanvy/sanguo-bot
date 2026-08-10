package peony.service.enhance;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopException;
import peony.service.shop.ShopService;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.service.tong.TongSkill;
import peony.service.tong.TongSkill4;

public class EquipEnhanceCall extends ClientSessionAsyncCall implements NoItemShopBuyI{

	public static final int MONEY = 200;
	
	Player p = null;
	int serial ;
	int itemId ;
	int instanceId ;
	EnhanceService enhService;
	GameItem gameItem;
	Object owner;
	/*不允许强化的装备ID**/
	public static int[] intensifyEuips={1008369,1008370,1008371,1008372};
	
	public EquipEnhanceCall(ClientSession session,Packet packet) {
		super(session);
		p = (Player) session.getClient();
		enhService = Server.server.getServiceRegistry().getEnhanceService();
		serial = packet.getInt();
		itemId = packet.getInt();
		instanceId = packet.getInt();
	}
	
	public void run() {
		if (p != null) {
			Object[] os = ItemUtil.findPlayerEquipment(p, itemId, instanceId);
			if (os != null) {
				gameItem = (GameItem) os[0];
				owner = os[1];
				if (gameItem == null) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.ENHANCE_EQUIP_CLIENT, peony.Messages.STRING_01352);
					return;
				}
				if (!gameItem.template.isEquipment()) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.ENHANCE_EQUIP_CLIENT, peony.Messages.STRING_01353);
					return;
				}
				if(canIntensify(itemId)){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.ENHANCE_EQUIP_CLIENT, "此装备不允许强化");
					return;
				}
				int enhanceTimes = enhService.getEnhanceTimes(p);
				//军团专属科技  装备强化福利 
				int maxTimes = 20;
				int decMoney = MONEY;
				TongService ts = Server.server.getServiceRegistry().getTongService();
				TongMember tm = ts.getPlayerInfo(p.id);
				if(tm!=null && tm.skills != null && tm.skills.get(4)!=null){
					TongSkill4 tskill = (TongSkill4)tm.skills.get(4);
					if(tskill != null){
						maxTimes = tskill.getValue();
						if(tskill.level == 1){
							decMoney = 100;
						}else if(tskill.level == 2){
							decMoney = 0;
						}
					}
				}
				if(enhanceTimes>=maxTimes){
					try {
						ShopService service = Server.server.getServiceRegistry().getShopService();
						int shopId = service.getShopByItemId(NoItemShopBuy.LIANGYUANBAO).id;
						NoItemShopBuy dib = new NoItemShopBuy(p,serial,shopId,NoItemShopBuy.LIANGYUANBAO,1,this,null);
						Server.server.getServiceRegistry().getShopService().buy(p, dib);
					} catch (ShopException e) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.ENHANCE_EQUIP_CLIENT, peony.Messages.STRING_01354);
						return;
					}
				}else{
					PlayerTransaction tx = p.newTransaction("ENHANCEEQU");
					try {
						p.decMoney(decMoney, tx, true);
						tx.commit();
						addToClientSession();
					} catch (NoEnoughValueException ex) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.ENHANCE_EQUIP_CLIENT, peony.Messages.STRING_01355);
						return;
					}
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ENHANCE_EQUIP_CLIENT, peony.Messages.STRING_00173);
			}
		}
	}

	public void process(Object[] o) {
		addToClientSession();
	}

	public void procssFail(Object[] o) {
		error(peony.Messages.STRING_00405);
		addToClientSession();
	}

	public void callFinish() throws Exception {
		if(success){
			enhService.equipEnhance(p, gameItem, owner, serial);
		}else{
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ENHANCE_EQUIP_CLIENT, errorMessage);
		}
	}

	/*是否可强化**/
	public static boolean canIntensify(int id){
		for(int tempid:intensifyEuips){
			if(id==tempid){
				return true;
			}
		}
		return false;
	}
	
	

}
