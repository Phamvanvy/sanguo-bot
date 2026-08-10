package peony.patchs;

import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;

public class SuiteEffectPatch implements Runnable{

	int[] suiteTemplates = {1007792,1007793,1007794,1007795,1007802,1007803,1007807};
	
	public void run(){
		for(int i=0;i<suiteTemplates.length;i++) {
			ItemTemplate template = ObjectAccessor.getItemTemplate(suiteTemplates[i]);
			if(template.equipment != null) {
				template.equipment.suiteEffects = ObjectAccessor.suites.get(79);
				System.out.println(suiteTemplates[i] + ":ok");
			}
		}
	}
}
