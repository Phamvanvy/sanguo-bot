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

public class RefAttendantSkillItemEffect implements ItemEffect {

	protected int groupId0;
	protected int level0;
	protected int groupId1;
	protected int level1;
	protected int groupId2;
	protected int level2;
	protected boolean useBuffs;
	
	public RefAttendantSkillItemEffect(int groupId0,int level0,int groupId1,int level1
			,int groupId2,int level2,boolean useBuffs){
		this.groupId0 = groupId0;
		this.level0 = level0;
		this.groupId1 = groupId1;
		this.level1 = level1;
		this.groupId2 = groupId2;
		this.level2 = level2;
		this.useBuffs = useBuffs;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		// 特殊处理金钱爆和爆竹的使用
		if(target instanceof Player){
			Player tar = (Player)target;
			if((item.template.id==1687 || item.template.id==1686) && tar.level<35){
				throw new UseItemException("不能对35级以下玩家使用");
			}
			if(item.template.id==2273 || item.template.id==2274 || item.template.id==2275)
				throw new UseItemException("不能对玩家使用");
		}
		if((item.template.id==1687 || item.template.id==1686) && source.canAttack(target)==13){
			throw new UseItemException("受保护状态，不能攻击");
		}
		if((item.template.id==2273 || item.template.id==2274 || item.template.id==2275) 
				&& target.id!=3539125)
			throw new UseItemException("只能对南海的新春年兽使用");
		if(source instanceof Player){
			Player src = (Player)source;
			if(src!=null){
				if(src.attendant!=null){
					Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(groupId1, level1));
					Attack attack = new Attack(skill,source,target,0,useBuffs,true);
					attack.trigger();
					Skill attendantSkill = ObjectAccessor.getSkill(Skills.getSkillId(groupId2, level2));
					Attack attendantAttack = new Attack(attendantSkill,src.attendant,src.attendant,0,useBuffs,true);
					attendantAttack.trigger();
					return;
				}
			}
		}
		Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(groupId0, level0));
		Attack attack = new Attack(skill,source,target,0,useBuffs,true);
		attack.trigger();
	}

	public boolean isAsync(){
		return false;
	}

}
