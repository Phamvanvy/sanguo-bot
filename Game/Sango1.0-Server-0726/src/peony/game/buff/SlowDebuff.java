package peony.game.buff;

import java.text.MessageFormat;

import org.apache.mina.common.ByteBuffer;

import peony.game.CommonUtil;
import peony.game.GameObjectRef;
import peony.game.PropertyCalculator;
import peony.game.PropertyEnhancer;
import peony.game.Time;
import peony.game.Unit;
import peony.game.Updatable;

/**
 * 减速DEBUFF。
 * 
 * @author lighthu
 */
public class SlowDebuff implements Updatable, Buff, PropertyEnhancer {
	/*
	 * 减速级别，每级1%
	 */
	protected int level;
	/*
	 * 过期时间
	 */
	protected int time;
    protected int instanceID;
    protected GameObjectRef source;

	public SlowDebuff(Unit src, int level, int time) {
        if (src != null) {
            source = src.ref();
        }
        instanceID = BuffUtil.getNextID();
		this.level = level;
		this.time = Time.currTime + time;
	}
	
    // 用于从数据库load时创建buff对象
    public SlowDebuff(int lvl, Unit src, Unit tgt, int dmg) {
        instanceID = BuffUtil.getNextID();
    }
    
    public int getInstanceID() {
        return instanceID;
    }
    
    public String getName() {
        return "减速";
    }

	public int getId() {
		return 10004;
	}
	
	public String getDesc() {
		return MessageFormat.format("移动速度降低{0}",CommonUtil.formatPercent(level * 0.01f * 100.0f));
	}
	
	/**
	 * 取得BUFF到期时间。如果BUFF永不到期，返回-1。
	 */
	public int getEndTime() {
		return time;
	}

	/**
	 * 是否良性BUFF。
	 */
	public boolean isGood() {
		return false;
	}
	
	/**
	 * 是否允许驱散。
	 */
	public boolean dispelable() {
		return true;
	}

    /**
     * 是否死亡后保持。
     */
    public boolean keepOnDie() {
        return false;
    }

	public int getIconID() {
		return 67;
	}
	
	/**
	 * 更新游戏时间，检查BUFF是否到期。
	 */
	public boolean update(int diff) {
		return (time <= Time.currTime);
	}

	public boolean isAreaBuff() {
		return false;
	}

	public boolean isNeedMerge() {
		return true;
	}

	/**
	 * 合并BUFF，高级BUFF可以覆盖低级BUFF
	 */
	public boolean merge(Buff buff) {
		if (buff instanceof SlowDebuff) {
			SlowDebuff other = (SlowDebuff) buff;
			if (other.level >= level) {
				level = other.level;
				time = other.time;
			}
			return true;
		}
		return false;
	}

	/**
	 * 减小速度。
	 */
	public void enhance(PropertyCalculator pc) {
		pc.slow(level * 0.01f);
	}

    /**
     * 取得BUFF的原始施法者。如果原始施法者无法被确认，返回null。
     */
    public GameObjectRef getSource() {
        return source;
    }

    /**
     * 设置BUFF所有者。在BUFF加到一个对象上时需要设置。
     */
    public void setOwner(GameObjectRef o) {
    }
    
    /**
     * 刷新BUFF属性。当BUFF所有者获得或失去一个ParamEnhancer类型的BUFF时调用以重算基本属性。
     */
    public void resetParams(Unit owner) {
    }
    
    /**
     * 从保存的数据中恢复BUFF数据。
     */
    public void load(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        level = buf.getInt();
        time = Time.elapseTime(buf.getLong());
    }
    
    /**
     * 把BUFF数据保存到byte数组中。
     */
    public byte[] save() {
        ByteBuffer buf = ByteBuffer.allocate(12, false);
        buf.putInt(level);
        buf.putLong(Time.currentTimeMillis(time));
        return buf.array();
    }
    
    /**
     * 在Player从Loaded状态到Logined状态时需要使用此接口进行更新，如果返回true，那么将要把此buf更新
     * @param time  时间偏移，从上次Loaded状态到Logined状态的时间
     * @return
     */
    public boolean update2(int time) {
        this.time += time;
        return (time < Time.currTime);
    }
}
