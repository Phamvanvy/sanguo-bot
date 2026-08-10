package peony.game.beautyparade;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
@Table(name="voteplayer")
public class VotePlayer {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@Column(name="playerid",length=11,nullable=false)
	public int playerId;
	
	@Column(name="name",nullable=false)
	public String name;
	
	@Column(name="votes",nullable=false)
	public int votes; // 所投票数总和
	
	@Column(name="faction",nullable=false)
	public int faction;
	
}
