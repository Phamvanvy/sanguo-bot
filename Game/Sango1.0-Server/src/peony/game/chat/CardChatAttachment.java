package peony.game.chat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CardChatAttachment implements ChatAttachment {

	protected int cato;
	protected String title;
	protected int cardPropertyType;//品质
	protected int cardLevel;//卡片等级
	
	public CardChatAttachment(int cato, String title,int type,int level){
		this.cato = cato;
		this.title = title;
		this.cardLevel=level;
		this.cardPropertyType=type;
	}
	
	public byte[] toBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(4);
			dos.write(cato);
			dos.writeUTF(title);
			dos.write(cardLevel);
			dos.write(cardPropertyType);
		}catch(IOException ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}

}
