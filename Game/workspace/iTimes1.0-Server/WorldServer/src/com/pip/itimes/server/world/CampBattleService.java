package com.pip.itimes.server.world;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.stage.CampBuff;
import com.pip.itimes.server.stage.CampBuffConfig;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.ChristmasShowInfo;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.chr.ChristmasConfig;

/**
 * 用于阵营战场的服务
 *
 */

public class CampBattleService {
	
	private static final Logger log = Logger.getLogger(CampBattleService.class);
	protected MailService mailService;
	protected PlayerService playerService;
	protected ConnectService connectService;
	protected BufService bufService;
	protected ChatService chatService;
	
	private AtomicInteger bufId = new AtomicInteger(0);
	public static List<CampBuff> brightCampBuffList = new ArrayList();
	public static List<CampBuff> darkCampBuffList = new ArrayList();
	public static CampBuff brightCampBuff = null;
	public static CampBuff darkCampBuff = null;
	
	public static final int GOOD_MAN_CARD_ID = 201055;
	 
	public CampBattleService () {
	 
	}
	 
	public void setMailService(MailService mailService) {
      this.mailService = mailService;
	}
	 
	public void setPlayerService (PlayerService playerService) {
		this.playerService = playerService;
	}
	 
	public void setConnectService (ConnectService connectService) {
		this.connectService = connectService;
	}
	
	public void setChatService (ChatService chatService) {
		this.chatService = chatService;
	}
	
	public void setBufService (BufService bufService) {
		this.bufService = bufService;
	}
	
	/**
	 * 获得胜方所有的捐献者
	 * @return playerMap
	 */
	public Map<Integer, ChristmasShowInfo> getVictoryPlayer () {
		Map<Integer, ChristmasShowInfo> playerMap = null;
		if (ChristmasProcessor.darkChrItemTotal > ChristmasProcessor.brightChrItemTotal) {
			playerMap = ChristmasProcessor.darkChrItemPlayer;
		} else if (ChristmasProcessor.brightChrItemTotal > ChristmasProcessor.darkChrItemTotal) {
			playerMap = ChristmasProcessor.brightChrItemPlayer;
		}
		return playerMap;
	}
	
	/**
	 * 获得胜方阵营
	 * @return ret
	 */
	public int getVictoryCamp () {
		int ret = 0;
		if (ChristmasProcessor.darkChrItemTotal > ChristmasProcessor.brightChrItemTotal) {
			ret = Utils.CAMP_DARK;
		} else if (ChristmasProcessor.brightChrItemTotal > ChristmasProcessor.darkChrItemTotal) {
			ret = Utils.CAMP_BRIGHT;
		} else {
			ret = -1;
		}
		return ret;
	}
	
	/**
	 * 获得胜方排行榜
	 * @param type
	 * @return List
	 */
	public List getVictoryTopTen () {
		if (ChristmasProcessor.darkChrItemTotal > ChristmasProcessor.brightChrItemTotal) {
			return ChristmasProcessor.darkTopList;
		} else if (ChristmasProcessor.brightChrItemTotal > ChristmasProcessor.darkChrItemTotal) {
			return ChristmasProcessor.brightTopList;
		}
		return null;
	}
	 
	/**
	 * 为上榜者颁发好人卡
	 * @throws Exception 
	 */
	public void sendPrizesToTop (List list) throws Exception {
		for(int i = 0; i < Math.min(list.size(), ChristmasConfig.TOP); i++) {
			Map.Entry<Integer, ChristmasShowInfo> infoMap = (Entry<Integer, ChristmasShowInfo>) list.get(i);
			ChristmasShowInfo showInfo = infoMap.getValue();
			if(showInfo != null && showInfo.getId() > 0){
//				boolean mark = false;
//				WorldPlayer player = playerService.getWorldPlayer(showInfo.getId());
//				if (player == null) {
//					player = playerService.loadWorldPlayer(showInfo.getId());
//					mark = true;
//				}
				WorldPlayer player = playerService.getWorldPlayerAndCatch(showInfo.getId());
				if (player != null) {
					IItem iit = Items.getTemplate(GOOD_MAN_CARD_ID).newInstance();
					byte[] att = ItemUtils.item2dbAttachment(iit, 1);
					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
							iit.getName() + "*" + 1, "感谢您在阵营资源争夺战中为本阵营做出的贡献，由于您捐献的资源数量已经排在捐献榜的前十位，特此发给你这张好人卡作为奖励，希望你再接再厉，继续为阵营做出贡献。", att, 0, true);
					log.info("ChristmasProcessor sendMail Victory gift to playerId[" + player.getId()
							+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
							iit.getItemId() + "] ItemName [" + iit.getName() + "] ItemCount[" + 1 + "]");
				}
//				if (mark) {
//					playerService.savePlayer(player);
//				}
				playerService.releasePlayer(player);
			}
		}
	}
	
	/**
	 * 设置双方的BUFF
	 * @param campType
	 */
	public void setVictoryCampBuff () {
		brightCampBuff = getTodayCampBuff(CampBuffConfig.campBrightBuff);
		darkCampBuff = getTodayCampBuff(CampBuffConfig.campDarkBuff);
		
		int victoryCamp = getVictoryCamp();
		if (victoryCamp == Utils.CAMP_BRIGHT) {
			checkCampBuffList(brightCampBuffList, brightCampBuff);
		} else if (victoryCamp == Utils.CAMP_DARK) {
			checkCampBuffList(darkCampBuffList, darkCampBuff);
		} else if (victoryCamp == -1) {
			checkCampBuffList(brightCampBuffList, brightCampBuff);
			checkCampBuffList(darkCampBuffList, darkCampBuff);
		}
	}
	
	/**
	 * 检查CampBuff是否过期,或者检查新加的BUFF效果是否相同
	 * @param list
	 * @param addBuff
	 */
	public void checkCampBuffList (List<CampBuff> list, CampBuff addBuff) {
		boolean mark = false;
		for (int i = 0; i < list.size(); i ++) {
			CampBuff campBuff = list.get(i);
			if (addBuff != null) {
				if (addBuff.getProperty() == campBuff.getProperty()) {
					list.remove(i);
					list.add(addBuff);
					mark = true;
					break;
				}
			}
			long time = new Date().getTime();
			if (time >= campBuff.getEndTime().getTime()) {
				list.remove(i);
				break;
			}
		}
		if (addBuff != null && !mark) {
			list.add(addBuff);
		}
	}
	
	/**
	 * 获得今天的BUFF
	 * @param buff
	 * @return CampBuff
	 */
	public CampBuff getTodayCampBuff (TreeMap<Integer, CampBuff> buff) {
		Calendar calendar = Calendar.getInstance(); 
		Date date = new Date(); 
		calendar.setTime(date); 
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		CampBuff tmpBuff = buff.get(dayOfWeek);
		if (tmpBuff != null) {
			CampBuff ret = new CampBuff();
			ret.setMessage(tmpBuff.getMessage());
			ret.setProperty(tmpBuff.getProperty());
			ret.setTime(tmpBuff.getTime());
			ret.setUnit(tmpBuff.getUnit());
			ret.setValue(tmpBuff.getValue());
			ret.setEndTime(new Date(tmpBuff.getTime() * 1000L + new Date().getTime()));
			return ret;
		}
		return null;
	}
	
	/**
	 * 为胜方加BUFF
	 * @param campType
	 * @throws Exception 
	 */
	public void addVictoryCampBuff (int campType) throws Exception {
		CampBuff cbuff = null;
		if (campType == Utils.CAMP_BRIGHT) {
			cbuff = brightCampBuff;
		} else if (campType == Utils.CAMP_DARK) {
			cbuff = darkCampBuff;
		}
		if (cbuff != null) {
			Buf buff = new Buf(bufId.incrementAndGet(), (byte)cbuff.getProperty(), cbuff.getValue(), cbuff.getTime(), cbuff.getUnit());
			Iterator ite = bufService.getPlayers().values().iterator();
			while (ite.hasNext()) {
				PlayerData player = (PlayerData) ite.next();
				if (player != null) {
					synchronized (player) {
						if (player.getCamp() == campType) {
							Changed changed = new Changed();
							buff.setTimestamp(System.currentTimeMillis());
							player.addBuf(buff, changed);
							connectService.sendGetItem(changed, player.getId(), (byte) 4);
						}
					}
				}
			}
			chatService.sendCampMessage(-1, "系统", cbuff.getMessage(), campType);
		}
	}
	
	/**
	 * 平局两边都加
	 */
	public void addDrawCampBuff () {
		CampBuff darkCampBuffTmp = darkCampBuff;
		CampBuff brightCampBuffTmp = brightCampBuff;
		Buf darkBuff = new Buf(bufId.incrementAndGet(), (byte)darkCampBuffTmp.getProperty(), darkCampBuffTmp.getValue(), darkCampBuffTmp.getTime(), darkCampBuffTmp.getUnit());
		Buf brightBuff = new Buf(bufId.incrementAndGet(), (byte)brightCampBuffTmp.getProperty(), brightCampBuffTmp.getValue(), brightCampBuffTmp.getTime(), brightCampBuffTmp.getUnit());
		Iterator ite = bufService.getPlayers().values().iterator();
		while (ite.hasNext()) {
			Changed changed = new Changed();
			PlayerData player = (PlayerData) ite.next();
			if (player != null) {
				synchronized (player) {
					if (player.getCamp() == Utils.CAMP_BRIGHT) {
						brightBuff.setTimestamp(System.currentTimeMillis());
						player.addBuf(brightBuff, changed);
					} else if (player.getCamp() == Utils.CAMP_DARK) {
						darkBuff.setTimestamp(System.currentTimeMillis());
						player.addBuf(darkBuff, changed);
					}
					connectService.sendGetItem(changed, player.getId(), (byte) 4);
				}
			}
		}
		chatService.sendCampMessage(-1, "系统", brightCampBuffTmp.getMessage(), Utils.CAMP_BRIGHT);
		chatService.sendCampMessage(-1, "系统", darkCampBuffTmp.getMessage(), Utils.CAMP_DARK);
	}
	
	/**
	 * 
	 * @param player
	 */
	public void addLoginCampBuff (WorldPlayer player) {
		List<CampBuff> list = getCampBuffList(player.getCamp());
		if (list != null) {
			checkCampBuffList(list, null);
			if (list.size() > 0) {
				Changed changed = new Changed();
				for (int i = 0; i < list.size(); i ++) {
					CampBuff campBuff = list.get(i);
					if (player.hasCampBattleBuff(campBuff.getProperty()) == false) {
						int time = (int) (campBuff.getEndTime().getTime() - new Date().getTime()) / 1000;
						Buf buff = new Buf(bufId.incrementAndGet(), (byte)campBuff.getProperty(), campBuff.getValue(), time, campBuff.getUnit());
						buff.setTimestamp(System.currentTimeMillis());
						player.addBuf(buff, changed);
					}
				}
				connectService.sendGetItem(changed, player.getId(), (byte) 4);
			}
		}
	}
	
	public List<CampBuff> getCampBuffList (int campType) {
		if (campType == Utils.CAMP_BRIGHT) {
			return brightCampBuffList;
		} else if (campType == Utils.CAMP_DARK) {
			return darkCampBuffList;
		} else {
			return null;
		}
	}
	
	/**
	 * 为捐赠者发奖
	 * @param map
	 * @throws Exception
	 */
	public void sendPrizesToDonors (Map<Integer, ChristmasShowInfo> map) throws Exception {
		for (Map.Entry<Integer, ChristmasShowInfo> entry : map.entrySet()) {
//			boolean mark = false;
//			WorldPlayer player = playerService.getWorldPlayer(entry.getKey());
//			if (player == null) {
//				player = playerService.loadWorldPlayer(entry.getKey());
//				mark = true;
//			}
			WorldPlayer player = playerService.getWorldPlayerAndCatch(entry.getKey());
			if (player != null) {
				IItem iit = Items.getTemplate(ChristmasConfig.giftId).newInstance();
				byte[] att = ItemUtils.item2dbAttachment(iit, 1);
				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
						iit.getName() + "*" + 1, "感谢您在阵营资源争夺战中为本阵营做出的贡献，由于您在阵营资源争夺战中捐献过资源，特此发给你一个" + iit.getName() + "作为奖励，希望你再接再厉，继续为阵营做出贡献。", att, 0, true);
				log.info("CampBattleService sendPrizesToDonors playerId[" + player.getId()
						+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
						iit.getItemId() + "] ItemName [" + iit.getName() + "] ItemCount[" + 1 + "]");
			}
//			if (mark) {
				//只是发邮件，没必要进行保存角色 而且也没有进行reset();
//				playerService.savePlayer(player);
//			}
			playerService.releasePlayer(player);
		}
	}
	
	/**
	 * 自动发奖，加BUFF，TOPTEN发送好人卡
	 * @throws Exception
	 */
	public void sendPrizes () throws Exception {
		if (ChristmasConfig.currentSegment == ChristmasConfig.STAGE_NOT_STARTED
				&& ChristmasConfig.lastSegment > ChristmasConfig.STAGE_NOT_STARTED) {
			Map<Integer, ChristmasShowInfo> playerMap = new HashMap<Integer, ChristmasShowInfo>();
			// 为获胜方的捐助者发奖
			playerMap = getVictoryPlayer();
			if (playerMap != null) {
				sendPrizesToDonors(playerMap);
			}
			// 为获胜方全体加BUFF
			int campType = getVictoryCamp();
			setVictoryCampBuff();
			if (campType > 0 && darkCampBuff != null && brightCampBuff != null) {
				addVictoryCampBuff(campType);
			} else if (campType == -1 && darkCampBuff != null && brightCampBuff != null) {
				addDrawCampBuff();
			}
			// 为获胜方的前十名发放好人卡
			/**
			 * 2012年11月26日18:13:51 zxyu 关闭前10名好人卡的发放
			 */
//			List topList = getVictoryTopTen();
//			if (topList != null) {
//				sendPrizesToTop(topList);
//			}
		}
		ChristmasConfig.setLastSegment(ChristmasConfig.currentSegment);
	}
	
}
