package peony.service.quest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import peony.game.Creature;
import peony.game.GameObjectRef;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.nation.Nation;

/***
 * 个人押镖,每次个人都新建一个此类的实例,记录所有此次押镖镖车的相关信息
 * @author Jeffrey
 *
 */
public class PlayerConvoy {
	//押镖的个人
	public int playerId;	//镖车所有人的ID
	public int faction;		//所属阵营
	public int convoyType;	//镖车类型
	public int convoyLevel;	//镖车品质
	public int isVipDouble;	//是否VIP双倍奖励(0-不是，1-是)
	
	public int escortDieCount;	//镖车修复次数
	public int escortPointIndex;//当前镖车的位置
	
	private String name;	//镖车名称
	
	public PlayerConvoyDef def;
	
	////押镖的所有人的ref
	//public Set<GameObjectRef> sourceRefs = new HashSet<GameObjectRef>();
	
	//劫镖的所有人的ref
	public Set<GameObjectRef> destRefs = new HashSet<GameObjectRef>();

	//押镖的起始时间以及终止时间
	public int beginTime, endTime;
	
	public Creature npc;
	
	public PlayerConvoy(Player player, PlayerConvoyDef def, int beginTime, 
			int convoyLevel, int convoyType, int dieCount, int pointIndex, int isVipDouble){
		this.playerId = player.id;
		this.faction = player.faction;
		this.def = def;
		this.beginTime = beginTime;
		this.convoyLevel = convoyLevel;
		this.convoyType = convoyType;
		this.escortDieCount = dieCount;
		this.escortPointIndex = pointIndex;
		this.isVipDouble = isVipDouble;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
}
