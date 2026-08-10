package peony.service;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.Player;
import peony.game.Server;

public class ItemTrackService implements Service {

	protected Map<Integer, ItemTrack> itemTracks = new HashMap<Integer, ItemTrack>();
	
	private static final Logger log = Logger.getLogger(ItemTrackService.class);
	
	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("itemtrack.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		try {
			Element root = doc.getRootElement();
			List<Element> list = root.elements("track");
			if(list!=null && list.size()>0){
				for(Element el : list){
					int id = Integer.parseInt(el.attributeValue("id"));
					ItemTrack itemTrack = new ItemTrack(id);
					List<Element> list1 = el.elements("item");
					for(Element e : list1){
						int itemId = Integer.parseInt(e.attributeValue("itemId"));
						int totle = Integer.parseInt(e.attributeValue("totle"));
						itemTrack.addIT(itemId, totle);
					}
					itemTracks.put(id, itemTrack);
				}
			}
		} catch (NumberFormatException e) {
			log.error(e, e);
		}
	}
	
	public ItemTrack getItemTrackById(Player player){
		if(player!=null){
			for(ItemTrack it : itemTracks.values()){
				if(it.id==0){
					if(player.map!=null && player.map.id==1488)
						return it;
					return null;
				}
			}
		}
		return null;
	}

	public void shutdown() {
		
	}

}
