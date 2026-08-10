package peony.patchs;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Timer;

import org.dom4j.Document;

import peony.game.CommonUtil;
import peony.game.Server;
import peony.game.beautyparade.BeautyParadeService;

public class BeautyTimerPatch implements Runnable {

	public void run() {
		BeautyParadeService service = Server.server.getServiceRegistry().getBeautyParadeService();
		try {
			Field fTimer = BeautyParadeService.class.getDeclaredField("timer");
			fTimer.setAccessible(true);
			Timer timer = (Timer)fTimer.get(service);
			timer.cancel();
			timer.purge();
			timer = new Timer();
			fTimer.set(service, timer);
			byte[] bytes = Server.server.getServiceRegistry().getDataService().data
			.findFile("beautyparade.xml");
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			Method mParse = BeautyParadeService.class.getMethod("parse", Document.class);
			mParse.invoke(service, doc);
			Method mProcessNotify = BeautyParadeService.class.getMethod("processNotify");
			mProcessNotify.invoke(service);
			Method mProcessTimer = BeautyParadeService.class.getMethod("processTimer");
			mProcessTimer.invoke(service);
			System.out.println("Beauty reload ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
