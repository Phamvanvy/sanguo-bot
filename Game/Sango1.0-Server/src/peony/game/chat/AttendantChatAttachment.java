package peony.game.chat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import peony.game.attendant.Attendant;

public class AttendantChatAttachment implements ChatAttachment{
	protected Attendant att;
	protected int playerId;
	
	public AttendantChatAttachment(Attendant att, int playerId){
		this.att = att;
		this.playerId = playerId;
	}
	
	public byte[] toBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(5);
			dos.writeInt(att.instanceId);
			dos.writeInt(playerId);
		}catch(IOException ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}
}
