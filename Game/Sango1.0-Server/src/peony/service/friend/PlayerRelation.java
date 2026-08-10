package peony.service.friend;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import peony.db.PlayerRelationDAO;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;

@Entity
@Table(name = "relation")
public class PlayerRelation {
    @Id
	@Column(name="id")
    public int id;

    @Column(name="friends",nullable=false)
	@Type(type="peony.service.friend.RelationListType")
	public RelationList friends;
    
    @Column(name="blacklist",nullable=false)
	@Type(type="peony.service.friend.RelationListType")
	public RelationList blackList;

    @Column(name="enemies",nullable=false)
	@Type(type="peony.service.friend.RelationListType")
	public RelationList enemies;

    @Column(name="templist",nullable=false)
	@Type(type="peony.service.friend.RelationListType")
	public RelationList tempList;
    
    @Column(name="apprenticelist",nullable=false)
	@Type(type="peony.service.friend.RelationListType")
	public RelationList apprenticeList;
    
   @Column(name="mateid",nullable=false)
    public int mateId = -1;
   
   @Column(name="teacherid",nullable=false)

    public int teacherId = -1;
   
 
    
    /**
     * 添加一个配偶 
     */
    public boolean addMate(int mateId){
    	this.mateId = mateId;
    	return true;
    }
    
   /**
    * 删除配偶
    */
    public boolean removeMate(){
    	this.mateId = -1;
    	return true;
    }
    
    /**
     * 拜师
     */
    public boolean addTeacher(int teacherId){
    	this.teacherId = teacherId;
    	return true;
    }
    
   /**
    * 解除师徒关系
    */
    public boolean removeTeacher(){
    	this.teacherId = -1;
    	return true;
    }
    /*
     * 定义各关系表的大小。
     */
    public static final int MAX_FRIENDS = 50;
    public static final int MAX_BLACKLIST = 10;
    public static final int MAX_ENEMIES = 20;
    public static final int MAX_TEMPLIST = 20;
    public static final int MAX_APPRENTICE = 3;
    
    /*
     * 定义临时联系人类型。
     */
    public static final int INTERACT_CHAT = 1;
    public static final int INTERACT_PK = 2;
    public static final int INTERACT_ATTACK = 3;
    public static final int INTERACT_TRADE = 4;
    public static final int INTERACT_TEAM = 5;
    
    /**
     * 添加一个好友。如果添加成功，且此用户当前在其他关系表中存在，则删除。
     * @param actor
     * @return 如果好友表已满，返回false。
     */
    public boolean addFriend(Actor actor) {
    	if (friends.getCount() >= MAX_FRIENDS) {
    		return false;
    	}
    	int id = actor.id;
    	friends.addPlayer(actor, 0);
    	if (blackList.exists(id)) {
    		blackList.removePlayer(id);
    	}
    	if (enemies.exists(id)&& enemies.isLocks.get(id)==0) {
    		enemies.removePlayer(id);
    	}
    	if (tempList.exists(id)) {
    		tempList.removePlayer(id);
    	}
    	return true;
    }
    
    /**
     * 添加一个用户到黑名单。如果添加成功，且此用户当前在其他关系表中存在，则删除。
     * @param actor
     * @return 如果黑名单已满，返回false。
     */
    public boolean addBlackList(Actor actor) {
    	if (blackList.getCount() >= MAX_BLACKLIST) {
    		return false;
    	}
    	int id = actor.id;
    	blackList.addPlayer(actor, 0);
    	if (friends.exists(id)) {
    		friends.removePlayer(id);
    	}
    	if (enemies.exists(id) && enemies.isLocks.get(id)==0) {
    		enemies.removePlayer(id);
    	}
    	if (tempList.exists(id)) {
    		tempList.removePlayer(id);
    	}
    	return true;
    }
    
    /**
     * 添加一个用户到仇人录。如果添加成功，且此用户当前在其他关系表中存在，则删除。
     * @param actor
     * @return 如果仇人录已满，则删除最旧的一条数据。
     */
    public boolean addEnemy(Actor actor,Player client) {
    	//若条数达到最大条数   则删除最旧的一条条数据
    	if(enemies.getCount() >= PlayerRelation.MAX_ENEMIES){
    		Object[] actors = enemies.players.toArray();
    		boolean flag = true;
    		for(int i = (PlayerRelation.MAX_ENEMIES - 1); i > 0; i --){
    			if(enemies.isLockedOfPlayer(((Actor)actors[i]).id)){ 
    				continue;
    			}
    			enemies.removePlayer(((Actor)actors[i]).id);
    			flag = false;
    			break;
    		}
    		if(flag){//列表已经遍历完成
    			ErrorHandler.sendErrorMessage(client.session,-1 ,OpCode.CHANGE_FACTION_CLIENT , peony.Messages.STRING_00774);
    			return false;
    		}
    	}
    	int id = actor.id;
    	enemies.addPlayer(actor, 0);
    	if (friends.exists(id)) {
    		friends.removePlayer(id);
    	}
    	if (blackList.exists(id)) {
    		blackList.removePlayer(id);
    	}
    	if (tempList.exists(id)) {
    		tempList.removePlayer(id);
    	}
    	return true;
    }
    
    /**
     * 添加一个用户到临时联系人表。如果添加成功，此人将会处于临时联系人表的顶端。
     * @param actor
     * @param cause 临时联系人的操作
     * @return 如果此用户已经在其他关系表中存在，返回false。
     */
    public boolean addTempList(Actor actor, int cause) {
    	int id = actor.id;
    	if (friends.exists(id)) {
    		return false;
    	}
    	if (blackList.exists(id)) {
    		return false;
    	}
    	if (enemies.exists(id)) {
    		return false;
    	}
    	tempList.addPlayer(actor, cause);
    	tempList.truncate(MAX_TEMPLIST);
    	return true;
    }
    
   /**
    * 添加一个徒弟
    * @param actor
    * @param cause
    * @return
    */
    public boolean addApprentice(Player client,Actor actor){
    	int id = actor.id;
    	if(apprenticeList.exists(id)){
    		ErrorHandler.sendErrorMessage(client.session, -1, OpCode.PLAYER_APPRENTICE_CLIENT, peony.Messages.STRING_00775);
    		return false;
    	}
    	apprenticeList.addPlayer(actor, 0);
    	apprenticeList.truncate(MAX_APPRENTICE);
    	return true;
    }
    
    /**
     * 解除师徒关系
     * @param actorID
     */
    public void removeApprentice(int actorID){
    	apprenticeList.removePlayer(actorID);
    }
    
    /**
     * 删除一个好友。
     * @param actorID
     */
    public void removeFriend(int actorID) {
    	friends.removePlayer(actorID);
    }
    
    /**
     * 把一个玩家从黑名单中移除。
     * @param actorID
     */
    public void removeBlackList(int actorID) {
    	blackList.removePlayer(actorID);
    }

    /**
     * 把一个玩家从仇人录中移除。
     * @param actorID
     */
    public void removeEnemy(int actorID) {    		
    		enemies.removePlayer(actorID);
    }
    
    /**
     * 改变好友列表中一个好友的友好度。
     * @param actorID
     * @param delta 修改值。如果要扣除好友度，则传递一个负数。
     */
    public void changeFriendDegree(int actorID, int delta) {
    	if (friends.exists(actorID)) {
    		int oldDegree = friends.getDegreeOfPlayer(actorID);
    		friends.setDegreeOfPlayer(actorID, oldDegree + delta);
    	}
    }
    
    /**
     * 解除关联的婚姻关系
     */
    public synchronized void removeMarriageRelation(){
    	if(mateId==-1)
    		return;
    	RelationService service = Server.server.getServiceRegistry().getRelationService();
    	PlayerRelationDAO dao = Server.server.getServiceRegistry().getDbService().playerRelationDAO;
    	PlayerRelation mateRelation = service.get(mateId);
    	if(mateRelation==null){
    		mateRelation = dao.findPlayerRelation(mateId);
    		mateRelation.removeMate();
    		if(mateRelation.apprenticeList == null){
    			mateRelation.apprenticeList = new RelationList();
    		}
    		dao.updateEntity(mateRelation);
    	}else{
    		mateRelation.removeMate();
    	}
    }
    
    public PlayerRelation clone(){
    	PlayerRelation relation = new PlayerRelation();
    	relation.blackList = this.blackList.clone();
    	relation.enemies = this.enemies.clone();
    	relation.friends = this.friends.clone();
    	relation.mateId = mateId;
    	relation.tempList = this.tempList.clone();
    	relation.apprenticeList = this.apprenticeList.clone();
    	relation.teacherId = teacherId;
    	return relation;
    }
    
    public boolean hasApprenticeRelation(Player p){
    	if(p!=null){
    		if(p.level<70){
    			if(teacherId!=-1){
    				return true;
    			}
    		} else {
    			if(apprenticeList!=null && apprenticeList.getCount()>0){
    				return true;
    			}
    		}
    	}
    	return false;
    }
}
