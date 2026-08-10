package peony.game.gift;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "gift")
public class GiftHistory {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	public int id;
	@Column(name="playerid",nullable=false)
	public int playerId;
	@Column(name="groupid",nullable=false)
	public int groupId;
	@Column(name="lasttime",nullable=false)
	public Date lastTime;
	/**
	 * 总共领取的次数
	 */
	@Column(name="alltimes",nullable=false)
	public int allTimes;
	/**
	 * 周期内领取的次数
	 */
	@Column(name="repeattimes",nullable=false)
	public int repeatTimes;
	/**
	 * 每个周期开始时间
	 */
	@Column(name="endtime",nullable=false)
	public Date startTime;
}
