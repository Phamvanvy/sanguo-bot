package com.pip.itimes.server.world.game;

import com.pip.itimes.server.world.InstanceDefinition;
import com.pip.itimes.server.world.WorldPlayer;

/**
 * 阵营战场副本模型
 * @author hchen
 *
 */
public interface CampBattlefieldInstanceModel extends InstanceModel {
	/**
	 * 开始
	 * @param instanceID
	 * @param name
	 * @param type
	 * @param levelType
	 * @param forbidEnterTime
	 * @param endTime
	 * @param players
	 * @throws CampBattlefieldException
	 */
	public int start (int instanceID, String name, String type, int levelType, long forbidEnterTime, CampBattlefieldPlayer[] players) throws CampBattlefieldException;
	/**
	 * 是否是战场召唤时间
	 * @param levelType
	 * @param instance
	 * @return
	 */
	public boolean isSummon (int levelType, CampBattlefieldInstance instance);
	/**
	 * 是否超过离线时间限制，是T出战场，否加入战场
	 * @param player
	 * @param currentTime
	 * @param idf
	 * @return
	 */
	public boolean moreThanOffLineTimeLimit (WorldPlayer player, long currentTime, InstanceDefinition idf);
	/**
	 * 战场发奖
	 * @param instance
	 * @param currentTime
	 */
	public void sendPrizes (CampBattlefieldInstance instance, long currentTime);
	/**
	 * 处理
	 * @param currentTime
	 */
	public void process (long currentTime);
	/**
	 * 释放战场
	 * @param instance
	 * @param currentTime
	 */
	public void cancel (CampBattlefieldInstance instance, long currentTime);
	/**
	 * 强制关闭
	 */
	public void shutDown ();
	
	public void exitCampBattlefield (CampBattlefieldInstance instance, int playerID, long currentTime);
}
