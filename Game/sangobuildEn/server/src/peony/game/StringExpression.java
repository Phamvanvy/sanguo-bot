package peony.game;

import java.util.ArrayList;
import java.util.List;

public class StringExpression {
	protected List<StringExpressionNode> nodes = new ArrayList<StringExpressionNode>();
	
	public StringExpression(){
		
	}
	
	public void addNode(StringExpressionNode node){
		nodes.add(node);
	}
	
	public List<StringExpressionNode> getNodes(){
		return nodes;
	}
}
