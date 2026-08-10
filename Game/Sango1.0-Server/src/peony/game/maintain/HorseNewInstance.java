package peony.game.maintain;

import java.util.List;

import org.apache.log4j.Logger;

import peony.auction.Auction;
import peony.auction.AuctionService;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.LogUtil;
import peony.game.Mail;
import peony.game.Player;
import peony.game.Server;
import peony.game.TransactionBagGrid;
import peony.game.mail.ItemMailAttachment;
import peony.game.mail.MailAttachment;

public class HorseNewInstance {
	
	private static final Logger log = Logger.getLogger(HorseNewInstance.class);
	
	private int len = 10;
	
	public void launch(int start,int step){
		log.info("HorseNewInstance Start.");
		len = step;
		while(true){
			List<Player> l = Server.server.getServiceRegistry().getDbService().playerDAO.getPlayers(start, len);
			log.info("process:"+start);
			for(Player p:l){
				try{
					process(p);
				}catch(Exception ex){
					log.info("error:"+p.id);
					log.error(ex,ex);
				}
			}
			start+=len;
			if(l.size()<len){
				break;
			}
		}
		log.info("HorseNewInstance End.");
		start = 0;
		while(true){
			List<Mail> l = Server.server.getServiceRegistry().getDbService().mailDAO.getMails(start, len);
			log.info("process mail:"+start);
			for(Mail m:l){
				try{
					process(m);
				}catch(Exception ex){
					log.info("errormail:"+m.getId());
					log.error(ex,ex);
				}
			}
			start += len;
			if(l.size()<len)
				break;
		}
		for(Auction a:Server.server.getServiceRegistry().getAuctionService().cache){
			process(a);
		}
	}
	
	protected void process(Auction a){
		log.info("process auction "+a.getId());
		GameItem item = a.getItem();
		if(item!=null&&item.object!=null&&item.object instanceof Horse){
			((Horse)item.object).instanceId = Server.server.getServiceRegistry()
			.getSleepyCatService().generatorHorseId();
			if(a.getCount()>1){
				log.info("[AUCTIONHORSEERROR]MAIL["+a.getId()+"]SOURCE["+a.getPlayerId()+"]"+LogUtil.getGameItemString(item, a.getCount()));
				a.setCount(1);
			}
			Server.server.getServiceRegistry().getDbService().auctionDAO.updateEntity(a);
		}
	}
	
	protected void process(Mail m){
		log.info("process mail "+m.getId());
		MailAttachment att = m.getAttachment();
		if(att!=null&&att instanceof ItemMailAttachment){
			ItemMailAttachment it = (ItemMailAttachment)att;
			if(it.getGameItem()!=null){
				GameItem item = it.getGameItem();
				if(item.object!=null&&item.object instanceof Horse){
					((Horse)item.object).instanceId = Server.server.getServiceRegistry()
					.getSleepyCatService().generatorHorseId();
					if(it.getCount()>1){
						log.info("[MAILHORSEERROR]MAIL["+m.getId()+"]SOURCE["+m.getSourceId()+"]DEST["+m.getDestId()+"]"+LogUtil.getGameItemString(item, it.getCount()));
						it.setCount(1);
					}
					Server.server.getServiceRegistry().getDbService().mailDAO.updateEntity(m);
				}
			}
		}
	}
	
	protected void process(Player p) {
		log.info("process " + p.id);
		for (Horse h : p.horseBag.horses) {
			h.instanceId = Server.server.getServiceRegistry()
					.getSleepyCatService().generatorHorseId();
			for (GameItem item : h.equs.equs) {
				if (item != null && item.bindInstance != -1) {
					item.bindInstance = 0;
				}
			}
		}
		List<TransactionBagGrid> grids = p.bag.getGrids();
		for (TransactionBagGrid grid : grids) {
			if (grid != null && grid.getItem() != null) {
				GameItem item = grid.getItem();
				if (item.bindInstance != -1 && item.bindInstance != 0) {
					item.bindInstance = 0;
				}
				if (item.object != null && item.object instanceof Horse) {
					Horse h = (Horse) item.object;
					h.instanceId = Server.server.getServiceRegistry()
							.getSleepyCatService().generatorHorseId();
				}
			}
		}
		Server.server.getServiceRegistry().getDbService().playerDAO.updateEntity(p);
	}
}
