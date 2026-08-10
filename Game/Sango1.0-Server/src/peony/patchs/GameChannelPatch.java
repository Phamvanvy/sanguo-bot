package peony.patchs;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.dom4j.Document;
import peony.game.CommonUtil;
import peony.game.Server;
import peony.game.gift.ChannelGiftRule;
import peony.game.gift.GameChannelService;

/**
 * 渠道登陆下发礼包修复
 * @author dchen
 */
public class GameChannelPatch implements Runnable {

	@SuppressWarnings("unchecked")
	public void run() {
		GameChannelService service = Server.server.getServiceRegistry()
				.getGameChannelService();
		try {
			Field rulesField = GameChannelService.class.getDeclaredField("rules");
			Field messageField = GameChannelService.class.getDeclaredField("messages");
			rulesField.setAccessible(true);
			messageField.setAccessible(true);
			List<ChannelGiftRule> rules = (List<ChannelGiftRule>) rulesField.get(service);
			ConcurrentHashMap<Integer, String> messages = (ConcurrentHashMap<Integer, String>) messageField.get(service);
			rules.clear();
			messages = new ConcurrentHashMap<Integer, String>();
			byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("channelgift1.xml");
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			Method m = GameChannelService.class.getDeclaredMethod("parse", Document.class);
			m.setAccessible(true);
			m.invoke(service, doc);
			System.out.println("load GameChannelService OK");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
