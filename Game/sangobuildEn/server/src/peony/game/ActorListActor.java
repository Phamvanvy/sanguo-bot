package peony.game;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "player")
public class ActorListActor {
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(name="id")
	public int id;
	@Column(name="accountid")
	public int accountId;
	@Column(name="name",nullable=false)
	public String name;
	@Column(name="sex",nullable=false)
	public int sex;
	@Column(name="level",nullable=false)
	public int level = -1;
	@Column(name="clazz",nullable=false)
	public int clazz;
	@Column(name="faction", nullable=false)
	public int faction;
	@Column(name="exist",nullable=false)
	public int exist;
	@Column(name="equipments")
	public byte[] equipments;
	@Column(name="mapid",nullable=false)
	public int mapId;
	@Column(name="createtime",nullable=false)
	public Date createTime;
	@Transient
	public int[] equipmentScores = new int[4];
}
