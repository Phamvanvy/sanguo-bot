package peony.service.pluginstance;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.CommonUtil;
import peony.game.Creature;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapUtil;
import peony.game.instance.NormalVMapManager;
import peony.game.mail.MailService;
import peony.service.Service;

/**
 * 梦幻打地鼠
 * @author dchen
 */
public class MouseInstanceService implements Service {

	public static int mapId = 1616;
	public static int maxcreatures = 50;
	public static int winnerItem = 3625;
	protected List<Mouse> mouses = new ArrayList<Mouse>();
	protected ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
	protected Map<Integer, Map<Integer, Long>> npcRfreshTime = new HashMap<Integer, Map<Integer,Long>>();
	protected long lastUpdateTime;
	protected Map<Integer, Boolean> hasGetReward = new HashMap<Integer, Boolean>();
	protected static int[] quitNpc = {6619147,6619148,6619149,6619150,6619151,6619152,6619153,6619154,6619155,6619156};

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("Areas/mousebattle.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) throws Exception{
		Element root = doc.getRootElement();
		List<Element> towerList = root.elements("tower");
		for(Element el : towerList){
			int towerId = Integer.parseInt(el.attributeValue("toweId"));
			int npcId = Integer.parseInt(el.attributeValue("npcId"));
			int refreshTime = Integer.parseInt(el.attributeValue("refreshTime"));
			int x = Integer.parseInt(el.attributeValue("x"));
			int y = Integer.parseInt(el.attributeValue("y"));
			Mouse mouse = new Mouse(towerId, npcId, refreshTime, x ,y);
			mouses.add(mouse);
		}
	}
	
	public void update(){
		if(System.currentTimeMillis()-lastUpdateTime>5000){
			NormalVMapManager manager = (NormalVMapManager) Server.server.getWorld().getVMapManager(mapId);
			List<VMap> maps = manager.getMaps(mapId);
			for(VMap map : maps){
				if(map!=null && (map.getPlayersByFaction(GameObject.FACTION_WEI).size()>0 || 
						map.getPlayersByFaction(GameObject.FACTION_SHU).size()>0 || 
						map.getPlayersByFaction(GameObject.FACTION_WU).size()>0)){
					for(Mouse mouse : mouses){
						if(mouse!=null){
							if(map.getCreatureById(mouse.towerId)!=null && 
									map.getCreatureById(mouse.towerId).isVisible() && 
									map.getAllCreatures().size()<maxcreatures){
								Map<Integer, Long> refreshTime = npcRfreshTime.get(map.instance.getId());
								if(refreshTime==null || refreshTime.get(mouse.npcId)==null || System.currentTimeMillis()-refreshTime.get(mouse.npcId)>mouse.refreshTime*1000L){
									GameMapObject gmo = GameMapObject.findByID(proj, mouse.npcId);
									GameObject npc0 = VMapUtil.addCreature(map, mouse.x, 
											mouse.y, (GameMapNPC) gmo, true, 0, null);
									if(refreshTime==null){
										refreshTime = new HashMap<Integer, Long>();
										npcRfreshTime.put(map.instance.getId(), refreshTime);
									}
									refreshTime.put(mouse.npcId, System.currentTimeMillis());
								}
							}
						}
					}
					processWinner(map);
				}
			}
			lastUpdateTime = System.currentTimeMillis();
		}
	}
	
	protected void processWinner(VMap map){
		if(map!=null){
			if(hasGetReward.get(map.instance.getId())!=null && hasGetReward.get(map.instance.getId()))
				return;
			List<Creature> creatures = map.getAllCreatures();
			Map<Integer, Creature> creatureMap = new HashMap<Integer, Creature>();
			for(Creature c : creatures){
				creatureMap.put(c.id, c);
			}
			if(creatureMap.size()<=10){
				for(Creature c : creatureMap.values()){
//					if(c!=null && !(c.name.contains("已废弃的召唤点") || c.name.contains("梦幻打地鼠规则讲解员")))
//						return;
					if(c!=null && !isQuitNpc(c.id))
						return;
				}
				for(int faction=GameObject.FACTION_WEI;faction<=GameObject.FACTION_WU;faction++){
					List<Player> players = map.getPlayersByFaction(faction);
					for(Player player : players){
						if(player!=null){
							PlayerTransaction tx = player.newTransaction("MOUSEINSTANCE");
							GameItem item = ObjectAccessor.createGameItem(winnerItem);
							try {
								player.bag.addGameItemComplete(item, 1, tx, false);
								tx.commit();
							} catch (NoEnoughSpaceException e) {
								tx.rollback();
								MailService service = Server.server.getServiceRegistry().getMailService();
								service.sendSystemMail(player.id, peony.Messages.STRING_00004, peony.Messages.STRING_01195, "", 0, item, 1, "MOUSEINSTANCE");
							}
							Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, 
									peony.Messages.STRING_01750);
//							try {
//								player.goMap(map.getRelivePoint(player.faction)[0], map.getRelivePoint(player.faction)[1], map.getRelivePoint(player.faction)[2]);
//							} catch (VMapException e) {
//								
//							}
						}
					}
				}
				hasGetReward.put(map.instance.getId(), true);
			}
		}
	}
	
	public boolean isQuitNpc(int npcId){
		for(int npc : quitNpc){
			if(npc==npcId)
				return true;
		}
		return false;
	}
	
	public void shutdown() {
		
	}
	
	class Mouse{
		
		public int towerId;
		public int npcId;
		public int refreshTime;
		public int x;
		public int y;
		
		public Mouse(int towerId, int npcId, int refreshTime, int x, int y){
			this.towerId = towerId;
			this.npcId = npcId;
			this.refreshTime = refreshTime;
			this.x = x;
			this.y = y;
		}
		
	}
		
}
