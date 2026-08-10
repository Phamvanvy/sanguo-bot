package peony.game;

public interface ItemEffect {
	
	public static final int OK = 0;
	public static final int ERROR_TARGET = 1; //无效目标
	public static final int ERROR_NOENOUGHGRID = 2; //包格满
	public static final int ERROR_PROPERTY_FULL = 3; //属性已经满不能再加，比如加蓝，加血
	public static final int ERROR_QUEST = 4; //无法接任务
	public static final int ERROR_STATE = 5; //目标状态错误
	public static final int ERROR_NOITEM = 6;
	public static final int ERROR_ITEM = 7;
	public static final int ERROR_INUSESTATE = 8; //还有物品没有使用
	public static final int ERROR_BOOKSKILL = 9;
	public static final int ERROR_TITLE = 10; //已经有Title
	public static final int ERROR_NOHORSE = 11; //当前没有骑马
	public static final int ERROR_HORSE_FULL = 12; //马饱的
	
	public void use(Unit source,GameItem item,Unit target,PlayerTransaction tx) throws UseItemException;
	public boolean isAsync();
	
}
