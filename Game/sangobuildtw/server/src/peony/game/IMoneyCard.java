package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;


public class IMoneyCard implements GameItemObject {
	
	protected int value;
	protected String cardno;
	protected String password;
	
	public IMoneyCard(int value,String cardno,String password){
		this.value = value;
		this.cardno = cardno;
		this.password = password;
	}
	
	public int getValue(){
		return value;
	}
	
	public String getCardno(){
		return cardno;
	}
	
	public String getPassword(){
		return password;
	}
	
	public IMoneyCard clone(){
		return new IMoneyCard(value,cardno,password);
	}
	
	public String getDesc() {
		return null;
	}

	public String logString() {
		StringBuilder sb = new StringBuilder(200);
		sb.append("[CARD[").append(value).append(',').append(cardno).append(',').append(password).append("]]");
		return sb.toString();
	}
	
	public byte[] toDBBytes(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeByte(1);
            dos.writeInt(value);
            dos.writeUTF(cardno);
            dos.writeUTF(password);
            dos.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
	}

	public static IMoneyCard fromDBBytes(DataInputStream dis, GameItem owner){
        try {
            byte version = dis.readByte();
            if (version == 1) {
            	int value = dis.readInt();
            	String cardno = dis.readUTF();
            	String password = dis.readUTF();
            	return new IMoneyCard(value,cardno,password);
			}else{
				return null;
			}
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
	
	public Class<? extends Marshaller> marshallerClass() {
		return IMoneyCardPersistence.class;
	}

	public Class<? extends Serializer> serializerClass() {
		return IMoneyCardPersistence.class;
	}

	/**
	 * 把对象添加到一个日志字符串中。
	 */
	public void dump(StringBuilder out) {
		out.append("CNO=").append(cardno).append(",VLU=").append(value);
	}
}
