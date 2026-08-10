package peony.game.itemeffect;

import peony.game.Attack;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Skills;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.skill.Skill;

public class SkillItemEffect implements ItemEffect {
	
	protected int groupId;
	protected int level;
	protected boolean useBuffs;
	
	public SkillItemEffect(int groupId,int level,boolean useBuffs){
		this.groupId = groupId;
		this.level = level;
		this.useBuffs = useBuffs;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		// 特殊处理金钱爆和爆竹的使用
		if(target instanceof Player){
			Player tar = (Player)target;
			if((item.template.id==1687 || item.template.id==1686) && tar.level<35){
				throw new UseItemException(peony.Messages.STRING_00415);
			}
			if(item.template.id==2273 || item.template.id==2274 || item.template.id==2275
					|| item.template.id==DropItemEffect.ATT_ITEMID)
				throw new UseItemException(peony.Messages.STRING_00416);
//			if(item.template.id==4065 || item.template.id==4066)
//				throw new UseItemException(peony.Messages.STRING_00416);
		}
		if((item.template.id==1687 || item.template.id==1686) && source.canAttack(target)==13){
			throw new UseItemException(peony.Messages.STRING_00417);
		}
		if((item.template.id==2273 || item.template.id==2274 || item.template.id==2275) 
				&& target.id!=3539125)
			throw new UseItemException(peony.Messages.STRING_00418);
		if(item.template.id==DropItemEffect.ATT_ITEMID && target.id!=DropItemEffect.CREATURE_NIANSHOU_NEW)
			throw new UseItemException("只能对南越的新春年兽使用");
		if((item.template.id==4065 || item.template.id==4066) 
				&& target.id!=8323221)
			throw new UseItemException(peony.Messages.STRING_00418);
		Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(groupId, level));
		Attack attack = new Attack(skill,source,target,0,source.map.map.getInstanceId(),useBuffs,true);
		attack.trigger();
	}

	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
