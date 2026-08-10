package peony.game.suite;

import peony.game.buff.Buff;

public class SuiteEffect {
	public int count;
	public Buff buff;
	public int buffId;
	public int buffLevel;
	
	public int type = TYPE_NORMAL;
	
	/** 普通套效 */
	public static int TYPE_NORMAL = 0;
	/** 按照权重值计算套效 */
	public static int TYPE_WEIGHT_CALC = 1;
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public Buff getBuff() {
		return buff;
	}
	public void setBuff(Buff buff) {
		this.buff = buff;
	}
	public int getBuffId() {
		return buffId;
	}
	public void setBuffId(int buffId) {
		this.buffId = buffId;
	}
	public int getBuffLevel() {
		return buffLevel;
	}
	public void setBuffLevel(int buffLevel) {
		this.buffLevel = buffLevel;
	}
}
