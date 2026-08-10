package peony.game.association;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AssociationMemberList {

	public List<AssociationMember> members = new ArrayList<AssociationMember>();
	
	public AssociationMemberList clone(){
		AssociationMemberList ret = new AssociationMemberList();
		ret.members = new ArrayList<AssociationMember>(members);
		return ret;
	}
	
	public byte[] toDbByte(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		List<AssociationMember> members0 = new ArrayList<AssociationMember>(members);
		try {
			dos.write(members0.size());
			for(AssociationMember mem : members0){
				dos.write(mem.duty);
				dos.writeInt(mem.playerId);
				dos.write(mem.state);
				dos.writeLong(mem.inviteTime);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public AssociationMemberList getFromDbBytes(byte[] bytes){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		try {
			int size = dis.read();
			AssociationMemberList ret = new AssociationMemberList();
			for(int i=0;i<size;i++){
				int duty = dis.read();
				int playerId = dis.readInt();
				int state = dis.read();
				long inviteTime = dis.readLong();
				AssociationMember m = new AssociationMember(duty, playerId, state);
				m.inviteTime = inviteTime;
				ret.members.add(m);
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
