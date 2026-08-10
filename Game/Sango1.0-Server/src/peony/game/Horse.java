package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;

import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.changed.ChangedItem;
import peony.game.changed.HorseFoodChange;
import peony.game.changed.HorseIntPropertyChangedItem;
import peony.game.changed.HorseSkillChangedItem;
import peony.game.changed.HorseStringPropertyChangedItem;
import peony.game.skill.Skill;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.account.Account;
import peony.service.account.AccountService;
import peony.service.cards.CardInfo;
import peony.service.cards.CardService;
import peony.service.shop.ShopService;
import peony.service.stat.StatService;

import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.HorseType;

public class Horse implements PropertyEnhancer,GameItemObject{
	
	private static final Random rnd = new Random();
	
	private static final Logger log = Logger.getLogger(Horse.class);
	
	public HorseType template;
	
	public static final int MAXDEGREE = 100;
	
	public int skillSize;
	public List<Skill> skills;

	public int strength;
	public int agility;
	public int intellect;
	public int stamina;
	public int speed;
	
	public int point;
	
	public int strengthAdded;
	public int agilityAdded;
	public int intellectAdded;
	public int staminaAdded;
	public int speedAdded;
	
	public int exp;
	
	public int degree;
	
	public int level;
	
	public int instanceId;
	
	public short score;
	
	public String name;
	
	public int imageId;	//坐骑的图片ID
	public int imageIdChange = -1;	//幻化后的图片ID
	
	public int iconImage;	//坐骑的icon图标的图片
	public int iconImageChange = -1;
	
	public int iconId;
	public int iconIdChange = -1;
	
	
	public int agentHorse = 0; // 0非代理饲养 1代理饲养
	public long agentTime;
	public int leavingExp;
	public int lockSkillId;
	public int initLevel;
	
	public int itemId; //生成马的物品Id
	public int itemIdChange; //生成马的物品Id(幻化后对象的坐骑物品)
	
	public int foodId;
	
	public HorseEquipments equs;
	
	//最后一次减去饱食度的时间
	public int lastDecDegreeTime; 
	
	public int notOnlineExpTime;
	
	public byte state; //坐骑状态    0：已过期未激活    1：已激活   2：未过期 未激活
	
	protected static int[] freeHorseItemIds = {2475, 2476, 2477, 2478, 2580, 2581, 2582, 2583, 
		2585, 2586, 2587, 2588, 2605, 2591, 2592, 2593, 2595, 2606, 2597, 2598, 2600, 2601, 2602, 2603,4000,4001,4002,4003,4005,4006,4007,4008,4010,4011,4012,4547,4549,4550,4551,4552}; //具有免费使用时间的物品ID
	
	protected static int[] freeTimes = {7*24*3600*1000,7*24*3600*1000,7*24*3600*1000,7*24*3600*1000,24*3600*1000,24*3600*1000,24*3600*1000,24*3600*1000,
		2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,
		2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,
		2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000,2*3600*1000};
	
	public static int[] freeHorse = {4333,4334,4336,4335};//按职业排序
	
	public static int[] autoRideHorse = {822,823,824,825};//获得自动上骑坐骑
	
	public long freeHorseEndTime; //免费使用结束时间
	
	public static int STATE_ACTIVE = 1; //激活状态
	
	public int fixCount;	//坐骑合成次数
	public static int maxFixCount = 15; //坐骑最大合成次数
	public static int minFixLevel = 55; //坐骑合成最低级别
	public static int minChangeImgLevel = 55; //坐骑幻化最低级别
	public static int[] noFixAndChangeImgHorses = {818, 820}; //不能进行合成和幻化的坐骑
	public static int fixCreditParam = 880; //合成扣战功
	public static int horseChangeCredit = 10000; //坐骑幻化扣除战功
	public static int removeHorseChangeCredit = 10000; //还原坐骑扣除战功
	
	public static int[] fixLevel = {3,4,5,6,7,8,9,10,11,12,13,14,15};   //坐骑合成等级
	public static int[] buffLevel = {1,1,1,3,3,3,6,6,6,10,10,10,15};  //合成等级对应的宝石效果buff等级
	public static int jewelBuffId = 579;  //宝石效果buffid
	
	public Skill tempNewSkill;		//临时领悟的新技能
	public int removeSkillIndex;	//领悟时要失去的技能
	
	public Horse(HorseType type, int instanceId) {
		this.template = type;
		this.instanceId = instanceId;
		skills = new ArrayList<Skill>();
		equs = new HorseEquipments(this);
		skillSize = 6;
		foodId = -1;
	}
	
	@Override
	public Horse clone(){
		Horse h = new Horse(template,instanceId);
		h.skillSize = skillSize;
		h.skills = new ArrayList<Skill>(skills);
		h.strength = strength;
		h.agility = agility;
		h.intellect = intellect;
		h.stamina = stamina;
		h.speed = speed;
		h.point = point;
		h.strengthAdded = strengthAdded;
		h.agilityAdded = agilityAdded;
		h.intellectAdded = intellectAdded;
		h.staminaAdded = staminaAdded;
		h.speedAdded = speedAdded;
		h.exp = exp;
		h.degree = degree;
		h.level = level;
		h.score = score;
		h.name = name;
		h.imageId = imageId;
		h.imageIdChange = imageIdChange;
		h.initLevel = initLevel;
		h.foodId = foodId;
		h.iconImage = iconImage;
		h.iconId = iconId;
		h.itemId = itemId;
		h.itemIdChange = itemIdChange;
		h.agentHorse = agentHorse;
		h.agentTime = agentTime;
		h.leavingExp = leavingExp;
		h.lockSkillId = lockSkillId;
		h.equs = equs.clone();
		h.fixCount = fixCount;
		return h;
	}
	
	public void enhance(PropertyCalculator pc) {
		pc.stamina += stamina;
		pc.strength += strength;
		pc.agility += agility;
		pc.intellect += intellect;
		double speedRateAdd;
		if (speed < 300) {
			speedRateAdd = speed / 400.0f;
		} else {
			speedRateAdd = 0.75f + Math.pow(speed - 300, 0.8) / 400;
		}
		pc.setHorseSpeed(0.15f + (float)speedRateAdd);
		this.equs.enhance(pc, false);
	}
	
	public void addSkill(Skill skill, Player p){
		skills.add(skill);
		if(p!=null&&p.horse==this){
			p.buffs.addBuff(skill.newBuff());
		}
		HorseSkillChangedItem c = new HorseSkillChangedItem(this,skill,true,true);
		if(p!=null)
			p.changed.addChangedItem(c);

	}
	
	public Skill getSkill(int skillId){
		for(Skill skill:skills){
			if(skill.getId()==skillId)
				return skill;
		}
		return null;
	}
	
	public Skill removeSkill(int skillId,Player p){
		Iterator<Skill> ite = skills.iterator();
		while(ite.hasNext()){
			Skill skill = ite.next();
			if(skill.getId()==skillId){
				ite.remove();
				p.buffs.removeBuff(skill.getId());
				HorseSkillChangedItem c = new HorseSkillChangedItem(this,skill,false,true);
				p.changed.addChangedItem(c);
				return skill;
			}
		}
		return null;
	}
	
	/**
	 * 随机遗忘一个技能
	 * @param p
	 * @return
	 */
	public Skill removeSkill(Player p){
		int i = rnd.nextInt(skills.size());
		while(skills.get(i).getId()==lockSkillId){
			i = rnd.nextInt(skills.size());
		}
		Skill s = skills.remove(i);
		p.buffs.removeBuff(s.getId());
		HorseSkillChangedItem c = new HorseSkillChangedItem(this,s,false,true);
		p.changed.addChangedItem(c);
		return s;
	}
	
	/**
	 * 领悟新技能时，获得要失去技能的ID，-1是空
	 */
	public int getRemoveSkillId(Player p, Skill newSkill){
		int i = rnd.nextInt(skills.size());
		if(HorseUtil.getLockSkillCount(this)>0 && skills.size()>HorseUtil.getLockSkillCount(this)){
			while(((lockSkillId>>i)&1)==1){
				i = rnd.nextInt(skills.size());
			}
		}else if(HorseUtil.getLockSkillCount(this)>0 && skills.size()==HorseUtil.getLockSkillCount(this)){
			return -1;
		}
		return i;
	}
	
	public Skill removeSkill(Player p, Skill newSkill, int skillIndex){
//		int i = rnd.nextInt(skills.size());
//		if(HorseUtil.getLockSkillCount(this)>0 && skills.size()>HorseUtil.getLockSkillCount(this)){
//			while(((lockSkillId>>i)&1)==1){
//				i = rnd.nextInt(skills.size());
//			}
//		}else if(HorseUtil.getLockSkillCount(this)>0 && skills.size()==HorseUtil.getLockSkillCount(this)){
//			return null;
//		}
		
		if(skillIndex >= skills.size() || skillIndex < 0){
			return null;
		}
			
		Skill s = skills.get(skillIndex);
		if (s!=null && newSkill != null) {
			p.buffs.removeBuff(s.getId());
			skills.set(skillIndex, newSkill);
			if(p!=null){
				if(!(newSkill.getClazz()==5&&newSkill.getLevel()<=0)){
				    p.message(-1, MessageFormat.format(peony.Messages.STRING_00882, newSkill.getName()), -1, -1);
				}
			}
			if(p!=null&&p.horse==this){
				p.buffs.addBuff(newSkill.newBuff());
			}
		}
		if(skills!=null && skills.size()>0 && p!=null){
			Packet pt = new Packet(OpCode.HORSE_SKILLS_SERVER);
			pt.putInt(skills.size());
			for(Skill skill : skills){
				pt.put(skill.toClientBytes(p));
			}
			p.send(pt);
		}
		return s;
	}
	
	public void update(Player p){
		if(lastDecDegreeTime!=0){
			if(p.systemState!=Player.SYSTEMSTATE_READY)
				return;
			int newDegree = this.degree;
			if((Time.currTime-lastDecDegreeTime)>=60*1000L){
				newDegree = Math.max(degree-1, 0);
//				setDegree(d, p);
				lastDecDegreeTime = Time.currTime;
			}
			if(newDegree<=20&&foodId!=-1&&p.battleType!=Player.TYPE_ASYNC_PLAYER){
				PlayerTransaction tx = p.newTransaction("AFD");
				GameItem item = p.bag.removeGameItem(foodId, -1, 1, tx, false);
				if(item!=null){
					try{//装备的口粮自动变更
						int itemCount = p.bag.getGameItemCount(foodId);
						if(itemCount <=1){
							int[] foodIds = new int[]{ItemUtil.ITEM_HORSEFOOD_ADDBUFF,ItemUtil.ITEM_HORSEFOOD,ItemUtil.ITEM_HORSEFOODS};
							for(int i=0;i<foodIds.length;i++){
								if(foodId!=foodIds[i]){
									int count = p.bag.getGameItemCount(foodIds[i]);
									if(count>0){
										HorseFoodChange changedItem = new HorseFoodChange(this,foodIds[i]);
										p.changed.addChangedItem(changedItem);
										this.foodId = foodIds[i];
										break;
									}
								}
							}
						}
					}catch(Exception e){
						
					}
					tx.commit();
					int v = 50;
					if(item.template.id==ItemUtil.ITEM_HORSEFOOD_ADDBUFF){
						v = 70;
					}
					newDegree += v;
//					setDegree(Math.min(degree, MAXDEGREE),p);
					if(item.template.id==ItemUtil.ITEM_HORSEFOOD_ADDBUFF){
						p.buffs.addBuff(BuffUtil.createBuff(164, 1, p, p, 0));
					} 
				}else{
					tx.rollback();
				}
			}
			setDegree(Math.min(newDegree, MAXDEGREE),p);
			if(this.degree==0){
				p.horseUnride(-1);
			}
			if(freeHorseEndTime>0 && Time.currTime>Time.elapseTime(freeHorseEndTime)){
				p.horseUnride(-1);
				unActive();
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_STATE, state, false);
				if(StatService.isInArray(freeHorse, this.itemId)!=-1){
					AccountService as = Server.server.getServiceRegistry()
					.getAccountService();
					Account account = as.getAccount(p.accountId);
					long iMoney = account.getLongIMoney() / 100;
					if(iMoney<100*36){
					    p.message(-1, "主人，我是不需要特意激活的，只要你拥有100元宝就可以激发我体内的小宇宙啦，去商城中看看怎么充值，然后再来激活我吧！", -1, -1);
					}
					return;
				}
				Packet pt = new Packet(OpCode.OPENUI_SERVER);
				pt.putString("ui_npc_dialog");
				ShopService shopService = Server.server.getServiceRegistry().getShopService();
				int activeId = HorseActiveCall.getActiveItemByHorseId(itemId);
				String price = "0";
				if(activeId!=-1){
					DecimalFormat df = new DecimalFormat("0.00");
					double tempPrice = (double)(shopService.getItemPrice(activeId)/36f);
					price = df.format(tempPrice);
				} 
				pt.putString("ACTIVE_HORSE|"+instanceId+"|"+price);
				p.send(pt);
			}
		}
	}
	
	public String getLeavingTime(){
		if(indexOfFreeHorses(itemId)!=-1 && !isActive()){
			return MessageFormat.format(peony.Messages.STRING_00883, 0);
		}
		if(freeHorseEndTime<=0)
			return "";
		if(System.currentTimeMillis() > freeHorseEndTime){
			return MessageFormat.format(peony.Messages.STRING_00883, 0);
		}
		int leaving = Time.elapseTime(freeHorseEndTime) - Time.currTime;
		int day = leaving / (1000 * 3600 * 24);
		if(day>0)
			return MessageFormat.format(peony.Messages.STRING_00884, day);
		int hour = leaving / (1000 * 3600);
		if(hour>0)
			return MessageFormat.format(peony.Messages.STRING_00885, hour);
		return "";
	}
	
	public void setExp(int exp,Player p,String cause){
		if (this.exp != exp) {
			// 记录获得经验日志
			LogUtil.logGetHorseExp(p, this, this.exp, exp, cause);

			int inc = exp - this.exp;
			int upLevel = HorseUtil.getUpLevel(level, exp);
			addIntPropertyChangedItem(p.changed,ChangedItem.HORSE_GAINEXP, exp - this.exp,
					true, false);
			if (upLevel > 0) {
				int lvl1 = level;
				int exp1 = exp;
				int newLevel = level + upLevel;
				exp -= HorseUtil.getUpLevelExp(level, newLevel);
				this.exp = exp;
				addIntPropertyChangedItem(p.changed,ChangedItem.HORSE_EXP, this.exp, false,
						true);
				setLevel(newLevel, p);
//				setPoint(this.point + upLevel,p);
				int upExp = HorseUtil
						.getUpLevelExp(this.level, this.level + 1);
				addIntPropertyChangedItem(p.changed,ChangedItem.HORSE_UPLEVELEXP, upExp, false,
						true);
				refreshProperties(true,p);
				p.refreshProperties(false);
				notifyHorseUpLevel(lvl1,p);
				
				// 记录升级日志
				LogUtil.logHorseLevelUp(p, this, lvl1, exp1, this.level, this.exp);
			} else {
				this.exp = exp;
				addIntPropertyChangedItem(p.changed,ChangedItem.HORSE_EXP, this.exp, false,
						true);
			}
		}
	}
	
	public void notifyHorseUpLevel(int oldLevel,Player player){
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_HORSE_LEVELUP, player,oldLevel));
	}
	
	public void setStrength(int strength,Player p){
		if(this.strength!=strength){
			this.strength = strength;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_STRENGTH, this.strength, false);
			}
		}
	}
	
	public void setDegree(int degree,Player p){
		if(this.degree!=degree){
			this.degree = degree;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_DEGREE, this.degree, false);
			}
		}
	}
	
	public void setAgility(int agility,Player p){
		if(this.agility!=agility){
			this.agility = agility;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_AGILITY, this.agility, false);
			}
		}
	}
	
	public void setStamina(int stamina,Player p){
		if(this.stamina!=stamina){
			this.stamina = stamina;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_STAMINA, this.stamina, false);
			}
		}
	}
	public void setIntellect(int intellect,Player p){
		if(this.intellect!=intellect){
			this.intellect = intellect;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_INTELLECT, this.intellect, true);
			}
		}
	}
	
	public void setSpeed(int speed,Player p){
		if(this.speed!=speed){
			this.speed = speed;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_SPEED, this.speed, false);
			}
		}
	}
	
//	public void setPoint(int point,Player p){
//		if(this.point!=point){
//			this.point = point;
//			if(p!=null){
//				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_POINT, this.speed, false);
//			}
//		}
//	}
	
	public void setLevel(int level,Player p){
		if(this.level!=level){
			this.level = level;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_LEVEL, this.level, false, true);
			}
			int skillLen = this.level / 15 + 1;
			skillLen = Math.min(skillLen, this.skillSize);
			if(skills.size()<skillLen){
				int currentSkillSize = skills.size();
				for(int i=0;i<skillLen-currentSkillSize;i++){
					int[] s = new int[skills.size()];
					for(int j=0;j<s.length;j++){
						s[j] = skills.get(j).getGroupId();
					}
					Skill skill = HorseUtil.getSkill(0, s);
					if(skill!=null){
						addSkill(skill,p);
					}
				}
			}
		}
	}
	
	public void setActive(){
		state = 1; 
	}
	
	public void unActive(){
		state = 0;
	}
	/**是否可骑乘**/
	public boolean isActive(){
		if(state == 1 || state == 2){
			return true;
		}else{
			return false;
		}
	}
	
	public void refreshProperties(boolean levelUp,Player p){
		PropertyCalculator calc = new PropertyCalculator(this);
		if(p!=null && p.buffs!=null)
			p.processHorsePreBuff(calc);
		equs.enhance(calc, true);
		enhanceCards(calc, p);
		setStrength(calc.strength,p);
		setAgility(calc.agility,p);
		setStamina(calc.stamina,p);
		setIntellect(calc.intellect,p);
		setSpeed(calc.speed,p);
	}
	
	public void refreshPropertiesExcepPlayer(boolean levelUp,Player p){
		PropertyCalculator calc = new PropertyCalculator(this);
		equs.enhance(calc, true);
		setStrength(calc.strength,p);
		setAgility(calc.agility,p);
		setStamina(calc.stamina,p);
		setIntellect(calc.intellect,p);
		setSpeed(calc.speed,p);
	}
	
	public void enhanceCards(PropertyCalculator pc, Player player){
		try {
			CardService service = Server.server.getServiceRegistry().getCardService();
			for(CardInfo info : player.cards.horseEquipCards){
				if(info!=null){
					int cardId = info.cardId;
					int cardLevel = info.level;
					Card card = service.getCardByCardId(cardId);
					if(card!=null){
						int cardPropertyType = card.prorertyType;
						int baseValue = card.propertyBaseValue;
						int upLevelValue = card.propertyUpLevelValue;
						int quality = ObjectAccessor.createGameItem(card.itemId).template.quality;
						service.enhanceCardValue(pc, cardLevel, cardPropertyType, baseValue, upLevelValue,quality);
					}
				}
			}
		} catch (Exception e) {
			
		}
	}
	
	public void food(int itemId, Player p, int serial) {
		if (itemId == -1) {
			foodId = -1;
		} else {
			ItemTemplate t = ObjectAccessor.getItemTemplate(itemId);
			if (t != null) {
				foodId = itemId;
			}
		}
		if (p != null) {
			Packet pt = new Packet(OpCode.HORSE_FOOD_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}
	
	public void changeName(String name, Player p, int serial) {
		this.name = name;
		if (p != null) {
			String itemName = "";
			try {
				itemName = ObjectAccessor.getItemTemplate(itemId).name;
			} catch (Exception e) {
			}
			String horseName = name;
			if(fixCount > 0){
				horseName = MessageFormat.format("{0} +{1}", horseName, this.fixCount);
			}
			if(imageIdChange >= 0){
				horseName = MessageFormat.format("{0}(已幻化)", horseName);
			}
			String comName = itemName + "\t" + horseName;
			addStringPropertyChangedItem(p.changed, ChangedItem.HORSE_NAME, comName+getLeavingTime(), false);
			Packet pt = new Packet(OpCode.HORSE_CHANGENAME_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}
	
	/**
	 * 坐骑合成幻化后的名称改变
	 */
	public void horseChangeName(Player p) {
		String itemName = "";
		try {
			itemName = ObjectAccessor.getItemTemplate(itemId).name;
		} catch (Exception e) {
		}
		String horseName = this.name;
		if(this.fixCount > 0){
			horseName = MessageFormat.format("{0} +{1}", horseName, this.fixCount);
		}
		
		if(this.imageIdChange >= 0){
			horseName = MessageFormat.format("{0}(已幻化)", horseName);
		}
		String comName = itemName + "\t" + horseName;
		addStringPropertyChangedItem(p.changed, ChangedItem.HORSE_NAME, comName+getLeavingTime(), false);
	}
	
	public void feed(GameItem item,Player p,int serial){
		int v = 50;
		if(item.template.id==ItemUtil.ITEM_HORSEFOOD_ADDBUFF){
			v = 70;
		}
		int degree = this.degree + v;
		setDegree(Math.min(degree, MAXDEGREE),p);
		if(item.template.id==ItemUtil.ITEM_HORSEFOOD_ADDBUFF){
			p.buffs.addBuff(BuffUtil.createBuff(164, 1, p, p, 0));
		}
		if(p!=null){
			Packet pt = new Packet(OpCode.HORSE_FEED_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}
	
	/** 是否允许进行合成和幻化 */
	public static boolean canFixOrChangeImg(int horseItemId){
		for(int itemId : noFixAndChangeImgHorses){
			if(itemId==horseItemId)
				return false;
		}
		return true;
	}
	
	public void ride(Unit u){
		for(Skill s:skills){
			u.buffs.addBuff(s.newBuff());
		}
//		if(foodId==1242){
//			if(u.type==GameObject.TYPE_PLAYER){
//				if(((Player)u).bag.getGameItem(ItemUtil.ITEM_HORSEFOOD_ADDBUFF)!=null){
//					u.buffs.addBuff(BuffUtil.createBuff(164, 1, u, u, 0));
//				}
//			}
//		}
		lastDecDegreeTime = Time.currTime;
		processRideBuff(u);
		u.refreshProperties(false);
		if(u instanceof Player){
			((Player)u).refreshHorseStarState();
		}
	}
	
	public void unRide(Unit u){
		for(Skill s:skills){
			u.buffs.removeBuff(s.getId());
		}
		if(u instanceof Player)
			((Player)u).removeSuiteEquipmentBuffs(equs.equs);
//		u.buffs.removeBuff(164);
		processUnRideBuff(u);
		lastDecDegreeTime = 0;
		u.refreshProperties(false);
		if(u instanceof Player){
			refreshPropertiesExcepPlayer(false, (Player)u);
			((Player)u).refreshHorseStarState();
		}
	}
	
	/**
	 * 上骑时处理buff
	 * @param u
	 */
	public void processRideBuff(Unit u){
		Buff buff = null;
		int index = StatService.isInArray(fixLevel, this.fixCount);
		if(index!=-1 && index<buffLevel.length){
			int bLevel = buffLevel[index];
			buff= BuffUtil.createSuiteBuff(jewelBuffId, bLevel);
			if(buff!=null)
			    u.buffs.addBuff(buff);
		}
	}
	
	/**
	 * 下骑时移除buff
	 * @param u
	 */
	public void processUnRideBuff(Unit u){
		if(u.buffs!=null){
			u.buffs.removeBuff(jewelBuffId);
		}
	}
	
	public int getRepairMoney(){
		int ret = 0;
		for(int i=0;i<equs.equs.length;i++){
			GameItem item = equs.equs[i];
			if(item!=null){
				ret += item.getRepairMoney();
			}
		}
		return ret;
	}

	public void repair(Player owner){
		boolean needRefresh = false;
		for (int i = 0; i < equs.equs.length; i++) {
			GameItem item = equs.equs[i];
			if (item != null) {
			    if (item.duration == 0) {
			        needRefresh = true;
			    }
			    item.repair(owner);
			}
		}
		if (needRefresh&&owner.horse==this) {
			owner.refreshProperties(false);
		}
	}
	
	public static int getFreeTime(int horseId){
		int index = indexOfFreeHorse(horseId);
		if(index!=-1)
			return freeTimes[index];
		return 0;
	}
	
	private static int indexOfFreeHorse(int horseId){
		for(int i=0;i<freeHorseItemIds.length;i++){
			if(freeHorseItemIds[i]==horseId)
				return i;
		}
		return -1;
	}
	
	public static int indexOfAutoRide(int horseId){
		for(int i=0;i<autoRideHorse.length;i++){
			if(autoRideHorse[i]==horseId)
				return i;
		}
		return -1;
	}
	
	public int indexOfFreeHorses(int horseId){
		for(int i=0;i<freeHorse.length;i++){
			if(freeHorse[i]==horseId)
				return i;
		}
		return -1;
	}
	
	public void addStringPropertyChangedItem(Changed changed,int id,String value,boolean notify){
		if(changed!=null){
			HorseStringPropertyChangedItem sc = new HorseStringPropertyChangedItem(this,id,value,notify);
			changed.addChangedItem(sc);
		}
	}

	public void addIntPropertyChangedItem(Changed changed,int id, int oldValue, int newValue,
			boolean notify) {
		if (changed != null) {
			HorseIntPropertyChangedItem ic = new HorseIntPropertyChangedItem(this,id,
					newValue, false, true);
			changed.addChangedItem(ic);
			if (notify) {
				HorseIntPropertyChangedItem oic = new HorseIntPropertyChangedItem(this,id,
						newValue - oldValue, true);
				changed.addChangedItem(oic);
			}
		}
	}
	
	public void addIntPropertyChangedItem(Changed changed,int id,int value,boolean notify,boolean overwrite){
		if (changed != null) {
			HorseIntPropertyChangedItem ic = new HorseIntPropertyChangedItem(this,id, value,
					false, overwrite);
			changed.addChangedItem(ic);
			if (notify) {
				HorseIntPropertyChangedItem oic = new HorseIntPropertyChangedItem(this,id,
						value, true, overwrite);
				changed.addChangedItem(oic);
			}
		}
	}
	
	//有些属性是不需要知道原来的值的，比如sex，clazz
	public void addIntPropertyChangedItem(Changed changed,int id,int value,boolean notify){
		if (changed != null) {
			if (notify) {
				HorseIntPropertyChangedItem oic = new HorseIntPropertyChangedItem(this,id,
						value, true);
				changed.addChangedItem(oic);
			} else {
				HorseIntPropertyChangedItem ic = new HorseIntPropertyChangedItem(this,id, value,
						false);
				changed.addChangedItem(ic);
			}
		}
	}
	

	public static Horse fromDBBytes(DataInputStream dis){
		try{
			int version = dis.read();//version;
			Horse h = null;
			if(version==1){
				int horseType = dis.readInt();
				int itemId = dis.readInt();
				int instanceId = dis.readInt();
				h = new Horse(ObjectAccessor.getHorseType(horseType),instanceId);
				h.name = dis.readUTF();
				h.itemId = itemId;
				h.level = dis.read();
				h.exp = dis.readInt();
				h.point = dis.readShort();
				h.degree = dis.readShort();
				h.strengthAdded = dis.readShort();
				h.agilityAdded = dis.readShort();
				h.intellectAdded = dis.readShort();
				h.staminaAdded = dis.readShort();
				h.speedAdded = dis.readShort();
				h.imageId = dis.readShort();
				h.iconId = dis.readShort();
				h.initLevel = dis.read();
				h.foodId = dis.readInt();
				h.skillSize = dis.read();
				int size = dis.read();
				for(int i=0;i<size;i++){
					Skill skill = ObjectAccessor.getSkill(dis.readInt());
					if(!h.skills.contains(skill))
						h.skills.add(skill);
				}
				h.equs = ItemUtil.getHorseEquipmentsFromDB(dis, h);
				h.agentHorse = 0;
				h.agentTime = 0L;
				h.leavingExp = 0;
				h.lockSkillId = 0;
				h.setActive();
			}else if(version==2){
				int horseType = dis.readInt();
				int itemId = dis.readInt();
				int instanceId = dis.readInt();
				h = new Horse(ObjectAccessor.getHorseType(horseType),instanceId);
				h.name = dis.readUTF();
				h.itemId = itemId;
				h.level = dis.read();
				h.exp = dis.readInt();
				h.point = dis.readShort();
				h.degree = dis.readShort();
				h.strengthAdded = dis.readShort();
				h.agilityAdded = dis.readShort();
				h.intellectAdded = dis.readShort();
				h.staminaAdded = dis.readShort();
				h.speedAdded = dis.readShort();
				h.imageId = dis.readShort();
				h.iconId = dis.readShort();
				h.initLevel = dis.read();
				h.foodId = dis.readInt();
				h.skillSize = dis.read();
				int size = dis.read();
				for(int i=0;i<size;i++){
					Skill skill = ObjectAccessor.getSkill(dis.readInt());
					if(!h.skills.contains(skill))
						h.skills.add(skill);
				}
				h.equs = ItemUtil.getHorseEquipmentsFromDB(dis, h);
				h.agentHorse = dis.readByte();
				h.agentTime = dis.readLong();
				h.leavingExp = dis.readInt();
				h.lockSkillId = dis.readInt();
				h.setActive();
			}else if(version==3){
				int horseType = dis.readInt();
				int itemId = dis.readInt();
				int instanceId = dis.readInt();
				h = new Horse(ObjectAccessor.getHorseType(horseType),instanceId);
				h.name = dis.readUTF();
				h.itemId = itemId;
				h.level = dis.read();
				h.exp = dis.readInt();
				h.point = dis.readShort();
				h.degree = dis.readShort();
				h.strengthAdded = dis.readShort();
				h.agilityAdded = dis.readShort();
				h.intellectAdded = dis.readShort();
				h.staminaAdded = dis.readShort();
				h.speedAdded = dis.readShort();
				h.imageId = dis.readShort();
				h.iconId = dis.readShort();
				h.initLevel = dis.read();
				h.foodId = dis.readInt();
				h.skillSize = dis.read();
				int size = dis.read();
				for(int i=0;i<size;i++){
					Skill skill = ObjectAccessor.getSkill(dis.readInt());
					if(!h.skills.contains(skill))
						h.skills.add(skill);
				}
				h.equs = ItemUtil.getHorseEquipmentsFromDB(dis, h);
				h.agentHorse = dis.readByte();
				h.agentTime = dis.readLong();
				h.leavingExp = dis.readInt();
				h.lockSkillId = dis.readInt();
				h.freeHorseEndTime = dis.readLong();
				h.state = dis.readByte();
				if(h.freeHorseEndTime > 0 && System.currentTimeMillis() < h.freeHorseEndTime ){
					h.state = 2;
				}else if(h.freeHorseEndTime > 0 && System.currentTimeMillis() > h.freeHorseEndTime ){
					//由于数据溢出导致的状态改变
					h.state = 0;
				}
				if(h.freeHorseEndTime==0){
					h.setActive();
				}
			}else if(version==4){
				int horseType = dis.readInt();
				int itemId = dis.readInt();
				int instanceId = dis.readInt();
				h = new Horse(ObjectAccessor.getHorseType(horseType),instanceId);
				h.name = dis.readUTF();
				h.itemId = itemId;
				h.level = dis.read();
				h.exp = dis.readInt();
				h.point = dis.readShort();
				h.degree = dis.readShort();
				h.strengthAdded = dis.readShort();
				h.agilityAdded = dis.readShort();
				h.intellectAdded = dis.readShort();
				h.staminaAdded = dis.readShort();
				h.speedAdded = dis.readShort();
				h.imageId = dis.readShort();
				h.imageIdChange = dis.readShort();
				h.iconId = dis.readShort();
				h.initLevel = dis.read();
				h.foodId = dis.readInt();
				h.skillSize = dis.read();
				int size = dis.read();
				for(int i=0;i<size;i++){
					Skill skill = ObjectAccessor.getSkill(dis.readInt());
					if(!h.skills.contains(skill))
						h.skills.add(skill);
				}
				h.equs = ItemUtil.getHorseEquipmentsFromDB(dis, h);
				h.agentHorse = dis.readByte();
				h.agentTime = dis.readLong();
				h.leavingExp = dis.readInt();
				h.lockSkillId = dis.readInt();
				h.freeHorseEndTime = dis.readLong();
				h.state = dis.readByte();
				if(h.freeHorseEndTime > 0 && System.currentTimeMillis() < h.freeHorseEndTime ){
					h.state = 2;
				}else if(h.freeHorseEndTime > 0 && System.currentTimeMillis() > h.freeHorseEndTime ){
					//由于数据溢出导致的状态改变
					h.state = 0;
				}
				if(h.freeHorseEndTime==0){
					h.setActive();
				}
				h.fixCount = dis.readByte();
			}else if(version==5){
				int horseType = dis.readInt();
				int itemId = dis.readInt();
				int itemIdChange = dis.readInt();
				int instanceId = dis.readInt();
				h = new Horse(ObjectAccessor.getHorseType(horseType),instanceId);
				h.name = dis.readUTF();
				h.itemId = itemId;
				h.itemIdChange = itemIdChange;
				h.level = dis.read();
				h.exp = dis.readInt();
				h.point = dis.readShort();
				h.degree = dis.readShort();
				h.strengthAdded = dis.readShort();
				h.agilityAdded = dis.readShort();
				h.intellectAdded = dis.readShort();
				h.staminaAdded = dis.readShort();
				h.speedAdded = dis.readShort();
				h.imageId = dis.readShort();
				h.imageIdChange = dis.readShort();
				h.iconId = dis.readShort();
				h.initLevel = dis.read();
				h.foodId = dis.readInt();
				h.skillSize = dis.read();
				int size = dis.read();
				for(int i=0;i<size;i++){
					Skill skill = ObjectAccessor.getSkill(dis.readInt());
					if(!h.skills.contains(skill))
						h.skills.add(skill);
				}
				h.equs = ItemUtil.getHorseEquipmentsFromDB(dis, h);
				h.agentHorse = dis.readByte();
				h.agentTime = dis.readLong();
				h.leavingExp = dis.readInt();
				h.lockSkillId = dis.readInt();
				h.freeHorseEndTime = dis.readLong();
				h.state = dis.readByte();
				if(h.freeHorseEndTime > 0 && System.currentTimeMillis() < h.freeHorseEndTime ){
					h.state = 2;
				}else if(h.freeHorseEndTime > 0 && System.currentTimeMillis() > h.freeHorseEndTime ){
					//由于数据溢出导致的状态改变
					h.state = 0;
				}
				if(h.freeHorseEndTime==0){
					h.setActive();
				}
				h.fixCount = dis.readByte();
			}else if(version==6){	//新增坐骑的ICON图标图片
				int horseType = dis.readInt();
				int itemId = dis.readInt();
				int itemIdChange = dis.readInt();
				int instanceId = dis.readInt();
				h = new Horse(ObjectAccessor.getHorseType(horseType),instanceId);
				h.name = dis.readUTF();
				h.itemId = itemId;
				h.itemIdChange = itemIdChange;
				h.level = dis.read();
				h.exp = dis.readInt();
				h.point = dis.readShort();
				h.degree = dis.readShort();
				h.strengthAdded = dis.readShort();
				h.agilityAdded = dis.readShort();
				h.intellectAdded = dis.readShort();
				h.staminaAdded = dis.readShort();
				h.speedAdded = dis.readShort();
				h.imageId = dis.readShort();
				h.imageIdChange = dis.readShort();
				h.iconImage = dis.read();
				h.iconId = dis.readShort();
				h.initLevel = dis.read();
				h.foodId = dis.readInt();
				h.skillSize = dis.read();
				int size = dis.read();
				for(int i=0;i<size;i++){
					Skill skill = ObjectAccessor.getSkill(dis.readInt());
					if(!h.skills.contains(skill))
						h.skills.add(skill);
				}
				h.equs = ItemUtil.getHorseEquipmentsFromDB(dis, h);
				h.agentHorse = dis.readByte();
				h.agentTime = dis.readLong();
				h.leavingExp = dis.readInt();
				h.lockSkillId = dis.readInt();
				h.freeHorseEndTime = dis.readLong();
				h.state = dis.readByte();
				if(h.freeHorseEndTime > 0 && System.currentTimeMillis() < h.freeHorseEndTime ){
					h.state = 2;
				}else if(h.freeHorseEndTime > 0 && System.currentTimeMillis() > h.freeHorseEndTime ){
					//由于数据溢出导致的状态改变
					h.state = 0;
				}
				if(h.freeHorseEndTime==0){
					h.setActive();
				}
				h.fixCount = dis.readByte();
			}else if(version == 7){
				int horseType = dis.readInt();
				int itemId = dis.readInt();
				int itemIdChange = dis.readInt();
				int instanceId = dis.readInt();
				h = new Horse(ObjectAccessor.getHorseType(horseType),instanceId);
				h.name = dis.readUTF();
				h.itemId = itemId;
				h.itemIdChange = itemIdChange;
				h.level = dis.read();
				h.exp = dis.readInt();
				h.point = dis.readShort();
				h.degree = dis.readShort();
				h.strengthAdded = dis.readShort();
				h.agilityAdded = dis.readShort();
				h.intellectAdded = dis.readShort();
				h.staminaAdded = dis.readShort();
				h.speedAdded = dis.readShort();
				h.imageId = dis.readShort();
				h.imageIdChange = dis.readShort();
				h.iconImage = dis.read();
				h.iconImageChange = dis.read();
				h.iconId = dis.readShort();
				h.iconIdChange = dis.readShort();
				h.initLevel = dis.read();
				h.foodId = dis.readInt();
				h.skillSize = dis.read();
				int size = dis.read();
				for(int i=0;i<size;i++){
					Skill skill = ObjectAccessor.getSkill(dis.readInt());
					if(!h.skills.contains(skill))
						h.skills.add(skill);
				}
				h.equs = ItemUtil.getHorseEquipmentsFromDB(dis, h);
				h.agentHorse = dis.readByte();
				h.agentTime = dis.readLong();
				h.leavingExp = dis.readInt();
				h.lockSkillId = dis.readInt();
				h.freeHorseEndTime = dis.readLong();
				h.state = dis.readByte();
				if(h.freeHorseEndTime > 0 && System.currentTimeMillis() < h.freeHorseEndTime ){
					h.state = 2;
				}else if(h.freeHorseEndTime > 0 && System.currentTimeMillis() > h.freeHorseEndTime ){
					//由于数据溢出导致的状态改变
					h.state = 0;
				}
				if(h.freeHorseEndTime==0){
					h.setActive();
				}
				h.fixCount = dis.readByte();
			}
			try {
				h.refreshProperties(false, null);
			} catch (Exception e) {
				
			}
			return h;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public byte[] toDBBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(7);//version
			dos.writeInt(template.id);
			dos.writeInt(itemId);
			dos.writeInt(itemIdChange);
			dos.writeInt(instanceId);
			dos.writeUTF(name);
			dos.write(level);
			dos.writeInt(exp);
			dos.writeShort(point);
			dos.writeShort(degree);
			dos.writeShort(strengthAdded);
			dos.writeShort(agilityAdded);
			dos.writeShort(intellectAdded);
			dos.writeShort(staminaAdded);
			dos.writeShort(speedAdded);
			dos.writeShort(imageId); //imageId
			dos.writeShort(imageIdChange); //imageId
			dos.write(iconImage);
			dos.write(iconImageChange);
			dos.writeShort(iconId);
			dos.writeShort(iconIdChange);
			dos.write(initLevel);
			dos.writeInt(foodId);
			dos.write(skillSize);
			dos.write(skills.size());
			for(Skill skill:skills){
				dos.writeInt(skill.getId());
			}
			dos.write(ItemUtil.getHorseEquipmentsDBBytes(equs));
			dos.writeByte(agentHorse);
			dos.writeLong(agentTime);
			dos.writeInt(leavingExp);
			dos.writeInt(lockSkillId);
			dos.writeLong(freeHorseEndTime);
			dos.writeByte(state);
			dos.write(fixCount);
		}catch(Exception e){
			e.printStackTrace();
		}
		return baos.toByteArray();
		
	}

	public byte[] toClientBytes(Unit owner){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(instanceId);
			String itemName = "";
			try {
				itemName = ObjectAccessor.getItemTemplate(itemId).name;
			} catch (Exception e) {
			}
			
			String horseName = name;
			if(fixCount > 0){
				horseName = MessageFormat.format("{0} +{1}", horseName, this.fixCount);
			}
			if(imageIdChange >= 0){
				horseName = MessageFormat.format("{0}(已幻化)", horseName);	
			}
			dos.writeUTF(itemName + "\t" + horseName + getLeavingTime());
			dos.write(level);
			dos.writeInt(exp);
			dos.writeInt(HorseUtil.getUpLevelExp(level, level+1));
			dos.writeShort(point);
			dos.writeShort(MAXDEGREE);
			dos.writeShort(degree);
			dos.writeInt(template.summonTime);
			
			dos.writeShort(strength);
			dos.writeShort(agility);
			dos.writeShort(intellect);
			dos.writeShort(stamina);
			dos.writeShort(speed);
			
			float[] f = template.fixAttributes(level, fixCount);
			int strengthFix = (int)f[0];
			int agilityFix = (int)f[1];
			int staminaFix = (int)f[2];
			int intellect = (int)f[3];
			int speedFix = (int)f[4];
			dos.writeShort(strengthFix);
			dos.writeShort(agilityFix);
			dos.writeShort(intellect);
			dos.writeShort(staminaFix);
			dos.writeShort(speedFix);
			
			dos.writeShort(score);
			dos.writeShort(imageId); //imageId
			dos.writeShort(imageIdChange); //imageId 幻化后
			dos.write(iconImage);
			dos.writeShort(iconId);
			dos.write(skillSize);
			dos.write(skills.size());
			for(Skill skill:skills){
				dos.write(skill.toClientBytes(owner));
			}
			dos.write(equs.toClientBytes());
			dos.writeBoolean((foodId == -1 || foodId == 0)?false:true);
			dos.writeUTF(template.showName);
			dos.write((agentHorse<<7)|lockSkillId);
			dos.write(state);
			ShopService shopService = Server.server.getServiceRegistry().getShopService();
			int activeId = HorseActiveCall.getActiveItemByHorseId(itemId);
			if(activeId!=-1){
				DecimalFormat df = new DecimalFormat("0.00");
				double price = (double)(shopService.getItemPrice(activeId)/36f);
				String showPrice = df.format(price);
				dos.writeUTF(showPrice);
			} else {
				dos.writeUTF("0");
			}
			dos.write(fixCount);
		}catch(Exception e){
			
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public byte[] toClientBytesAdmin(Unit owner){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(instanceId);
			String itemName = "";
			try {
				itemName = ObjectAccessor.getItemTemplate(itemId).name;
			} catch (Exception e) {
			}
			String horseName = name;
			if(fixCount > 0){
				horseName = MessageFormat.format("{0} +{1}", horseName, this.fixCount);
			}
			if(imageIdChange >= 0){
				horseName = MessageFormat.format("{0}(已幻化)", horseName);	
			}
			dos.writeUTF(itemName + "\t" + horseName + getLeavingTime());
			dos.write(level);
			dos.writeInt(exp);
			dos.writeInt(HorseUtil.getUpLevelExp(level, level+1));
			dos.writeShort(point);
			dos.writeShort(MAXDEGREE);
			dos.writeShort(degree);
			dos.writeInt(template.summonTime);
			dos.writeShort(strength);
			dos.writeShort(agility);
			dos.writeShort(intellect);
			dos.writeShort(stamina);
			dos.writeShort(speed);
			
			float[] f = template.fixAttributes(level, fixCount);
			int strengthFix = (int)f[0];
			int agilityFix = (int)f[1];
			int staminaFix = (int)f[2];
			int intellect = (int)f[3];
			int speedFix = (int)f[4];
			dos.writeShort(strengthFix);
			dos.writeShort(agilityFix);
			dos.writeShort(intellect);
			dos.writeShort(staminaFix);
			dos.writeShort(speedFix);
			
			dos.writeShort(score);
			dos.writeShort(imageId); //imageId
			dos.writeShort(imageIdChange); //imageId 幻化后
			dos.write(iconImage);
			dos.writeShort(iconId);
			dos.write(skillSize);
			dos.write(skills.size());
			for(Skill skill:skills){
				dos.write(skill.toClientBytes(owner));
			}
			dos.write(equs.toClientBytes());
			dos.writeBoolean((foodId == -1 || foodId == 0)?false:true);
			dos.writeUTF(template.showName);
			dos.write((agentHorse<<7)|lockSkillId);
			dos.write(state);
			dos.write(fixCount);
		}catch(Exception e){
			
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	public String getDesc() {
		StringBuilder sb = new StringBuilder();
		sb.append(MessageFormat.format(peony.Messages.STRING_00886, name,level,template.showName));
		int count = 0;
		if(strength > 0){
			sb.append(MessageFormat.format(peony.Messages.STRING_00887, strength));
			count++;
			if(count % 2 == 0)
				sb.append("\n");
			else
				sb.append(" ");
		}
		
		if(intellect > 0){
			sb.append(MessageFormat.format(peony.Messages.STRING_00888, intellect));
			count++;
			if(count % 2 == 0)
				sb.append("\n");
			else
				sb.append(" ");
		}
		if(agility > 0){
			sb.append(MessageFormat.format(peony.Messages.STRING_00889, agility));
			count++;
			if(count % 2 == 0)
				sb.append("\n");
			else
				sb.append(" ");
		}
		if(stamina > 0){
			sb.append(MessageFormat.format(peony.Messages.STRING_00890, stamina));
			count++;
			if(count % 2 == 0)
				sb.append("\n");
			else
				sb.append(" ");
		}
		if(speed > 0){
			sb.append(MessageFormat.format(peony.Messages.STRING_00891, speed));
			count++;
			if(count % 2 == 0)
				sb.append("\n");
			else
				sb.append(" ");
		}
		if(count % 2 == 1)
			sb.append("\n");
		sb.append("</c>");
		sb.append("<c9370DB>");
		String title = peony.Messages.STRING_00892;
		for (int i=0;i<skills.size();i++) {
			if(i == 0){
				sb.append(title);
			}
			Skill skill = skills.get(i);
			sb.append(MessageFormat.format(peony.Messages.STRING_00893, skill.getName(), skill.getLevel()));
			sb.append("\n");
		}
		sb.append("</c>");
		return sb.toString();
	}
	
	public String logString() {
		StringBuilder sb = new StringBuilder(200);
		sb.append("[TEMPLATE[").append(template.id).append("]ITEMID[").append(
				itemId).append("]NAME[").append(name).append("]INSTANCE[")
				.append(instanceId).append("]LEVEL[").append(level).append("]SKILLS[");
		for(Skill skill:skills){
			sb.append(skill.getId()).append(",");
		}
		sb.append("]]");
		return sb.toString();
	}

	public Class<? extends Marshaller> marshallerClass() {
		return HorsePersistence.class;
	}

	public Class<? extends Serializer> serializerClass() {
		return HorsePersistence.class;
	}

	/**
	 * 把对象添加到一个日志字符串中。
	 */
	public void dump(StringBuilder out) {
		out.append("TID=").append(template.id).append(",NM=").append(LogUtil.filter(name));
		out.append(",IID=").append(itemId).append(",LVL=").append(level);
		out.append(",SKLS=");
		for (int i = 0; i < skills.size(); i++) {
			if (i > 0) {
				out.append('+');
			}
			out.append(skills.get(i).getId());
		}
	}
}
