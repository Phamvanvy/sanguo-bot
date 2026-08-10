package com.pip.itimes.server.world.admin;

import java.util.ArrayList;
import java.util.List;

import com.pip.itimes.net.UWAPData;
import com.pip.itimes.server.bean.Admin;
import com.pip.itimes.server.bean.Admin.AdminAuth;
import com.pip.itimes.server.stage.Command;
import com.pip.itimes.server.world.AdminSession;
import com.pip.itimes.server.world.AdminSession.IAdminFunction;

public class RootAdmin implements IAdminFunction {
    public static String helpStr = "GM账户管理 \n" + 
    "    命令格式: RootAdmin changeAuth <GM-ID> <GM_PASS> authStr\n"
			;
    public boolean canExecute(Admin admin) {
    	return admin != null && admin.hasAuth(AdminAuth.root);
    }

	public void execCommand(AdminSession admin, Command command) {
		String cmd = command.getCommand().toLowerCase();
		if (cmd.equals("rootadmin")) {
			execRoot(admin,command);
		} else if (cmd.equals("grant")) {
			execGrant(admin,command);
		} else {
			admin.write("不支持命令");
		}
		
	}
	private void execRoot(AdminSession adminSession, Command command) {
		Admin admin = adminSession.getAdmin();
		if (admin == null || !admin.hasAuth(AdminAuth.root)) {
			adminSession.write("没有登陆或者没有权限");
			return;
		}
        if (command.getParamCount() == 0) {
        	List adminList = adminSession.adminService.getAdminList();
        	if (adminList != null) {
	        	StringBuffer buf = new StringBuffer("[ADMIN-LIST]账户名;密码;权限:");
	        	for (Object obj : adminList) {
	        		Admin adm = (Admin)obj;
	        		buf.append("\n");
	        		buf.append(adm.getName());
	        		buf.append(";");
	        		buf.append(adm.getPassword());
	        		buf.append(";");
	        		buf.append(adm.getAuth());
	        	}
	    		adminSession.write(buf.toString());
        	} else {
        		adminSession.write("请求失败");
        	}
        } else {
        	String cmd = command.getParam(0);
        	if (cmd.equals("list")) {
        		ArrayList<AdminSession> lst = adminSession.adminService.listOnlines();
        		StringBuffer buf = new StringBuffer("值班GM:");
        		for (AdminSession session : lst) {
        			Admin adm = session.getAdmin();
        			buf.append("\n");
        			buf.append(adm.getName());
        			buf.append(":");
        			buf.append(adm.getPassword());
        			buf.append("[");
        			buf.append(adm.getAuth());
        			buf.append("]");
        		}
        		adminSession.write(buf.toString());
        	}
        }
	}
	private void execGrant(AdminSession adminSession, Command command) {
		Admin admin = adminSession.getAdmin();
		if (admin == null || !admin.hasAuth(AdminAuth.root)) {
			adminSession.write("没有登陆或者没有权限");
			return;
		}
        if (command.getParamCount() != 3) {
            adminSession.write("参数不对");
        } else {
        	String adminName = command.getParam(0);
        	String adminPass = command.getParam(1);
        	String adminAuth = command.getParam(2);
        	Admin adm = adminSession.adminService.getAdmin(adminName, adminPass);
        	if (adm == null) {
        		adm = new Admin();
        		adm.setName(adminName);
        		adm.setPassword(adminPass);
        	}
    		adm.setAuth(adminAuth);
    		if (adm.getAuth().toLowerCase().equals("0x0")) {
        		adminSession.adminService.deleteAdmin(adm);
        		adminSession.write("成功");
    		} else {
        		adminSession.adminService.addAdmin(adm);
        		adminSession.write("成功");
    		}
        }
	}

	
	public void execCommand(AdminSession adminSession, UWAPData data) throws Exception {
	}

	public String getHelp() {
		return helpStr;
	}
	public int[] getProtocolId() {
		return new int[]{};
	}
	public String[] getCommands() {
		return new String[]{"grant"};
	}
}
