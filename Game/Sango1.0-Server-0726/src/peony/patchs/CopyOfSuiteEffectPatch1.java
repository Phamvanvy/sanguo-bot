package peony.patchs;

import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;

public class CopyOfSuiteEffectPatch1 implements Runnable{

	int[] suiteTemplates = {1007422,1007423,1007424,1007425,1007432,1007433,1007437};
	
	public void run(){
		for(int i=0;i<suiteTemplates.length;i++) {
			ItemTemplate template = ObjectAccessor.getItemTemplate(suiteTemplates[i]);
			if(template.equipment != null) {
				template.equipment.suiteEffects = ObjectAccessor.suites.get(30);
				System.out.println(suiteTemplates[i] + ":ok");
			}
		}
	}
}
