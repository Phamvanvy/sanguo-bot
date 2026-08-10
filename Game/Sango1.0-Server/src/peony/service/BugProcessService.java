package peony.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.TransactionBagGrid;
import peony.game.attendant.Attendant;
import peony.util.IntHashMap;

/**
 * bug处理服务
 * @author dchen
 */
public class BugProcessService implements Service, ServiceEventListener {

	private static final Logger log = Logger.getLogger(BugProcessService.class);
	
	public IntHashMap<Integer> illegalItems = new IntHashMap<Integer>();
	public IntHashMap<Integer> trustPlayers = new IntHashMap<Integer>();
	
	public static int ILLEGALITEMPROCMOD_EVERYLOAD = 1;
	public static int ILLEGALITEMPROCMOD_FIRSTLOAD = 0;
	public static boolean illegalItemProc_isActive = true;
	public static int illegalItemsProcessMod = 0;
	
	public void startup() throws Exception {
		try {
			illegalItems.clear();
			byte[] bytes = Server.server.getServiceRegistry().getDataService().data .findFile("illegalitems.xml");
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
			Server.server.getEventManager().registerListener(this);
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		List<Element> list = root.elements("item");
		for(Element el : list){
			int itemId = Integer.parseInt(el.attributeValue("id"));
			illegalItems.put(itemId, 0);
		}
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
			processTestEquipment((Player)event.param1);
			break;
		}
	}
	
	protected void processTestEquipment(Player player){
		if(!illegalItemProc_isActive)
			return;
		if(illegalItemsProcessMod==ILLEGALITEMPROCMOD_FIRSTLOAD && trustPlayers.get(player.id)!=null)
			return;
		try {
			if(player!=null){
				List<Integer> removeItems = new ArrayList<Integer>();
				PlayerTransaction tx = null;
				for(GameItem item : player.equipments.equs){
					if(item!=null && item.template!=null && item.template.isEquipment() && illegalItems.get(item.template.id)!=null){
						player.unequip(item.template.id, item.instanceId, 0);
					}
				}
				if(player.attendantBag!=null){
					for(Attendant attendant : player.attendantBag.attendants){
						for(GameItem item : attendant.equs){
							if(item!=null && item.template!=null && item.template.isEquipment() && illegalItems.get(item.template.id)!=null){
								attendant.unEquip(item.template.id, item.instanceId);
							}
						}
					}
				}
				for(Horse horse : player.horseBag.horses){
					for(GameItem item : horse.equs.equs){
						if(item!=null && item.template!=null && item.template.isEquipment() && illegalItems.get(item.template.id)!=null){
							horse.equs.unequip(item.template.id, item.instanceId, player);
						}
					}
				}
				for(TransactionBagGrid grid : player.bag.getGrids()){
					GameItem item = grid.getItem();
					if(item!=null && item.template!=null && illegalItems.get(item.template.id)!=null){
						removeItems.add(item.template.id);
					}
				}
				if(removeItems.size()>0){
					for(int itemId : removeItems){
						tx = player.newTransaction("ILLEGALITEM");
						try {
							player.bag.removeGameItemIngoreInstanceId(itemId, 1, tx, false);
							tx.commit();
							trustPlayers.put(player.id, 0);
						} catch (Exception e) {
							tx.rollback();
						}
					}
				}
				for(TransactionBagGrid grid : player.depot.getGrids()){
					GameItem item = grid.getItem();
					if(item!=null && item.template!=null && illegalItems.get(item.template.id)!=null){
						removeItems.add(item.template.id);
					}
				}
				if(removeItems.size()>0){
					for(int itemId : removeItems){
						tx = player.newTransaction("DEPOTILLEGALITEM");
						try {
							player.depot.removeGameItemIngoreInstanceId(itemId, 1, tx, false);
							tx.commit();
							trustPlayers.put(player.id, 0);
						} catch (Exception e) {
							tx.rollback();
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}

}
