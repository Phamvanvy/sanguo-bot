package peony.service.stat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import peony.game.Actor;
import peony.game.PropertyPool;

@Entity
@Table(name = "pvpinfo")
public class PvpInfo {
    @Id
	@Column(name="id")
	public int id;
    @Column(name="totaldiecount")
	public int totalDieCount;
    @Column(name="totalkillcount")
	public int totalKillCount;
    @Column(name="yesterdaydiecount")
	public int yesterdayDieCount;
    @Column(name="yesterkillcount")
	public int yesterdayKillCount;
    @Column(name="todaydiecount")
	public int todayDieCount;
    @Column(name="todaykillcount")
	public int todayKillCount;
	@Column(name="faction")
    public int faction;
	@Column(name="pool")
	@Type(type="peony.game.PropertyPoolType")
	public PropertyPool pool = new PropertyPool();
    @Transient
    public Actor actor;
    
    public PvpInfo(){
    	
    }
    
    public PvpInfo(int id,int faction){
    	this.id = id;
    	this.faction = faction;
    }
}
