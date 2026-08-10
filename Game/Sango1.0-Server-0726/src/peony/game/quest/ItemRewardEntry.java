package peony.game.quest;

import peony.game.Gain;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;
import peony.game.Server;
import peony.service.QuestRewardService;

public class ItemRewardEntry implements QuestRewardEntry {

	protected ItemTemplate template;
	protected int count;
	protected int questId;
	
	public ItemRewardEntry(ItemTemplate template,int count,int questId){
		this.template = template;
		this.count = count;
		this.questId = questId;
	}
	
	public void gain(Gain gain) {
		QuestRewardService service = Server.server.getServiceRegistry().getQuestRewardService();
		if(!template.newInstance){
			GameItem item = ObjectAccessor.createGameItem(template, -1);
			if(service.isSpecialItem(questId,template.id))
				item = service.getGameItem(questId, template.id);
			if(item!=null){
				gain.addGainItem(item,count);
			}
		}else{
			for(int i=0;i<count;i++){
				GameItem item = ObjectAccessor.createGameItem(template, -1);
				if(service.isSpecialItem(questId,template.id))
					item = service.getGameItem(questId, template.id);
				if(item!=null){
					gain.addGainItem(item,1);
				}
			}
		}
	}

}
