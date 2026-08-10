package peony.game.skill;

import peony.game.CombatEffect;
import peony.game.Unit;
import peony.game.buff.Buff;

/**
 * 技能接口。所有技能，包括主动和被动技能，都必须实现此接口。
 * @author jeffrey
 */
public interface Skill {
	/**
	 * 技能类型标志位：主动攻击技。
	 */
	public static final int TYPE_ATTACK = 1;  //主动伤害
	/**
	 * 技能类型标志位：主动辅助技。
	 */
	public static final int TYPE_AID = 1<<1;  //主动辅助
	/**
	 * 技能类型标志位：被动技。
	 */
	public static final int TYPE_PASSIVE = 1<<2;  //被动
	/**
	 * 技能类型标志位：光环。
	 */
	public static final int TYPE_BUFF = 1<<3;  //光环
	/**
	 * 技能类型标识位：复活。
	 */
	public static final int TYPE_RELIVE = 1<<4;  // 复活
	/**
	 * 技能类型标志位：可装配。
	 */
	public static final int TYPE_VISIBLE = 1<<7; //可以放在技能栏
	/**
	 * 技能类型标志位：可马上使用。
	 */
	public static final int TYPE_RIDE_USE = 1<<8;
	
	/*
	 * 技能目标类型字用3位表示：第一位表示目标类型，1表示敌人，0表示友军；第二位表示范围：
	 * 1表示单体，0表示群体；第三位表示是否以自己为目标：1表示是，0表示否。
	 */
	public static final int TARGET_FLAG_ATTACK = 1;
	public static final int TARGET_FLAG_SINGLE = 1<<1;
	public static final int TARGET_FLAG_SELF = 1<<2;
	
	/**
	 * 技能目标类型：攻击单体。
	 */
	public static final int TARGET_SINGLE_ATTACK = TARGET_FLAG_SINGLE + TARGET_FLAG_ATTACK;
	/**
	 * 技能目标类型：攻击群体（以选中目标为中心）。
	 */
	public static final int TARGET_AOE_ATTACK_TARGET = TARGET_FLAG_ATTACK;
	/**
	 * 技能目标类型：攻击群体（以自己为中心）。
	 */
	public static final int TARGET_AOE_ATTACK_SELF = TARGET_FLAG_SELF + TARGET_FLAG_ATTACK;
	/**
	 * 技能目标类型：辅助单体。
	 */
	public static final int TARGET_SINGLE_AID = TARGET_FLAG_SINGLE;
	/**
	 * 技能目标类型：辅助自己。
	 */
	public static final int TARGET_AID_SELF = TARGET_FLAG_SINGLE + TARGET_FLAG_SELF;
	/**
	 * 技能目标类型：辅助群体（以选中目标为中心）。
	 */
	public static final int TARGET_AOE_AID_TARGET = 0;
	/**
	 * 技能目标类型：辅助群体（以自己为中心）。
	 */
	public static final int TARGET_AOE_AID_SELF = TARGET_FLAG_SELF;
	
	/**
	 * 技能ID。每个技能的ID必须唯一，并作为技能附带的BUFF的ID。技能ID的高16位是技能组ID，低
	 * 16位是技能等级。
	 */
	int getId();
	/**
	 * 技能组ID。
	 */
	int getGroupId();
	/**
	 * 技能名字。
	 */
	String getName();
	/**
	 * 技能等级。
	 */
	int getLevel();
	/**
	 * 技能描述。
	 */
	String getDesc(Unit owner);
	/**
	 * 攻击距离(像素)
	 */
	int getDistance(Unit owner);
	/**
	 * 施法时间(毫秒)
	 */
	int getActTime(Unit owner);
	/**
	 * 冷却组ID
	 */
	int getCDGroup();
	/**
	 * 冷却时间(毫秒)
	 */
	int getCDTime(Unit owner);
	/**
	 * AOE攻击范围半径(像素)
	 */
	int getRange(Unit owner);
	/**
	 * 图标ID
	 */
	int getIconId();
    /**
     * 准备动画
     */
    int getPrepareAnimation(Unit src);
	/**
	 * 释放动画
	 */
	int getCastAnimation(Unit src);
	/**
	 * 命中动画
	 */
	int getHitAnimation(Unit src);
	/**
	 * 技能类型标志，按位划分(0:主动伤害1:主动辅助2:被动技能3:光环技能7:可装配技能)
	 */
	int getType();
	/**
	 * 技能目标类型，按位划分(0:目标阵营1:目标范围2:AOE中心)
	 */
	int getTargetType();
	/**
	 * 是否玩家可学习技能。玩家技能在开始的时候会同步技能名字，在战斗中就不需要发送技能名字了。
	 */
	boolean isPlayerSkill();
	/**
	 * 是否自动学习第一级。
	 */
	boolean isAutoLearn();
	/**
	 * 技能所属的职业
	 */
	int getClazz();
	/**
	 * 学习技能耗费的技能点
	 */
	int getPoint();
	/**
	 * 使用技能需要消耗的蓝
	 */
	int getMP(Unit owner);
	/**
	 * 取得使用此技能需要的武器类型，null表示不限制。
	 */
	int[] getRequireWeapon();
	/**
	 * 学习技能需要的人物等级
	 */
	int getRequireLevel();
	/**
	 * 取得本技能的下一级，如果没有下一级返回null。
	 */
	Skill getNextLevel();
	/**
	 * 创建被动技能对应的BUFF。被动技能不直接参与战斗计算过程，而是通过BUFF来影响计算。
	 * @return 一个新的BUFF对象。如果此技能不想创建BUFF，返回null。
	 */
	Buff newBuff();
	/**
	 * 取得主动技能对应的一个战斗效果。
	 * @return 对于一个Skill对象来说，每次调用此方法应返回同一个Buff对象。如果此技能不想
	 *     影响通过这种方式影响战斗计算，返回null，
	 */
	CombatEffect getActEffect();
	/**
	 * 取得光环技能对应的BUFF。这个BUFF会加给所有在同场景的队友。
	 * @return 如果不是光环技能，返回null。
	 */
	Buff getAreaBuff();
	/**
	 * 转换为客户端可以识别的格式。
	 */
	byte[] toClientBytes(Unit owner);
}
