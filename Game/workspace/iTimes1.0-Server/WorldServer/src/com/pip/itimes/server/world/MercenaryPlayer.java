package com.pip.itimes.server.world;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.EquipmentHelper;
import com.pip.itimes.server.stage.Friend;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.stage.PlayerDataException;
import com.pip.itimes.server.stage.RoleFaceData;
import com.pip.itimes.server.util.IntHashSet;
import com.pip.itimes.server.world.game.GameMap;

public class MercenaryPlayer implements PositionSprite, IPlayerData{
	
	private static final Logger log = Logger.getLogger(MercenaryPlayer.class);
	
	private int id;
	private Team team;
	private boolean needRefreshPosition;
	private IntHashSet positionHistory = new IntHashSet(50);
	private String playerName;
	private Client client; //用户
	private byte sex;
	private byte gemLightLevel;			// 宝石发光等级
	private int level;
	private String tongName;
	private byte returnTimes;
	private String title;
	private boolean inBattle;
	private long positionTime = 0;
	private short x;
    private short y;
    private byte camp;
    private int teamState = -1;
    private GameMap map;
    private int hp;
    private int mp;
    private int maxhp;
    private int maxmp;
    
    protected List diamondShineBufs = new ArrayList();		//宝辉效果产生Buf
    
    //佣兵单独拥有
    private MercenaryShop mercenaryShop;
    private IEquipment[] useEquipment;
    private short[] skillList;
    
    
    
    /**
     * 在获取Id的时候 返回的是负值 该值与角色ID区分
     */
	public int getId() {
		return -id;
	}
	
	public void setId(int id){
		this.id = id;
	}

	public Team getTeam() {
		return team;
	}
	
	public void setTeam(Team team){
		this.team = team;
	}

	public byte getLightLevel() {
		return gemLightLevel;
	}
	
	public void setLightLevel(byte lightLevel){
		gemLightLevel = lightLevel;
	}

	public boolean isNeedRefreshPosition() {
		return needRefreshPosition;
	}

	public void addPositionDest(int id) {
		positionHistory.add(id);
	}

	public void removePositionDest(int id) {
		positionHistory.remove(id);
	}

	public String getPlayerName() {
		return playerName;
	}
	
	public void setPlayerName(String playerName){
		this.playerName = playerName;
	}

	public short getFace() {
		return (short)mercenaryShop.getMercenary().getFace();
	}

	public Client getClient() {
		return client;
	}
	
	public void setClient(Client client){
		this.client = client;
	}

	public byte getSex() {
		return sex;
	}
	
	public void setSex(byte sex){
		this.sex = sex;
	}

	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level){
		this.level = level;
	}

	public String getTongName() {
		return tongName;
	}
	
	public void setTongName(String tongName){
		this.tongName = tongName;
	}

	public byte getReturnTimes() {
		return returnTimes;
	}
	
	public void setReturnTimes(byte returnTimes){
		this.returnTimes = returnTimes;
	}

	public Pet getPet() {
		return null;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title){
		this.title = title;
	}

	public String getCreditName() {
		return "新兵";
	}

	public boolean isInBattle() {
		return inBattle;
	}
	
	public boolean hasBattle(){
		return false;
	}

	public boolean hasBuf(int pro) {
		return false;
	}

	public void setNeedRefreshPosition(boolean needRefreshPosition) {
		this.needRefreshPosition = needRefreshPosition;
	}

	public void setPositionTime(long time) {
		if (positionTime + 60000L <= time) {
            positionHistory.clear();
            positionTime = time;
        }
	}

	public boolean containsPosition(int id) {
		return positionHistory.contains(id);
	}

	public byte getCamp() {
		return camp;
	}
	
	public void setCamp(byte camp){
		this.camp = camp;
	}

	public short getX() {
		return x;
	}
	
	public void setX(short x){
		this.x = x;
	}

	public short getY() {
		return y;
	}
	
	public void setY(short y){
		this.y = y;
	}

	public byte getPhizTitleType() {
		return 0;
	}

	public short getPhizTitleIndex() {
		return 0;
	}
	
	public MercenaryShop getMercenaryShop(){
		return mercenaryShop;
	}
	
	public void setMercenaryShop(MercenaryShop mercenaryShop){
		this.mercenaryShop = mercenaryShop;
	}

	public void setTeamState(int state) {
		this.teamState = state;
	}
	
	public int getTeamState() {
		return teamState;
	}

	public void setMap(GameMap map) {
		this.map = map;
	}

	public GameMap getMap() {
		return map;
	}
	
	public boolean inPkMap() {
		if(map==null){
            return false;
        }
        return map.isCanPk();
	}

	public int getRealStrength() {
		if(mercenaryShop == null || mercenaryShop.getMercenary() == null){
			return 0;
		}
		return mercenaryShop.getMercenary().getStrength();
	}

	public int getRealVitality() {
		if(mercenaryShop == null || mercenaryShop.getMercenary() == null){
			return 0;
		}
		return mercenaryShop.getMercenary().getVitality();
	}

	public int getRealIntelligence() {
		if(mercenaryShop == null || mercenaryShop.getMercenary() == null){
			return 0;
		}
		return mercenaryShop.getMercenary().getIntelligence();
	}

	public int getRealAgility() {
		if(mercenaryShop == null || mercenaryShop.getMercenary() == null){
			return 0;
		}
		return mercenaryShop.getMercenary().getAgility();
	}

	public int getHp() {
		return hp;
	}

	public int getMp() {
		return mp;
	}

	public int getLuck() {
		return 0;
	}

	public int getVianyType() {
		if(mercenaryShop == null || mercenaryShop.getMercenary() == null){
			return 0;
		}
		return mercenaryShop.getMercenary().getViany();
	}

	public int getBufProperty(int pro) {
		return 0;
	}

	public IEquipment[] getUsedEquipments() {
		if(useEquipment == null){
			useEquipment = MercenaryService.getUsedEquipment(mercenaryShop.getMercenary().getUsedequipments());
		}
		return useEquipment;
	}
	
	public void addDiamondShineBuf(int[] level) {
	}

	public List getDiamondShineList() {
		return diamondShineBufs;
	}

	public int getDiamondShineBufAttri(int pro) {
		return 0;
	}

	public int calculateMaxHp() {
		return maxhp;
	}

	public int calculateMaxMp() {
		return maxmp;
	}

	public Pet[] getOetherPet() {
		return null;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public void setMp(int mp) {
		this.mp = mp;
	}

	public int getMaxMp() {
		return maxmp;
	}

	public int getMaxHp() {
		return maxhp;
	}
	
	public void setMaxHp(int maxhp){
		this.maxhp = maxhp;
	}
	
	public void setMaxMp(int maxmp){
		this.maxmp = maxmp;
	}

	public boolean hasItem(IItem item, int count) {
		return false;
	}

	public boolean containsAbility(Ability ability) {
		return false;
	}

	public IEquipment getWeapon() {
		if(useEquipment == null){
			return null;
		}
		if(useEquipment[7] == null){
			return null;
		}
		return useEquipment[7];
	}

	public void incCampDeadTime(int playerLevel) {
	}

	public Buf[] getBufs() {
		return new Buf[0];
	}

	public void removeBuf(Buf buf, Changed changed) {
	}

	public int getCredit() {
		return 0;
	}

	public int hasCampLoopTask() {
		return 0;
	}
	
	public int getClientDataVersion(){
		int flag = 0;
		if(client != null ){
			flag = client.getDataVersion();
		}
		return flag;
	}

	public int addItem(IItemTemplate template, int count, Changed changed,
			int dataVersion) {
		return 0;
	}

	public void addCampWin(int win) {
	}

	public void addCampLost(int lost) {
	}

	public void addCredit(int credit, Changed changed) {
	}

	public int decCredit(int credit, Changed changed) {
		return 0;
	}

	public void addEnemy(int id, String name, long lastTime) {
	}

	public void addSneaks(int num) {
	}

	public void setDeadTime(int value) {
	}

	public void removeUsedEquipmentDurability(IEquipment equ, int value,
			Changed changed) {
	}

	public short getMapId() {
		return 0;
	}

	public int getMoeny() {
		return 0;
	}

	public Grid getEquipmentByInstanceid(int instanceId){
    	for (int i = 0; i < useEquipment.length; i++) {
    		IEquipment item = useEquipment[i];
			if (item.getId() == instanceId){
				Grid grid = new Grid();
				grid.item = item;
				grid.count = 1;
            	return grid;
            }
        }
    	return null;
    }

	public IItem completeRemoveItem(IItem item, int count, Changed changed) {
		return null;
	}

	public boolean hasItem(int itemId) {
		return false;
	}

	public void acquire() {
	}

	public void setBattle(boolean battleFlag) {
	}

	public void clearDeadTime() {
	}

	public boolean hasTask(short taskId) {
		return false;
	}

	public void release() {
	}

	public int getItemCount(int itemId) {
		return 0;
	}

	public int getRef() {
		return 0;
	}

	public int calcOnlineLife() {
		return 0;
	}

	public void setUnlineOnlineLife(int life) {
	}

	public int getFame() {
		return 0;
	}

	public void setLastlogoutTime(Date lastlogoutTime) {
	}

	public int getAccountId() {
		return mercenaryShop.getMercenary().getAccountid();
	}

	public void reset() {
	}

	public Player getPlayer() {
		return null;
	}

	public List getImage() {
		return null;
	}

	public void setDefaultFace() {
	}

	public RoleFaceData completeAddRoleFace(int face, int count,
			Changed changed, long time) {
		return null;
	}

	public void resetImage() {
	}

	public void addMoney(int money, Changed changed) {
	}

	public void setMoeny(int moeny) {
	}

	public int getFriendFavorite(PlayerData player) {
		return 0;
	}

	public Friend[] getFriends() {
		return new Friend[0];
	}

	public boolean hasEquipmented(int itemId) {
		for (int i = 0; i < useEquipment.length; i++) {
            if (useEquipment[i] != null) {
                if (useEquipment[i].getItemId() == itemId) {
                    return true;
                }
            }
        }
        return false;
	}

	public int getArenaV1Id() {
		return 0;
	}

	public void decMoney(int money, Changed changed) {
	}

	public void setArenaV1Id(int arenaV1Id) {
	}

	public int getArenaLevel() {
		return 0;
	}

	public void setArenaLevel(int arenaLevel) {
	}

	public int getArenaV2Id() {
		return 0;
	}

	public void setArenaV2Id(int arenaV2Id) {
	}

	public int getArenaV3Id() {
		return 0;
	}

	public void setArenaV3Id(int arenaV3Id) {
	}

	public int getTongId() {
		return 0;
	}

	public void setTongTitle(String tongTitle) {
	}

	public int getTongDuty() {
		return 0;
	}

	public void setTongDuty(int tongDuty) {
	}

	public String getTongTitle() {
		return null;
	}

	public void setTongId(int tongId) {
	}

	public void setContribution(int contribution) {
	}

	public int getContribution() {
		return 0;
	}

	public void removeFavor(Pet pet, int value, Changed changed, Random rnd) {
	}
	
	public short[] getSkillList(){
		if(skillList == null){
			boolean except = mercenaryShop.getMercenary().getProfession() == 0;
			skillList = MercenaryConstants.getSkillList(mercenaryShop.getMercenary().getAbilities(), except);
		}
		return skillList;
	}

	public Buf getCampBuf(int pro) {
		return null;
	}

	public byte getFantasyGemLightLevel() {
		return 0;
	}

	public byte getHolyGemLightLevel() {
		return 0;
	}

	public void setFantasyGemLightLevel(byte level) {
		
	}

	public void setHolyGemLightLevel(byte level) {
		
	}

	public void setBossRushStage(int bossRushStage) {
		
	}

	public int[] getSuitEffectDiamondAddValue() {
		return null;
	}

	@Override
	public int calculateMaxHpWithoutDiamondShine() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getTrainLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getTrainAttributeAddValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVipNewLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getMagicPosLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getMagicPosFloor() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
