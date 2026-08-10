package peony.game.instance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Members {
	
	public List<Member> list = new ArrayList<Member>();
	
	public List<Member> getPlayers(){
		return list;
	}
	
	public void addMember(Member member){
		list.add(member);
	}
	
	public void clearMemeber(){
		list.clear();
	}
	
	public Members clone(){
		Members member = new Members();
		member.list = new ArrayList<Member>(list);
		return member;
	}
	
	public static Members fromDBBytes(byte[] bytes){
		Members ret = new Members();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bis);
		try{
			int size = dis.readShort();
			for(int i=0;i<size;i++){
				int id = dis.readInt();
				int level = dis.readInt();
				int faction = dis.readInt();
				String name = dis.readUTF();
				int sex = dis.readInt();
				int clazz = dis.readInt();
				ret.list.add(new Member(faction,id,level,name,sex,clazz));
			}
		}catch(Exception ex){
			
		}
		return ret;
	}
	
	public byte[] toDBBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.writeShort(list.size());
			for(Member m:list){
				dos.writeInt(m.id);
				dos.writeInt(m.level);
				dos.writeInt(m.faction);
				dos.writeUTF(m.name);
				dos.writeInt(m.sex);
				dos.writeInt(m.clazz);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}
}
