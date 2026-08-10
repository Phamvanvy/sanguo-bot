package peony.db;

import java.util.List;
import peony.common.AsyncCall;
import peony.game.Server;
import peony.game.VMap;
import peony.game.convoy.NationConvoyService;
import peony.service.activity.CleanBossActivity;
import peony.service.activity.MayDayActivity;
import peony.service.activity.NewYearActivity;
import peony.service.duel.DuelService;
import peony.service.feast.FeastInstanceService;
import peony.service.nationDayActivity.NationDayService;
import peony.service.tong.battle.TongBattleVMapManager;
import peony.npc.service.ExchangeNpcService;
import peony.npc.service.Npc;
import peony.npc.service.NpcService;
import peony.npc.service.PloyNpcService;
import peony.npc.service.Position;

public class RefreshNpcCall implements AsyncCall{
	
	protected int type;
	public static final int NATIONCONVOY = 1;//国家押运
	public static final int FEASTINSTANCE = 2;//满汉全席
	public static final int DUELSERVICE = 3;//比武招亲
	public static final int NATIONDAY = 4;//提交密函
	public static final int TONGBATTLE = 5;//城战
	
	public int mapId;//刷新地图id
	public List<Npc> npcs;//刷新npc
	public static final int EXCHANGENPC = 6;  //乌巢战役
	public static final int PLOYNPC = 7;    //野外boss刷新(张角，木鹿大王等)
	
	public List<Integer> npcIds;//NpcService.java
	public List<Position> position;
	public static final int NPCSERVICE = 8; //西域挖宝箱
	
	public int npcId;
	public VMap map;
	public static final int CLEANBOSSACTIVITY = 9;
	
	public int nationConveyStartTimeIndex = 0;
	
	public int index;
	public static final int NEWYEARACTIVITY = 10;// 新年神秘星史活动
	
	public static final int MAYDAYACTIVITY = 11;// 五一众志传烽活动

	public RefreshNpcCall(int type) {
		this.type = type;
	}

	public void callFinish() throws Exception {
		if(type == NATIONCONVOY){//国家押运刷新镖车
			NationConvoyService service = Server.server.getServiceRegistry().getNationConvoyService();
			service.nationConvoy(nationConveyStartTimeIndex);
		}else if(type == FEASTINSTANCE){//满汉全席刷新报名NPC
			FeastInstanceService service = Server.server.getServiceRegistry().getFeastInstanceService();
			service.refreshNpc();
		}else if(type == DUELSERVICE){//比武招亲刷新公主雕像
			DuelService duelService = Server.server.getServiceRegistry().getDuelService();
			duelService.endInstances();
		}else if(type == NATIONDAY){//刷新倭寇侍女或女王
			NationDayService nationService = Server.server.getServiceRegistry().getNationDayService();
		    nationService.refreshNpc();
		}else if(type == TONGBATTLE){//城战刷新箭塔等
			TongBattleVMapManager tongBattleService = Server.server.getServiceRegistry().getTongBattleVMapManager();
			tongBattleService.openInstance();
		}else if(type == EXCHANGENPC){//斗阵刷新NPC
			ExchangeNpcService exchangeNpcService = Server.server.getServiceRegistry().getExchangeNpcService();
			if(mapId!=0 && npcs!=null)
			    exchangeNpcService.RefrNpc(mapId, npcs);
		}else if(type == PLOYNPC){//野外boss刷新
			PloyNpcService ployNpcService = Server.server.getServiceRegistry().getPloyNpcService();
			if(mapId!=0 && npcs!=null)
				ployNpcService.refreshNpc(mapId, npcs);
		}else if(type == NPCSERVICE){//西域挖宝
			NpcService npcService = Server.server.getServiceRegistry().getNpcService();
			if(mapId!=0 && npcIds!=null && position!=null)
				npcService.refreshNpc(mapId, npcIds, position);
		}else if(type == CLEANBOSSACTIVITY){//中秋赏月活动
			if(npcId!=0 && map!=null)
			     CleanBossActivity.refreshBoss(npcId, map);
		}else if(type == NEWYEARACTIVITY){
			NewYearActivity.refreshNpc(index);
		}else if(type == MAYDAYACTIVITY){
			MayDayActivity.refreshCreature();
		}
	}

	public void run() {
		
	}

}
