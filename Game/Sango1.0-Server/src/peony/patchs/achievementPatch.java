package peony.patchs;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import peony.game.CommonUtil;
import peony.game.Server;
import peony.service.stat.StatService;

public class achievementPatch implements Runnable {

	public void run() {
		StatService service = Server.server.getServiceRegistry().getStatService();
		try {
			service.achieveId2Achieve.clear();
			Field f = StatService.class.getDeclaredField("type2AchiveId");
			f.setAccessible(true);
			Map<Integer,List<Integer>> type2AchiveId = (Map<Integer, List<Integer>>) f.get(service);
			type2AchiveId.clear();
			byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("achievement.xml");
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			service.parse(doc);
			System.out.println("______________load stat OK");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
