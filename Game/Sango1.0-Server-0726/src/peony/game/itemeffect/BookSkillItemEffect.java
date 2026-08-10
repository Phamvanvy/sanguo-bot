package peony.game.itemeffect;

import java.text.MessageFormat;
import java.util.Collection;

import org.apache.log4j.Logger;

import peony.game.Action;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Skills;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.skill.Skill;

public class BookSkillItemEffect implements ItemEffect {
	
	private static final Logger log = Logger.getLogger(BookSkillItemEffect.class);

	protected int skillGroupId;
	protected int skillLevel;

	public BookSkillItemEffect(int skillGroupId, int skillLevel) {
		this.skillGroupId = skillGroupId;
		this.skillLevel = skillLevel;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player p = (Player) target;
		boolean hasSkill = false;
		Collection<Skill> oldSkills = p.skills.getBookSkills();
		for(Skill oldSkill:oldSkills){
			if(oldSkill.getGroupId() == skillGroupId && oldSkill.getLevel()>=skillLevel)
				throw new UseItemException("已经存在此技能");
		}
		if (skillLevel > 1) {
			hasSkill = p.skills.hasSkill(Skills.getSkillId(skillGroupId,
					skillLevel - 1));
			if (!hasSkill) {
				throw new UseItemException("必须学习低等级的技能");
			}
		}
		Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(skillGroupId,
				skillLevel));
		if (skill.getRequireLevel() > p.level) {
			throw new UseItemException(MessageFormat.format("必须{0}级才能学习此技能书", skill.getRequireLevel()));
		}
		if (!hasSkill&&p.skills.getBookSkillSize() <= p.skills.getCurrentBookSkillSize()) {
			throw new UseItemException("技能书已满");
		}
		p.addBookSkill(skill);
		LogUtil.logGetBookSkill(p, skill);
		
		// 记录玩家动作
		p.addAction(Action.LEARN_SKILLBOOK);
	}
	
	public boolean isAsync(){
		return false;
	}
}
