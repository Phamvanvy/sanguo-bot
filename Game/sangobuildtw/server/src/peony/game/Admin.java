package peony.game;

import peony.net.ClientSession;
import peony.net.Packet;

public class Admin implements Client {
	
	protected ClientSession session;
	
	private static final int[] IDS = new int[1000];
	
	public int id;
	public String name;
	public String password;
	
	public Admin(String name,String password) throws Exception{
		this.name = name;
		this.password = password;
		this.id = getValidId();
	}
	
	public void setSession(ClientSession session) {
		this.session = session;
	}
	
	public void send(Packet pt){
		if(session!=null){
			session.send(pt);
		}
	}
	
	public void invalid(){
		IDS[id-10] = 0;
	}
	
	private static int getValidId() throws Exception{
		for(int i=0;i<IDS.length;i++){
			if(IDS[i]==0){
				IDS[i] = 1;
				return i+10;
			}
		}
		throw new Exception();
	}
}
