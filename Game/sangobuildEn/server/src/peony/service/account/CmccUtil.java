package peony.service.account;

import java.util.concurrent.atomic.AtomicInteger;

public class CmccUtil {
	private static final AtomicInteger id_generator = new AtomicInteger(1);
	
	public static int generateId(){
		return id_generator.incrementAndGet();
	}
}
