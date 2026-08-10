package peony.game.instance;

import java.text.MessageFormat;

import peony.game.NoEnoughValueException;
import peony.game.Player;
import peony.game.VMapException;
import ch.javasoft.util.IntArray;

public class NormalInstanceDefinition {
	public String name;
	public int maxPlayer;
	public int minLevel;
	public int refreshSecond;
	public IntArray mapIds = new IntArray();
	public int outMapId,outX,outY;
	public int id;
	public int maxTimes; // 进入副本的最大次数
	public int usePower; // 每次进入副本消耗行动力
	
	public NormalInstanceDefinition(String name,int id,int maxPlayer,int minLevel,int refreshSecond,int maxTimes,int power){
		this.name = name;
		this.id = id;
		this.maxPlayer = maxPlayer;
		this.minLevel = minLevel;
		this.refreshSecond = refreshSecond;
		this.maxTimes = maxTimes;
		this.usePower = power;
	}
	
	public void addMapId(int mapId){
		mapIds.add(mapId);
	}
	
	public void checkEnter(Player p, int oldTimes) throws VMapException {
		if (oldTimes >= maxTimes) {
			throw new VMapException(MessageFormat.format("此副本一天只能進入{0}次", maxTimes));
		}
		if (usePower > 0) {
			try {
				p.decActivePower(usePower);
			} catch (NoEnoughValueException e) {
				throw new VMapException(MessageFormat.format("進入此副本需要{0}行動力", usePower));
			}
		}
	}
}
