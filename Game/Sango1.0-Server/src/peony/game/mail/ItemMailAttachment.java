package peony.game.mail;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import peony.game.GameItem;

public class ItemMailAttachment implements MailAttachment{

	protected GameItem item;
	protected int count;
	
	public ItemMailAttachment(GameItem item,int count){
		this.item = item;
		this.count = count;
	}
	
	public GameItem getGameItem(){
		return item;
	}
	
	public int getCount(){
		return count;
	}
	
	public void setCount(int count){
		this.count = count;
	}
	
	public byte[] toClientBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(1);
			dos.writeInt(item.template.id);
			dos.writeInt(item.instanceId);
			dos.write(count);
			dos.writeUTF(item.template.name);
			dos.write(item.template.showImage);
			dos.writeShort(item.template.showType);
			dos.write(item.template.quality);
		}catch(IOException ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	@Override
	public ItemMailAttachment clone(){
		return new ItemMailAttachment(item,count);
	}

}
