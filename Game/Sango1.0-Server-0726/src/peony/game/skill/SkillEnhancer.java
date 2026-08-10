package peony.game.skill;

import peony.util.IntHashSet;

/**
 * 技能属性修正接口。
 * @author lighthu
 */
public interface SkillEnhancer {
	/**
	 * 取得影响的技能的组ID（注意不是技能ID）。
	 */
	IntHashSet getAffectSkillIDs();
	/**
	 * 修正一个技能的CD时间。
	 * @param skill 技能对象
	 * @param cd 原CD时间(毫秒)
	 * @return 修正后的CD时间(毫秒)
	 */
	float updateCDTime(Skill skill, float cd);
	/**
	 * 修正一个技能的攻击距离。
	 * @param skill 技能对象
	 * @param distance 原攻击距离(像素)
	 * @return 修正后的攻击距离
	 */
	float updateDistance(Skill skill, float distance);
	/**
	 * 修正一个技能的施法时间。
	 * @param skill 技能对象
	 * @param actTime 原施法时间
	 * @return 修正后的施法时间(毫秒)
	 */
	float updateActTime(Skill skill, float actTime);
	/**
	 * 修正一个技能的AOE半径。
	 * @param skill 技能对象
	 * @param range 原AOE半径(像素)
	 * @return 修正后的AOE半径
	 */
	float updateRange(Skill skill, float range);
	/**
	 * 修正一个技能的MP消耗。
	 * @param skill 技能对象
	 * @param mp 原技能消耗(像素)
	 * @return 修正后的技能消耗
	 */
	float updateMP(Skill skill, float mp);
}
