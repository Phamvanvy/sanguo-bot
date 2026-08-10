package peony.service.player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import peony.db.DBService;
import peony.db.PlayerDAO;
import peony.game.Actor;
import peony.game.Player;
import peony.game.Server;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 管理用户简略信息。
 * @author lighthu
 */
public class ActorCacheService implements Service, ServiceEventListener {
	protected Map<Integer, Actor> actors = new ConcurrentHashMap<Integer, Actor>();
	protected Map<String, Actor> actorNameMap = new ConcurrentHashMap<String, Actor>();

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}


	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_PLAYER_LEVELUP,
				ServiceEvent.EVENT_PLAYER_LOGINED,
				ServiceEvent.EVENT_PLAYER_LOGOUTED,
				ServiceEvent.EVENT_PLAYER_CHANGENAME,
				ServiceEvent.EVENT_PLAYER_CREATED,
				ServiceEvent.EVENT_PLAYER_CHANGE_FACTION,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_PLAYER_LEVELUP:
			playerLevelUp((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGINED:
			playerLogined((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			playerLogouted((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_CHANGENAME:
			playerChangeName((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_CREATED:
			playerCreated((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_CHANGE_FACTION:
			playerChangeFaction((Player)event.param1);
			break;
		}
	}
	
	private void playerChangeFaction(Player player){
		Actor actor = actors.get(player.id);
		if (actor != null) {
			actor.faction = player.faction;
		}
	}
	
	private void playerCreated(Player player){
		Actor actor = new Actor();
		actor = new Actor();
		actor.id = player.id;
		actor.name = player.name;
		actor.exist = 1;
		actor.online = false;
		actor.accountId = player.accountId;
		actor.sex = player.sex;
		actor.level = player.level;
		actor.clazz = player.clazz;
		actor.faction = player.faction;
		actors.put(actor.id, actor);
		actorNameMap.put(actor.name, actor);
	}
	
	/*
	 * 玩家升级时，更新Actor对象中的级别。
	 * @param player
	 */
	private void playerLevelUp(Player player) {
		Actor actor = actors.get(player.id);
		if (actor != null) {
			actor.level = player.level;
		}
	}
	
	/*
	 * 玩家登录时，更新Actor对象中的数据，或创建一个新的Actor对象。
	 * @param player
	 */
	private void playerLogined(Player player) {
		Actor actor = actors.get(player.id);
		if (actor == null) {
			actor = new Actor();
			actor.id = player.id;
			actor.name = player.name;
			actor.exist = 1;
			actors.put(actor.id, actor);
			actorNameMap.put(actor.name, actor);
		} else if (!actor.name.equals(player.name)) {
			// 发现名字不匹配，重新设置名字查找表
			actorNameMap.remove(actor.name);
			actor.name = player.name;
			actorNameMap.put(actor.name, actor);
		}
		actor.online = true;
		actor.accountId = player.accountId;
		actor.sex = player.sex;
		actor.level = player.level;
		actor.clazz = player.clazz;
		actor.faction = player.faction;
	}
	
	/*
	 * 玩家下线时，更新玩家在线状态。
	 * @param player
	 */
	private void playerLogouted(Player player) {
		Actor actor = actors.get(player.id);
		if (actor != null) {
			actor.online = false;
		}
	}
	
	/*
	 * 玩家改名时，更新数据。
	 * @param player
	 */
	private void playerChangeName(Player player) {
		Actor actor = actors.get(player.id);
		if (actor != null) {
			actor.online = false;
			actorNameMap.remove(actor.name);
			actor.name = player.name;
			actorNameMap.put(actor.name, actor);
		}
	}

	/**
	 * 根据ID查找一个玩家的信息。如果此玩家没有被载入过，则尝试从数据库载入。
	 * @param id
	 * @return
	 */
	public Actor find(int id) {
		// 先在缓存中查找
		Actor actor = actors.get(id);
		if (actor != null) {
			return actor;
		}
		
		// 从数据库载入
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		actor = dbs.playerDAO.getActor(id);
		if(actor==null)
			return null;
		actors.put(actor.id, actor);
		actorNameMap.put(actor.name, actor);
		return actor;
	}
	
	/**
	 * 根据名字查找一个玩家的信息。如果此玩家没有被载入过，则尝试从数据库载入。
	 * @param id
	 * @return
	 */
	public Actor find(String name) {
		// 先在缓存中查找
		Actor actor = actorNameMap.get(name);
		if (actor != null) {
			return actor;
		}
		
		// 从数据库载入
		DBService dbs = Server.server.getServiceRegistry().getDbService();
		actor = dbs.playerDAO.getActor(name);
		if(actor==null)
			return null;
		Actor oldActor = actors.get(actor.id);
		if (oldActor != null) {
			// 如果通过ID能查找到，但通过名字不能，则需要更新名字缓存
			actorNameMap.remove(oldActor.name);
			oldActor.name = actor.name;
			actorNameMap.remove(actor.name);
			
			// 如果旧的记录数据不完整，则补足之
			if (oldActor.level == -1) {
				oldActor.accountId = actor.accountId;
				oldActor.sex = actor.sex;
				oldActor.level = actor.level;
				oldActor.clazz = actor.clazz;
				oldActor.faction = actor.faction;
			}
			return oldActor;
		} else {
			// 缓存新查找出来的用户
			actors.put(actor.id, actor);
			actorNameMap.put(actor.name, actor);
			return actor;
		}
	}
	
	/**
	 * 根据ID查找一个玩家的信息。如果此玩家没有被载入过或者信息不完整，则尝试从数据库载入。
	 * @param id
	 * @return
	 */
	public Actor load(int id) {
		Actor actor = actors.get(id);
		if (actor == null || actor.level == -1) {
			DBService dbs = Server.server.getServiceRegistry().getDbService();
			actor = dbs.playerDAO.getActor(id);
			if (actor != null) {
				actors.put(id, actor);
				actorNameMap.put(actor.name, actor);
			}
		}
		return actor;
	}
	
	/**
	 * 根据名字查找一个玩家的信息。如果此玩家没有被载入过或者信息不完整，则尝试从数据库载入。
	 * @param id
	 * @return
	 */
	public Actor load(String name) {
		Actor actor = actorNameMap.get(name);
		if (actor == null || actor.level == -1) {
			DBService dbs = Server.server.getServiceRegistry().getDbService();
			actor = dbs.playerDAO.getActor(name);
			if (actor != null) {
				actors.put(actor.id, actor);
				actorNameMap.put(actor.name, actor);
			}
		}
		return actor;
	}
	
	/**
	 * 注册一个不完整的玩家信息对象。
	 * @param id 玩家ID
	 * @param name 玩家名称
	 * @return
	 */
	public Actor add(int id, String name) {
		Actor actor = actors.get(id);
		if (actor == null) {
			actor = new Actor();
			actor.id = id;
			actor.name = name;
			actor.online = false;
			actor.exist = 1;
			actors.put(id, actor);
		}
		return actor;
	}
	
	/**
	 * 载入一组玩家的信息。如果玩家已经被载入，并且信息完整，则不会重复载入。
	 * @param ids
	 */
	public void loadAll(int[] ids) {
		int[] tmparr = new int[ids.length];
		int count = 0;
		for (int i = ids.length - 1; i >= 0; i--) {
			Actor actor = actors.get(ids[i]);
			if (actor == null || actor.level == -1) {
				tmparr[count] = ids[i];
				count++;
			}
		}
		if (count > 0) {
			int[] tmparr2 = new int[count];
			System.arraycopy(tmparr, 0, tmparr2, 0, count);
			DBService dbs = Server.server.getServiceRegistry().getDbService();
			List<Actor> as = dbs.playerDAO.getActors(tmparr2);
			for (Actor actor : as) {
				if (!actors.containsKey(actor.id)) {
					actors.put(actor.id, actor);
				}
			}
		}
	}
	
	/**
	 * 根据角色名字找到数据库中所有此名字的玩家
	 */
	public List<Actor> fingActorsByName(String name){
		PlayerDAO dao = Server.server.getServiceRegistry().getDbService().playerDAO;
		return dao.getActorsByName(name);
	}
}
