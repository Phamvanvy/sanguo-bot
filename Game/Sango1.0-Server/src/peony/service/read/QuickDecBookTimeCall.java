package peony.service.read;

import java.text.MessageFormat;

import com.pip.sanguo.data.BookChapter;
import com.pip.sanguo.data.BookConfig;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.VIP.VipPrivilegeService;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopService;

public class QuickDecBookTimeCall extends ClientSessionAsyncCall implements NoItemShopBuyI{
	
	protected int serial;
	protected int bookId;
	protected Player p = null;
	protected int price = 0;
	protected Book b = null;
	protected int cnt = 0;
	BookConfig bc = null;
	BookChapter bookChapter =null;

	public QuickDecBookTimeCall(Packet packet,ClientSession session) {
		super(session);
		this.serial = packet.getInt();
		this.bookId = packet.getInt();
		p = (Player)session.getClient();
		bc = BookUtil.getBookConfig(bookId);
	}

	public void process(Object[] o) {
	    bookChapter = BookUtil.getBookChapter(b.chapter+1, bc);
	    int leftTime = 0;
	    if(bookChapter!=null){
//	       b.chapter ++;
	       leftTime = bookChapter.time;;
		   if(p.vipLevel>=2){
		        leftTime = (int) (bookChapter.time*(1-VipPrivilegeService.BOOK_DECTIME_RATIO));
		   }
	    }else{
	    	leftTime = 0;
	    }
        b.alreadyRead = 0;
        b.onRead = Book.STATE_UNREAD;
        p.book = null;
        if(bc.auto==1){
		    cnt++;
		    p.pool.setInt(Player.PROPERTY_PAYFORBOOK_LASTTIME,cnt);
        }
	    Packet pt = new Packet(OpCode.PLAYER_READBOOK_SERVER);
		pt.putInt(-1);
		pt.putInt(b.getId());
		pt.putInt(b.chapter);
		pt.putInt(leftTime);
		pt.put(bookChapter == null?3:b.onRead);
		pt.putString(b.getPropertyName(bc));
		p.send(pt);  
	    addToClientSession();
	}

	public void procssFail(Object[] o) {
		error(peony.Messages.STRING_00924);
		addToClientSession();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.QUICKDEC_BOOKTIME_SERVER);
			pt.putInt(serial);
		    p.send(pt);
		}
	}

	public void run() {
		if(p!=null){
			b = p.book;
			if(b!=null){
				if(b.getId() != bookId){
				    ErrorHandler.sendErrorMessage(session, serial, OpCode.QUICKDEC_BOOKTIME_ClIENT, "当前没在阅读");
				    return;
				}
				bookChapter = BookUtil.getBookChapter(b.getLevel(), bc);
				if(bc!=null && bookChapter!=null){
					long timeLast = bookChapter.time;
					long al = Time.currentTimeMillis(Time.currTime) - b.startReadTime + b.alreadyRead;
					int leftTime = (int)((timeLast*60*1000l-al)/(60*1000l));
					cnt = p.pool.getInt(Player.PROPERTY_PAYFORBOOK_LASTTIME,1);
					if(leftTime ==0){
						leftTime = 1;
					}
					int count = cnt*leftTime;
					LogUtil.logPayForBookTry(p, b, "QUICKDEC");
					ShopService service = Server.server.getServiceRegistry().getShopService();
					try{
						if(bc.auto!=1){
							leftTime = (int)(Math.ceil(b.startReadTime -System.currentTimeMillis())/(60*1000l));
							int z = leftTime/60;
							if(leftTime%60!=0 || leftTime == 0){
								z++;
							}
							count = z*3;
						}
						int shopId = service.getShopByItemId(NoItemShopBuy.BOOKYIYUANBAO).id;
						NoItemShopBuy ibuy = new NoItemShopBuy(p,serial,shopId,NoItemShopBuy.BOOKYIYUANBAO,count,this,null);
						service.buy(p, ibuy);
						LogUtil.logPayForBookSuccess(p, b, "QUICKDEC");
					}catch(Exception e){
						LogUtil.logPayForBookFail(p, b, "QUICKDEC");
						ErrorHandler.sendErrorMessage(session, serial, OpCode.QUICKDEC_BOOKTIME_ClIENT, peony.Messages.STRING_00911);
						return;
					}
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.QUICKDEC_BOOKTIME_ClIENT, "请求已超时");
				return;
			}
		}
	}

}
