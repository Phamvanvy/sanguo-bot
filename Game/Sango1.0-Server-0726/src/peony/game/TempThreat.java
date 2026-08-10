package peony.game;

/**
 * 记录给某个Unit加上的临时威胁值，到时间需要扣除。
 * @author lighthu
 */
public class TempThreat {
    /**
     * 威胁到的目标。
     */
    public GameObjectRef target;
    /**
     * 威胁值。
     */
    public float value;
    /**
     * 到期时间。
     */
    public int endTime;
}
