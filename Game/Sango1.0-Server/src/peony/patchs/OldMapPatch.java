package peony.patchs;

import java.lang.reflect.Field;

import peony.db.PlayerLoadCall;

public class OldMapPatch implements Runnable {

	protected int[] oldMaps = {864,1456,2016,1552,128,97,144,160,544,672,656,64,32,16,176,480,608,624,336,304,288,416,497,592,
			688,1808,1809};
	
	public void run() {
		try {
			Class clazz = PlayerLoadCall.class;
			Field f = clazz.getDeclaredField("oldMaps");
			f.setAccessible(true);
			f.set(clazz, oldMaps);
			System.out.println("____________oldMap set OK");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
