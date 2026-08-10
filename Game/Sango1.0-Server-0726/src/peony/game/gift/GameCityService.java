package peony.game.gift;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.GameQuest;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.TransactionException;
import peony.net.ClientSession;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.account.Account;

public class GameCityService implements Service, ServiceEventListener {
	List<CityGiftRule> cityRules = new ArrayList<CityGiftRule>();
	ConcurrentHashMap<Integer,String> messages = new ConcurrentHashMap<Integer,String>();
	public static int QUEST_REWARD = 1941;

	public void shutdown() {

	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("citygift.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		Server.server.getEventManager().registerListener(this);

	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_PLAYER_CREATED, ServiceEvent.EVENT_PLAYER_FIRSTLOAD};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_CREATED:
			 checkRuleAndSendGift((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			sendMessage((Player)event.param1);
			break;
		}

	}
	
	protected void sendMessage(Player player){
		String msg = messages.remove(player.id);
		if(msg != null){
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, msg);
		}
	}

	protected void checkRuleAndSendGift(Player p) {
		ClientSession session = p.session;
		Account account = null;
		if (session != null) {
			account = (Account) session.getIdentity();
		}
		if (account != null) {
			for(CityGiftRule rule : cityRules){
				if(checkCityRule(p, account, rule)){
					addGift(p,rule.itemId,rule.count);
					messages.put(p.id, rule.message);
				}
			}
		}
	}
	
	public boolean checkCityRule(Player player, Account account, CityGiftRule rule){
		if(rule.inCity(account.getCity()==null ? "" : account.getCity()) && rule.inRevision(Server.server.revision)){
			if (rule.type == ChannelGiftRule.TYPE_EVERYACTOR)
				return true;
			else if (rule.type == ChannelGiftRule.TYPE_EVERYACCOUNT) {
				return (Server.server.getServiceRegistry().getDbService().playerDAO
						.countPlayer(player.accountId) == 1L);
			}
		}
		return false;
	}
	
	protected void addGift(Player player, int itemId, int count){
		if(itemId==0 || count==0)
			return;
		PlayerTransaction tx = player.newTransaction("GFT");
		try {
			player.bag.addGameItemComplete(ObjectAccessor.createGameItem(itemId), count, tx, false);
		} catch (NoEnoughSpaceException e) {
			tx.rollback();
		}
		tx.commit();
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		if (root != null) {
			List cg = root.elements("city");
			for (int i = 0; i < cg.size(); i++) {
				String city = ((Element) cg.get(i)).attributeValue("name");
				int itemId = Integer.parseInt(((Element) cg.get(i))
						.attributeValue("itemId"));
				int count = Integer.parseInt(((Element) cg.get(i))
						.attributeValue("count"));
				int type = Integer.parseInt(((Element) cg.get(i))
						.attributeValue("type"));
				String message = ((Element) cg.get(i))
						.attributeValue("message");
				String revision = ((Element) cg.get(i)).attributeValue("revision");
				CityGiftRule chg = new CityGiftRule(city, itemId,
						count, type, message, revision);
				cityRules.add(chg);
			}
		}
	}
	
	public List<CityGiftRule> getCityRules(){
		return this.cityRules;
	}
	
	public void gameCityReward(GameQuest quest, Player player){
		for(CityGiftRule rule : cityRules){
			try {
				if(checkCityRule(player, player.getAccount(), rule)){
					if(quest.questInfo.owner.type==1){
						// ³¡¾°ÈÎÎñ
						PlayerTransaction tx = player.newTransaction("CITYGIFT");
						try {
							player.bag.addGameItemComplete(ObjectAccessor.createGameItem(GameCityService.QUEST_REWARD), 1, tx, true);
						} catch (NoEnoughSpaceException e) {
							tx.rollback();
						}
						tx.commit();
					}
				}
			} catch (TransactionException e) {
				
			}
		}
	}

}
