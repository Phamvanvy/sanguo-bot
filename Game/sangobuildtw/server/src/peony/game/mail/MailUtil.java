package peony.game.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;

public class MailUtil {
	
	public static MailAttachment getMailAttachmentFromDB(byte[] bytes){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		try {
			int version = dis.read();//version
			int type = dis.read();
			if(type==2){ //money
				return new MoneyMailAttachment(dis.readInt());
			}
			else if(type==1){
				int count = dis.readInt();
				GameItem item = ItemUtil.getGameItemFromDB(dis,version);
				if(item.instanceId!=GameItem.GENERAL_INSTANCEID){
					ObjectAccessor.addGameItemToCached(item);
				}
				return new ItemMailAttachment(item,count);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] getAttachmentDBBytes(ItemMailAttachment attachment){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(2);//version
			dos.write(1);
			dos.writeInt(attachment.getCount());
			dos.write(ItemUtil.getGameItemDBBytes(attachment.getGameItem()));
		}catch(IOException ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public static byte[] getAttachmentDBBytes(MoneyMailAttachment attachment){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(2);//version
			dos.write(2);
			dos.writeInt(attachment.getCount());
		}catch(IOException ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();	
	}
	
//	public MailAttachment getMailAttachmentFromClient(byte[] bytes,Player p) {
//		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//		DataInputStream dis = new DataInputStream(bais);
//		int type = dis.read();
//		if(type==1){ //item
//			int itemId = dis.readInt();
//			int instanceId = dis.readInt();
//			int count = dis.read();
//		}
//		else if(type==2){
//			new MoneyMailAttachment(dis.readInt());
//		}
//		else
//			throw new IllegalArgumentException();
//	}
}
