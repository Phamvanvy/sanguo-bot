package peony.game.mail;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MoneyMailAttachment implements MailAttachment{

	protected int count;
	
	public MoneyMailAttachment(int count){
		this.count = count;
	}
	
	public int getCount(){
		return count;
	}
	
	public byte[] toClientBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(2);
			dos.writeInt(count);
		}catch(IOException ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	@Override
	public MoneyMailAttachment clone(){
		return new MoneyMailAttachment(count);
	}

}
