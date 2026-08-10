package peony.game.chat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import peony.game.GameItem;

public class ItemChatAttachment implements ChatAttachment{
	
	protected GameItem item;
	
	public ItemChatAttachment(GameItem item){
		this.item = item;
	}
	
	//01(byte),itemId,instanceId(int),name(string),showType(byte),quality(byte)
	public byte[] toBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(1);
			dos.writeInt(item.template.id);
			dos.writeInt(item.instanceId);
			dos.writeUTF(item.template.name);
			dos.write(item.template.showType);
			dos.write(item.template.quality);
		}catch(IOException ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}
}
