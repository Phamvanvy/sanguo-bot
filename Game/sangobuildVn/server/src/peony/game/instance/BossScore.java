package peony.game.instance;

import java.util.ArrayList;
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
@Table(name = "bossscore")
@AccessType("field")
public class BossScore {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@Column(name = "bossid")
	public int bossId; 
	
	
	@Column(name="score")
	public int score; //名次，从1开始
	
	@Column(name="members")
	@Type (type="peony.game.instance.MemberUserType")
	public Members members = new Members();
	
	@Column(name="date")
	public Date date;
	
	public BossScore() {
		
	}
	
	public BossScore(int bossId) {
		super();
		this.bossId = bossId;
	}

	@Transient
	public List<Actor> actors = new ArrayList<Actor>();
	
}
