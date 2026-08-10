package peony.game;


public interface VMapManager {
	/**
	 * 如果可以进入返回新的VMap,否则返回null,check表示是否检查过期情况，用在副本中（例子：登录需要从过期的副本移出来)
	 */
	public VMap addToMap(Player player,int mapId,int x,int y,boolean check) throws VMapException;
	/**
	 * 返回指定地图Id，对于player的地图，create表示如果不存在是否创建一个
	 * @param player
	 * @return
	 */
//	public VMap getVMap(Player player,int mapId,boolean create);
	public void removeFromMap(Player player);
	public void update(int diff);
	
	/**
	 * 处理地图数据变化，尽可能地更新已有对象的属性。
	 */
	public void mapChanged(GameMapDefinition mapDef);
	
	public DieCallback dieCallback();
	
	public CreatureDieCallback creatureDieCallback();
	
	public MoveCallback moveCallback();
	
	public void outPrison(Player p);
}
