package peony.auction;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObjectRef;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 竞拍请求 
 * serial 		int 
 * auctionID 	int 	拍卖行
 * ID price 	long 	出价
 * public static final short AUCTION_BUY_CLIENT = 481;
 * 竞拍成功
 * serial		int
 * auctionID	int		拍卖行ID
 * currentPrice	int		当前价
 * result 		String 	竞拍结果
 * public static final short AUCTION_BUY_SERVER = 482;
 */
public class AuctionBuyCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(AuctionBuyCall.class);
	protected GameObjectRef playerRef;
	protected Auction auction;
	private Player player;
	protected GameItem gameItem;
	protected int serial;
	protected int auctionID;
	protected int price;

	public AuctionBuyCall(ClientSession session, Packet packet, Player player) {
		super(session);
		this.playerRef = player.ref();
		this.serial = packet.getInt();
		this.auctionID = packet.getInt();
		this.price = packet.getInt();
		this.player = player;
	}

	public void callFinish() throws Exception {
		
		if (success) {
			Packet pt = new Packet(OpCode.AUCTION_BUY_SERVER);
			pt.putInt(serial);
			pt.putInt(auction.getId());
			pt.putInt(auction.getCurrentPrice());
			if(auction.getCurrentPrice() == -1){
				pt.putString("購買成功,請查收飛鴿查看拍賣結果");
			}else{
				pt.putString("出价成功");
			}
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.SHOP_BUY_SERVER, errorMessage);
		}
	}

	public void run() {
		if(price<=0)
			return;
		AuctionService auctionService = null;
		try {
			auctionService = Server.server.getServiceRegistry().getAuctionService();//new AuctionService(new AuctionDAO());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		synchronized(auctionService){
			this.auction = auctionService.getFromCache(this.auctionID);
			try {
				gameItem = this.auction.getItem();
			} catch (Exception e1) {
				error(null, "此拍賣已經不存在");
				addToClientSession();
				return;
			}
			Player p = ObjectAccessor.getPlayer(playerRef.id);
			if(p!=null){
				int currentMoney = p.getMoney();
				if (currentMoney < price) {
					error(null, "沒有足夠的金錢");
					addToClientSession();
				} else {
					try {
						auctionService.buy(auctionID, price, p, gameItem, this.auction.getCount());
					}catch (AuctionException e) {
						error(e, e.getMessage());
					}
					addToClientSession();
				}
			}
		}
	}
	
}
