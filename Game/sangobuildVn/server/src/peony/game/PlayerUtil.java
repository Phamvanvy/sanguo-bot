package peony.game;

import java.util.List;

import peony.game.skill.Skill;

public class PlayerUtil {
	
	public static final int[] LEVELUP_EXP = {
		0,
		5 ,
		6 ,
		8 ,
		15 ,
		30 ,
		57 ,
		101 ,
		169 ,
		267 ,
		405 ,
		590 ,
		834 ,
		1147 ,
		1541 ,
		1692 ,
		2189 ,
		2789 ,
		3504 ,
		4349 ,
		5338 ,
		5561 ,
		6698 ,
		8000 ,
		9484 ,
		11166 ,
		13061 ,
		15189 ,
		17566 ,
		20213 ,
		23148 ,
		26391 ,
		29964 ,
		33888 ,
		38186 ,
		42880 ,
		47994 ,
		53552 ,
		59580 ,
		66103 ,
		79243 ,
		87469 ,
		96320 ,
		105825 ,
		116018 ,
		136693 ,
		149254 ,
		162662 ,
		176953 ,
		192166 ,
		238102 ,
		257728 ,
		278544 ,
		300596 ,
		323932 ,
		392177 ,
		421486 ,
		452407 ,
		485000 ,
		519323 ,
		678866 ,
		857134 ,
		1055465 ,
		1275253 ,
		1517954 ,
		2125094 ,
		2710701 ,
		3358548 ,
		4072675 ,
		4857276 ,
		8929951 ,
		13787227 ,
		22717179 ,
		36504406 ,
		59221585 ,
		95725991 ,
		154947576 ,
		250673567 ,
		405621143 ,
		656294711 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
		1061915854 ,
	};
	
	public static final int[] SKILL_POINT = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
			2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12,
			12, 13, 13, 14, 14, 15, 15, 16, 16, 17, 17, 18, 18, 19, 19, 20, 20,
			21, 21, 22, 22, 23, 23, 24, 24, 25, 25, 26, 26, 27, 27, 28, 28, 29,
			29, 30, 30, 31, 31, 32, 32, 33, 33, 34, 34, 35, 35, 36, 36, 37, 37,
			38, 38, 39, 39, 40, 40, 41, 41, 42, 42, 43, 43, 44, 44, 45, 45, 46,
			46, };
	
//	public static final int[][] BORN_POINT = {{464,109,242},{576,109,242},{640,109,242}};
	public static final int[][] BORN_POINT = {{1395,66,176},{1395,66,176},{1395,66,176}};

	public static final int[][] CREATE_POINT = {{1442,300,70},{1410,300,60},{1426,100,120}}; 
	protected static final byte[] ACTIONBAR_BYTES = {1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0,0,0,0,0,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,2,0,0,0,8,0,0,0,46,2,0,0,2,108,0,0,0,47,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0,-1,-1,-1,-1,-1,0,0,0,0,-1,-1,-1,-1,-1,0,0,0,0,-1,-1,-1,-1,-1,0,0,0,0,-1,-1,-1,-1,-1,0,0,0,0,-1,-1,-1,-1,-1,0,0,0,0,-1,-1,-1,-1,-1,0,0,0,0,-1,-1,-1,-1,-1,0,0,0,0,-1,-1,-1,-1,-1,0,0,0,0,-1,-1,-1,-1,0,-1,-1,-40,-16,-1,-1,-1,-1};
	protected static final int[] INIT_EQUIPMENT = {1000572,1000573,1000570,1000574};
	
	protected static final int[] REFRESH_SKILL_MONEY = {
		10000,
		20000,
		40000,
		80000,
		160000,
		320000,
		640000,
		1280000,
	};
	
	public static final int[] CHAT_COUNT = {
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		10,
		10,
		10,
		10,
		10,
		10,
		10,
		10,
		10,
		10,
		12,
		12,
		12,
		12,
		12,
		12,
		12,
		12,
		12,
		12,
		15,
		15,
		15,
		15,
		15,
		15,
		15,
		15,
		15,
		15,
		20,
		20,
		20,
		20,
		20,
		20,
		20,
		20,
		20,
		20,
		25,
		25,
		25,
		25,
		25,
		25,
		25,
		25,
		25,
		25,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
		30,
	};
	
	public static int getRefreshSkillMoney(int times){
		int index = Math.min(times, REFRESH_SKILL_MONEY.length-1);
		return REFRESH_SKILL_MONEY[index];
	}
	
	public static int getFactionChatCount(int level){
		if(level>=CHAT_COUNT.length)
			return CHAT_COUNT[CHAT_COUNT.length-1];
		return CHAT_COUNT[level];
	}
	
    public static int getUpLevel(int oldLevel, int exp){
        int level = 0;
        int upLevelExp = LEVELUP_EXP[oldLevel];
        while(true){
            if(upLevelExp > exp)
                return level;
            exp -= upLevelExp;
            level++;
            upLevelExp = LEVELUP_EXP[oldLevel + level];
        }
    }

    public static int getUpLevelExp(int oldLevel, int newLevel){
    	if(newLevel==oldLevel+1)
    		return LEVELUP_EXP[oldLevel];
        int ret = 0;
        for(int i = oldLevel; i < newLevel; i++){
            ret += LEVELUP_EXP[i];
        }
        return ret;
    }
    
    
    public static int getGrowSkillPoint(int oldLevel, int newLevel){
    	return SKILL_POINT[newLevel] - SKILL_POINT[oldLevel];
    }
	
	public static Player createPlayer(String name,int sex,int clazz,int faction,int accountId){
		if(sex<0||sex>1)
			return null;
		if(clazz<0||clazz>3)
			return null;
		if(faction<GameObject.FACTION_WEI||faction>GameObject.FACTION_WU)
			return null;
		Player player = new Player(accountId,name,sex,clazz);
		player.level = 1;
		player.faction = faction;
		player.propertyPoint = 2;
		player.skillPoint = 0;
		player.state = GameObject.STATE_IDLE;
		player.direct = Unit.DIRECT_DOWN;
		player.actionBarOptions = ACTIONBAR_BYTES;
		int mapId = 0;
		int x = 0;
		int y = 0;
		player.pool.setInt(Player.PROPERTY_GATHER_ABILITY, 1); // 初始化采集技能熟练度
		player.pool.setInt(Player.PROPERTY_PRODUCE_ABILITY, 1); // 初始化打造技能熟练度
//		  魏国：0xc0 288，74
//		  蜀国：0xd0 301，63
//		  吴国：0x180 74，153

		if(faction==GameObject.FACTION_WEI){
			mapId = BORN_POINT[0][0];
			x = BORN_POINT[0][1];
			y = BORN_POINT[0][2];
			player.pool.setString("leavecontry", CREATE_POINT[0][0] + "," + CREATE_POINT[0][1] + "," + CREATE_POINT[0][2]);
			// 河东副本入口
			player.pool.setString("goinstance", "161,211,105");
		} else if(faction==GameObject.FACTION_SHU){
			mapId = BORN_POINT[1][0];
			x = BORN_POINT[1][1];
			y = BORN_POINT[1][2];
			player.pool.setString("leavecontry", CREATE_POINT[1][0] + "," + CREATE_POINT[1][1] + "," + CREATE_POINT[1][2]);
			// 河东副本入口
			player.pool.setString("goinstance", "177,550,97");
		} else if(faction==GameObject.FACTION_WU){
			mapId = BORN_POINT[2][0];
			x = BORN_POINT[2][1];
			y = BORN_POINT[2][2];
			player.pool.setString("leavecontry", CREATE_POINT[2][0] + "," + CREATE_POINT[2][1] + "," + CREATE_POINT[2][2]);
			// 河东副本入口
			player.pool.setString("goinstance", "417,77,565");
		}
		
		player.map = new VMapReference();
		player.map.id = mapId;
		player.x = x;
		player.y = y;
		
		List<Skill> skills = ObjectAccessor.getPlayerInitSkills(player.clazz);
		for(Skill skill:skills){
			player.skills.addSkill(skill, null, false);
		}
		player.chatOptions = ChatOptions.newDefaultChatOptions();
		player.refreshProperties(false);
		player.hp = player.maxhp;
		player.mp = player.maxmp;
		Gain gain = new Gain();
		GameItem item = ObjectAccessor.createGameItem(8); 
		gain.addGainItem(item, 10);
		item = ObjectAccessor.createGameItem(620);
		gain.addGainItem(item, 10);
		GameItem equ = ObjectAccessor.createGameItem(INIT_EQUIPMENT[player.clazz]);
		gain.addGainItem(equ, 1);
		PlayerTransaction tx = player.newTransaction("CCL");
		player.bag.addGain(gain, tx, false);
		tx.commit();
		player.equip(equ.template.id, equ.instanceId, -1);
		player.setCredit(0, "");
		player.activePower = 100;
//		player.equip(itemId, instanceId, serial)
		player.changed.clean();
		return player;
	}
}
