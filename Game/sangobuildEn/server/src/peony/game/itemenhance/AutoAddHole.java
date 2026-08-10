package peony.game.itemenhance;

import java.util.Random;

import peony.game.GameItem;

public class AutoAddHole {
	
	public int serial;
	
	public GameItem gameItem;
	public int wantHole;
	public int destHole;
	
	public int realHoles;
	public int useBannerAccount;
	public int decMoney;
	
	public static Random rand = new Random();
	
	public AutoAddHole(GameItem gameItem, int wantHole, int destHole, int serial) {
		this.gameItem = gameItem;
		this.wantHole = wantHole;
		this.destHole = destHole;
		this.serial = serial;
	}
	
}
