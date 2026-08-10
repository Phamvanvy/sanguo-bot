package peony.service.worldmap;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMapException;
import peony.game.VMapUtil;
import peony.game.asyncbattle.AsyncBattleService;
import peony.game.itemeffect.ActivityItemEffect;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.MonthlyPayService;
import peony.service.Service;
import peony.service.feast.FeastInstanceService;
import peony.service.pluginstance.LanternFestivalService;
import peony.service.pluginstance.MayDayFestivalService;

import com.pip.sanguo.data.map.GameMapExit;

public class WorldMapService implements Service {
	
	private static Logger log = Logger.getLogger(WorldMapService.class);
	public static int worldTeleportItemId = 1165; // 传送符ID
	public static int[]  neutra = {448};
	public static int[][] exits = {{},{161,209,105},{177,557,86},{417,74,575}};
	public static int[][] mapPosition = {
		//魏国关卡
		{1442,272,228,1},//许田镇
		{129,436,558,1},//颖川
		{256,302,288,1},//许昌城外
		{272,857,588,1},//许昌
		{96,707,724,1},//官渡
		{145,169,307,1},//徐州
		{161,464,612,1},//下邳
		{400,365,120,1},//小沛
		{545,376,369,1},//幽州
		{673,607,257,1},//渔阳
		{657,758,158,1},//乌桓
		//蜀国关卡
		{1410,119,156,2},//江津村
		{80,800,184,2},//峨眉山
		{224,380,267,2},//成都城外
		{240,796,524,2},//成都
		{33,525,432,2},//落凤坡
		{17,594,492,2},//永昌郡
		{177,235,300,2},//且兰郡
		{528,100,180,2},//一登
		{481,527,286,2},//建宁
		{609,768,447,2},//朱提
		{625,455,690,2},//夜郎
		//吴国关卡
		{1426,188,234,3},//稻香村
		{337,406,628,3},//秦淮河
		{368,324,310,3},//建业城外（紫金山）
		{352,838,598,3},//建业
		{305,241,187,3},//曲阿
		{289,401,721,3},//会稽
		{417,600,545,3},//茅山
		{496,417,818,3},//越王池
		{560,226,117,3},//建安
		{593,133,595,3},//武夷山
		{689,786,172,3},//瓯宁
		//中立关卡
		{848,1344,181,1},//西域
		{848,87,1367,2},
		{848,1373,1226,3},
		{1008,481,78,1},//西域山城
		{1008,958,96,2},
		{1008,1376,122,3},
		{816,114,1206,1},//朔方
		{816,797,1484,2},
		{816,1383,1182,3},
		{1024,165,1370,1},//匈奴王庭
		{1024,770,1371,2},
		{1024,1368,1363,3},
		{896,628,111,1},//江陵
		{896,1330,141,2},
		{896,120,262,3},
		{1056,111,871,1},//荆州
		{1056,747,1146,2},
		{1056,1371,1209,3},
		{2032,147,114,1},//南海
		{2032,1430,867,2},
		{2032,183,1332,3},
		{2016,1317,144,1},//南越
		{2016,177,305,2},
		{2016,114,1126,3},
		{2000,461,107,1},//乌巢
		{2000,195,1350,2},
		{2000,1375,1355,3},
		//副本入口
		{448,335,361,1},//河东（魏）
		{448,335,361,2},
		{448,335,361,3},
		{768,315,194,1},//古墓入口
		{768,315,194,2},
		{768,315,194,3},
		{976,266,284,1},//天龙阵入口
		{976,266,284,2},
		{976,266,284,3},
		{1344,61,94,1},//洛阳行宫
		{1344,61,94,2},
		{1344,61,94,3}
		};
	
	/**
	 * 世界地图传送
	 * @param packet
	 * @param session
	 */
	public void worldTeleport(Packet packet, ClientSession session){
		Player p = (Player)session.getClient();
		if(p!=null){
			int serial = packet.getInt();
			int mapId = packet.getInt();
			if(p.isInStep){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WORLD_TELEPORT_CLIENT, "跨服战场期间不能使用此功能");
				return;
			}
			if(p.getThreatCount()>0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WORLD_TELEPORT_CLIENT, peony.Messages.STRING_01148);
				return;
			}
			if(mapId==p.map.id){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WORLD_TELEPORT_CLIENT, peony.Messages.STRING_01149);
				return;
			}
			if(p.map.id == FeastInstanceService.MAPID || p.map.id==AsyncBattleService.battleMap){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WORLD_TELEPORT_CLIENT, "该场景无法传送");
				return;
			}
			int[] positionArr = getPosition(mapId,p.faction);
			if(positionArr==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WORLD_TELEPORT_CLIENT, peony.Messages.STRING_01150);
				return;
			}
			int x = positionArr[0];
			int y = positionArr[1];
			PlayerTransaction tx = p.newTransaction("MTL");
			GameItem item = p.bag.removeGameItem(worldTeleportItemId, -1, 1, tx, false);
			MonthlyPayService service = Server.server.getServiceRegistry().getMonthlyPayService();
			if(p.vipLevel>=1){
				log.info("[WORLDTELEPORT]"+LogUtil.getPlayerLogString(p)+"SOURSE["+p.map.id+"]TARGET["
						+mapId+"]TYPE[VIPCHARGE]TRY");
			}else if(!service.inService(p, worldTeleportItemId)){
				log.info("[WORLDTELEPORT]"+LogUtil.getPlayerLogString(p)+"SOURSE["+p.map.id+"]TARGET["
						+mapId+"]COUNT["+p.bag.getGameItemCount(worldTeleportItemId)+"]TRY");
			} else {
				log.info("[WORLDTELEPORT]"+LogUtil.getPlayerLogString(p)+"SOURSE["+p.map.id+"]TARGET["
						+mapId+"]TYPE[MONTHPAY]TRY");
			}
			if(item!=null || service.inService(p, worldTeleportItemId) || ActivityItemEffect.hasTeleportEffect(p) || p.vipLevel>=1){
				try {
					p.goMap(mapId, x, y);
					//元宵活动初始化
					LanternFestivalService lService = Server.server.getServiceRegistry().getLanternFestivalService();
					lService.initState(p);
					//五一活动初始化
					MayDayFestivalService mService = Server.server.getServiceRegistry().getMayDayFestivalService();
					mService.initState(p);
					for(int id : neutra){
						if(mapId==id){
							GameMapExit[] gmes = VMapUtil.getExits(mapId);
							for (GameMapExit exit : gmes) {
								if (exit.exitType == GameMapExit.TYPE_RECALL) {
									p.pool.setString(exit.positionVarName,exits[p.faction][0] + "," 
											+ exits[p.faction][1] + "," + exits[p.faction][2]);
								}
							}
						}
					}
					if(!service.inService(p, worldTeleportItemId)&&!ActivityItemEffect.hasTeleportEffect(p)){
					   tx.commit();
					} else {
						tx.rollback();
					}
					Packet pt = new Packet(OpCode.WORLD_TELEPORT_SERVER);
					pt.putInt(serial);
					p.send(pt);
					log.info("[WORLDTELEPORT]"+LogUtil.getPlayerLogString(p)+"COUNT["+p.bag.getGameItemCount(worldTeleportItemId)+"]OK");
				} catch (VMapException e) {
					//不可能走到这一步
					tx.rollback();
					log.error(e,e);
				}
			}else{
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WORLD_TELEPORT_CLIENT, 
						MessageFormat.format(peony.Messages.STRING_01151, 
								ObjectAccessor.getItemTemplate(worldTeleportItemId).name));
				log.info("[WORLDTELEPORT]"+LogUtil.getPlayerLogString(p)+"COUNT["+p.bag.getGameItemCount(worldTeleportItemId)+"]FAILED");
			}
		}
	}
	
	/**
	 * 获取跳转后的位置坐标
	 * @param mapId
	 * @return
	 */
	public int[] getPosition(int mapId, int faction){
		for(int [] arr : mapPosition){
			if(arr[0]==mapId && arr[3]==faction){
				int[] result = new int[2];
				result[0] = arr[1];
				result[1] = arr[2];
				return result;
			}
		}
		return null;
	}

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		
	}

}
