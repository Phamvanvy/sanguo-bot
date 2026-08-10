package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;

public class Title {
	
    /** 其他称号 */
    public static final int TYPE_OTHER = 0;
    /** 官职称号 */
    public static final int TYPE_OFFICIAL = 1;
    /** 国家称号 */
    public static final int TYPE_COUNTRY = 2;
	
	public int id;
	public int level;
	public String name;
	public int type;
	public int faction;
	public int salary;
	public int price;
	public int buffId;
	public int buffLevel;
	public String desc;
	
	protected byte[] clientBytes;

	public Title() {}
	
	public void create(int id, int level,String name, int type, int faction, int salary,
			int price, int buffId, int buffLevel,String desc) {
		this.id = id;
		this.level = level;
		this.name = name;
		this.type = type;
		this.salary = salary;
		this.faction = faction;
		this.price = price;
		this.buffId = buffId;
		this.buffLevel = buffLevel;
		this.desc = desc;
		createClientBytes();
	}

	public Buff newBuff(Unit u) {
		if (buffId == -1)
			return null;
		return BuffUtil.createSuiteBuff(buffId, buffLevel);
	}
	
	
	
	public void createClientBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.writeShort(id);
			dos.writeUTF(name);
			dos.write(type);
			dos.writeInt(salary);
			dos.write(faction);
			dos.writeInt(price);
			dos.writeUTF(desc==null?"":desc);
			dos.write(level);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		clientBytes = baos.toByteArray();
	}
	
	public byte[] toClientBytes(){
		return clientBytes;
	}
}
