package peony.game.admin;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "gmrequest")
public class GMRequest {
	
	public static final int STATE_COMMITED = 0;
	public static final int STATE_RESOLVED = 1;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	public int id;
	@Column(name="playerid",nullable=false)
	public int playerId;
	@Column(name="playername",nullable=false)
	public String playerName;
	@Column(name="cause",nullable=false)
	public String cause;
	@Column(name="state",nullable=false)
	public int state;
	@Column(name="solvent",nullable=false)
	public String solvent;
	@Column(name="createtime",nullable=false)
	public Date createTime;
	@Column(name="type",nullable=false)
	public int type;
	@Column(name="mapid",nullable=false)
	public int mapId;
	@Column(name="x",nullable=false)
	public int x;
	@Column(name="y",nullable=false)
	public int y;
	@Column(name="model",nullable=false)
	public String model;
	
	public GMRequest(){
		
	}
	
	public GMRequest(int type,int playerId,String playerName,String cause,int mapId,int x,int y,String model){
		this.type = type;
		this.playerId = playerId;
		this.playerName = playerName;
		this.cause = cause;
		this.solvent = "";
		this.state = STATE_COMMITED;
		this.createTime = new Date();
		this.mapId = mapId;
		this.model = model;
		this.x = x;
		this.y = y;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
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

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getSolvent() {
		return solvent;
	}

	public void setSolvent(String solvent) {
		this.solvent = solvent;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}


}
