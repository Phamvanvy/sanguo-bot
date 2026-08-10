package peony.game.exchange;

import peony.game.GameItem;

public class ExchangeGrid {
	public GameItem item;
	public int count;
	protected int wantAddCount;
	
	public int addGameItem(GameItem item, int count) {
		if (count <= 0)
			throw new IllegalArgumentException();
		if (this.item == null) {
			this.item = item;
		} else {
			if (!item.equals(this.item)) {
				return 0;
			}
		}
		int max = item.template.maxCount;
		int v = Math.min(max - this.count - wantAddCount, count);
		wantAddCount += v;
		return v;
	}
	
	public void complete(boolean commit){
		if(commit){
			this.count += this.wantAddCount;
			this.wantAddCount = 0;
		}else{
			this.wantAddCount = 0;
			if(this.count==0)
				this.item = null;
		}
	}
	
	public boolean isEmpty(){
		return item==null;
	}
}
