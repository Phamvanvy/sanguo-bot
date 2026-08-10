package peony.game.changed;

public class PlayerEnemyChangeToken extends ChangedItem{
	
	protected int enemyId;
	protected byte together;

	public PlayerEnemyChangeToken(int enemyId,byte together) {
		super(TYPE_COMPLEX, ChangedItem.ENEMY_IN_NUTURAL, false);
		this.enemyId = enemyId;
		this.together = together;
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
