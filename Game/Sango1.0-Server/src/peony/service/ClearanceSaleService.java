package peony.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.Actor;
import peony.game.ChatOption;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.net.Packet;
import peony.util.TimeUtil;

/**
 * 团购服务
 * @author dchen
 */
public class ClearanceSaleService implements Service {

	private boolean onSale; //团购开启标志
	private int decItem; //团购报名扣除物品
	private int decItemCount; //团购报名扣除物品数量
	private int minPlayers; //团购最低人数
	private int maxPlayers; //团购人数限制
	private int rewardItem; //奖励物品
	private long endTime; //结束时间
	private int costPrice; //原价
	private int price; //现价
	private String nextItem; //下期商品
	private List<Integer> signUps = new ArrayList<Integer>(); //团购报名
	public static int[][] NPC = {{272,1114127,493,759},{240,983047,499,668},{352,1441878,649,401}};
	protected List<GameObject> npctemps = new ArrayList<GameObject>();

	public void startup() throws Exception {
		
	}
	
	public void signUp(Player player) throws Exception{
		if(player!=null){
			if(!onSale)
				throw new Exception(peony.Messages.STRING_01333);
			if(signUps.contains(player.id))
				throw new Exception(peony.Messages.STRING_01334);
			if(signUps.size()>=maxPlayers)
				throw new Exception(peony.Messages.STRING_01335);
			ItemTemplate template = ObjectAccessor.getItemTemplate(decItem);
			if(template==null)
				template = ObjectAccessor.createGameItem(decItem).template;
			if(player.bag.getGameItemCount(decItem)>0){
				PlayerTransaction tx = player.newTransaction("CLEARSALE");
				try {
					GameItem item = player.bag.removeGameItemIngoreInstanceId(decItem, decItemCount, tx, true);
					if(item==null){
						tx.rollback();
						throw new Exception(MessageFormat.format(peony.Messages.STRING_01336, template.name, decItemCount));
					}
					tx.commit();
					signUps.add(player.id);
				} catch (Exception e) {
					tx.rollback();
					throw new Exception(MessageFormat.format(peony.Messages.STRING_01336, template.name, decItemCount));
				}
			}else{
				throw new Exception(MessageFormat.format(peony.Messages.STRING_01336, template.name, decItemCount));
			}
		}
	}
	
	public void begin(int decItem, int decItemCount, int minPlayers, int maxPlayers, 
			int rewardItem, long endTime, int costPrice, int price, String nextItem){
		this.onSale = true;
		this.decItem = decItem;
		this.decItemCount = decItemCount;
		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
		this.rewardItem = rewardItem;
		this.endTime = endTime;
		this.costPrice = costPrice;
		this.price = price;
		this.nextItem = nextItem;
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		chatService.sendSystemMessage(ChatOption.SYSTEM, peony.Messages.STRING_00004, peony.Messages.STRING_01337);
		refreshNpc();
	}
	
	private void refreshNpc(){
		for(int[] n : NPC){
			int mapId = n[0];
			int npcId = n[1];
			int x = n[2];
			int y = n[3];
			VMapManager manager = Server.server.getWorld().getVMapManager(mapId);
			VMap[] maps = ((NoInstanceVMapManager) manager).getVMaps(mapId);
			ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
			GameMapObject gmo = GameMapObject.findByID(proj, npcId);
			for (VMap map : maps) {
				if(map!=null){
					GameObject npc0 = VMapUtil.addCreature(map, x, y, (GameMapNPC) gmo, true, 0, null);
					npctemps.add(npc0);
				}
			}
		}
	}
	
	private void clearNpc(){
		for(GameObject npc : npctemps){
			if(npc!=null && npc.getVMap() != null){
				npc.removeFromWorld();
			}
		}
		npctemps.clear();
	}
	
	public void end(){
		this.onSale = false;
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		if(signUps.size()>=minPlayers){
			MailService mailService = Server.server.getServiceRegistry().getMailService();
			GameItem attItem = ObjectAccessor.createGameItem(rewardItem);
			for(int playerId : signUps){
				mailService.sendSystemMailAsync(playerId, "系统", peony.Messages.STRING_01338, "感谢您参加团购，请查收附件", 0, attItem, 1, "CLEARSALE");
			}
			chatService.sendSystemMessage(ChatOption.SYSTEM, peony.Messages.STRING_00004, peony.Messages.STRING_01339);
		}else{
			chatService.sendSystemMessage(ChatOption.SYSTEM, peony.Messages.STRING_00004, peony.Messages.STRING_01340);
		}
		signUps.clear();
		clearNpc();
	}
	
	public void toPlayer(Player player, int serial){
		if(player!=null){
			if(!onSale){
				ErrorHandler.sendErrorMessage(player.session, serial, OpCode.CLEARSALE_LIST_CLIENT, peony.Messages.STRING_01341);
				return;
			}
			Packet pt = new Packet(OpCode.CLEARSALE_LIST_SERVER);
			pt.putInt(serial);
			ItemTemplate item = ObjectAccessor.getItemTemplate(rewardItem);
			if(item==null)
				item = ObjectAccessor.createGameItem(rewardItem).template;
			ItemTemplate item0 = ObjectAccessor.getItemTemplate(decItem);
			if(item==null)
				item0 = ObjectAccessor.createGameItem(decItem).template;
			pt.putString(decItemCount+item0.name);
			pt.putInt(item.showType);
			pt.putString(item.name);
			pt.putString(ItemUtil.parseUseConfirm(item.desc));
			pt.putShort(costPrice);
			pt.putShort(price);
			pt.putShort(minPlayers);
			pt.putShort(maxPlayers);
			pt.putString(getLeavingTime());
			pt.putString(nextItem);
			int currentPlayers = signUps.size();
			pt.putShort(currentPlayers);
			List<Integer> onlineIndex = new ArrayList<Integer>();
			for(int i=0;i<signUps.size();i++){
				int playerId = signUps.get(i);
				Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(playerId);
				if(actor!=null && actor.online){
					pt.putInt(actor.id);
					pt.putString(actor.name);
					pt.put(actor.online?1:0);
					pt.putShort(actor.level);
					pt.put(actor.sex);
					pt.put(actor.clazz);
					onlineIndex.add(i);
				}
			}
			for(int i=0;i<signUps.size();i++){
				boolean online = false;
				for(int index : onlineIndex){
					if(index==i)
						online = true;
				}
				if(!online){
					Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(signUps.get(i));
					pt.putInt(actor==null ? 0 : actor.id);
					pt.putString(actor==null ? peony.Messages.STRING_01342 : actor.name);
					pt.put(actor==null ? 0 : (actor.online?1:0));
					pt.putShort(actor==null ? 0 : actor.level);
					pt.put(actor==null ?  0 : actor.sex);
					pt.put(actor==null ? 0 : actor.clazz);
				}
			}
			player.send(pt);
		}
	}
	
	public String getLeavingTime(){
		long lev = (endTime-System.currentTimeMillis())/1000;
		if(lev<0)
			lev = 0;
		int[] arr = TimeUtil.getH_M_S(Math.abs((int) (lev)));
		return MessageFormat.format(peony.Messages.STRING_01343, arr[0], arr[1], arr[2]);
	}

	public void shutdown() {
		
	}

}
