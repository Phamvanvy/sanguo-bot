package peony.patchs;

import java.lang.reflect.Field;
import java.util.Map;

import peony.game.Admin;
import peony.game.Server;
import peony.game.admin.AdminService;

public class AdminResetAdmins implements Runnable {

	public void run() {
		AdminService service = Server.server.getServiceRegistry().getAdminService();
		try {
			Field field = AdminService.class.getDeclaredField("admins");
			field.setAccessible(true);
			Map<String,Admin> m = (Map<String,Admin>)field.get(service);
			m.clear();
			System.out.println("AdminReset");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
