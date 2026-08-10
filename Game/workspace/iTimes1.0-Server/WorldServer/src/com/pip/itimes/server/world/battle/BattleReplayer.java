package com.pip.itimes.server.world.battle;

import java.util.Enumeration;
import java.util.Vector;

import com.pip.itimes.net.UWAPData;
import com.pip.itimes.server.stage.BasicItem;
import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Effect;
import com.pip.itimes.server.stage.IEffectItem;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.Monster;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.util.TestRandom.RandomMake;
import com.pip.itimes.server.world.WorldPlayer;

/**
 * @author wpjiang
 *	有关战斗回放的计算，每次战斗都会有一个，，一个玩家只有一份，如果有两个视为作弊
 */
public class BattleReplayer{
	
	/**
	 * 战斗是否有效
	 */
	private int normal; 
	
	public int getNormal() {
		return normal;
	}


	public void setNormal(int normal) {
		this.normal = normal;
	}

	/**
	 * @return 检测是否合法
	 */
	public boolean checkNoraml(){
		return (normal == BattleReplayerService.nomal);
	}
	public RandomMake getRandom() {
		return random;
	}


	public void setRandom(RandomMake random) {
		this.random = random;
	}


	public int getBoutCount() {
		return boutCount;
	}


	public void setBoutCount(int boutCount) {
		this.boutCount = boutCount;
	}


	public int getMgId() {
		return mgId;
	}


	public void setMgId(int mgId) {
		this.mgId = mgId;
	}

	public int getPlayerSkillId() {
		return playerSkillId;
	}


	public void setPlayerSkillId(int playerSkillId) {
		this.playerSkillId = playerSkillId;
	}


	public byte getPlayerTarget() {
		return playerTarget;
	}


	public void setPlayerTarget(byte playerTarget) {
		this.playerTarget = playerTarget;
	}

	/**
	 * 每次客户端战斗的时候下发的随机数
	 */
	private RandomMake random;
	
	/**
	 * 当前的战斗回合数
	 */
	private int boutCount;
	
	/**
	 * 当前战斗下的怪物id
	 */
	private int mgId;
	
	
	public BattleSprite getPlayer() {
		return player;
	}


	public void setPlayer(BattleSprite player) {
		this.player = player;
	}


	public BattleSprite[] getMonsters() {
		return monsters;
	}


	public void setMonsters(BattleSprite[] monsters) {
		this.monsters = monsters;
	}

	private BattleSprite player;
	/**
	 * 当前的怪物
	 */
	private BattleSprite[] monsters;
	
	
	/**
	 * 玩家宠物
	 */
	private BattleSprite playerPet;

	
	/**
	 * 玩家使用的技能
	 */
	private int playerSkillId; 
	
	/**
	 * 玩家的目标
	 */
	private byte playerTarget; 
	
	
	/**
	 * 玩家宠物使用的技能
	 */
	private int playerPetSkillId; 
	
	public int getPlayerPetSkillId() {
		return playerPetSkillId;
	}


	public void setPlayerPetSkillId(int playerPetSkillId) {
		this.playerPetSkillId = playerPetSkillId;
	}


	public byte getPlayerPetTarget() {
		return playerPetTarget;
	}


	public void setPlayerPetTarget(byte playerPetTarget) {
		this.playerPetTarget = playerPetTarget;
	}


	/**
	 * 玩家宠物的目标
	 */
	private byte playerPetTarget;
	
	
	
	
	
	/**
	 * 玩家的血量
	 *//*
	private int playerHp;
	
	*//**
	 * 玩家的蓝量
	 *//*
	private int playerMp;
	
	*//**
	 * 玩家的宠物血量
	 *//*
	private int petHp;
	
	*//**
	 * 玩家宠物魔法量
	 *//*
	private int petMp;*/
	
	private WorldPlayer playerThis;
	public BattleReplayer(BattleReplayerService battleReplayerService,
			BattleStrategy normalClientStrategy, int boundCount, int monsterId, 
			Monster[] monster, int playerSkillId, byte playerTarget,
			int playerPetSkillId2, byte playerPetTarget, int bountSeed,
			int[][] monsterProperty, WorldPlayer player) {
		// TODO Auto-generated constructor stub
		this.boutCount = 0;
		this.mgId = monsterId;
		this.random = new RandomMake();
		random.setSeed(bountSeed);
		init(player, monster, random);
		this.playerThis = player;
		this.normal = battleReplayerService.nomal;
	}

	/**
	 * @param monsterProperty 
	 * @param bountSeed 
	 * @param playerPetTarget 
	 * @param playerPetSkillId 
	 * @param playerTarget2 
	 * @param playerSkillId2 
	 * @param monsterId 
	 * @param boundCount 
	 * @return 对玩家的操作进行合法性检查，如果合法返回0，不合法返回错误类型
	 */
	public byte check(int boundCount, int monsterId, int playerSkillId, byte playerTarget, int playerPetSkillId, 
			byte playerPetTarget, int[][] monsterProperty, WorldPlayer player,
			int playerHp, int playerMp, int petHp, int petMp, int bountSeed){
		byte success = BattleReplayerService.nomal;
		//首先检测
		if(boundCount == this.boutCount + 1){//回合数限制
			//回合数没有问题
			success = checkRandomSeed(bountSeed);
			if(success != BattleReplayerService.nomal){
				return success;
			}
			//进行战斗计算
			success = checkDamage(playerSkillId, playerTarget, playerPetSkillId, playerPetTarget,
					playerHp, playerMp, petHp, petMp, boundCount, monsterProperty);
			this.boutCount = boundCount;
			
			//检测战斗中怪物是否已经全部死亡
			
		}else{//回合数作弊
			success = BattleReplayerService.tryAutoBattleCountNoSerial;
		}
		
		return success;
	}
	
	public byte checkRandomSeed(int bountSeed){
		byte seedFlag = BattleReplayerService.nomal;
		if(this.random.getOrginSeed() != bountSeed){
			seedFlag  = BattleReplayerService.tryAutoBattleSeedFinal;
		}
		return seedFlag;
	}
	
	//进行消耗的血蓝计算
	public byte checkDamage(int playerSkillId, byte playerTarget, int petSkillId, byte petTarget,
			int playerHp, int playerMp, int petHp, int petMp, int boundCount, int[][] monsterProperty){
		byte successFlag = BattleReplayerService.nomal;
		//battleBout(new BattleSprite[]{this.player}, this.monsters, new BattleSprite[]{this.playerPet}, new BattleSprite[0], boundCount, rand);
		playerSkillId = (short)(playerSkillId & 0xFFFF);
		Skill skill = Skill.getSkill(playerSkillId);
		int[][] damage = new int[3][2]; //检测怪物被伤害值
		if(playerSkillId == skill.SKILL_ATTACK){//计算人物伤害
			//processPlayerAttack(damage, playerSkillId, playerTarget);
		}
		return successFlag;
	}
	
	/**
	 * @param damage
	 * @param playerSkillId
	 * @param playerTarget
	 * 用玩家最的攻击力来进行计算
	 */
	protected void processPlayerAttack(int[][]damage, int playerSkillId, byte playerTarget){
		 int[] battleResult = this.player.doBattle(BattleSprite.ACTION_PATTACK, new BattleSprite[]{this.player}, new BattleSprite[]{this.playerPet}, 
				 this.monsters, new BattleSprite[0]);
		 if(battleResult[2] == Skill.ATTACK_NO_CRI){//没有暴击  假设每次最高2.5倍的暴击
			 battleResult[1] = (int) (battleResult[1] * 2.5);
		 }
		 
		 //查找目标范围，，有3个战斗技能 有群攻效果， 缤纷，霜冻。群鹰出击     反弹技能有2个荆棘之墙， 魔力镜子
		 //还有中毒  致命之毒  混乱    排除
		 
		 //只排查一招致死的。
		
		 
	}
	
	
	protected void init(WorldPlayer player,Monster[] monsters, RandomMake rand){
		this.player = initPlayer(player);
		this.playerPet = initPet(player, this.player,0);
		this.monsters = new BattleSprite[monsters.length];
        for(int i = 0; i < monsters.length; i++){
        	if(monsters[i] != null){
        		this.monsters[i] = initMonster(monsters[i]);
        	}
        }
        
    }
	
	 protected BattleSprite initPlayer(WorldPlayer player) {
	        BattleSprite sprite = new BattleSprite();
	        int vit = player.getRealVitality();
	        int str = player.getRealStrength();
	        int inte = player.getRealIntelligence();
	        int agi = player.getRealAgility();
	        
	        int hp = player.getHp() + player.getBufProperty(Changed.HP);
	        int mp = player.getMp() + player.getBufProperty(Changed.MP);
	        
	        Buf bufEva = player.getCampBuf(Buf.CAMP_EVA);
	        int evaValue = 0;
	        if(bufEva != null){
	        	evaValue = bufEva.getValue();
	        }
	        Buf bufStone = player.getCampBuf(Buf.CAMP_STONE);
	        int stoneValue = 0;
	        if(bufStone != null){
	        	stoneValue = bufStone.getValue();
	        }
	        int [] addpoint = player.getSuitEffectDiamondAddValue();	//各属性宝石加成
	        int [] trainlevel = player.getTrainLevel();
	        int [] trainlevelstone = player.getTrainAttributeAddValue();
	        int []magicposlevel = player.getMagicPosLevel();
			int []magicposfloor = player.getMagicPosFloor();
	        sprite.initBattleData((byte) 0, player.getLevel(), vit, str, inte, agi,
	                              player.getLuck(), hp, mp, player.getVianyType(),
	                              evaValue, stoneValue, addpoint,trainlevel,trainlevelstone,magicposlevel,magicposfloor);
	        //sprite.initIntervene(player);
	        sprite.id = player.getId();

	        IEquipment[] equips = player.getUsedEquipments();
	        sprite.initEquipData(equips);

	        sprite.skillList = new short[0];
	        sprite.player = player;
	        sprite.name = player.getPlayerName();
	        sprite.face = (byte)player.getFace();
	        sprite.setStatus(BattleSprite.SEAL_SKILL_ATTACK,false);
	        if(player.getLevel()>10)
	            sprite.setStatus(BattleSprite.SEAL_SKILL_CATCH,false);
	        sprite.setStatus(BattleSprite.SEAL_SKILL_ITEM,false);
	        sprite.setStatus(BattleSprite.SEAL_SKILL_RUNAWAY,false);
	        sprite.setStatus(BattleSprite.SEAL_SKILL_SKILL,false);
	        sprite.initIntervene(player,sprite);
	        return sprite;
	    }

	    protected BattleSprite initPet(WorldPlayer player, BattleSprite owner,int pkflag) {
	        Pet pet = player.getPet();
	        if (pet != null && pet.getFavor() > 30) {
	            BattleSprite sprite = new BattleSprite();
	            
	            int vit = pet.getRealVitality();
	            int str = pet.getRealStrength();
	            int inte = pet.getRealIntelligence();
	            int agi = pet.getRealAgility();
	            
	            sprite.initBattleData(BattleSprite.TYPE_PLAYER_PET, pet.getLevel(),
	                    vit, 
	                    str,
	                    inte, 
	                    agi, 0,
	                    pet.getHp(), pet.getMp(), player.getVianyType(), 0, 0, null,null,null,null,null);
	            sprite.id = pet.getId();
	            sprite.pet = pet;
	            sprite.skillList = new short[0];
	            sprite.setStatus(BattleSprite.SEAL_SKILL_ATTACK, false);
	            sprite.setStatus(BattleSprite.SEAL_SKILL_SKILL, false);
	            sprite.setStatus(BattleSprite.SEAL_SKILL_DEF,false);
				try{
					
					IEquipment[] equips = new IEquipment[pet.getUsedEquipments().length];
					for(int jj = 0;jj<equips.length;jj++){
						if (pet.getUsedEquipments()[jj] != null){
							equips[jj] = (IEquipment)pet.getUsedEquipments()[jj].item;
						}
					}
			        sprite.initPetEquipData(equips,pkflag,pet.getEvolutionLevel());
				}catch (Exception e) {
					
				}
	            return sprite;
	        }
	        return null;
	    }

	    protected BattleSprite initMonster(Monster monster) {
	        BattleSprite sprite = new BattleSprite();
	        sprite.initBattleData((byte) 1, monster.getLevel(), monster.getVit(),
	                              monster.getStr(), monster.getInt(),
	                              monster.getAgi(), 0, 0, 0, 0, 0, 0, null,null,null,null,null);
	        sprite.initSpecial(monster.getPMinAttack(), monster.getPMaxAttack(),
	                           monster.getPDef(), monster.getMMinAttack(),
	                           monster.getMMaxAttack(), monster.getMDef(),
	                           monster.getParry(), monster
	                           .getHit(), monster.getPCritial(),
	                           monster.getMCritial(), monster.getHp(),
	                           monster.getMp());
	        sprite.skillList = new short[0];
	        sprite.hp = sprite.attributes[BattleSprite.ATTR_HPMAX];
	        sprite.mp = sprite.attributes[BattleSprite.ATTR_MPMAX];
	        monster.setMaxHp(sprite.hp);
	        monster.setMaxMp(sprite.mp);
	        sprite.ai = AiService.getAi(monster.getAiClass());
	        sprite.monster = monster;
	        sprite.skill = Skill.NOTREADY_SKILL;
	        return sprite;
	    }
}
