package peony.game.nation;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;

@Entity
@Table(name = "forbid")
@AccessType("field")
public class Forbid {
	@Id
	@Column(name="id")
	public int id; // ԴID
	@Column(name="targetid")
	public int targetId;
	@Column(name="endtime")
	public Date endTime;
	@Column(name="createtime")
	public Date createTime;
	
	public Forbid(){
		
	}
	
	public Forbid(int id,int targetId,Date endTime,Date createTime){
		this.id = id;
		this.targetId = targetId;
		this.endTime = endTime;
		this.createTime = createTime;
	}
}
