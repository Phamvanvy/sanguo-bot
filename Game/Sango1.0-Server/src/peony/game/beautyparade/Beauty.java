package peony.game.beautyparade;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
@Table(name="beauty")
public class Beauty {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@Column(name="playerid",length=11,nullable=false)
	public int playerId;
	
	@Column(name="name",nullable=false)
	public String name;
	
	@Column(name="sex",nullable=false)
	public int	sex;
	
	@Column(name="votes",nullable=false)
	public int votes;
	
	@Column(name="signupdate",nullable=false)
	public Date signUpDate;
	
	@Column(name="slogan",nullable=false)
	public String slogan;
	
	@Column(name="faction",nullable=false)
	public int faction;
	
}
