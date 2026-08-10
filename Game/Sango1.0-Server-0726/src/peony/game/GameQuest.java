package peony.game;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import peony.game.quest.CreditRewardEntry;
import peony.game.quest.ExpRewardEntry;
import peony.game.quest.HonorRewardEntry;
import peony.game.quest.ItemRewardEntry;
import peony.game.quest.MoneyRewardEntry;
import peony.script.Expression;
import peony.script.ExpressionList;
import peony.script.Parser;
import peony.script.Script;
import peony.script.Trigger;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.QuestRewardItem;
import com.pip.sanguo.data.quest.QuestRewardSet;
import com.pip.sanguo.data.quest.QuestTarget;
import com.pip.sanguo.data.quest.QuestTrigger;
import com.pip.sanguo.data.quest.QuestVariable;

public class GameQuest {
	
	protected Quest quest;
	public QuestInfo questInfo;
	protected String className;
	
	protected ExpressionList preCondition;
	protected Script script;
	protected ExpressionList finishCondition;
	protected StringExpression descExpression,preDescExpression,postDescExpression,unFinishDescExpression;
	protected QuestReward reward;
	protected int faction;
	protected boolean isActive;
	
	protected CycleInfo cycleInfo;//跑环任务信息，如果不是跑环任务，那么为null
	
	
	
	private static final Logger log = Logger.getLogger(GameQuest.class);
	
	public GameQuest(Quest quest, QuestInfo questInfo) throws IOException {
		this.quest = quest;
		this.questInfo = questInfo;
		if (quest.condition != null) {
//			log.debug(String.format("quest:%d condition:%s", quest.getID(),quest.condition));
			Parser parser = new Parser(new StringReader(quest.condition));
			preCondition = parser.expressionList();
		}
		script = new Script(quest.getID());
		for(QuestVariable var:questInfo.variables){
			script.addField(var.name);
		}
		if(quest.finishCondition!=null){
//			log.debug(String.format("quest:%d finishCondition:%s", quest.getID(),quest.finishCondition));
			Parser parser = new Parser(new StringReader(quest.finishCondition));
			finishCondition = parser.expressionList();
			if(finishCondition!=null)
				script.setFinishCondition(finishCondition);
		}
		for(QuestTrigger trigger:questInfo.getServerTriggers()){
//			log.debug(String.format("quest:%d triggerCondition:%s", quest.getID(),trigger.condition));
			Parser cParser = new Parser(new StringReader(trigger.condition));
			ExpressionList condition = cParser.expressionList();
			condition.setType(ExpressionList.CONDITIONS);
//			log.debug(String.format("quest:%d triggerAction:%s", quest.getID(),trigger.action));
			Parser aParser = new Parser(new StringReader(trigger.action));
			ExpressionList action = aParser.expressionList();
			action.setType(ExpressionList.ACTIONS);
			Trigger t = new Trigger(condition,action);
			script.addTrigger(t);
		}
		descExpression = getStringExpression(quest.description);
		preDescExpression = getStringExpression(quest.preDescription);
		postDescExpression = getStringExpression(quest.postDescription);
		unFinishDescExpression = getStringExpression(quest.unfinishDescription);
		reward = new QuestReward(quest.id);
		List<QuestRewardSet> ls = getRewardSets();
		for(QuestRewardSet rewardSet:ls){
			QuestRewardBranch branch = new QuestRewardBranch(rewardSet.id);
			List<QuestRewardItem> items = rewardSet.rewardItems;
			for(QuestRewardItem item:items){
				if(item.rewardType==QuestRewardItem.REWARD_ITEM){
					ItemTemplate template = ObjectAccessor.getItemTemplate(item.rewardValue);
					assert template!=null;
					branch.addQuestRewardEntry(new ItemRewardEntry(template,item.itemCount,getId()));
				}else if(item.rewardType==QuestRewardItem.REWARD_MONEY){
					branch.addQuestRewardEntry(new MoneyRewardEntry(item.rewardValue));
				}else if(item.rewardType==QuestRewardItem.REWARD_EXP){
					branch.addQuestRewardEntry(new ExpRewardEntry(this, item.rewardValue));
				}else if(item.rewardType==QuestRewardItem.REWARD_HONOR){
					branch.addQuestRewardEntry(new CreditRewardEntry(item.rewardValue));
				}else if(item.rewardType==QuestRewardItem.REWARD_CREDIT){
					branch.addQuestRewardEntry(new HonorRewardEntry(item.rewardValue));
				}
			}
			reward.addQuestRewardBranch(branch);
		}
		if(quest.startNPC==-1){
			faction = -1;
		}else{
			GameMapNPC npc = (GameMapNPC)GameMapObject.findByID(Server.server.getServiceRegistry().getDataService().data, quest.startNPC);
			faction = npc.faction.id;
		}
		this.isActive = quest.active;
	}
	
	public void setCycleInfo(CycleInfo cycleInfo){
		this.cycleInfo = cycleInfo;
	}
	
	public CycleInfo getCycleInfo(){
		return this.cycleInfo;
	}
	
	
	public void setFaction(int faction){
		this.faction = faction;
	}
	
	public int getFaction(){
		return faction;
	}
	
	public String getClassName() {
	    return className;
	}
	
	public void setClassName(String s) {
	    className = s;
	}
	
	public QuestReward getReward(){
		return reward;
	}
    /**
     * 重复类型：0 - 不可重复、1 - 每月可完成1次、2 - 每周可完成1次、3 - 每天可完成1次、4 - 无限重复
     */
	public int getRepeatType(){
		return quest.repeatType;
	}
	
	protected StringExpression getStringExpression(String s) throws IOException{
		Node[] nodes = parserStringExpression(s);
		StringExpression ret = new StringExpression();
		for(Node node:nodes){
			if(node.type==Node.TYPE_STRING){
				ret.addNode(new StringNode(node.value));
			}
			else if(node.type==Node.TYPE_EXPRESSION){
				Parser parser = new Parser(new StringReader(node.value));
				Expression exp = parser.expression();
				ret.addNode(new ExpressionNode(exp));
			}
			else
				throw new IllegalArgumentException();
		}
		return ret;
	}

	public boolean isAreaQuest() {
		return quest.areaID!=-1;
	}
	
	public int getAreaId(){
		return quest.areaID;
	}

	public int getId() {
		return quest.id;
	}
	
	public String getName(){
		return quest.title;
	}

	public StringExpression getDesc(){
		return descExpression;
	}
	
	public int getStartNpc() {
		return quest.startNPC;
	}

	public int getFinishNpc() {
		return quest.finishNPC;
	}

	public ExpressionList getPreCondition() {
		return preCondition;
	}

	public Script getScript() {
		return script;
	}
	
	public List<QuestTarget> getTargets(){
		return quest.targets;
	}
	
	public List<QuestRewardSet> getRewardSets(){
		return quest.rewards;
	}
	
	public StringExpression getPreDesc(){
		return preDescExpression;
	}
	
	public StringExpression getPostDesc(){
		return postDescExpression;
	}
	
	public StringExpression getUnFinishDesc(){
		return unFinishDescExpression;
	}
	
	public byte[] getClientETF(){
		return questInfo.getClientETF();
	}
	
	public boolean isNotifyFinish(){
		return quest.notifyFinish;
	}

	public int getRequireFreeBag(){
		return quest.requireFreeBag;
	}
	
	public boolean isClientModificationAllowed(int index){
		return questInfo.isClientModificationAllowed(index);
	}
	
	public int getLevel(){
		return quest.level;
	}
	public boolean getIsActive(){
		return isActive;
	}
	public void openQuest(){
		this.isActive = true;
	}
	public void closeQuest(){
		this.isActive = false;
	}
	public static Node[] parserStringExpression(String desc){
		if(desc.length()==0)
			return new Node[]{new Node(Node.TYPE_STRING,"")};
		List<Node> l = new ArrayList<Node>(20);
		while(true){
			int begin = desc.indexOf("${");
			if(begin!=-1){
				int end = desc.indexOf("}");
				if(0<begin){
					l.add(new Node(Node.TYPE_STRING,desc.substring(0,begin)));
					l.add(new Node(Node.TYPE_EXPRESSION,desc.substring(begin+2,end)));
				}else{
					l.add(new Node(Node.TYPE_EXPRESSION,desc.substring(begin+2,end)));
				}
				if(end==l.size()-1)
					break;
				else{
					desc = desc.substring(end+1);
				}
			}else{
				if(desc.length()>0)
					l.add(new Node(Node.TYPE_STRING,desc));
				break;
			}
			
		}
		Node[] ret = new Node[l.size()];
		l.toArray(ret);
		return ret;
	}
	public static class Node{
		public static final int TYPE_STRING = 0;
		public static final int TYPE_EXPRESSION = 1;
		
		public int type;
		public String value;

		public Node(int type,String value){
			this.type = type;
			this.value = value;
		}
		
		@Override
		public String toString(){
			return type+":"+value;
		}
	}
	
	/**
	 * 跑环信息
	 * @author Jeffrey
	 *
	 */
	public static class CycleInfo{
		public final static int TYPE_NODE = 0;
		public final static int TYPE_HEAD = 1;
		public final static int TYPE_TAIL = 2;
		
		public int type; //类型
		public int cycle; //环数
		public int level; //最低可接等级
		public int index; //第N环，从1开始
		public int questId;
		public int nextQuestId;
		
		public CycleInfo(int type,int cycle,int level,int index,int questId,int nextQuestId){
			this.type = type;
			this.cycle = cycle;
			this.level = level;
			this.index = index;
			this.questId = questId;
			this.nextQuestId = nextQuestId;
		}
	}
}

