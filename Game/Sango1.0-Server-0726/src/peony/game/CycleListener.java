package peony.game;

/**
 * 需要加入游戏循环的工作接口。
 * @author lighthu
 */
public interface CycleListener {
	/**
	 * 每游戏循环调用。
	 * @param diff 本次循环和上次循环的时间差
	 * @return 如果本对象在本次循环后失效，返回true。
	 */
	public boolean update(int diff);
}
