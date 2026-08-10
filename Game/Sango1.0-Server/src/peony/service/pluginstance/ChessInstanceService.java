package peony.service.pluginstance;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.dom4j.Document;
import org.dom4j.Element;
import org.joda.time.MutableDateTime;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapExit;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.CommonUtil;
import peony.game.GameObject;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapUtil;
import peony.game.attendant.Attendant;
import peony.game.instance.NormalInstance;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.apprentice.ApprenticeService;
import peony.service.fiveelement.FiveElementService;
import peony.service.stat.StatService;

public class ChessInstanceService implements Service, ServiceEventListener {
	
	public static int MAPID = 2192;
	private static int MAX = 6;
	private static Random rand = new Random();
	public Map<Integer, int[][]> instance2Board = new HashMap<Integer, int[][]>();
	public Map<Integer, GameObject[][]> gameObject2Board = new HashMap<Integer, GameObject[][]>();
	public List<int[][]> boardList1 = new ArrayList<int[][]>();
	public List<int[][]> boardList2 = new ArrayList<int[][]>();
	//打黑棋连白棋样板
	public int[][] board10= {{8978433,8978434,8978434,8978433,8978434,8978434},{8978434,8978433,8978434,8978434,8978434,8978434},{8978434,8978434,8978434,8978433,8978433,8978433},{8978434,8978433,8978434,8978434,8978433,8978434},{8978434,8978433,8978433,8978434,8978434,8978434},{8978433,8978434,8978434,8978434,8978434,8978433}};
	public int[][] board11= {{8978434,8978434,8978433,8978433,8978433,8978433},{8978433,8978434,8978434,8978433,8978434,8978434},{8978433,8978433,8978433,8978434,8978433,8978433},{8978433,8978433,8978433,8978433,8978434,8978434},{8978434,8978433,8978433,8978434,8978434,8978433},{8978433,8978434,8978433,8978433,8978433,8978433}};
	public int[][] board12= {{8978434,8978434,8978434,8978434,8978433,8978433},{8978434,8978433,8978433,8978433,8978433,8978434},{8978434,8978433,8978434,8978434,8978433,8978433},{8978433,8978434,8978433,8978433,8978434,8978434},{8978434,8978433,8978433,8978434,8978434,8978433},{8978433,8978433,8978434,8978434,8978434,8978434}};
	public int[][] board13= {{8978434,8978433,8978434,8978433,8978433,8978434},{8978434,8978434,8978433,8978433,8978433,8978434},{8978434,8978434,8978433,8978433,8978433,8978433},{8978433,8978433,8978433,8978433,8978434,8978434},{8978434,8978433,8978433,8978434,8978434,8978434},{8978433,8978434,8978434,8978433,8978433,8978434}};
	public int[][] board14= {{8978433,8978434,8978434,8978434,8978433,8978433},{8978433,8978433,8978434,8978433,8978434,8978434},{8978434,8978433,8978433,8978433,8978433,8978434},{8978434,8978434,8978434,8978434,8978433,8978433},{8978433,8978434,8978434,8978434,8978433,8978434},{8978433,8978433,8978434,8978433,8978433,8978434}};
	public int[][] board15= {{8978434,8978433,8978434,8978433,8978433,8978433},{8978434,8978434,8978434,8978434,8978433,8978433},{8978433,8978433,8978433,8978433,8978434,8978433},{8978433,8978433,8978433,8978433,8978434,8978434},{8978433,8978434,8978433,8978433,8978434,8978434},{8978434,8978434,8978433,8978433,8978433,8978433}};
	public int[][] board16= {{8978433,8978434,8978433,8978434,8978433,8978433},{8978434,8978433,8978433,8978433,8978434,8978434},{8978433,8978433,8978433,8978434,8978433,8978433},{8978434,8978434,8978433,8978433,8978434,8978433},{8978434,8978433,8978434,8978434,8978434,8978434},{8978433,8978433,8978434,8978434,8978433,8978434}};
	public int[][] board17= {{8978434,8978434,8978434,8978433,8978433,8978434},{8978433,8978433,8978434,8978433,8978434,8978433},{8978433,8978434,8978434,8978433,8978434,8978434},{8978434,8978433,8978433,8978433,8978434,8978433},{8978433,8978434,8978434,8978434,8978434,8978433},{8978433,8978434,8978433,8978433,8978433,8978433}};
	public int[][] board18= {{8978433,8978433,8978434,8978434,8978434,8978433},{8978434,8978433,8978434,8978433,8978434,8978434},{8978434,8978434,8978434,8978434,8978433,8978433},{8978433,8978433,8978433,8978434,8978434,8978434},{8978433,8978433,8978433,8978434,8978433,8978433},{8978433,8978434,8978433,8978434,8978433,8978433}};
	public int[][] board19= {{8978433,8978433,8978434,8978434,8978434,8978433},{8978434,8978433,8978434,8978433,8978434,8978434},{8978434,8978434,8978434,8978434,8978433,8978433},{8978433,8978433,8978433,8978434,8978434,8978434},{8978433,8978433,8978433,8978434,8978433,8978433},{8978433,8978434,8978433,8978434,8978433,8978433}};
	//打白棋连黑棋样板
	public int[][] board20= {{8978436,8978435,8978435,8978435,8978435,8978436},{8978435,8978435,8978436,8978436,8978436,8978436},{8978436,8978435,8978436,8978435,8978435,8978436},{8978435,8978435,8978435,8978436,8978436,8978436},{8978436,8978436,8978435,8978436,8978435,8978435},{8978436,8978436,8978435,8978435,8978436,8978436}};
	public int[][] board21= {{8978436,8978436,8978436,8978436,8978435,8978435},{8978435,8978435,8978436,8978435,8978436,8978435},{8978435,8978436,8978436,8978435,8978436,8978435},{8978435,8978435,8978436,8978436,8978436,8978436},{8978436,8978436,8978435,8978436,8978435,8978435},{8978436,8978435,8978436,8978436,8978435,8978435}};
	public int[][] board22= {{8978435,8978436,8978435,8978435,8978436,8978435},{8978435,8978436,8978435,8978435,8978436,8978435},{8978436,8978436,8978435,8978435,8978436,8978436},{8978436,8978435,8978435,8978436,8978436,8978436},{8978435,8978436,8978436,8978436,8978435,8978436},{8978435,8978435,8978436,8978436,8978436,8978436}};
	public int[][] board23= {{8978436,8978436,8978435,8978435,8978436,8978436},{8978435,8978435,8978435,8978436,8978435,8978436},{8978435,8978436,8978435,8978435,8978435,8978436},{8978435,8978436,8978435,8978436,8978436,8978436},{8978436,8978436,8978436,8978436,8978435,8978435},{8978436,8978435,8978436,8978436,8978435,8978436}};
	public int[][] board24= {{8978435,8978435,8978435,8978435,8978436,8978435},{8978436,8978436,8978436,8978436,8978435,8978436},{8978436,8978435,8978435,8978436,8978436,8978436},{8978436,8978435,8978435,8978436,8978435,8978436},{8978435,8978435,8978435,8978435,8978436,8978435},{8978435,8978435,8978436,8978436,8978435,8978436}};
	public int[][] board25= {{8978435,8978435,8978435,8978435,8978436,8978435},{8978435,8978436,8978436,8978436,8978435,8978435},{8978436,8978436,8978436,8978435,8978435,8978435},{8978436,8978435,8978436,8978436,8978435,8978436},{8978436,8978436,8978435,8978435,8978435,8978436},{8978435,8978436,8978436,8978436,8978436,8978435}};
	public int[][] board26= {{8978436,8978436,8978435,8978435,8978435,8978436},{8978435,8978435,8978436,8978436,8978436,8978436},{8978436,8978436,8978436,8978435,8978436,8978436},{8978436,8978435,8978435,8978435,8978435,8978436},{8978436,8978436,8978435,8978435,8978436,8978435},{8978435,8978435,8978435,8978435,8978436,8978435}};
	public int[][] board27= {{8978435,8978436,8978435,8978435,8978435,8978435},{8978436,8978436,8978435,8978436,8978435,8978436},{8978436,8978436,8978435,8978436,8978436,8978436},{8978436,8978435,8978435,8978435,8978436,8978435},{8978436,8978436,8978436,8978435,8978435,8978436},{8978435,8978436,8978435,8978436,8978435,8978435}};
	public int[][] board28= {{8978436,8978435,8978435,8978435,8978436,8978435},{8978436,8978436,8978436,8978435,8978436,8978435},{8978436,8978436,8978435,8978436,8978436,8978436},{8978436,8978435,8978436,8978435,8978436,8978436},{8978435,8978436,8978435,8978436,8978435,8978435},{8978435,8978435,8978435,8978436,8978436,8978435}};
	public int[][] board29= {{8978436,8978435,8978436,8978436,8978435,8978435},{8978436,8978436,8978435,8978435,8978435,8978436},{8978435,8978436,8978436,8978435,8978435,8978436},{8978436,8978435,8978436,8978435,8978436,8978435},{8978436,8978436,8978435,8978435,8978436,8978436},{8978435,8978435,8978436,8978436,8978436,8978436}};
	Board[][] index2Board = new Board[MAX][MAX];
	private static int NPC_CREATURE[] = { 8978436, 8978433 }; //怪物NPCid,按白棋，黑棋排列
	private static int NPC_NEUTRO[] = { 8978434, 8978435 }; //中立NPCid,按白棋，黑棋排列
	public static int QUESTTYPE[][] = { { 0, 0 }, { 3475, 3472 },{ 3476, 3473 }, { 3477, 3474 } }; //任务id 按照白棋 黑棋排列
	public static int OUTMAP_CHESSQUEST[][] = { { 0, 0, 0 }, { 400, 335,108 },{ 528, 337,381 }, { 560, 485,329 } };
	public static int QUEST_NATIONDAY = 3319;
	public static int OUTMAP_NATIONDAY[] = {2032,850,1007};
	public static int QUEST_SHITU[] = {0,3485,3486,3487};
	public static int OUTMAP_SHITUQUEST[][] = { { 0, 0, 0 }, { 272, 880,410 },{ 240, 675, 245 }, { 352, 580, 745 } };
	protected static MutableDateTime cachedCal = new MutableDateTime();
	
	public static int[][] exits_chess = {{},{400,341,112},{528,330,383},{560,463,337}};
	public static int EXP_ADD = 4020; //每连珠增加的经验
	
	public static int[][] exits_apprentice = {{},{272,892,412},{240,699,236},{352,593,735}};
		
	public static int CHESSCOUNT_QUEST = 5;//五子棋任务连珠数值
	public static int CHESSCOUNT_APPRENTICE = 30;//师徒五子棋任务连珠数值
	public static int CHESSCOUNT_NATIONDAY = 30;//国庆活动五子棋任务连珠数值


	public static int[] getQuestArr(Player player) {
		return QUESTTYPE[player.faction];
	}
	
	public static int getShituQuest(Player player){
		return QUEST_SHITU[player.faction];
	}

	public int[] getOutMap(Player player) {
		return OUTMAP_CHESSQUEST[player.faction];
	}
	
	public int[] getShituOutMap(Player player) {
		return OUTMAP_SHITUQUEST[player.faction];
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("chessinstance.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		Server.server.getEventManager().registerListener(this);
		initBoard();
	}
	
	public void initBoard(){
		boardList1.add(board10);
		boardList1.add(board11);
		boardList1.add(board12);
		boardList1.add(board13);
		boardList1.add(board14);
		boardList1.add(board15);
		boardList1.add(board16);
		boardList1.add(board17);
		boardList1.add(board18);
		boardList1.add(board19);
		
		boardList2.add(board20);
		boardList2.add(board21);
		boardList2.add(board22);
		boardList2.add(board23);
		boardList2.add(board24);
		boardList2.add(board25);
		boardList2.add(board26);
		boardList2.add(board27);
		boardList2.add(board28);
		boardList2.add(board29);
	}
	
	public int[][] getRndBoard(Player player){
		int index = -1;
		if(player.chessType == 0){
			index = hasQuest(player);
		}else if(player.chessType == 1){
			index = hasNationDayQuest(player);
		}else if(player.chessType == 2){
			index = hasShituQuest(player);
		}
		int[][] BOARD = null;
		if(index!=-1){
			if(index == 0){
				int v = rand.nextInt(boardList1.size());
				BOARD = boardList1.get(v);
			}else if(index == 1){
				int v = rand.nextInt(boardList2.size());
				BOARD = boardList2.get(v);
			}
		}
		return BOARD;
	}

	@SuppressWarnings("unchecked")
	public void parse(Document doc) {
		Element root = doc.getRootElement();
		if (root != null) {
			List chess = root.elements("chess");
			for (int i = 0; i < chess.size(); i++) {
				int indexX = Integer.parseInt(((Element) chess.get(i))
						.attributeValue("indexX"));
				int indexY = Integer.parseInt(((Element) chess.get(i))
						.attributeValue("indexY"));
				int x = Integer.parseInt(((Element) chess.get(i))
						.attributeValue("x"));
				int y = Integer.parseInt(((Element) chess.get(i))
						.attributeValue("y"));
				Board board = new Board(indexX, indexY, x, y);
				index2Board[indexX][indexY] = board;
			}
		}
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_UNIT_DIE,
				ServiceEvent.EVENT_MAP_PLAYER_ADDED,
				ServiceEvent.EVENT_MAP_PLAYER_REMOVED};
	}

	public static int hasQuest(Player player) {
		int[] QUESTARR = getQuestArr(player);
		if (player.asmVm.hasTask(QUESTARR[0]) == 1) {
			return 0;
		} else if (player.asmVm.hasTask(QUESTARR[1]) == 1) {
			return 1;
		} else {
			return -1;
		}
	}
	
	public static int hasNationDayQuest(Player player) {
		if (player.asmVm.hasTask(QUEST_NATIONDAY) == 1) {
			return 0;
		}
        return -1;
	}
	
	public static int hasShituQuest(Player player) {
		int questId = getShituQuest(player);
		if (player.asmVm.hasTask(questId) == 1) {
			return 0;
		}
        return -1;
	}
	
	
	public static int accessQuest(Player player,int questId) {
		int[] QUESTARR = getQuestArr(player);
		if(StatService.isInArray(QUESTARR, questId)!=-1){
			if (player.asmVm.hasTask(QUESTARR[0]) == 1) {
				return 0;
			} else if (player.asmVm.hasTask(QUESTARR[1]) == 1) {
				return 1;
			} 
		}
		return -1;
	}
	

	public static int hasQuest2(Player player) {
		int[] QUESTARR = getQuestArr(player);
		if (player.asmVm.hasTask(QUESTARR[0]) == 1) {
			return QUESTARR[0];
		} else if (player.asmVm.hasTask(QUESTARR[1]) == 1) {
			return QUESTARR[1];
		} else {
			return -1;
		}
	}
	
	public int getAnother(int[] arr,int value){
		for(int i=0;i<arr.length;i++){
			if(arr[i]!=value){
				return arr[i];
			}
		}
		return -1;
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_UNIT_DIE:
			processUnitDie((Unit) event.param1, (Unit) event.param2);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
			processPlayerAddMap((VMap) event.param1, (Player) event.param2);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_REMOVED:
			processPlayerLeaveMap((VMap) event.param1, (Player) event.param2);
			break;
		}
	}
	
	/** 玩家离开副本时移除副本缓存数据 */
	public void processPlayerLeaveMap(VMap map,Player player){
		if(player != null && map != null){
			if(map.getMapID() == MAPID){
				NormalInstance instance = (NormalInstance) map.instance;
				
				if (instance != null) {
					if(instance2Board.containsKey(instance.id)){
						instance2Board.remove(instance.id);
					}
					if(gameObject2Board.containsKey(instance.id)){
						gameObject2Board.remove(instance.id);
					}
					//移除副本
					Server.server.getServiceRegistry().getNormalVMapManager()
					.clear(player.id);
				}
			}
		}
	}

	public void processPlayerAddMap(VMap map, Player player) {
		if (player != null && map != null) {
			if (map.getMapID() == MAPID) {
				NormalInstance instance = (NormalInstance) player.map.map.instance;
				if (instance != null) {
					try {
						int value = -1;
						player.chessCount = 0;
						if(player.chessType == 0){
							value = hasQuest(player);
						}else if(player.chessType == 1){
							value = hasNationDayQuest(player);
						}else if(player.chessType == 2){
							value = hasShituQuest(player);
						}
//						int value = hasQuest(player);
						if (value != -1) {
							for (GameObject go : map.instanceid2objects
									.values()) {
								if (go.type != GameObject.TYPE_PLAYER
										&& (StatService.isInArray(NPC_CREATURE,
												go.id) != -1 || StatService
												.isInArray(NPC_NEUTRO, go.id) != -1)) {
									go.removeFromWorld();
								}
							}
							int[][] b = getRndBoard(player);
							instance2Board.put(instance.id, b);
							GameObject[][] objects = new GameObject[MAX][MAX];
							for (int i = 0; i < MAX; i++) {
								for (int j = 0; j < MAX; j++) {
									Board board = getBoardByIndex(i, j);
									ProjectData proj = Server.server
											.getServiceRegistry()
											.getDataService().data;
									GameMapObject gmo = GameMapObject.findByID(
											proj, b[i][j]);
									GameObject npc0 = VMapUtil.addCreature(map,
											board.getX(), board.getY(),
											(GameMapNPC) gmo, true, 0, null);
									objects[i][j] = npc0;
								}
							}
							gameObject2Board.put(instance.id, objects);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void processUnitDie(Unit u1, Unit u2) {
		if (StatService.isInArray(NPC_CREATURE, u1.id) != -1
				&& u2.map.map.getId() == MAPID && (u2.type == GameObject.TYPE_ATTENDANT || u2.type == GameObject.TYPE_PLAYER)) {
			try {
				Player player = null;
				if(u2.type == GameObject.TYPE_ATTENDANT){
					Attendant att = (Attendant) u2;
					if(att!=null)
					   player = att.owner;
				}else{
					player = (Player) u2;
				}
				if(player != null){
					NormalInstance instance = (NormalInstance) u2.map.map.instance;
//					int value = hasQuest(player);
					int value = -1;
					if(player.chessType == 0){
						value = hasQuest(player);
					}else if(player.chessType == 1){
						value = hasNationDayQuest(player);
					}else if(player.chessType == 2){
						value = hasShituQuest(player);
					}
					if (value != -1) {
						int[] BASE = getBaseByQuest(value);
						Board b = getBoardByXY(u1.x, u1.y);
						if (gameObject2Board.containsKey(instance.id)) {
							GameObject[][] o = gameObject2Board.get(instance.id);
							int[][] boards = instance2Board.get(instance.id);
							boards[b.indexX][b.indexY] = BASE[value];
							instance2Board.put(instance.id, boards);
							ProjectData proj = Server.server.getServiceRegistry()
									.getDataService().data;
							GameMapObject gmo = GameMapObject.findByID(proj,
									BASE[value]);
							GameObject npc0 = VMapUtil.addCreature(u2.map.map,
									u1.x, u1.y, (GameMapNPC) gmo, true, 0, null);
							o[b.indexX][b.indexY] = npc0;
							gameObject2Board.put(instance.id, o);
							List<Board> boardList = new ArrayList<Board>();
							int c = 0;//连击控制
							checkResult(player,boards,b.indexX,b.indexY,BASE[value],boardList,o,c);
							instance2Board.put(instance.id, boards);
							gameObject2Board.put(instance.id, o);
						}else{
							tranPlayerOut(player);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/** 击杀棋子后判断连珠结果 */
	public void checkResult(Player player,int[][] BOARD,int x ,int y,int chess,List<Board> boardList,GameObject[][] o,int c){
		int v = getResult(BOARD,x ,y,chess,boardList);
		if(v==0){
			return;
		}else{
			c++;
			if(player.party!=null){
			     synchronized (player.party){
					if(player.party.members!=null && player.party.members.size() == 2){
						Player togetherPlayer = player. party.getPartyFriend(player.id);
						Player tPlayer = ObjectAccessor.getPlayer(togetherPlayer.id);
						if(tPlayer!=null && tPlayer.map.getId() == player.map.getId()){
							if(player.chessCount>=0)
							     player.chessCount += v;
							if(tPlayer.chessCount>=0)
							     tPlayer.chessCount += v;
							Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, MessageFormat.format("恭喜您获得{0}个连珠", v));
							Server.server.getServiceRegistry().getChatService().sendPrivateMessage(tPlayer.id, MessageFormat.format("恭喜您获得{0}个连珠", v));
						}
					}
				}
			}else{
				if(player.chessType!=2){
				    player.chessCount += v;
				    Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, MessageFormat.format("恭喜您获得{0}个连珠", v));
				    if(player.chessType == 0){
				    	for(int i=0;i<v;i++){
				    		PlayerTransaction tx = player.newTransaction("CHESS");
				    	    player.addExp(EXP_ADD, tx, true);
				    	    tx.commit();
				    	}
				    }
				}
			}
			if(boardList.size()>0){
				processBoardList(player,boardList,BOARD,o);
				if(c<5){
					List<Board> boardList2 = new ArrayList<Board>();
					checkResult(player,BOARD,x,y,chess,boardList2,o,c);
				}
			}
		}
	}
	
	/** 处理被消掉的棋子 */
	public void processBoardList(Player player,List<Board> boardList,int[][] boards,GameObject[][] o){
		int value = -1;
		if(player.chessType == 0){
			value = hasQuest(player);
		}else if(player.chessType == 1){
			value = hasNationDayQuest(player);
		}else if(player.chessType == 2){
			value = hasShituQuest(player);
		}
		if (value != -1) {
			int[] BASE = getBaseByQuest(value);
		    for(Board b : boardList){
				o[b.indexX][b.indexY].removeFromWorld();
				int v = rand.nextInt(BASE.length);
				boards[b.indexX][b.indexY] = BASE[v];
				ProjectData proj = Server.server.getServiceRegistry()
				.getDataService().data;
		        GameMapObject gmo = GameMapObject.findByID(proj,BASE[v]);
		        GameObject npc0 = VMapUtil.addCreature(player.map.map,
				b.x, b.y, (GameMapNPC) gmo, true, 0, null);
		        o[b.indexX][b.indexY] = npc0;
			}
		}
	}
	
	/** 判断连珠结果 */
	public int getResult(int[][] BOARD,int x ,int y,int chess,List<Board> boardList){
		int bearls = 0;
		int oldx = x;
		int oldy = y;
		//横向判断
		int top = y;
		if (y > 0) {
			for (int i = y - 1; i >= 0; i--) {
				if (BOARD[x][i] == chess) {
					top = i;
				} else {
					break;
				}
			}
		}
		// 向回统计所有chess的个数
		int count = 0;
		y = top;

		for (int j = y; j < MAX; j++) {
			if (BOARD[x][j] == chess) {
				count++;
			} else {
				break;
			}
		}
		
		y = top;
		if(count>=MAX-1){
			bearls ++;
			if(boardList!=null){
				for (int j = y; j < MAX; j++) {
					if (BOARD[x][j] == chess) {
					    Board board = getBoardByIndex(x,j);
					    if(!boardList.contains(board)){
					    	boardList.add(board);
					    }
					} else {
						break;
					}
				}
			}
		}
		
		
		
		//竖向判断
		count = 0;
		x = oldx;
		y = oldy;
		top = x;
		if (x > 0) {
			for (int i = x - 1; i >= 0; i--) {
				if (BOARD[i][y] == chess) {
					top = i;
				} else {
					break;
				}
			}
		}
		// 向回统计所有chess的个数，如果是COUNT个就赢了
		x = top;
		for (int i = x; i < MAX; i++) {
			if (BOARD[i][y] == chess) {
				count++;
			} else {
				break;
			}
		}
		
		x = top;
		if(count>=MAX-1){
			bearls ++;
			if(boardList!=null){
				for (int i = x; i < MAX; i++) {
					if (BOARD[i][y] == chess) {
						Board board = getBoardByIndex(i,y);
					    if(!boardList.contains(board)){
					    	boardList.add(board);
					    }
					} else {
						break;
					}
				}
			}
		}
		
		//左向右斜判断
		x = oldx;
		y = oldy;
		top = y;
		int left = x;
		count = 0;
		while (true) {
			if (x == 0 || y == 0 || BOARD[x - 1][y - 1] != chess) {
				// 如果x已经是棋盘的边缘， 或者的前一个不是chess
				// 就不再继续查找了
				break;
			}
			x--;
			y--;
			top = y;
			left = x;
		}
		// 向回统计所有chess的个数
		x = left;
		y = top;
		while (true) {
			if (x == MAX || y == MAX || BOARD[x][y] != chess) {
				break;
			}
			count++;
			x++;
			y++;
		}
		
		x = left;
		y = top;
		
		if(count>=MAX-1){
			bearls ++;
			if(boardList!=null){
				while (true) {
					if (x == MAX || y == MAX || BOARD[x][y] != chess) {
						break;
					}
					Board board = getBoardByIndex(x,y);
				    if(!boardList.contains(board)){
				    	boardList.add(board);
				    }
					x++;
					y++;
				}
		    }
		}
		
		
		//右向左斜判断
		x = oldx;
		y = oldy;
		count = 0;
		top = y;
		left = x;
		while (true) {
			if (x == MAX - 1 || y == 0 || BOARD[y - 1][x + 1] != chess) {
				break;
			}
			x++;
			y--;
			top = y;
			left = x;
		}
		
		x = left;
		y = top;
		while (true) {
			if (x < 0 || y == MAX || BOARD[y][x] != chess) {
				// 如果找到头 或者 下一个子不是chess 就不再继续统计了
				break;
			}
			count++;
			x--;
			y++;
		}
		// 向回统计所有chess的个数
		x = left;
		y = top;
		if(count>=MAX-1){
			bearls ++;
			if(boardList!=null){
				while (true) {
					if (x < 0 || y == MAX || BOARD[y][x] != chess) {
						// 如果找到头 或者 下一个子不是chess 就不再继续统计了
						break;
					}
					Board board = getBoardByIndex(x,y);
				    if(!boardList.contains(board)){
				    	boardList.add(board);
				    }
					x--;
					y++;
				}
			}
		}
		return bearls;
	}
	
	/** 判断当天是否有已完成的五子棋任务 */
	public static boolean finishTime(Player player,int questId){
		if(hasQuest(player)==-1){
			int[] QUESTARR = getQuestArr(player);
			if(StatService.isInArray(QUESTARR, questId)!=-1){
				for(int i=0;i<QUESTARR.length;i++){
					if(QUESTARR[i]!=questId){
						if(player.asmVm.getFinishTime(QUESTARR[i])!=0){
							long now = System.currentTimeMillis();
							cachedCal.setMillis(now);
							int nowDayofYear = cachedCal.getDayOfYear();
							int nowYear = cachedCal.getYear();
							long questTime = player.asmVm.getFinishTime(QUESTARR[i]);
							cachedCal.setMillis(questTime);
							int questDayofYear = cachedCal.getDayOfYear();
							int questYear = cachedCal.getYear();
							return nowYear == questYear && nowDayofYear == questDayofYear; 
						}
					}
				}
			}
		}else if(player.chessType == 2 && hasShituQuest(player)==-1){
			int qId = getShituQuest(player);
			if(qId == questId && player.asmVm.getFinishTime(qId)!=0){
				long now = System.currentTimeMillis();
				cachedCal.setMillis(now);
				int nowDayofYear = cachedCal.getDayOfYear();
				int nowYear = cachedCal.getYear();
				long questTime = player.asmVm.getFinishTime(qId);
				cachedCal.setMillis(questTime);
				int questDayofYear = cachedCal.getDayOfYear();
				int questYear = cachedCal.getYear();
				return nowYear == questYear && nowDayofYear == questDayofYear; 
			}
		}
		return false;
	}
	
	/** 玩家完成任务时传出副本地图 */
	public void tranPlayerOut(Player player){
		try {
			if(player.chessType==0){
				int[] mapInfo = getOutMap(player);
				if (mapInfo[0] != 0 && mapInfo[1] != 0
						&& mapInfo[2] != 0)
					player.goMap(mapInfo[0], mapInfo[1],
							mapInfo[2]);
			}else if(player.chessType == 1){
				player.goMap(OUTMAP_NATIONDAY[0], OUTMAP_NATIONDAY[1], OUTMAP_NATIONDAY[2]);
			}else if(player.chessType == 2){
				int[] mapInfo = getShituOutMap(player);
				if (mapInfo[0] != 0 && mapInfo[1] != 0
						&& mapInfo[2] != 0)
					player.goMap(mapInfo[0], mapInfo[1],
							mapInfo[2]);
			}
		} catch (VMapException e) {

		}
	}
	
	public void processAbandonQuest(Player player,int questId){
		try{
			int[] QUESTARR = getQuestArr(player);
			if(StatService.isInArray(QUESTARR, questId)!=-1 && player.map!=null && player.map.getId() == MAPID){
				tranPlayerOut(player);
			}
		}catch(Exception e){
			
		}
	}

	/** 玩家进入地图*/
	public void enterInstance(Player player,int type) throws Exception {
		if(type == 0){//五子棋任务
			int questId = hasQuest2(player) ;
			if (questId == -1)
				throw new Exception("请您接取五子棋任务后再进入副本！");
		    if(player.asmVm.canFinish(questId)==1)
		    	throw new Exception("您已经完成任务，赶快交任务领取奖励吧!");
		    if(player.party!=null){
		    	throw new Exception("请先离开队伍后再进入副本！");
		    }
		    player.chessType = 0;
		}else if(type==1){//国庆任务
			int value = hasNationDayQuest(player);
			if(value == -1)
				throw new Exception("请您接取国庆任务后再进入副本！");
			if(player.asmVm.canFinish(QUEST_NATIONDAY)==1)
		    	throw new Exception("您已经完成任务，赶快交任务领取奖励吧!");
			if(player.party!=null){
		    	throw new Exception("请先离开队伍后再进入副本！");
		    }
			 player.chessType = 1;
		}else if(type == 2){//师徒任务
			if(!ApprenticeService.inPartyTogether(player)){
		    	throw new Exception("该场景需要师徒两人组队共同挑战，快去找到你的师父/徒弟一起来吧！");
		    }
			Player targetPlayer =player. party.getPartyFriend(player.id);
			int questId = getShituQuest(player);
			if(finishTime(player,questId) || finishTime(targetPlayer,questId)){
				throw new Exception("每天只能完成一次师徒任务，请明天再来吧！");
			}
			
			int valueApp = hasShituQuest(player);
			int valueTeacher = hasShituQuest(targetPlayer);
			if(valueApp == -1  || valueTeacher == -1)
				throw new Exception("师徒两人都领取师徒任务后方可进入副本！");
			
			if(player.asmVm.canFinish(questId)==1)
		    	throw new Exception("您已经完成任务，赶快交任务领取奖励吧!");
			 player.chessType = 2;
		}
		try {
			player.goMap(MAPID, 345, 345);
			if(type == 0){
				GameMapExit[] gmes = VMapUtil.getExits(MAPID);
				for (GameMapExit exit : gmes) {
					if (exit.exitType == GameMapExit.TYPE_RECALL) {
						player.pool.setString(exit.positionVarName,exits_chess[player.faction][0] + "," 
								+ exits_chess[player.faction][1] + "," + exits_chess[player.faction][2]);
					}
				}
			}else if(type == 2){
				GameMapExit[] gmes = VMapUtil.getExits(MAPID);
				for (GameMapExit exit : gmes) {
					if (exit.exitType == GameMapExit.TYPE_RECALL) {
						player.pool.setString(exit.positionVarName,exits_apprentice[player.faction][0] + "," 
								+ exits_apprentice[player.faction][1] + "," + exits_apprentice[player.faction][2]);
					}
				}
			}
		} catch (VMapException e) {
			// log.error(e,e);
		}
	}

	
	public Board getBoardByIndex(int indexX, int indexY) {
		if (indexX < MAX && indexY < MAX)
			return index2Board[indexX][indexY];
		return null;
	}

	public Board getBoardByXY(int x, int y) {
		for (int i = 0; i < MAX; i++) {
			for (int j = 0; j < MAX; j++) {
				Board b = index2Board[i][j];
				if (b.x == x && b.y == y) {
					return b;
				}
			}
		}
		return null;
	}

	public int[] getBaseByQuest(int dex) {
		if (dex >= 0) {
			int[] BASE = new int[2];
			if (dex == 0) {
				BASE[0] = NPC_NEUTRO[0];
				BASE[1] = NPC_CREATURE[1];
			} else if (dex == 1) {
				BASE[0] = NPC_CREATURE[0];
				BASE[1] = NPC_NEUTRO[1];
			}
			return BASE;
		}
		return null;
	}

}

class Board {
	int indexX;
	int indexY;
	int x;
	int y;

	public Board(int indexX, int indexY, int x, int y) {
		this.indexX = indexX;
		this.indexY = indexY;
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
