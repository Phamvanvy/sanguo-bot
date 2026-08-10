package peony.game.clientbbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
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
import peony.game.exp.ExpService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;

public class ClientBbsService implements Service {

	private static final Logger log = Logger.getLogger(ClientBbsService.class);

	protected File file;

	List<ClientBbs> bbs = new ArrayList<ClientBbs>(); // 储存由客服输入的活动项目
	
	protected int[] min = {30,50,55,70};
	
	protected int[] max = {50,55,70,100};

	public void startup() throws Exception {
		loadClientBbsFromDb();
	}

	public void loadClientBbsFromDb() {
		ClientBbsDAO dao = Server.server.getServiceRegistry().getDbService().clientBbsDao;
		bbs.addAll(dao.getClientBbs());
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
					try{
						String active = b.active;
						BufferedReader br = new BufferedReader(
								new StringReader(active));
						String line;
						while ((line = br.readLine()) != null) {
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
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		}
			if (hasContent == false) {
				packet.putString("<cff0000>暫無公告</c>");
				packet.putShort(0);
			}
			ExpService service = Server.server.getServiceRegistry()
					.getExpService();
			int offLineExp = service.getNotOnineExps(p);
			packet.putString(String.valueOf(offLineExp));
			p.send(packet);
	}
}
	
	public void getClientBbs(Packet pt, ClientSession session) {
		Admin p = (Admin) session.getClient();
		int serial = pt.getInt();
		if (p != null) {
			int minLevel = pt.getInt();
			int maxLevel = pt.getInt();
			if(!isInLevel(minLevel,maxLevel)){
				ErrorHandler.sendAdminErrorMessage(session, serial, 
						OpCode.ADMIN_SAVECLIENTBBS_CLIENT, "請輸入正确的等級區間,區間為：30-50,50-55,55-70,70-100");
				return;
			}
			ClientBbs newbbs = new ClientBbs(minLevel,maxLevel);
			Iterator<ClientBbs> it = bbs.iterator();
			while(it.hasNext()){
				ClientBbs b = it.next();
				if(b.minLevel == newbbs.minLevel && b.maxLevel == newbbs.maxLevel && b.enable == true){
					b.obsoleteTime = new Date();
					b.enable = false;
					Server.server.getServiceRegistry().getDbService().clientBbsDao
					                 .updateEntity(b);
					it.remove();
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
			newbbs.pulishTime = new Date();
			newbbs.enable = true;
			bbs.add(newbbs);
			Server.server.getServiceRegistry().getDbService().clientBbsDao
			                 .makePersistent(newbbs);
			Packet packet = new Packet(OpCode.ADMIN_SAVECLIENTBBS_SERVER);
			packet.putInt(serial);
			p.send(packet);
		}
	}

	public void shutdown() {
		
	}
}