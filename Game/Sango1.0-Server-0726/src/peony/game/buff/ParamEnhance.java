package peony.game.buff;

/**
 * 对某一个技能或BUFF属性参数的修正。
 * @author lighthu
 */
public class ParamEnhance {
    /**
     * 修正的总次数。
     */
    public int times;
    /**
     * 增加或减少的绝对值。
     */
    public float value;
    /**
     * 增加或减少的百分比。
     */
    public float percent;
}
