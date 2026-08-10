package peony.game;

public class ActivePowerDayListener implements DayListener {

	public void dayChanged() {
		for(Player p:ObjectAccessor.players.values()){
			p.checkActivePower();
		}
	}

}
