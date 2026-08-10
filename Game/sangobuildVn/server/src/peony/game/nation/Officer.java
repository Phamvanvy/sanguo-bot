package peony.game.nation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

import peony.game.Actor;
import peony.game.PropertyPool;

/**
 * 官职,包括了：国公，丞相，御史大夫，大司马，骠骑将军

 * @author Jeffrey
 *
 */
@Entity
@Table(name = "officer")
@AccessType("field")
public class Officer {
	public static final int KING = 0; //国公
	public static final int LEVEL1 = 1;//丞相
	public static final int LEVEL2 = 2;//御使大夫
	public static final int LEVEL3 = 3;//大司马
	public static final int LEVEL4 = 4;//骠骑将军
	
	public static final int LEVEL_COUNT = 5;
	
	public static final String[] NAME = {"Quốc công","丞相","Ngự sứ đại phu","Đại tư mã","骠骑将军"};
	
	public static final int[] SLOGAN_TIMES = {
		10,0,2,0,0
	};
	
	public static final int[] FORBID_TIMES = {
		10,3,0,3,0
	};
	
	public static final int[] PUNISH_TIMES = {
		5,3,0,0,2
	};
	
	public static final int[] PUNISH_MONEY = {
		3000,1000,0,0,500
	};
	
	public static final int[] FORBID_TIME = {
		3600*1000,1800*1000,0,1800*1000,0
	};
	
	public static final String PROPERTY_SLOGAN_TIMES = "SLOGANT";
	public static final String PROPERTY_FORBID_TIMES = "FORBIDT";
	public static final String PROPERTY_PUNISH_TIMES = "PUNISHT";
	@Id
	@Column(name="id")
	public int id; //角色ID
	@Column(name="level")
	public int level; //官阶
	@Column(name="faction")
	public int faction; //阵营
	
	@Column(name = "pool")
	@Type(type = "peony.game.PropertyPoolType")
	public PropertyPool pool;
	
	@Transient
	public Actor actor;
	
	public Officer(){
		
	}
	
	public Officer(int id,int level,int faction,Actor actor){
		this.id = id;
		this.level = level;
		this.faction = faction;
		pool =new PropertyPool();
		this.actor = actor;
	}

	public static boolean checkLevel(int level){
		return level>KING&&level<=LEVEL4;
	}
	
	public String getName(){
		return NAME[level];
	}
	
	public int getForbidTime(){
		return FORBID_TIME[level];
	}
	
	public int getMaxPunishMoney(){
		return PUNISH_MONEY[level];
	}
	
	public void setSloganTimes(int t){
		pool.setInt(PROPERTY_SLOGAN_TIMES, t);
	}

	public void setForbidTimes(int t){
		pool.setInt(PROPERTY_FORBID_TIMES, t);
	}
	
	public void setPunishTimes(int day,int t){
		pool.setInt(PROPERTY_PUNISH_TIMES+day, t);
	}
	
	public int getSloganTimes(){
		return pool.getInt(PROPERTY_SLOGAN_TIMES,0);
	}
	
	public int getForbidTimes(){
		return pool.getInt(PROPERTY_FORBID_TIMES,0);
	}
	
	public int getPunishTimes(int day){
		return pool.getInt(PROPERTY_PUNISH_TIMES+day,0);
	}
	
	public int getMaxSloganTimes(){
		return SLOGAN_TIMES[level];
	}
	
	public int getMaxForbidTimes(){
		return FORBID_TIMES[level];
	}
	
	public int getMaxPunishTimes(){
		return PUNISH_TIMES[level];
	}
	
}
