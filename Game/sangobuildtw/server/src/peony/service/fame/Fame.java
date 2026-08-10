package peony.service.fame;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;
import peony.game.ChatOptions;
import peony.game.Equipments;
import peony.game.HorseBag;
import peony.game.PropertyPool;
import peony.game.Titles;
import peony.game.TransactionBag;

@Entity
@Table(name = "fame")
@AccessType("field")
public class Fame {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
    public int id;
	@Column(name = "playerid")
	public int playerId;
	@Column(name = "name")
	public String name;
	@Column(name = "mp")
	public int mp;
	@Column(name = "hp")
	public int hp;
	@Column(name = "faction")
	public int faction;
	@Column(name = "sex")
	public int sex;
	@Column(name = "level")
	public int level;
	@Column(name = "clazz")
	public int clazz;
	@Column(name = "x")
	public int x;
	@Column(name = "y")
	public int y;
	@Column(name = "guildname")
	public String guildName;
	@Column(name = "matename")
	public String mateName;
	@Column(name = "rank")
	public int rank;
	@Column(name = "credit")
	public int credit;
	@Column(name = "weekcredit")
	public int weekCredit;
	@Column(name = "activepower")
	public int activePower;
	@Column(name="equipments")
	@Type(type="peony.service.fame.EquipmentsUserType")
	public Equipments equipments;
	@Type(type = "peony.service.fame.ChatOptionsUserType")
	@Column(name = "chatoptions")
	public ChatOptions chatOptions;
	@Column(name = "titles")
	@Type(type = "peony.service.fame.TitlesUserType")
	public Titles titles;
	@Column(name = "horseinstanceid")
	public int horseinstanceid;
	@Column(name = "horsebag")
	@Type(type = "peony.service.fame.HorseBagUserType")
	public HorseBag horseBag;
	@Column(name = "bag")
	@Type(type = "peony.service.fame.TransactionBagUseType")
	public TransactionBag bag;
	@Column(name = "pool")
	@Type(type = "peony.game.PropertyPoolType")
	public PropertyPool pool;
	public Fame(){
	
	}
}
