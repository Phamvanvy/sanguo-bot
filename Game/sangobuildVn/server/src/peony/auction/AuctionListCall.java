package peony.auction;

import java.text.SimpleDateFormat;

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
 * 查看详细信息 
 * serial 		int 	
 * type 		int 		物品类型 0为武器,1为防具,2为饰品,3为普通物品类型 
 * quality 		int 		物品的品质
 * level 		int 		物品的等级 
 * name 		String 		物品名字 
 * sortfeild 	int 		排序号 1为名称排序，2为当前价格排序，3为结时间排束序 
 * pageNum 		int 		页号
 * amount 		int 		每页显示条数 
 * asc 			int 		0为升序排列，1为降序排列 
 * public static final short AUCTION_LIST_CLIENT = 479;
 * 
 * 查看详细信息返回结果 
 * serial 			int 
 * pageamount 		int	 		数据的总页数 
 * amount			int			数据总条数
 * pageNum			int			页数
 * articleamount	int			本页实际条数
 * 循环N次
 * 	auctionId 		int 		拍卖行ID 
 * 	item 			byte[] 		物品信息 
 * 	itemamount		int			物品数量
 * 	startPrice		int			起拍价
 * 	currentPrice 	int 		当前价格
 * 	endPrice		int			一口价
 * 	playername 		String 		拍卖者名字 
 *  validtime		String		结束时间
 * public static final short AUCTION_LIST_SERVER = 480;
 */

public class AuctionListCall extends ClientSessionAsyncCall {

	protected final Logger log = Logger.getLogger(AuctionBuyCall.class);
	private AuctionResult result = null;
	private Auction[] auctions = null;
	private GameObjectRef playerRef;
	protected int serial;
	protected int endPrice;
	protected int type;
	protected int leveldown;
	protected int levelup;
	protected int quality;
	protected String name;
	protected int sortfeild;
	protected int pageNum;
	protected int amount;
	protected int asc;

	public AuctionListCall(ClientSession session, Packet packet, Player player) {
		super(session);
		this.playerRef = player.ref();
		this.serial = packet.getInt();
		this.type = packet.getInt();
		this.quality = packet.getInt();
		this.leveldown = packet.getInt();
		this.levelup = packet.getInt();
		this.name = packet.getString();
		
		this.sortfeild = packet.getInt();
		this.pageNum = packet.getInt();
		this.amount = packet.getInt();
		this.asc = packet.getInt();
	}

	public void callFinish() throws Exception {
		Player p = ObjectAccessor.getPlayer(playerRef.id);
		if (success) {
			Packet pt = new Packet(OpCode.AUCTION_LIST_SERVER);
			pt.putInt(serial);
			pt.putInt(result.getPageAmount());
			pt.putInt(result.getTotal());
			pt.putInt(pageNum);
			pt.putInt(result.getArticleamount());
			for (Auction auction : this.auctions) {
				if(auction != null){
				pt.putInt(auction.getId());
				pt.put(auction.getItem().toClientBytes());
				pt.putInt(auction.getCount());
				pt.putInt(auction.getStartPrice());
				pt.putInt(auction.getCurrentPrice());
				pt.putInt(auction.getEndPrice());
				pt.putString(auction.getPlayerName());
				//格式化日期
				SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
				String validTime = format.format(auction.getValidTime());
				pt.putString(validTime);
				}
			}
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.AUCTION_LIST_SERVER, errorMessage);
		}
	}

	public void run() {
		AuctionService auctionService = null;
		Player p = ObjectAccessor.getPlayer(playerRef.id);
		try {
			auctionService = Server.server.getServiceRegistry().getAuctionService();//new AuctionService(new AuctionDAO());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if(p != null){
			if (sortfeild != 1 && sortfeild != 2 && sortfeild != 3) {
				error(null, "Đoạn chữ vô hiệu");
			} else if (asc != 0 && asc != 1) {
				error(null, "Phương thức tăng thứ tự vô hiệu");
			} else if(pageNum <= 0){
				error(null, "Số trang vô hiệu");
			}else if(amount <= 0){
				error(null, "无效的每页显示条数");
			}else {
				try {
					this.result = auctionService.getAuctions(type, quality, leveldown, levelup, name, sortfeild, asc, pageNum, amount,p.id);
				} catch (AuctionException e) {
					error(e,e.getMessage());
				}
				this.auctions = result.getAuctions();
				if (this.auctions == null) {
					error(null, "Không có thông tin bán đầu giá phù hợp điều kiện");
				}
			}
		}
		addToClientSession();
	}
}
