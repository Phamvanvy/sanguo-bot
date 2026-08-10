package peony.game.changed;

import peony.game.Horse;

public class HorseIntPropertyChangedItem extends HorseChangedItem {
	protected int value;
	protected boolean overwrite;

	public HorseIntPropertyChangedItem(Horse horse, int id, int value,
			boolean notify) {
		this(horse, id, value, notify, notify);
	}

	/**
	 * 
	 * @param id
	 * @param value
	 * @param notify
	 * @param overwrite merge策略是覆盖还是添加
	 */
	public HorseIntPropertyChangedItem(Horse horse, int id, int value,
			boolean notify, boolean overwrite) {
		super(horse, TYPE_HORSE_INT, id, notify);
		this.value = value;
		this.overwrite = overwrite;
	}

	@Override
	public boolean merge(ChangedItem other) {
		if (other instanceof HorseIntPropertyChangedItem) {
			HorseChangedItem otherChange = (HorseChangedItem)other;
			if (notify == other.notify && id == other.id && otherChange.horseId==horseId) {
				if (overwrite) {
					value = ((HorseIntPropertyChangedItem) other).value;
				} else {
					value += ((HorseIntPropertyChangedItem) other).value;
				}
				return true;
			} else {
				return false;
			}
		} else
			return false;
	}

	@Override
	public void accept(ChangedItemVisitor visitor) {
		visitor.visit(this);
	}
}
