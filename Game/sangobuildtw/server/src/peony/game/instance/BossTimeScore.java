package peony.game.instance;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

import peony.game.Actor;

@Entity
@Table(name = "bosstimescore")
@AccessType("field")
public class BossTimeScore {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@Column(name="bossid")
	public int bossId;
	
	@Column(name="members")
	@Type (type="peony.game.instance.MemberUserType")
	public Members members = new Members(); //成员Id
	
	@Column(name="time")
	public int time; //打死boss的时间，以毫秒为单位
	
	@Column(name="date")
	public Date date;
	
	@Transient
	public List<Actor> actors;
}
