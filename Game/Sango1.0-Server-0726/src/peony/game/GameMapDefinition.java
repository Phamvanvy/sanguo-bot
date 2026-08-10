package peony.game;

import com.pip.mapeditor.data.GameMap;
import com.pip.sanguo.data.map.GameMapInfo;

public class GameMapDefinition {
	
	public GameMapInfo mapInfo;
	public GameMap map;
	
	public GameMapDefinition(GameMapInfo mapInfo,GameMap map){
		this.mapInfo = mapInfo;
		this.map = map;
	}
}
