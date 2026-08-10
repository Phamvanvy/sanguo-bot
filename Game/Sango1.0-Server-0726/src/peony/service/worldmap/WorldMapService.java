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
import peony.game.VMapException;
import peony.game.VMapUtil;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;

import com.pip.sanguo.data.map.GameMapExit;

public class WorldMapService implements Service {
	
	private static Logger log = Logger.getLogger(WorldMapService.class);
	public static int worldTeleportItemId = 1165; // 传送符ID
	public static int[]  neutra = {448};
	public static int[][] exits = {{},{161,209,105},{177,557,86},{417,74,575}};
	public static int[][] mapPosition = {
		{1442,272,228,1},
		{128,175,379,1},
		{256,302,288,1},
		{272,857,588,1},
		{97,264,386,1},
		{144,240,164,1},
		{160,220,260,1},
		{400,220,459,1},
		{544,363,170,1},
		{672,174,254,1},
		{656,271,200,1},
		{448,335,361,1},
		{1410,119,156,2},
		{64,166,241,2},
		{224,380,267,2},
		{240,796,524,2},
		{32,174,262,2},
		{16,280,146,2},
		{176,429,242,2},
		{528,164,310,2},
		{480,122,178,2},
		{608,365,267,2},
		{624,456,291,2},
		{448,335,361,2},
		{1426,188,234,3},
		{336,209,199,3},
		{368,324,310,3},
		{352,838,598,3},
		{304,222,231,3},
		{288,310,342,3},
		{416,219,369,3},
		{497,203,194,3},
		{560,226,117,3},
		{592,133,192,3},
		{688,283,265,3},
		{448,335,361,3},
		{848,1344,181,1},
		{848,87,1367,2},
		{848,1373,1226,3},
		{1008,481,78,1},
		{1008,958,96,2},
		{1008,1376,122,3},
		{768,315,194,1},
		{768,315,194,2},
		{768,315,194,3},
		{816,114,1206,1},
		{816,797,1484,2},
		{816,1383,1182,3},
		{1024,165,1370,1},
		{1024,770,1371,2},
		{1024,1368,1363,3},
		{896,628,111,1},
		{896,1330,141,2},
		{896,120,262,3},
		{1056,111,871,1},
		{1056,747,1146,2},
		{1056,1371,1209,3},
		{864,818,101,1},
		{864,129,97,2},
		{864,1408,247,3},
		{1040,1318,91,1},
		{1040,775,97,2},
		{1040,1390,402,3},
		{976,266,284,1},
		{976,266,284,2},
		{976,266,284,3},
		{1456,1314,1352,1},//司隶[魏]
		{1456,124,1373,2},//司隶[蜀]
		{1456,410,72,3},//司隶[吴]
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
			if(p.getThreatCount()>0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WORLD_TELEPORT_CLIENT, "战斗状态不可传送");
				return;
			}
			if(mapId==p.map.id){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WORLD_TELEPORT_CLIENT, "已经在此场景中");
				return;
			}
			int[] positionArr = getPosition(mapId,p.faction);
			if(positionArr==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.WORLD_TELEPORT_CLIENT, "此场景不能到达");
				return;
			}
			int x = positionArr[0];
			int y = positionArr[1];
			PlayerTransaction tx = p.newTransaction("MTL");
			GameItem item = p.bag.removeGameItem(worldTeleportItemId, -1, 1, tx, false);
			log.info("[WORLDTELEPORT]"+LogUtil.getPlayerLogString(p)+"SOURSE["+p.map.id+"]TARGET["
					+mapId+"]COUNT["+p.bag.getGameItemCount(worldTeleportItemId)+"]TRY");
			if(item!=null){
				try {
					p.goMap(mapId, x, y);
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
					tx.commit();
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
						MessageFormat.format("您没有{0}无法前往，请进入充值商店购买", 
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
