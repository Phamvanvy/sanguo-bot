package peony.service.read;

import java.text.MessageFormat;

import com.pip.sanguo.data.BookConfig;
import com.pip.sanguo.data.BookChapter;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.VIP.VipPrivilegeService;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopService;

public class PayForReadCall extends ClientSessionAsyncCall implements NoItemShopBuyI{
	
	protected int serial;
	protected int bookId;
	protected Player player = null;
	protected int leftTime = 0;
	protected Book b = null;
	BookConfig bc = null;
	BookChapter bookChapter = null;
	public static int DECK_TIME = 60*60*1000; //每次付费缩短阅读时间

	public PayForReadCall(Packet packet,ClientSession session) {
		super(session);
		this.serial = packet.getInt();
		this.bookId = packet.getInt();
		player = (Player)session.getClient();
		b = player.book;
		bc = BookUtil.getBookConfig(bookId);
		bookChapter = BookUtil.getBookChapter(b.chapter, bc);
		
	}

	public void process(Object[] o) {
		addToClientSession();
		
	}

	public void procssFail(Object[] o) {
		long lastTime = bookChapter.time*60*1000l;
		long time = System.currentTimeMillis() - b.startReadTime + b.alreadyRead;
		leftTime = (int)((lastTime - time)/(60*1000l));
		error(null, "没有足够元宝");
		addToClientSession();
	}

	public void callFinish() throws Exception {
		if(success){
			b.payTimes++;
			bookChapter = BookUtil.getBookChapter(b.chapter, bc);
			long lastTime = bookChapter.time*60*1000l;
			b.alreadyRead += DECK_TIME; 
			long time = System.currentTimeMillis() - b.startReadTime + b.alreadyRead;
			leftTime = (int)((lastTime - time)/(60*1000l));
			if(leftTime<=0){
				bookChapter = BookUtil.getBookChapter(b.chapter+1, bc);
				if(bookChapter != null){
//					b.chapter ++;
					leftTime =bookChapter.time;
					if(player.vipLevel>=2){
				        leftTime = (int) (bookChapter.time*(1-VipPrivilegeService.BOOK_DECTIME_RATIO));
				   }
				}else{
					leftTime = 0;
				}
				b.alreadyRead = 0;
				b.onRead = Book.STATE_UNREAD;
				player.book = null;
				Packet pt = new Packet(OpCode.PLAYER_READBOOK_SERVER);
				pt.putInt(-1);
				pt.putInt(b.getId());
				pt.putInt(b.chapter);
				pt.putInt(leftTime);
				pt.put(bookChapter == null?3:b.onRead);
				pt.putString(b.getPropertyName(bc));
				player.send(pt);  
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PLAYER_PAYFORREDA_CLIENT, "升级成功");
			}
			Packet pt = new Packet(OpCode.PLAYER_PAYFORREDA_SERVER);
			pt.putInt(serial);
			int cnt = pow(b.payTimes);
			int price = (int)(Server.server.getServiceRegistry().getShopService().getItemPrice(NoItemShopBuy.LIUSHIYUANBAO)/36);
			int Yb = (int)price*cnt;
			if(Yb>=588888){
				cnt = (int)(588888/price);
			}
			pt.putString(String.valueOf(cnt*price));
		    pt.putInt(leftTime);
		    player.send(pt);
		}
	}

	public void run() {
		if(player!=null){
			if(b==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PLAYER_PAYFORREDA_CLIENT, "当前没有正在阅读的书籍");
				return;
			}
			if(b.onRead == Book.STATE_UNREAD){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PLAYER_PAYFORREDA_CLIENT, "当前没有正在阅读这本书籍");
				return;
			}
			bookChapter = BookUtil.getBookChapter(b.chapter+1, bc);
			if(bookChapter==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PLAYER_PAYFORREDA_CLIENT, "已满级");
				return;
			}
			LogUtil.logPayForBookTry(player, b, "DECONHOUR");
			ShopService service = Server.server.getServiceRegistry().getShopService();
			try{
				int shopId = service.getShopByItemId(NoItemShopBuy.LIUSHIYUANBAO).id;
				int count = pow(b.payTimes);
				//所需i币金额越界，超过按照588888扣费
				float price = service.getItemPrice(NoItemShopBuy.LIUSHIYUANBAO)/36;
				int Yb = (int)price*count;
				if(Yb>=588888){
					count = (int)(588888/price);
				}
				NoItemShopBuy ibuy = new NoItemShopBuy(player,serial,shopId,NoItemShopBuy.LIUSHIYUANBAO,count,this,null);
				service.buy(player, ibuy);
				LogUtil.logPayForBookSuccess(player, b, "DECONHOUR");
			}catch(Exception e){
				LogUtil.logPayForBookFail(player, b, "DECONHOUR");
				ErrorHandler.sendErrorMessage(session, serial, OpCode.PLAYER_PAYFORREDA_CLIENT, peony.Messages.STRING_00911);
				return;
			}
		}
	}
	
	public synchronized static int pow(int n){
		if(n>1){
			int ret=1;
			for(int i=1;i<n;i++){
				ret = ret*2;
			}
			return ret;
		}
		return 1;
	}
}
