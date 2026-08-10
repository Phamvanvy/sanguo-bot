package peony.game;

import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

/**
 * 放在主循环的最后执行update，一般用在异步的程序某个步骤需要在主循环中执行
 * @author Jeffrey
 *
 */
public class SyncRunner {
	
	private static final Logger log = Logger.getLogger(SyncRunner.class);
	
	protected ArrayBlockingQueue<Runnable> runs = new ArrayBlockingQueue<Runnable>(1024);
	
	public void add(Runnable run){
		runs.offer(run);
//		runs.add(run);
	}
	
	public void update(){
		Runnable run = null;
//		Iterator<Runnable> ite = runs.iterator();
		while((run = runs.poll()) != null) {
//		while(ite.hasNext()){
//			Runnable run = ite.next();
			try{
				run.run();
			}catch(Exception ex){
				log.error(ex,ex);
			}finally{
//				ite.remove();
			}
		}
	}
}
