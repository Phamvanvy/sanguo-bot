package com.pip.itimes.server.suit;

import java.util.HashMap;
import java.util.Vector;

public class Suit{
    private int id;
    private String name;
    private int color;
    private Vector<SuitEffect> effects = new Vector<SuitEffect>();
    public Vector<SuitEffect> getEffects() {
		return effects;
	}

	private Vector<Integer> equips = new Vector<Integer>();
    
	private HashMap<String, Integer> equipsName = new HashMap<String, Integer>();
	
	public void addEquipName(String equipName, int itemId){
		if(!equipsName.containsKey(equipName)){
			equipsName.put(equipName, itemId);
		}
		
	}
	public HashMap getEquipsName(){
		return equipsName;
		
	}
	
    public void setId(int id){
        this.id = id;
    }
    
    public int getId(){
        return id;
    }

    public int getCount(){
        return equips.size();
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
    
    public int getColor(){
        return color;
    }
    
    public void setColor(int color){
        this.color = color;
    }
    
    public void addEffect(SuitEffect suitEffect){
        effects.add(suitEffect);
    }
    
    public void clearEffects(){
        effects.clear();
    }
    
    public void addEquip(int equipItemId){
        if(!equips.contains(equipItemId)){
            equips.add(equipItemId);
        }
    }
    
    public Vector<Integer> getEquips(){
        Vector<Integer> result = new Vector<Integer>();
        
        for(int i = 0; i < equips.size(); i++){
            result.add(equips.get(i));
        }
        
        return result;
    }
    
    public void clearEquips(){
        equips.clear();
    }
    
    public SuitEffect[] getSuitEffect(int hasCount){
        Vector<SuitEffect> hasEffects = new Vector<SuitEffect>();
        
        for(int i = 0; i < effects.size(); i++){
            SuitEffect suitEffect = effects.elementAt(i);
            
            if(hasCount >= suitEffect.getCount()){
                hasEffects.add(suitEffect);
            }
        }
        
        if(hasEffects.size() == 0){
            return null;
        }else{
            SuitEffect[] result = new SuitEffect[hasEffects.size()];
            return hasEffects.toArray(result);
        }
    }
}
