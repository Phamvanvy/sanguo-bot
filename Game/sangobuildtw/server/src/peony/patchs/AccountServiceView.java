package peony.patchs;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import ch.javasoft.util.intcoll.IntHashMap;

import peony.game.Server;
import peony.net.ClientSession;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;
import peony.service.account.AccountService;

public class AccountServiceView implements Runnable {

	public void run() {
		AccountService service = Server.server.getServiceRegistry().getAccountService();
		try {
			Field field1 = AccountService.class.getDeclaredField("calls");
			field1.setAccessible(true);
			Field field2 = AccountService.class.getDeclaredField("accounts");
			field2.setAccessible(true);
			Field field3 = AccountService.class.getDeclaredField("sessions");
			field3.setAccessible(true);
			ConcurrentHashMap<Integer,AccountAsyncCall> calls = (ConcurrentHashMap<Integer,AccountAsyncCall>)field1.get(service);
			IntHashMap<Account> accounts = (IntHashMap<Account>)field2.get(service);
			IntHashMap<ClientSession> sessions = (IntHashMap<ClientSession>)field3.get(service);
			System.out.print("calls size:"+calls.size());
			System.out.println("accounts size:"+accounts.size());
			System.out.println("sessions size:"+sessions.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
