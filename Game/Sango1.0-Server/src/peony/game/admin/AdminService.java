package peony.game.admin;

import java.util.HashMap;
import java.util.Map;

import peony.game.Admin;
import peony.game.Server;
import peony.service.Service;

public class AdminService implements Service{
	
	protected Map<String,Admin> admins = new HashMap<String,Admin>();
	protected String password = "123456789";

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		Server.server.getServiceRegistry().getChannelService().createChannel("gm", false);
		String password = Server.server.getConfig().getString("gmpassword");
		if(password != null){
			this.password = password;
		}
	}
	
	public Admin getAdmin(String name){
		return admins.get(name);
	}
	
	public void removeAdmin(String name){
		admins.remove(name);
	}
	
	public void addAdmin(Admin admin){
		admins.put(admin.name, admin);
	}
	
	public Admin getAdmin(int id){
		for(Admin admin:admins.values()){
			if(admin.id==id)
				return admin;
		}
		return null;
	}
	
	public String getPassword(){
		return password;
	}
}
