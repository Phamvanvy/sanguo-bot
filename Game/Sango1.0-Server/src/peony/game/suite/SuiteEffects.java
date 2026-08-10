package peony.game.suite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuiteEffects {
    protected int id;
	protected String name;
	protected SuiteEffect[] effects;
	protected List<Integer> equips = new ArrayList<Integer>(); // 没件套装包含的装备id的集合 
	public Map<Integer, Integer> weights = new HashMap<Integer, Integer>();
	
	public static final int TYPE_NORMAL=0;//没有权重
	public static final int TYPE_WEIGHT=1;//有权重
	
	public int type=TYPE_NORMAL;
	
	public SuiteEffects(int id, String name,SuiteEffect[] effects){
	    this.id = id;
		this.name = name;
		this.effects = effects;
	}
	public int getID() {
	    return id;
	}
	public SuiteEffect[] getEffects() {
		return effects;
	}
	 public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/**
     * 添加装备
     */
    public void addEquip(int equipItemId){
        if(!equips.contains(equipItemId)){
            equips.add(equipItemId);
        }
    }
    /**
     * 获取装备
     */
    public List<Integer> getEquips(){
        List<Integer> result = new ArrayList<Integer>();
        for(int i = 0; i < equips.size(); i++){
            result.add(equips.get(i));
        }
        return result;
    }
    /**
     * 清除所有装备
     */
    public void clearEquips(){
        equips.clear();
    }
}
