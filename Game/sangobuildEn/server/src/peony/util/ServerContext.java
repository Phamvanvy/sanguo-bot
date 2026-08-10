package peony.util;

public class ServerContext {
	public static ServerContext context;
	
	private Reg reg;
	
	public ServerContext(){
		context = this;
		this.reg = new Reg();
	}
	
	public Reg getReg(){
		return reg;
	}
}
