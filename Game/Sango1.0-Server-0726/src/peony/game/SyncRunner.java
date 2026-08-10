package peony.game;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

/**
 * 放在主循环的最后执行update，一般用在异步的程序某个步骤需要在主循环中执行
 * @author Jeffrey
 *
 */
public class SyncRunner {
	
	private static final Logger log = Logger.getLogger(SyncRunner.class);
	
	protected ConcurrentLinkedQueue<Runnable> runs = new ConcurrentLinkedQueue<Runnable>();
	
	public void add(Runnable run){
		runs.add(run);
	}
	
	public void update(){
		Iterator<Runnable> ite = runs.iterator();
		while(ite.hasNext()){
			Runnable run = ite.next();
			try{
				run.run();
			}catch(Exception ex){
				log.error(ex,ex);
			}finally{
				ite.remove();
			}
		}
	}
}
