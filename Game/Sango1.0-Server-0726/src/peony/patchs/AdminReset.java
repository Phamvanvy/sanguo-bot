package peony.patchs;

import java.lang.reflect.Field;

import peony.game.Admin;

public class AdminReset implements Runnable {

	public void run() {
		try {
			Field field = Admin.class.getDeclaredField("IDS");
			field.setAccessible(true);
			int[] indexes = (int[])field.get(null);
			for(int i=0;i<indexes.length;i++){
				indexes[i] = 0;
			}
			System.out.println("AdminResetOk");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
