package peony.auction;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

import peony.game.GameItem;

@Entity
@AccessType("field")
@Table(name="auction")
public class Auction implements Serializable {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;//主键
	
	@Column(name="playerid",length=11,nullable=false)
    private int playerId = 0;//拍卖玩家的ID
	
	@Column(name="createtime",nullable=false)
    private Date createTime;//建立拍卖信息的时间
	
	@Column(name="startprice",nullable=false,length=11)
    private int startPrice = 0;//初始价格
	
	@Column(name="currentprice",nullable=false)
    private int currentPrice = 0;//当前价格
	
	@Column(name="endprice",nullable=false,length=11)
    private int endPrice = 0;//一口价
	
	@Column(name="item",nullable=false)
	@Type(type="peony.auction.GameItemUserType")
	private GameItem item;//物品信息
	
	@Column(name="count",nullable=false,length=11)
    private int count = 0;//物品数量
	
	@Column(name="type",nullable=false,length=11)
    private int type = 0;//类型 0为武器,1为防具,2为饰品,3为普通物品类型 
	
	@Column(name="lastplayerid",nullable=false,length=11)
    private int lastPlayerId = -1;//最后出价的玩家ID
	
	@Column(name="playername",nullable=false,length=20)
    private String playerName;//玩家的名字
	
	@Column(name="quality",nullable=false,length=11)
    private int quality = 0;//物品的品质
	
	@Column(name="level",nullable=false,length=11)
    private int level = 0;//物品的等级
	
	@Column(name="name",nullable=false,length=255)
    private String name;//物品名字
	
	@Column(name="validtime",nullable=false)
    private Date validTime;//到期时间
	
	@Column(name="faction",nullable=false)
	private int faction;//派别
	
	public int getFaction() {
		return faction;
	}
	public void setFaction(int faction) {
		this.faction = faction;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPlayerId() {
		return playerId;
	}
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public int getStartPrice() {
		return startPrice;
	}
	public void setStartPrice(int startPrice) {
		this.startPrice = startPrice;
	}
	public int getCurrentPrice() {
		return currentPrice;
	}
	public void setCurrentPrice(int currentPrice) {
		this.currentPrice = currentPrice;
	}
	public int getEndPrice() {
		return endPrice;
	}
	public void setEndPrice(int endPrice) {
		this.endPrice = endPrice;
	}
	public GameItem getItem() {
		return item;
	}
	public void setItem(GameItem item) {
		this.item = item;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getLastPlayerId() {
		return lastPlayerId;
	}
	public void setLastPlayerId(int lastPlayerId) {
		this.lastPlayerId = lastPlayerId;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public int getQuality() {
		return quality;
	}
	public void setQuality(int quality) {
		this.quality = quality;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getValidTime() {
		return validTime;
	}
	public void setValidTime(Date validTime) {
		this.validTime = validTime;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Auction other = (Auction) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
