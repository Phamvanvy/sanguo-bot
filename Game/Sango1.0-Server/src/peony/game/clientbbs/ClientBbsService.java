package peony.game.clientbbs;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import peony.db.ClientBbsDAO;
import peony.game.Admin;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.exp.ExpService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;

public class ClientBbsService implements Service, Runnable {

	private static final Logger log = Logger.getLogger(ClientBbsService.class);

	protected File file;

	public List<ClientBbs> bbs = new ArrayList<ClientBbs>(); // 储存由客服输入的活动项目
	public List<ClientBbs> bbsLater = new ArrayList<ClientBbs>(); // 预储存由客服输入的活动项目
	
	protected int[] min = {1,30,50,55,70};
	
	protected int[] max = {30,50,55,70,100};
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public void startup() throws Exception {
		loadClientBbsFromDb();
		new Thread(this).start();
	}

	public void loadClientBbsFromDb() {
		ClientBbsDAO dao = Server.server.getServiceRegistry().getDbService().clientBbsDao;
		List<ClientBbs> loadedBbs = dao.getClientBbs();
		for(ClientBbs b : loadedBbs){
			if(b.isschedule)
				bbsLater.add(b);
			else{
				bbs.add(b);
			}
		}
//		bbs.addAll(dao.getClientBbs());
	}
	
	// 判断由GM工具输入的等级区间是否正确
	public boolean isInLevel(int minLevel,int maxLevel){
		for(int i = 0;i < min.length;i++){
			if(minLevel == min[i] && maxLevel == max[i]){
				return true;
			}
		}
		return false;
	}

	public void sendBbs(Packet pt, ClientSession session) {
		Player p = (Player) session.getClient();
		int serial = pt.getInt();
		if (p != null) {
			Packet packet = new Packet(OpCode.CLIENTBBS_LOOK_OVER_SERVER);
			packet.putInt(serial);
			boolean hasContent = false;
			for (ClientBbs b : bbs) {
				if (b.in(p.level)) {
					hasContent = true;
					packet.putString((b.explaination == null) ? ""
							: b.explaination);
					String line = b.active;
					List<String> str = new ArrayList<String>();
					List<String> strr = new ArrayList<String>();
					String[] ss = line.split("/");
					for(int i=0;i<ss.length;i++){
						if(i%2==0){
							str.add(ss[i]);
						} else {
							strr.add(ss[i]);
						}
					}
					packet.putShort(str.size());
					for (int i=0;i<str.size();i++) {
						packet.putString(str.get(i));
						packet.putString(strr.get(i));
					}
				}
			}
			if (hasContent == false) {
				packet.putString(peony.Messages.STRING_00770);
				packet.putShort(0);
			}
			ExpService service = Server.server.getServiceRegistry()
					.getExpService();
			int offLineExp = service.getNotOnineExps(p);
			packet.putString(String.valueOf(offLineExp));
			p.send(packet);
	}
}
	
	public synchronized void saveClientBbs(Packet pt, ClientSession session) {
		Admin p = (Admin) session.getClient();
		int serial = pt.getInt();
		if (p != null) {
			String publish = pt.getString();
			Date publishDate = null;
			try {
				publishDate = df.parse(publish);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			int minLevel = pt.getInt();
			int maxLevel = pt.getInt();
			if(!isInLevel(minLevel,maxLevel)){
				ErrorHandler.sendAdminErrorMessage(session, serial, 
						OpCode.ADMIN_SAVECLIENTBBS_CLIENT, peony.Messages.STRING_01989);
				return;
			}
			ClientBbs newbbs = new ClientBbs(minLevel,maxLevel);
			if(publishDate!=null)
				newbbs.isschedule = true;
			newbbs.pulishTime = publishDate;
			Iterator<ClientBbs> it = bbs.iterator();
			if(!newbbs.isschedule){
				while(it.hasNext()){
					ClientBbs b = it.next();
					if(b.minLevel == newbbs.minLevel && b.maxLevel == newbbs.maxLevel && b.enable == true){
						b.obsoleteTime = new Date();
						b.enable = false;
						Server.server.getServiceRegistry().getDbService().clientBbsDao.updateEntity(b);
						it.remove();
					}
				}
			}
			String explaination = pt.getString();
			newbbs.explaination = explaination;
			short size = pt.getShort();
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<size;i++){
				String activeName = pt.getString();
				sb.append(activeName);
				sb.append("/");
				String detailContent = pt.getString();
				sb.append(detailContent);
				if(i!=size-1){
					sb.append("/");
					}
				}
			String str = sb.toString();
			newbbs.active = str;
			if(publishDate==null)
				newbbs.pulishTime = new Date();
			newbbs.enable = true;
			if(!newbbs.isschedule)
				bbs.add(newbbs);
			else{
				Iterator<ClientBbs> iterator = bbsLater.iterator();
				while(iterator.hasNext()){
					ClientBbs bb = iterator.next();
					if(bb.minLevel == newbbs.minLevel && bb.maxLevel == newbbs.maxLevel && bb.enable == true){
						Server.server.getServiceRegistry().getDbService().clientBbsDao.makeTransient(bb);
						iterator.remove();
					}
				}
				bbsLater.add(newbbs);
			}
			Server.server.getServiceRegistry().getDbService().clientBbsDao.makePersistent(newbbs);
			Packet packet = new Packet(OpCode.ADMIN_SAVECLIENTBBS_SERVER);
			packet.putInt(serial);
			p.send(packet);
		}
	}
	
	public boolean isDue(ClientBbs b){
		if(b!=null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(b.pulishTime);
			int day = (cal.get(Calendar.YEAR)<<16)|cal.get(Calendar.DAY_OF_YEAR);
			if(day!=Time.day && b.enable == true){
				return true;
			}
		}
		return false;
	}

	public void shutdown() {
		
	}

	public void run() {
		while(true){
			synchronized(this){
				Iterator<ClientBbs> it = bbsLater.iterator();
				while(it.hasNext()){
					ClientBbs newbbs = it.next();
					if(newbbs.pulishTime.before(Time.currDate)){
						Iterator<ClientBbs> it1 = bbs.iterator();
						while(it1.hasNext()){
							ClientBbs b1 = it1.next();
							if(b1.minLevel == newbbs.minLevel && b1.maxLevel == newbbs.maxLevel && b1.enable == true){
								b1.obsoleteTime = new Date();
								b1.enable = false;
								Server.server.getServiceRegistry().getDbService().clientBbsDao.updateEntity(b1);
								it1.remove();
							}
						}
						newbbs.isschedule = false;
						bbs.add(newbbs);
						Server.server.getServiceRegistry().getDbService().clientBbsDao.updateEntity(newbbs);
						it.remove();
					}
				}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}