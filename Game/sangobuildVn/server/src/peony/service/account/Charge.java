package peony.service.account;

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
@Table(name="charge")
public class Charge {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@Column(name="accountid",nullable=false)
	public int accountId;
	
	@Column(name="money",nullable=false)
	public int money;
	
	@Column(name="chargetime",nullable=false)
	public Date chargeTime;
	
	public Charge(){
		
	}
	
	public Charge(int accountId, int money, Date chargeTime){
		this.accountId = accountId;
		this.money = money;
		this.chargeTime = chargeTime;
	}
	
}
