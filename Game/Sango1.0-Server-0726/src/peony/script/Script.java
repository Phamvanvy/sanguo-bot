package peony.script;

import java.util.ArrayList;
import java.util.List;

public class Script {
	
	protected int id;
	protected List<Trigger> triggers = new ArrayList<Trigger>();
	protected List<Field> vars = new ArrayList<Field>();
	protected ExpressionList finishCondition = null;
	
	public Script(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public void setFinishCondition(ExpressionList postCondition){
		this.finishCondition = postCondition;
	}
	
	public void addTrigger(Trigger trigger){
		triggers.add(trigger);
	}
	
	public void addField(String varName){
		int index = vars.size();
		Field field = new Field(index,varName);
		vars.add(field);
	}
	
	public Trigger[] getTriggers(){
		Trigger[] ret = new Trigger[triggers.size()];
		triggers.toArray(ret);
		return ret;
	}
	
	public Field[] getFields(){
		Field[] ret = new Field[vars.size()];
		vars.toArray(ret);
		return ret;
	}
	
	public Field getField(int index){
		return vars.get(index);
	}
	
	public int getFieldIndex(String name){
		for(int i=0;i<vars.size();i++){
			Field f = vars.get(i);
			if(f.name.equals(name))
				return f.id;
		}
		return -1;
	}
	
	public int getFieldIndexFromV(String name){
		String s = name.substring(1);
		return Integer.parseInt(s);
	}
	
	public void run(Context ctx){
		for(Trigger trigger:triggers){
			trigger.run(ctx);
		}
	}
	
	public int runFinishCondition(Context ctx){
		if(finishCondition!=null){
			return finishCondition.getValue(ctx).intValue();
		}
		return 0;
	}
	
	public ExpressionList getFinishCondition(){
		return finishCondition;
	}
}
