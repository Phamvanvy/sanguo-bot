package peony.auction;

import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.game.GameItem;
import peony.game.Player;
import peony.game.ErrorHandler;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.game.OpCode;
import peony.game.mail.MailService;

/**
 * ÅÄÂôÐÐ³·µ¥²Ù×÷
 * @author pmeng
 * 
 */
public class AuctionDeleteCall extends ClientSessionAsyncCall{
	protected final Logger log = Logger.getLogger(AuctionDeleteCall.class);
	
	protected int serial;
	protected int auctionID;
	protected Auction auction;
	protected Player player;
	
	public AuctionDeleteCall(ClientSession session,Packet packet,Player player){
		super(session);
		this.serial = packet.getInt();
		this.auctionID = packet.getInt();
		this.player = player;
	}

	public void callFinish() throws Exception {
		if (success) {
			Packet pt = new Packet(OpCode.AUCTION_DELETE_SERVER);
			pt.putInt(serial);
			pt.putInt(auctionID);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.AUCTION_DELETE_CLIENT, peony.Messages.STRING_00407);
		}
	}

	public void run() {
		if(player != null){
			AuctionService auctionService = null;
			try {
				auctionService = Server.server.getServiceRegistry().getAuctionService();
			} catch (Exception e1) {
				e1.printStackTrace();
				error(peony.Messages.STRING_00408);
				addToClientSession();
				return;
			}
			synchronized (auctionService) {
				auction = auctionService.getFromCache(this.auctionID);
				if(auction==null){
					error(peony.Messages.STRING_00408);
					addToClientSession();
					return;
				}else{
					if(auction.getPlayerId() == player.id){
						GameItem item = auction.getItem();
						int count = auction.getCount();
						if(auction.getLastPlayerId() == -1 && item != null){
							PlayerTransaction tx = player.newTransaction("AUCDEL");
							try{
								player.bag.addGameItemComplete(item, count, tx, true);
								tx.commit();
							}catch(Exception e){
								tx.rollback();
								MailService mailService = Server.server.getServiceRegistry().getMailService();
								mailService.sendSystemMailAsync(player.id, peony.Messages.STRING_00004, peony.Messages.STRING_00409, "", 0, item, count, "AUCDEL");
							}
							auctionService.deleteAuction(auction);
							log.info("[AUCTIONDELSUC]PLAYEID[" + player.id + "]AUCTIONID[" + auction.getId() + "]");
						}else{
							error(peony.Messages.STRING_00410);
						}
					}else{
						error(peony.Messages.STRING_00411);
					}
				}
			}
		}else{
			error(null, peony.Messages.STRING_00412);
		}
		addToClientSession();
	}
}
