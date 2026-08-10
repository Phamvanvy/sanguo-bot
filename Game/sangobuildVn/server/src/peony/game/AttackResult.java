package peony.game;

public class AttackResult {
	
	public static final byte TYPE_HIT = 1;  //命中
	public static final byte TPPE_MISS = 2;  //未命中
	public static final byte TYPE_DODGE = 3; //闪避
	
	protected GameObject source;
	protected Unit target;
	protected Attack attack;
	
	protected byte type;
	protected int damage;
	
	public AttackResult(GameObject source,Unit target,Attack attack,byte type,int damage){
		this.source = source;
		this.target = target;
		this.attack = attack;
		this.type = type;
		this.damage = damage;
	}

	public GameObject getSource() {
		return source;
	}

	public Unit getTarget() {
		return target;
	}

	public Attack getAttack() {
		return attack;
	}

	public byte getType() {
		return type;
	}

	public int getDamage() {
		return damage;
	}
	
	public int getTargetId(){
		return target==null?-1:target.id;
	}
	

}
