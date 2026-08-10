package peony.patchs;

public class LogFormulaPatch implements Runnable {

	public void run() {
		try {
//			ItemUtil.loadFormulas(Server.server.getServiceRegistry().getDataService().data);
			System.out.println("load formula ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
