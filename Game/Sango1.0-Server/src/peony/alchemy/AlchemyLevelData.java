package peony.alchemy;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.log4j.Logger;
import peony.game.Player;

public class AlchemyLevelData{
	
	protected static final Logger log = Logger.getLogger(AlchemyLevelData.class);
	
	public Player player;
	
	public byte practiceLevel;//修炼重数(0-4共5重天)
	
	public byte pulseIndex;//当前修炼到第几脉(共0-4脉)
	
	public byte acupointNum;//穴位数（0-8共9个穴位）
	
	public byte acupointLevel;//穴位修炼层数(0-10共10层)
	
	public int alchemyExp;//修炼经验值
	
	public float attackPowerup;//物攻
	
	public float spellPower;//法攻
	
	public float hp;//生命
	
	public float defense;//护甲
	
	public float spellDefense;//法防
	
	public float jewelEnhance;//宝石光效
	
	public int alchemyCount;//修炼次数
	
	
	public int restExp;//留存经验
	
	//是否已突破此重天
	public boolean[] levelBreak=new boolean[]{false,false,false,false,false};
	
	public AlchemyLevelData(Player player){
		this.player = player;
	}
	
	public Object clone() {
		AlchemyLevelData alchemy=new AlchemyLevelData(player);
		return alchemy;
	}
	public byte[] toDBBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
        	dos.writeByte(1);
        	dos.writeByte(practiceLevel);
        	dos.writeByte(pulseIndex);
        	dos.writeByte(acupointNum);
        	dos.writeByte(acupointLevel);
        	dos.writeInt(alchemyExp);
        	dos.writeInt(restExp);
    	} catch (IOException e) {
			e.printStackTrace();
		} 
		return baos.toByteArray();
	}
	
	public static AlchemyLevelData getFromDBBytes(DataInputStream dis, Player player){
		try {
			dis.readByte();//version
			AlchemyLevelData alchemy=new AlchemyLevelData(player);
			alchemy.practiceLevel=dis.readByte();
			alchemy.pulseIndex=dis.readByte();
			alchemy.acupointNum=dis.readByte();
			alchemy.acupointLevel=dis.readByte();
			alchemy.alchemyExp=dis.readInt();
			alchemy.restExp=dis.readInt();
			return alchemy;
		} catch (Exception e) {
			e.printStackTrace();
			return new AlchemyLevelData(null);
		} 
	}
	
}
