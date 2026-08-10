package peony.patchs;

import peony.game.Title;
import peony.game.TitleUtil;

public class TitlePatch2 implements Runnable {

	public void run() {
		Title t1 = TitleUtil.getTitle(52);
		Title t2 = TitleUtil.getTitle(53);
		Title t3 = TitleUtil.getTitle(54);
		t1.name = "魏國新兵";
		t1.desc = "";
		t1.type = Title.TYPE_OTHER;
		t1.createClientBytes();
		t2.name = "蜀國新兵";
		t2.desc = "";
		t2.type = Title.TYPE_OTHER;
		t2.createClientBytes();
		t3.name = "吳國新兵";
		t3.desc = "";
		t3.type = Title.TYPE_OTHER;
		t3.createClientBytes();
		TitleUtil.weiTitles.remove(t1);
		TitleUtil.shuTitles.remove(t2);
		TitleUtil.wuTitles.remove(t3);
		TitleUtil.otherTitles.add(t1);
		TitleUtil.otherTitles.add(t2);
		TitleUtil.otherTitles.add(t3);
		System.out.println("TitleModifyOk");
	}

}
