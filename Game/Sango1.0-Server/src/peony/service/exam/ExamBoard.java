package peony.service.exam;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.AccessType;
import peony.game.Time;

@Entity
@Table(name = "examboard")
@AccessType("field")
public class ExamBoard {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	public int id;
	
	@Column(name = "playerid", nullable = false)
	public int playerId;
	
	@Column(name = "passcount", nullable = false)
	public int passCount;
	
	@Column(name = "totaltime", nullable = false)
	public int totalTime; //µ¥Î»100ms
	
	@Column(name = "todaycount", nullable = false)
	public int todayCount;
	
	@Column(name = "recordday", nullable = false)
	public int recordDay;
	
	@Column(name = "examtype", nullable = false)
	public int examType;
	
	public void freshDayData(){
		if(Time.day!=recordDay){
			recordDay = Time.day;
			todayCount = 0;
		}
	}
	
	public int getExamType() {
		return examType;
	}

	public void setExamType(int examType) {
		this.examType = examType;
	}

	public int getTodayCount() {
		return todayCount;
	}

	public void setTodayCount(int todayCount) {
		this.todayCount = todayCount;
	}

	public int getRecordDay() {
		return recordDay;
	}

	public void setRecordDay(int recordDay) {
		this.recordDay = recordDay;
	}

	public ExamBoard(){
		
	}

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
	
	public void resetData(){
		setPassCount(0);
		setTodayCount(0);
		setTotalTime(0);
	}
	
	public ExamBoard duplicate(){
		ExamBoard b = new ExamBoard();
		b.setId(getId());
		b.setExamType(examType);
		b.setPassCount(passCount);
		b.setPlayerId(playerId);
		b.setRecordDay(recordDay);
		b.setTodayCount(todayCount);
		b.setTotalTime(totalTime);
		return b;
	}
	
}
