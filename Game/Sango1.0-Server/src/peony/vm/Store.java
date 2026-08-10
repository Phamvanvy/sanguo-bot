package peony.vm;

import java.util.HashMap;
import java.util.Map;

import peony.script.Field;
import peony.script.Script;
import peony.script.Value;


public class Store {
	protected int id;
	protected Map<String,Value> values = new HashMap<String,Value>();
	
	public Store(Script script){
		this.id = script.getId();
		Field[] vars = script.getFields();
		for(Field f:vars){
			values.put(f.getName(), new Value(0));
		}
	}
	
	public int getId(){
		return id;
	}
	
	public int getIntValue(String var){
		return values.get(var).intValue();
	}
	
	public Value set(String var,int value){
		Value v = new Value(value);
		values.put(var,v);
		return v;
	}
	
	public Value add(String var,int value){
		Value v = get(var);
		int iv = v.intValue() + value;
		return set(var,iv);
	}
	
	public Value add(String var,int value,int maxValue){
		Value v = get(var);
		int iv = Math.min(v.intValue()+value, maxValue);
		return set(var,iv);
	}
	
	public Value dec(String var,int value){
		Value v = get(var);
		int iv = v.intValue() - value;
		return set(var,iv);
	}
	
	public Value get(String var){
		return values.get(var);
	}
}
