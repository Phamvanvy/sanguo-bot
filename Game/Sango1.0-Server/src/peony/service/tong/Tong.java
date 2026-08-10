package peony.service.tong;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import peony.game.NoEnoughValueException;
import peony.game.PropertyPool;
import peony.game.Server;
import peony.game.Time;

@Entity
@Table(name = "tong")
public class Tong {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	public int id;
	
	/**
	 * 军团名称
	 */
	@Column(name="name",nullable=false)
	public String name;
	
	/**
	 * 创建时间
	 */
	@Column(name="createtime",nullable=false)
	public java.util.Date createTime;
	
	/**
	 * 创建人（不一定是都督）
	 */
	@Column(name="owner",nullable=false)
	public int owner;
	
	/**
	 * 军团公告（口号）
	 */
	@Column(name="slogan",nullable=false)
	public String slogan;
	
	/**
	 * 军团级别
	 */
	@Column(name="level",nullable=false)
	public int level;
	
	/**
	 * 军团荣誉
	 */
	@Column(name="honor",nullable=false)
	public int honor;
	
	/**
	 * 军团资金
	 */
	@Column(name="money",nullable=false)
	public int money;
	
	/**
	 * 军团元宝
	 */
	@Column(name="imoney",nullable=false)
	public int imoney;
	
	/**
	 * 所有军团成员的列表，在军团载入时全部载入。
	 */
	@Transient
	public List<TongMember> members;
	
	@Column(name="pool")
	@Type(type = "peony.game.PropertyPoolType")
	public PropertyPool pool = new PropertyPool();
	
	@Column(name="skills")
	@Type(type="peony.service.tong.TongSkillsUserType")
	public TongSkills skills = new TongSkills();
	
	@Column(name="tax")
	public float taxRate;
	
	
	/**
	 * 军团贡献度
	 */
	@Column(name="contribute",nullable=false)
	public int contribute;
	
	/**
	 * 是否开启自动接收新人
	 * 0:关闭   1：开启
	 */
	@Column(name="autoaccept",nullable=false)
	public int autoaccept;
	
	/**
	 * 军团人数
	 */
	@Column(name="peoplenum",nullable=false)
	public int peoplenum;
	
	/**
	 * 军团科技是否维护
	 * 0 ：未维护    1：已维护
	 */
	@Column(name="ismaintain",nullable=false)
	public int ismaintain;
	
	@Transient
	public boolean modify = false;
	
	public static final String PROPERTY_TONG_QUEST = "QUEST"; // 用于军团任务
	public static final String PROPERTY_TONGBATTLE_WIN = "WIN"; // 团战胜利次数
	public static final String PROPERTY_TONGBATTLE_FAIL = "FAIL"; // 团战失败次数
	public static int MIN_QUEST_MONEY = 50000;
	public static int QUEST_MONEY = 1000000;
	public static int AUTOACCEPT_OPEN = 1;//自动接收新人开启
	public static int AUTOACCEPT_CLOSE = 0;//自动接收新人关闭
	
	public synchronized int addMoney(int value){
		if(value < 0)
			throw new IllegalArgumentException();
		this.money += value;
		this.modify = true;
		return this.money;
	}
	
	public synchronized int decMoney(int value) throws NoEnoughValueException{
		if(value < 0)
			throw new IllegalArgumentException();
		if(this.money < value)
			throw new NoEnoughValueException();
		this.money -= value;
		this.modify = true;
		return this.money;
	}
	
	public String getChairmanName(){
		for(TongMember member : members){
			if(member.duty==TongService.CHAIRMAN)
				return member.actor.name;
		}
		return null;
	}
	
	public void update(){
		for (TongSkill skill : skills.skills.values()) {
			if (skill.level >0 && skill.maintainDay != Time.day) { // 每日需要扣除维护费，如果维护费用不够，那么降级
				synchronized (this) {
					maintain(skill);
				}
			}
		}
	}
	
	/**
	 * 维护所有军团技能
	 */
	public void maintainAll(){
		for (TongSkill skill : skills.skills.values()) {
			if(skill.level>0)
				maintain(skill);
		}
	}
	
	/**
	 * 维护军团技能
	 * @param skill
	 */
	protected void maintain(TongSkill skill) {
		try {
			decMoney(skill.getMaintainContribute(skill.level));
		} catch (NoEnoughValueException e) {
			skill.level -= 1;
			skill.maintainDay = Time.day;
//			skill.maintainDay = 0; //方便测试
//			skill.upgradeDay = 0; //方便测试
			Server.server.getServiceRegistry().getChatService()
					.sendGuildSystemMessage(MessageFormat.format(peony.Messages.STRING_01758, skill.name, skill.level), id);
		}
		skill.maintainDay = Time.day;
		this.modify = true;
//		skill.maintainDay = 0; //方便测试
//		skill.upgradeDay = 0; //方便测试
	}
	
	public int getTongFaction(){
		Iterator<TongMember> it = members.iterator();
		while(it.hasNext()){
			TongMember mem = it.next();
			if(mem!=null && mem.actor!=null)
				return mem.actor.faction;
		}
		return -1;
	}
	
}
