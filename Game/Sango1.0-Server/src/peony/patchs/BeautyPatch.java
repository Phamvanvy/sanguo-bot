package peony.patchs;

import java.lang.reflect.Field;
import peony.game.beautyparade.BeautyParadeService;

public class BeautyPatch implements Runnable {

	private static int[] items = {2621,2622,2623,2624,2625,2626,2627,2628,2629,2630,2620}; // Ω±¿¯ŒÔ∆∑
	
	public void run() {
		try {
			Field items2Field = BeautyParadeService.class.getDeclaredField("items2");
			items2Field.setAccessible(true);
			items2Field.set(BeautyParadeService.class, items);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
