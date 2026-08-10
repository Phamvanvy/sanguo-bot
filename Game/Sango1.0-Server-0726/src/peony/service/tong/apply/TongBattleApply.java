package peony.service.tong.apply;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.AccessType;
import peony.game.Server;
import peony.service.tong.Tong;

@Entity
@AccessType("field")
@Table(name="tongbattleapply")
public class TongBattleApply {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@Column(name="mapid")
	public int mapId;
	
	@Column(name="tongid")
	public int tongId;
	
	@Transient
	public Tong tong;
	
	@Column(name="money")
	public int money;
	
	@Column(name="state")
	public int state; // 0,申请    1,占有
	
	@Column(name="tongname")
	public String tongName;
	
	@Column(name="faction")
	public int faction;
	
	@Transient
	public boolean hasBuyTower; // 是否城买了攻城车
	
	@Transient
	public int useAccount; // 攻城车使用次数
	
	public TongBattleApply(){
		
	}
	
	public TongBattleApply(int tongId, int mapId, int faction) {
		this.tongId = tongId;
		this.mapId = mapId;
		this.tong = Server.server.getServiceRegistry().getTongService().getTong(tongId);
		this.tongName = tong.name;
		this.faction = faction;
	}
	
	public void addMoney(int value){
		this.money += value;
	}
	
	public Tong getTong(){
		return Server.server.getServiceRegistry().getTongService().getTong(tongId);
	}
	
}
