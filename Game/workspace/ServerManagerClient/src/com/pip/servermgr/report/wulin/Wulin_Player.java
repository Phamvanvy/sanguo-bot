package com.pip.servermgr.report.wulin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.servermgr.report.IPlayer;
import com.pip.util.ResultRow;
import com.pip.wulin2.server.bean.Player;
import com.pip.wulin2.server.stage.Effect;
import com.pip.wulin2.server.stage.Grid;
import com.pip.wulin2.server.stage.PlayerData;
import com.pip.wulin2.server.stage.UnionTecControl;
import com.pip.wulin2.server.stage.item.ExtendedItem;
import com.pip.wulin2.server.stage.item.IEquipment;
import com.pip.wulin2.server.stage.item.IItem;
import com.pip.wulin2.server.stage.item.mount.Mount;
import com.pip.wulin2.server.stage.item.pet.Pet;
import com.pip.wulin2.server.stage.learn.LearnSkills;
import com.pip.wulin2.server.stage.skill.SkillDef;
import com.pip.wulin2.server.stage.skill.Union;
import com.pip.wulin2.server.stage.unionscience.UnionScience;


public class Wulin_Player implements IPlayer {
	public byte[] currenttask;
	public byte[] finishedtask;
	public int firstItemID;
	public int consumeMoney;
	
	public int[] xinfa = new int[]{0,0,0,0,0,0,0,0,0};
	
	public Player innerPlayer;
	public PlayerData playerData;		
	private int maxPetsLevel; //pets max level 
	
	public static Wulin_Player parse(ResultRow row) throws IOException {
		Wulin_Player ret = new Wulin_Player();
		
		// 创建一个武林服务器的Player对象
		ret.innerPlayer = new Player();
		ret.innerPlayer.setId(row.getInt(1));
		ret.innerPlayer.setAccountId(row.getInt(2));
		ret.innerPlayer.setPlayerName(row.getString(3));
		ret.innerPlayer.setLevel(row.getInt(4));
		ret.innerPlayer.setMapId((short)row.getInt(5));
		ret.innerPlayer.setX((short)row.getInt(6));
		ret.innerPlayer.setY((short)row.getInt(7));
		ret.innerPlayer.setSex((byte)row.getInt(8));
		ret.innerPlayer.setExp(row.getInt(9));
		ret.innerPlayer.setMoney(row.getInt(10));
		ret.innerPlayer.setTongId(row.getInt(11));
		ret.innerPlayer.setCreateTime(row.getDate(12));
		ret.innerPlayer.setLastLoginTime(row.getDate(13));
		ret.innerPlayer.setCredit(row.getInt(14));
		ret.innerPlayer.setLeavePoints(row.getInt(15));
		ret.innerPlayer.setBasicItems((byte[])row.getObject(16));
		ret.innerPlayer.setPets((byte[])row.getObject(17));
		ret.innerPlayer.setMetaItems((byte[])row.getObject(18));
		ret.innerPlayer.setEquipments((byte[])row.getObject(19));
		ret.innerPlayer.setUsedEquipments((byte[])row.getObject(20));
		ret.innerPlayer.setFriends((byte[])row.getObject(21));
		ret.innerPlayer.setPaletteid((byte)row.getInt(23));
		ret.innerPlayer.setRestValue(row.getInt(25));
		ret.innerPlayer.setAddedGridSize(row.getInt(26));
		ret.innerPlayer.setAbilityTimes(row.getInt(27));
		ret.innerPlayer.setValid(row.getInt(28) == 1);
		ret.innerPlayer.setBankSize(row.getInt(30));
		ret.innerPlayer.setBankItems((byte[])row.getObject(31));
		ret.innerPlayer.setPlayerUnionId(row.getInt(32));
		ret.innerPlayer.setLevelUpPoint(row.getInt(33));
		ret.innerPlayer.setLastStation((byte[])row.getObject(34));
		ret.innerPlayer.setTongMoney(row.getInt(35));
		ret.innerPlayer.setJbCount(row.getInt(36));
		ret.innerPlayer.setTongCredit(row.getInt(37));
		ret.innerPlayer.setFoes((byte[])row.getObject(38));
		ret.innerPlayer.setUnionCredit(row.getInt(39));
		ret.innerPlayer.setKmoney(row.getInt(40));
		ret.innerPlayer.setAchieveCount(row.getInt(41));
		ret.innerPlayer.setTransferLevel(row.getInt(42));
		ret.innerPlayer.setBableCredit(row.getInt(43));
		ret.innerPlayer.setVipCardType(row.getInt(44));
		ret.innerPlayer.setImoneyPoint(row.getInt(45));
		ret.innerPlayer.setStatistics((byte[])row.getObject(46));
		/*ret.initPetsLevel();//初始化宠物数据*/
		try {
			ret.playerData = new PlayerData(ret.innerPlayer,0);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("解析数据错误");
		}
		
		return ret;
	}
	
	/**
	 * 取得某个统计项数据。
	 * @param type 参见Sanguo_ReportEngine里的常量
	 * @return 可能是Boolean, Integer, Float
	 */
	public Object getValue(int type) {
		switch (type) {
		case Wulin_ReportEngine.TYPE_ISALIVE:
			// 是否存活
			return (System.currentTimeMillis() - innerPlayer.getLastLoginTime().getTime()) / 86400000L < 7;
		case Wulin_ReportEngine.TYPE_LIVETIME:
			// 存活天数
			return (int)((innerPlayer.getLastLoginTime().getTime() - innerPlayer.getCreateTime().getTime()) / 86400000L);
		case Wulin_ReportEngine.TYPE_LEVEL:
			// 等级
			return innerPlayer.getLevel();
		case Wulin_ReportEngine.TYPE_ISPAY:
			// 是否付费
			return consumeMoney > 0;
		case Wulin_ReportEngine.TYPE_PAY:
			// 消费金额
			return (int)(consumeMoney / 3.6);
		case Wulin_ReportEngine.TYPE_MONEY:
			// 金钱
			return (int)innerPlayer.getMoney();
		case Wulin_ReportEngine.TYPE_UNION:
			// 门派
			return innerPlayer.getPlayerUnionId();
		case Wulin_ReportEngine.TYPE_EQULEVEL:
			// 装备总价值In
			// TODO Integer
			return 0;
		case Wulin_ReportEngine.TYPE_ALL_STONE_BY_LEVEL:
			//所有宝石的分类统计数
			return getAllStoneByLevel();	
		case Wulin_ReportEngine.TYPE_JEWELLEVEL:
			// 宝石总等级
			return getStoneLevelSum();
			
		case Wulin_ReportEngine.TYPE_HOLECOUNT:
			// 镶嵌宝石数
			return getHoleCount();
			
		case Wulin_ReportEngine.TYPE_STARLEVEL:
			// 总星级
			return getStartLevelSum();
			
		case Wulin_ReportEngine.TYPE_ZIZHILEVEL:
			// 总资质等级
			return getZiZhiLevelSum();
		case Wulin_ReportEngine.TYPE_SKILLPOINT:
			// 技能点使用率
			return this.getUseSkillPointPro();
		case Wulin_ReportEngine.TYPE_CLAZZ:
			// 职业
			return (int)innerPlayer.getPaletteid();
		case Wulin_ReportEngine.TYPE_ATTRPOINT:
			// 属性点使用率
			return this.getUsePropertyPointPro();
		case Wulin_ReportEngine.TYPE_PETLVL:
			// 最高宠物等级
			initPetsLevel();//初始化宠物数据
			return this.getMaxLevelPets();
		case Wulin_ReportEngine.TYPE_PETCOUNT:
			// 宠物数量
			return this.getPetSize();
		case Wulin_ReportEngine.TYPE_PETEQULEVEL:
			// 宠物装备总价值
			// TODO Integer
			return 0;
		case Wulin_ReportEngine.TYPE_PETJEWELLEVEL:
			// 宠物宝石总等级
			return this.getPetEquipStoneLevelSum();
		case Wulin_ReportEngine.TYPE_PETHOLECOUNT:
			// 宠物镶嵌宝石数			
			return this.getPetEquipHoleCount();
		case Wulin_ReportEngine.TYPE_PETHOLECOUNTMAX:
			// 宠物镶嵌宝石数	中 最多的那只		
			return this.getPetEquipHoleCountMax();
		case Wulin_ReportEngine.TYPE_PETSTARLEVEL:
			// 宠物装备总星级
			return this.getPetEquipStarLevelSum();
		case Wulin_ReportEngine.TYPE_PETZIZHILEVEL:
			// 宠物装备总资质等级
			return this.getPetEquipZiZhiLevelSum();
		case Wulin_ReportEngine.TYPE_MANULEVEL:
			// 打造等级
			// TODO Integer
			return 0;
		case Wulin_ReportEngine.TYPE_FRIENDCOUNT:
			// 好友数
			return this.getFriendsAmount();
		case Wulin_ReportEngine.TYPE_FABAOCOUNT:
			// 法宝数			
			return this.getFaBaoCount(this.playerData.getAllEquipment(), 
			this.playerData.getUsedEquipments());
		case Wulin_ReportEngine.TYPE_FABAOLEVEL:
			// 最高法宝等级
			return this.getFaBaoMaxLevel(this.playerData.getAllEquipment(), 
					this.playerData.getUsedEquipments());
		case Wulin_ReportEngine.TYPE_FIRSTBUYITEM:
			// 首次购买物品
			return firstItemID;
		case Wulin_ReportEngine.TYPE_HORSECOUNT:
			// 坐骑数	
			return this.getFlyPetAmount();
		case Wulin_ReportEngine.TYPE_ISHORSEENH:
			// 是否强化过坐骑
			// TODO Boolean
			return false;
		case Wulin_ReportEngine.TYPE_FINISHQUESTCOUNT:
			// 完成任务数
			return this.getCompletedTasksAmount();
		case Wulin_ReportEngine.TYPE_QUESTCOUNT:
			// 当前任务数
			return this.getCurrentTasksAmount();
		case Wulin_ReportEngine.TYPE_HASTONG:
			// 是否有公会
			return innerPlayer.getTongId() > 0;
		case Wulin_ReportEngine.TYPE_TRANSFERLEVEL:
			// 传功等级
			return playerData.getTransferLevel();
		case Wulin_ReportEngine.TYPE_HAVE_PET_COUNT:
			//宠物个数（包括宠物蛋）
			return getPetCountSum();
		case Wulin_ReportEngine.TYPE_PET_MIX_MAXLEVEL:
			//宠物中最高的强化等级
			return getMaxMixLevelByPets();
		case Wulin_ReportEngine.TYPE_PET_MIX_MAXEXP:
			//宠物最高的强化所有累计经验
			return getMaxMixExpByPets();
		case Wulin_ReportEngine.TYPE_KMONEY:
			
			return (int)playerData.getKmoney();
		case Wulin_ReportEngine.TYPE_CREDIT:
			return playerData.getCredit();
		case Wulin_ReportEngine.TYPE_BABLECREDIT:
			return playerData.getBableCredit();
		case Wulin_ReportEngine.TYPE_ITEMCOUNT:
//			Shell shell = new Shell();
//			numDialog nd = new numDialog(shell);
//			shell.open();
			return getExtentItemCount(900151);
		case Wulin_ReportEngine.TYPE_USED_IMONEY_POINT:
			return innerPlayer.getImoneyPoint();
		case Wulin_ReportEngine.TYPE_VIP_TYPE:
			return innerPlayer.getVipCardType();
		case Wulin_ReportEngine.TYPE_XINFA_ALL:
			int xinfaAll = 0;
			for(int i=1;i<=6;i++){
				xinfaAll+= xinfa[i];
			}
			return xinfaAll;
		case Wulin_ReportEngine.TYPE_XINFA_ATK:
			int xinfaAtk = 0;
			for(int i=1;i<=3;i++){
				xinfaAtk+= xinfa[i];
			}
			return xinfaAtk;
		case Wulin_ReportEngine.TYPE_XINFA_DEF:
			int xinfaDef = 0;
			for(int i=4;i<=6;i++){
				xinfaDef+= xinfa[i];
			}
			return xinfaDef;
		case Wulin_ReportEngine.TYPE_SHENGJIFANLI:
			int temp = this.playerData.getStatistics((short)160);//升级返利活动在statistics中的id 
			if(temp > 0 ){
				return true;
			}else{
				return false;
			}
		}
		
		return 0;
	}
	
	class numDialog extends Dialog{
		private Text textLevel;
		int count;
		
	
		public numDialog(Shell parent) {
			super(parent);
		}
		
	}	
	

	// 总资质等级
	public int getZiZhiLevelSum(){
		int grade = 0;
		Grid[] equs = playerData.getEquipments();
		for(int i=0;i<equs.length;i++){
			IEquipment equ = (IEquipment) equs[i].item;
			grade += equ.getGrade();
		}
		return grade;
	}
	
	
	// 总星级
	public int getStartLevelSum(){
		int start = 0;
		Grid[] equs = playerData.getEquipments();
		for(int i=0;i<equs.length;i++){
			IEquipment equ = (IEquipment) equs[i].item;
			start += equ.getProperty(IEquipment.EQUIP_ADD_REFINE_SRAR);
		}
		return start;
	}
	
	
	//角色装备上镶嵌宝石总数
	public int getHoleCount(){
		int count = 0;
		Grid[] equs = playerData.getEquipments();
		for(int i=0;i<equs.length;i++){
			IEquipment equ = (IEquipment) equs[i].item;
			count += equ.getStoneCount();
		}
		
		IEquipment[] equs2= playerData.getUsedEquipments();
		for(int i=0;i<equs2.length;i++){
			IEquipment equ = equs2[i];
			if(equ != null)
				count += equ.getStoneCount();
		}
		
		
		
		return count;
		
	}
	
	public int getAllStoneByLevel(){
		
		int all = 0;
		
		Grid[] equs = playerData.getEquipments();
		for(int i=0;i<equs.length;i++){
			IEquipment equ = (IEquipment) equs[i].item;
			if(equ != null){
				for(int j=0;j<equ.getStones().length;j++){
					all = addStoneByLevel(all, equ.getStones()[j],1);
					
				}
			}
		}
		
		IEquipment[] equs2= playerData.getUsedEquipments();
		for(int i=0;i<equs2.length;i++){
			IEquipment equ = equs2[i];
			if(equ != null){
				for(int j=0;j<equ.getStones().length;j++){
					all = addStoneByLevel(all, equ.getStones()[j],1);
				}
			}
		}
		
		Grid[] extend = playerData.getExtendedItems(); //获取扩展物品
		for(int i=0;i<extend.length;i++){
			all = addStoneByLevel(all, extend[i].item.getItemId(),extend[i].count);
		}
		
		Grid[] bank = playerData.getBankItems();//仓库物品
		for(int i=0;i<bank.length;i++){
			if (bank[i] != null) {
				if (bank[i].item.getType() == IItem.TYPE_EQU) {
					IEquipment equ  = (IEquipment) bank[i].item;
					for(int j=0;j<equ.getStones().length;j++){
						all = addStoneByLevel(all, equ.getStones()[j],1);
					}
				}else if(bank[i].item.getType() == IItem.TYPE_EXTENDED){
					all = addStoneByLevel(all, bank[i].item.getItemId(),bank[i].count);
				}
			}
		}
		
		Pet[] pets = playerData.getPets();
		for(int i=0;i<pets.length;i++){
			IEquipment[] equsPet= pets[i].getPetEquips();
			for(int j=0;j<equsPet.length;j++){
				IEquipment equ = equsPet[j];
				if(equ != null){
					for(int k=0;k<equ.getStones().length;k++){
						all = addStoneByLevel(all, equ.getStones()[k],1);
					}
				}
			}
		}
		
		return all;
		
	}
	
	
	
	//角色装备上镶嵌宝石的等级总和
	public int getStoneLevelSum(){
		int jewellLevels = 0;
		Grid[] equs1 = playerData.getEquipments();
		for(int i=0;i<equs1.length;i++){
			IEquipment equ = (IEquipment) equs1[i].item;
			int[] stonges = equ.getStones();
			for(int j=0;j<stonges.length;j++){
				jewellLevels += stoneId2Level(stonges[j]);
			}
		}
		return jewellLevels;
	}
	
	//取得单个宝石的等级
	public int stoneId2Level(int stoneId){
		if(stoneId < 900001 || (stoneId > 900133 && stoneId < 900150)||stoneId > 900169){
			return 0;
		}
		if(stoneId >= 900001 && stoneId <=900019){//一级宝石
			return 1;
		}else if(stoneId >= 900020 && stoneId <=900038){//二级宝石
			return 2;
		}else if(stoneId >= 900039 && stoneId <=900057){//三级宝石
			return 3;
		}else if(stoneId >= 900058 && stoneId <=900076){//四级宝石
			return 4;
		}else if(stoneId >= 900077 && stoneId <=900095){//五级宝石
			return 5;
		}else if(stoneId >= 900096 && stoneId <=900114){//六级宝石
			return 6;
		}else if(stoneId >= 900115 && stoneId <=900133){//七级宝石
			return 7;
		}else if(stoneId >= 900151 && stoneId <=900169){//三级瑕疵宝石
			return 3;
		}
		return 0;
	}
	
	/**
	 * 获取已完成任务的数量
	 * @throws IOException 
	 */
	public int getCompletedTasksAmount(){		
		if (this.finishedtask != null && this.finishedtask.length > 0) {	
			return this.finishedtask.length/2;
		}		
		return 0;
	}
	/**
	 * 获取当前拥有的任务数量
	 * @return
	 */
	public int getCurrentTasksAmount(){
		if (this.currenttask!= null && this.currenttask.length > 0) {	
			return this.currenttask.length/2;
		}		
		return 0;		
	}
	
	/**
	 * 获取坐骑数量
	 * @return
	 */
	public int getFlyPetAmount(){
		int hasMountAmount = 0;
		Mount [] mounts = this.playerData.getMounts();
		for (Mount mount : mounts) {
			if (mount == null || mount.isOverTime())//过期 不存在的  跳出
				continue;
		
			if(mount.getType() == Mount.UNIONMANAGER_MOUNT_TYPE
					|| mount.getType() == Mount.NETPLAYER_MOUNT_TYPE)//掌门坐骑或者试用坐骑 暂不做处理 
				continue; 
			
			hasMountAmount ++;
		}				
		return hasMountAmount;
	}
	
	/**
	 * 获取好友数量
	 * @return
	 * @throws IOException 
	 */
	public int getFriendsAmount(){
		return this.playerData.getFriends().length;		
	}
	
	/**
	 * Initialization list pets
	 */
	public void initPetsLevel(){
		Pet [] pets = this.playerData.getPets();
		for (Pet pet : pets) {
			if(pet.getLevel() > this.maxPetsLevel)
				maxPetsLevel = pet.getLevel();
		}			
	}
	
	/**
	 * 获取宠物数量
	 * @return
	 */	
	public int getPetSize() {
		return this.playerData.getPetCount();
	}

	/**
	 * 获取最高的宠物等级
	 * @return
	 */
	public int getMaxLevelPets(){
		return this.maxPetsLevel;
	}
	
	/**
	 * 获取法宝的最高等级 
	 * @return
	 */
	public int getFaBaoMaxLevel(List<IEquipment> equips, IEquipment [] usedEquips){
		IEquipment[] es = new IEquipment[equips.size()];
		es = equips.toArray(es);
		int level1 = calcOfEquipmentFaBao(es, CALC_MAXLEVEL);
		int level2 = calcOfEquipmentFaBao(usedEquips, CALC_MAXLEVEL);
		return Math.max(level1, level2);
	}
	/**
	 * 计算法宝数量
	 * @param equips
	 * @return
	 */
	public int getFaBaoCount(List<IEquipment> equips, IEquipment [] usedEquips){
		IEquipment[] es = new IEquipment[equips.size()];
		es = equips.toArray(es);
		return calcOfEquipmentFaBao(es, CALC_COUNT)
		+ calcOfEquipmentFaBao(usedEquips, CALC_COUNT);
	}
	
	private static final int CALC_COUNT = 0;
	private static final int CALC_MAXLEVEL = 1; 
	/**
	 * 在法宝装备中计算相应的值.(如等级、数量等)
	 * @param equips
	 * @param type 计算的类型 0.计算法宝数量  1.计算法宝最高等级 
	 * @return
	 */
	private int calcOfEquipmentFaBao(IEquipment[] equips, int type){
		int count = 0 ;
		int maxLevel = 0;
		for (IEquipment iEquipment : equips) {
			//必须是法宝装备
			System.out.println("iequipment " + iEquipment);
			if(iEquipment instanceof IEquipment){
				if(iEquipment.getPart() == IEquipment.EQUIP_TYPE_SHIPIN2){
					if(type == CALC_COUNT){
						count ++;
					}else if(type == CALC_MAXLEVEL){
						if(maxLevel < iEquipment.getLevel()){
							maxLevel = iEquipment.getLevel();
						}
					}
				}
			}
		}
		//return result
		if(type == CALC_COUNT){
			return count;
		}else if(type == CALC_MAXLEVEL){
			return maxLevel;
		}
		return 0;//default
	}
	
	/**
	 * 获取宠物装备的总星级
	 * @return
	 */
	public int getPetEquipStarLevelSum(){
		Pet [] pets = this.playerData.getPets();
		return this.traversePetsListAndCalculation(pets, PET_CALC_STAR_SUM);
	}
	
	/**
	 * 获取宠物装备的总资质
	 * @return
	 */
	public int getPetEquipZiZhiLevelSum(){
		Pet [] pets = this.playerData.getPets();
		return this.traversePetsListAndCalculation(pets, PET_CALC_GRADE_SUM);
	}
	
	/**
	 * 获取宠物装备镶嵌宝石的数量 
	 * @return
	 */
	public int getPetEquipHoleCount(){
		Pet [] pets = this.playerData.getPets();
		return this.traversePetsListAndCalculation(pets, PET_CALC_STONE_NUM_SUM);
	}
	/**
	 * 获取宠物装备镶嵌宝石的数量 中 单只宠物宝石最多的
	 * @return
	 */
	public int getPetEquipHoleCountMax(){
		Pet [] pets = this.playerData.getPets();
		return this.traversePetsListAndCalculationMax(pets, PET_CALC_STONE_NUM_SUM);
	}
	
	/**
	 * 宠物装备上镶嵌宝石的等级总和
	 * @return
	 */
	public int getPetEquipStoneLevelSum(){
		Pet [] pets = this.playerData.getPets();
		return this.traversePetsListAndCalculation(pets, PET_CALC_STONE_LEVEL_SUM);
	}
	
	
	private static final short PET_CALC_STAR_SUM = 0;
	private static final short PET_CALC_GRADE_SUM = 1;
	private static final short PET_CALC_STONE_NUM_SUM = 2;
	private static final short PET_CALC_STONE_LEVEL_SUM = 3;
	
	/**
	 * 遍历宠物并且计算
	 * @return
	 */
	private int traversePetsListAndCalculation(Pet [] pets, short calcType){
		int ret = 0;
		for (Pet pet : pets) {
			IEquipment [] es = pet.getPetEquips();
			ret += calcValueOfPetEquipments(es, calcType);
		}
		return ret;
	}
	
	/**
	 * 遍历宠物并且计算 得到最大值
	 * @return
	 */
	private int traversePetsListAndCalculationMax(Pet [] pets, short calcType){
		int ret = 0;
		int result = 0;
		for (Pet pet : pets) {
			ret = 0;
			IEquipment [] es = pet.getPetEquips();
			ret += calcValueOfPetEquipments(es, calcType);
			if(ret > result){
				result = ret;
			}
		}
		
		return result;
	}
	
	
	/**
	 * 计算宠物装备的属性
	 * @param es
	 * @param type 0.总星级 1.总资质 2.总宝石数量 3.总宝石 等级
	 * @return
	 */
	private int calcValueOfPetEquipments(IEquipment [] es, short type){
		int ret = 0;
		for (IEquipment iEquipment : es) {
			if(iEquipment instanceof IEquipment){
				switch(type){
				case PET_CALC_GRADE_SUM:
					ret += iEquipment.getGrade();
					break;
				case PET_CALC_STAR_SUM:
					ret += iEquipment.getProperty(IEquipment.EQUIP_ADD_REFINE_SRAR);
					break;
				case PET_CALC_STONE_NUM_SUM:
					ret += iEquipment.getStoneCount();
					break;
				case PET_CALC_STONE_LEVEL_SUM:
					int[] stonges = iEquipment.getStones();
					for(int j=0; j < stonges.length; j++){
						ret += stoneId2Level(stonges[j]);
					}
					break;
				default :
					return 0;
				}
				
			}
		}
		return ret;
	}
	
	/**
	 * 获取角色使用属性点的概率
	 * @return
	 */
	public int getUsePropertyPointPro(){
		int levelPoint = this.playerData.getLevel(); //总共获得属性点
		int hasPoint = this.playerData.getLeavePoints();//剩余可分配的属性点
		return levelPoint - hasPoint;
	}
	
	/**
	 * 获取角色使用技能点的概率
	 * @return
	 */
	public int getUseSkillPointPro(){
		//int oldLevel = 1;
		int skillLevelPoint = this.playerData.getAllLevelUpPoint();//总共获得的技能升级点
		
	/*	for (int i = oldLevel; i <= this.playerData.getLevel(); i++) {
			System.out.print("级别" + i);
			System.out.println("  " + skillLevelPoint + "应该加" + LevelUpPoint.getLevelPoint(i));
			skillLevelPoint += LevelUpPoint.getLevelPoint(i); // 如果不是按每升1级加1点时,打开注释,配置升点规则
			System.out.println("现值" + skillLevelPoint + "\n ------------");
		}*/
		int hasSkillPoint = this.innerPlayer.getLevelUpPoint();//当前的技能升级点
		
		System.out.println("role level " + this.playerData.getLevel());
		System.out.println("sum skill point : " + skillLevelPoint + "   has skill point " + hasSkillPoint);
		return skillLevelPoint - hasSkillPoint;
	}
	
	/**
	 * 获取还可以升级的技能列表
	 * @return
	 */
	public int getCanUpdateSkillCount(){
		List<SkillDef> skillList = new ArrayList<SkillDef>();
		List<SkillDef> l = SkillDef.getSkillByPalette(this.playerData.getCareer());// 此职业的技能表
		
		int union = this.playerData.getUnion();
		for (SkillDef s : l) {
			// 老门派不添加新职业技能
			if (union >= 0 && union <= 4 && s.id >= 1085
					&& s.id <= 1117) {
				continue;
			}
			skillList.add(s);
		}
		if (union >= 0 && union <= 4) {// 只有老门派下发门派技能
			List<Integer> s = Union.unions.get(union).getSkills(this.playerData.getCareer());// 获取门派技能
			if (s != null) {
				for (int i = 0; i < s.size(); i++) {
					int skillid = s.get(i);
					SkillDef skillDef = SkillDef.getSkill(skillid);
					if (skillDef != null) {
						skillList.add(skillDef);
					}
				}
			}
		} else if (union >= 5 && union <= 7) {
			// 增加门派科技技能的计算
			List<UnionTecControl> list = this.playerData.uScience.get(new Integer(
					UnionScience.UNION_TEC_BATTLE));// 门派科技类型2：战斗科技
			if (list != null && list.size() > 0) {
				UnionTecControl utvc = null;
				for (int j = 0; j < list.size(); j++) {
					utvc = list.get(j);
					if (utvc != null && utvc.getCurState()) {
						boolean isOver = (Math.abs((new Date()).getTime() - utvc.getDate()) / PlayerData.UNION_TEC_VALIDITY_TIME >= 1) ? true : false;
						if (!isOver) {
							int skillId = utvc.getUnionTecLevel();
							SkillDef skillDef = SkillDef.getSkill(skillId);
							if (skillDef != null) {
								skillList.add(skillDef);
							}
						}
					}
				}
			}
		}
		List<SkillDef> canUpdata = new ArrayList<SkillDef>();
		SkillDef[] skills = new SkillDef[skillList.size()];
		skillList.toArray(skills);
		for (SkillDef skill : skills) {
			int index = this.playerData.hasAbilities(skill);
			if (index >= 0) {				
				int currLevel = this.playerData.abilitiesLevel.get(index); // 当前的级别
				if (skill.level >= currLevel + 1) {
					LearnSkills ls = new LearnSkills();
					ls.setSkillid(skill.id);
					ls.setSkillLevel(currLevel + 1);
					int[] skillInfo = skill.getSkillInfo(currLevel + 1);
					if (this.innerPlayer.getLevel() >= skillInfo[SkillDef.SKILL_INFO_PLAYERLV]
							&& this.innerPlayer.getLevelUpPoint() >= ls.getCostPoint()) {
						canUpdata.add(skill);
					}
				}
			}
		}
		
		return canUpdata.size();
	}
	
	//拥有的宠物个数
	public int getPetCountSum(){
		if(playerData.getPets() == null)return 0;
		
		int petsCount = playerData.getPets().length;
		Grid[] gs = playerData.getExtendedItems(); //获取扩展物品
		
		for (Grid grid : gs) {
			if(grid.item instanceof ExtendedItem){
				ExtendedItem item = (ExtendedItem)grid.item;
				Effect [] efs  = item.getEffects();
				for (Effect e : efs) {
					if(e.getType() == 47){
						petsCount += grid.count;
					}
				}
			}
		}
		return petsCount;
	}
	
	//获取宠物中  最高的强化等级
	public int getMaxMixLevelByPets(){		
		Pet [] pets = playerData.getPets();
		if(pets == null) return 0;
			
		int maxLevel = 0;
		for (Pet pet : pets) {
			if(pet == null ) continue;
			
			if(pet.getMixLevel() > maxLevel){
				maxLevel = pet.getMixLevel();
			}
		}
		return maxLevel;
	}
	
	//获取宠物最高的强化所有累计经验
	public int getMaxMixExpByPets(){		
		Pet [] pets = playerData.getPets();
		if(pets == null) return 0;
		
		//计算经验
		int[] MIXUPLEVELEXP_TABLE_PET = {
				  100,200,400,800,1600,3200  
			  };
		List<Integer> allExps = new ArrayList<Integer>();
				
		for (Pet pet : pets) {
			if(pet == null) continue;
			
			if(pet != null){
				int exp = pet.getMixExp();
				int mixLevel = pet.getMixLevel();
				
				if(mixLevel > 0){
					for(int i = 0; i < mixLevel; i ++){
						exp += MIXUPLEVELEXP_TABLE_PET[i];
					}
				}
				allExps.add(exp);
			}
		}
		if(allExps.size()>0){
			return Collections.max(allExps);
		}else{
			return 0;
		}
	}
	
	public int addStoneByLevel(int all ,int stoneid,int count){
	
//		if((stoneid >= 900001 && stoneid <= 900057) || (stoneid >= 900151 && stoneid <= 900169)){
//			all += count;
//			return all;
//		} else if (stoneid >= 900058 && stoneid <= 900095){
//			all += 1000*count;
//			return all;
//		} else if (stoneid >= 900096 && stoneid <= 900133){
//			all += 1000000*count;
//			return all;
//		}
		
		
//		if((stoneid >= 900001 && stoneid <= 900057) || (stoneid >= 900151 && stoneid <= 900169)){
//			all += count;
//			return all;
//		} else if (stoneid >= 900058 && stoneid <= 900095){
//			all += count;
//			return all;
//		} else if (stoneid >= 900096 && stoneid <= 900133){
//			all += count;
//			return all;
//		}
		
		if(stoneid >= 900151 && stoneid <= 900169){
			all += count;
			return all;
		}
		
		return all;
	}
	
	public int getExtentItemCount(int id){
		int count = 0;
		Grid[] gs = playerData.getExtendedItems(); //获取扩展物品
		
		for (Grid grid : gs) {
			if(grid.item.getItemId() == id){
				count += grid.count;
			}
		}
		return count;
	}
	
	
	
	
}
