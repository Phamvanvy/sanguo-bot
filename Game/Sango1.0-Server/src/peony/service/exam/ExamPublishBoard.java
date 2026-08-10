package peony.service.exam;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.AccessType;

@Entity
@Table(name = "exampublishboard")
@AccessType("field")
public class ExamPublishBoard {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	public int id;
	
	@Column(name = "playerid", nullable = false)
	public int playerId;
	
	@Column(name = "passcount", nullable = false)
	public int passCount;
	
	@Column(name = "totaltime", nullable = false)
	public int totalTime;
	
	@Column(name = "ranking", nullable = false)
	public int ranking;
	
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
	public int getPassCount() {
		return passCount;
	}
	public void setPassCount(int passCount) {
		this.passCount = passCount;
	}
	public int getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(int totalTime) {
		this.totalTime = totalTime;
	}
	public int getRanking() {
		return ranking;
	}
	public void setRanking(int ranking) {
		this.ranking = ranking;
	}
	
	public ExamPublishBoard duplicate(){
		ExamPublishBoard board = new ExamPublishBoard();
		board.setId(getId());
		board.setPassCount(passCount);
		board.setPlayerId(playerId);
		board.setRanking(ranking);
		board.setTotalTime(totalTime);
		return board;
	}
	
}
