package com.pip.itimes.server.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.dao.CampDao;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.ChristmasShowInfo;
import com.pip.itimes.server.stage.Command;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.util.PropertyPool;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.chr.ChristmasConfig;

public class ChristmasProcessor implements CommandProcessor {
	
	private static final Logger log = Logger.getLogger(ConnectSession.class);
	private final int CHR_ITEMID = 201081;			//捐的物品的ID
	
    //采集任务sendCmd上传的type类型常量
    public static final int VIEW_CAMP_COUNT = 1;		//查看捐的个数
    public static final int BRIGHT_ITEM_ONE = 2;		//光明捐1个
    public static final int BRIGHT_ITEM_TEN = 3;		//黑暗捐1个
    public static final int DARK_ITEM_ONE = 4;			//光明捐10个
    public static final int DARK_ITEM_TEN = 5;			//黑暗捐10个
    public static final int CHR_GOTO = 8;				//传送
	
	ConnectSession connectSession;
	
	public static CampDao campDao = new CampDao();
	public static PropertyPool darkPool = null;
	public static PropertyPool brightPool = null;
	public static final String CHR_ITEM_TOTAL = "chrItemTotal";
	public static int darkChrItemTotal = 0;
	public static int brightChrItemTotal = 0;
	public static Map<Integer, ChristmasShowInfo> darkChrItemPlayer = new HashMap<Integer, ChristmasShowInfo>();
    public static Map<Integer, ChristmasShowInfo> brightChrItemPlayer = new HashMap<Integer, ChristmasShowInfo>();
    public static List darkTopList = new ArrayList();
    public static List brightTopList = new ArrayList();
	
	public ChristmasProcessor(ConnectSession connectSession){
		this.connectSession = connectSession;
		initPool();
	}
	
	static public void initPool(){
		synchronized (campDao) {
			if(campDao == null){
				campDao = new CampDao();
			}
		}
		try {
			synchronized (darkPool){
				if(darkPool == null){
					darkPool.parse(campDao.getCampPool(Utils.CAMP_DARK));
				}
			}
			synchronized (brightPool){
				if(brightPool == null){
					brightPool.parse(campDao.getCampPool(Utils.CAMP_BRIGHT));
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void setChrItemTotal(int camp, int total){
		if(camp == Utils.CAMP_DARK){
			darkChrItemTotal = total;
		}else if(camp == Utils.CAMP_BRIGHT){
			brightChrItemTotal = total;
		}
	}
	static public int getChrItemTotal(int camp){
		if(camp == Utils.CAMP_DARK){
			return darkChrItemTotal;
		}else if(camp == Utils.CAMP_BRIGHT){
			return brightChrItemTotal;
		}
		return 0;
	}
	static public void resetChrItemTotal(int camp){
		if(camp == Utils.CAMP_DARK){
			synchronized (darkPool){
				darkPool.setInt(CHR_ITEM_TOTAL, getChrItemTotal(camp));
			}
		}else if(camp == Utils.CAMP_BRIGHT){
			synchronized (brightPool){
				brightPool.setInt(CHR_ITEM_TOTAL, getChrItemTotal(camp));
			}
		}
	}
	
	public static Map<Integer, ChristmasShowInfo> getDarkChrItemPlayer () {
		return darkChrItemPlayer;
	}
	
	public static Map<Integer, ChristmasShowInfo> getBrightChrItemPlayer () {
		return brightChrItemPlayer;
	}
	
	static public String getPool(int camp){
		if(camp == Utils.CAMP_BRIGHT){
			return brightPool.toString();
		}else if(camp == Utils.CAMP_DARK){
			return darkPool.toString();
		}
		return "";
	}
	
	/**
	 * 保存阵营的总数
	 */
	static public void saveTotal(){
		try {
			synchronized (campDao){
				resetChrItemTotal(Utils.CAMP_BRIGHT);
//				campDao.updateCampPool(Utils.CAMP_BRIGHT, getPool(Utils.CAMP_BRIGHT));
				resetChrItemTotal(Utils.CAMP_DARK);
//				campDao.updateCampPool(Utils.CAMP_DARK, getPool(Utils.CAMP_DARK));
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * 捐赠物品
	 * @param player
	 * @param count
	 */
	public void playerAddItem(WorldPlayer player, int count){
		int camp = player.getCamp();
		if(camp == Utils.CAMP_BRIGHT){
			synchronized (brightChrItemPlayer){
				if(brightChrItemPlayer.containsKey(player.getId())){
					ChristmasShowInfo csi = (ChristmasShowInfo) brightChrItemPlayer.get(player.getId());
					csi.setCount(csi.getCount() + count);
					brightChrItemPlayer.put(player.getId(), csi);
				}else{
					ChristmasShowInfo csi = new ChristmasShowInfo (player.getId(), player.getLevel(), player.getPlayerName(), count);
					brightChrItemPlayer.put(player.getId(), csi);
				}
			}
		}else if(camp == Utils.CAMP_DARK){
			synchronized (darkChrItemPlayer){
				if(darkChrItemPlayer.containsKey(player.getId())){
					ChristmasShowInfo csi = (ChristmasShowInfo) darkChrItemPlayer.get(player.getId());
					csi.setCount(csi.getCount() + count);
					darkChrItemPlayer.put(player.getId(), csi);
				}else{
					ChristmasShowInfo csi = new ChristmasShowInfo (player.getId(), player.getLevel(), player.getPlayerName(), count);
					darkChrItemPlayer.put(player.getId(), csi);
				}
			}
		}
	}
	
	/**
	 * 排序
	 * @param type
	 * @return List
	 */
	public static List sort (int type) {
		Map<Integer,ChristmasShowInfo> map_Data = null;
		switch (type) {
		case ChristmasConfig.VIEW_BRIGHT_LIST:
			map_Data = getBrightChrItemPlayer();
			break;
		case ChristmasConfig.VIEW_DARK_LIST:
			map_Data = getDarkChrItemPlayer();
			break;
		}
		//将map转换成list形态，以便進行排序
		List<Map.Entry<Integer, ChristmasShowInfo>> list_Data = new ArrayList<Map.Entry<Integer, ChristmasShowInfo>>(map_Data.entrySet());
		//排序
		Collections.sort(list_Data, new Comparator<Map.Entry<Integer, ChristmasShowInfo>> () {
			public int compare(Map.Entry<Integer, ChristmasShowInfo> o1, Map.Entry<Integer, ChristmasShowInfo> o2){
				return (o2.getValue().getCount() - o1.getValue().getCount());
			}
		});
		return list_Data;
	}
	
	/**
	 * 设置排行榜
	 */
	public static void setTopList () {
		darkTopList = sort(ChristmasConfig.VIEW_DARK_LIST);
		brightTopList = sort(ChristmasConfig.VIEW_BRIGHT_LIST);
	}
	
	/**
	 * 获得排行榜
	 * @param type
	 * @return List
	 */
	public List getTopList (int type) {
		if (type == ChristmasConfig.VIEW_DARK_LIST) {
			return darkTopList;
		} else if (type == ChristmasConfig.VIEW_BRIGHT_LIST) {
			return brightTopList;
		}
		return null;
	}
	
	public void process(WorldPlayer player, Command command) throws Exception {
		int type = Integer.parseInt(command.getParam(0));
		if(type != VIEW_CAMP_COUNT && type != ChristmasConfig.VIEW_DARK_LIST && type != ChristmasConfig.VIEW_BRIGHT_LIST
				&& type != CHR_GOTO
				){
			if (ChristmasConfig.currentSegment == ChristmasConfig.STAGE_NOT_STARTED) {
				connectSession.sendMessage("现在不是捐物资的时间，请过会再来！", 
						command.getSerial(), command.getSessionId());
				return;
			}
		}
		switch(type){
		case VIEW_CAMP_COUNT:				//查看两个阵营捐的数量
			connectSession.sendMessage("光明阵营捐了<cff0000>" + getChrItemTotal(Utils.CAMP_BRIGHT) + "</c>个" + 
					"\n黑暗阵营捐了<cff0000>" + getChrItemTotal(Utils.CAMP_DARK) + "</c>个", 
					command.getSerial(), command.getSessionId());
			break;
		case BRIGHT_ITEM_ONE:				//捐1个	圣诞老人穿过的袜子
		case DARK_ITEM_ONE:
			if((type == BRIGHT_ITEM_ONE && player.getCamp() != Utils.CAMP_BRIGHT) || type == DARK_ITEM_ONE && player.getCamp() != Utils.CAMP_DARK){
				connectSession.sendMessage("你不是本阵营的人！", command.getSerial(), command.getSessionId());
				throw new ITimesException("你不是本阵营的人！", command.getSerial(), command.getSessionId(), command.getAppType());
			}else{
				if(player.hasItem(CHR_ITEMID)){
					Changed changed = new Changed();
					player.completeRemoveItem(CHR_ITEMID, 1, changed);
					if(ChristmasConfig.itemid != 0){
						IItemTemplate item = Items.getTemplate(ChristmasConfig.itemid);
						if(item != null){
							player.completeAddItem(item.newInstance(), 1, changed, player.getClientDataVersion());
						}
					}
	                connectSession.sendGetItem(changed, command.getSerial(), command.getSessionId(),
	                            (byte) 12);
	                player.setChrItemCount(player.getChrItemCount() + 1);
	                playerAddItem(player, 1);
	                if(type == DARK_ITEM_ONE){
	                	setChrItemTotal(Utils.CAMP_DARK, getChrItemTotal(Utils.CAMP_DARK) + 1);
	                }else{
	                	setChrItemTotal(Utils.CAMP_BRIGHT, getChrItemTotal(Utils.CAMP_BRIGHT) + 1);
	                }
				}else{
					IItemTemplate item = Items.getTemplate(CHR_ITEMID);
					connectSession.sendMessage("您没有足够的(<cff0000>" + item.getName() + "</c>)", command.getSerial(), command.getSessionId());
				}
			}
			break;
		case BRIGHT_ITEM_TEN:				//捐10个
		case DARK_ITEM_TEN:
			if((type == BRIGHT_ITEM_TEN && player.getCamp() != Utils.CAMP_BRIGHT) || (type == DARK_ITEM_TEN && player.getCamp() != Utils.CAMP_DARK)){
				connectSession.sendMessage("你不是本阵营的人！", command.getSerial(), command.getSessionId());
				throw new ITimesException("你不是本阵营的人！", command.getSerial(), command.getSessionId(), command.getAppType());
			}else{
				if(player.hasItem(CHR_ITEMID, 10)){
					Changed changed = new Changed();
					player.completeRemoveItem(CHR_ITEMID, 10, changed);
					IItemTemplate item = Items.getTemplate(ChristmasConfig.itemid);
					if(item != null){
						player.completeAddItem(item.newInstance(), 10, changed, player.getClientDataVersion());
					}
	                connectSession.sendGetItem(changed, command.getSerial(), command.getSessionId(),
	                            (byte) 12);
	                player.setChrItemCount(player.getChrItemCount() + 10);
	                playerAddItem(player, 10);
	                if(type == DARK_ITEM_TEN){
	                	setChrItemTotal(Utils.CAMP_DARK, getChrItemTotal(Utils.CAMP_DARK) + 10);
	                }else{
	                	setChrItemTotal(Utils.CAMP_BRIGHT, getChrItemTotal(Utils.CAMP_BRIGHT) + 10);
	                }
				}else{
					IItemTemplate item = Items.getTemplate(CHR_ITEMID);
					connectSession.sendMessage("您没有足够的(<cff0000>" + item.getName() + "</c>)", command.getSerial(), command.getSessionId());
				}
			}
			break;
		case ChristmasConfig.VIEW_BRIGHT_LIST:	// 查看光明排行榜
		case ChristmasConfig.VIEW_DARK_LIST:	// 查看黑暗排行榜
			if ((type == ChristmasConfig.VIEW_BRIGHT_LIST && player.getCamp() != Utils.CAMP_BRIGHT)
					|| (type == ChristmasConfig.VIEW_DARK_LIST && player.getCamp() != Utils.CAMP_DARK)) {
				throw new ITimesException("您不是本阵营的人，无法查看，我很钦佩您的勇气。", command.getSerial(), command.getSessionId(), command.getAppType());
			} else {
				ChristmasConfig.setCurrentSegment(ChristmasConfig.calcCurrentSegment());
				if (ChristmasConfig.currentSegment == ChristmasConfig.STAGE_NOT_STARTED) {
					List list = getTopList(type);
					if (list == null || list.isEmpty() || list.size() == 0) {
						connectSession.sendMessage("暂时无排行，请稍后查询。", command.getSerial(), command.getSessionId());
					} else {
						UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
						seg.writeShort((short) 10267);
						seg.writeString("排行榜");
						seg.write((byte) 0);
						seg.writeShort((short) Math.min(list.size(), ChristmasConfig.TOP));
						for(int i = 0; i < Math.min(list.size(), ChristmasConfig.TOP); i++) {
							Map.Entry<Integer, ChristmasShowInfo> infoMap = (Entry<Integer, ChristmasShowInfo>) list.get(i);
							ChristmasShowInfo showInfo = infoMap.getValue();
							if(showInfo == null || showInfo.getPlayerName()== null ||((showInfo.getPlayerName().equals("") && showInfo.getPlayerName().length() ==0))){
								continue;
							}
							seg.writeInt(showInfo.getId());
							seg.writeString(i + 1 + "." + showInfo.getPlayerName() + " " + showInfo.getCount() + "个");
							seg.writeInt(Utils.CLR_WHITE);
						}
						connectSession.connectService.writeTo(seg, player.getId());
					}
				} else {
					connectSession.sendMessage("请活动结束后再来查看。", command.getSerial(), command.getSessionId());
				}
			}
			break;
		case CHR_GOTO:
			if(player.getTeam()!=null){
				connectSession.sendMessage("组队不能进入活动区域哦，请解散队伍再进入吧。", command.getSerial(), command.getSessionId());
			}else{
				connectSession.sendGotoMap(player.getId(), (short)Integer.parseInt(command.getParam(1)), 
						(short)Integer.parseInt(command.getParam(2)), (short)Integer.parseInt(command.getParam(3)));
			}
			break;
		}
	}
}
