package peony.game.clientbbs;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "clientbbs")
public class ClientBbs {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "id")
	public int id;
	@Column(name = "minlevel")
	public int minLevel;
	@Column(name = "maxlevel")
	public int maxLevel;
	@Column(name = "explaination")
	public String explaination;
	@Column(name = "active")
	public String active;
	@Column(name = "publishtime")
	public Date pulishTime;
	@Column(name = "obsoletetime")
	public Date obsoleteTime;
	@Column(name = "enable")
	public boolean enable;
	@Column(name = "isschedule")
//	@Transient
	public boolean isschedule;

	public ClientBbs() {

	}

	public ClientBbs(int minLevel, int maxLevel) {
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
	}

	public boolean in(int level) {
		if (level >= minLevel && level < maxLevel)
			return true;
		return false;
	}
}
