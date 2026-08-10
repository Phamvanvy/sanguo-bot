package peony.game.gift;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.account.Account;

public class GameChannelService implements Service, ServiceEventListener {
	List<ChannelGiftRule> rules = new ArrayList<ChannelGiftRule>();
	ConcurrentHashMap<Integer,String> messages = new ConcurrentHashMap<Integer,String>();

	public void shutdown() {

	}

	public void startup() throws Exception {
		loadConfig();
		Server.server.getEventManager().registerListener(this);

	}
	
	public void loadConfig() throws Exception{
		rules.clear();
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("channelgift.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
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
		if(msg != null && !msg.equals("")){
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
			for (ChannelGiftRule rule : rules) {
				if(checkRule(p,account.getChannel(),rule)){
					addGift(p,rule.itemId,rule.count);
					messages.put(p.id, rule.message);
				}
			}
		} else {
			// 快速注册用户
			for (ChannelGiftRule rule : rules) {
				if(checkQuickRegistryRule(p, p.accountId, rule)){
					addGift(p,rule.itemId,rule.count);
					messages.put(p.id, rule.message);
				}
			}
		}
		if(Server.REVISION_TYPE_CMCC.equals(Server.server.revision)){
			if(Server.server.getServiceRegistry().getDbService().playerDAO
					.countPlayer(p.accountId) == 1L){
				addGift(p,490,1);
				messages.put(p.id, peony.Messages.STRING_01126);
			}
		}
	}
	
	protected boolean checkRule(Player player, String channel, ChannelGiftRule rule){
		if(rule.channel.equals("")){
			if (rule.type == ChannelGiftRule.TYPE_EVERYACTOR)
				return true;
			else if (rule.type == ChannelGiftRule.TYPE_EVERYACCOUNT) {
				return (Server.server.getServiceRegistry().getDbService().playerDAO
						.countPlayer(player.accountId) == 1L);
			}
		}
		if (rule.inChannel(channel)) {
			if (rule.type == ChannelGiftRule.TYPE_EVERYACTOR)
				return true;
			else if (rule.type == ChannelGiftRule.TYPE_EVERYACCOUNT) {
				return (Server.server.getServiceRegistry().getDbService().playerDAO
						.countPlayer(player.accountId) == 1L);
			}
		}
		return false;
	}
	
	protected boolean checkQuickRegistryRule(Player player, int accountId, ChannelGiftRule rule){
		return checkRule(player, player.session.getInitChannel(), rule);
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
			List cg = root.elements("channel");
			for (int i = 0; i < cg.size(); i++) {
				String channel = ((Element) cg.get(i)).attributeValue("name");
				int itemId = Integer.parseInt(((Element) cg.get(i))
						.attributeValue("itemId"));
				int count = Integer.parseInt(((Element) cg.get(i))
						.attributeValue("count"));
				int type = Integer.parseInt(((Element) cg.get(i))
						.attributeValue("type"));
				String message = ((Element) cg.get(i))
						.attributeValue("message");
				ChannelGiftRule chg = new ChannelGiftRule(channel, itemId,
						count, type, message);
				rules.add(chg);
			}
		}
	}

}
