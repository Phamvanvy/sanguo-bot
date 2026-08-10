package peony.game.exchange;

import java.util.ArrayList;
import java.util.List;

import peony.game.Gain;
import peony.game.GameItem;

public class ExchangeBag {
	
	public List<ExchangeGrid> grids;

	public ExchangeBag(int size){
		grids = new ArrayList<ExchangeGrid>(size);
		for(int i=0;i<size;i++){
			grids.add(new ExchangeGrid());
		}
	}
	
	public boolean addItem(GameItem item, int count) {
		int all = count;
		List<ExchangeGrid> gs = new ArrayList<ExchangeGrid>(grids.size());
		boolean ret = false;
		for (ExchangeGrid grid : grids) {
			int v = grid.addGameItem(item, all);
			if(v!=0){
				gs.add(grid);
			}
			all -= v;
			if (all == 0){
				ret = true;
				break;
			}
		}
		for(ExchangeGrid grid:grids){
			grid.complete(ret);
		}
		return ret;
	}
	
	public ExchangeGrid getGrid(int gridId){
		return grids.get(gridId);
	}
	
	public ExchangeGrid removeGrid(int gridId){
		ExchangeGrid eg = grids.get(gridId);
		ExchangeGrid g = new ExchangeGrid();
		g.item = eg.item;
		g.count = eg.count;
		eg.item = null;
		eg.count = 0;
		return g;
	}
	
	public int getSize(){
		return grids.size();
	}
	
	public void restoreToGain(Gain gain){
		for(ExchangeGrid g:grids){
			if(!g.isEmpty()){
				gain.addGainItem(g.item, g.count);
			}
		}
	}
}
