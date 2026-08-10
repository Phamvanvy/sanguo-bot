package peony.game.pk;

import java.util.concurrent.atomic.AtomicInteger;

import peony.game.GameObjectRef;
import peony.game.VMap;

public class PkInfo {
	
	public static final AtomicInteger ids = new AtomicInteger(1);
	
	public static final int STATE_INIT = 0; //pk初始化，等待确认阶段
	public static final int STATE_STARTED = 1; //pk开始，战斗阶段
	public static final int STATE_END = 2; //Pk结束，如果Pk结束了将state设置成这个值，那么PkService会将齐从Pk队列中剔除

	public static final int INIT_TIME = 60*1000; //无响应到期时间
	public static final int OUT_TIME = 5*1000; //出界被判输的时间
	
	public int id;
	public GameObjectRef source;
	public GameObjectRef target;
	public int state = STATE_INIT;
	public int startTime;
	public int wager;
	public VMap map;
	public int x,y;
	public int r;
	
	public int sourceLastOutTime = 0; //source上次出界的时间
	public int targetLastOutTime = 0; //target上次出界的时间
	
	public PkService service;

	public PkInfo(PkService pkService,GameObjectRef source, GameObjectRef target,
			int wager, int startTime, int r) {
		this.id = ids.incrementAndGet();
		this.source = source;
		this.target = target;
		this.wager = wager;
		this.r = r;
		this.startTime = startTime;
		this.service = pkService;
		this.service.addPkInfo(this);
	}

	public void update() {
	}
	
	public GameObjectRef getVictimRef(int playerId){
		if(source.id==playerId)
			return target;
		else if(target.id==playerId)
			return source;
		throw new IllegalArgumentException();
	}
	
	public boolean inRange(VMap map,int x,int y){
		if(this.map!=map)
			return false;
		int startX = Math.max(0, this.x - r);
		int endX = Math.min(this.map.getWidth(), this.x+r);
		int startY = Math.max(0, this.y - r);
		int endY = Math.min(this.map.getHeight(), this.y+r);
		return x>=startX&&x<=endX&&y>=startY&&y<=endY;
	}
	
	public boolean isSourceTimeOut(int time){
		if(sourceLastOutTime==0){
			sourceLastOutTime = time;
			return false;
		}else{
			return time - sourceLastOutTime >= OUT_TIME;
		}
	}
	
	public boolean isTargetTimeOut(int time){
		if(targetLastOutTime==0){
			targetLastOutTime = time;
			return false;
		}else{
			return time - targetLastOutTime >= OUT_TIME;
		}
	}
	
	public boolean in(GameObjectRef ref){
		if(source.equals(ref)||target.equals(ref))
			return true;
		return false;
	}
}
