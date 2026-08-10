package peony.service.quest;

import java.util.List;
import peony.game.Creature;
import peony.game.CreatureAI;
import peony.game.GameObject;
import peony.game.MapPoint;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;

public class PlayerConvoyAI implements CreatureAI {

	public State state;

	public int createTime, runningTime;

	protected PlayerConvoy convoy;

	protected Creature creature;
	
	public static int creatureWaitTime = 10 * 1000;
	public static int creatureMoveTime = 2 * 1000;
	
	public static int convoyTimes = 30 * 60 * 1000;	//押镖时间30分钟
	
	public boolean isLeave;	//是否离镖车30码
	
	public PlayerConvoyAI(PlayerConvoy convoy, Creature creature, int index) {
		this.convoy = convoy;
		this.creature = creature;
		this.isLeave = false;
		this.state = new InitState(Time.currTime, index);
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
		Server.server.getServiceRegistry().getEscortQuestService().success(
				convoy);
	}

	interface State {
		void update();
	}

	class InitState implements State {
		
		int createTime;
		int cycle;
		int indexInit;
		public InitState(int createTime, int index) {
			this.createTime = createTime;
			this.indexInit = index;
		}

		public void update() {
			if ((Time.currTime - createTime) >= creatureWaitTime) {
				PlayerConvoyAI.this.state = new RunningState(Time.currTime, indexInit);
			}
		}
	}
	
	class RunningState implements State {

		int runningTime;
		int index;
		int cycle;
		int stopTime;

		public RunningState(int runningTime, int index) {
			this.runningTime = runningTime;
			this.index = index;
		}

		public void update() {
			Player player = ObjectAccessor.getPlayer(convoy.playerId);
			if(Time.currTime - convoy.beginTime > convoyTimes){	//押镖3个小时没有完成
				convoy.escortDieCount = 0;
				Server.server.getServiceRegistry().getEscortQuestService().fail(convoy, null);
			}
			cycle++;
			if(player != null){	//在线
				PlayerConvoyDef def = convoy.def;
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
					}
					
					if(player.inRange(creature, 5*40)){	//玩家在镖车范围内
						if (Time.currTime - stopTime > creatureMoveTime) {
							MapPoint nextPoint = def.getMapPoint(index + 1);
							if (nextPoint.mapId != point.mapId) {
								creature.goMap(nextPoint.mapId, nextPoint.x,
										nextPoint.y);
								creature.go(true);
							} else {
								creature.speed = creature.patrolSpeed;
								creature.go(true);
								creature.setNextPoint(nextPoint.x, nextPoint.y);
							}
							index++;
							convoy.escortPointIndex = index;
							stopTime = 0;
						}
					}else{
						stopTime = Time.currTime;
						//creature.stop();
					}
				}
				if (cycle % 5 == 0) { // 每5个cycle检查一次
					if (cycle % 10 == 0)
						creature.moveType |= GameObject.MOVE_RUNNING_STATE;
				}
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
