package peony.patchs;

import java.text.MessageFormat;

import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.suite.SuiteEffect;

public class SuitePatch implements Runnable {

	public void run() {
		GameItem item = ObjectAccessor.createGameItem(1007841);
		if(item!=null && item.template.isEquipment()){
			SuiteEffect[] effects2 = item.template.equipment.suiteEffects.getEffects();
			for (SuiteEffect effect : effects2) {
				int effectCount = effect.getCount();
				System.out.println(MessageFormat.format(peony.Messages.STRING_01110, effectCount,effect.buff.getDesc()));
			}
		}
	}

}
