package peony.game.nation;

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
@Table(name="candidate")
public class Candidate {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@Column(name="playerid",nullable=false,length=11)
	public int playerId;
	
	@Column(name="faction",nullable=false,length=11)
	public int faction;
	
	@Column(name="funds",nullable=false,length=11)
	public int funds;
	
	@Column(name="credit",nullable=false,length=11)
	public int credit;
	
	@Column(name="createtime",nullable=false)
	public Date createTime;
	
	@Column(name="votes",nullable=false,length=11)
	public int votes;
	
	@Column(name="previousking",nullable=false,length=11)
	public int previousKing; // 1代表当过国王
	
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
	public int getFaction() {
		return faction;
	}
	public void setFaction(int faction) {
		this.faction = faction;
	}
	public int getFunds() {
		return funds;
	}
	public void setFunds(int funds) {
		this.funds = funds;
	}
	public int getCredit() {
		return credit;
	}
	public void setCredit(int credit) {
		this.credit = credit;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public int getVotes() {
		return votes;
	}
	public void setVotes(int votes) {
		this.votes = votes;
	}
	public int getPreviousKing() {
		return previousKing;
	}
	public void setPreviousKing(int previousKing) {
		this.previousKing = previousKing;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + playerId;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Candidate other = (Candidate) obj;
		if (playerId != other.playerId)
			return false;
		return true;
	}
	
}
