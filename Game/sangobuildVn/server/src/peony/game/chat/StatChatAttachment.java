package peony.game.chat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StatChatAttachment implements ChatAttachment {

	protected int cato;
	protected String title;
	
	public StatChatAttachment(int cato, String title){
		this.cato = cato;
		this.title = title;
	}
	
	public byte[] toBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(3);
			dos.write(cato);
			dos.writeUTF(title);
		}catch(IOException ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}

}
