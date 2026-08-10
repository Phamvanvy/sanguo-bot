package peony.game.buff;

import java.util.Random;

import peony.game.GameObjectRef;
import peony.game.Unit;

/**
 * 加在人物/怪物身上的增/减益效果。
 * @author lighthu
 */
public interface Buff {
    public static final Random RND = new Random();
    
	/**
	 * 效果ID。同一ID的效果只能加1个。
	 */
	public int getId();
	/**
	 * 实例id
	 */
	public int getInstanceID();
	/**
	 * 名字
	 */
	public String getName();
	/**
	 * BUFF描述
	 */
	public String getDesc();
	/**
	 * 图标ID。-1表示客户端不显示。
	 */
	public int getIconID();
	/**
	 * 取得BUFF到期时间。如果BUFF永不到期，返回-1。
	 */
	public int getEndTime();
	/**
	 * 是否良性BUFF。
	 */
	public boolean isGood();
	/**
	 * 是否允许驱散。
	 */
	public boolean dispelable();
	/**
	 * 死亡后是否保持。
	 */
	public boolean keepOnDie();
	/**
	 * 是否地区BUFF，例如光环。地区BUFF在用户离开地区时或者队友离开地区时需要移除重算。
	 */
	public boolean isAreaBuff();
	/**
	 * 效果是否允许和其他同类效果合并。
	 */
	public boolean isNeedMerge();
	/**
	 * 尝试把一个BUFF合并到已有BUFF中。
	 * @param buff 要合并的新BUFF
	 * @return 如果新的BUFF被合并，返回true，否则返回false。
	 */
	public boolean merge(Buff buff);
	/**
	 * 取得BUFF的原始施法者。如果原始施法者无法被确认，返回null。
	 */
	public GameObjectRef getSource();
	/**
	 * 设置BUFF所有者。在BUFF加到一个对象上时需要设置。
	 */
	public void setOwner(GameObjectRef o);
	/**
	 * 刷新BUFF属性。当BUFF所有者获得或失去一个ParamEnhancer类型的BUFF时调用以重算基本属性。
	 */
	public void resetParams(Unit owner);
}
