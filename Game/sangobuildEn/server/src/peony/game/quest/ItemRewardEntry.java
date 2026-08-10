package peony.game.quest;

import peony.game.Gain;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;

public class ItemRewardEntry implements QuestRewardEntry {

	protected ItemTemplate template;
	protected int count;
	
	public ItemRewardEntry(ItemTemplate template,int count){
		this.template = template;
		this.count = count;
	}
	
	public void gain(Gain gain) {
		if(!template.newInstance){
			GameItem item = ObjectAccessor.createGameItem(template, -1);
			if(item!=null){
				gain.addGainItem(item,count);
			}
		}else{
			for(int i=0;i<count;i++){
				GameItem item = ObjectAccessor.createGameItem(template, -1);
				if(item!=null){
					gain.addGainItem(item,1);
				}
			}
		}
	}

}
