package peony.testclient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.pip.sanguo.data.map.Period;



public class TestClient {
	
	
	public static void main(String[] args){
//		String s = "6sabcsssfsfs33";
//		StringBuilder sb = new StringBuilder("6sabcsssfsfs33");
//		sb.delete(2, 4);
//		sb.delete(sb.length()-2, sb.length());
//		System.out.println(sb.toString());
//		String s = "abcdef\nbadsef";
//		System.out.print(s.replaceAll("[\r\n]", ""));
//		CacheManager cacheManager = CacheManager.create();
//		Cache cache = new Cache("recycle",3000,false,false,5,5);
//		cacheManager.addCache(cache);
//		cache.put(new Element(1,"aaa"));
//		for(;;){
//			System.out.println("Size:"+cache.getSize());
//			try {
//				Thread.sleep(200);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		SocketConnector connector = new SocketConnector();
//		SocketAcceptorConfig cfg = new SocketAcceptorConfig();
//        cfg.getFilterChain().addLast( "codec", new ProtocolCodecFilter(MinaUAEncoder.class,MinaUADecoder.class));
//        connector.connect(new InetSocketAddress("127.0.0.1",7001), handler, cfg);
//		new Thread(new Worker(0),"A").start();
//		new Thread(new Worker(1),"B").start();
//		new Thread(new Worker(2),"C").start();
		
//		String s = "abc|def";
//		String[] ss = s.split("\\|");
//		for(String sss:ss){
//			System.out.println(sss);
//		}
//		Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.MONTH, 9);
//		cal.set(Calendar.DAY_OF_MONTH, 17);
//		for(int i=1;i<=70;i++){
//			long v = (System.currentTimeMillis()-cal.getTimeInMillis())/(30*60*1000L);
//			int exp = (int)(v * ExpService.onlineExps[i]);
//			System.out.printf("当前等级%d,获得经验%d,所有获得经验可以升级至%d,百分三十经验可以升级至%d\n",i,exp,PlayerUtil.getUpLevel(i, exp)+i,PlayerUtil.getUpLevel(i, (int)(exp*0.3f))+i);
//		}
//		for(int i=1;i<=10;i++){
//			System.out.printf("当前等级1级，获得经验%d,升级至%d\n",10000000*i,1+PlayerUtil.getUpLevel(1, 10000000*i));
//		}
//		int v = 12515040;
//		System.out.println(PlayerUtil.getUpLevel(40, v/2));
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
		List<Period> ps = new ArrayList<Period>();
		ps.add(new Period(12,0,13,0));
		ps.add(new Period(20,0,21,0));
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 10);
		for(int i=0;i<20;i++){
			System.out.print("Time:"+format.format(calendar.getTime()));
			System.out.println("----"+format.format(Period.getNextTimeInPeriods(calendar, ps).getTime()));
			calendar.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		
	}
}

class Worker implements Runnable{
	
	static int count = 0;
	static Object lock = new Object();
	
	int index;
	int pCount;
	
	public Worker(int index){
		this.index = index;
	}
	
	public void run() {
		synchronized (lock) {
			for (;;) {
				if(count%3==index){
					System.out.println(Thread.currentThread().getName());
					count++;
					pCount++;
					lock.notifyAll();
					if(pCount==10)
						break;
				}else{
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
