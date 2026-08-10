package peony.service.tong;

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
	
	public static final String PROPERTY_TONG_QUEST = "QUEST"; // 用于军团任务
	public static final String PROPERTY_TONGBATTLE_WIN = "WIN"; // 团战胜利次数
	public static final String PROPERTY_TONGBATTLE_FAIL = "FAIL"; // 团战失败次数
	public static int MIN_QUEST_MONEY = 50000;
	public static int QUEST_MONEY = 1000000;
	
	public synchronized int addMoney(int value){
		if(value < 0)
			throw new IllegalArgumentException();
		this.money += value;
		return this.money;
	}
	
	public synchronized int decMoney(int value) throws NoEnoughValueException{
		if(value < 0)
			throw new IllegalArgumentException();
		if(this.money < value)
			throw new NoEnoughValueException();
		this.money -= value;
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
	
	public void maintainAll(){
		for (TongSkill skill : skills.skills.values()) {
			if(skill.level>0)
				maintain(skill);
		}
	}
	
	protected void maintain(TongSkill skill) {
		try {
			decMoney(skill.getMaintainMoney(skill.level));
		} catch (NoEnoughValueException e) {
			skill.level -= 1;
			skill.maintainDay = Time.day;
//			skill.maintainDay = 0; //方便测试
//			skill.upgradeDay = 0; //方便测试
			Server.server.getServiceRegistry().getChatService()
					.sendGuildSystemMessage(skill.name+"因没有维护下降至"+skill.level+"Cấp", id);
		}
		skill.maintainDay = Time.day;
//		skill.maintainDay = 0; //方便测试
//		skill.upgradeDay = 0; //方便测试
	}
}
