package peony.game.chat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import peony.vm.ASMQuest;

public class QuestChatAttachment implements ChatAttachment{
	protected ASMQuest quest;
	
	public QuestChatAttachment(ASMQuest quest){
		this.quest = quest;
	}
	//{02{byte},questId(int,name(string)}
	public byte[] toBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(2);
			dos.writeInt(quest.getId());
			dos.writeUTF(quest.getGameQuest().getName());
		}catch(IOException ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}
}
