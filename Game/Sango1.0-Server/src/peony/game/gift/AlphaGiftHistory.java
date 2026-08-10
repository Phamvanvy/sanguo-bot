package peony.game.gift;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "alphagift")
public class AlphaGiftHistory {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	public int id;
	@Column(name="name",nullable=false)
	public String name;
	@Column(name="password",nullable=false)
	public String password;
	@Column(name="exchanged",nullable=false)
	public boolean exchanged;
}
