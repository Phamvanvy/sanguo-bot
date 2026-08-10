package peony.service.ranking;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "ranking")
public class Ranking {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	public int id;
	
	@Column(name="playerid",nullable=false)
	public int playerId;
	
	@Column(name="playername",nullable=false)
	public String playerName;
	
	@Column(name="type",nullable=false)
	public String type;
	
	@Column(name="value",nullable=false)
	public int value;
	
	@Column(name="time",nullable=false)
	public Date time;
	
	@Column(name="faction",nullable=false)
	public int faction;
	
	@Column(name="value2",nullable=false)
	public int value2;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getValue() {
		return value;
	}
	
	public int getValue2(){
		return value2;
	}
	
	public int getFaction(){
		return faction;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public void setValue2(int value2){
		this.value2 = value2;
	}
	
	public void setFaction(int faction){
		this.faction = faction;
	}

}
