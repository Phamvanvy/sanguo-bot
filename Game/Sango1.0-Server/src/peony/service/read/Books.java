package peony.service.read;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import com.pip.sanguo.data.BookConfig;
import com.pip.sanguo.data.BookChapter;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PropertyCalculator;
import peony.game.PropertyEnhancer;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.VIP.VipPrivilegeService;
import peony.service.shop.NoItemShopBuy;


public class Books {
	private static Logger log = Logger.getLogger(Books.class);
	public Map<Integer,Book> books = new HashMap<Integer,Book>();
	protected Unit owner;
	public static int LEVELLIMIT = 30;//玩家阅读等级限制
	public static int PAYLIMIT = 4;//使用立即完成功能限制
	
	public static int BOOK_TIMELIMIT = 5; //每天可读次数

	public Books(Unit owner) {
		this.owner = owner;
	}
	
	@Override
	public Books clone(){
		Books b = new Books(owner);
		for(Book bk:books.values()){
			b.books.put(bk.getId(), bk);
		}
		return b;
	}
	
	public Book getBookById(int bookId){
		return books.get(bookId);
	}
	
	public void addBook(Book book){
		books.put(book.id, book);
	}
	
	/**
	 * 开始阅读
	 * @param bookId
	 * @throws Exception
	 */
	public void readBook(int bookId,BookConfig bc)throws Exception{
		Book b = owner.book;
		if(b!=null){
			if(b.getId() == bookId){
			    throw new Exception("时间还没结束，请不要重复阅读");
			} else {
				throw new Exception("您现在正在阅读书籍，请等待时间结束后，再阅读其他书籍");
			}
		}
		Book newBook = getBookById(bookId);
		if(newBook!=null){
			if(newBook.chapter>0 && newBook.onRead == Book.STATE_UNREAD){
				BookChapter bookChapter = BookUtil.getBookChapter(newBook.chapter+1, bc);
				if(bookChapter == null){
				   throw new Exception("已经满级了");
				}
			}
			if(bc.auto==0){
				if(((Player)owner).readBookCount>=Books.BOOK_TIMELIMIT){
					throw new Exception("读书切忌贪多，应细细体会。每日可阅5章");
				}
			}
			newBook.chapter++;
			owner.refreshProperties(false);
			newBook.onRead = Book.STATE_READ;
			owner.book = newBook;
			BookChapter bookLevel = BookUtil.getBookChapter(newBook.chapter, bc);
			if(bc.auto == 1){
				newBook.startReadTime = System.currentTimeMillis();
				if(((Player)owner).vipLevel>=2)
			          newBook.startReadTime = (long) (System.currentTimeMillis()-bookLevel.time*60*1000l*VipPrivilegeService.BOOK_DECTIME_RATIO);
			}else{
				newBook.startReadTime = System.currentTimeMillis()+bookLevel.time*60*1000l;
				if(((Player)owner).vipLevel>=2)
				    newBook.startReadTime = (long) (System.currentTimeMillis()+bookLevel.time*60*1000l*(1-VipPrivilegeService.BOOK_DECTIME_RATIO));
				Player player = (Player)owner;
				if(player!=null){
					player.readBookCount++;
					player.pool.setInt(Player.PROPERTY_READBOOK_COUNT, player.readBookCount);
				}
			}
		}
	}
	
	/**
	 * 暂停阅读
	 * @param bookId
	 * @throws Exception
	 */
	public void pauseReadBook(int bookId)throws Exception{
		Book b = owner.book;
		if(b!=null){
			if(b.getId() != bookId){
			    throw new Exception("您当期没有阅读该书籍");
			}
			b.onRead = Book.STATE_PAUSE;
			b.alreadyRead += System.currentTimeMillis() - b.startReadTime;
			owner.book = null;
		}
	}
	
	/**
	 * 立即完成费用申请
	 * @param session
	 * @param packet
	 */
	public void decBookTimePay(ClientSession session,Packet packet){
		int serial = packet.getInt();
		int bookId = packet.getInt();
		Book b = owner.book;
		Player p = (Player)owner;
		if(b!=null){
			if(b.getId() != bookId){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.BOOKTIME_PAY_ClIENT, "您当期没有阅读该书籍");
				return;
			}
			BookConfig bc = BookUtil.getBookConfig(bookId);
			BookChapter bookChapter = BookUtil.getBookChapter(b.getLevel(), bc);
			int timesNow = p.pool.getInt(Player.PROPERTY_PAYFORBOOK_LASTTIME,1);
			if(PAYLIMIT-timesNow<=0 && bc.auto==1){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.BOOKTIME_PAY_ClIENT, "您今天所有次数已用完，请明天再来使用");
				return;
			}
			if(bc.auto==0 && p.readBookCount>=Books.BOOK_TIMELIMIT){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.BOOKTIME_PAY_ClIENT, "您今天所有次数已用完，请明天再来使用");
				return;
			}
			int price = 0;
			if(bc!=null && bookChapter!=null){
				long timeLast = bookChapter.time;
				long al = Time.currentTimeMillis(Time.currTime) - b.startReadTime + b.alreadyRead;
				int leftTime = (int)((timeLast*60*1000l-al)/(60*1000l));
				int itemPrice = (int)(Server.server.getServiceRegistry().getShopService().getItemPrice(NoItemShopBuy.BOOKYIYUANBAO)/36);
				int payTime = -1;
				if(bc.auto==1){
				   if(leftTime == 0){
					   leftTime = 1;
				   }
				   price = itemPrice*timesNow*leftTime;
				   payTime = PAYLIMIT-timesNow;
				}else{
					leftTime = (int)(Math.ceil(b.startReadTime -System.currentTimeMillis())/(60*1000l));
					int z = leftTime/60;
					if(leftTime%60!=0 || leftTime ==0){
						z++;
					}
					price = z*3*itemPrice;
				}
				Packet pt = new Packet(OpCode.BOOKTIME_PAY_SERVER);
				pt.putInt(serial);
				pt.putString(String.valueOf(price));
				pt.putInt(payTime);
				p.send(pt);
			}
		}
	}
	
	
	public Book getBookReadNow(){
		for(Book b : books.values()){
			if(b.onRead == Book.STATE_READ){
				return b;
			}
		}
		return null;
	}
	
	public void processBookRead(){
		Book b = getBookReadNow();
		Player p = (Player)owner;
		if(b!=null){
			owner.book = b;
			checkDue(b);
		}
		p.bookDay = p.pool.getInt(Player.PROPERTY_PAYFORBOOK_DAYTIME,0);
		if(p.bookDay != Time.day){
			p.bookDay = Time.day;
			p.pool.setInt(Player.PROPERTY_PAYFORBOOK_DAYTIME,Time.day);
			p.pool.setInt(Player.PROPERTY_PAYFORBOOK_LASTTIME, 1);
		}
	}
	
	public void checkDue(Book b){
		Player p = (Player)owner;
		int leftTime = 0;
		BookConfig bc = BookUtil.getBookConfig(b.getId());
		BookChapter chapter = BookUtil.getBookChapter(b.chapter, bc);
		long time = Time.currentTimeMillis(Time.currTime)-b.startReadTime+b.alreadyRead;
		long timeLast = chapter.time*60*1000l;
		if(bc.auto == 0){
			time = b.startReadTime - System.currentTimeMillis();
		}
		if((time-timeLast>0 && bc.auto==1)||(time<=0 && bc.auto==0)){
		  b.alreadyRead = 0;
		  chapter = BookUtil.getBookChapter(b.chapter+1, bc);
		  if(chapter!=null){
			  b.chapter ++;
			  leftTime = chapter.time;
		  }
		  b.alreadyRead = 0;
		  b.onRead = Book.STATE_UNREAD;
		  owner.book = null;
		  owner.refreshProperties(false);
		  Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, MessageFormat.format("您当前《{0}》书籍可以升级了，赶快打开看看吧！", bc.getTitle()));
		  Packet pt = new Packet(OpCode.PLAYER_READBOOK_SERVER);
		  pt.putInt(-1);
		  pt.putInt(b.getId());
		  pt.putInt(b.chapter);
		  pt.putInt(leftTime);
		  pt.put(chapter==null?3:b.onRead);
		  pt.putString(b.getPropertyName(bc));
		  p.send(pt);  
		}else{
			leftTime = (int)((timeLast-time)/(60*1000l));
			if(bc.auto==0){
				leftTime = (int)(time/(60*1000l));
			}
			int hour = leftTime/60;
			int minute = leftTime%60;
			String msg = MessageFormat.format("当前《{0}》书籍还有{1}小时{2}分可以升级，您可以立即完成来减少等待的痛苦！",bc.getTitle(),hour,minute);
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, msg);
		}
   }
	
	/**
	 * 秘籍调书籍等级
	 * @param count
	 */
	public void clearBooks(){
		if(books != null){
			for(Book b : books.values()){
				BookConfig bc = BookUtil.getBookConfig(b.getId());
				b.chapter = bc.upLimit;
				b.alreadyRead = 0;
				b.onRead = Book.STATE_UNREAD;
			}
			owner.book = null;
		}
	}
	
//	public void bookLevelUpPacket(Player p,Book b,int leftTime){
//		Packet pt = new Packet(OpCode.BOOK_LEVELUP_SERVER);
//		pt.putInt(b.id);
//		pt.putInt(leftTime);
//		BookConfig bc = BookUtil.getBookConfig(b.id);
//		BookChapter chapter = BookUtil.getBookChapter(b.chapter+1, bc);
//		b.chapter++;
//		b.startReadTime = Time.currentTimeMillis(Time.currTime);
//		owner.refreshProperties(false);
//		pt.put(b.onRead);
//		p.send(pt);
//	}
	
	/**
	 * 计算书籍对人物属性的影响。
	 * @param pc 人物属性计算环境
	 */
	public void enhance(PropertyCalculator pc) {
		if(books.size()>0){
			for (Book book : books.values()) {
				if(pc.unit instanceof Player)
					if (book instanceof PropertyEnhancer && book.chapter>0) {
						((PropertyEnhancer) book).enhance(pc);
					}
			}
		}
	}
	
	public byte[] toDBBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(1);//version
			dos.writeShort(books.size());
			for(Book b:books.values()){
				dos.writeInt(b.getId());
				dos.writeInt(b.chapter);
				dos.writeLong(b.alreadyRead);
				dos.writeLong(b.startReadTime);
				dos.writeByte(b.onRead);
				dos.writeInt(b.payTimes);
			}
		}catch(Exception e){
			
		}
		return baos.toByteArray();
	}
	
	public static Books fromDBBytes(byte[] bytes,Player owner){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		Books bs = new Books(owner);
		try{
			int version = dis.read();
			if(version == 1){
				int size = dis.readShort();
				for(int i=0;i<size;i++){
					int id = dis.readInt();
				    int level = dis.readInt();
				    long alreadyRead = dis.readLong();
				    long startReadTime = dis.readLong();
				    byte onRead = dis.readByte();
				    int payTimes = dis.readInt();
				    Book book = new Book();
				    book.id = id;
				    book.chapter = level;
				    book.onRead = onRead;
				    book.alreadyRead = alreadyRead;
				    book.startReadTime = startReadTime;
				    book.payTimes = payTimes;
				    bs.addBook(book);
				}
			}
		}catch(Exception e){
			log.error(e,e);
		}
		return bs;
	}
}
