package peony.auction;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameObjectRef;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
/**
 * serial 		int
 * public static final short AUCTION_PUBLISHIED_CLIENT = 483;
 * 
 * 
	 * 返回本角色最近在拍卖行发布的拍卖信息
	 * serial		int
	 * count1		int			循环次数
	 * 循环N次
	 * 		auctionId		int 		拍卖行ID
	 * 		item			byte[]		物品信息
	 * 		itemamount		int			物品数量
	 * 		startPrice		int			起拍价
	 * 		currentPrice	int			当前价格
	 * 		endPrice		int			一口价 0为一口价交易
	 * 		validtime		String		结束时间
	 * count2		int 		循环次数
	 * 		auctionId		int 		拍卖行ID
	 * 		item			byte[]		物品信息
	 * 		name			String		玩家姓名
	 * 		itemamount		int			物品数量
	 * 		startPrice		int			起拍价
	 * 		currentPrice	int			当前价格
	 * 		endPrice		int			一口价 0为一口价交易
	 * 		validtime		String		结束时间
 *	public static final short AUCTION_PUBLISHIED_SERVER = 484;
 */
public class PublishiedCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(PublishiedCall.class);
	protected int serial;
	protected int playerId;
	protected List<Auction> publishiedAuctions = null;
	protected List<Auction> record;
	private GameObjectRef playerRef;
	public PublishiedCall(ClientSession session,Packet packet, Player player) {
		super(session);
		this.serial = packet.getInt();
		this.playerId = player.id;
		this.playerRef = player.ref();
	}

	public void callFinish() throws Exception {
		if(success){
		Packet p = new Packet(OpCode.AUCTION_PUBLISHIED_SERVER);
		p.putInt(serial);
		if(publishiedAuctions == null){
			p.putInt(0);
		}else{
		p.putInt(publishiedAuctions.size());
		TreeSet<Auction> treeSet = new TreeSet<Auction>(new AuctionComparator());
		for(Auction auction : publishiedAuctions){
			treeSet.add(auction);
		}
		for(Auction auction : treeSet){
			p.putInt(auction.getId());
			p.put(auction.getItem().toClientBytes());
			p.putInt(auction.getCount());
			p.putInt(auction.getStartPrice());
			p.putInt(auction.getCurrentPrice());
			p.putInt(auction.getEndPrice());
			SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
			String validTime = format.format(auction.getValidTime());
			p.putString(validTime);
		}}
		if(this.record == null){
			p.putInt(0);
		}else{
		p.putInt(record.size());
		TreeSet<Auction> treeSet = new TreeSet<Auction>(new AuctionComparator());
		for(Auction auction : record){
			treeSet.add(auction);
		}
		for(Auction auction : treeSet){
			p.putInt(auction.getId());
			p.put(auction.getItem().toClientBytes());
			p.putString(auction.getPlayerName());
			p.putInt(auction.getCount());
			p.putInt(auction.getStartPrice());
			p.putInt(auction.getCurrentPrice());
			p.putInt(auction.getEndPrice());
			SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
			String validTime = format.format(auction.getValidTime());
			p.putString(validTime);
		}}
		session.send(p);
		}else{
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.AUCTION_PUBLISHIED_SERVER, errorMessage);
		}
		
	}

	public void run() {
		AuctionService service = Server.server.getServiceRegistry().getAuctionService();
		try {
			publishiedAuctions = service.getPublishiedAuctionsByPlayerId(playerId);
			this.record = service.getJoinAuctions(ObjectAccessor.getPlayer(playerRef.id));
		} catch (AuctionException e) {
		}catch(Exception e1){
			log.error(e1,e1);
		}
		addToClientSession();
	}

}
