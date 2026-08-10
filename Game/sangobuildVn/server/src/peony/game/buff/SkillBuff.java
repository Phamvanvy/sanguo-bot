package peony.game.buff;

import peony.game.skill.Skill;

/**
 * 被动技能对应的BUFF需要实现此接口。
 * @author lighthu
 */
public interface SkillBuff {
    Skill getSkill();
}
