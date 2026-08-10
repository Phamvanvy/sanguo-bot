package peony.game.asyncbattle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import peony.game.Attack;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.attendant.Attendant;
import peony.game.skill.AbstractSkill;
import peony.game.skill.Skill;

public class PlayerBodyAi implements PlayerAi{

	protected Player player; //镜像
	protected Player target; //玩家
	public int lastMove;
	protected int[] path;
	public int attackCount;
	public static int addHpValue=2000;
	public static int addMpValue=2000;
	
	public static int hpCoolDownGroupId=101;
	public static int mpCoolDownGroupId=102;
	public static int hpCoolDownTime=30;
	public static int mpCoolDownTime=30;
	
	int firstUseSkillCount = -1;//进场使用 特殊技能次数
	public static int[] firstSkill=new int[]{0,2,1,0};//进场使用技能ID次数
	
	public float limitHp;//血量百分比触发相关技能
	
	public static float LIMITHP_CLAZZ1[]=new float[]{0.3f,0.3f,0.5f,0.5f};
	
	
	private static Map<Integer,List<Integer>> skillIndexs=new HashMap<Integer, List<Integer>>();
	
	private List<Integer> skills=new ArrayList<Integer>();
	
	static{
		/*技能优先级：霸王翔吼——霸王卸甲——嗜血飞斧——威凌天下——血战八方——坚盾反击——如封似闭——普通攻击
		对应技能id：6——1——2——9——5——132——7
		*/
		List<Integer> skills=new ArrayList<Integer>();
		skills.add(0,6);
		skills.add(1,1);
		skills.add(2,2);
		skills.add(3,9);
		skills.add(4,5);
		skills.add(5,132);
		skills.add(6,7);
		skills.add(7,126);
		skillIndexs.put(0, skills);
		/*技能优先级：三环套月——偷天换日——百步穿杨——九龙诛心——追星逐月——气断须弥——普通攻击
		对应技能id：25——26——21——23——24——29
		*/
		List<Integer> skills1=new ArrayList<Integer>();
		skills1.add(0,25);
		skills1.add(1,26);
		skills1.add(2,25);
		skills1.add(3,26);
		skills1.add(4,21);
		skills1.add(5,23);
		skills1.add(6,24);
		skills1.add(7,29);
		skills1.add(8,127);
		skillIndexs.put(1, skills1);
		/*技能优先级：气血俱动——静默术——天擎紫电——雷动九天——碎月连环剑——巧借东风——天罡烈焰——火烧连营——普通攻击
		对应技能id：47——140——42——49——43——46——41——48
		*/
		List<Integer> skills2=new ArrayList<Integer>();
		skills2.add(0,47);
		skills2.add(1,47);
		skills2.add(2,140);
		skills2.add(3,42);
		skills2.add(4,49);
		skills2.add(5,43);
		skills2.add(6,46);
		skills2.add(7,41);
		skills2.add(8,48);
		skills2.add(9,134);
		skillIndexs.put(2, skills2);
		/*
		 技能优先级：邪心共鸣——寒冰护体——冰封诅咒——寒冰利刃——牧野流星——嗜血无痕——普通攻击
		对应技能id：70——129——62——61——69——65
		血量低于50%时，优先释放镇心理气（id：63）和碧海潮生（id：66）
		优先级变为：邪心共鸣——镇心理气——碧海潮生——寒冰护体——冰封诅咒——寒冰利刃——牧野流星——嗜血无痕——普通攻击*/
		List<Integer> skills3=new ArrayList<Integer>();
		skills3.add(0,70);
		skills3.add(1,129);
		skills3.add(2,62);
		skills3.add(3,61);
		skills3.add(4,69);
		skills3.add(5,65);
		skills3.add(6,65);
		skills3.add(7,70);
		skills3.add(8,63);
		skills3.add(9,66);
		skills3.add(10,62);
		skillIndexs.put(3, skills3);
	}
	
	public Skill nextSkill; //下次要释放的技能
	public Skill skill; //当前释放的技能
	protected int attackRange = 0; //攻击距离
	protected boolean findPath = true; //是否寻路
	public int lastAttackTime; //上次释放技能时间
	public int lastAddMpTime; //上级加蓝时间
	
	
	
	public PlayerBodyAi(Player player, Player target){
		this.player = player;
		this.target = target;
		init();
	}
	
	public void backState() {
		
	}

	public boolean canOutOfBattle() {
		return false;
	}

	public void init() {
		this.skills=skillIndexs.get(player.clazz);
		if(player.clazz == Player.CLASS_2){//不是武将则开场有先释放的技能
			firstUseSkillCount=2;
		}
		if( player.clazz == Player.CLASS_3){
			firstUseSkillCount=1;
		}
		limitHp=LIMITHP_CLAZZ1[player.clazz];
		
		if(player.clazz==Unit.CLASS_1){//武将
			attackRange=2 * 8;//两码
		}else if(player.clazz==Unit.CLASS_2){//刺客
			Skill skill=player.skills.getSkillByGroupId(0);
			attackRange=skill.getDistance(player)/2;
		}else if(player.clazz==Unit.CLASS_3){//谋士
			Skill skill=player.skills.getSkillByGroupId(0);
			attackRange=skill.getDistance(player)/2;
		}else if(player.clazz==Unit.CLASS_4){//方士
			Skill skill=player.skills.getSkillByGroupId(0);
			attackRange=skill.getDistance(player)/2;
		}
		target.setHp(target.maxhp, true);
		target.setMp(target.maxmp, true);
		
		player.setHp(player.maxhp, true);
		player.setMp(player.maxmp, true);
		
		player.coolDowns.clearCoolDowns(player);
		target.coolDowns.clearCoolDowns(target);
	}

	public void update() {
		if(Time.currTime - 50 > lastMove){
			if(!target.asyncLoadFinish) //等待挑战者load地图成功
				return;
			
			if(firstUseSkillCount > 0){
				//进场释放特殊技能, 一次性释放完毕
				int index = 0;
				while(firstUseSkillCount > 0){
					int skillIndex=skills.get(index);
					Skill s = player.skills.getSkillByGroupId(skillIndex);
					if(s!=null&&canPostSkill(player, (AbstractSkill)s)&&!player.coolDowns.atCoolDown(skillIndex)){
						player.prepareSkillAttack(target.id, s.getId(), 0, 0);
						player.coolDowns.setCommonCD(0);
						if(player.attack!=null)
							player.attack.attackType = Attack.ATTACK_TYPE_ASYNC_SOURCE;
					}
					index++;
					firstUseSkillCount--;
				}
			}
			
			if(findPath && !player.inRange(target, attackRange)){ //寻路
				path = player.map.map.mapDef.mapInfo.getPathFinder().findPath(player.x, player.y, target.x, target.y);
				player.move(player.x, player.y, (byte)1, (short)1, Time.currTime, 0, path[0], path[1]);
				processAttendantFollow(player, player.direct);
			}else{
				if(Time.currTime-lastAttackTime<600) //两次释放技能时间间隔,模拟玩家手动释放技能的速度
					return;
				if(skill==null){ //查找下一个可以释放的技能
					int begin = 0;
					if(player.clazz==Player.CLASS_2)
						begin = 2;
					else if(player.clazz==Player.CLASS_3)
						begin = 1;
					int end = skills.size();
					if(player.clazz==Player.CLASS_4){
						if(player.hp<player.maxhp*0.6f){
							begin = 7;
							end = skills.size();
						}else{
							end = 7;
						}
					}
					for(int i=begin;i<end;i++){
						int skillIndex=skills.get(i);
						Skill temp = player.skills.getSkillByGroupId(skillIndex);
						if(temp!=null && canPostSkill(player, (AbstractSkill)temp) 
								&& !player.coolDowns.atCoolDown(temp.getCDGroup()) && 
								player.mp >= temp.getMP(player)){
							nextSkill=temp;
							break;
						}
					}
					
					if(nextSkill!=null){
						attackRange = nextSkill.getDistance(player);
						if((nextSkill.getTargetType() & Skill.TARGET_FLAG_SELF) == Skill.TARGET_FLAG_SELF){
							//以自己为目标的技能
							if(nextSkill.getDistance(player)==0 && nextSkill.getRange(player)==0){
								//给自己加BUFF的技能
								findPath = false;
							}else{
								attackRange = nextSkill.getRange(player);
								findPath = true;
							}
						}else{
							findPath = true;
						}
					}
				}
				if(nextSkill!=null && skill==null){
					if(findPath && player.inRange(target, attackRange) || !findPath){ 
						//如果选择的技能不需要追击距离，直接释放
						//如果技能需要追击距离并且已在范围内，直接释放技能
						skill = nextSkill;
						int code = 0;
						if(skill.getTargetType()==Skill.TARGET_SINGLE_AID)
							code= player.prepareSkillAttack(player.id, skill.getId(), 0, Attack.ATTACK_TYPE_ASYNC_SOURCE);
						else if(skill.getTargetType()==Skill.TARGET_AID_SELF)
							code= player.prepareSkillAttack(player.id, skill.getId(), 0, Attack.ATTACK_TYPE_ASYNC_SOURCE);
						else if(skill.getTargetType()==Skill.TARGET_AOE_AID_TARGET)
							code= player.prepareSkillAttack(player.id, skill.getId(), 0, Attack.ATTACK_TYPE_ASYNC_SOURCE);
						else if(skill.getTargetType()==Skill.TARGET_AOE_AID_SELF)
							code= player.prepareSkillAttack(player.id, skill.getId(), 0, Attack.ATTACK_TYPE_ASYNC_SOURCE);
						else
							code= player.prepareSkillAttack(target.id, skill.getId(), 0, Attack.ATTACK_TYPE_ASYNC_SOURCE);
						if(player.attack!=null)
							player.attack.attackType = Attack.ATTACK_TYPE_ASYNC_SOURCE;
						if(skill.getGroupId()==0)
							attackCount++;
						if(player.clazz==Player.CLASS_2 && attackCount>5){
							if(!player.coolDowns.atCoolDown(hpCoolDownGroupId)){
								player.setHp(player.hp+addHpValue, false);
									player.setCoolDown(hpCoolDownGroupId,
											Time.currTime, Time.currTime
													+ hpCoolDownTime);
							}
							if(!player.coolDowns.atCoolDown(mpCoolDownGroupId)){
								player.setMp(player.mp+addMpValue, false);
									player.setCoolDown(mpCoolDownGroupId,
											Time.currTime, Time.currTime
											+ mpCoolDownTime);
							}
							attackCount = 0;
						}
					}
				}else{ //如果没有可释放的技能，则选择普通攻击
					if(skill==null && !player.coolDowns.atCoolDown(0)){
						nextSkill = player.skills.getSkillByGroupId(0);
						findPath = true;
						attackRange = nextSkill.getDistance(player);
					}
				}
			}
			if(player.mp<300 && (Time.currTime-lastAddMpTime)>30000){
				int total = player.mp + 2300;
				player.setMp(Math.min(player.maxmp, total), true);
				lastAddMpTime = Time.currTime;
			}
			lastMove = Time.currTime;
		}
	}
	
	protected void processAttendantFollow(Player player, int direct){
		if(player!=null && player.attendant!=null){
			player.attendant.speed = player.getSpeed() * Attendant.SPEEDRATIO;
			int[] po = VMap.getAttendantPositon(direct, player);
			player.attendant.move(po[0], po[1]);
			player.attendant.moveType |= GameObject.MOVE_ALL;
		}
	}

	public void processHpMp(int itemId) {
		GameItem item=ObjectAccessor.createGameItem(itemId);
		if(item!=null){
			if(item.template.useType.coolDownId==hpCoolDownGroupId){
				attackCount=0;
				if(!player.coolDowns.atCoolDown(hpCoolDownGroupId)){
					player.setHp(player.hp+addHpValue, false);
						player.setCoolDown(hpCoolDownGroupId,
								Time.currTime, Time.currTime
										+ hpCoolDownTime);
				}
			}else if(item.template.useType.coolDownId==mpCoolDownGroupId){
				attackCount=0;
				if(!player.coolDowns.atCoolDown(mpCoolDownGroupId)){
					player.setMp(player.mp+addMpValue, false);
						player.setCoolDown(mpCoolDownGroupId,
								Time.currTime, Time.currTime
								+ mpCoolDownTime);
				}
			}
		}
	}
	
	protected boolean canPostSkill(Player player, AbstractSkill skill){
		int[] minorTypes = skill.getRequireWeapon();
		List<Integer> currTypes = new ArrayList<Integer>();
		boolean ok = true;
		if(minorTypes!=null){
			for(GameItem item : player.equipments.equs){
				if(item!=null && item.template.isEquipment())
					currTypes.add(item.template.equipment.minorType);
			}
			for(int type : minorTypes){
				if(!currTypes.contains(type)){
					ok = false;
					break;
				}
			}
		}
		if(player.isDumb() || player.isFear() || player.isParalyze() || player.isStay())
			ok = false;
		return ok;
	}

}
