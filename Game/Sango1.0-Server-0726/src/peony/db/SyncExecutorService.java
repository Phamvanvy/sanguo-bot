package peony.db;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import peony.common.AsyncCall;
import peony.service.Service;

/**
 * 需要同步执行的线程池管理器
 * @author dchen
 */
public class SyncExecutorService implements Service {

	protected ExecutorService executor = new ThreadPoolExecutor(5, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	public static int autoMergerAndRemoveFlag = 1;
	
	public void startup() throws Exception {

	}
	
	public void schedule(AsyncCall call){
		executor.execute(call);
	}

	public void shutdown() {
		
	}

}
