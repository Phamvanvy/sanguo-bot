package peony.game;

/**
 * 需要加入游戏循环的工作接口。
 * @author lighthu
 */
public interface Updatable {
	/**
	 * 每游戏循环调用。
	 * @param diff 本次循环和上次循环的时间差
	 * @return 如果本对象在本次循环后失效，返回true。
	 */
	public boolean update(int diff);
	
	/**
	 * 从保存的数据中恢复BUFF数据。
	 */
	public void load(byte[] bytes);
	
    /**
     * 把BUFF数据保存到byte数组中。
     */
	public byte[] save();
	
	/**
	 * 在Player从Loaded状态到Logined状态时需要使用此接口进行更新。
	 * @param time 	时间偏移，从上次Loaded状态到Logined状态的时间
	 * @return 如果BUFF仍然有效，返回true；否则返回false。
	 */
	public boolean update2(int time);
}
