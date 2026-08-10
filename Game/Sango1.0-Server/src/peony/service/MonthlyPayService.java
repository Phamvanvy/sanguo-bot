package peony.service;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.decimoney.DecImoneyBuy;
import peony.game.CommonUtil;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.game.itemeffect.ActivityItemEffect;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.VIP.VipPrivilegeService;
import peony.service.account.AccountProperty;
import peony.service.shop.ShopService;

/**
 * 包月服务
 * @author dchen
 */
public class MonthlyPayService implements Service, ServiceEventListener{

	public static final String PROPERTY_MONTHPAY_TELEPORT = "monthPayTeleport"; //传送包月 
	public static long DURATION = 30 * 24 * 3600 * 1000L;
//	public static long DURATION = 60 * 1000L;
	
	public Map<Integer,Integer> monthlyBuys = new HashMap<Integer,Integer>();
	public Map<Integer,String> name = new HashMap<Integer,String>();
	public Map<Integer,String> monthPayDec = new HashMap<Integer,String>();
	
	/** 传送包月 */
	public static final int MONTHPAY_TYPE_TELEPORT = 1165;
	
	public static int[] MONTHPAY_TYPE = {MONTHPAY_TYPE_TELEPORT};

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.
		findFile("monthlybuy.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Server.server.getEventManager().registerListener(this);
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) throws Exception {
		Element root = doc.getRootElement();
		List<Element> monthlyBuy = root.elements("item");
		for(Element buy : monthlyBuy){
			int item = Integer.parseInt(buy.attributeValue("itemId"));
			int amount = Integer.parseInt(buy.attributeValue("amount"));
			monthlyBuys.put(item, amount);
			String na = buy.attributeValue("name");
			name.put(item, na);
			String dec = buy.attributeValue("dec");
			monthPayDec.put(item, dec);
		}
	}
	
	public void monthlyPay(ClientSession session,Packet packet){
		int serial = packet.getInt();
		int itemId = packet.getInt();
		Player player = (Player)session.getClient();
		GameItem item = ObjectAccessor.createGameItem(itemId);
		if(player!=null){
			if(inService(player,itemId)){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.MONTH_PAY_CLIENT, peony.Messages.STRING_01662);
				return;
			}
			try {
				if(monthlyBuys.keySet().contains(itemId)){
					int amount = monthlyBuys.get(itemId);
					pay(player,amount);
					monthPay(player,itemId);
					LogUtil.logOpenMonthPay(player, itemId, name.get(itemId));
					Packet pt = new Packet(OpCode.MONTH_PAY_SERVER);
					pt.putInt(serial);
					player.send(pt);
					if(!ActivityItemEffect.hasTeleportEffect(player)){
					   player.addIntPropertyChangedItem(ChangedItem.TIMEOUT,item.template.id,false,true);
					}
				} else {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.MONTH_PAY_CLIENT, peony.Messages.STRING_01663);
				}
			} catch (Exception e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.MONTH_PAY_CLIENT, e.getMessage());
			}
		}
	}
	
	public void monthPay(Player player, int type) throws Exception {
		String pool = getPoolByType(type);
		if(pool!=null){
			player.pool.setLong(pool, System.currentTimeMillis());
			player.monthPay.put(type, player.pool.getLong(pool,0l));
		} else {
		  throw new Exception(peony.Messages.STRING_01663);
		}
	}
	
	public void pay(Player p,int amount) throws Exception{
		if(p!=null){
			ShopService shopService = Server.server.getServiceRegistry().getShopService();
			int price = amount;
			if(price<=0){
				throw new Exception(peony.Messages.STRING_01664);
			}
			try {
				DecImoneyBuy dib = new DecImoneyBuy(p,price*36);
				shopService.buy(p, dib);
			} catch (Exception e) {
				throw new Exception(peony.Messages.STRING_01665);
			}
		}
	}
	
	public void monthPayList(ClientSession session,Packet packet){
		int serial = packet.getInt();
		Player player = (Player)session.getClient();
		if(player!=null){
			if(monthlyBuys!=null && monthlyBuys.size()>0){
				Packet pt = new Packet(OpCode.MONTH_PAY_LIST_SERVER);
				pt.putInt(serial);
				pt.putInt(monthlyBuys.size()-1);
				for(Integer key : monthlyBuys.keySet()){
					if(key == MonthlyPayService.MONTHPAY_TYPE_TELEPORT){//去掉传送特权
						continue;
					}
					pt.putInt(key);
					pt.putString(name.get(key));
					pt.putInt(monthlyBuys.get(key));
					String pool = getPoolByType(key);
					if(key == MonthlyPayService.MONTHPAY_TYPE_TELEPORT && player.vipLevel>=1){
						pt.putInt(1);
					}else{
						if(pool!=null && player.monthPay.get(key)!=0){
							long leaving = System.currentTimeMillis() - player.pool.getLong(pool, 0l);
							if(DURATION>leaving)
			            		pt.putInt(Math.round((DURATION-leaving)/(24*3600*1000L))==0?1:Math.round((DURATION-leaving)/(24*3600*1000L))); //包月服务剩余时间(天 )
	                        else
			            		pt.putInt(0);
						} else {
							pt.putInt(0);
						}
					}
					if(key == MonthlyPayService.MONTHPAY_TYPE_TELEPORT && player.vipLevel>=1){
					     pt.putString("开通自由传送特权后即可在三国世界中随心所欲地使用世界传送功能,不会消耗传送符");
					}else{
						 pt.putString(monthPayDec.get(key));
					}
					if(key == MonthlyPayService.MONTHPAY_TYPE_TELEPORT && player.vipLevel>=1){
						pt.putInt(0);
					}else
					    pt.putInt(Math.round(DURATION/(24*3600*1000))==0?1:Math.round(DURATION/(24*3600*1000L)));
				}
				player.send(pt);
			}
		}
	}
	
	public boolean inService(Player player, int type){
		AccountProperty ap = Server.server.getServiceRegistry().getVipPrivilegeService().getAccountProperty(player.accountId);
		int vipLevel = ap.pool.getInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL,0);
		if(player.monthPay.containsKey(type)){
			if(type == MonthlyPayService.MONTHPAY_TYPE_TELEPORT && vipLevel>=1){
				return true;
			}
			long startTime = player.monthPay.get(type);
			if(startTime!=0){
				return System.currentTimeMillis()-startTime<=DURATION;
			}
		}
		return false;
	}
	
	public String getPoolByType(int type){
		switch(type){
		case MONTHPAY_TYPE_TELEPORT:
			return PROPERTY_MONTHPAY_TELEPORT;
		}
		return null;
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_PLAYER_LOADED };
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_LOADED:
			playerLoad((Player) event.param1);
			break;
		}
	}
	
	public void playerLoad(Player p){
		for(int i=0;i<MONTHPAY_TYPE.length;i++){
			String pool = getPoolByType(MONTHPAY_TYPE[i]);
			long startTime = p.pool.getLong(pool, 0);
			p.monthPay.put(MONTHPAY_TYPE[i], startTime);
		}
	}
	
//	public int getLeftDay(long leaving){
//		int ret = 1;
//		double tempDay = (double)(DURATION-leaving)/(24*3600*1000L);
//		ret = (int)Math.ceil(tempDay);
//		return ret;
//	}
}