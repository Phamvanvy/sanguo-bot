package peony.game.convoy;

import java.util.List;
import peony.game.Creature;
import peony.game.CreatureAI;
import peony.game.GameObject;
import peony.game.MapPoint;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;

public class ConvoyAI implements CreatureAI {

	public State state;

	public int createTime, runningTime;

	protected NationConvoy convoy;

	protected Creature creature;
	
	public static int creatureWaitTime = 5 * 60 * 1000;
	
	public static int creatureMoveTime = 10 * 1000;

	public ConvoyAI(NationConvoy convoy, Creature creature) {
		this.convoy = convoy;
		this.state = new InitState(Time.currTime);
		this.creature = creature;
	}

	public void backState() {

	}

	public boolean canOutOfBattle() {
		return false;
	}

	public void init() {

	}

	public void update() {
		state.update();
	}

	public void success() {
		Server.server.getServiceRegistry().getNationConvoyService().success(
				convoy);
	}

	interface State {
		void update();
	}

	class InitState implements State {
		
		int createTime;
		int cycle;

		public InitState(int createTime) {
			this.createTime = createTime;
		}

		public void update() {
			if ((Time.currTime - createTime) >= creatureWaitTime) {
				ConvoyAI.this.state = new RunningState(Time.currTime);
			}
			if (cycle % 5 == 0) {
				checkSourceAndDestPlayers();
			}
		}
	}
	
	protected void checkSourceAndDestPlayers(){
		VMap map = creature.getVMap();
		List<Player> players = map.getPlayersInRange(creature, 15 * 8); // 15码内的玩家加入到列表
		for (Player p : players) {
			if (p.faction == convoy.def.faction) {
				convoy.sourceRefs.add(p.ref());
			} else {
				convoy.destRefs.add(p.ref());
			}
		}
	}

	class RunningState implements State {

		int runningTime;
		int index;
		int cycle;
		int stopTime;

		public RunningState(int runningTime) {
			this.runningTime = runningTime;
		}

		public void update() {
			cycle++;
			NationConvoyDef def = ConvoyAI.this.convoy.def;
			MapPoint point = def.getMapPoint(index);
			if (point.equals(creature.getVMap().getId(), creature.x, creature.y)) {
				if (def.size() - 1 == index) {
					success();
					state = new EndState(Time.currTime);
					convoy.endTime = Time.currTime;
					return;
				}
				if (stopTime == 0) {
					stopTime = Time.currTime;
					creature.stop();
				}
				if (Time.currTime - stopTime > creatureMoveTime) {
					MapPoint nextPoint = def.getMapPoint(index + 1);
					if (nextPoint.mapId != point.mapId) {
						creature.goMap(nextPoint.mapId, nextPoint.x,
								nextPoint.y);
					} else {
						creature.speed = creature.patrolSpeed;
						creature.go(true);
						creature.setNextPoint(nextPoint.x, nextPoint.y);
					}
					index++;
					stopTime = 0;
				}
			}
			if (cycle % 5 == 0) { // 每5个cycle检查一次
				checkSourceAndDestPlayers();
				if (cycle % 10 == 0)
					creature.moveType |= GameObject.MOVE_RUNNING_STATE;
			}
		}
	}

	class EndState implements State {

		int endTime;

		public EndState(int endTime) {
			this.endTime = endTime;
		}

		public void update() {
			creature.removeFromWorld();
		}
	}
}
