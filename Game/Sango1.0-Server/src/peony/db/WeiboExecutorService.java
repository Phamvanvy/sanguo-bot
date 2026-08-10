package peony.db;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import peony.common.AsyncCall;
import peony.service.Service;

public class WeiboExecutorService implements Service{

	protected ExecutorService executor = new ThreadPoolExecutor(8, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	
	public void startup() throws Exception {

	}
	
	public void schedule(AsyncCall call){
		executor.execute(call);
	}

	public void shutdown() {
		
	}
}
