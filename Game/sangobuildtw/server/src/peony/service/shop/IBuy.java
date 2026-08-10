package peony.service.shop;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;

@Entity
@Table(name = "ibuy")
@AccessType("field")
public class IBuy {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
    public int id;
	@Column(name = "accountid")
    public int accountid;
	@Column(name = "playerid")
    public int playerid;
	@Column(name = "itemname")
    public String itemname;
	@Column(name = "itemid")
    public int itemid;
	@Column(name = "type")
    public int type;//物品种类
	@Column(name = "imoney")
    public int imoney;//消费实际i币（单位i）
	@Column(name = "buytime")
    public Date buytime;
	@Column(name = "otherplayerid")
    public int otherplayerid;//赠与角色id（-1为自用）
	@Column(name = "otherplayername")
    public String otherplayername;//赠与角色名（空为自用）
	@Column(name = "count")
    public int count;//购买数量
	@Column(name = "giftflag")
    public int giftflag;//是否用券
	@Column(name = "level")
	public Integer level;
	
	public IBuy(int accountId,int playerId,int itemId,String itemName,int count,int type,int imoney,int level){
		this.accountid = accountId;
		this.playerid = playerId;
		this.itemid = itemId;
		this.itemname = itemName;
		this.type = type;
		this.imoney = imoney;
		this.count = count;
		this.buytime = new Date();
		this.otherplayerid = -1;
		this.otherplayername = "";
		this.giftflag = 0;
		this.level = level;
	}
	
	public IBuy(){
		
	}
}
