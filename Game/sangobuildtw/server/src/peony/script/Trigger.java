package peony.script;


public class Trigger {
	
	protected ExpressionList conditions;
	protected ExpressionList actions;
	
	public Trigger(ExpressionList conditions,ExpressionList actions){
		this.conditions = conditions;
		this.actions = actions;
	}
	
	public ExpressionList getConditions(){
		return conditions;
	}
	
	public ExpressionList getActions(){
		return actions;
	}
	
	public void run(Context ctx){
		if(conditions.getValue(ctx).intValue()==1){
			actions.getValue(ctx);
		}
	}
}
