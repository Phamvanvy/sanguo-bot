package peony.game.suite;

import java.util.ArrayList;
import java.util.List;

public class SuiteEffects {
    protected int id;
	protected String name;
	protected SuiteEffect[] effects;
	protected List<Integer> equips = new ArrayList<Integer>(); // 没件套装包含的装备id的集合 
	
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
