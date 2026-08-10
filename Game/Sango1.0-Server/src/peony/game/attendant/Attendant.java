package peony.game.attendant;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import peony.game.Attack;
import peony.game.CombatContext;
import peony.game.EquipmentTemplate;
import peony.game.Equipments;
import peony.game.GameItem;
import peony.game.GameItemObject;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.Marshaller;
import peony.game.NoEnoughSpaceException;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.PropertyCalculator;
import peony.game.Serializer;
import peony.game.Server;
import peony.game.Skills;
import peony.game.Time;
import peony.game.Titles;
import peony.game.TransactionBagGrid;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapReference;
import peony.game.asyncbattle.AsyncPlayer;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.changed.AttendantEquipChangedItem;
import peony.game.changed.AttendantIntProoertyChangedItem;
import peony.game.changed.AttendantSkillChangedItem;
import peony.game.changed.BindChangedItem;
import peony.game.changed.ChangedItem;
import peony.game.skill.AbstractSkill;
import peony.game.skill.Skill;
import peony.game.suite.SuiteEffect;
import peony.game.suite.SuiteEffects;
import peony.net.Packet;
import com.pip.sanguo.data.AttendantType;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.skill.BuffConfig;

/**
 * 随从实体类
 * @author dchen
 */
public class Attendant extends Unit implements GameItemObject{
	
	private static final Logger log = Logger.getLogger(Attendant.class);
	
	public AttendantType attendantType;					//随从模板
    public int attLevel=0;                                //随从等级
	public boolean[] skillSwitchs; 						//技能槽
	public boolean[] canActive;							//是否允许激活
	public List<Skill> skills; 							//每个技能槽对应的技能
	public Player owner; 								//主人
    public int qulity = 1; 								//品质 (1-9品)
    public int duration; 								//战斗状态发呆间歇
    public int distance; 								//生命链接系数
    public int eat; 									//食量
    public int loyal; 									//忠诚度
    public Titles titles; 								//称号
    public GameItem[] equs; 							//装备
    public int skillDamageType;							//普攻伤害计算类型
    public List<Skill> specialSkills;                   //非技能槽技能
    
    protected int angle;
    public int speed; 									//当前速度
    protected static String[] qulityNames = {
    	peony.Messages.STRING_01759,peony.Messages.STRING_01760,peony.Messages.STRING_01761,peony.Messages.STRING_01762,peony.Messages.STRING_01763
    	,peony.Messages.STRING_01764,peony.Messages.STRING_01765,peony.Messages.STRING_01766,peony.Messages.STRING_01766,peony.Messages.STRING_01766
    }; 													//品质名称
    public static int maxLoyal = 100; 					//最大忠诚度
    public static int GENERAL_DURATION = 10000;			//非战斗状态发呆间歇
    
    protected int cycle; 								//cycle计数器
    protected static Random random = new Random(); 		//随机数
    protected int lastAttackTime; 						//上次非战斗状态发完技能的时间
    protected int lastBattleAttackTime; 				//上次战斗状态发完技能的时间
    protected int lastDecLoyalTime;						//上次减少忠诚度时间
    
    protected static int RELIVETIME = 30000;			//自动复活时间
    public static int skillCount = 3;					//当前开放技能槽数量
    public static int SPEEDRATIO = 2;					//随从速度系数
    
    public static int ATTENDANT_REISSUE_ID = 3661;//补发随从的物品id
    public static int BASE_ATTENDANT_INSTANCEID = 500000000;
    public static int DURATION_SKILL_ID_PHISIC = 358;	//物理伤害型普攻
    public static int DURATION_SKILL_ID_MAGIC = 429;	//法术伤害型普攻
    public static int NAME_MAX_LENGTH = 7;
    public static int SKILLCLASS = 6;//编辑器里增加的新的技能
    
    public int battleType;
    public int asyncMapInstanceId;
    
    public long lastProcessCardBuffTime;
    
	public int getInstanceId() {
		return instanceId + BASE_ATTENDANT_INSTANCEID;
	}

	public Attendant(int id, Player owner){
		super(GameObject.TYPE_ATTENDANT);
		this.id = id;
		this.instanceId = Server.server.getServiceRegistry().getSleepyCatService().generateAttendantInstanceId();
		this.owner = owner;
		init();
	}
	
	public Attendant(int id, int instanceId, Player owner, ProjectData data){
		super(GameObject.TYPE_ATTENDANT);
		this.id = id;
		this.instanceId = instanceId;
		this.owner = owner;
		init0(data);
	}
	
	protected void init0(ProjectData data){
		if(data==null)
			data = Server.server.getServiceRegistry().getDataService().data;
		this.attendantType = (AttendantType) data.findObject(AttendantType.class, id);
		this.distance = attendantType.distance;
		this.qulity = attendantType.qulity;
		this.sex = attendantType.sex;
		this.equs = new GameItem[10];
		this.skillSwitchs = new boolean[6];
		this.canActive = new boolean[6];
		for(int i=0;i<attendantType.skillSwitch.length;i++){
			this.canActive[i] = attendantType.canActive[i];
		}
		this.skills = new ArrayList<Skill>();
		this.level = owner==null ? 0 : (owner.level);
		this.duration = attendantType.duration;
		this.skillDamageType = attendantType.skillType;
		int[] specialIds = attendantType.specialSkillIds;
		this.specialSkills = new ArrayList<Skill>();
		for(int i=0;i<specialIds.length;i++){
			int skillId = specialIds[i];
			Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(skillId, Math.round(this.attLevel/2)));
			if(skillId == 0)
				this.specialSkills.add(null);
			else
			    this.specialSkills.add(skill);
		}
	}
	
	protected void init(){
		ProjectData data = Server.server.getServiceRegistry().getDataService().data;
		this.attendantType = (AttendantType) data.findObject(AttendantType.class, id);
		this.name = attendantType.title;
		this.equs = new GameItem[10];
		this.duration = attendantType.duration;
		this.distance = attendantType.distance;
		this.eat = attendantType.eat;
		this.qulity = attendantType.qulity;
		this.level = owner.level;
		this.sex = attendantType.sex;
		this.loyal = maxLoyal;
		this.skillDamageType = attendantType.skillType;
		
		this.skillSwitchs = new boolean[6];
		this.canActive = new boolean[6];
		for(int i=0;i<attendantType.skillSwitch.length;i++){
			this.skillSwitchs[i] = attendantType.skillSwitch[i];
			this.canActive[i] = attendantType.canActive[i];
		}
		int[] skillIds = attendantType.skillId;
		this.skills = new ArrayList<Skill>();
		for(int i=0;i<skillIds.length;i++){
			int skillId = skillIds[i];
			Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(skillId, 1));
			if(skillId==0)
				this.skills.add(null);
			else
				this.skills.add(skill);
		}
		int[] specialIds = attendantType.specialSkillIds;
		this.specialSkills = new ArrayList<Skill>();
		for(int i=0;i<specialIds.length;i++){
			int skillId = specialIds[i];
			Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(skillId, 0));
			if(skillId == 0)
				this.specialSkills.add(null);
			else
			    this.specialSkills.add(skill);
		}
		
	}
	
	public synchronized void follow(){
		if(loyal<=0 || hp<=0)
			return;
		this.map = new VMapReference();
		this.angle = 0;
		int[] po = VMap.getAttendantPositon(owner.direct, owner);
		addToMap(owner.getVMap(),po[0],po[1]);
		owner.getVMap().addAttendant(this, owner.x, owner.y);
		owner.attendant = this;
		faction = owner.faction;
		this.lastDecLoyalTime = Time.currTime;
		if(battleType==Player.TYPE_ASYNC_PLAYER){
			if(asyncMapInstanceId>0)
				ObjectAccessor.asyncAttendants.put(AsyncPlayer.getSearchKey(getInstanceId(), asyncMapInstanceId), this);
			else
				ObjectAccessor.asyncAttendants.put(AsyncPlayer.getSearchKey(getInstanceId(), getVMap().asyncbattleInstanceId), this);
		}else
			ObjectAccessor.addGameObject(this);
		this.level = owner.level;
		owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.RIDE, 1, false));
		healthrestore = 0;
		manarestore = 0;
		owner.pool.setInt(Player.PROPERTY_LAST_ATTENDANT_INSTANCEID, getInstanceId());
		refreshProperties(false);
	}
	
	public void cancelFollow(){
		removeFromWorld();
		owner.attendant = null;
		owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.RIDE, 0, false));
	}
	
	public boolean hasSkill(int skillId){
		for(Skill skill : skills){
			if(skill!=null && skillId==skill.getId())
				return true;
		}
		return false;
	}
	
	public boolean canLight(int index){
		if(attendantType!=null && attendantType.canActive!=null){
			return attendantType.canActive[index];
		}
		return false;
	}
	
	public void lightSkill(int skillIndex){
		skillSwitchs[skillIndex] = true;
		LogUtil.logAttendantLightSkillSwitch(owner, id, instanceId, skillIndex);
	}
	
	public void equip(int itemId, int instanceId) throws AttendantException {
		if(owner!=null){
			if(Server.server.getServiceRegistry().getCandidateService().isKingEquipment(itemId))
				throw new AttendantException(peony.Messages.STRING_01767);
			PlayerTransaction tx = owner.newTransaction("ATTEQ");
			TransactionBagGrid grid = owner.bag.removeGameItemInstance(itemId, instanceId, tx, false);
			if (grid != null && grid.getItem().template.isEquipment()) {
				GameItem equ = grid.getItem();
				if (equ.template.equipment.limitType>-1&&equ.template.equipment.limitType!=LIMITTYPE_ATTENDANT) {
					tx.rollback();
					throw new AttendantException(peony.Messages.STRING_01586);
				} 
				if (equ.template.hasDuration() && equ.duration <= 0) {
					tx.rollback();
					throw new AttendantException(peony.Messages.STRING_01583);
				}
				if (equ.template.bindType == ItemTemplate.BIND_USED
						|| equ.template.bindType == ItemTemplate.BIND_REWARD) {
					if (!equ.isBound()){
						equ.bindInstance = 0;
						BindChangedItem item = new BindChangedItem(equ);
						owner.changed.addChangedItem(item);
					}
				}
				tx.commit();
				GameItem old = equip(equ, this);
				if (old != null) {
					tx = owner.newTransaction("ATTUEQ");
					grid.addGameItem(old, 1, tx, false);
					tx.commit();
				}
				if (old != null && old.template.equipment.specialEffect != null) {
					buffs.removeBuff(old.template.equipment.specialEffect);
				}
				if (old != null
						&& (old.template.equipment.suiteEffects != null)
						&& (old.template.equipment.suiteEffects != equ.template.equipment.suiteEffects)) { // 如果套装效果一样就没必要去除了
					unSuiteEffect(old.template.equipment);
				}
				// 添加套装特效
				if (equ.template.equipment.specialEffect != null) {
					Buff specialBuff = buffs
							.getBuffByID(equ.template.equipment.specialEffect.getId());
					if (specialBuff == null) {
						buffs.addBuff(equ.template.equipment.specialEffect);
					}
				}
				// 添加新装备后添加Buff
				if(equ.template.equipment.suiteEffects!=null&&equ.template.equipment.suiteEffects.type==SuiteEffects.TYPE_NORMAL){
					if (equ.template.equipment.suiteEffects != null
							&& ((old == null) || (old != null && old.template.equipment.suiteEffects != equ.template.equipment.suiteEffects))) { // 如果套装效果一样就没必要加了
						SuiteEffect[] suiteEffects = equ.template.equipment.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
						GameItem[] gameItems = equs;
						boolean isChangeBuff=false;
						for (SuiteEffect effect : suiteEffects) {
							int suiteEffectType = effect.type;
							Buff buff = effect.buff;
							if(suiteEffectType==SuiteEffect.TYPE_WEIGHT_CALC){//套装效果有权重时
								int weight=0;
								for (GameItem gameItem : gameItems) {
									if (gameItem != null
											&& gameItem.template.isEquipment()) {
										if (gameItem != null
												&& gameItem.template.equipment.suiteEffects != null
												&& gameItem.template.equipment.suiteEffects == equ.template.equipment.suiteEffects) {
											int equipWeight=gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id);
											weight+=equipWeight;
										}
									}
								}
								if(!isChangeBuff){
									isChangeBuff=true;
									buffs.removeBuff(effect.buffId);
									buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel,weight);
									buffs.addBuff(buff);
								}
							}else if(suiteEffectType==SuiteEffect.TYPE_NORMAL){//套装效果无权重时按老套装执行
								if(buffs.isNoSingleSuiteBuff(buff.getId())){//如果要加单例的套装效果必须放到Buffs.noSingleSuiteBuffs里
									buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel);
								}
								int effectCount = effect.getCount();
								int count = 0;
								if (gameItems != null) {
									for (GameItem gameItem : gameItems) {
										if (gameItem != null
												&& gameItem.template.isEquipment()) {
											if (gameItem != null
													&& gameItem.template.equipment.suiteEffects != null
													&& gameItem.template.equipment.suiteEffects == equ.template.equipment.suiteEffects) {
												count++;
											}
										}
									}
								}
								if (count == effectCount) { // 添加新装备后如果正好构成一个套装效果，则添加此套装效果
									buffs.addBuff(buff);
								}
							}
						}
					}
				}else{
					if (equ.template.equipment.suiteEffects != null) { // 如果套装效果一样就没必要加了
						SuiteEffect[] suiteEffects = equ.template.equipment.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
						GameItem[] gameItems = equs;
						boolean isChangeBuff=false;
						for (SuiteEffect effect : suiteEffects) {
							int suiteEffectType = effect.type;
							Buff buff = effect.buff;
							if(suiteEffectType==SuiteEffect.TYPE_WEIGHT_CALC){//套装效果有权重时
								int weight=0;
								for (GameItem gameItem : gameItems) {
									if (gameItem != null
											&& gameItem.template.isEquipment()) {
										if (gameItem != null
												&& gameItem.template.equipment.suiteEffects != null
												&& gameItem.template.equipment.suiteEffects == equ.template.equipment.suiteEffects) {
											int equipWeight=gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id);
											weight+=equipWeight;
										}
									}
								}
								if(!isChangeBuff){
									isChangeBuff=true;
									buffs.removeBuff(effect.buffId);
									buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel,weight);
									buffs.addBuff(buff);
								}
							}
						}
					}
				}
				processCardBuff();
			}else{
				tx.rollback();
			}
		}
	}
	
	public void unEquip(int itemId, int instanceId) throws AttendantException {
		GameItem old = null;
		for(int i=0;i<equs.length;i++){
			if(equs[i]!=null&&equs[i].template.id==itemId&&equs[i].instanceId==instanceId){
				old = equs[i];
				equs[i] = null;
				if(owner.changed!=null){
					owner.changed.addChangedItem(new AttendantEquipChangedItem(this, i, null, false));
				}
				break;
			}
		}
		if(old!=null){
			PlayerTransaction tx = owner.newTransaction("ATTUEQ");
			try {
				owner.bag.addGameItemComplete(old, 1, tx, false);
				tx.commit();
			} catch (NoEnoughSpaceException e) {
				equip(old, owner);
				tx.rollback();
				throw new AttendantException(peony.Messages.STRING_01582);
			}
			unSuiteEffect(old.template.equipment);
		}
		processCardBuff();
	}
	
	protected void unSuiteEffect(EquipmentTemplate template) {
		GameItem[] gameItems = equs; // 得到player身上的装备
		SuiteEffects effects = template.suiteEffects;
		// 清除Buff
		if (template.suiteEffects != null
				&& template.suiteEffects.getEffects() != null) {
			if (effects != null && gameItems != null) {
				boolean hadUnEquip=false;
				for (SuiteEffect effect : template.suiteEffects.getEffects()) {
					Buff buff = effect.getBuff();
					int type=effect.type;
					if(type==SuiteEffect.TYPE_WEIGHT_CALC){
						if(!hadUnEquip){
							hadUnEquip=true;
							buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel, getEquipmentsWeight(template.suiteEffects));
							buffs.removeBuff(buff.getId());
							buffs.addBuff(buff);
						}
					}else if(type==SuiteEffect.TYPE_NORMAL){
						int effectCount = effect.getCount();
						int count = 0;
						for (GameItem gameItem : gameItems) {
							if (gameItem != null
									&& template.suiteEffects != null
									&& gameItem.template.equipment.suiteEffects != null
									&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
								count++;
							}
						}
						if (count == effectCount - 1) { // 如果卸下装备后影响原来的套装效果，则清除此效果
							buffs.removeBuff(buff);
							if(buffs.isNoSingleSuiteBuff(buff.getId()))
								buffs.removeBuff(buff.getId());
						}
					}
				}
			}
		}
		//判断是否是高级覆盖低级buff
		if(template.suiteEffects!=null){
			SuiteEffect[] effects2 = template.suiteEffects.getEffects();
			ProjectData data = Server.server.getServiceRegistry().getDataService().data;
			if(template.suiteEffects.type==SuiteEffects.TYPE_NORMAL){
				BuffConfig config = (BuffConfig)data.findObject(BuffConfig.class, effects2[0].buff.getId());
				if(config.mergeStrategy==BuffConfig.MERGE_LEVEL){
					SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
					for (SuiteEffect effect : suiteEffects) {
						Buff buff = effect.buff;
						if(buffs.isNoSingleSuiteBuff(buff.getId())){//如果要加单例的套装效果必须放到Buffs.noSingleSuiteBuffs里
							buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel);
						}
						int effectCount = effect.getCount();
						int count = 0;
						if (gameItems != null) {
							for (GameItem gameItem : gameItems) {
								if (gameItem != null
										&& gameItem.template.isEquipment()) {
									if (gameItem != null
											&& gameItem.template.equipment.suiteEffects != null
											&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
										count++;
									}
								}
							}
						}
						if (count == effectCount) { // 添加新装备后如果正好构成一个套装效果，则添加此套装效果
							buffs.addBuff(buff);
						}
					}
				}
			}else if(template.suiteEffects.type==SuiteEffects.TYPE_WEIGHT){
				int equipWeight=getEquipmentsWeight(template.suiteEffects);
				SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
				boolean hadAddBuff=false;
				for (SuiteEffect effect : suiteEffects) {
					if(effect.type==SuiteEffect.TYPE_WEIGHT_CALC){
						if(!hadAddBuff){
							hadAddBuff=true;
							Buff buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel,equipWeight);
							buffs.addBuff(buff);
						}
					}
				}
			}
		}
	}
	
	/***
	 * 获取随从对应装备相同套效的权重值
	 * @return
	 */
	public int getEquipmentsWeight(SuiteEffects suiteEffects){
		int weight=0;
		for(GameItem gameItem:equs){
			if (gameItem != null
					&& gameItem.template.isEquipment()) {
				if (gameItem != null
						&& gameItem.template.equipment.suiteEffects != null&&gameItem.template.equipment.suiteEffects==suiteEffects) {
					int equipWeight=gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id)==null?0:gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id);
					weight+=equipWeight;
				}
			}
		}
		return weight;
	}
	
	private GameItem equip(GameItem equ,Unit horseOwner){
		if(!equ.template.isEquipment())
			throw new IllegalArgumentException();
		int index = Equipments.getIndex(equ.template.equipment.minorType);
		GameItem oldItem = equs[index];
		equs[index] = equ;
		if(owner.changed!=null){
			owner.changed.addChangedItem(new AttendantEquipChangedItem(this, index, equ, false));
		}
		return oldItem;
	}
	
	public Skill removeSkill(int skillId){
		Iterator<Skill> it = skills.iterator();
		while(it.hasNext()){
			Skill skill = it.next();
			if(skill!=null && skillId==skill.getId()){
				it.remove();
				return skill;
			}
		}
		return null;
	}
	
	public void addSkill(Skill skill, int index) {
		Skill oldSkill = skills.get(index);
		oldSkill = skills.set(index, skill);
		if (oldSkill != null) {
			buffs.removeBuff(oldSkill.getId());
			owner.changed.addChangedItem(new AttendantSkillChangedItem(this, oldSkill, index, false, false));
		}
		LogUtil.logAttendantStudySkill(owner,oldSkill, id, instanceId, skill.getId(), index);
		if (skill.getLevel() > 0) {
			Buff b = skill.newBuff();
			if (b != null) {
				buffs.addBuff(b);
			}
			b = skill.getAreaBuff();
			if (b != null)
				buffs.addBuff(b);
		}
		owner.changed.addChangedItem(new AttendantSkillChangedItem(this, skill, index, true, false));
	}
	
	protected int getNullSkillSwitchIndex(){
		for(int i=0;i<skillSwitchs.length;i++){
			if(skillSwitchs[i] && skills.get(i)==null)
				return i;
		}
		return -1;
	}
	
	
	public Skill getSpecialSkill(int skillId){
		for(Skill skill : specialSkills){
			if(skill!=null && skillId==skill.getGroupId())
				return skill;
		}
		return null;
	}
	
	/** 随从升级时技能自动升级 */
	public Skill refreshSpeicialSkill(int skillId,Skill newSkill){
	   if(specialSkills.size()>0){
			for(int i=0;i<specialSkills.size();i++){
				Skill skill = specialSkills.get(i);
				if(skill!=null && skillId==skill.getId()){
					if (skill != null) {
						Buff b = skill.newBuff();
						if(b!=null && owner!=null && owner.attendant!=null && owner.attendant.getInstanceId() == this.getInstanceId()){
						   owner.buffs.removeBuff(b.getId());
						   owner.refreshProperties(false);
						}
					}
					specialSkills.set(i,newSkill);
					if (newSkill.getLevel() > 0 && newSkill.getClazz() == SKILLCLASS) {
						Buff b = newSkill.newBuff();
						if (b != null && owner!=null && owner.attendant!=null && owner.attendant.getInstanceId() == this.getInstanceId()) {
						    owner.buffs.addBuff(b);
						    owner.refreshProperties(false);
						}
					}
					return skill;
				}
			}
		}
		return null;
	}
		
	/** 随从页面上显示的特殊技能*/
	public int getShowSkillId(){
		return attendantType.specialSkillIds[0];
	}
	
	public boolean hasEquipments(){
		for(GameItem item : equs){
			if(item!=null && item.template!=null && item.template.isEquipment())
				return true;
		}
		return false;
	}
	
	public int lastCheckEquipTime;
	public void updateEquipState(){
		if(Time.currTime-lastCheckEquipTime>1000*60*10){
			List<GameItem> list = new ArrayList<GameItem>();
			int count = 0;
			for(GameItem item:this.equs){
				if(item!=null && item.template!=null ){
					if(System.currentTimeMillis()/60000>item.validTime&& item.validTime>0){
						item.validTime=-1;
					}
					if (item != null && item.template.equipment.specialEffect != null&&item.validTime==-1) {
						buffs.removeBuff(item.template.equipment.specialEffect);
					}
					owner.changed.addChangedItem(new AttendantEquipChangedItem(this, count, item, false));
					refreshProperties(false);
					processCardBuff();
					list.add(item);
				}
				count++;
			}
//			GameItem[] equipsItems=null;
//			if(list.size()>0){
////				equipsItems=new GameItem[list.size()];
//				for(int i=0;i<list.size();i++){
////					equipsItems[i]=(GameItem)list.get(i);
//					BindChangedItem item = new BindChangedItem((GameItem)list.get(i));
//					owner.changed.addChangedItem(item);
//				}
//			}
//			InvalidItem invalidItem=new InvalidItem(equipsItems,null);
//	    	this.changed.addChangedItem(invalidItem);
	    	lastCheckEquipTime=Time.currTime;
		}
	}
	
	public void update(int diffTime) {
		if ((Time.tick % 5) == 0) {
			try {
				if(ObjectAccessor.getPlayer(owner.id)!=null || ObjectAccessor.getAsyncGameObject(owner.id, owner.asyncMapInstanceId)!=null){
					updateEquipState();
					cycle++;
					processMove(owner);
					if (attack != null) {
						if (attack.update(diffTime) != -1)
							attack = null;
					}
					if(isAlive() && Time.currTime - lastRestoreTime > 5000) {
						lastRestoreTime = Time.currTime;
						setHp(this.hp + healthrestore, false);
						setMp(this.mp + manarestore, false);
					}
					processAttendantSkill();
					processDecLoyal();
					listenHp();
					processMoveExt();
					coolDowns.update();
					if(System.currentTimeMillis()-lastProcessCardBuffTime>Player.checkCardBuffDis){
						processCardBuff();
						lastProcessCardBuffTime = System.currentTimeMillis();
					}
					try {
						buffs.update(diffTime);
					} catch (Exception e) {
						log.error(e, e);
					}
				}
			} catch (Exception ex) {
				log.error(ex,ex);
			}
		}
	}
	
	protected void processCardBuff(){
//		CardService service = Server.server.getServiceRegistry().getCardService();
//		for(int buff : service.getCardBuffs())
//			buffs.removeBuff(buff);
//		for(GameItem item : equs){
//			if(item!=null && item.template!=null && item.template.isEquipment()){
//				Object obj = item.object;
//				if(obj!=null && obj instanceof ItemEnhance){
//					ItemEnhance enhance = (ItemEnhance)obj;
//					for(int i=0;i<enhance.cards.length;i+=2){
//			        	int cardId = enhance.cards[i + 1];
//			            service.addCardBuff(owner, cardId, PropertyCalculator.TYPE_ATTENDANT, getInstanceId(), item.template.id, item.instanceId);
//					}
//				}
//			}
//		}
	}
	
	protected void listenHp(){
		if(hp<=0){
			cancelFollow();
			setLoyal(loyal-2<=0 ? 0 : loyal-2);
			if(owner!=null)
				owner.attendantWaitRelive = true;
			Server.server.scheduExec.schedule(new Runnable(){
				public void run() {
					if(owner!=null && owner.attendantBag.attendants.size()>0 && owner.attendantBag.getAttendant(Attendant.this.getInstanceId())!=null){
						setHp(maxhp, false);
						setMp(maxmp, false);
						//随从死亡时如果忠诚度为0，自动消耗随从激励符
						if(loyal <= 0){
							addLoyal();
						}
						if(owner.attendant==null){
	//						follow();
	//						addBuff((Player)owner);
							Server.server.getWorld().schedule(new AttendantFollowCall(Attendant.this));
						}
					}else{
						owner.attendantWaitRelive = false;
					}
				}
			}, RELIVETIME, TimeUnit.MILLISECONDS);
		}
	}
	
	public void addBuff(Player player){
		if(player!=null){
			if(loyal<=0 || hp<=0)
				return;
			removeBuff((Player)owner);
			boolean refresh = false;
			for(Skill skill:this.specialSkills){
				if(skill!=null && skill.getLevel()>0){
					Buff buff = skill.newBuff();
					if(buff!=null){
					     player.buffs.addBuff(buff);
					     refresh = true;
					}
				}
			}
			if(refresh){
				player.refreshProperties(false);
			}
		}
	}
	
	public void removeBuff(Player player){
		boolean refresh = false;
		for(Buff buff : player.buffs.getBuffs()){
			Skill skill = ObjectAccessor.getSkill(buff.getId());
			if(skill!=null && skill.getClazz() == Attendant.SKILLCLASS){
				player.buffs.removeBuff(buff);
				refresh = true;
			}
		}
		if(refresh)
		    player.refreshProperties(false);
	}
	
	
	public void addLoyal(){
		PlayerTransaction tx = owner.newTransaction("ATTLOYAL");
		GameItem item = owner.bag.removeGameItemIngoreInstanceId(ItemUtil.ATTENDANT_ADDLOYAL_ITEM, 1, tx, false);
		if(item!=null){
			setLoyal(maxLoyal);
			tx.commit();
		}else{
			tx.rollback();
			cancelFollow();
			removeBuff((Player)owner);//移除玩家身上的随从特殊buff
		}
	}
	
	public void addLoyal(int point){
		PlayerTransaction tx = owner.newTransaction("ATTLOYAL");
		try{
			LogUtil.logAttChangeLoyal(this, this.loyal, point);
			setLoyal(point);
			tx.commit();
		}catch(Exception e){
			tx.rollback();
		}
		
		
		
	}
	
	protected void processMoveExt(){
		if((moveExtended&MOVEEXT_BUFFS)!=0){
			Packet pt = getBuffsPacket();
			broadcast(pt,null,null,false,false,false);
		}
		moveExtended = 0;
	}
	
	protected void processDecLoyal(){
		if(Time.currTime-lastDecLoyalTime>=120000 && loyal>0){
			int currentLoyal = this.loyal - 1;
			lastDecLoyalTime = Time.currTime;
			setLoyal(currentLoyal);
		}
		if(loyal<=0){
			PlayerTransaction tx = owner.newTransaction("ATTLOYAL");
			GameItem item = owner.bag.removeGameItemIngoreInstanceId(ItemUtil.ATTENDANT_ADDLOYAL_ITEM, 1, tx, false);
			if(item!=null){
				setLoyal(maxLoyal);
				tx.commit();
			}else{
				tx.rollback();
				cancelFollow();
				removeBuff((Player)owner);//移除玩家身上的随从特殊buff
			}
		}
	}
	
	public int canAttack(GameObject unit) {
		if(attack!=null && (attack.getSkill().getType() & Skill.TYPE_ATTACK)==1){
			if(owner!=null && owner.id==unit.id)
				return 8;
		}
		if(owner!=null)
			return owner.canAttack(unit);
		return super.canAttack(unit);
	}
	
	protected void processAttendantSkill(){
		if(owner.getVMap()==null)
			return;
		int index = random.nextInt(skillSwitchs.length);
		if(owner.attack==null && owner.getAutoAttack()==null 
				|| (owner.attack!=null && owner.attack.getSkill()!=null && !((AbstractSkill)owner.attack.getSkill()).isAttackSkill())  
				|| (owner.getAutoAttack()!=null && owner.getAutoAttack().getSkill()!=null && !((AbstractSkill)owner.getAutoAttack().getSkill()).isAttackSkill())){
			// 主人非战斗状态随从释放技能
			if(Time.currTime-lastAttackTime>=GENERAL_DURATION){
				for(int i=0;i<5;i++){
					if(skillSwitchs[index]==false || skills.get(index)==null 
							|| (skills.get(index)!=null && skills.get(index).getClazz()!=-2)){
						index = random.nextInt(skillSwitchs.length);
					}
				}
				if(skills.get(index)!=null && skills.get(index).getClazz()==-2 
						&& !coolDowns.atCoolDown(skills.get(index).getGroupId())){
					prepareSkillAttack(this, skills.get(index), 0);
					lastAttackTime = Time.currTime;
				}
			}
		}else{
			// 主人战斗状态随从释放的技能
			if(Time.currTime-lastBattleAttackTime>=duration){
//				index = random.nextInt(skillSwitchs.length);
//				if(skills.get(index)!=null && skills.get(index).getClazz()!=-2 
//						&& !coolDowns.atCoolDown(skills.get(index).getGroupId())){
//					Unit target = getTarget();
//					if(target!=null && target.faction!=owner.faction){
//						prepareSkillAttack(target, skills.get(index), 0);
//					}
//					lastBattleAttackTime = Time.currTime;
//				}else{
				Skill nextSkill = getNextSkill();
				if(nextSkill!=null && mp>0 && nextSkill.getMP(this)>0){
					Unit target = getTarget();
					if(target!=null && (target.faction!=owner.faction || target.minorFaction!=owner.minorFaction) || Server.isStepServer || owner.battleType==Player.TYPE_ASYNC_PLAYER)
						prepareSkillAttack(target, nextSkill, 0);
				}else{
					Unit target = getTarget();
					if(target!=null && (target.faction!=owner.faction || target.minorFaction!=owner.minorFaction) || Server.isStepServer){
						Skill skill = ObjectAccessor.getSkill(Skills.getSkillId(DURATION_SKILL_ID_PHISIC, 1));
						if(skillDamageType==CombatContext.DAMAGE_MAGIC)
							skill = ObjectAccessor.getSkill(Skills.getSkillId(DURATION_SKILL_ID_MAGIC, 1));
						prepareSkillAttack(target, skill, 0);
					}
				}
				lastBattleAttackTime = Time.currTime;
			}
		}
	}
	
	protected Skill getNextSkill(){
		Skill skill = null;
		for(int i=0;i<skills.size();i++){
			if(skillSwitchs[i]){
				Skill currSkill = skills.get(i);
				if(currSkill!=null && currSkill.getClazz()!=-2 && !coolDowns.atCoolDown(currSkill.getGroupId())){
					if(skill==null || skill.getCDTime(this)<currSkill.getCDTime(this))
						skill = currSkill;
				}
			}
		}
		return skill;
	}
	
	/**
	 * 战斗状态获取目标
	 */
	public Unit getTarget(){
		Unit target = null;
		Attack attack = null;
		if(owner.attack!=null){
			attack = owner.attack;
		}else if(owner.getAutoAttack()!=null){
			attack = owner.getAutoAttack();
		}
		if(attack!=null){
			if(attack.targetRef!=null){
				GameObject tar = ObjectAccessor.getGameObject(attack.targetRef);
				if(tar instanceof Unit && tar.id!=owner.id)
					target = (Unit) ObjectAccessor.getGameObject(attack.targetRef);
				if(attack.targetRef.battleType==Player.TYPE_ASYNC_PLAYER)
					target = (Unit) ObjectAccessor.getAsyncGameObject(attack.targetRef.instanceId, attack.targetRef.mapInstanceId);
			}else if(attack.targets!=null && attack.targets.length>0){
				for(GameObjectRef ref : owner.attack.targets){
					if(ref!=null){
						GameObject tar = ObjectAccessor.getGameObject(attack.targetRef);
						if(tar instanceof Unit && tar.id!=owner.id){
							target = (Unit) ObjectAccessor.getGameObject(attack.targetRef);
							break;
						}
						if(ref.battleType==Player.TYPE_ASYNC_PLAYER){
							target = (Unit) ObjectAccessor.getAsyncGameObject(ref.instanceId, ref.mapInstanceId);
							break;
						}
					}
				}
			}
		}
		return target;
	}
	
	public int prepareSkillAttack(Unit target, Skill skill, int offsetTime) {
		int ret = super.prepareSkillAttack(target, skill, offsetTime);
		if (target != this && target!=null) {
			try {
				int newAngle = calcAngle(x, y, target.x, target.y);
				if(newAngle!=angle){
					angle = newAngle;
					moveType |= MOVE_ANGLE;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	public GameObjectRef ref() {
		if(map!=null && map.map!=null){
			if(asyncMapInstanceId>0)
				return new GameObjectRef(type, id, getInstanceId(), asyncMapInstanceId, battleType);
			return new GameObjectRef(type, id, getInstanceId(), map.map.asyncbattleInstanceId, battleType);
		}
		return new GameObjectRef(type, id, getInstanceId(), 0, battleType);
	}
	
	public boolean isStatic() {
		if(battleType==Player.TYPE_ASYNC_PLAYER)
			return false;
		return true;
	}
	
	public void go(boolean notify) {
		super.go();
		if(notify)
			moveType |= MOVE_RUNNING_STATE;
	}
	
	public void goMap(int mapId, int x, int y) {
		if(map.id==mapId)
			move(x,y);
		else{
			removeFromMap();
			NoInstanceVMapManager manager = (NoInstanceVMapManager)Server.server.getWorld().getVMapManager(mapId);
			VMap[] maps = manager.getVMaps(mapId);
			maps[0].addAttendant(this, x, y);
		}
	}
	
	public void removeFromWorld(){
		VMap map = getVMap();
		if(map!=null){
			map.removeGameObject(this, true);
			ObjectAccessor.asyncAttendants.remove(AsyncPlayer.getSearchKey(getInstanceId(), map.asyncbattleInstanceId));
		}
		ObjectAccessor.removeGameObject(this);
		ObjectAccessor.asyncAttendants.remove(AsyncPlayer.getSearchKey(getInstanceId(), asyncMapInstanceId));
		healthrestore = 0;
		manarestore = 0;
	}
	
	public Packet getInfoPacket(){
		Packet pt = new Packet(OpCode.UNIT_INFO_SERVER);
		pt.putInt(getInstanceId());
		pt.put(0);
		pt.put(0);
		pt.putString("");
		return pt;
	}
	
	protected byte getDirect(int beginX,int beginY,int endX,int endY){
		if(beginX==endX){
			if(endY==beginY)
				return direct;
			return endY > beginX?DIRECT_DOWN:DIRECT_UP;
		}
		else {
			return endX > endY?DIRECT_RIGHT:DIRECT_LEFT;
		}
	}
	
	public void setInvisible(){
		super.setInvisible();
		removeFromMap();
		ObjectAccessor.removeGameObject(this);
	    VMap.notifyDisappear(this);
		moveType |= MOVE_STATE;
	}
	
	public void setInvisibleOnly(){
		super.setInvisible();
		VMap.notifyDisappear(this);
		moveType |= MOVE_STATE;
	}

	public void setVisibleOnly(){
        super.setVisible();
        VMap.notifyAppear(this);
        moveType |= MOVE_ALL;
	}
	
	public void setVisible(){
        super.setVisible();
        VMap.notifyAppear(this);
        moveType |= MOVE_ALL;
    }
	
	public void stop(int angle){
		this.stop();
		this.angle = angle;
	}
	
	public void stop(){
		super.stop();
		moveType |= MOVE_RUNNING_STATE;
	}
	
	public void fear(){
		super.fear();
		moveType |= MOVE_POINT_STATE;
	}
	
	public void unFear(){
		super.unFear();
		moveType |= MOVE_POINT_STATE;
	}
	
	public void paralyze(){
		super.paralyze();
		moveType |= MOVE_POINT_STATE;
	}
	
	public void unParalyze(){
		super.unParalyze();
		moveType |= MOVE_POINT_STATE;
	}
	
	public void stay(){
		super.stay();
		moveType |= MOVE_POINT_STATE;
	}
	
	public void unStay(){
		super.unStay();
		moveType |= MOVE_POINT_STATE;
	}
	
	public boolean isRunning(){
		if((state&STATE_STOP)!=0)
			return false;
		return super.isRunning();
	}
	
	public void setAttack(Attack attack){
		super.setAttack(attack);
	}
	
	public int getHalfAngle(){
		return angle/2;
	}
	
	public void removeFromMap(){
		if(this.map!=null&&map.map!=null){
			map.removeGameObject(this,true);
		}
	}
	
	public void backState(int x,int y){
		clearThreats();
		buffs.removeUpdatableBuffs();
		move(x,y);
		moveType|=GameObject.MOVE_RUNNING;
		setHp(maxhp,false);
		setMp(maxmp, false);
	}
	
	public Packet getMovePacket(short moveType){
		moveType &= ~MOVE_HORSE;
		Packet pt = new Packet(OpCode.UNIT_MOVE_SERVER);
		pt.put(type|moveType);
		pt.putInt(getInstanceId());
		if((moveType&MOVE_POINT)!=0){
			pt.putShort(map.id);
			pt.putShort(x);
			pt.putShort(y);
		}
		if ((moveType & MOVE_ANGLE) != 0) {
			pt.put(getHalfAngle());
			pt.putInt(Time.currTime);
			pt.put(getSpeed());
			if (!isRunning()) {
				pt.putShort(-1);
				pt.putShort(-1);
			} else {
				pt.putShort(getNextPointX());
				pt.putShort(getNextPointY());
			}
		}
		if((moveType&MOVE_HPMP)!=0){
			pt.put(maxhp==0?200:hp*200/maxhp);
			pt.put(maxmp==0?200:mp*200/maxmp);			
		}
		if((moveType&MOVE_STATE)!=0){
			pt.putShort(state);
		}
		if((moveType&MOVE_DETAIL)!=0){
			pt.put(moveType>>8);
			if((moveType&MOVE_NAME)!=0){
				pt.putString(name);
			}
			if((moveType&MOVE_LEVEL)!=0){
				pt.put(level);
				pt.put(qulity);
			}
			if((moveType&MOVE_FACTION)!=0){
				pt.put(faction);
			}
			if((moveType&MOVE_EQUIPMENT)!=0){
				pt.putInt(head_score);
				pt.putInt(body_score);
				pt.putInt(weapon_score);
				pt.put(flashLevel);
			}
			if((moveType&MOVE_SEX)!=0){
				pt.put(sex);
			}
			if((moveType&MOVE_OWNER)!=0){
				pt.putInt(-1);
			}
			if((moveType&MOVE_CLAZZ)!=0){
				pt.put(clazz);
			}
		}
		return pt;
	}
	
	public Packet getRefreshPacket(boolean visible){
		Packet pt = new Packet(OpCode.UNIT_REFRESH_SERVER);
		byte b = type;
		if(!visible){
			b |= 1<<7;
		}
		pt.put(b);
		pt.putInt(id);
		pt.putInt(getInstanceId());
		if(visible)
			pt.putShort(attendantType.image.getID());
		return pt;
	}
	
	public void getRefreshPacket(Packet pt,boolean visible){
		byte b = type;
		if(!visible){
			b |= 1<<7;
		}
		pt.put(b);
		pt.putInt(id);
		pt.putInt(getInstanceId());
		if(visible)
			pt.putShort(attendantType.image.getID());
	}
	
	public void shout(String message,int dist,int color, int duration){
		VMap map = getVMap();
		if(map!=null){
			List<Player> l = map.getPlayersInRange(this, dist);
			if(l.size()>0){
				Packet pt = new Packet(OpCode.SHOUT_SERVER);
				pt.putString(message);
				pt.putInt(color);
				pt.putInt(duration);
				for(Player p:l){
					p.send(pt);
				}
			}
		}
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public void enhance(PropertyCalculator pc, boolean includeBasicAttrs) {
		for(int i=0;i<equs.length;i++){
			GameItem item = equs[i];
			if(item!=null){
				if(item.validTime!=-1){				
				    if (includeBasicAttrs) {
				        item.enhance(pc);
				    } else {
				        item.enhanceWithOutBasicAttrs(pc);
				    }
				}
			}
		}
	}
	
	public void refreshProperties(boolean levelUp){
		PropertyCalculator calc = new PropertyCalculator(this);
		processPreBuff(calc);
		//属性增强
		enhance(calc, true);
		//处理随从特殊buff
		Server.server.getServiceRegistry().getAttendantFixService().addSpecialBuff(this);
		buffs.enhance(calc);
		//属性计算
		calc.attendantPtyCaculate();
		//属性改变通知
		setDefense(calc.defense); //护甲
		setSpelldefense(calc.spelldefense); //法防
		setAttackpowerdown(calc.attackpowerdown); //物攻下线
		setAttackpowerup(calc.attackpowerup); //物攻上线
		setSpellpower(calc.spellpower); //法攻
		setCritical(calc.critical); //物理暴击
		setSpellCritical(calc.spellcritical); //法术暴击
		setStrength(calc.strength); //力
		setAgility(calc.agility); //敏
		setIntellect(calc.intellect); //智
		setStamina(calc.stamina); //体
		setAnticrit(calc.anticrit); //免暴
		setHit(calc.hit); //物命中
		setSpellhit(calc.spellhit); //法命
		setDodge(calc.dodge); //物闪
		setSpelldodge(calc.spelldodge); //法闪
		setHealthrestore(calc.healthrestore); //5秒回蓝
		setManarestore(calc.manarestore); //5秒回血
		setMaxhp(calc.basichp + calc.hp + stamina * 12); //最大生命
		setMaxmp((int) (calc.basicmp + calc.mp + agility + stamina * 2 + intellect * 1.5 + strength)); //最大精力
		setHp(this.hp, false);
		setMp(this.mp, false);
		if(equs!=null)
			setFlashLevel(Equipments.getFlashLevel(equs));
	}
	
	public void setFlashLevel(int value){
		if(this.flashLevel!=value){
			owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.FLASHLEVEL, value, false));
			this.flashLevel = value;
			moveType |= MOVE_EQUIPMENT|MOVE_DETAIL;
		}
	}
	
	public void setManarestore(int value){
		if(this.manarestore!=value){
			owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.MANARESTORE, value, false));
			this.manarestore = value;
		}
	}
	
	public void setHealthrestore(int value){
		if(this.healthrestore!=value){
			owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.HEALTHRESTORE, value, false));
			this.healthrestore = value;
		}
	}
	
	public void setAnticrit(float value){
		int oldValue = Math.round(this.anticrit*100);
		this.anticrit = value;
		int newValue = Math.round(this.anticrit*100);
		if(oldValue!=newValue){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.ANTICRIT, Math.round(newValue), false));
			}
			this.anticrit = value;
		}
	}
	
	public void setLoyal(int value){
		if(this.loyal!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.LOYAL, value, false));
			}
			this.loyal = value;
		}
	}
	
	public void setStrength(int value){
		if(this.strength!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.STRENGTH, value, false));
			}
			this.strength = value;
		}
	}
	
	public void setAgility(int value){
		if(this.agility!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.AGILITY, value, false));
			}
			this.agility = value;
		}
	}
	
	public void setIntellect(int value){
		if(this.intellect!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.INTELLECT, value, false));
			}
			this.intellect = value;
		}
	}
	
	public void setStamina(int value){
		if(this.stamina!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.STAMINA, value, false));
			}
			this.stamina = value;
		}
	}
	
	public void setCritical(float value){
		if(this.critical!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.CRITICAL, Math.round(value * 100), false));
			}
			this.critical = value;
		}
	}
	
	public void setSpellCritical(float value){
		if(this.spellcritical!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.SPELLCRITICAL, Math.round(value * 100), false));
			}
			this.spellcritical = value;
		}
	}
	
	public void setSpellpower(float value){
		if(this.spellpower!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.SPELLPOWER, Math.round(value), false));
			}
			this.spellpower = value;
		}
	}
	
	public void setAttackpowerup(float value){
		if(this.attackpowerup!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.ATTACKPOWERUP, Math.round(value), false));
			}
			this.attackpowerup = value;
		}
	}
	
	public void setAttackpowerdown(float value){
		if(this.attackpowerdown!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.ATTACKPOWERDOWN, Math.round(value), false));
			}
			this.attackpowerdown = value;
		}
	}
	
	public void setSpelldefense(float value){
		if(this.spelldefense!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.SPELLDEFENSE, Math.round(value), false));
			}
			this.spelldefense = value;
		}
	}
	
	public void setDefense(float value){
		if(this.defense!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.DEFENSE, Math.round(value), false));
			}
			this.defense = value;
		}
	}
	
	public int setHp(int value, boolean notify){
		value = Math.min(maxhp, value);
		value = (value<0 ? 0 : value);
		if(this.hp!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.HP, value, false));
			}
			this.hp = value;
		}
		return value;
	}
	
	public void setMaxhp(int value){
		if(this.maxhp!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.MAXHP, value, false));
			}
			this.maxhp = value;
			setHp(Math.min(this.maxhp, this.hp), false);
		}
	}
	
	public void setMp(int value, boolean notify){
		if(this.mp!=value){
			value = Math.min(maxmp, value);
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.MP, value, false));
			}
			this.mp = value;
		}
	}
	
	public void setMaxmp(int value){
		if(this.maxmp!=value){
			if(owner.changed!=null){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.MAXMP, value, false));
			}
			this.maxmp = value;
			setMp(Math.min(this.maxmp, this.mp),false);
		}
	}
	
	public void setHit(float value){
		if(this.hit!=value){
			int oldValue = Math.round(this.hit*100);
			this.hit = value;
			int newValue = Math.round(this.hit*100);
			if(oldValue!=newValue){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.HIT, Math.round(newValue), false));
			}
		}
	}
	
	public void setSpellhit(float value){
		if(this.spellhit!=value){
			int oldValue = Math.round(this.spellhit*100);
			this.spellhit = value;
			int newValue = Math.round(this.spellhit*100);
			if(oldValue!=newValue){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.SPELLHIT, Math.round(newValue), false));
			}
		}
	}
	
	public void setDodge(float value){
		if(this.dodge!=value){
			int oldValue = Math.round(this.dodge*100);
			this.dodge = value;
			int newValue = Math.round(this.dodge*100);
			if(oldValue!=newValue){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.DODGE, Math.round(newValue), false));
			}
		}
	}
	
	public void setSpelldodge(float value){
		if(this.spelldodge!=value){
			int oldValue = Math.round(this.spelldodge*100);
			this.spelldodge = value;
			int newValue = Math.round(this.spelldodge*100);
			if(oldValue!=newValue){
				owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.SPELLDODGE, Math.round(newValue), false));
			}
		}
	}
	
	public void setSpellheal(float value){
        if(this.spellheal!=value){
            int oldValue = Math.round(this.spellheal);
            this.spellheal = value;
            int newValue = Math.round(this.spellheal);
            if(oldValue!=newValue){
            	owner.changed.addChangedItem(new AttendantIntProoertyChangedItem(this, ChangedItem.TYPE_ATTENDANT_INT, ChangedItem.SPELLHEAL, Math.round(newValue), false));
            }
        }
    }
    
    public byte[] toDBBytes(){
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(3);//version
			dos.writeInt(id);
			dos.writeInt(instanceId);
			dos.writeUTF(name);
			dos.writeInt(attLevel);
			dos.writeInt(loyal);
			dos.writeInt(hp);
			dos.writeInt(mp);
			dos.write(skillSwitchs.length);
			for(boolean b : skillSwitchs){
				dos.write(b ? 1 : 0);
			}
			dos.write(skills.size());
			for(Skill skill:skills){
				if(skill==null)
					dos.writeInt(0);
				else
					dos.writeInt(skill.getId());
			}
;			dos.write(specialSkills.size());
			if(specialSkills.size()>0){
				for(Skill skill : specialSkills){
					if(skill==null)
						dos.writeInt(0);
					else
						dos.writeInt(skill.getId());
				}
			}
			dos.write(ItemUtil.getEquipmentsDBBytes(equs));
		}catch(Exception e){
			e.printStackTrace();
		}
		return baos.toByteArray();
    }
    
    public static Attendant fromDBBytes(DataInputStream dis, Player owner, ProjectData data){
    	StringBuilder sb = new StringBuilder(200);
    	try{
    		sb.append("[ATTENDANT]");
    		if(owner!=null)
    			sb.append("PLAYER["+owner.id+"]");
    		sb.append("VS[");
			int version = dis.read();//version;
			sb.append(version+"]ID[");
			Attendant attendant = null;
			if(version==1){
				int id = dis.readInt();
				sb.append(id+"]INS[");
				int instanceId = dis.readInt();
				sb.append(instanceId+"]NAME[");
				attendant = new Attendant(id,instanceId,owner,data);
				sb.append("]TID[" + attendant.attendantType.id);
				attendant.name = dis.readUTF();
				sb.append(attendant.name+"]LOYAL[");
				attendant.loyal = dis.read();
				sb.append(attendant.loyal+"]HP[");
				attendant.hp = dis.read();
				sb.append(attendant.hp+"]MP[");
				attendant.mp = dis.read();
				sb.append(attendant.mp+"]SWITCH[");
				int switchSize = dis.read();
				for(int i=0;i<switchSize;i++){
					int b = dis.read();
					attendant.skillSwitchs[i] = (b==1 ? true : false);
					sb.append(b+",");
				}
				sb.append("]SKILLSIZE[");
				int size = dis.read();
				sb.append(size+"]SKILL[");
				for(int i=0;i<size;i++){
					int skillId = dis.readInt();
					sb.append(skillId+",");
					Skill skill = ObjectAccessor.getSkill(skillId);
					if(skillId==0){
						attendant.skills.add(null);
					}else{
						attendant.skills.add(skill);
					}
				}
				sb.append("]EQP[");
				attendant.equs = ItemUtil.getAttendantEquipmentsFromDB(dis, attendant);
				sb.append(getEquipLog(attendant.equs)+"]");
			}else if(version==2){
				int id = dis.readInt();
				sb.append(id+"]INS[");
				int instanceId = dis.readInt();
				sb.append(instanceId+"]NAME[");
				attendant = new Attendant(id,instanceId,owner,data);
				attendant.name = dis.readUTF();
				sb.append("]TID[" + attendant.attendantType.id);
				sb.append(attendant.name+"]LOYAL[");
				attendant.loyal = dis.readInt();
				sb.append(attendant.loyal+"]HP[");
				attendant.hp = dis.readInt();
				sb.append(attendant.hp+"]MP[");
				attendant.mp = dis.readInt();
				sb.append(attendant.mp+"]SWITCH[");
				int switchSize = dis.read();
				for(int i=0;i<switchSize;i++){
					int b = dis.read();
					attendant.skillSwitchs[i] = (b==1 ? true : false);
					sb.append(b+",");
				}
				sb.append("]SKILLSIZE[");
				int size = dis.read();
				sb.append(size+"]SKILL[");
				for(int i=0;i<size;i++){
					int skillId = dis.readInt();
					sb.append(skillId+",");
					Skill skill = ObjectAccessor.getSkill(skillId);
					if(skillId==0){
						attendant.skills.add(null);
					}else{
						attendant.skills.add(skill);
					}
				}
				sb.append("]EQP[");
				attendant.equs = ItemUtil.getAttendantEquipmentsFromDB(dis, attendant);
				sb.append(getEquipLog(attendant.equs)+"]");
				
			}else if(version == 3){
				int id = dis.readInt();
				sb.append(id+"]INS[");
				int instanceId = dis.readInt();
				sb.append(instanceId+"]NAME[");
				attendant = new Attendant(id,instanceId,owner,data);
				attendant.name = dis.readUTF();
				sb.append("]TID[" + attendant.attendantType.id);
				sb.append(attendant.name+"]LEVEL[");
				attendant.attLevel = dis.readInt();
				sb.append(attendant.attLevel+"]LOYAL[");
				attendant.loyal = dis.readInt();
				sb.append(attendant.loyal+"]HP[");
				attendant.hp = dis.readInt();
				sb.append(attendant.hp+"]MP[");
				attendant.mp = dis.readInt();
				sb.append(attendant.mp+"]SWITCH[");
				int switchSize = dis.read();
				for(int i=0;i<switchSize;i++){
					int b = dis.read();
					attendant.skillSwitchs[i] = (b==1 ? true : false);
					sb.append(b+",");
				}
				sb.append("]SKILLSIZE[");
				int size = dis.read();
				sb.append(size+"]SKILL[");
				for(int i=0;i<size;i++){
					int skillId = dis.readInt();
					sb.append(skillId+",");
					Skill skill = ObjectAccessor.getSkill(skillId);
					if(skillId==0){
						attendant.skills.add(null);
					}else{
						attendant.skills.add(skill);
					}
				}
				sb.append("]SPECIALSKILLSIZE[");
				int size2 = dis.read();
				sb.append(size2+"]SKILL[");
				if(size2>0){
					attendant.specialSkills.clear();
				}
				for(int i=0;i<size2;i++){
					int skillId = dis.readInt();
					sb.append(skillId+",");
					Skill skill = ObjectAccessor.getSkill(skillId);
					if(skill!=null){
						attendant.specialSkills.add(skill);
					}
				}
				sb.append("]EQP[");
				attendant.equs = ItemUtil.getAttendantEquipmentsFromDB(dis, attendant);
				sb.append(getEquipLog(attendant.equs)+"]");
			}
			if(attendant!=null && attendant.owner!=null)
				attendant.refreshProperties(false);
			LogUtil.logAttendant(attendant, "[ATTENDANT]");
			return attendant;
		}catch(Exception e){
			log.info(sb.toString());
			e.printStackTrace();
			return null;
		}
    }
    
    public byte[] toClientBytes(Unit owner){
    	refreshProperties(false);
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(getInstanceId());
			dos.writeUTF(name);
			dos.writeInt(attLevel);
			int upExp = AttendantFixService.getUpExp(qulity,attLevel);
			if(upExp>0){
                dos.writeInt(upExp);
            }else{
            	if(attLevel>=10){
            		dos.writeInt(-1);
            	}else{
            		dos.writeInt(0);
            	}
            }
			dos.write(sex);
			dos.writeShort(attendantType.image.id);
			dos.write(qulity);
			dos.writeUTF(qulityNames[qulity-1]);
			dos.writeInt(loyal);
			dos.writeByte(maxLoyal);
			dos.writeShort(hp);
			dos.writeShort(maxhp); //maxHp
			dos.writeShort(mp);
			dos.writeShort(maxmp); //maxMp
			dos.writeShort(Math.round(defense)); //护甲
			dos.writeShort(Math.round(spelldefense)); //法防
			dos.writeShort(Math.round(attackpowerdown)); //武器攻击下限
			dos.writeShort(Math.round(attackpowerup)); //武器攻击上限
			dos.writeShort(Math.round(critical * 100)); //物理暴击
			dos.writeShort(Math.round(spellcritical * 100)); //法术暴击
			dos.writeShort(Math.round(spellpower)); //法攻
			dos.writeShort(Math.round(dodge * 100)); //物闪
			dos.writeShort(Math.round(spelldodge * 100)); //法闪
			dos.writeShort(Math.round(anticrit * 100)); //免爆
			dos.writeShort(Math.round(hit * 100)); //物理命中
			dos.writeShort(Math.round(spellhit * 100)); //法术命中
			dos.writeShort(strength); //力
			dos.writeShort(agility); //敏
			dos.writeShort(intellect); //智
			dos.writeShort(stamina); //体
			dos.write(skillSwitchs.length);
			for(int i=0;i<skillSwitchs.length;i++){
				boolean canLight = canActive[i];
				dos.writeByte(canLight ? 1 : 0);
				boolean b = skillSwitchs[i];
				dos.writeByte(b ? 1 : 0);
				dos.writeByte(skills.get(i)==null ? 0 : 1);
				if(skills.get(i)!=null)
					dos.write(skills.get(i).toClientBytes(owner));
			}
			dos.write(getEquipmentsClientBytes());
			int showSkillId = getShowSkillId();
			Skill showSpecialSkill = null;
			if(showSkillId != 0){
				showSpecialSkill = getSpecialSkill(showSkillId);
			}
			//修复随从特殊技能问题
			if(showSkillId != 0 && showSpecialSkill==null){
				specialSkills.clear();
				Skill correctSkill  = ObjectAccessor.getSkill(Skills.getSkillId(showSkillId, Math.round(this.attLevel/2)));
				specialSkills.add(correctSkill);
				showSpecialSkill = correctSkill;
			}
			byte b0=(byte)((specialSkills == null|| showSpecialSkill == null) ? 0 : 1);
			dos.writeByte(b0);
			if(b0!=0)
		        dos.write(showSpecialSkill.toClientBytes(owner));
			int exchangeExp = Server.server.getServiceRegistry().getAttendantFixService().getExchangeExp(this.qulity,this.attLevel);
			dos.writeInt(exchangeExp);
		}catch(Exception e){
			e.printStackTrace();
		}
		return baos.toByteArray();
    }
    
    private byte[] getEquipmentsClientBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			for(GameItem item:equs){
				if(item!=null){
					dos.write(1);
					dos.write(item.toClientBytes());
				}else{
					dos.write(0);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
    
    public static String getEquipLog(GameItem[] items){
    	StringBuilder sb = new StringBuilder();
    	for(GameItem item : items){
    		if(item!=null && item.template!=null){
    			sb.append(LogUtil.getGameItemString(item, 0));
    		}
		}
    	return sb.toString();
    }

	public void dump(StringBuilder out) {
		out.append("IID["+ instanceId + "]");
	}
	
	public String getDesc() {
		return null;
	}

	public String logString() {
		return null;
	}

	public Class<? extends Marshaller> marshallerClass() {
		return AttendantPersitence.class;
	}

	public Class<? extends Serializer> serializerClass() {
		return AttendantPersitence.class;
	}
	
	public Attendant clone(){
		Attendant atd = new Attendant(id, instanceId, owner, null);
		atd.name = name;
		atd.attLevel = attLevel;
		atd.sex = sex;
		atd.attendantType = attendantType;
		atd.qulity = qulity;
		atd.loyal = loyal;
		atd.hp = hp;
		atd.maxhp = maxhp;
		atd.mp = mp;
		atd.maxmp = maxmp;
		atd.defense = defense;
		atd.spelldefense = spelldefense;
		atd.attackpowerdown = attackpowerdown;
		atd.attackpowerup = attackpowerup;
		atd.critical = critical;
		atd.spellcritical = spellcritical;
		atd.spellpower = spellpower;
		atd.dodge = dodge;
		atd.spelldodge = spelldodge;
		atd.anticrit  = anticrit;
		atd.hit = hit;
		atd.spellhit = spellhit;
		atd.strength = strength;
		atd.agility = agility;
		atd.intellect = intellect;
		atd.stamina = stamina;
		atd.skillSwitchs = skillSwitchs;
		atd.equs = equs.clone();
		atd.specialSkills=specialSkills;
		atd.skills.addAll(skills);
		return atd;
	}
	
	@Override
	public boolean isAlive(){
		return true;
	}
	
	public Unit[] getAidUnits(GameObject ref, int dist) {
		List<Unit> list = new ArrayList<Unit>();
		Player owner = this.owner;
		if(owner!=null){
			if (owner.party == null) {
				if (this.map.map == ref.map.map && this.inRange(ref, dist))
					list.add(this);
				list.add(owner);
			} else {
				List<Player> ps = owner.party.getPlayerInRange(this.map.map, dist, ref.x, ref.y);
				for (Player p : ps) {
				    if (p != ref && p.isAlive() && canAid(p)) {
				        list.add(p);
				    }
				}
			}
			Unit[] ret = new Unit[list.size()];
			list.toArray(ret);
			return ret;
		}
		return null;
	}
	
}
