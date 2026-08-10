package peony.service.tong;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import peony.game.Actor;

@Entity
@Table(name = "tongmember")
public class TongMember {
	/**
	 * 玩家ID
	 */
    @Id
	@Column(name="id")
    public int id;
    
    /**
     * 军团ID
     */
    @Column(name="tongid",nullable=false)
    public int tongID;
    
    /**
     * 军团内职务
     */
    @Column(name="duty",nullable=false)
    public int duty;

    /**
     * 军团内头衔
     */
    @Column(name="title",nullable=false)
    public String title;

    /**
     * 军功，标志此玩家在军团内的累计贡献
     */
    @Column(name="honor",nullable=false)
    public int honor;

    /**
     * 金钱，用于消费军团服务
     */
    @Column(name="money",nullable=false)
    public int money;
    
    /**
     * 是否禁言。
     */
    @Column(name="forbid",nullable=false)
    public boolean forbid;
    
    /**
     * 对应的玩家对象。此字段应该在载入时设置。
     */
    @Transient
    public Actor actor;
    
    @Column(name="battletag")
    public int battleTag; //城战标记
}
