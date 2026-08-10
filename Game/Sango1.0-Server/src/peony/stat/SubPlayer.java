package peony.stat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 专用于每周战功结算的临时表对象。
 * @author lighthu
 */
@Entity
@Table(name = "subplayer")
public class SubPlayer {
	@Id
	@Column(name="id")
	public int id;
	@Column(name="weekcredit")
	public int weekCredit;
	@Column(name="faction",nullable=false)
	public int faction;
	@Column(name="rank",nullable=false)
	public int rank = -1;
}
