package com.pip.servermgr.report.itimes;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pip.itimes.net.ServerConstants;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.stage.EquipmentHelper;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.stage.PlayerDataException;
import com.pip.servermgr.report.IPlayer;
import com.pip.util.ResultRow;

public class ITimes_Player implements IPlayer {
	public byte[] currenttask;
	public byte[] finishedtask;
	public int firstItemID;
	public int firstLevel;
	public int consumeMoney;
	
	public Player innerPlayer;
	public PlayerData playerData;
	
	public static ITimes_Player parse(ResultRow row) throws IOException {
		ITimes_Player ret = new ITimes_Player();
		
		// 创建一个幻想服务器的Player对象
		ret.innerPlayer = new Player();
		ret.innerPlayer.setId(row.getInt(1));
		ret.innerPlayer.setAccountId(row.getInt(2));
		ret.innerPlayer.setPlayerName(row.getString(3));
		ret.innerPlayer.setLevel(row.getInt(4));
		ret.innerPlayer.setMapId((short)row.getInt(5));
		ret.innerPlayer.setX((short)row.getInt(6));
		ret.innerPlayer.setY((short)row.getInt(7));
		ret.innerPlayer.setSex((byte)row.getInt(8));
		ret.innerPlayer.setExp(row.getInt(9));
		ret.innerPlayer.setMoeny(row.getInt(10));
		ret.innerPlayer.setTongId(row.getInt(11));
		ret.innerPlayer.setCreateTime(row.getDate(12));
		ret.innerPlayer.setLastLoginTime(row.getDate(13));
		ret.innerPlayer.setCredit(row.getInt(14));
		ret.innerPlayer.setLeavePoints(row.getInt(15));
		ret.innerPlayer.setBasicItems((byte[])row.getObject(16));
		ret.innerPlayer.setPets((byte[])row.getObject(17));
		ret.innerPlayer.setMetaItems((byte[])row.getObject(18));
		ret.innerPlayer.setEquipments((byte[])row.getObject(19));
		ret.innerPlayer.setUsedEquipments((byte[])row.getObject(20));
		ret.innerPlayer.setFriends((byte[])row.getObject(21));
		ret.innerPlayer.setAbilityPoints(row.getInt(22));
		ret.innerPlayer.setPoint(row.getInt(23));
		ret.innerPlayer.setPetId(row.getInt(24));
		ret.innerPlayer.setAddedGridSize(row.getInt(25));
		ret.innerPlayer.setAbilityTimes(row.getInt(26));
		ret.innerPlayer.setValid(row.getInt(27) == 1);
		ret.innerPlayer.setHouseLevel(row.getInt(28));
		ret.innerPlayer.setFace((short)row.getInt(29));
		ret.innerPlayer.setAbilities((byte[])row.getObject(30));
		ret.innerPlayer.setTechSkills((byte[])row.getObject(31));
		ret.innerPlayer.setPetSize(row.getInt(32));
		ret.innerPlayer.setKills(row.getInt(33));
		ret.innerPlayer.setSneaks(row.getInt(34));
		ret.innerPlayer.setArenaLevel(row.getInt(35));
		ret.innerPlayer.setArenaPoint(row.getInt(36));
		ret.innerPlayer.setArenaLevel2(row.getInt(37));
		ret.innerPlayer.setArenaLevel3(row.getInt(38));
		ret.innerPlayer.setCamp((byte)row.getInt(39));

		ret.innerPlayer.setCampwin(row.getInt(40));
		ret.innerPlayer.setCamplost(row.getInt(41));
		ret.innerPlayer.setCampcredit(row.getInt(42));
		ret.innerPlayer.setImage((byte[])row.getObject(43));
		
		try {
			ret.playerData = new PlayerData(ret.innerPlayer);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("解析数据错误");
		}
		
		return ret;
	}
	
	/**
	 * 取得某个统计项数据。
	 * @param type 参见Sanguo_ReportEngine里的常量
	 * @return 可能是Boolean, Integer, Float
	 */
	public Object getValue(int type) {
		switch (type) {
		case ITimes_ReportEngine.TYPE_ISALIVE:
			// 是否存活
			return (System.currentTimeMillis() - innerPlayer.getLastLoginTime().getTime()) / 86400000L < 7;
		case ITimes_ReportEngine.TYPE_LIVETIME:
			// 存活天数
			return (int)((innerPlayer.getLastLoginTime().getTime() - innerPlayer.getCreateTime().getTime()) / 86400000L);
		case ITimes_ReportEngine.TYPE_LEVEL:
			// 等级
			return innerPlayer.getLevel();
		case ITimes_ReportEngine.TYPE_ISPAY:
			// 是否付费
			return consumeMoney > 0;
		case ITimes_ReportEngine.TYPE_PAY:
			// 消费金额
			return (int)(consumeMoney / 3.6);
		case ITimes_ReportEngine.TYPE_MONEY:
			// 金钱
			return innerPlayer.getMoeny();
		case ITimes_ReportEngine.TYPE_FACTION:
			// 阵营
			return (int)innerPlayer.getCamp();
		case ITimes_ReportEngine.TYPE_EQULEVEL:
			// 装备总价值In
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_JEWELLEVEL:
			// 宝石总等级
			//sky add
			
			return 0;
		case ITimes_ReportEngine.TYPE_HOLECOUNT:
			// 镶嵌宝石数
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_STARLEVEL:
			// 总星级
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_ZIZHILEVEL:
			// 总资质等级
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_SKILLPOINT:
			// 技能点使用率
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_CLAZZ:
			// 职业
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_ATTRPOINT:
			// 属性点使用率
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_PETLVL:
			// 最高宠物等级
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_PETCOUNT:
			// 宠物数量
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_PETEQULEVEL:
			// 宠物装备总价值
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_PETJEWELLEVEL:
			// 宠物宝石总等级
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_PETHOLECOUNT:
			// 宠物镶嵌宝石数
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_PETSTARLEVEL:
			// 宠物装备总星级
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_PETZIZHILEVEL:
			// 宠物装备总资质等级
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_MANULEVEL:
			// 打造等级
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_FRIENDCOUNT:
			// 好友数
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_FABAOCOUNT:
			// 法宝数
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_FABAOLEVEL:
			// 最高法宝等级
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_FIRSTBUYITEM:
			// 首次购买物品
			return firstItemID;
		case ITimes_ReportEngine.TYPE_HORSECOUNT:
			// 坐骑数
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_ISHORSEENH:
			// 是否强化过坐骑
			// TODO Boolean
			return false;
		case ITimes_ReportEngine.TYPE_FINISHQUESTCOUNT:
			// 完成任务数
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_QUESTCOUNT:
			// 当前任务数
			// TODO Integer
			return 0;
		case ITimes_ReportEngine.TYPE_HASTONG:
			// 是否有公会
			return innerPlayer.getTongId() > 0;
		case ITimes_ReportEngine.TYPE_HOUSELEVEL:
			// 房屋等级
			return innerPlayer.getHouseLevel();
		case ITimes_ReportEngine.TYPE_FACE:
			// 形象
			return (int)innerPlayer.getFace();
		case ITimes_ReportEngine.TYPE_KILLS:
			// 杀人数
			return innerPlayer.getKills();
		case ITimes_ReportEngine.TYPE_SNEAKS:
			// 偷袭数
			return innerPlayer.getSneaks();
		case ITimes_ReportEngine.TYPE_ARENALEVEL:
			// 竞技场等级
			return innerPlayer.getArenaLevel();
		case ITimes_ReportEngine.TYPE_ARENAPOINT:
			// 竞技场积分
			return innerPlayer.getArenaPoint();
		case ITimes_ReportEngine.TYPE_ARENALEVEL2:
			// 竞技场等级2
			return innerPlayer.getArenaLevel2();
		case ITimes_ReportEngine.TYPE_ARENALEVEL3:
			// 竞技场等级3
			return innerPlayer.getArenaLevel3();
		case ITimes_ReportEngine.TYPE_CAMPWIN:
			// 阵营杀人数
			return innerPlayer.getCampwin();
		case ITimes_ReportEngine.TYPE_CAMPLOST:
			// 阵营被杀数
			return innerPlayer.getCamplost();
		case ITimes_ReportEngine.TYPE_CAMPCREDIT:
			// 阵营声望
			return innerPlayer.getCampcredit();
		}
		return 0;
	}
	
	//sky add 201107
	
	// 宝石总等级
	private int get_player_totaljewellevel() {
        int result = 0;
        try {
        	
        	List equipments = get_player_equipments(innerPlayer.getEquipments());
        	Grid[] usedEquipments = get_player_usedequipments(innerPlayer.getUsedEquipments());
        	
            
            return result;
        } catch (Exception e) {
            return result;
        }
    }
	
	
	//base function
	private List get_player_equipments(byte[] bytes) {
		List equipments = new ArrayList();
        try {
        	
        	if (bytes != null && bytes.length > 2) {
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                DataInputStream dis = new DataInputStream(bis);
                byte version = dis.readByte();
                short size = dis.readShort();
                for (int i = 0; i < size; i++) {
                    IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
                    if (equ == null)
                        return null;
                    Grid grid = new Grid();
                    grid.item = equ;
                    grid.count = 1;
                    equipments.add(grid);
                }
            }
            
            return equipments;
        } catch (Exception e) {
            return equipments;
        }
    }
	
	private Grid[] get_player_usedequipments(byte[] bytes) {
		Grid[] usedEquipments = new Grid[9];
        try {
        	
        	if (bytes != null && bytes.length > 2) {
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                DataInputStream dis = new DataInputStream(bis);
                byte version = dis.readByte();
                short size = dis.readShort();
                for (int i = 0; i < size; i++) {
                    IEquipment equ = EquipmentHelper.createFromDbBytes(version,dis);
                    if (equ == null)
                        return null;
                    Grid grid = new Grid();
                    grid.item = equ;
                    grid.count = 1;
                    usedEquipments[equ.getPart()] = grid;
                }
            }            
            return usedEquipments;
        } catch (Exception e) {
            return usedEquipments;
        }
    }
}
