package peony.game;

import java.util.Iterator;

public class MapCellIterator implements Iterator<MapCell> {

	protected VMap map;
	protected int startX,startY,endX,endY;
	protected int index,total;
	
	public MapCellIterator(VMap map){
		this.map = map;
		this.startX = -1;
		this.startY = -1;
		this.endX = -1;
		this.endY = -1;
	}
	
	public void init(int startX,int startY,int endX,int endY){
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.total = (endX - startX) * (endY - startY);
		this.index = 0;
	}
	
	public boolean hasNext() {
		return index < total;
	}

	public MapCell next() {
		int v = endY - startY;
		int x = index/v;
		int y = index%v;
		index++;
		return map.cells[startX+x][startY+y];
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
