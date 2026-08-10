package peony.game;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

import peony.game.party.Party;
import peony.game.skill.Skill;
import peony.game.touchaction.TouchAction;
import peony.net.Packet;

import com.pip.sanguo.data.Animation;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.map.Period;

public class Creature extends Unit {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(Creature.class);
	
	
	protected CreatureAI ai;
	
	protected int dieTime = 0;
	public int refreshTime = 10000;
	public int disappearTime;    // 自动消失时间（通常用于召唤的怪物），0表示不自动消失
	public boolean isGuard;   // 是否PVP卫兵，PVP卫兵和怪物保持中立
	public boolean isStaticField;  // 是否静态NPC，静态NPC在玩家进入场景时立刻刷新给玩家
	public boolean dynamicRefresh = true;
	public int linkDistance;    // 仇恨关联距离（像素）
	public int originalX,originalY;
//	public PipAnimateSet image;
	public Animation npcImage;
//	public int templateId;
	public List<int[]> patrolPath;  //循环路径
	public int cycle;
	public boolean canPass;
    public boolean isFunctional = false;
    public boolean canBeAttacked = true;  // 是否可以被攻击
    public int eyeshot;  //视野范围
    public String functionName = "";
    public String functionScript = "";
    public int dieRefreshNPC;
    public String searchName;      // 寻路名称，null表示不可寻路
    
    public String title,subTitle; //编辑器中如果有用|符号分割开来的名字，那么第一部分会被转换成title,第二部分会被转换成subTitle,如果还有其他部分不管
    
    public Fall fall;
    public Skill skill;
    public TouchAction[] touchAction;
    public int angle;
    public int chaseDistance; //追击范围
    public int chaseSpeed; //追击速度
    public int patrolSpeed; //巡逻速度
    public int speed; //当前速度
    
    public int lastAttackedTime;
    
    public int nextX,nextY,startX,startY;
    private int nextDistance;			// 到下一点的距离
    private int needRunTime;			// 到下一点的预计时间
    public int runToNextPointTime; //从一点开始到另外一点的时间
//    public int runToNextMaxLen;
    
    //被任务脚本刷出来的npc，此字段指明是由哪个用户的脚本触发的
    public GameObjectRef ownerRef;
    
    public NPCTemplate template = null;
    
    public ThreatGroup threatGroup = null;
	
    // 怪物的伤害贡献表，战斗开始时生成，结束时清除
    @Transient
    public BattleContributionList battleContribList;
    // 怪物进入战斗的时间
    @Transient
    public int battleStartTime;
    
    public List<Period> refreshPeriods = null;
    
    public CreatureDieCallback dieCallback;
    
    public boolean isPvp;
    
    public static int[] specialIds = {393251,594064,659485,1642605,2232457,135226,69709,725050,2166882,1970287,1249359,1183823,1708115,2031682,2297966};
    
	public Creature(int id,String name,int x,int y,VMap map){
		super(GameObject.TYPE_CREATURE);
		this.id = id;
		this.instanceId = ids.incrementAndGet();
		this.setName(name);
		splitName(name);
		this.map = new VMapReference();
		this.angle = 0;
		addToMap(map,x,y);
		this.originalX = x;
		this.originalY = y;
		this.startX = x;
		this.startY = y;
		this.nextX = x;
		this.nextY = y;
		this.nextDistance = 0;
		this.clazz = Unit.CLASS_1;
		this.speed = patrolSpeed;
	}
	
	protected void splitName(String s){
		String[] ss = s.split("\\|");
		this.title = ss[0];
		if(ss.length > 1){
			this.subTitle = normalizeSubTitle(ss[1]);
		}
	}
	
	protected String normalizeSubTitle(String s){
		if(s.charAt(0)!='<'){
			return s;
		}else{
			return s.substring(1, s.length()-1);
		}
	}
	
	public void initPatrolPath(List<int[]> path){
		if(path==null||path.isEmpty()){
			patrolPath = new ArrayList<int[]>(0);
			return;
		}
		patrolPath = new ArrayList<int[]>(path.size()+1);
		int[] begin = {originalX,originalY};
		patrolPath.add(begin);
		for(int i=0;i<path.size();i++){
			int[] point = path.get(i);
//			System.out.println(String.format("patrol x[%d]y[%d]",point[0]+originalX,point[1]+originalY));
			patrolPath.add(new int[]{point[0]+originalX,point[1]+originalY});
		}
	}
	
	public void setAI(CreatureAI ai){
		this.ai = ai;
		if(ai!=null)
			ai.init();
	}
	
	public CreatureAI getCreatureAI(){
		return ai;
	}
	
	@Override
	public boolean isStatic() {
	    return isStaticField;
	}
	
	/**
	 * 判断是否服务器动态刷新出来的（没有关卡数据支持）。
	 * @return
	 */
	public boolean isDynamic() {
	    return false;
	}
	
	public void go(boolean notify) {
		super.go();
		this.nextX = this.x;
		this.nextY = this.y;
		this.nextDistance = 0;
		if(notify)
			moveType |= MOVE_RUNNING_STATE;
	}
	
	@Override
	public void update(int diffTime) {
		try {
			cycle++;
			processDie();
			if (isAlive()) {
			    if (disappearTime >0  && disappearTime < Time.currTime) {
			        removeFromWorld();
			        return;
			    }
				if (attack != null) {
					if (attack.update(diffTime) != -1)
						attack = null;
				}
				if (isRunning() && cycle % 5 == 0) {
					runToNextPoint();
				}
				if (ai != null)
					ai.update();
				if (refreshPeriods != null){ //如果不在战斗状态并且超过刷新时间段，那么就刷没
					boolean inRefreshPeriods = false;
					for(Period p:refreshPeriods){
						if(p.in(Time.currDate)){
							inRefreshPeriods = true;
							break;
						}
					}
					if(!inRefreshPeriods){
						die(null);
						setInvisible();
					}
				}
			}
			if (!isVisibleAndAlive()) {
				if (dieTime > 0 && dieTime < Time.currTime)
					relive(maxhp, maxmp);
			}
			buffs.update(diffTime);
			// if(buffs.update(diffTime)){
			// refreshProperties(false);
			// }
			if (battleContribList != null) {
			    battleContribList.update(diffTime);
			}
			processThreats();
			processMove(null);
			processMoveExt();
			if (!isAlive() && dieTime < 0) {
                removeFromMap();
                ObjectAccessor.removeGameObject(this);
			}
		} catch (Exception ex) {
			log.error(ex,ex);
		}
	}
	
	protected boolean isSpecialId(int creatureId){
		for(int id : specialIds){
			if(id==creatureId)
				return true;
		}
		return false;
	}
	
	public void setNextPoint(int x,int y){
		if(nextX!=x||nextY!=y){
			this.startX = this.x;
			this.startY = this.y;
			this.nextX = x;
			this.nextY = y;
			this.nextDistance = (int)Math.sqrt((this.nextX - this.startX) * (this.nextX - this.startX) + 
					(this.nextY - this.startY) * (this.nextY - this.startY));
			this.runToNextPointTime = Time.currTime;
			if (this.speed == 0) {
				this.needRunTime = runToNextPointTime;
			} else {
				this.needRunTime = this.runToNextPointTime + (this.nextDistance * 1000) / this.speed;
			}
			angle = calcAngle(this.x, this.y, nextX, nextY);
		}
	}
	
	@Override
	public int getNextPointX(){
		return nextX;
	}
	
	@Override
	public int getNextPointY(){
		return nextY;
	}
	
	@Override
	public int prepareSkillAttack(Unit target, Skill skill, int offsetTime) {
		int ret = super.prepareSkillAttack(target, skill, offsetTime);
		if (target != this) {
			int newAngle = calcAngle(x, y, target.x, target.y);
			if(newAngle!=angle){
				angle = newAngle;
				moveType |= MOVE_ANGLE;
			}
		}
		return ret;
	}
	
	@Override
	public void goMap(int mapId, int x, int y) {
		if(map.id==mapId)
			move(x,y);
		else{
			removeFromMap();
			NoInstanceVMapManager manager = (NoInstanceVMapManager)Server.server.world.getVMapManager(mapId);
			VMap[] maps = manager.getVMaps(mapId);
			maps[0].addCreature(this, x, y);
		}
	}
	
	
	public void runToNextPoint() {
		if (cannotMove())
			return;
		if (isRunning()) {
			if (this.nextDistance == 0) {
				return;
			}
			if (Time.currTime < runToNextPointTime || Time.currTime >= needRunTime) {
				// 时间错误或时间超出，都直接跳转到目标位置
				move(nextX, nextY);
			} else {
				int distance = getSpeed() * (Time.currTime - runToNextPointTime) / 1000;
				int dx = distance * (this.nextX - this.startX) / this.nextDistance;
				int dy = distance * (this.nextY - this.startY) / this.nextDistance;
				move(startX + dx, startY + dy);
			}
			lastMoveTime = Time.currTime;
		}
	}

	
	protected byte getDirect(int beginX,int beginY,int endX,int endY){
		if(beginX==endX){
			if(endY==beginY)
				return direct;
			return endY > beginX?DIRECT_DOWN:DIRECT_UP;
		}
		else {
			return endX > endY?DIRECT_RIGHT:DIRECT_LEFT;
		}
	}
	
	protected int[] getBeginPosition(int index){
		return patrolPath.get(index);
	}
	
	protected int[] getEndPosition(int index){
		if(++index==patrolPath.size())	index = 0;
		return patrolPath.get(index);
	}
	
	protected void processMoveExt(){
		if((moveExtended&MOVEEXT_BUFFS)!=0){
			Packet pt = getBuffsPacket();
			broadcast(pt,null,null,false,false,false);
		}
		moveExtended = 0;
	}

	@Override
	public void relive(int hp, int mp) {
		ObjectAccessor.removeGameObject(this);
		VMap oldMap = map.map;
		map.removeGameObject(this, false);
		lastRef = null;
		instanceId = ids.incrementAndGet();
		instanceTime = Time.currTime;
		x = originalX;
		y = originalY;
		refreshProperties(true);
		setHp(maxhp, false);
		setMp(maxmp, false);
		state &= MASK_CLEAR;
		if (ai != null) {
			ai.init();
		}
		ObjectAccessor.addGameObject(this);
		if (oldMap != null) {
			oldMap.addCreature(this);
			// map.map = oldMap;
		}
		lastMoveTime = CommonUtil.currentMillis();
		if (ai != null)
			ai.init();
		moveType |= MOVE_ALL;
	}
	
//	public void notifyState(){
//		if(map!=null)
//			map.notifyState(this);
//	}
	
	
	@Override
	public void realDie(Unit source){
		super.realDie(source);
		LogUtil.logUnitDie(this, source);
		if (battleContribList != null) {
			List<Player> owners = battleContribList.checkOwners();
			LogUtil.logUnitDieDist(this, owners);
		    if (owners != null) {
		        // 每个人都算杀死这个怪了
		        for (Player p : owners) {
		            p.addKillCreatureCount(this.template.getID(), owners);
		        }
		        
		        // 为每个玩家分别计算掉落
		        Gain[] gains = new Gain[owners.size()];
                for (int i = 0; i < gains.length; i++) {
                    gains[i] = new Gain(owners.get(i),null,true);
                }
                if (gains.length > 0) {
                    this.fall.gain(RND, gains);
                    for (Gain gain : gains) {
                        gain.addToPlayer(true, "DRP");
                    }
                }
		    }
		}
		
		// 记录战斗胜利日志
		LogUtil.logBossFight(this, Time.currTime - battleStartTime, true);
		
		// 统计
		Server.server.getServiceRegistry().getRealtimeStatService().fightCounter++;
		
		clearBattleData();
		
        if (dieRefreshNPC >= 0) {
            // 死亡后刷新一个NPC出来(必须在同一场景)
            ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
            GameMapObject gmo = GameMapObject.findByID(proj, dieRefreshNPC);
            if (gmo != null && gmo instanceof GameMapNPC) {
                VMapUtil.addCreature(this.getVMap(), (GameMapNPC)gmo, true, 0, Server.server.revision);
            }
        }
        
		if(refreshTime<0)  //刷新时间 
			dieTime = -1;
		else{
			int refreshTime = getRefreshTime();
			if(refreshPeriods!=null){
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(System.currentTimeMillis() + refreshTime);
				dieTime = Time.elapseTime(Period.getNextTimeInPeriods(cal,refreshPeriods).getTimeInMillis());
			}else{
				dieTime = Time.currTime + refreshTime;
			}
		}
		moveType |= MOVE_POINT_STATE;
		CreatureDieCallback callback = creatureDieCallback();
		if(callback!=null){
			callback.die(this, source);
		}
	}
	
//	private static final int[] REFRESH_RATE = {
//	    100, 100, 100, 80, 70, 60, 50, 45, 40, 35, 30, 25, 24, 23, 22, 21, 20, 19,
//	    18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 9, 8, 8, 7, 7, 6, 6, 5, 5, 4, 4,
//	    3, 3, 2, 2, 1 
//	};
    private static final int[] REFRESH_RATE = {
        100, 80, 60, 50, 40, 30, 20, 15, 10, 9, 8, 7, 6, 5, 4, 3, 1
    };
	protected int getRefreshTime(){
		if(mapCell==null || !dynamicRefresh){
			return refreshTime;
		}
		int total = mapCell.getNearPlayerCount();
		int ret;
		if (total >= REFRESH_RATE.length) {
		    ret = refreshTime / 100;
		} else {
		    ret = refreshTime * REFRESH_RATE[total] / 100;
		}
		if (ret < 500) {
		    ret = 500;
		}
		return ret;
	}
	
	@Override
	public void setInvisible(){
		super.setInvisible();
		if(refreshTime<0)
			dieTime = -1;
		else
			dieTime = Time.currTime + refreshTime;
		if (dieTime < 0) {
			removeFromMap();
			ObjectAccessor.removeGameObject(this);
		} else {
		    VMap.notifyDisappear(this);  // 广播消失 
		}
		moveType |= MOVE_STATE;
	}
	
	public void setInvisibleOnly(){
		super.setInvisible();
		 VMap.notifyDisappear(this);  // 广播消失 
		 moveType |= MOVE_STATE;
	}

	public void setVisibleOnly(){
        super.setVisible();
        VMap.notifyAppear(this);
        moveType |= MOVE_ALL;
	}
	
	@Override
	public void setVisible(){
        super.setVisible();
        VMap.notifyAppear(this);
        moveType |= MOVE_ALL;
    }
	
	public void stop(int angle){
			this.stop();
			this.angle = angle;
	}
	
	@Override
	public void stop(){
		super.stop();
		moveType |= MOVE_RUNNING_STATE;
	}
	
	@Override
	public void fear(){
		super.fear();
		moveType |= MOVE_POINT_STATE;
	}
	
	@Override
	public void unFear(){
		super.unFear();
		moveType |= MOVE_POINT_STATE;
	}
	
	@Override
	public void paralyze(){
		super.paralyze();
		moveType |= MOVE_POINT_STATE;
	}
	
	@Override
	public void unParalyze(){
		super.unParalyze();
		moveType |= MOVE_POINT_STATE;
	}
	
	@Override
	public void stay(){
		super.stay();
		moveType |= MOVE_POINT_STATE;
	}
	
	@Override
	public void unStay(){
		super.unStay();
		moveType |= MOVE_POINT_STATE;
	}
	
	@Override
	public boolean isRunning(){
		if((state&STATE_STOP)!=0)
			return false;
		return super.isRunning();
	}
	
	
//	@Override
//	public void attack(AttackResult ar){
//		
//	}
	
	@Override
	public void setAttack(Attack attack){
		super.setAttack(attack);
	}
	
	@Override
	public int getSpeed(){
		return (int)(speed * speedRating);
	}
	
	@Override
	public int getHalfAngle(){
		return angle/2;
	}
	
	@Override
	public Packet getInfoPacket(){
		Packet pt = new Packet(OpCode.UNIT_INFO_SERVER);
		pt.putInt(instanceId);
		pt.put(canPass?1:0);
		pt.put(isFunctional?1:0);
		pt.putString(functionName);
		return pt;
	}
	
	/**
	 * 判断是否在刷新时段内
	 * @return
	 */
	public boolean inRefreshPeriods(){
		if(refreshPeriods == null)
			return true;
		else{
			Calendar cal = Calendar.getInstance();
			for(Period p:refreshPeriods){
				if(p.in(cal))
					return true;
			}
			return false;
		}
	}
	
	@Override
	public void attacked(CombatContext context){
		lastAttackedTime = Time.currTime;

		// 更新战斗贡献记录
		if (battleContribList == null) {
		    battleContribList = new BattleContributionList(this);
		    battleStartTime = Time.currTime;
		}	
		battleContribList.attacked(context.source);
		if(isPvp && context.source instanceof Player){
			setPlayerWarState((Player)context.source);
		}
		if(context.source instanceof Player && context.target instanceof Creature){
			NPCTemplate npcTemplate = ((Creature)context.target).template;
			if(npcTemplate.isPvp){
				setPlayerWarState((Player)context.source);
			}
		}
	}
	
	private void setPlayerWarState(Player p){
		if(p.warState==Player.PVPPVESTATE){
			p.setWarState(Player.PVPSTATE);
		}
	}
	
	private void superAddThreatUnit(Unit u, float initThreat, boolean direct) {
		super.addThreatUnit(u, initThreat, direct);
	}
	
	@Override
	public void addThreatUnit(Unit u, float initThreat, boolean direct) {
		int oldCount = threats.count;
		super.addThreatUnit(u, initThreat, direct);

		// 更新战斗贡献记录
		if (battleContribList == null) {
			battleContribList = new BattleContributionList(this);
			battleStartTime = Time.currTime;
		}
		battleContribList.newThreat(u, initThreat);
//		if(threatGroup!=null){
//			for(GameObjectRef ref:threatGroup.refs){
//				Creature c = (Creature)ObjectAccessor.getGameObject(ref);
//				if(c!=null&&c.isVisibleAndAlive()){
//					c.superAddThreatUnit(u, 0.0f, direct);
//					u.addThreatUnit(c, 0.0f, direct);
//				}
//			}
//		}

		// 如果是导致进入战斗的第一个仇恨，则在四周寻找链接怪物。如果找到，进行仇恨传播。
		if (oldCount == 0 && linkDistance != 0) {
			List<Creature> l = findLinkCreatures(linkDistance);
			if (l.size() > 0) {
//				ThreatGroup tg = new ThreatGroup(u.ref(),getVMap());
//				tg.addCreature(this);
				for (Creature c : l) {
					c.cloneInitThreats(this);
//					tg.addCreature(c);
				}
			}
		}
	}
	
	/**
	 * 在整个场景指定范围内查找同阵营的怪物。
	 * @param dist
	 * @return
	 */
	public List<Creature> findLinkCreatures(int dist){
//		List<Creature> ret = new ArrayList<Creature>();
//		if (this.mapCell != null) {
//		    MapCell[] checkCells = this.mapCell.map.getMapCells(this ,this.x, this.y, dist);
//		    for (MapCell cell : checkCells) {
//				for (GameObject o : cell.objects.values()) {
//				    // 检查条件：不是自己、必须是怪物、同阵营、可见、存活、未进入战斗、在范围内
//				    if (o != this && o.type == GameObject.TYPE_CREATURE && 
//				            o.faction == this.faction && o.isVisibleAndAlive() && 
//				            ((Creature)o).getThreatCount() == 0 && inRange(o, dist)) {
//				        ret.add((Creature)o);
//				    }
//				}
//			}
//		}
//		return ret;
		List<Creature> ret = new ArrayList<Creature>();
		if (this.mapCell != null) {
			MapCellIterator ite = this.mapCell.map.getMapCellsSync(this ,this.x, this.y, dist);
		    while (ite.hasNext()) {
		    	MapCell cell = ite.next();
				for (GameObject o : cell.objects.values()) {
				    // 检查条件：不是自己、必须是怪物、同阵营、可见、存活、未进入战斗、在范围内
				    if (o != this && o.type == GameObject.TYPE_CREATURE && 
				            o.faction == this.faction && o.isVisibleAndAlive() && 
				            ((Creature)o).getThreatCount() == 0 && inRange(o, dist)) {
				        ret.add((Creature)o);
				    }
				}
			}
		}
		return ret;
	}
	
	public void removeFromMap(){
		if(this.map!=null&&map.map!=null){
			map.removeGameObject(this,true);
		}
	}
	
	public void clearBattleData() {
		if (this.battleContribList != null && this.battleContribList.getOwner() != null) {
			moveType |= MOVE_OWNER|MOVE_DETAIL;
		}
		threatGroup = null;
		battleContribList = null;
	}
	
	public void backState(int x,int y){
		clearThreats();
		buffs.removeUpdatableBuffs();
		move(x,y);
		moveType|=GameObject.MOVE_RUNNING;
		setHp(maxhp,false);
		setMp(maxmp, false);
		
		// 记录战斗胜利失败
		LogUtil.logBossFight(this, Time.currTime - battleStartTime, false);
		
		clearBattleData();
	}
	
	
	protected int lastNotifyX,lastNotifyY;
	
	
	@Override
	public Packet getMovePacket(short moveType){
		moveType &= ~MOVE_HORSE;
		Packet pt = new Packet(OpCode.UNIT_MOVE_SERVER);
		pt.put(type|moveType);
		pt.putInt(instanceId);
		if((moveType&MOVE_POINT)!=0){
			pt.putShort(map.id);
			pt.putShort(x);
			pt.putShort(y);
		}
		if ((moveType & MOVE_ANGLE) != 0) {
			pt.put(getHalfAngle());
			pt.putInt(Time.currTime);
			pt.put(getSpeed());
			if (!isRunning()) {
				pt.putShort(-1);
				pt.putShort(-1);
			} else {
				pt.putShort(getNextPointX());
				pt.putShort(getNextPointY());
			}
		}
		if((moveType&MOVE_HPMP)!=0){
			pt.put(maxhp==0?200:hp*200/maxhp);
			pt.put(maxmp==0?200:mp*200/maxmp);			
		}
		if((moveType&MOVE_STATE)!=0){
			pt.putShort(state);
		}
		if((moveType&MOVE_DETAIL)!=0){
			pt.put(moveType>>8);
			if((moveType&MOVE_NAME)!=0){
				pt.putString(name);
			}
			if((moveType&MOVE_LEVEL)!=0){
				pt.put(level);
			}
			if((moveType&MOVE_FACTION)!=0){
				pt.put(faction);
			}
			if((moveType&MOVE_EQUIPMENT)!=0){
				pt.putInt(head_score);
				pt.putInt(body_score);
				pt.putInt(weapon_score);
				pt.put(flashLevel);
			}
			if((moveType&MOVE_SEX)!=0){
				pt.put(sex);
			}
			if((moveType&MOVE_OWNER)!=0){
			    if (battleContribList == null || battleContribList.getOwner() == null) {
			        pt.putInt(-1);
			    } else {
			        Object owner = battleContribList.getOwner();
			        if (owner instanceof GameObjectRef) {
			            pt.putInt(((GameObjectRef)owner).id);
			        } else {
			            pt.putInt(((Party)owner).leader.player.id);
			        }
			    }
			}
			if((moveType&MOVE_CLAZZ)!=0){
				pt.put(clazz);
			}
		}
		return pt;
	}
	
	
	@Override
	public Packet getRefreshPacket(boolean visible){
		Packet pt = new Packet(OpCode.UNIT_REFRESH_SERVER);
		byte b = type;
		if(!visible){
			b |= 1<<7;
		}
		pt.put(b);
		pt.putInt(id);
		pt.putInt(instanceId);
		if(visible)
			pt.putShort(npcImage.getID());
		return pt;
	}
	
	@Override
	public void getRefreshPacket(Packet pt,boolean visible){
		byte b = type;
		if(!visible){
			b |= 1<<7;
		}
		pt.put(b);
		pt.putInt(id);
		pt.putInt(instanceId);
		if(visible)
			pt.putShort(npcImage.getID());
	}
	
	public void shout(String message,int dist,int color, int duration){
		VMap map = getVMap();
		if(map!=null){
			List<Player> l = map.getPlayersInRange(this, dist);
			if(l.size()>0){
				Packet pt = new Packet(OpCode.SHOUT_SERVER);
				pt.putString(message);
				pt.putInt(color);
				pt.putInt(duration);
				for(Player p:l){
					p.send(pt);
				}
			}
		}
	}
	
	/**
	 * 复制另外一个怪物的所有仇恨表玩家给这个怪物。
	 * @param creature
	 */
	public void cloneInitThreats(Creature creature){
		if (creature != this) {
		    for (GameObjectRef ref : creature.getAllThreats()) {
                Unit u = (Unit)ObjectAccessor.getGameObject(ref);
                if (u != null) {
                    u.addThreatUnit(this, 0.0f, true);
                    super.addThreatUnit(u, 0.0f, true);
                }
            }
		}
	}
	
	/**
     * 模板属性改变，重新设置对象属性。
     * @param renew 是否重置血蓝
     */
    public void updateTemplate(boolean renew) {
        clazz = template.clazz;
        level = template.level;
        eyeshot = template.eyeshot;
        chaseSpeed = template.speed;
        patrolSpeed = template.walkSpeed;
        speed = template.walkSpeed;
        chaseDistance = template.chaseDistance;
        npcImage = template.image;
        fall = new Fall(this);
        VMapUtil.createFall(fall, template);
        refreshProperties(renew);
    }
    
    /**
     * NPC属性改变，重新设置对象属性。
     * @param npc
     */
    public void updateSetting(GameMapNPC npc) {
        name = npc.name;
        if (patrolPath.size() == 0 && (x != npc.x || y != npc.y)) {
            // 如果不是巡逻NPC，则位置可以更新
            move(npc.x, npc.y);
            originalX = startX = x;
            originalY = startY = y;
        }
        faction = npc.faction.id;
        canPass = npc.canPass;
        isFunctional = npc.isFunctional;
        functionName = npc.functionName;
        functionScript = npc.functionScript;
        if (npc.refreshInterval == -1) {
            refreshTime = -1;
        } else {
            refreshTime = npc.refreshInterval * 1000;
        }
        searchName = npc.searchName;
        if (searchName.length() == 0) {
        	searchName = null;
        }
        dynamicRefresh = npc.dynamicRefresh;
        linkDistance = npc.linkDistance;
        isGuard = npc.isGuard;
        isStaticField = npc.isStatic;
        setupTouchAction();
    }
    
    /**
     * 根据编辑器设置初始化NPC对话动作。
     */
    public void setupTouchAction() {
        if (isFunctional && functionScript != null && functionScript.length() > 0) {
        	String[] funcs = functionScript.split(";;;;");
        	touchAction = new TouchAction[funcs.length];
        	for (int i = 0; i < funcs.length; i++) {
	            String[] args = funcs[i].split("\\s+");  //类名和参数用空格分割
	            try {
	                if (args.length == 1) {
	                    touchAction[i] = (TouchAction) Class.forName("peony.game.touchaction." + functionScript).newInstance();
	                } else {
	                    if (args[0].equals("GeneralTouchAction")) {
	                        if (args.length == 3) {
	                            Constructor c = Class.forName(
	                                    "peony.game.touchaction." + args[0])
	                                    .getConstructor(String.class, String.class);
	                            touchAction[i] = (TouchAction)c.newInstance(args[1], args[2]);
	                        } else if(args.length==2){
	                            Constructor c = Class.forName(
	                                    "peony.game.touchaction." + args[0])
	                                    .getConstructor(String.class);
	                            touchAction[i] = (TouchAction) c.newInstance(args[1]);
	                        } else if (args.length > 3) {
	                        	Constructor c = Class.forName(
	                                    "peony.game.touchaction." + args[0])
	                                    .getConstructor(String.class, String.class);
	                        	StringBuilder sb = new StringBuilder();
	                        	for (int j = 2; j < args.length; j++) {
	                        		if (sb.length() > 0) {
	                        			sb.append(" ");
	                        		}
	                        		sb.append(args[j]);
	                        	}
	                            touchAction[i] = (TouchAction)c.newInstance(args[1], sb.toString());
	                        }
	                    } else {
	                        Constructor c = Class.forName(
	                                "peony.game.touchaction." + args[0])
	                                .getConstructor(String[].class);
	                        touchAction[i] = (TouchAction)c.newInstance((Object)args);
	                    }
	                }
	            } catch (Exception e) {
	                log.error(e, e);
	            }
        	}
        } else {
            touchAction = null;
        }
    }
    
	protected CreatureDieCallback creatureDieCallback() {
		if(dieCallback != null){
			return dieCallback;
		}
		if (map.map != null) {
			if (map.map.manager.creatureDieCallback() != null) {
				return map.map.manager.creatureDieCallback();
			}
		}
		return null;
	}
}
