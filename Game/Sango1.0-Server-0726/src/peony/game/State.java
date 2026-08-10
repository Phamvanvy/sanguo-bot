package peony.game;

/**
 * 状态接口，可用来表示任何游戏内的对象所处的状态
 * @author Jeffrey
 *
 */
public interface State {
	//当进入此状态时需要调用的接口
	public void enter(Player player);
	//当退出此状态时需要调用的接口
	public void exit(Player player);
	
	public void update(Player player);
}
