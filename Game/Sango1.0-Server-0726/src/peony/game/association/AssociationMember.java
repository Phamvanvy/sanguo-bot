package peony.game.association;

import peony.game.Actor;

/**
 * 结义联盟成员
 * @author dchen
 */
public class AssociationMember {

	/** 职务 */
	public int duty;
	
	/** 角色信息 */
	public Actor actor;
	
	/** 成员ID */
	public int playerId;
	
	/** 状态 */
	public int state;
	
	/** 等待状态 */
	public static int STAT_WAIT = 0;
	
	/** 加入状态 */
	public static int STAT_WORK = 1;
	
	/** 职位_盟主 */
	public static int DUTY_LEADER = 1;
	
	/** 职位_普通成员 */
	public static int DUTY_MEM = 0;
	
	/** 接受邀请时间 */
	public long inviteTime;
	
	public AssociationMember(int duty, int playerId, int state){
		if(state==STAT_WAIT)
			inviteTime = System.currentTimeMillis();
		this.duty = duty;
		this.playerId = playerId;
		this.state = state;
	}
	
}
