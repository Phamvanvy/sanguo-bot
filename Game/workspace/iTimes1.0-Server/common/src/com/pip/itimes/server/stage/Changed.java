package com.pip.itimes.server.stage;

import java.io.*;
import java.util.*;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;


/**
 * @author Jeffrey
 * @version 1.0
 */
public class Changed {

    public static final byte SEX = 1;
    public static final byte FACE = 2;
    public static final byte RETURNTIMES = 3;
    public static final byte LEVEL = 4;
    public static final byte EXP = 5;
    public static final byte MONEY = 6;
    public static final byte CREDIT = 7;
    public static final byte STRENGTH = 8;
    public static final byte AGILITY = 9;
    public static final byte VITALITY = 10;
    public static final byte INTELLIGENCE = 11;
    public static final byte LUCK = 12;
    public static final byte HP = 13;
    public static final byte MP = 14;
    public static final byte LEAVEPOINTS = 15;
    public static final byte PATTACK = 16;
    public static final byte PDEFENSE = 17;
    public static final byte MATTACK = 18;
    public static final byte MDEFENSE = 19;
    public static final byte HIT = 20;
    public static final byte PARRY = 21;
    public static final byte PCRITICAL = 22;
    public static final byte MCRITICAL = 23;
    public static final byte ARMOR = 24;
    public static final byte GAINEXP = 25;
    public static final byte UPLEVELEXP = 26;
    public static final byte GRIDSIZE = 27;
    public static final byte POINT = 28;

    public static final byte SKILL_BLACKSMITHING = 29;
    public static final byte SKILL_ALCHEMY = 30;
    public static final byte SKILL_TAILOR = 31;
    public static final byte SKILL_HERBALISM = 32;
    public static final byte SKILL_HUNTERING = 33;
    public static final byte SKILL_MINING = 34;
    public static final byte SKILL_COOKING = 35;
    public static final byte SKILL_FISHING = 36;
    public static final byte REFRESH_ABILITY = 37;
    public static final byte REFRESH_CAMPTYPE = 38;
    public static final byte CAMPKING = 39;

    public static final byte PET_NAME = 40;
    public static final byte PET_LEVEL = 41;
    public static final byte PET_CURRENTPOINT = 42; //当前可分配的点数
    public static final byte PET_POINT = 43; //当前可兑换的点数
    public static final byte PET_FAVOR = 44;
    public static final byte PET_AGILITY = 45;
    public static final byte PET_STRENGTH = 46;
    public static final byte PET_VITALITY = 47;
    public static final byte PET_INTELLIGENCE = 48;
    public static final byte PET_HP = 49;
    public static final byte PET_MP = 50;
    public static final byte PET_EXP = 51;
    public static final byte PET_UPLEVELEXP = 52;
    public static final byte PET_RUNAWAY = 53;
    public static final byte PET_GRID = 54;
    public static final byte PET_NEXT_LEVEL_EXP = 55;
    public static final byte PET_PERCETPION_LEVEL = 56;        	// 悟性升级
    public static final byte PET_PERCETPION_POINT = 57;        	// 当前悟性的经验
    public static final byte PET_UPLEVELPERCETPIONPOINT = 58;   // 升级后经验重置
    public static final byte PET_NEXT_PERCEPTION_POINT = 59;	// 当前悟性最大值（下次升级需要的悟性经验）
    public static final byte PET_SPIRITUALITY_LEVEL = 60;		// 灵性升级
    public static final byte PET_ADD_SKILL = 70;				// 增加宠物技能

    public static final byte CREDIT_STRING = 61;
    public static final byte TITLE_STRING = 62;
    public static final byte PASSWORD = 63;
    public static final byte PLAYERNAME = 64;
    public static final byte NAMECOLOR = 65;
    public static final byte GUARDSTATE = 66;
    public static final byte BUILD_PROFICIENCY = 67;			// 打造熟练度
    public static final byte ADD_SUIT_STRENGTH = 68;			// 套装增加的力量
    public static final byte ADD_SUIT_INTELLIGENCE = 69;		// 套装增加的智力
    public static final byte ADD_SUIT_VITALITY = 70;			// 套装增加的体力
    public static final byte ADD_SUIT_AGILITY = 71;				// 套装增加的敏捷
    public static final byte ADD_SUIT_HP = 72;					// 新套装宝石加成血量
    public static final byte ADD_SUIT_MP = 73;					// 新套装宝石加成魔法
    public static final byte ADD_SUIT_PATTACK = 74;				// 新套装宝石加成物攻
    public static final byte ADD_SUIT_PDEFENSE = 75;			// 新套装宝石加成物防
    public static final byte ADD_SUIT_MATTACK = 76;				// 新套装宝石加成魔攻
    public static final byte ADD_SUIT_MDEFENSE = 77;			// 新套装宝石加成魔防
    public static final byte ADD_SUIT_HIT = 78;					// 新套装宝石加成命中
    public static final byte ADD_SUIT_PARRY = 79;				// 新套装宝石加成闪避
    public static final byte ADD_SUIT_ARMOR = 80;				// 新套装宝石加成免暴
    public static final byte ADD_SUIT_STRENGTH_DIAMOND = 81;	// 新套装宝石加成力量
    public static final byte ADD_SUIT_INTELLIGENCE_DIAMOND = 82;// 新套装宝石加成智力
    public static final byte ADD_SUIT_VITALITY_DIAMOND = 83;	// 新套装宝石加成体力
    public static final byte ADD_SUIT_AGILITY_DIAMOND = 84;		// 新套装宝石加成敏捷
    public static final byte ADD_NINE_EQU_DIAMOND_STR = 85;		// 全身9钻效果加成力量
    public static final byte ADD_NINE_EQU_DIAMOND_INT = 86;		// 全身9钻效果加成智力
    public static final byte ADD_NINE_EQU_DIAMOND_VIT = 87;		// 全身9钻效果加成体力
    public static final byte ADD_NINE_EQU_DIAMOND_AGI = 88;		// 全身9钻效果加成敏捷
    public static final byte ADD_TRAIN_PHIT = 89;				// 修心点加成攻击
    public static final byte ADD_TRAIN_PDEF = 90;				// 修心点加成防御
    public static final byte ADD_TRAIN_MHIT = 91;				// 修心点加成魔攻
    public static final byte ADD_TRAIN_MDEF = 92;				// 修心点加成魔防
    public static final byte ADD_TRAIN_HIT = 93;				// 修心点加成命中
    public static final byte ADD_TRAIN_NOCRI = 94;				// 修心点加成免爆
    public static final byte ADD_TRAINLEVE_DIAMOND_STR = -1;	// 聚灵等级宝石加成力量
    public static final byte ADD_TRAINLEVE_DIAMOND_INE = -2;	// 聚灵等级宝石加成智力
    public static final byte ADD_TRAINLEVE_DIAMOND_VIT = -3;	// 聚灵等级宝石加成体力
    public static final byte ADD_TRAINLEVE_DIAMOND_AGI = -4;	// 聚灵等级宝石加成敏捷
    public static final byte ADD_TRAINLEVE_DIAMOND_PATT = -5;	// 聚灵等级宝石加成物攻
    public static final byte ADD_TRAINLEVE_DIAMOND_PDEF = -6;	// 聚灵等级宝石加成物防
    public static final byte ADD_TRAINLEVE_DIAMOND_MATT = -7;	// 聚灵等级宝石加成魔攻
    public static final byte ADD_TRAINLEVE_DIAMOND_MDEF = -8;	// 聚灵等级宝石加成魔防
    public static final byte ADD_TRAINLEVE_DIAMOND_HIT = -9;	// 聚灵等级宝石加成命中
    public static final byte ADD_TRAINLEVE_DIAMOND_FLEE = -10;	// 聚灵等级宝石加成闪避
    public static final byte ADD_TRAINLEVE_DIAMOND_NOCRI = -11;	// 聚灵等级宝石加成免爆
    public static final byte ADD_TRAINLEVE_DIAMOND_HP = -12;	// 聚灵等级宝石加成血量
    public static final byte ADD_TRAINLEVE_DIAMOND_MP = -13;	// 聚灵等级宝石加成魔法
    public static final byte ADD_PLAYER_VIP_LEVEL = -14;
    public static final byte ADD_PLAYER_VIP_PAY = -15;
    
    //封印法阵提高属性
    public static final byte ADD_MAGIC_POS_ATTACK = -16;		
    public static final byte ADD_MAGIC_POS_MATTACK = -17;
    public static final byte ADD_MAGIC_POS_PDEF = -18;
    public static final byte ADD_MAGIC_POS_MDEF = -19;
    public static final byte ADD_MAGIC_POS_HIT = -20;
    public static final byte ADD_MAGIC_POS_PCRI = -21;
    public static final byte ADD_MAGIC_POS_MCRI = -22;
    public static final byte ADD_MAGIC_POS_FLEE = -23;
    public static final byte ADD_MAGIC_POS_NOCRI = -24;
    public static final byte ADD_MAGIC_POS_HP = -25;
    public static final byte ADD_MAGIC_POS_MP = -26;
    //end
    public static final byte ADD_PET_DIVINE = -27;				// 宠物占卜之力
    public static final byte ADD_PET_EVOLUTION_LEVEL = -28;				// 宠物进化等级
    public static final byte ADD_PET_EVOLUTION_PA = -29;				// 宠物进化
    public static final byte ADD_PET_EVOLUTION_MA = -30;				// 宠物进化
    public static final byte ADD_PET_EVOLUTION_PD = -31;				// 宠物进化
    public static final byte ADD_PET_EVOLUTION_MD = -32;				// 宠物进化
    public static final byte ADD_PET_EVOLUTION_HP = -33;				// 宠物进化
    public static final byte ADD_PET_EVOLUTION_TYPE = -34;				// 宠物进化
    
    public static final byte GRIDFULL = 100;
    public static final byte PETFULL = 101;
    
    public static final byte ADDHPMAX = 102;
    public static final byte ADDMPMAX = 103;
    public static final byte ADDPATTCMAX = 104;
    public static final byte ADDPATTCMIN = 105;
    public static final byte ADDMATTCMAX = 106;
    public static final byte ADDMATTCMIN = 107;
    
    public static final byte ADDNOCRI = 108;
    public static final byte ADDAGILITY = 109;
    public static final byte ADDSTRENGTH = 110;
    public static final byte ADDINTELLIGENCE = 111;
    public static final byte ADDPCRITICAL = 112;
    public static final byte ADDMCRITICAL = 113;
    
    public static final byte ADDCAMPBUFF = 114;				//增加阵营BUFF
    public static final byte KILL_POINT = 115;				//杀戮点数
    public static final byte VIANY_TYPE = 116;				//属性攻类型
    public static final byte PHIZTITLE = 117;				//表情称号
    public static final byte LEADERSHIP = 118;				//统御值
    public static final byte CONTRIBUTION = 119;			//公会贡献值
    public static final byte TONGCREIDT = 120;				//公会荣誉值
    public static final byte FARMMONEY = 121;				//吸血鬼金元
    public static final byte ADDVITALITY = 122;             //宝辉增加体力
    public static final byte ADDATTR_STRENGTH = 123;		//永久增加力量
    public static final byte ADDATTR_AGILITY = 124;			//永久增加敏捷
    public static final byte ADDATTR_VITALITY = 125;		//永久增加体力
    public static final byte ADDATTR_INTELLIGENCE = 126;	//永久增加智力
    public static final byte DOWNLOAD_POINT = 127;			//安卓版下载积分
    
    
    private Map pros = new TreeMap();
    private List basicItems = new ArrayList();
    private List taskItems = new ArrayList();
    private List extendedItems = new ArrayList();
    private List equipments = new ArrayList();
    private List removedEquipments = new ArrayList();
    private List bufs = new ArrayList();
    private List removedBufs = new ArrayList();
    private List pets = new ArrayList();
    private List removedPets = new ArrayList();
    public List getRemovedPets() {
		return removedPets;
	}
	private List petPros = new ArrayList();
    private List durabilities = new ArrayList();
    private List binded = new ArrayList();
    
    private List petAddAbility = new ArrayList();
    private List updatEquipmentProperty = new ArrayList();

    public Changed(){

    }


    public void addBuf(Buf buf){
        bufs.add(buf);
    }

    public void addRemovedBuff(Buf buf){
        removedBufs.add(buf);
    }

    public void addProperty(byte pro,int value){
        if(value==0)
            return;
        Integer old = (Integer)pros.get(new Byte(pro));
        if(old!=null){
            int newValue = old.intValue()+value;
            pros.put(new Byte(pro),new Integer(newValue));
        }else{
            pros.put(new Byte(pro), new Integer(value));
        }
    }
    
    public void addPetAbility (Pet pet, byte pro, Ability[] ab) {
    	PetAbility p = new PetAbility (pet, pro, ab);
    	petAddAbility.add(p);
    }

    public void addPetProperty(Pet pet,byte pro,Object value){
		PetProperty p = new PetProperty(pet,pro,value);
		petPros.add(p);
    }
    
    public void updatEquipmentProperty (IEquipment equ, byte recalculate, int level) {
    	EquipmentProperty ep = new EquipmentProperty(equ, recalculate, level);
    	updatEquipmentProperty.add(ep);
    }

    public void addPetProperty(Pet pet,byte pro,int value){
        if(value==0&&pro!=PET_UPLEVELEXP)
            return;
        addPetProperty(pet,pro,new Integer(value));
    }

    public void setProperty(byte pro,int value){
        pros.put(new Byte(pro),new Integer(value));
    }

    public void setProperty(byte pro,String value){
        pros.put(new Byte(pro),value);
    }

//    public int[][] getProperties(){
//        int[][] ret = new int[pros.size()][2];
//        Set entrys = pros.entrySet();
//        Iterator ite = entrys.iterator();
//        int i = 0;
//        while(ite.hasNext()){
//            Map.Entry entry = (Map.Entry)ite.next();
//            ret[i][0] = ((Byte)entry.getKey()).byteValue();
//            ret[i][1] = ((Integer)entry.getValue()).intValue();
//            i++;
//        }
//        return ret;
//    }

    public int getProperty(byte pro){
        Integer value = (Integer)pros.get(new Byte(pro));
        if(value!=null)
            return value.intValue();
        else
            return 0;
    }
//mengjie add PET_LEVEL
    public int getPetproperty(byte pro){
        for(int i = 0; i < petPros.size(); i++){
            if (((PetProperty)petPros.get(i)).getPro() == pro){
                return 1;
            }
        }
        return 0;
    }
    
    public Pet getPeton(byte pro){//是否装备宠物
        for(int i = 0; i < pets.size(); i++){
        	Grid grid = (Grid)pets.get(i);
            if (grid.item.getType() == pro){
                return (Pet)grid.item;
            }
        }
        return null;
    }
    
    public void addItem(int id){
        addItem(id,1);
    }

//    public void addEquipment(int id){
//        Equipment equ = Items.getEquipment(id);
//        Grid grid = new Grid();
//        grid.item = equ;
//        grid.count = 1;
//        equipments.add(grid);
//    }

    public void addEquipment(IEquipment equ){
        addEquipment(equ,1);

    }

    public void addEquipment(IEquipment equ,int count){
        if (count == 1) {
            Grid grid = new Grid();
            grid.item = equ;
            grid.count = 1;
            equipments.add(grid);
        }else{
            Grid grid = new Grid();
            grid.item = equ;
            grid.count = -1;
            removedEquipments.add(grid);
        }
    }

    public void addItem(int id, int count) {
        IItemTemplate template = Items.getTemplate(id);
        IItem item = template.newInstance();
        addItem(item,count);
    }

    public void addItem(IItem item,int count){
        byte type = item.getType();
        if(type==IItem.TYPE_BASIC){
            for(int i=0;i<basicItems.size();i++){
                Grid grid = (Grid)basicItems.get(i);
                if(grid.item.getItemId()==item.getItemId()){
                    grid.count += count;
                    return;
                }
            }
            Grid grid = new Grid();
            grid.item = item;
            grid.count = (short)count;
            basicItems.add(grid);
        }
        else if(type==IItem.TYPE_EXTENDED){
            for(int i=0;i<extendedItems.size();i++){
                Grid grid = (Grid)extendedItems.get(i);
                if(grid.item.getItemId()==item.getItemId()){
                    grid.count += count;
                    return;
                }
            }
            Grid grid = new Grid();
            grid.item = item;
            grid.count = (short)count;
            extendedItems.add(grid);
        }
        else if(type==IItem.TYPE_TASK){
            for(int i=0;i<taskItems.size();i++){
                Grid grid = (Grid)taskItems.get(i);
                if(grid.item.getItemId()==item.getItemId()){
                    grid.count += count;
                    return;
                }
            }
            Grid grid = new Grid();
            grid.item = item;
            grid.count = (short)count;
            taskItems.add(grid);
        }
        else if(type==IItem.TYPE_EQU){
            Grid grid = new Grid();
            grid.item = item;
            grid.count = (short)count;
            if(grid.count>0)
                equipments.add(grid);
            else
                removedEquipments.add(grid);
        }
        else if(type==IItem.TYPE_PET){
            Grid grid = new Grid();
            grid.item = item;
            grid.count = (short)count;
            if(grid.count>0)
                pets.add(grid);
            else
                removedPets.add(grid);
        }
    }



    public Grid[] getBasicItems(){
        Grid[] ret = new Grid[basicItems.size()];
        basicItems.toArray(ret);
        return ret;
    }

    public Grid[] getTaskItems(){
        Grid[] ret = new Grid[taskItems.size()];
        taskItems.toArray(ret);
        return ret;
    }

    public Grid[] getExtendedItems() {
        Grid[] ret = new Grid[extendedItems.size()];
        extendedItems.toArray(ret);
        return ret;
    }

    public Grid[] getEquipments(){
        Grid[] ret = new Grid[equipments.size()];
        equipments.toArray(ret);
        return ret;
    }

    public Grid[] getRemovedEquipments(){
        Grid[] ret = new Grid[removedEquipments.size()];
        removedEquipments.toArray(ret);
        return ret;
    }

    public void addAward(TaskAward award,byte subId){
        SubTaskAward common = award.getCommonAward();
        addSubAward(common);
        SubTaskAward sub = award.getAward(subId);
        addSubAward(sub);
    }

    private void addSubAward(SubTaskAward award){
        if(award==null)
            return;
        int money = award.getMoney();
        if(money>0){
            addProperty(MONEY,money);
        }
        int exp = award.getExp();
        if(exp>0){
            addProperty(EXP,exp);
        }
        int credit = award.getCredit();
        if(credit>0)
            addProperty(CREDIT,credit);
        TemplateGrid[] items = award.getAddItems();
        for(int i=0;i<items.length;i++){
            addItem(items[i].template.newInstance(),items[i].count);
        }
    }

    public void addDurability(IEquipment equ,int value){
        Durability dur = new Durability(equ,value);
        durabilities.add(dur);
    }

    public void addBinded(IEquipment equ){
        binded.add(equ);
    }

    /**
     * @return 这个是写日志用的，，都用的是最新版本，不用老的
     */
    public byte[] toBytes(){
    	//这里默认用最新的协议去改
        Object[] os = toClientBytes(1);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            for (int i = 0; i < os.length; i++) {
                bos.write((byte[]) os[i]);
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }
    
    //日志里的输出字节流已经完全用toBytes,这里不用区分了
   /* *//**
     * @return 写下日志的字节
     *//*
    public byte[] toLogBytes(){
        Object[] os = toLog();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            for (int i = 0; i < os.length; i++) {
                bos.write((byte[]) os[i]);
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    public Object[] toLog(){
        try {
            List l = new ArrayList();
            if (bufs.size() > 0 || removedBufs.size() > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(6);
                dos.writeByte(bufs.size() + removedBufs.size());
                for (int i = 0; i < removedBufs.size(); i++) {
                    dos.write(((Buf) removedBufs.get(i)).toRemovedBytes());
                }
                for (int i = 0; i < bufs.size(); i++) {
                    dos.write(((Buf) bufs.get(i)).toClientBytes());
                }
                l.add(bos.toByteArray());
            }
//            int[][] pros = getProperties();
            if (pros.size() > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(1);
                dos.writeByte(pros.size());
                Iterator ite = pros.entrySet().iterator();
                while (ite.hasNext()) {
                    Map.Entry entry = (Map.Entry) ite.next();
                    int pro = ((Byte) entry.getKey()).byteValue();
                    dos.writeByte(pro);
                    if(pro==Changed.CREDIT_STRING||pro==Changed.TITLE_STRING||pro==Changed.PASSWORD||pro==Changed.PLAYERNAME){
                        dos.writeUTF((String)entry.getValue());
                    }
                    else{
                        dos.writeInt(((Integer)entry.getValue()).intValue());
                    }
                }
                l.add(bos.toByteArray());
            }
            if(petPros.size()>0){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(1);
                dos.writeByte(getPetProSize());
                for(int i=0;i<petPros.size();i++){
                    PetProperty p = (PetProperty)petPros.get(i);
                    if(p.pro==Changed.PET_EXP){
                        int value = ((Integer)p.value).intValue();
                        for(int j=0;j<value;j++){
                            dos.write(p.pro);
                            dos.writeInt(p.pet.getId());
                            dos.writeInt(1);
                        }
                    }else{
                        dos.write(p.pro);
                        dos.writeInt(p.pet.getId());
                        if (p.pro == Changed.PET_NAME) {
                            dos.writeUTF((String) p.value);
                        } else {
                            dos.writeInt(((Integer) p.value).intValue());
                        }
                    }
                }
                l.add(bos.toByteArray());
            }
            Grid[] basicItems = getBasicItems();
            if (basicItems.length > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(2);
                dos.writeByte(basicItems.length);
                for (int i = 0; i < basicItems.length; i++) {
                    dos.write(basicItems[i].item.toClientBytes());
                    dos.writeByte(basicItems[i].count);
                }
                l.add(bos.toByteArray());
            }
            Grid[] taskItems = getTaskItems();
            if (taskItems.length > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(3);
                dos.writeByte(taskItems.length);
                for (int i = 0; i < taskItems.length; i++) {
                    dos.write(taskItems[i].item.toClientBytes());
                    dos.writeByte(taskItems[i].count);
                }
                l.add(bos.toByteArray());
            }
            Grid[] extendedItems = getExtendedItems();
            if (extendedItems.length > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(4);
                dos.writeByte(extendedItems.length);
                for (int i = 0; i < extendedItems.length; i++) {
                    dos.write(extendedItems[i].item.toClientBytes());
                    dos.writeByte(extendedItems[i].count);
                }
                l.add(bos.toByteArray());
            }
            Grid[] equipments = getEquipments();
            if (equipments.length > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(5);
                dos.writeByte(equipments.length);
                for (int i = 0; i < equipments.length; i++) {
                    dos.write(equipments[i].item.toClientBytes());
                }
                l.add(bos.toByteArray());
            }
            Grid[] removedEquipments = getRemovedEquipments();
            if(removedEquipments.length>0){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(7);
                dos.writeByte(removedEquipments.length);
                for (int i = 0; i < removedEquipments.length; i++) {
                    dos.writeInt(removedEquipments[i].item.getItemId());
                    dos.writeInt(removedEquipments[i].item.getId());
                }
                l.add(bos.toByteArray());
            }

            if(pets.size()>0){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(8);
                dos.writeByte(pets.size());
                for(int i=0;i<pets.size();i++){
                    Grid grid = (Grid)pets.get(i);
                    dos.write(grid.item.toClientBytes());
                }
                l.add(bos.toByteArray());
            }
            if(removedPets.size()>0){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(9);
                dos.writeByte(removedPets.size());
                for(int i=0;i<removedPets.size();i++){
                    Grid grid = (Grid)removedPets.get(i);
                    Pet p = (Pet)grid.item;
                    dos.write(grid.item.toClientBytes());
                }
                l.add(bos.toByteArray());
            }
            if(durabilities.size()>0){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(10);
                dos.writeByte(durabilities.size());
                for(int i=0;i<durabilities.size();i++){
                    Durability dur = (Durability)durabilities.get(i);
                    dos.writeInt(dur.equ.getItemId());
                    dos.writeInt(dur.equ.getId());
                    dos.writeShort(dur.value);
                }
                l.add(bos.toByteArray());
            }
            if(binded.size()>0){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(11);
                dos.writeByte(binded.size());
                for(int i=0;i<binded.size();i++){
                    IEquipment equ = (IEquipment)binded.get(i);
                    dos.writeInt(equ.getItemId());
                    dos.writeInt(equ.getId());
                }
                l.add(bos.toByteArray());
            }
            return l.toArray();
        } catch (IOException ex) {
            return new Object[0];
        }
    }*/

    //leo add
    public Object[] getAutoUseData(){
        try{
            List l = new ArrayList();
            
            List commands = new ArrayList();
            Grid[] extendedItems = getExtendedItems();
            
            if (extendedItems.length > 0) {
            	int commandcount = 0;
            	String commandstr = "";
                for (int i = 0; i < extendedItems.length; i++) {
                    ExtendedItem item = (ExtendedItem)extendedItems[i].item;
                    
                    if(extendedItems[i].count >0 && item.getAutoUse()){
                        AutoUseData atUse = new AutoUseData();
                        
                        atUse.setId(item.getItemId());
                        
                        if(item.getAutoUseMessage().startsWith("AUTO")){
                        	for(int c = 0; c < extendedItems[i].count; c++){
                        		commandcount ++ ;
                            	commands.add("autouseitem " + atUse.getId());
                        	}
//                            atUse.setTaskId(31029);
//                            atUse.setMessage("");
//                            atUse.setCommand("autouseitem " + atUse.getId());
                        }else{
                            atUse.setTaskId(31002);
                            
                            String tmp = item.getAutoUseMessage();
                            
                            if(tmp.trim().length() == 0){
                                tmp = "你得到了item，要使用吗？\n1.马上使用\n2.回头再说";
                            }
                            
                            tmp = tmp.replaceAll("item", "\"" + item.getName() + "\"");
                            tmp = tmp.replaceAll("\\\\n", "\n");
                            
                            atUse.setMessage(tmp);
                            atUse.setCommand("autouseitem " + atUse.getId());
                        }
                        if(!item.getAutoUseMessage().startsWith("AUTO")){
                        	l.add(atUse);
                        }
                        
                    }
                }
                if(commandcount>0){
                	AutoUseData atUse = new AutoUseData();
                	atUse.setTaskId(31048);
                    atUse.setMessage("31048");
                    String[] commandsresult = new String[commands.size()];
                    commands.toArray(commandsresult);
                    atUse.setCommands(commandsresult);
                    l.add(atUse);
                }
                
            }
            
            return l.toArray();
        }catch (Exception ex) {
            return new Object[0];
        }
    }
    //leo add end
    
    public Object[] toClientBytes(int dataVersion){
    	//
        try {
            List l = new ArrayList();
            if (bufs.size() > 0 || removedBufs.size() > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(6);
                dos.writeByte(bufs.size() + removedBufs.size());
                for (int i = 0; i < removedBufs.size(); i++) {
                    dos.write(((Buf) removedBufs.get(i)).toRemovedBytes());
                }
                for (int i = 0; i < bufs.size(); i++) {
                    dos.write(((Buf) bufs.get(i)).toClientBytes());
                }
                l.add(bos.toByteArray());
            }
//            int[][] pros = getProperties();
            if (pros.size() > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(1);
                dos.writeByte(pros.size());
                Iterator ite = pros.entrySet().iterator();
                while (ite.hasNext()) {
                    Map.Entry entry = (Map.Entry) ite.next();
                    int pro = ((Byte) entry.getKey()).byteValue();
                    dos.writeByte(pro);
                    if(pro==Changed.CREDIT_STRING||pro==Changed.TITLE_STRING||pro==Changed.PASSWORD||pro==Changed.PLAYERNAME){
                        dos.writeUTF((String)entry.getValue());
                    }
                    else{
                        dos.writeInt(((Integer)entry.getValue()).intValue());
                    }
                }
                l.add(bos.toByteArray());
            }
            if (petPros.size() > 0 || petAddAbility.size() > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(1);
                dos.writeByte(getPetProSize() + getPetAddAbilitySize());
                for (int i = 0; i < petPros.size(); i++) {
                    PetProperty p = (PetProperty)petPros.get(i);
                    if(p.pro==Changed.PET_EXP){
                        int value = ((Integer)p.value).intValue();
                        /*changed by leo
                        for(int j=0;j<value;j++){
                            dos.write(p.pro);
                            dos.writeInt(p.pet.getId());
                            dos.writeInt(1);
                        }*/
                        dos.write(p.pro);
                        dos.writeInt(p.pet.getId());
                        dos.writeInt(value);
                    } else {
                        dos.write(p.pro);
                        dos.writeInt(p.pet.getId());
                        if (p.pro == Changed.PET_NAME) {
                            dos.writeUTF((String) p.value);
                        } else {
                            dos.writeInt(((Integer) p.value).intValue());
                        }
                    }
                }
            	for (int i = 0; i < petAddAbility.size(); i ++) {
            		PetAbility p = (PetAbility)petAddAbility.get(i);
            		dos.write(p.pro);
            		dos.writeInt(p.pet.getId());
            		dos.writeInt(p.ab.length);
            		for (int j = 0; j < p.ab.length; j ++) {
            			dos.writeShort(p.ab[j].getId());
            			dos.writeUTF(p.ab[j].getName());
            		}
            	}
                l.add(bos.toByteArray());
            }
            Grid[] basicItems = getBasicItems();
            if (basicItems.length > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(2);
                dos.writeByte(basicItems.length);
                for (int i = 0; i < basicItems.length; i++) {
                    dos.write(basicItems[i].item.toClientBytes(dataVersion));
                    dos.writeByte(basicItems[i].count);
                }
                l.add(bos.toByteArray());
            }
            Grid[] taskItems = getTaskItems();
            if (taskItems.length > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(3);
                dos.writeByte(taskItems.length);
                for (int i = 0; i < taskItems.length; i++) {
                    dos.write(taskItems[i].item.toClientBytes(dataVersion));
                    dos.writeByte(taskItems[i].count);
                }
                l.add(bos.toByteArray());
            }
            Grid[] extendedItems = getExtendedItems();
            if (extendedItems.length > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(4);
                dos.writeByte(extendedItems.length);
                for (int i = 0; i < extendedItems.length; i++) {
                    dos.write(extendedItems[i].item.toClientBytes(dataVersion));
                    dos.writeByte(extendedItems[i].count);
                }
                l.add(bos.toByteArray());
            }
            Grid[] equipments = getEquipments();
            if (equipments.length > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(5);
                dos.writeByte(equipments.length);
                for (int i = 0; i < equipments.length; i++) {
                    dos.write(equipments[i].item.toClientBytesWithLevel(-1));
                }
                l.add(bos.toByteArray());
            }
            Grid[] removedEquipments = getRemovedEquipments();
            if(removedEquipments.length>0){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(7);
                dos.writeByte(removedEquipments.length);
                for (int i = 0; i < removedEquipments.length; i++) {
                    dos.writeInt(removedEquipments[i].item.getItemId());
                    dos.writeInt(removedEquipments[i].item.getId());
                }
                l.add(bos.toByteArray());
            }

            if(pets.size()>0){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(8);
                dos.writeByte(pets.size());
                for(int i=0;i<pets.size();i++){
                    Grid grid = (Grid)pets.get(i);
                    dos.write(grid.item.toClientBytesWithLevel(-1));
                }
                l.add(bos.toByteArray());
            }
            if(removedPets.size()>0){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(9);
                dos.writeByte(removedPets.size());
                for(int i=0;i<removedPets.size();i++){
                    Grid grid = (Grid)removedPets.get(i);
                    Pet p = (Pet)grid.item;
                    dos.writeInt(p.getId());
                }
                l.add(bos.toByteArray());
            }
            if(durabilities.size()>0){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(10);
                dos.writeByte(durabilities.size());
                for(int i=0;i<durabilities.size();i++){
                    Durability dur = (Durability)durabilities.get(i);
                    dos.writeInt(dur.equ.getItemId());
                    dos.writeInt(dur.equ.getId());
                    dos.writeShort(dur.value);
                }
                l.add(bos.toByteArray());
            }
            if(binded.size()>0){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(11);
                dos.writeByte(binded.size());
                for(int i=0;i<binded.size();i++){
                    IEquipment equ = (IEquipment)binded.get(i);
                    dos.writeInt(equ.getItemId());
                    dos.writeInt(equ.getId());
                }
                l.add(bos.toByteArray());
            }
            if (updatEquipmentProperty.size() > 0) {
            	ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(12);
                dos.writeByte(updatEquipmentProperty.size());
                for (int i = 0; i < updatEquipmentProperty.size(); i++) {
                	EquipmentProperty ep = (EquipmentProperty)updatEquipmentProperty.get(i);
                    dos.writeInt(ep.equ.getItemId());
                    dos.writeInt(ep.equ.getId());
                    dos.writeByte(ep.recalculate);
                    dos.write(ep.equ.toClientBytesWithLevel(ep.level));
                }
                l.add(bos.toByteArray());
            }
            return l.toArray();
        } catch (IOException ex) {
            return new Object[0];
        }
    }

    private int getPetProSize(){
        int ret = petPros.size();
        /*delete by leo
        for(int i=0;i<petPros.size();i++){
            PetProperty p = (PetProperty)petPros.get(i);
            if(p.pro==Changed.PET_EXP){
                int value = ((Integer)p.value).intValue();
                ret += (value-1);
            }
        }*/
        return ret;
    }
    
    private int getPetAddAbilitySize () {
    	int ret = petAddAbility.size();
    	return ret;
    }
    
    public int getPetAddAbility (byte pro) {
        for (int i = 0; i < petAddAbility.size(); i ++) {
            if (((PetAbility)petAddAbility.get(i)).getPro() == pro) {
                return 1;
            }
        }
        return 0;
    }
    
    public Grid[] getChangedItems(){
        List l = new ArrayList(basicItems.size()+taskItems.size()+extendedItems.size()+equipments.size());
        l.addAll(basicItems);
        l.addAll(taskItems);
        l.addAll(extendedItems);
        l.addAll(equipments);
        Grid[] ret = new Grid[l.size()];
        l.toArray(ret);
        return ret;
    }
}
class PetProperty{
    Pet pet;
    byte pro;
    Object value;
    public PetProperty(Pet pet,byte pro,Object value){
        this.pet = pet;
        this.pro = pro;
        this.value = value;
    }
    public byte getPro(){
        return pro;
    }    
}

class PetAbility {
	Pet pet;
    byte pro;
    Ability[] ab;
    public PetAbility(Pet pet, byte pro, Ability[] ab){
        this.pet = pet;
        this.pro = pro;
        this.ab = ab;
    }
    public byte getPro(){
        return pro;
    }
}

class Durability{
    IEquipment equ;
    int value;
    public Durability(IEquipment equ,int value){
        this.equ = equ;
        this.value = value;
    }
}

class EquipmentProperty {
    IEquipment equ;
    byte recalculate;
    int level;
    public EquipmentProperty (IEquipment equ, byte recalculate, int level) {
        this.equ = equ;
        this.recalculate = recalculate;
        this.level = level;
    }
}