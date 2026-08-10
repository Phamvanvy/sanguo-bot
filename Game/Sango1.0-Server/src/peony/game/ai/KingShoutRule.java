package peony.game.ai;

import java.text.MessageFormat;

import peony.game.Server;
import peony.game.Time;

/**
 * 国王喊话规则
 * 
 * @author Jeffrey
 * 
 */
public class KingShoutRule implements AIRule {

	protected float mpratio = 1.0f;
	protected boolean started = false;
	protected int lastStartShoutTime = -10 * 60 * 1000;

	private StateCreatureAI ai;

	
	public KingShoutRule(StateCreatureAI ai) {
		this.ai = ai;
	}

	public void update() {
		if (ai.creature.getThreatCount() == 0) {
			mpratio = 1.0f;
			started = false;
			lastStartShoutTime = -10 * 60 * 1000;
		} else {
			if (!started) {
				started = true;
				if ((Time.currTime - lastStartShoutTime) > 2 * 60 * 1000) {
					lastStartShoutTime = Time.currTime;
					String kingName = ai.creature.name.substring(0, 2);
					if(kingName.contains("曹操")){
						kingName = kingName.replace("操", "孟德");
					}
//					Server.server.getServiceRegistry().getChatService()
//							.sendFactionSystemMessage(
//									ai.creature.faction,
//									MessageFormat.format(peony.Messages.STRING_01057,
//											ai.creature.getFactionName(),kingName));
					Server.server.getServiceRegistry().getChatService()
						.sendWorldMessage(MessageFormat.format(peony.Messages.STRING_01057,ai.creature.getFactionName(),kingName));
				}
			} else {
				float ratio = ai.creature.hp*1.0f / ai.creature.maxhp;
				if(ratio<(mpratio-0.1f)){
					mpratio = mpratio-0.1f;
					Server.server.getServiceRegistry().getChatService()
					.sendFactionSystemMessage(
							ai.creature.faction,
							MessageFormat.format(peony.Messages.STRING_01058,
									ai.creature.getFactionName()));
				}
			}
		}
	}
	
	public void init(){
		mpratio = 1.0f;
		started = false;
	}
}
