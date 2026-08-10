package peony.patchs;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import peony.util.KeywordsManager;
import peony.util.StringUtil;

public class KeyWordPatch1 implements Runnable {

	public void run() {
		try {
			Field f = StringUtil.class.getDeclaredField("keywordsManager");
			f.setAccessible(true);
			KeywordsManager manager = (KeywordsManager) f.get(StringUtil.class);
			if(manager!=null){
				Class cc = KeywordsManager.class;
				String dir = System.getProperty("user.dir");
				File file = new File(dir, "keywords.xml");
				if (file != null) {
					Method m = cc.getDeclaredMethod("loadKeywords", File.class, List.class);
					m.setAccessible(true);
					m.invoke(manager, file, new ArrayList<String>(0));
					System.out.println("________________Ok");
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
