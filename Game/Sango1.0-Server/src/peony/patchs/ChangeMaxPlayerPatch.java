package peony.patchs;

import peony.game.Server;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapInfo;

public class ChangeMaxPlayerPatch implements Runnable {
	private int[] arr = new int[] { 1426, 10, 1427, 10, 1442, 10, 1443, 10,
			128, 10, 129, 10, 64, 10, 80, 10, 336, 10, 337, 10, 96, 15, 97, 15,
			32, 15, 33, 15, 34, 15, 35, 15, 304, 15, 305, 15 };

	public void run() {
		try{
			ProjectData data = Server.server.getServiceRegistry().getDataService().data;
			for (int i = 0; i < arr.length; i += 2) {
				GameMapInfo gmi = GameMapInfo.findByID(data, arr[i]);
				gmi.maxPlayer = arr[i + 1];
				
				System.out.println("Change Map Max Player : " + arr[i] + " , " + arr[i + 1]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
