package pip.gm.fw;

/**
 * 游戏相关的信息,如场景id和场景名称的对应,物品id和物品名称的对应,技能,等待.
 * 需要在加载时读取,并且可以通过UDP更新?.
 * @author Administrator
 *
 */
public class GameInfo {
	public String getSceneName(int sceneId) {
		return "未知";
	}
	public boolean isBlurMap(int sceneId) {
		return false;
	}
	public String getItemName(int itemId) {
		return "未知";
	}

}
