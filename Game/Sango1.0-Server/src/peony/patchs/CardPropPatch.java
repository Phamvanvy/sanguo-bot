package peony.patchs;

import peony.service.cards.CardService;

public class CardPropPatch implements Runnable {

	public void run() {
		CardService.BLUE_ENHANCE_VALUES = new int[]{
				4,
				8,
				12,
				16,
				20,
				24,
				28,
				34,
				41,
				50,
				61,
				74};
	}

}
