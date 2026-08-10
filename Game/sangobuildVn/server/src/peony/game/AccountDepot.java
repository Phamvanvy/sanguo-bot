package peony.game;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "accountdepot")
@AccessType("field")
public class AccountDepot {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@Column(name="accountid",length=11,nullable=false)
	public int accountId;
	
	@Type(type = "peony.depot.AccountDepotUserType")
	@Column(name = "depot")
	public TransactionBag depot;
	
}
