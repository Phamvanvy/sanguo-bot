package peony.game;

import java.util.Random;

import org.apache.log4j.Logger;

import peony.net.Packet;

import com.pip.sanguo.data.Animation;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pipimage.image.PipAnimateSet;

public class GatherUnit extends GameObject {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(GatherUnit.class);
	
	private static final Random rnd = new Random();
	
    public int disappearTime;    // 自动消失时间（通常用于召唤的怪物），0表示不自动消失
	public int refreshTime;
	public int gatherTime;
	public int questId;
	public Fall fall;
	
	public int dieTime;
	
	public int startTime;
	public boolean isStaticField;

	public NPCTemplate template;
	public PipAnimateSet image;
	public Animation npcImage;
	
	public GameObjectRef ref;
	
	public GatherEndCall call;
	
	public boolean isPvp; //pvp采集点，如果采集的时候正在pvppve状态，那么取消状态到pvp状态
	
	public int level;
	
	public int isGathering;
	
	public long lastRefreshTime;
	
	public GatherUnit(){
		this(null);
		faction = FACTION_GATHER;
	}
	
	public GatherUnit(GatherEndCall call){
		super(GameObject.TYPE_GATHER);
		instanceId = ids.incrementAndGet();
		this.map = new VMapReference();
		this.call = call;
		faction = FACTION_GATHER;
	}
	
	public void gatherStart(Player p){
		if(questId!=-1){
			if(p.asmVm.hasTask(questId)==0||p.asmVm.taskFinished(questId)==1){
				ErrorHandler.sendErrorMessage(p.session, -1, OpCode.GATHER_START_CLIENT, "不能采集此物品");
				return;
			}
		}
		startTime = Time.currTime;
		ref = p.ref();
	}
	
	public void gatherEnd(Player p){
		startTime = 0;
		ref = null;
		die();
		moveType |= MOVE_STATE;
		if(call!=null)
			call.gatherEnd(this, p);
	}
	
	@Override
	public void update(int diffTime) {
		try {
		    if (isAlive()) {
                if (disappearTime != 0 && disappearTime < Time.currTime) {
                    removeFromWorld();
                    return;
                }
		    }
			if (!isAlive()) {
				dieTime -= diffTime;
				if (dieTime < 0)
					dieTime = 0;
				if (dieTime == 0)
					relive();
			}
			processMove(null);
		} catch (Exception ex) {
			log.error(ex,ex);
		}
	}
	
	public void die(){
		int v = (int)(refreshTime * 0.2f);
		int f = 0;
		if(v>0){
			f = rnd.nextInt(v*2);
			f -=v;
		}
		dieTime = refreshTime + f;
		state |= STATE_DIE;
		moveType |= MOVE_STATE;
	}
	
	public void relive() {
		if (map != null)
			ObjectAccessor.removeGameObject(this);
		VMap oldMap = map.map;
		map.removeGameObject(this, false);
		ref = null;
		lastRef = null;
		instanceId = ids.incrementAndGet();
		instanceTime = Time.currTime;
		state = 0;
		ObjectAccessor.addGameObject(this);
		oldMap.addGatherUnit(this);
		moveType |= MOVE_ALL;
	}
	
	@Override
	public boolean isAlive(){
		return (state&STATE_DIE) == 0;
	}
	
	@Override
	public boolean isStatic() {
	    return isStaticField;
	}

    /**
     * 判断是否服务器动态刷新出来的（没有关卡数据支持）。
     * @return
     */
    public boolean isDynamic() {
        return false;
    }
	
	@Override
	public Packet getInfoPacket() {
		Packet pt = new Packet(OpCode.UNIT_INFO_SERVER);
		pt.putInt(instanceId);
		pt.putInt(questId);
		pt.putInt(gatherTime);
		return pt;
	}
	
	public static final short MOVE_MASK = MOVE_POINT|MOVE_STATE|MOVE_DETAIL|MOVE_NAME|MOVE_LEVEL|MOVE_FACTION; //GatherUnit只支持这几种move信息，其他的将全被剔除
	
	@Override
	public Packet getMovePacket(short moveType){
		short t = (short)(moveType&MOVE_MASK);
		Packet pt = new Packet(OpCode.UNIT_MOVE_SERVER);
		pt.put(type|t);
		pt.putInt(instanceId);
		if((t&MOVE_POINT)!=0){
			pt.putShort(map.id);
			pt.putShort(x);
			pt.putShort(y);
		}
		if((t&MOVE_STATE)!=0){
			pt.putShort(state);
		}
		if((t&MOVE_DETAIL)!=0){
			pt.put(t>>8);
			if((t&MOVE_NAME)!=0){
				pt.putString(name);
			}
			if((t&MOVE_LEVEL)!=0){
				pt.put(level);
			}
			if((t&MOVE_FACTION)!=0){
				int faction = this.faction;
				if(isStatic()&&questId==-1) faction |= (1<<7);
				pt.put(faction);
			}
		}
		return pt;
	}
	
	
	@Override
	public Packet getRefreshPacket(boolean visible){
		Packet pt = new Packet(OpCode.UNIT_REFRESH_SERVER);
		byte b = type;
		if(!visible){
			b |= 1<<7;
		}
		pt.put(b);
		pt.putInt(id);
		pt.putInt(instanceId);
		if(visible)
			pt.putShort(npcImage.getID());
		return pt;
	}
	
	
	@Override
	public void getRefreshPacket(Packet pt,boolean visible){
		byte b = type;
		if(!visible){
			b |= 1<<7;
		}
		pt.put(b);
		pt.putInt(id);
		pt.putInt(instanceId);
		if(visible)
			pt.putShort(npcImage.getID());
	}
	
	
	/**
	 * 模板属性改变，重新设置对象属性。
	 */
	public void updateTemplate() {
	    gatherTime = template.collectTime;
	    questId = template.questId;
	    npcImage = template.image;
	    fall = new Fall(this);
	    VMapUtil.createFall(fall, template);
	}

/**
     * NPC属性改变，重新设置对象属性。
     * @param npc
     */
    public void updateSetting(GameMapNPC npc) {
        name = npc.name;
        move(npc.x, npc.y);
        refreshTime = npc.refreshInterval * 1000;
        isStaticField = npc.isStatic;
    }
}
