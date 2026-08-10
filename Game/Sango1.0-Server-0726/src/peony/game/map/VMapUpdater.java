package peony.game.map;

import org.apache.log4j.Logger;

import peony.game.VMapManager;
import peony.game.World;

/**
 * 更新一个VMapManager的线程，用以实现不同的VMapManager用不同的线程处理，提高系统吞吐量。
 * @author lighthu
 */
public class VMapUpdater extends Thread {
	private static Logger log = Logger.getLogger(VMapUpdater.class);
	
	// 所属世界
	protected World owner;
	// 本线程负责的VMapManager
	protected VMapManager manager;
	// 本次cycle和上次的间隔时间
	protected int diff;
	// 是否正在处理的标志
	protected boolean running;
	
	public VMapUpdater(World owner, VMapManager mgr) {
		this.owner = owner;
		this.manager = mgr;
		running = false;
		this.start();
	}
	
	/**
	 * 开始一次处理。本方法由World的主循环调用，唤醒VMapManager的更新处理线程完成一次处理。 
	 * @param diff
	 */
	public void update(int diff) {
		this.diff = diff;
		this.running = true;
		synchronized (this) {
			notify();
		}
	}
	
	/**
	 * 判断当前处理是否已经完成。
	 */
	public boolean isRunning() {
		return running;
	}
	
	public void run() {
		while (true) {
			// 等待下一次唤醒
			synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
				}
				if (!running) {
					continue;
				}
			}
			
			// 执行一次处理
			long cycleStart = System.currentTimeMillis();
			try {
				manager.update(diff);
			} catch (Throwable e) {
				log.error(e, e);
			}
			long usedTime = System.currentTimeMillis() - cycleStart;
			if (usedTime >= 80) {
				log.warn("Cycle too long for " + manager + ": " + usedTime);
			}
			
			// 唤醒世界主线程
			synchronized (owner) {
				running = false;
				owner.notify();
			}
		}
	}
}
