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
			throw new UseItemException("错误的目标");
		// 特殊处理金钱爆和爆竹的使用
		if(target instanceof Player){
			Player tar = (Player)target;
			if((item.template.id==1687 || item.template.id==1686) && tar.level<35){
				throw new UseItemException("Không thể sử dụng đối với nhà chơi cấp 35 trở xuống");
			}
			if(item.template.id==2273 || item.template.id==2274 || item.template.id==2275)
				throw new UseItemException("Không thể sử dụng với người chơi");
		}
		if((item.template.id==1687 || item.template.id==1686) && source.canAttack(target)==13){
			throw new UseItemException("Trạng thái bảo hộ, không thể tấn công");
		}
		if((item.template.id==2273 || item.template.id==2274 || item.template.id==2275) 
				&& target.id!=3539125)
			throw new UseItemException("Chỉ có thể sự dụng với niên thú tân xuân Nam Hải");
		Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(groupId, level));
		Attack attack = new Attack(skill,source,target,0,useBuffs,true);
		attack.trigger();
	}

	public boolean isAsync(){
		return false;
	}
}
