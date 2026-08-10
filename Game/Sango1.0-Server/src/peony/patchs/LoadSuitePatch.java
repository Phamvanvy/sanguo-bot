package peony.patchs;

import java.util.List;
import peony.game.DataService;
import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;
import peony.game.Server;
import peony.game.buff.BuffUtil;
import peony.game.suite.SuiteEffect;
import peony.game.suite.SuiteEffects;
import com.pip.sanguo.data.equipment.SuiteConfig;

/**
 * hotfixÌ××°Ð§¹û
 * @author dchen
 */
public class LoadSuitePatch implements Runnable {

	public void run() {
		DataService dataService = Server.server.getServiceRegistry().getDataService();
		List suiteConfigs = dataService.data.getDataListByType(SuiteConfig.class);
		for(Object o:suiteConfigs) {
			SuiteConfig config = (SuiteConfig)o;
			SuiteEffect[] se = new SuiteEffect[config.effects.size()];
	        int i = 0;
	        for (com.pip.sanguo.data.equipment.SuiteConfig.SuiteEffect effect : config.effects) {
	            SuiteEffect effect2 = new SuiteEffect();
	            effect2.setBuff(BuffUtil.createSuiteBuff(effect.buffID, effect.buffLevel));
	            effect2.setCount(effect.count);
	            effect2.setBuffId(effect.buffID);
	            effect2.setBuffLevel(effect.buffLevel);
	            se[i] = effect2;
	            i++;
	        }
	        SuiteEffects effects = new SuiteEffects(config.id, config.title, se);
	        for (com.pip.sanguo.data.equipment.Equipment equ : config.equipments) {
	            ItemTemplate template = ObjectAccessor.getItemTemplate(equ.id);
	            template.equipment.suiteEffects = effects;
	            effects.addEquip(equ.id);
	        }
			ObjectAccessor.suites.put(effects.getID(), effects);
		}
	}

}
