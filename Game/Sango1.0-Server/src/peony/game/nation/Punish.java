package peony.game.nation;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
@Entity
@Table(name = "punish")
@AccessType("field")
public class Punish {
	@Id
	@Column(name="id")
	public int id; //ԴID
	@Column(name="targetid")
	public int targetId;
	@Column(name="money")
	public int money;
	@Column(name="createtime")
	public Date createTime;

	public Punish(){
		
	}
	
	public Punish(int id,int targetId,int money,Date createTime){
		this.id = id;
		this.targetId = targetId;
		this.money = money;
		this.createTime = createTime;
	}
}
