package peony.service.cards;

import peony.game.changed.ChangedItem;
import peony.game.changed.ChangedItemVisitor;

public class EquipCardChangedItem extends ChangedItem {

	public int index;
	public int upExp;
	public int type;
	public CardInfo cardInfo;
	
	protected EquipCardChangedItem(int index, int upExp, int type, CardInfo info) {
		super(ChangedItem.TYPE_COMPLEX,ChangedItem.CARD_EQUIP,false);
	}

	@Override
	public void accept(ChangedItemVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean merge(ChangedItem other) {
		return false;
	}

}
