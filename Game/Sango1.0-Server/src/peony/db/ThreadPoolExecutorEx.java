package peony.db;

import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Player;
import peony.util.TimeUtil;

/**
 * 扩展线程池，统计每个call执行的时间。
 * @author lighthu
 */
public class ThreadPoolExecutorEx extends ThreadPoolExecutor {
	private static Logger log = Logger.getLogger(ThreadPoolExecutorEx.class);
	private Hashtable<Runnable, Long> taskStartTime = new Hashtable<Runnable, Long>();
	
	public ThreadPoolExecutorEx(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		if (TimeUtil.monitorPerformace) {
			Long startTime = taskStartTime.remove(r);
			if (startTime != null) {
				long used = System.nanoTime() - startTime.longValue();
				if (used > 1000000000L) {
					// 单个call执行超过1s的，报警
					int playerID = -1;
					if (r instanceof ClientSessionAsyncCall) {
						ClientSessionAsyncCall call = (ClientSessionAsyncCall)r;
						if (call.getSession() != null && call.getSession().getClient() instanceof Player) {
							playerID = ((Player)call.getSession().getClient()).id;
						}
					}
					log.warn("[CALLTOOLONG]ID[" + playerID + "]CLASS[" + r.getClass().getSimpleName() + "]TIME[" + (used / 1000000) + "]");
				}
			}
		}
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		if (TimeUtil.monitorPerformace) {
			taskStartTime.put(r, System.nanoTime());
		}
	}
}
