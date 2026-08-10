package peony.service.tong;

import peony.game.Actor;

/**
 * 加入军团的邀请。如果一个军团管理者邀请另外一个玩家加入军团，则会生成一个新的邀请。
 * @author lighthu
 */
public class TongInvitation {
	/**
	 * 邀请ID
	 */
	public int id;
	
	/**
	 * 邀请人
	 */
	public Actor source;
	
	/**
	 * 邀请目标
	 */
	public Actor target;
	
	/**
	 * 邀请进入的军团
	 */
	public Tong tong;
}
