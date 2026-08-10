package peony.service.account;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;
import peony.game.PropertyPool;

@Entity
@AccessType("field")
@Table(name="firstcharge")
public class FirstCharge {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@Column(name="accountid",nullable=false)
	public int accountId;
	
	@Column(name = "pool")
	@Type(type = "peony.game.PropertyPoolType")
	public PropertyPool pool;
	
	public FirstCharge(){
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public PropertyPool getPool() {
		return pool;
	}

	public void setPool(PropertyPool pool) {
		this.pool = pool;
	}
	
	/** 是否领取了首冲奖励 */
	public boolean hasGetFirstGift(String imoney){
		return pool.getInt(imoney, 0) > 0;
	}
	
	public void charge(String imoney){
		pool.setInt(imoney, pool.getInt(imoney, 0) + 1);
	}
	
}
