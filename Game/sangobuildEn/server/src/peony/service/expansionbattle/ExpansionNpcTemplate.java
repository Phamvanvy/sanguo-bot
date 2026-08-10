package peony.service.expansionbattle;

/**
 * 战役NPC模板
 * @author dchen
 */
public class ExpansionNpcTemplate {
	
	public int type;
	public int instanceId;
	public int x;
	public int y;
	
	/** 城门 */
	public static int NPC_DOOR = 1;
	
	/** 箭塔1左 */
	public static int NPC_TOWER1L = 2;
	
	/** 箭塔1右 */
	public static int NPC_TOWER1R = 3;
	
	/** 箭塔2左 */
	public static int NPC_TOWER2L = 4;
	
	/** 箭塔2右 */
	public static int NPC_TOWER2R = 5;
	
	/** 箭塔3左 */
	public static int NPC_TOWER3L = 6;
	
	/** 箭塔3右 */
	public static int NPC_TOWER3R = 7;
	
	/** 军旗 */
	public static int NPC_FLAG = 8;
	
	/** 于吉*/
	public static int NPC_YUJI1 = 9;
	
	/** 于吉*/
	public static int NPC_YUJI2 = 10;
	
	/** 于吉*/
	public static int NPC_YUJI3 = 11;
	
	/** 于吉*/
	public static int NPC_YUJI4 = 12;
	
	/** 于吉*/
	public static int NPC_YUJI5 = 13;
	
	/** 于吉*/
	public static int NPC_YUJI6 = 14;
	
	/** 先锋 */
	public static int NPC_FOEMAN1 = 15;
	
	/** 前军 */
	public static int NPC_FOEMAN2 = 16;
	
	/** 中军 */
	public static int NPC_FOEMAN3 = 17;
	
	/** 后军 */
	public static int NPC_FOEMAN4 = 18;
	
	/** 讨逆兵 */
	public static int NPC_GUARD = 19;
	
	/** 华雄 */
	public static int NPC_HUAXIONG = 20;
	
	/** 吕布 */
	public static int NPC_LVBU = 21;
	
	/** 将军 */
	public static int NPC_GENERAL = 22;
	
	/** 参将 */
	public static int NPC_CANJIANG = 23;
	
	/** 非战时NPC */
	public static int NPC_NOTBATTLE = 50;
	
	public ExpansionNpcTemplate(int type, int instanceId, int x, int y){
		this.type = type;
		this.instanceId = instanceId;
		this.x = x;
		this.y = y;
	}
	
}
