package peony.game.drop;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.game.CommonUtil;
import peony.game.Gain;

/**
 * 根据权重选择掉落,在初始化的时候，每个掉落的权重会重新计算。比如，掉落A的权重是10，掉落B的权重是20。程序先载入A，这时候A的权重是10，
 * 再载入B，B的权重被重新计算为10+20=30。总权重也是30。在计算掉落的时候会计算出来[0,总权重)之间的数，然后一次查询掉落的权重，如果掉落
 * 的权重大于次数，那么就被确认是掉落
 * @author Jeffrey
 *
 */
public class LeveledGroupDrop extends RangeDrop {
	
	private static final Logger log = Logger.getLogger(LeveledGroupDrop.class);
	
	protected int minLevel,maxLevel,clazz,weight;
	protected List<WeightGroupDrop> drops = new ArrayList<WeightGroupDrop>();
	
	public LeveledGroupDrop(int minLevel,int maxLevel,int clazz,int min,int max){
		super(min,max);
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.clazz = clazz;
	}
	
	
	public void addGroupDrop(WeightGroupDrop groupDrop){
	    groupDrop.start = weight;
		weight += groupDrop.weight;
		groupDrop.weight = weight;
		drops.add(groupDrop);
	}
	
	public void calc(Random rnd, Gain gain) {
	    if (weight <= 0) {
	        return;
	    }
		int count = CommonUtil.getCount(rnd, min, max);  //先确认有几个掉落
		for (int i = 0; i < count; i++) {
			int c = rnd.nextInt(weight);
			WeightGroupDrop drop = getDropAtWeight(c);
			if (drop != null) {
			    drop.calc(rnd, gain);
			}
		}
	}

	protected WeightGroupDrop getDropAtWeight(int c) {
	    int start = 0; int end = drops.size() - 1;
        while (start <= end) {
            int mid = (start + end) / 2;
            WeightGroupDrop d = drops.get(mid);
            if (c < d.start) {
                end = mid - 1;
                continue;
            } else if (c >= d.weight) {
                start = mid + 1;
                continue;
            } else {
                return d;
            }
        }
        return null;
	}
}
