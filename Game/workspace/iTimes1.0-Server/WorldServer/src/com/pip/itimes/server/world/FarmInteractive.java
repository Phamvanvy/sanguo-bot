package com.pip.itimes.server.world;

import java.util.ArrayList;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Command;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.farm.FarmLandInfo;
import com.pip.itimes.server.world.game.FarmInstance;
import com.pip.itimes.server.world.game.FarmInstanceModel;
import com.pip.itimes.server.world.game.GameMap;

public class FarmInteractive implements CommandProcessor {

	private ConnectSession connectSession = null;
	
	public final static int FARM_GOTO = 0;				//传送到庄园
	public final static int FARM_LAND_CHECK = 1;		//检查土地
	public final static int FARM_LAND_SEE = 2;			//查看土地
	public final static int FARM_LAND_STEAL = 3;		//窃取土地
	public final static int FARM_LAND_SOWSEEDS = 4;		//播种
	public final static int FARM_LAND_UPLEVEL = 5;		//土地升级
	public final static int FARM_LAND_SELECTSEEDS = 6;	//选择种子进行播种
	public final static int FARM_LAND_RESULTS = 7;		//收获
	public final static int FARM_LAND_FERTILIZE = 8;	//施肥
	public final static int FARM_LAND_SHOP = 9;			//shop
	public final static int FARM_LAND_OPEN = 10;		//土地开放
	public final static int FARM_LAND_LEVELOK = 11;		//确认升级土地
	public final static int FARM_LAND_ABOUT = 12;		//关于
	public final static int FARM_LAND_REMOVE = 13;		//铲除种子
	public final static int FARM_LAND_REMOVE_OK = 14;	//确认铲除
	
	public FarmInteractive(ConnectSession connectSession){
		this.connectSession = connectSession;
	}
	
	public void process(WorldPlayer player, Command command) throws Exception {
		int type = Integer.parseInt(command.getParam(0));
		switch(type){
		case FARM_GOTO:
			String destPlayerName = command.getParam(1);
			WorldPlayer destPlayer = connectSession.playerService.getWorldPlayerAndCatch(destPlayerName);
			if(destPlayer == null){
				connectSession.sendMessage("没有找到玩家！", command.getSerial(), command.getSessionId());
				return;
			}
			try{
    			FarmInstance farmInstance = connectSession.farmInstanceModel.preTry(player, destPlayer.getId());
    			FarmInstance fi = connectSession.farmInstanceModel.tryGotoInstance(farmInstance.getId(), player, 0);
    			if(fi != null){
	    			InstanceDefinition idf = farmInstance.getDefinition();
	    			connectSession.sendGotoMap(player.getId(), idf.getMap(), idf.getX(), idf.getY());
    			}
			}catch(Exception e){
				connectSession.sendMessage(e.getMessage(), command.getSerial(), command.getSessionId());
			}finally{
				connectSession.playerService.releasePlayer(destPlayer);
			}
			break;
		case FARM_LAND_CHECK:
			int landIndex = Integer.parseInt(command.getParam(1));
			if(landIndex >= 0){
				if(command.getParamCount() > 2){
					int action = Integer.parseInt(command.getParam(2));
					connectSession.farmInstanceModel.inteeractive(player, landIndex, action);
				}else{
					boolean isMaster = connectSession.farmInstanceModel.isMaster(player);
					if(isMaster){
						byte hasSeed = connectSession.farmInstanceModel.hasSeed(player, landIndex);
						switch(hasSeed){
						case FarmInstanceModel.SEED_HASSEED:
						case FarmInstanceModel.SEED_NOSEED:
							FarmLandInfo farmLandInfo = connectSession.farmInstanceModel.getFarmLandInfo(player, landIndex);
							if(farmLandInfo == null){
								connectSession.sendMessage("该土地异常！", command.getSerial(), command.getSessionId());
								return;
							}
							if(farmLandInfo.getSeed() > 0){
								byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
			                            new String[] {"6", "1", "1.查看\n2.收获\n3.施肥\n4.升级\n5.铲除\n6.取消",
			                            "farmInteractive " + FARM_LAND_SEE + " " + landIndex, 
			                            "farmInteractive " + FARM_LAND_RESULTS + " " + landIndex, 
			                            "farmInteractive " + FARM_LAND_FERTILIZE + " " + landIndex, 
			                            "farmInteractive " + FARM_LAND_UPLEVEL + " " + landIndex,
										"farmInteractive " + FARM_LAND_REMOVE + " " + landIndex});
			                    UWAPSegment seg = new UWAPSegment(ClientConstants.
			                            GET_FILE_OK, command.getSerial(),
			                            command.getSessionId());
			                    seg.writeShort((short) 31010);
			                    seg.writeShort((short) 2);
			                    seg.write(bytes);
			                    connectSession.write(seg);
							}else{
								byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
			                            new String[] {"4", "1", "1.查看\n2.播种\n3.升级\n4.取消",
										"farmInteractive " + FARM_LAND_SEE + " " + landIndex, 
			                            "farmInteractive " + FARM_LAND_SOWSEEDS + " " + landIndex, 
			                            "farmInteractive " + FARM_LAND_UPLEVEL + " " + landIndex});
			                    UWAPSegment seg = new UWAPSegment(ClientConstants.
			                            GET_FILE_OK, command.getSerial(),
			                            command.getSessionId());
			                    seg.writeShort((short) 31010);
			                    seg.writeShort((short) 2);
			                    seg.write(bytes);
			                    connectSession.write(seg);
							}
							break;
						case FarmInstanceModel.SEED_NOLANDINFO:
							String landOpenResult = connectSession.farmInstanceModel.landOpen(player, landIndex, true, null);
							if(landOpenResult == null){
								int money = connectSession.farmInstanceModel.landOpenMoney(player, landIndex);
								byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
			                            new String[] {"2", "1", "该土地还未开放，开启需要" + money + "吸血鬼金元，是否开启？（植物成熟的时候会从打败的吸血鬼身上获得金元，当前金元" + player.getFarmMoney() + "）\n1.开启\n2.取消",
			                            "farmInteractive " + FARM_LAND_OPEN + " " + landIndex, 
			                            "ok"});
			                    UWAPSegment seg = new UWAPSegment(ClientConstants.
			                            GET_FILE_OK, command.getSerial(),
			                            command.getSessionId());
			                    seg.writeShort((short) 31010);
			                    seg.writeShort((short) 2);
			                    seg.write(bytes);
			                    connectSession.write(seg);
							}else{
								connectSession.sendMessage(landOpenResult, command.getSerial(), command.getSessionId());
							}
							break;
						default:
							connectSession.sendMessage("庄园的主人还没在这块土地上播种种子呢！", command.getSerial(), command.getSessionId());
							break;
						}
					}else{
						byte hasSeed = connectSession.farmInstanceModel.hasSeed(player, landIndex);
						switch(hasSeed){
						case FarmInstanceModel.SEED_HASSEED:
							byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
		                            new String[] {"3", "1", "1.查看\n2.窃取\n3.取消",
		                            "farmInteractive " + FARM_LAND_SEE + " " + landIndex, 
		                            "farmInteractive " + FARM_LAND_STEAL + " " + landIndex});
		                    UWAPSegment seg = new UWAPSegment(ClientConstants.
		                            GET_FILE_OK, command.getSerial(),
		                            command.getSessionId());
		                    seg.writeShort((short) 31010);
		                    seg.writeShort((short) 2);
		                    seg.write(bytes);
		                    connectSession.write(seg);
		                    break;
						default:
							connectSession.sendMessage("庄园的主人还没在这块土地上播种种子呢！", command.getSerial(), command.getSessionId());
						}
					}
				}
			}else{
				connectSession.sendMessage("土地不存在！", command.getSerial(), command.getSessionId());
			}
			break;
		case FARM_LAND_SEE:
			landIndex = Integer.parseInt(command.getParam(1));
			if(landIndex >= 0){
				connectSession.sendMessage(connectSession.farmInstanceModel.getLandInfo(player, landIndex), command.getSerial(), command.getSessionId());
			}else{
				connectSession.sendMessage("土地不存在！", command.getSerial(), command.getSessionId());
			}
			break;
		case FARM_LAND_STEAL:
			landIndex = Integer.parseInt(command.getParam(1));
			if(landIndex >= 0){
				Changed changed = new Changed();
				String message = connectSession.farmInstanceModel.stealLand(player, landIndex, changed);
				connectSession.sendGetItem(changed, command.getSerial(), command.getSessionId(), command.getAppType());
				connectSession.sendMessage(message, command.getSerial(), command.getSessionId());
			}else{
				connectSession.sendMessage("土地不存在！", command.getSerial(), command.getSessionId());
			}
			break;
		case FARM_LAND_SOWSEEDS:
			landIndex = Integer.parseInt(command.getParam(1));
			if(landIndex >= 0){
				String sowseedsMessage = connectSession.farmInstanceModel.canSowseeds(player, landIndex);
				//可以进行播种时 下发种子清单
				if(sowseedsMessage == null){
					Grid[] extendedItems = player.getExtendedItems();
					if(extendedItems == null || extendedItems.length <= 0){
						connectSession.sendMessage("对不起，你还没有可以种植的种子哦，你可以通过打80级的世界怪物，100级的副本BOSS或者每日任务来获得种子~", command.getSerial(), command.getSessionId());
						return;
					}
					ArrayList<IItem> seeds = new ArrayList<IItem>();
					for(int i=0; i<extendedItems.length; i++){
						if(extendedItems[i].item.getItemShowType() == 10){
							seeds.add(extendedItems[i].item);
						}
					}
					if(seeds.size() == 0){
						connectSession.sendMessage("对不起，你还没有可以种植的种子哦，你可以通过打80级以上的世界怪物，100级的副本BOSS或者每日任务来获得种子~", command.getSerial(), command.getSessionId());
						return;
					}
					UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST, command.getSerial(),
                            command.getSessionId());
                    seg.writeShort((short)10274);
                    seg.writeString("种子列表");
                    seg.write((byte) 4);
                    seg.writeShort((short)(seeds.size()));
                    for (int j = 0; j < seeds.size(); j++) {
                    	IItem item = seeds.get(j);
                        seg.writeInt(item.getItemId());
                        seg.writeString(item.getName());
                        seg.writeInt(Utils.CLR_WHITE);
                    }
                    seg.write((byte) 1);
                    seg.writeString("播种");
                    seg.writeString("farmInteractive " + FARM_LAND_SELECTSEEDS + " " + landIndex);
                    connectSession.write(seg);
				}else{
					connectSession.sendMessage("该土地已经有种子了！", command.getSerial(), command.getSessionId());
				}
			}else{
				connectSession.sendMessage("土地不存在！", command.getSerial(), command.getSessionId());
			}
			break;
		case FARM_LAND_UPLEVEL:
			landIndex = Integer.parseInt(command.getParam(1));
			if(landIndex >= 0){
				synchronized (player) {
					String uplevelResult = connectSession.farmInstanceModel.landLevelUp(player, landIndex, true, null);
					if(uplevelResult == null){
						int money = connectSession.farmInstanceModel.landLevelUpMoney(player, landIndex);
						byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
	                            new String[] {"2", "1", "是否确认升级土地？需要花费" + money +"吸血鬼金元。升级土地可以提高果实的产量哦！(植物成熟的时候会从打败的吸血鬼身上获得金元，当前金元" + player.getFarmMoney() + "）\n1.升级\n2.取消",
	                            "farmInteractive " + FARM_LAND_LEVELOK + " " + landIndex, 
	                            "ok"});
	                    UWAPSegment seg = new UWAPSegment(ClientConstants.
	                            GET_FILE_OK, command.getSerial(),
	                            command.getSessionId());
	                    seg.writeShort((short) 31010);
	                    seg.writeShort((short) 2);
	                    seg.write(bytes);
	                    connectSession.write(seg);
					}else{
						connectSession.sendMessage(uplevelResult, command.getSerial(), command.getSessionId());
					}
				}
			}else{
				connectSession.sendMessage("土地不存在！", command.getSerial(), command.getSessionId());
			}
			break;
		case FARM_LAND_SELECTSEEDS:
			landIndex = Integer.parseInt(command.getParam(1));
			if(landIndex >= 0){
				synchronized (player) {
					int itemid = Integer.parseInt(command.getParam(2));
					Changed changed = new Changed();
					boolean sowseedsResult = connectSession.farmInstanceModel.sowseeds(player, landIndex, itemid, changed);
					if(sowseedsResult){
						connectSession.sendGetItem(changed, command.getSerial(), command.getSessionId(), command.getAppType());
						connectSession.sendMessage("播种成功，你可以查看该植物的信息哦。", command.getSerial(), command.getSessionId());
					}else{
						connectSession.sendMessage("播种失败！", command.getSerial(), command.getSessionId());
					}
				}
			}else{
				connectSession.sendMessage("土地不存在！", command.getSerial(), command.getSessionId());
			}
			break;
		case FARM_LAND_RESULTS:
			landIndex = Integer.parseInt(command.getParam(1));
			if(landIndex >= 0){
				synchronized (player) {
					Changed changed = new Changed();
					String sowseedsResult = connectSession.farmInstanceModel.getResults(player, landIndex, changed);
					connectSession.sendGetItem(changed, command.getSerial(), command.getSessionId(), command.getAppType());
					connectSession.sendMessage(sowseedsResult, command.getSerial(), command.getSessionId());
				}
			}else{
				connectSession.sendMessage("土地不存在！", command.getSerial(), command.getSessionId());
			}
			break;
		case FARM_LAND_FERTILIZE:
			landIndex = Integer.parseInt(command.getParam(1));
			if(landIndex >= 0){
				synchronized (player) {
					String fertilizeResult = connectSession.farmInstanceModel.fertilize(player, landIndex, true, null);
					if(fertilizeResult == null){
						Changed changed = new Changed();
						fertilizeResult = connectSession.farmInstanceModel.fertilize(player, landIndex, false, changed);
						if(fertilizeResult != null){
							connectSession.sendGetItem(changed, command.getSerial(), command.getSessionId(), command.getAppType());
							connectSession.sendMessage(fertilizeResult, command.getSerial(), command.getSessionId());
						}
					}else{
						connectSession.sendMessage(fertilizeResult, command.getSerial(), command.getSessionId());
					}
				}
			}else{
				connectSession.sendMessage("土地不存在！", command.getSerial(), command.getSessionId());
			}
			break;
		case FARM_LAND_OPEN:
			landIndex = Integer.parseInt(command.getParam(1));
			if(landIndex >= 0){
				Changed changed = new Changed();
				String landOpenResult = connectSession.farmInstanceModel.landOpen(player, landIndex, false, changed);
				connectSession.sendGetItem(changed, command.getSerial(), command.getSessionId(), command.getAppType());
				connectSession.sendMessage(landOpenResult, command.getSerial(), command.getSessionId());
			}else{
				connectSession.sendMessage("土地不存在！", command.getSerial(), command.getSessionId());
			}
			break;
		case FARM_LAND_LEVELOK:
			landIndex = Integer.parseInt(command.getParam(1));
			if(landIndex >= 0){
				String uplevelResult = connectSession.farmInstanceModel.landLevelUp(player, landIndex, true, null);
				if(uplevelResult == null){
					Changed changed = new Changed();
					uplevelResult = connectSession.farmInstanceModel.landLevelUp(player, landIndex, false, changed);
					connectSession.sendGetItem(changed, command.getSerial(), command.getSessionId(), command.getAppType());
				}
				connectSession.sendMessage(uplevelResult, command.getSerial(), command.getSessionId());
			}else{
				connectSession.sendMessage("土地不存在！", command.getSerial(), command.getSessionId());
			}
			break;
		case FARM_LAND_ABOUT:
			connectSession.sendMessage("您可以将一些种子种在庄园的土地上，待种子成熟后便可以收获果实奖励哦。但果实也会引来吸血鬼的入侵，果实打败吸血鬼会得到金元和物品奖励，果实数量越多，攻击越高得到的金元和奖励就越多哦。金元可以用来开放新土地和升级土地。", command.getSerial(), command.getSessionId());
			break;
		case FARM_LAND_REMOVE:
			landIndex = Integer.parseInt(command.getParam(1));
			if(landIndex >= 0){
				synchronized (player) {
					byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
							new String[] {"2", "1", "是否铲除该种子" + "\n1.铲除\n2.取消",
							"farmInteractive " + FARM_LAND_REMOVE_OK + " " + landIndex, 
							"ok"});
					 UWAPSegment seg = new UWAPSegment(ClientConstants.
	                            GET_FILE_OK, command.getSerial(),
	                            command.getSessionId());
	                    seg.writeShort((short) 31010);
	                    seg.writeShort((short) 2);
	                    seg.write(bytes);
	                    connectSession.write(seg);
				}
			}
			break;
		case FARM_LAND_REMOVE_OK:
			landIndex = Integer.parseInt(command.getParam(1));
			if(landIndex >= 0){
				synchronized(player){
					GameMap gameMap = player.getMap();
					FarmInstance farmInstance = (FarmInstance)gameMap.getInstance(); 
					int ownerid = farmInstance.getOwnerId();
					if(ownerid == player.getId()){//是否是自己庄园
						FarmLandInfo farmLandInfo = connectSession.farmInstanceModel.getFarmLandInfo(player, landIndex);
						if(farmLandInfo.getSeed() > 0){
							farmLandInfo.setSeed(0);
							connectSession.sendMessage("铲除成功", command.getSerial(), command.getSessionId());
						}else{
							connectSession.sendMessage("还没有种子无法铲除", command.getSerial(), command.getSessionId());
						}
					}else{
						connectSession.sendMessage("不是主人的庄园", command.getSerial(), command.getSessionId());
					}
				}
			}
			break;
			
			
		}
	}

}
