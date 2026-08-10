package peony.game.convoy;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import peony.game.CommonUtil;
import peony.game.Creature;
import peony.game.CreatureDieCallback;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.MapPoint;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.buff.ImmuneAllBuff;
import peony.game.mail.MailService;
import peony.game.nation.Nation;
import peony.game.nation.NationDeclareException;
import peony.service.Service;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

public class NationConvoyService implements Service {
	

	
	public NationConvoyDef[] defs = new NationConvoyDef[4]; 
	public NationConvoy[] convoys = new NationConvoy[4];
	
	private static final Logger log = Logger.getLogger(NationConvoyService.class);
	
	public static int DEPOSITE = 200000;
	
	public void shutdown() {
		
	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("NationConvoy.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc){
		Element root = doc.getRootElement();
		List l = root.elements("convoy");
		if(l.size() != 3)
			throw new IllegalArgumentException();
		for(int i=0;i<l.size();i++){
			Element elConvoy = (Element)l.get(i);
			int faction = Integer.parseInt(elConvoy.attributeValue("faction"));
			int npcId = Integer.parseInt(elConvoy.attributeValue("npcid"));
			NationConvoyDef def = new NationConvoyDef(faction,npcId);
			defs[def.faction] = def;
			List l1 = elConvoy.elements("point");
			for(int j=0;j<l1.size();j++){
				Element elPoint = (Element)l1.get(j);
				int mapId = Integer.parseInt(elPoint.attributeValue("mapid"));
				int x = Integer.parseInt(elPoint.attributeValue("x"));
				int y = Integer.parseInt(elPoint.attributeValue("y"));
				def.addMapPoint(mapId, x, y);
			}
		}
	}
	
	public boolean isConvoying(int faction){
		return convoys[faction] != null;
	}
	
	public void startConvoy(Nation nation) throws ConvoyException{
		if(!Time.betweenHour(Time.currDate, 19, 24))
			throw new  ConvoyException("必須在19點至24點之間進行國家押送");
		if(convoys[nation.faction] != null){
			throw new ConvoyException("當前正有一個國家押送沒完成");
		}
		synchronized(nation){
			if(nation.money<DEPOSITE){
				throw new ConvoyException("需要20万國庫資金才能發起國家押送,請呼吁民眾捐款支援.");
			}
			nation.decMoney(DEPOSITE);
		}
		if(nation.pool.getInt(Nation.PROPERTY_NATION_CONVOY, 0)==Time.day){
			throw new ConvoyException("國家押運每天只能一次");
		}
		MapPoint point = defs[nation.faction].getFirstPoint();
		NationConvoy convoy = new NationConvoy(nation,defs[nation.faction],DEPOSITE,Time.currTime);
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		GameMapObject gmo = GameMapObject.findByID(proj, defs[nation.faction].npcId);
		VMapManager manager = Server.server.getWorld().getVMapManager(point.mapId);
		VMap[] maps = ((NoInstanceVMapManager)manager).getVMaps(point.mapId);
		
		Creature npc = (Creature) VMapUtil.addCreature(maps[0],point.x,point.y, (GameMapNPC) gmo,true,0,null);
		npc.isPvp = true;
		npc.dieCallback = new DieCallback(convoy);
		npc.buffs.addBuff(new ImmuneAllBuff());
		npc.setAI(new ConvoyAI(convoy,npc));
		convoy.npc = npc;
		convoys[nation.faction] = convoy;
		nation.pool.setInt(Nation.PROPERTY_NATION_CONVOY, Time.day);
		log.info("[CONVOYSTART]FACTION["+convoy.nation.faction+"]");
		Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(nation.faction, "為了保障前線戰事,國公下令押送軍備物資送往前線, 36級以上的玩家前往江陵營地押送,5分鐘后押送開始.");
		for(int i=1;i<4;i++){
			if(i != nation.faction){
				Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(i, 
						MessageFormat.format("{0}發起了國家押送,5分鐘后大家集結去江陵攔截對方的物資車,保家衛國.", GameObject.getFactionName(nation.faction)));
			}
		}
	}
	
	public void success(NationConvoy convoy) {
		log.info("[CONVOYSUCCESS]FACTION["+convoy.nation.faction+"]");
		convoys[convoy.nation.faction] = null;
		convoy.npc.removeFromWorld();
		convoy.nation.addMoney(DEPOSITE);
		MailService mailService = Server.server.getServiceRegistry().getMailService();
		List<Player> l = convoy.getSourcePlayers();
		for(Player p:l){
			GameItem item1 = ObjectAccessor.createGameItem(1311);
			GameItem item2 = ObjectAccessor.createGameItem(1683);
			mailService.sendSystemMailAsync(p.id, "系統", "國家押送獎勵", "", 0, item1, 2, "COV");
			mailService.sendSystemMailAsync(p.id, "系統", "國家押送獎勵", "", 0, item2, 1, "COV");
		}
		if (l.size() > 0) {
			int v = DEPOSITE / l.size();
			for (Player p : l) {
				try {
					PlayerTransaction tx = p.newTransaction("COV");
					p.addMoney(v, tx, true);
					tx.commit();
				} catch (Exception ex) {
					log.error(ex, ex);
				}
			}
		}
		Server.server.getServiceRegistry().getChatService()
				.sendFactionSystemMessage(convoy.nation.faction,
						"國家押送成功,國庫資金增加20万;參与國家押送的玩家平分20万,另每人獲得神秘禮包1個和珍珠2顆");
		Server.server.getServiceRegistry().getChatService().sendWorldMessage(
				MessageFormat.format("{0}國民齊心協力,成功將物資送往了前線.", GameObject.getFactionName(convoy.nation.faction)));
		
	}
	
	public void fail(NationConvoy convoy){
		log.info("[CONVOYFAIL]FACTION["+convoy.nation.faction+"]");
		convoys[convoy.nation.faction] = null;
		MailService mailService = Server.server.getServiceRegistry().getMailService();
		boolean[] bs = new boolean[4];
		Arrays.fill(bs, false);
		List<Player> l = convoy.getDestPlayers();
		for(Player p:l){
			bs[p.faction] = true;
			GameItem item1 = ObjectAccessor.createGameItem(1311);
			GameItem item2 = ObjectAccessor.createGameItem(1683);
			mailService.sendSystemMailAsync(p.id, "系統", "劫取國家押送獎勵", "", 0, item1, 1, "COV");
			mailService.sendSystemMailAsync(p.id, "系統", "劫取國家押送獎勵", "", 0, item2, 1, "COV");
		}
		if (l.size() > 0) {
			int v = DEPOSITE / l.size();
			for (Player p : l) {
				try {
					PlayerTransaction tx = p.newTransaction("COV");
					p.addMoney(v, tx, true);
					tx.commit();
				} catch (Exception ex) {
					log.error(ex, ex);
				}
			}
		}
		Server.server.getServiceRegistry().getChatService().sendWorldMessage(
				MessageFormat.format("{0}的鏢車已經被摧毀.", GameObject.getFactionName(convoy.nation.faction)));
		for(int i=1;i<bs.length;i++){
			if(bs[i]){
				Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(i, 
						MessageFormat.format("{0}送往前線的物資已被我國截獲,舉國歡騰", 
								GameObject.getFactionName(convoy.nation.faction)));
			}
		}
	}
	
	static class DieCallback implements CreatureDieCallback{
		
		protected NationConvoy convoy;
		
		public DieCallback(NationConvoy convoy){
			this.convoy = convoy;
		}
		
		public void die(Creature c,Unit source){
			Server.server.getServiceRegistry().getNationConvoyService().fail(convoy);
		}
	}
}
