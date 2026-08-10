package peony.service.tong.battle;

import peony.game.Point;

public class TongBattleSideDef {
	public static final int TYPE_DEFEND = 1;
	public static final int TYPE_ATTACK1 = 2;
	public static final int TYPE_ATTACK2 = 3;
	
	public int type;
	public Point in,relive,flag;
	public TongBattleDef battleDef;
	
	public TongBattleSideDef(int type,TongBattleDef battleDef){
		this.type = type;
		this.battleDef = battleDef;
	}
}
