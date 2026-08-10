package peony.game.nation;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

/***
 * 国家之间的关系，结盟，宣战等等
 * @author Jeffrey
 *
 */
@Entity
@Table(name = "nationrel")
@AccessType("field")
public class NationRel {
	
	public static final int TYPE_PEACE = 0; //和平状态
	public static final int TYPE_ALIGNMENT_REQUEST = 1; //发起结盟申请，并没有真正进行结盟
	public static final int TYPE_ALIGNMENT = 2; //结盟
	public static final int TYPE_WAR_REQUEST = 3; //发起宣战申请，并没有真正宣战
	public static final int TYPE_WAR_PREPARE = 4; //战争准备
	public static final int TYPE_ALIGNMENT_REQUESTED = 5; //收到结盟请求
	public static final int TYPE_WAR_REQUESTED = 6; //被宣战
	public static final int TYPE_ALIGNMENTED = 7; //被结盟
	public static final int TYPE_WARED_PREPARE = 8; //被动战争准备
	public static final int TYPE_WIN = 9; //胜利
	public static final int TYPE_FAIL = 10; //失败
	public static final int TYPE_ATTACK = 11; //进攻
	public static final int TYPE_DEFENSE = 12; //防守
	public static final int TYPE_SNEAK_REQUEST = 13; //发起反击战(现在实现中不会有这种状态)
	public static final int TYPE_SNEAK_REQUESTED = 14; //被发起反击战(现在实现中不会有这种状态)
	public static final int TYPE_SNEAK = 15; //反击
	public static final int TYPE_SNEAKED = 16; //被反击
	
	@Id
	@Column(name="id")
	public int id;
	@Column(name="source")
	public int sourceFaction;
	@Column(name="dest")
	public int destFaction;
	@Column(name="type")
	public int type;
	@Column(name="createtime")
	public Date createTime;
	@Column(name="endtime")
	public Date endTime;
	
	@Transient
	public int money; //如果类型是胜利或者失败，这里记录的是失败国被掠夺的国库的金钱数量
	
	public NationRel(){
		
	}
	
	public NationRel(int id,int sourceFaction,int destFaction,int type,Date createTime,Date endTime){
		this.id = id;
		this.sourceFaction = sourceFaction;
		this.destFaction = destFaction;
		this.type = type;
		this.createTime = createTime;
		this.endTime = endTime;
	}
}
