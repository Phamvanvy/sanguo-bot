package peony.patchs;

import java.lang.reflect.Field;

import edu.emory.mathcs.backport.java.util.Arrays;

import peony.game.Admin;

public class AdminLockPatch implements Runnable {
	public void run() {
		try {
			Class cls = Admin.class;
			Field fld = cls.getDeclaredField("IDS");
			fld.setAccessible(true);
			int[] ids = (int[])fld.get(null);
			Arrays.fill(ids, 0);
			System.out.println("over");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new AdminLockPatch().run();
	}
}
