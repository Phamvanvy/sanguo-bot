package peony.service.account;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.AccessType;

/**
 * °ó¶¨Ôª±¦
 * @author dchen
 */
@Entity
@AccessType("field")
@Table(name="bindimoney")
public class BindImoney {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@Column(name="accountid",nullable=false)
	public int accountId;
	
	@Column(name="imoney",nullable=false)
	public long imoney;
	
	public BindImoney(){
		
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

	public long getImoney() {
		return imoney;
	}

	public void setImoney(long imoney) {
		this.imoney = imoney;
	}
	
}
