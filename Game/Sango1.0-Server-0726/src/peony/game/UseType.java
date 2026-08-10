package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UseType {
	
	public static final int OCCASION_NOBATTLE = 1;
	public static final int OCCASION_BATTLE = 2;
	public static final int OCCASION_ALL = 3;
	
	public static final int TARGET_NOTARGET = 0;
	public static final int TARGET_SELF = 1;
	public static final int TARGET_PARTY = 2;
	public static final int TARGET_ENEMY = 4;
	public static final int TARGET_ALL = 7;
	
    public int useClazz;
    public String useConfirm;
	public int occasion;
	public int targetType;
	public boolean canUse;
	public boolean consume;
	public int spellTime;  //秒
	public int useCount;
	public int coolDownId;
	public int coolDownTime;  //秒
	public int distance;
	public ItemEffect effect;
	
	public static final UseType NOUSETYPE = new UseType(false);
	
	protected UseType(boolean canUse){
		this.canUse = canUse;
	}
	
	public UseType(){
		canUse = true;
	}
	
	public byte[] toClientBytes(){
		if(!canUse)
			return new byte[]{0};
		int flag = 1;
		flag|=occasion<<1;
		flag|=targetType<<3;
		flag|=(consume?1:0)<<6;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(flag);
			dos.writeShort(spellTime);
			dos.writeShort(coolDownId);
			dos.writeInt(coolDownTime);
			if(distance==-1)
				dos.write(distance);
			else
				dos.write(distance>>3);  //给客户端的是用码计算的
			dos.write(useCount);
			//bookskill
			dos.write(useClazz);
			dos.writeUTF(ItemUtil.parseUseConfirm(useConfirm));
		} catch (IOException e) {
		}
		return baos.toByteArray();
	}
}
