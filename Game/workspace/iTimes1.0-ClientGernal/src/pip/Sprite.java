package pip;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;


public class Sprite extends BattleSprite{
    public int playerID;
    protected byte state;
    protected byte subState;
    protected byte state_back;
    public long time;
    public ImageSet imageSet;
    public ImageSet weaponImageSet;
    protected short x, y;
    protected short lastX, lastY;
    protected short lastPositionX, lastPositionY, lastMapId;
    protected boolean visible = true;
    public byte direct;
    public String chatCircleName = "";

    //-------------Player Data Begin-------------//
    public byte sex; //性别
    public short face; //形象
    public byte reborn; //转生次数
    public short level; //等级
    public int exp; //经验值
    public int upLevelExp; //升级所需经验
    public int money; //金钱数
    public int credit; //声望
    public short bagSize;

    public int nameColor = CLR_NAME; //名字颜色
    public byte protectMode = 0; //不保护状态

    /**
     * 当前宠物，可以为null
     */
    public PetSprite petCurrent;

    /**
     * 宠物背包
     */
    public Hashtable petBag;

    /**
     * 宠物背包大小
     */
    public short petBagSize;

    public byte learnPoint; //剩余技能点数
    public byte[] ability; //已学技能分配情况
    public byte restAbility; //剩余战斗技能配点
    public short[] productSkill = new short[8]; //生产技能点数
    public int productVitality = 100; //生产体力值

    public String tongName = ""; //公会名称
    public String titleName = ""; //称号
    public String creditName = ""; //荣誉名称

    public byte tongDuty; //公会职位 100 会长  99 副会长  2 精英 1 会员

    public static final byte TONG_DUTY_NONE = -1;
    public static final byte TONG_DUTY_CROWD = 1;
    public static final byte TONG_DUTY_EXPERT = 2;
    public static final byte TONG_DUYT_NOSAY = 13;
    public static final byte TONG_DUTY_VICE_CHAIRMAN = 99;
    public static final byte TONG_DUTY_CHAIRMAN = 100;

    public static final String[] TONG_DUTY_NAME = {
                    "没有公会", "会员", "精英", "副会长", "会长", "禁闭中"
    };

    public static final byte SKILL_BLACKSMITHING = 0;
    public static final byte SKILL_ALCHEMY = 1;
    public static final byte SKILL_TAILOR = 2;
    public static final byte SKILL_HERBALISM = 3;
    public static final byte SKILL_HUNTERING = 4;
    public static final byte SKILL_MINING = 5;
    public static final byte SKILL_COOKING = 6;
    public static final byte SKILL_FISHING = 7;

    public static final String[] productSkillName = {
                    "锻造", "炼金", "裁缝", "采摘", "狩猎", "采矿", "烹饪", "钓鱼"
    };

    public static final short[] SKILL_LEVEL_POINT = {
                    40, 75, 110, 140, 170, 195, 220, 240, 260, 275, 285
    };

    public short[] baseAttribute = new short[4];
    public int[] attributeBackup;

    public int runAwayTime = -1; // 逃跑后5秒内怪不追

    /**
     * 技能配置表 <br>[0] skillEffect<br> [1] maxLevel<br> [2] currentLevel<br> [3] keys<br>[4] id;
     */
    public short[][] skillTable;

    public Vector basicItems; //基本物品
    //    public Vector extendItems; //扩展物品
    public Vector taskItems; //任务物品
    public Vector equipsInBag; // 包内装备
    //    public Vector equips; //已装装备

    public GameItem[] playerEquips = new GameItem[GameItem.EQUIP_TYPE_TOTAL];

    public GameItem[] playerEquipsBackup;

    //-------------Player Data End-------------//

    public static final byte UP = 0;
    public static final byte DOWN = 1;
    public static final byte LEFT = 2;
    public static final byte RIGHT = 3;

    public static final int CLR_NAME = 0xffff00;

    public static final int INDEXES[] = {
                    0, 1, 0, 2
    };

    public static final byte[][] FRAMESEQUENCE_WALK = new byte[][]{
                    //up
                    new byte[]{
                                    0, 0, 1, 1, 0, 0, 2, 2
                    },
                    //down
                    new byte[]{
                                    3, 3, 4, 4, 3, 3, 5, 5
                    },
                    //left
                    new byte[]{
                                    6, 6, 7, 7, 6, 6, 8, 8
                    },
                    //right
                    new byte[]{
                                    9, 9, 10, 10, 9, 9, 11, 11
                    }
    };
    public static final byte[][] FRAMESEQUENCE_STAND = new byte[][]{
                    //up
                    new byte[]{
                        0
                    },
                    //down
                    new byte[]{
                        3
                    },
                    //left
                    new byte[]{
                        6
                    },
                    //right
                    new byte[]{
                        9
                    }
    };

    public static final byte[][] FRAMESEQUENCE_BATTLE = new byte[][]{
                    //0 = stand
                    new byte[]{
                                    3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4
                    },
                    //1 = run
                    new byte[]{
                        3
                    },
                    //2 = attack
                    new byte[]{
                        5
                    },
                    //3 = take a beating
                    new byte[]{
                        3
                    },
                    //4 = runback
                    new byte[]{
                        1
                    },
                    //5 = die
                    new byte[]{
                                    0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 1, 1
                    }
    };

    public static final byte[][] FRAMESEQUENCE_WEAPON = new byte[][]{
                    //空手
                    {
                                    0, 0, 0, 0, 0, 0
                    },
                    //剑
                    {
                                    1, 1, 2, 2, 2, 2
                    },
                    //斧
                    {
                                    3, 3, 4, 4, 4, 4
                    },
                    //枪
                    {
                                    5, 5, 6, 6, 6, 6
                    },
                    //仗
                    {
                                    7, 7, 8, 8, 8, 8
                    }
    };

    public static int getSkillLevel(int skillPoint){
        if(skillPoint == -1){
            return 0;
        }
        for(int i = 0; i < SKILL_LEVEL_POINT.length; i++){
            if(SKILL_LEVEL_POINT[i] >= skillPoint)
                return i + 1;
        }
        return 0;
    }

    public static final byte STATE_IDLE = 0;
    public static final byte STATE_MOVING = 1;
    public static final byte STATE_AUTO_MOVING = 2;
    public static final byte STATE_DIE = 3;
    public static final byte STATE_BATTLE = 4;

    public static final byte STATE_WAYPOINT = 5;

    //#if (polish.identifier == Nokia/Series40Midp2) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (Directory == SE-S700)
    //# public static final int STEP = 5;
    //#elif JBlend == true
    //# public static final int STEP = 5;
    //#else
    //# public static final int STEP = 5;

    //#endif
    //#debug
    public static final int STEP = 5;

    //==========Way Point=============/
    public byte status = MonsterSprite.STATUS_WAYPOINT;

    public Vector wpList;
    public int wpPointer;
    public byte wpDir;
    public byte wpType;

    public boolean leaveParty;

    public void cycleWayPoint(){
        if(wpList != null){
            if(frameSequence != Sprite.FRAMESEQUENCE_WALK[direct])
                frameSequence = Sprite.FRAMESEQUENCE_WALK[direct];
            goWithWayPoint();
        }else{
            if(leaveParty){
                setState(STATE_IDLE, true);
                leaveParty = false;
            }
        }
    }

    private byte goWithWayPoint(){
        byte status = MonsterSprite.WPSTATUS_WALK;

        int npcX = x + getWidth() / 2;
        int npcY = y;
        int dest = ((Integer)(wpList.elementAt(wpPointer))).intValue();

        int destx = (dest >> 16) + getWidth() / 2;
        int desty = dest & 0x0000ffff;

        int diffx = destx - npcX;
        int diffy = desty - npcY;

        if(diffx == 0 && diffy == 0){
            wpPointer += wpDir;

            if(wpPointer == wpList.size()){
                //到达最后一个路点
                wpList = null;
                frameSequence = Sprite.FRAMESEQUENCE_STAND[direct];
                //frame = 0;
                if(leaveParty){
                    setState(STATE_IDLE, true);
                    leaveParty = false;
                }
            }
        }else{
            wpMoveTo(destx, desty);
        }

        return status;
    }

    public void addWayPoint(short x, short y){
        if(wpList == null){
            wpList = new Vector();
            wpList.addElement(new Integer((this.x << 16 | this.y)));
            wpPointer = 1;
            wpDir = 1;
        }
        wpList.addElement(new Integer((x << 16 | y)));
    }

    public boolean wpMoveTo(int destx, int desty){
        int dx, dy;
        int npcX = x + getWidth() / 2;
        int npcY = y;

        dx = STEP;
        dy = dx;

        int diffx = Math.abs(destx - npcX);
        int diffy = Math.abs(desty - npcY);

        if(diffx >= diffy && diffx != 0){
            int v = dx * 10000 / Math.abs(diffx);
            dy = diffy * v / 10000;
        }

        if(diffx < diffy && diffy != 0){
            int v = dy * 10000 / Math.abs(diffy);
            dx = diffx * v / 10000;
        }

        if(Math.abs(diffx) < dx){
            dx = Math.abs(diffx);
        }

        if(Math.abs(diffy) < dy){
            dy = Math.abs(diffy);
        }

        if(destx - npcX < 0){
            dx = -dx;
        }

        if(desty - npcY < 0){
            dy = -dy;
        }

        go(dx, dy);

        int x = destx - npcX;
        int y = desty - npcY;
        byte dir = this.direct;
        if(Math.abs(x) > Math.abs(y)){
            if(x > 0)
                dir = Sprite.RIGHT;
            else if(x < 0)
                dir = Sprite.LEFT;
        }else{
            if(y > 0)
                dir = Sprite.DOWN;
            else if(y < 0)
                dir = Sprite.UP;
        }
        if(dir != this.direct){
            this.direct = dir;
            frame = 0;
        }

        return dx == 0 && dy == 0;

    }

    public void go(int dx, int dy){
        this.x += dx;
        this.y += dy;
    }

    //========Way Point End ==============/

    //======== buf system ===============/

    public static final byte BUF_LENGTH_MAX = 3;
    public static final byte[] BUF_ICON = {
                    Buf.BUFTYPE_STR, Buf.BUFTYPE_VIT, Buf.BUFTYPE_INT, Buf.BUFTYPE_AGI, Buf.BUFTYPE_HP, Buf.BUFTYPE_HIT, Buf.BUFTYPE_PDEFENSE, Buf.BUFTYPE_PATTACK, Buf.BUFTYPE_PARRY,
                    Buf.BUFTYPE_PCRI, Buf.BUFTYPE_MP, Buf.BUFTYPE_HIT, Buf.BUFTYPE_MDEFENSE, Buf.BUFTYPE_MATTACK, Buf.BUFTYPE_MCRI
    };

    public static ImageSet bufIcon;

    class Buf{

        //        public static final byte BUFTYPE_SEX = 1; //sex
        public static final byte BUFTYPE_STR = 8; //str
        public static final byte BUFTYPE_AGI = 9; //agi
        public static final byte BUFTYPE_VIT = 10; //vit
        public static final byte BUFTYPE_INT = 11; //int
        public static final byte BUFTYPE_HP = 13; //hp
        public static final byte BUFTYPE_MP = 14; //mp
        public static final byte BUFTYPE_PATTACK = 16; //p.Attack
        public static final byte BUFTYPE_PDEFENSE = 17; //p.Defense
        public static final byte BUFTYPE_MATTACK = 18; //m.Attack
        public static final byte BUFTYPE_MDEFENSE = 19; //m.Defense
        public static final byte BUFTYPE_HIT = 20; //hit
        public static final byte BUFTYPE_PARRY = 21; //parry
        public static final byte BUFTYPE_PCRI = 22; //p.Cti
        public static final byte BUFTYPE_MCRI = 23; //m.Cti
        public static final byte BUFTYPE_AMOR = 24; //Amor

        /**
         * buf id
         */
        public int id;

        public byte bufType;

        public int bufValue;

        public int iconid;

        public Buf(int id, byte bufType, int bufValue){
            this.id = id;
            this.bufType = bufType;
            this.bufValue = bufValue;

            for(int i = 0; i < BUF_ICON.length; i++){
                if(BUF_ICON[i] == bufType){
                    iconid = i;
                    break;
                }
            }
        }

        public void drawIcon(Graphics g, int x, int y){
            bufIcon.drawFrame(g, iconid, x, y, Graphics.TOP | Graphics.LEFT);
        }

    }

    public Buf buf[] = new Buf[BUF_LENGTH_MAX];

    public void removeBuf(int bufid){
        for(int i = 0; i < buf.length; i++){
            if(buf[i] != null && buf[i].id == bufid){

                if(buf[i].bufType <= Buf.BUFTYPE_INT){
                    baseAttribute[buf[i].bufType - Buf.BUFTYPE_STR] -= buf[i].bufValue;
                }
                buf[i] = null;
                reGroupBuf();
                reCalculateAttributes();
                break;
            }
        }
    }

    public void addBuf(int bufid, byte bufType, int bufValue){
        Buf buf = new Buf(bufid, bufType, bufValue);
        for(int i = 0; i < this.buf.length; i++){
            if(this.buf[i] == null){
                this.buf[i] = buf;
                addBufEffect(buf);
                reCalculateAttributes();
                break;
            }
        }
        reGroupBuf();
    }

    public void reGroupBuf(){
        if(buf[0] == null && buf[1] != null){
            buf[0] = buf[1];
            buf[1] = null;
        }

        if(buf[1] == null && buf[2] != null){
            buf[1] = buf[2];
            buf[2] = null;
        }

        if(buf[0] == null && buf[2] != null){
            buf[0] = buf[2];
            buf[2] = null;
        }

    }

    public void addBufEffect(Buf buf){
        boolean attrChanged = false;
        switch(buf.bufType){
            case Buf.BUFTYPE_STR:
            case Buf.BUFTYPE_AGI:
            case Buf.BUFTYPE_VIT:
            case Buf.BUFTYPE_INT:
                baseAttribute[buf.bufType - Buf.BUFTYPE_STR] += buf.bufValue;
                attrChanged = true;
                break;
            case Buf.BUFTYPE_HP:
                attributes[BattleSprite.ATTR_HPMAX] += buf.bufValue;
                break;
            case Buf.BUFTYPE_MP:
                //mp
                attributes[BattleSprite.ATTR_MPMAX] = buf.bufValue;
                break;
            case Buf.BUFTYPE_PATTACK:
                //pattack
                attributes[BattleSprite.ATTR_PMIN] += buf.bufValue;
                attributes[BattleSprite.ATTR_PMAX] += buf.bufValue;
                break;
            case Buf.BUFTYPE_PDEFENSE:
                //pdefence
                attributes[BattleSprite.ATTR_PDEF] += buf.bufValue;
                break;
            case Buf.BUFTYPE_MATTACK:
                //mattack
                attributes[BattleSprite.ATTR_MMIN] += buf.bufValue;
                attributes[BattleSprite.ATTR_MMAX] += buf.bufValue;
                break;
            case Buf.BUFTYPE_MDEFENSE:
                //mdefence
                attributes[BattleSprite.ATTR_MDEF] += buf.bufValue;
                break;
            case Buf.BUFTYPE_HIT:
                attributes[BattleSprite.ATTR_PHIT] += buf.bufValue;
                attributes[BattleSprite.ATTR_MMIN] += buf.bufValue;
                break;
            case Buf.BUFTYPE_PARRY:
                //parry
                attributes[BattleSprite.ATTR_FLEE] += buf.bufValue;
                break;
            case Buf.BUFTYPE_PCRI:
                //pcri
                attributes[BattleSprite.ATTR_PCRI] += buf.bufValue;
                break;
            case Buf.BUFTYPE_MCRI:
                //mcri
                attributes[BattleSprite.ATTR_MCRI] += buf.bufValue;
                break;
        }
    }

    //======= buf ststem end ============/

    public Sprite(ImageSet in, short x, short y){
        imageSet = in;
        direct = UP;
        frameSequence = FRAMESEQUENCE_STAND[direct];
        setIndex((byte)0);
        this.x = x;
        this.y = y;
        this.state = STATE_IDLE;

        skillList = new short[0];
        bsType = TYPE_PLAYER;

    }

    public ImageSet getImageSet(){
        if(showDie){
            return World.dieImageSet;
        }else{
            return imageSet;
        }
    }

    public void initPlayerData(String name, byte[] data, boolean relogin){
        this.name = name;
        //#debug
        System.out.println("player data size : " + data.length);

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bis);

        try{
            sex = dis.readByte();
            face = dis.readShort();
            imageSet = World.getPlayerImage(World.getFaceIndex(face, false));
            reborn = dis.readByte();
            level = dis.readShort();
            exp = dis.readInt();
            upLevelExp = dis.readInt();
            money = dis.readInt();
            credit = dis.readInt();
            //#debug
            System.out.println("Player : " + exp + " , " + level + " , " + money);
            //#debug
            System.out.println("荣誉值：" + credit);

            short strength = dis.readShort();
            short agility = dis.readShort();
            short vitality = dis.readShort();
            short intelligence = dis.readShort();
            long luck = dis.readLong();

            int hp = dis.readInt();
            int mp = dis.readInt();

            bagSize = dis.readShort();

            baseAttribute[0] = strength;
            baseAttribute[1] = agility;
            baseAttribute[2] = vitality;
            baseAttribute[3] = intelligence;

            initBattleData(BattleSprite.TYPE_PLAYER, level, vitality, strength, intelligence, agility, luck, hp, mp);

            learnPoint = dis.readByte();
            ability = new byte[4];
            long tmp = dis.readLong();
            //#debug
            System.out.println("Ability Long: " + tmp);

            restAbility = (byte)((tmp >> 32) & 0xFF);
            ability[3] = (byte)((tmp >> 24) & 0xFF);
            ability[2] = (byte)((tmp >> 16) & 0xFF);
            ability[1] = (byte)((tmp >> 8) & 0xFF);
            ability[0] = (byte)(tmp & 0xFF);
            //#debug
            System.out.println("Ability : " + restAbility + " , " + ability[0] + " , " + ability[1] + " , " + ability[2] + " , " + ability[3]);

            short skillNumber = dis.readShort();
            skillList = new short[skillNumber];
            //#debug
            System.out.println();
            //#debug
            System.out.println("战斗技能数量：" + skillNumber);

            for(int i = 0; i < skillNumber; i++){
                skillList[i] = dis.readShort();
                //#debug
                System.out.println("    战斗技能id：" + skillList[i]);
            }

            for(int i = 0; i < productSkill.length; i++){
                productSkill[i] = dis.readShort();
                //#debug
                System.out.println(Sprite.productSkillName[i] + " ：" + productSkill[i]);
            }

            short usedEquipNumber = dis.readShort();
            //#debug
            System.out.println();
            //#debug
            System.out.println("已装备装备数量：" + usedEquipNumber);

            for(int i = 0; i < usedEquipNumber; i++){
                GameItem tmpEquip = readItemsData(dis, GameItem.TYPE_EQUIP);

                playerEquips[tmpEquip.equipType] = tmpEquip;

                //#debug
                System.out.println("已装备物品 ：");
                //#debug
                System.out.println(tmpEquip.name + ": 物品id " + tmpEquip.itemId + " 生成id " + tmpEquip.id + " 需要级别 " + tmpEquip.requiredLevel + " 装备级别 " + tmpEquip.equipLevel
                //#debug
                                + " 装备位置 " + GameItem.EQUIP_TYPE_NAME[tmpEquip.equipType]);
                //#debug
                System.out.println(" 最大耐久 " + tmpEquip.durability + " 剩余耐久 " + tmpEquip.currentDurability + " 价格 " + tmpEquip.price + " 绑定 " + (tmpEquip.bind? "是": "否") + " 打造次数　"
                //#debug
                                + tmpEquip.buildTimes);
                //#debug
                System.out.println("增加体力 " + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_VIT) + " 增加智力" + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_INT) + " 增加力量 "
                //#debug
                                + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_STR) + " 增加敏捷 " + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_AGI) + " 攻击力上限 " + tmpEquip.attackMax + " 攻击力下限 "
                                //#debug
                                + tmpEquip.attackMin + " 护甲 " + tmpEquip.defence);
                //#debug
                System.out.println("增加物理攻击 " + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_PATTACK) + " 增加魔法攻击" + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_MATTACK) + " 增加物理防御 "
                //#debug
                                + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_PDEFENCE) + " 增加魔法防御 " + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_MDEFENCE) + " 增加命中 "
                                //#debug
                                + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_HIT) + " 增加闪避 " + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_FLEE) + " 增加物理暴击 "
                                //#debug
                                + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_PCRI) + " 增加魔法暴击 " + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_MCRI));
                //#debug
                if(tmpEquip.equipType == GameItem.EQUIP_TYPE_WEAPON){
                    //#debug
                    System.out.println("武器类型 " + GameItem.WEAPON_TYPE_NAME[tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_WEAPON_TYPE)]);
                    //#debug
                }
            }

            for(int i = 0; i < playerEquips.length; i++){
                if(playerEquips[i] == null){
                    playerEquips[i] = GameItem.createNullEquip((byte)i);
                }
            }

            initEquipData(playerEquips, false, hp, mp); //根据武器情况初始化人物属性

            basicItems = new Vector();
            //            extendItems = new Vector();
            taskItems = new Vector();
            equipsInBag = new Vector();

            //读取基本物品信息
            readBasicItems(dis, false);
            //读取扩展物品信息
            readExtendItems(dis, false);
            //读取任务物品信息
            readTaskItems(dis, false);
            //读取装备物品信息
            readEquItems(dis, false);

            int fcount = dis.readByte();

            for(int i = 0; i < fcount; i++){
                Integer friendId = new Integer(dis.readInt());
                String friendName = dis.readUTF();
                short friendDegree = dis.readShort();

                World.addFriend(friendId, friendDegree,friendName);
            }

            //TODO change for blackList
            int bcount = dis.readByte();

            for(int i = 0; i < bcount; i++){
                Integer blackId = new Integer(dis.readInt());
                String blackName = dis.readUTF();

                World.addBlackList(blackId,blackName);
            }

            for(int i = 0; i < World.systemOption.length; i++){
                World.systemOption[i] = dis.readShort();

                //#debug
                System.out.println("System Option: " + World.systemOption[i]);
            }

            World.parseSystemOption();

            byte[] tdata = new byte[16];
            dis.readFully(tdata);

            for(int i = 0; i < World.net_chat_priority_option.length; i++){
                World.net_chat_priority_option[i] = tdata[i * 2];
                World.net_chat_color_option[i] = tdata[i * 2 + 1];
                //#debug
                System.out.println(tdata[i * 2] + " , " + tdata[i * 2 + 1]);
            }

            chatCircleName = dis.readUTF();
            //#debug
            System.out.println("所在圈：" + chatCircleName);

            //公会
            tongName = dis.readUTF();
            tongDuty = dis.readByte();
            //#debug
            System.out.println("公会名称：" + tongName + ", 职位：" + getTongDutyName(tongDuty));

            petBag = new Hashtable();
            petBagSize = dis.readByte();
            //            byte petCount = dis.readByte();
            //#debug
            System.out.println("宠物包格大小：" + petBagSize);
            //            System.out.println("宠物个数：" + petCount);

            //            for(int i = 0; i < petCount; i++){
            readPet(dis);
            //            }

            int currentPetId = dis.readInt();

            PetSprite tmpPet = (PetSprite)petBag.get(new Integer(currentPetId));

            if(tmpPet != null){
                petCurrent = tmpPet;
                //#debug
                System.out.println("当前装备宠物：" + tmpPet.name + " , " + tmpPet.petId);
                //#debug
            }else{
                //#debug
                System.out.println("未装备任何宠物");
            }

            World.netFriendsNeedShowStatus = false;

            try{
                titleName = dis.readUTF();
                creditName = dis.readUTF();

                //#debug
                System.out.println("称号：" + titleName);
                //#debug
                System.out.println("荣誉：" + creditName);
            }catch(Exception e){
            }

            buf = new Buf[BUF_LENGTH_MAX];
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }finally{
            try{
                if(dis != null){
                    dis.close();
                }
            }catch(Exception e1){
            }
        }
    }

    public static String getTongDutyName(int duty){
        int index = 0;

        switch(duty){
            case TONG_DUTY_NONE:
                index = 0;

                break;
            case TONG_DUTY_CROWD:
                index = 1;

                break;
            case TONG_DUTY_EXPERT:
                index = 2;

                break;
            case TONG_DUTY_VICE_CHAIRMAN:
                index = 3;

                break;
            case TONG_DUTY_CHAIRMAN:
                index = 4;

                break;
            case TONG_DUYT_NOSAY:
                index = 5;

                break;
        }

        return TONG_DUTY_NAME[index];
    }

    public final static String[] ATTRIBUTENAMES = new String[]{
                    "性别", "形象", "转生次数", "等级", "经验", "金币", "属性点", "战斗技能点", "锻造熟练度", "炼金熟练度", "裁缝熟练度", "采摘熟练度", "狩猎熟练度", "采矿熟练度", "烹饪熟练度", "钓鱼熟练度", "包位已满", "宠物经验", "宠物等级", "宠物栏已满", "荣誉点数"
    };

    public Vector updateAttributes(DataInputStream dis) throws IOException{
        int attNum = dis.readByte();
        Vector ret = new Vector();
        boolean attrChanged = false;
        int[] petChangeFlg = new int[petBagSize];
        int petChangeId = 0;
        int vi;

        for(int i = 0; i < attNum; i++){
            byte type = dis.readByte();
            switch(type){
                case 1:
                    // sex
                    vi = dis.readInt();
                    sex = (byte)vi;
                    face = sex;
                    //#debug
                    System.out.println("更新 [sex] " + vi);
                    break;
                case 2:
                    //face
                    vi = dis.readInt();
                    face = (short)vi;
                    //#debug
                    System.out.println("更新 [face] " + vi);
                    
                    refreshImageSet();
                    
                    break;
                case 3:
                    //rebone
                    vi = dis.readInt();
                    reborn = (byte)vi;
                    //#debug
                    System.out.println("更新 [rebone]" + vi);
                    break;
                case 4:
                    //level
                    vi = dis.readInt();
                    int[] v = new int[2];
                    v[0] = 4;
                    v[1] = vi - level;
                    ret.addElement(v);
                    level = (short)vi;
                    //#debug
                    System.out.println("更新 [level] " + vi);
                    break;
                case 5:
                    //exp
                    vi = dis.readInt();
                    exp = vi;
                    //#debug
                    System.out.println("更新 [exp] " + vi);
                    break;
                case 6:
                    //money
                    vi = dis.readInt();
                    v = new int[2];
                    v[0] = 6;
                    v[1] = vi;
                    ret.addElement(v);
                    money += vi;
                    //#debug
                    System.out.println("更新 [money] " + vi);
                    break;
                case 7:
                    //credit
                    vi = dis.readInt();
                    credit += vi;

                    v = new int[2];
                    v[0] = 21;
                    v[1] = vi;

                    ret.addElement(v);

                    //#debug
                    System.out.println("更新 [credit] " + vi);
                    break;
                case 8:
                    //str
                    vi = dis.readInt();
                    baseAttribute[ATTR_STR] = (short)vi;
                    //#debug
                    System.out.println("更新 [str] " + vi);
                    attrChanged = true;
                    break;
                case 9:
                    //agi
                    vi = dis.readInt();
                    baseAttribute[ATTR_AGI] = (short)vi;
                    //#debug
                    System.out.println("更新 [agi] " + vi);
                    attrChanged = true;
                    break;
                case 10:
                    //vit
                    vi = dis.readInt();
                    baseAttribute[ATTR_VIT] = (short)vi;
                    //#debug
                    System.out.println("更新 [vit] " + vi);
                    attrChanged = true;
                    break;
                case 11:
                    //int
                    vi = dis.readInt();
                    baseAttribute[ATTR_INT] = (short)vi;
                    //#debug
                    System.out.println("更新 [int] " + vi);
                    attrChanged = true;
                    break;
                case 12:
                    //luk
                    vi = dis.readInt();
                    luck = vi;
                    //#debug
                    System.out.println("更新 [luk] " + vi);
                    break;
                case 13:
                    //hp
                    vi = dis.readInt();
                    hp += vi;
                    attrChanged = true;
                    //#debug
                    System.out.println("更新 [hp] " + vi);
                    break;
                case 14:
                    //mp
                    vi = dis.readInt();
                    mp += vi;
                    attrChanged = true;
                    //#debug
                    System.out.println("更新 [mp] " + vi);
                    break;
                case 15:
                    //learnPoint
                    vi = dis.readInt();

                    v = new int[2];
                    v[0] = 7;
                    v[1] = vi - learnPoint;
                    ret.addElement(v);

                    learnPoint = (byte)vi;
                    //#debug
                    System.out.println("更新 [learnPoint] " + vi);
                    break;
                case 16:
                    //pattack
                    vi = dis.readInt();
                    //#debug
                    System.out.println("更新 [pattack] " + vi);
                    break;
                case 17:
                    //pdefence
                    vi = dis.readInt();
                    //#debug
                    System.out.println("更新 [pdefence] " + vi);
                    break;
                case 18:
                    //mattack
                    vi = dis.readInt();
                    //#debug
                    System.out.println("更新 [mattack] " + vi);
                    break;
                case 19:
                    //mdefence
                    vi = dis.readInt();
                    //#debug
                    System.out.println("更新 [mdefence] " + vi);
                    break;
                case 20:
                    //hit
                    vi = dis.readInt();
                    //#debug
                    System.out.println("更新 [hit] " + vi);
                    break;
                case 21:
                    //parry
                    vi = dis.readInt();
                    //#debug
                    System.out.println("更新 [parry] " + vi);
                    break;
                case 22:
                    //pcri
                    vi = dis.readInt();
                    //#debug
                    System.out.println("更新 [pcri] " + vi);
                    break;
                case 23:
                    //mcri
                    vi = dis.readInt();
                    //#debug
                    System.out.println("更新 [mcri] " + vi);
                    break;
                case 24:
                    //armor
                    vi = dis.readInt();
                    //#debug
                    System.out.println("更新 [armor] " + vi);
                    break;
                case 25:
                    //gainExp
                    vi = dis.readInt();
                    v = new int[2];
                    v[0] = 5;
                    v[1] = vi;
                    ret.addElement(v);
                    //#debug
                    System.out.println("得到经验 [gainExp] " + vi);
                    break;
                case 26:
                    //upLevelExp
                    vi = dis.readInt();
                    upLevelExp = vi;
                    //#debug
                    System.out.println("升级所需经验 [gainExp] " + vi);

                    break;
                case 27:
                    //bag Size
                    vi = dis.readInt();
                    //#debug
                    System.out.println("包格变为 " + vi);
                    bagSize = (short)vi;
                    break;
                case 28:
                    //战斗技能点
                    vi = dis.readInt();

                    v = new int[2];
                    v[0] = 8;
                    v[1] = vi - restAbility;
                    ret.addElement(v);

                    restAbility = (byte)vi;
                    //#debug
                    System.out.println("剩余战斗技能点数 " + vi);

                    break;
                case 29:
                    //锻造技能点
                    vi = dis.readInt();

                    v = new int[2];
                    v[0] = 9;
                    v[1] = vi - productSkill[0];
                    ret.addElement(v);

                    if(productSkill[0] < 0){
                        v[1] = vi;
                    }else if(vi < 0){
                        v[1]++;
                    }

                    productSkill[0] = (short)vi;
                    //#debug
                    System.out.println("锻造技能点变为 " + productSkill[0]);

                    break;
                case 30:
                    //炼金技能点
                    vi = dis.readInt();

                    v = new int[2];
                    v[0] = 10;
                    v[1] = vi - productSkill[1];
                    ret.addElement(v);

                    if(productSkill[1] < 0){
                        v[1] = vi;
                    }else if(vi < 0){
                        v[1]++;
                    }

                    productSkill[1] = (short)vi;
                    //#debug
                    System.out.println("炼金技能点变为 " + productSkill[1]);

                    break;
                case 31:
                    //裁缝技能点
                    vi = dis.readInt();

                    v = new int[2];
                    v[0] = 11;
                    v[1] = vi - productSkill[2];
                    ret.addElement(v);

                    if(productSkill[2] < 0){
                        v[1] = vi;
                    }else if(vi < 0){
                        v[1]++;
                    }

                    productSkill[2] = (short)vi;
                    //#debug
                    System.out.println("裁缝技能点变为 " + productSkill[2]);

                    break;
                case 32:
                    //采摘技能点
                    vi = dis.readInt();

                    v = new int[2];
                    v[0] = 12;
                    v[1] = vi - productSkill[3];
                    ret.addElement(v);

                    if(productSkill[3] < 0){
                        v[1] = vi;
                    }else if(vi < 0){
                        v[1]++;
                    }

                    productSkill[3] = (short)vi;
                    //#debug
                    System.out.println("采摘技能点变为 " + productSkill[3]);

                    break;
                case 33:
                    //狩猎技能点
                    vi = dis.readInt();

                    v = new int[2];
                    v[0] = 13;
                    v[1] = vi - productSkill[4];
                    ret.addElement(v);

                    if(productSkill[4] < 0){
                        v[1] = vi;
                    }else if(vi < 0){
                        v[1]++;
                    }

                    productSkill[4] = (short)vi;
                    //#debug
                    System.out.println("狩猎技能点变为 " + productSkill[4]);

                    break;
                case 34:
                    //采矿技能点
                    vi = dis.readInt();

                    v = new int[2];
                    v[0] = 14;
                    v[1] = vi - productSkill[5];
                    ret.addElement(v);

                    if(productSkill[5] < 0){
                        v[1] = vi;
                    }else if(vi < 0){
                        v[1]++;
                    }

                    productSkill[5] = (short)vi;
                    //#debug
                    System.out.println("采矿技能点变为 " + productSkill[5]);

                    break;
                case 35:
                    //烹饪技能点
                    vi = dis.readInt();

                    v = new int[2];
                    v[0] = 15;
                    v[1] = vi - productSkill[6];
                    ret.addElement(v);

                    if(productSkill[6] < 0){
                        v[1] = vi;
                    }else if(vi < 0){
                        v[1]++;
                    }

                    productSkill[6] = (short)vi;
                    //#debug
                    System.out.println("烹饪技能点变为 " + productSkill[6]);

                    break;
                case 36:
                    //钓鱼技能点
                    vi = dis.readInt();

                    v = new int[2];
                    v[0] = 16;
                    v[1] = vi - productSkill[7];
                    ret.addElement(v);

                    if(productSkill[7] < 0){
                        v[1] = vi;
                    }else if(vi < 0){
                        v[1]++;
                    }

                    productSkill[7] = (short)vi;
                    //#debug
                    System.out.println("钓鱼技能点变为 " + productSkill[7]);

                    break;
                case 37:
                    //战斗技能点数清空
                    vi = dis.readInt();

                    for(int j = 0; j < ability.length; j++){
                        ability[j] = 0;
                    }

                    skillList = new short[0];

                    initSkillTable();

                    //#debug
                    System.out.println("已会战斗技能清空");

                    break;
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                    int changeId = updataPetAttributes(type, dis, ret);

                    if(changeId != -1){
                        petChangeFlg[petChangeId] = changeId;
                        petChangeId++;
                    }

                    break;
                case 53:
                    //宠物逃跑
                    int petId = dis.readInt();
                    dis.readInt();

                    PetSprite runAwayPet = getPet(petId);

                    //#debug
                    if(runAwayPet != null){
                        //#debug
                        System.out.println("你的宠物：" + runAwayPet.name + " , " + runAwayPet.petId + " 跑掉了");
                        //#debug
                    }

                    petBag.remove(new Integer(petId));
                    World.monsterSetPetBattle = false ;
                    if(petCurrent != null && petCurrent.petId == petId){
                        petCurrent = null;
                    }

                    break;
                case 54:
                    vi = dis.readInt();
                    petBagSize = (short)vi;

                    break;
                case 61:
                    creditName = dis.readUTF();
                    ret.addElement("荣誉：" + creditName);

                    //#debug
                    System.out.println("获得荣誉：" + creditName);

                    break;
                case 62:
                    titleName = dis.readUTF();
                    ret.addElement("称号：" + titleName);

                    //#debug
                    System.out.println("获得称号：" + titleName);

                    break;
                case 63:
                    GameState.password = dis.readUTF();
                    World.saveData(World.RMS_DATA, World.stringToBytes(GameState.password), (byte)2);

                    //#debug
                    System.out.println("密码变为：" + GameState.password);

                    break;
                case 64:
                    GameState.actorName = dis.readUTF();
                    name = GameState.actorName;
                    World.saveData(World.RMS_DATA, World.stringToBytes(GameState.password), (byte)3);

                    //#debug
                    System.out.println("角色名变为：" + GameState.actorName);

                    break;
                case 65:
                    nameColor = dis.readInt();

                    //#debug
                    System.out.println("名字颜色变为" + Integer.toHexString(nameColor));

                    break;
                case 66:
                    protectMode = (byte)dis.readInt();

                    //#debug
                    System.out.println("保护状态变为：" + (protectMode != 0));

                    break;
                case 100:
                    //包位已满
                    vi = dis.readInt();

                    v = new int[2];
                    v[0] = 17;
                    v[1] = 0;

                    ret.addElement(v);
                    //#debug
                    System.out.println("包位已满");

                    break;
                case 101:
                    //宠物包位已满
                    vi = dis.readInt();

                    v = new int[2];
                    v[0] = 20;
                    v[1] = 0;

                    ret.addElement(v);
                    //#debug
                    System.out.println("宠物包位已满");

                    break;
            }
        }

        if(attrChanged){
            reCalculateAttributes();
        }
        for(int i = 0; i < petChangeId; i++){
            PetSprite pet = getPet(petChangeFlg[i]);
            pet.reCalculateBattleData();
        }

        return ret;
    }

    public int getAttribute(int attr){
        switch(attr){
            case ATTR_STR:
            case ATTR_AGI:
            case ATTR_VIT:
            case ATTR_INT:
            case ATTR_PMIN:
            case ATTR_PMAX:
            case ATTR_MMIN:
            case ATTR_MMAX:
            case ATTR_HPMAX:
            case ATTR_MPMAX:
                return attributes[attr];
            case ATTR_PDEF:
                return getDefence();
            case ATTR_MDEF:
                return getMagicDefence();
            case ATTR_PHIT:
                return getPHit();
            case ATTR_MHIT:
                return getMHit();
            case ATTR_FLEE:
                return getFlee();
            case ATTR_PCRI:
                return getPCri();
            case ATTR_MCRI:
                return getMCri();
        }

        return 0;
    }

    public int getShowAttribute(int attr){
        int result = 0;

        switch(attr){
            case ATTR_PHIT:
            case ATTR_MHIT:
            case ATTR_FLEE:
                BattleSprite tmpSprite = new ArmySprite();
                int tmpAttr = this.level + this.level / 4;

                tmpSprite.initBattleData(TYPE_PLAYER, this.level, tmpAttr, tmpAttr, tmpAttr, tmpAttr, 0, 0, 0);
                setTarget(tmpSprite, 0);

                if(attr == ATTR_PHIT){
                    result = calculateHitRate(0, BattleSprite.ACTION_PATTACK);
                }else if(attr == ATTR_MHIT){
                    result = calculateHitRate(0, BattleSprite.ACTION_MATTACK);
                }else{
                    tmpSprite.setTarget(this, 0);

                    int p = tmpSprite.calculateHitRate(0, BattleSprite.ACTION_PATTACK);
                    int m = tmpSprite.calculateHitRate(0, BattleSprite.ACTION_MATTACK);

                    result = (200 - (p + m)) / 2;
                }

                target = null;
                targetIndex = -1;
                targetType = 0;

                break;
            default:
                result = getAttribute(attr);
        }

        return result;
    }

    public static GameItem readItemsData(DataInputStream dis, byte itemType){
        GameItem result = null;
        byte flag;

        try{
            switch(itemType){
                case GameItem.TYPE_BASIC:
                    result = new GameItem(GameItem.TYPE_BASIC);

                    result.itemId = dis.readByte() & 0xFF;
                    result.price = dis.readInt();
                    result.name = dis.readUTF();

                    flag = dis.readByte();

                    result.canUse = (flag & 0x01) != 0;
                    result.bind = (flag & 0x80) != 0;

                    if(result.canUse){
                        result.basicEffect = new int[2];
                        result.basicEffect[0] = dis.readByte();
                        result.basicEffect[1] = dis.readInt();
                    }

                    result.count = (byte)(dis.readByte());

                    break;
                case GameItem.TYPE_EXTEND:
                    result = new GameItem(GameItem.TYPE_EXTEND);

                    result.itemId = dis.readInt();
                    result.price = dis.readInt();
                    result.name = dis.readUTF();

                    flag = dis.readByte();

                    result.canUse = (flag & 0x01) != 0;
                    result.bind = (flag & 0x80) != 0;

                    result.count = (short)dis.readByte();

                    break;
                case GameItem.TYPE_TASK:
                    result = new GameItem(GameItem.TYPE_TASK);

                    result.name = dis.readUTF();
                    result.count = (short)(dis.readByte());

                    break;
                case GameItem.TYPE_EQUIP:
                    result = new GameItem(GameItem.TYPE_EQUIP);

                    result.itemId = dis.readInt();
                    result.id = dis.readInt();
                    result.name = dis.readUTF();
                    dis.readByte(); //装备级别，client暂时没用
                    result.requiredLevel = (short)(dis.readByte() & 0xFF);
                    result.equipLevel = (short)(dis.readByte() & 0xFF);
                    result.equipType = dis.readByte();
                    result.durability = dis.readShort();
                    result.currentDurability = dis.readShort();
                    result.price = dis.readInt();

                    flag = dis.readByte();

                    result.bindType = (byte)(flag & 0x03);

                    result.bind = (flag & 0x80) != 0;
                    result.buildTimes = (short)(dis.readByte() & 0xFF);

                    int pcount = dis.readByte();
                    short[][] pArray = new short[pcount][2];

                    for(int j = 0; j < pcount; j++){
                        pArray[j][0] = dis.readByte();
                        pArray[j][1] = dis.readShort();
                    }

                    result.setProperties(pArray);

                    result.extraEffectColor = dis.readInt();
                    result.extraEffect = dis.readUTF();

                    if(result.extraEffect != null && result.extraEffect.trim().length() == 0){
                        result.extraEffect = null;
                    }

                    break;

            }
        }catch(IOException e){
            e.printStackTrace();

            result = null;
        }

        return result;
    }

    public Vector readBasicItems(DataInputStream dis, boolean drop) throws IOException{
        short basicItemNumber = drop? dis.readByte(): dis.readShort();
        //#debug
        System.out.println();
        //#debug
        System.out.println("基本物品数量：" + basicItemNumber);

        Vector ret = new Vector(basicItemNumber);

        for(int i = 0; i < basicItemNumber; i++){
            GameItem tmpBasicItem = readItemsData(dis, GameItem.TYPE_BASIC);
            addItemToBag(tmpBasicItem);

            if(tmpBasicItem.count != 0)
                ret.addElement(tmpBasicItem);

            //#debug
            System.out.println("    基本物品id: " + tmpBasicItem.itemId + " 数量: " + tmpBasicItem.count);
        }
        reGroupBasicBag();
        return ret;
    }

    public Vector readExtendItems(DataInputStream dis, boolean drop) throws IOException{
        short extItemNumber = drop? dis.readByte(): dis.readShort();
        //#debug
        System.out.println();
        //#debug
        System.out.println("扩展物品数量：" + extItemNumber);

        Vector ret = new Vector(extItemNumber);

        for(int i = 0; i < extItemNumber; i++){
            GameItem tmpExtendItem = readItemsData(dis, GameItem.TYPE_EXTEND);
            addItemToBag(tmpExtendItem);

            if(tmpExtendItem.count != 0)
                ret.addElement(tmpExtendItem);
            //#debug
            System.out.println("扩展物品 : " + tmpExtendItem.itemId + " , " + tmpExtendItem.price + " , " + tmpExtendItem.name + " , " + tmpExtendItem.count);
        }
        return ret;
    }

    public Vector readTaskItems(DataInputStream dis, boolean drop) throws IOException{
        short taskItemNumber = drop? dis.readByte(): dis.readShort();
        //#debug
        System.out.println();
        //#debug
        System.out.println("任务物品数量：" + taskItemNumber);

        Vector ret = new Vector(taskItemNumber);

        for(int i = 0; i < taskItemNumber; i++){
            GameItem tmpTaskItem = readItemsData(dis, GameItem.TYPE_TASK);
            addItemToBag(tmpTaskItem);

            if(tmpTaskItem.count != 0)
                ret.addElement(tmpTaskItem);

            //#debug
            System.out.println("任务物品 : " + tmpTaskItem.name + " , " + tmpTaskItem.count);
        }
        return ret;
    }

    public Vector readEquItems(DataInputStream dis, boolean drop) throws IOException{
        short bagEquipNumber = drop? dis.readByte(): dis.readShort();
        //#debug
        System.out.println();
        //#debug
        System.out.println("装备数量：" + bagEquipNumber);

        Vector ret = new Vector(bagEquipNumber);

        for(int i = 0; i < bagEquipNumber; i++){
            GameItem tmpEquip = readItemsData(dis, GameItem.TYPE_EQUIP);
            addItemToBag(tmpEquip);
            ret.addElement(tmpEquip);

            //#mdebug
            System.out.println("装备物品 ：");

            System.out.println(tmpEquip.name + ": 物品id " + tmpEquip.itemId + " 生成id " + tmpEquip.id + " 需要级别 " + tmpEquip.requiredLevel + " 装备级别 " + tmpEquip.equipLevel

                            + " 装备位置 " + GameItem.EQUIP_TYPE_NAME[tmpEquip.equipType]);

            System.out.println(" 最大耐久 " + tmpEquip.durability + " 剩余耐久 " + tmpEquip.currentDurability + " 价格 " + tmpEquip.price + " 绑定 " + (tmpEquip.bind? "是": "否") + " 打造次数　" + tmpEquip.buildTimes);

            System.out.println("增加体力 " + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_VIT) + " 增加智力" + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_INT) + " 增加力量 "
                            + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_STR) + " 增加敏捷 " + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_AGI) + " 攻击力上限 " + tmpEquip.attackMax + " 攻击力下限 "
                            + tmpEquip.attackMin + " 护甲 " + tmpEquip.defence);

            System.out.println("增加物理攻击 " + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_PATTACK) + " 增加魔法攻击" + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_MATTACK) + " 增加物理防御 "
                            + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_PDEFENCE) + " 增加魔法防御 " + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_MDEFENCE) + " 增加命中 "
                            + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_HIT) + " 增加闪避 " + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_FLEE) + " 增加物理暴击 "
                            + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_PCRI) + " 增加魔法暴击 " + tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_MCRI));
            if(tmpEquip.equipType == GameItem.EQUIP_TYPE_WEAPON){
                System.out.println("武器类型 " + GameItem.WEAPON_TYPE_NAME[tmpEquip.getPropertiesValue(GameItem.EQUIP_ADD_WEAPON_TYPE)]);
            }
            //#enddebug
        }
        return ret;
    }

    public void readBuf(DataInputStream dis) throws IOException{
        byte count = dis.readByte();
        for(int i = 0; i < count; i++){
            int bufid = dis.readInt();
            byte bufType = dis.readByte();
            int bufValue = dis.readInt();
            if(bufType == -1){
                removeBuf(bufid);
            }else{
                addBuf(bufid, bufType, bufValue);
            }
        }

    }

    public Vector readPet(DataInputStream dis) throws IOException{

        byte petCount = dis.readByte();

        Vector ret = new Vector(petCount);
        //#debug
        System.out.println("获得宠物：" + petCount + "个");

        for(int j = 0; j < petCount; j++){
            PetSprite tmpPet = new PetSprite(this);
            tmpPet.initPetData(dis);
            petBag.put(new Integer(tmpPet.petId), tmpPet);
            ret.addElement(tmpPet);
        }

        return ret;
    }

    public PetSprite getPet(int petId){
        return (PetSprite)petBag.get(new Integer(petId));
    }

    public boolean isCurrentPet(PetSprite pet){
        if(petCurrent == null || pet == null){
            return false;
        }

        if(pet.petId == petCurrent.petId){
            return true;
        }else{
            return false;
        }
    }

    public int updataPetAttributes(byte type, DataInputStream dis, Vector ret) throws IOException{
        int petId = dis.readInt();
        PetSprite tmpPet = getPet(petId);
        int retId = -1;
        if(tmpPet == null){
            return -1;
        }
        //#debug
        System.out.println("宠物：" + tmpPet.name + " , " + tmpPet.petId);

        switch(type){
            case 40:
                //宠物名称
                String name = dis.readUTF();
                tmpPet.name = name;
                //#debug
                System.out.println("  名字变为：" + name);

                break;
            case 41: {
                //宠物级别
                int level = dis.readInt();
                tmpPet.level += level;

                int[] v = new int[2];
                v[0] = 19;
                v[1] = tmpPet.level;

                ret.addElement(v);

                retId = petId;

                //#debug
                System.out.println("  级别变为：" + tmpPet.level + " , " + level);
            }
                break;
            case 42:
                //宠物当前可分配点数
                int restPoints = dis.readInt();
                tmpPet.restPoint += restPoints;
                //#debug
                System.out.println("  当前可分配点数变为：" + tmpPet.restPoint + " , " + restPoints);

                break;
            case 43:
                //宠物当前可兑换点数
                int unTradePoints = dis.readInt();
                tmpPet.unTradePoint += unTradePoints;
                //#debug
                System.out.println("  当前可兑换点数变为：" + tmpPet.unTradePoint + " , " + unTradePoints);

                break;
            case 44:
                //宠物忠诚度
                int petFealty = dis.readInt();
                boolean oldCanFollow = tmpPet.canFollow();
                boolean oldCanBattle = tmpPet.canBattle();
                byte oldFealty = tmpPet.getFealtyLevel();
                tmpPet.fealty += petFealty;
                boolean canFollow = tmpPet.canFollow();
                boolean canBattle = tmpPet.canBattle();
                byte fealty = tmpPet.getFealtyLevel();

                //ret.addElement(tmpPet.name + " 忠诚度 " + (petFealty > 0? ("+" + petFealty): String.valueOf(petFealty)));

                String msg = "";
                if(oldFealty != fealty){
                    msg += tmpPet.name + " 的忠诚度变为" + PetSprite.FEALTY_NAMES[fealty] + "\n";
                }

                if(oldCanFollow && !canFollow){
                    msg += tmpPet.name + " 无法跟随和战斗了";
                    World.monsterSetPetBattle = false ;
                }

                if(oldCanBattle && !canBattle){
                    msg += tmpPet.name + " 无法参加战斗了";
                    World.monsterSetPetBattle = false ;
                }

                if(!msg.equals("")){
                    World.showMessage(msg, (byte)10);
                }
                //#debug
                System.out.println("  忠诚度变为：" + tmpPet.fealty + " , " + petFealty + " , " + PetSprite.FEALTY_NAMES[tmpPet.getFealtyLevel()]);

                break;
            case 45:
                //宠物敏捷
                int petAgi = dis.readInt();
                tmpPet.attributes[BattleSprite.ATTR_AGI] += petAgi;
                retId = petId;
                //tmpPet.reCalculateBattleData();
                //#debug
                System.out.println("  敏捷变为：" + tmpPet.attributes[BattleSprite.ATTR_AGI] + " , " + petAgi);

                break;
            case 46:
                //宠物力量
                int petStr = dis.readInt();
                tmpPet.attributes[BattleSprite.ATTR_STR] += petStr;
                retId = petId;
                //tmpPet.reCalculateBattleData();
                //#debug
                System.out.println("  力量变为：" + tmpPet.attributes[BattleSprite.ATTR_STR] + " , " + petStr);

                break;
            case 47:
                //宠物体力
                int petVit = dis.readInt();
                tmpPet.attributes[BattleSprite.ATTR_VIT] += petVit;
                retId = petId;
                //tmpPet.reCalculateBattleData();
                //#debug
                System.out.println("  体力变为：" + tmpPet.attributes[BattleSprite.ATTR_VIT] + " , " + petVit);

                break;
            case 48:
                //宠物智力
                int petInt = dis.readInt();
                tmpPet.attributes[BattleSprite.ATTR_INT] += petInt;
                retId = petId;
                //tmpPet.reCalculateBattleData();
                //#debug
                System.out.println("  智力变为：" + tmpPet.attributes[BattleSprite.ATTR_INT] + " , " + petInt);

                break;
            case 49:
                //宠物hp
                int petHp = dis.readInt();
                tmpPet.hp += petHp;
                //#debug
                System.out.println("  hp变为：" + tmpPet.hp + " , " + petHp);

                break;
            case 50:
                //宠物mp
                int petMp = dis.readInt();
                tmpPet.mp += petMp;
                //#debug
                System.out.println("  mp变为：" + tmpPet.mp + " , " + petMp);

                break;
            case 51: {
                //宠物经验
                int petExp = dis.readInt();
                tmpPet.exp += petExp;

                int[] v = new int[2];
                v[0] = 18;
                v[1] = 1;
                
                boolean found = false;
                
                for(int i = 0; i < ret.size(); i++){
                    Object tmp = ret.elementAt(i);
                    
                    if(tmp instanceof int[]){
                        int[] tmp1 = (int[])tmp;
                        
                        if(tmp1[0] == 18){
                            tmp1[1]++;
                            found = true;
                            
                            break;
                        }
                    }
                }

                if(!found){
                    ret.addElement(v);
                }
                
                //#debug
                System.out.println("  经验变为：" + tmpPet.exp + " , " + petExp);
            }
                break;
            case 52:
                //宠物升级后经验重置
                int petReExp = dis.readInt();
                tmpPet.exp = petReExp;
                //#debug
                System.out.println("  升级后经验重置为：" + tmpPet.exp);

                break;
        }
        return retId;
    }

    public void addItemToBag(GameItem item){
        GameItem search = null;
        if(item.type != GameItem.TYPE_EQUIP)
            search = hasItem(item, false);
        if(search == null || item.type == GameItem.TYPE_EQUIP){
            if(item.type == GameItem.TYPE_EQUIP && item.count < 0){
                search = hasItem(item, false);

                if(search != null){
                    getBag(item.type).removeElement(search);
                }
            }else{
                if(item.type == GameItem.TYPE_EQUIP){
                    getBag(item.type).addElement(item);
                }else if(item.count > 0){
                    GameItem tmpItem = new GameItem(item.type);
                    tmpItem.itemId = item.itemId;
                    tmpItem.price = item.price;
                    tmpItem.name = item.name;
                    tmpItem.canUse = item.canUse;
                    tmpItem.bind = item.bind;
                    tmpItem.count = item.count;
                    tmpItem.basicEffect = item.basicEffect;
                    
                    getBag(tmpItem.type).addElement(tmpItem);
                }
            }
        }else{
            search.count += item.count;
            if(search.count <= 0){
                getBag(item.type).removeElement(search);
            }
        }
    }

    public void reGroupBasicBag(){
        for(int i = 0; i < basicItems.size(); i++){
            for(int j = i; j < basicItems.size(); j++){
                GameItem item1 = (GameItem)basicItems.elementAt(i);
                GameItem item2 = (GameItem)basicItems.elementAt(j);
                if(item1.itemId > item2.itemId){
                    basicItems.setElementAt(item2, i);
                    basicItems.setElementAt(item1, j);
                }
            }
        }
    }

    public Vector getBag(int type){
        switch(type){
            case GameItem.TYPE_BASIC:
                return basicItems;
            case GameItem.TYPE_EQUIP:
                return equipsInBag;
            case GameItem.TYPE_EXTEND:
                return basicItems;
            case GameItem.TYPE_TASK:
                return taskItems;
        }
        return null;
    }

    public int getItemTotalCount(){
        int ret = 0;
        for(int i = 0; i < 4; i++){
            if(i == GameItem.TYPE_EXTEND){
                continue;
            }
            Vector vec = getBag(i);
            if(vec != null)
                ret += vec.size();
        }
        return ret;
    }

    public GameItem hasItem(GameItem item){
        return hasItem(item, true);
    }

    public GameItem hasItem(GameItem item, boolean checkCount){
        if(item == null){
            return null;
        }

        switch(item.type){
            case GameItem.TYPE_TASK: {
                for(int i = 0; i < taskItems.size(); i++){
                    GameItem search = (GameItem)taskItems.elementAt(i);
                    if(search.name.equals(item.name) && (!checkCount || search.count >= item.count)){
                        return search;
                    }
                }
                break;
            }
            case GameItem.TYPE_BASIC: {
                for(int i = 0; i < basicItems.size(); i++){
                    GameItem search = (GameItem)basicItems.elementAt(i);
                    if(search.itemId == item.itemId && (!checkCount || search.count >= item.count)){
                        return search;
                    }
                }
                break;
            }
            case GameItem.TYPE_EQUIP: {
                for(int i = 0; i < equipsInBag.size(); i++){
                    GameItem search = (GameItem)equipsInBag.elementAt(i);
                    if(search.itemId == item.itemId && search.id == item.id){
                        return search;
                    }
                }
                for(int i = 0; i < playerEquips.length; i++){
                    GameItem search = playerEquips[i];
                    if(search.itemId == item.itemId && search.id == item.id && search.type != GameItem.TYPE_NULL){
                        return search;
                    }
                }
                break;
            }
            case GameItem.TYPE_EXTEND: {
                for(int i = 0; i < basicItems.size(); i++){
                    GameItem search = (GameItem)basicItems.elementAt(i);
                    if(search.itemId == item.itemId && (!checkCount || search.count >= item.count)){
                        return search;
                    }
                }
                break;
            }
        }
        return null;
    }

    public void initSkillTable(){
        //战斗技能排序
        for(int i = 0; i < skillList.length; i++){
            for(int j = i; j < skillList.length - 1; j++){
                if(skillList[i] > skillList[j]){
                    short ttt = skillList[i];
                    skillList[i] = skillList[j];
                    skillList[j] = ttt;
                }
            }
        }

        Hashtable tab = new Hashtable();

        for(int i = 0; i < skillList.length; i++){
            Skill skill = Skill.getSkill(skillList[i]);
            Integer effectId = new Integer(skill.effect);
            Integer skillLevel = new Integer(skill.level);

            if(!tab.containsKey(effectId)){
                tab.put(effectId, skill);
            }else{
                //                Integer oldLevel = (Integer)tab.get(effectId);
                Skill nskill = (Skill)tab.get(effectId);

                if(nskill.level < skillLevel.intValue()){
                    tab.put(effectId, skill);
                }
            }
        }

        skillTable = new short[tab.size()][5];

        int n = tab.size() - 1;

        Enumeration tab1 = tab.keys();

        while(tab1.hasMoreElements()){
            Integer effectId = (Integer)tab1.nextElement();
            Skill nskill = (Skill)tab.get(effectId);

            skillTable[n][0] = (short)effectId.intValue();
            skillTable[n][1] = nskill.level;
            skillTable[n][2] = skillTable[n][1];
            skillTable[n][3] = (short)(n + 1);
            skillTable[n][4] = nskill.id;

            n--;
        }
    }

    public void initEquipData(GameItem[] playerEquips, boolean unEquip, int hp, int mp){
        int vit1, int1, str1, agi1, hp1, mp1;

        vit1 = getAttribute(BattleSprite.ATTR_VIT);
        int1 = getAttribute(BattleSprite.ATTR_INT);
        str1 = getAttribute(BattleSprite.ATTR_STR);
        agi1 = getAttribute(BattleSprite.ATTR_AGI);
        hp1 = getAttribute(BattleSprite.ATTR_HPMAX);
        mp1 = getAttribute(BattleSprite.ATTR_MPMAX);
        int v = 1;
        if(unEquip)
            v = -1;

        for(int i = 0; i < playerEquips.length; i++){
            GameItem tmp = playerEquips[i];
            if(tmp == null || tmp.type == GameItem.TYPE_NULL
                            || (tmp.currentDurability <= 0 && tmp.equipType != GameItem.EQUIP_TYPE_NECKLACE && tmp.equipType != GameItem.EQUIP_TYPE_RING && tmp.equipType != GameItem.EQUIP_TYPE_WRIST)){
                continue;
            }

            vit1 += tmp.getPropertiesValue(GameItem.EQUIP_ADD_VIT) * v;
            int1 += tmp.getPropertiesValue(GameItem.EQUIP_ADD_INT) * v;
            str1 += tmp.getPropertiesValue(GameItem.EQUIP_ADD_STR) * v;
            agi1 += tmp.getPropertiesValue(GameItem.EQUIP_ADD_AGI) * v;
        }

        initBattleData(bsType, level, vit1, str1, int1, agi1, luck, hp, mp);

        //        changeHp(hpLimit - hp1);
        //        changeMp(mpLimit - mp1);

        for(int i = 0; i < playerEquips.length; i++){
            GameItem tmp = playerEquips[i];
            if(tmp == null || tmp.type == GameItem.TYPE_NULL
                            || (tmp.currentDurability <= 0 && tmp.equipType != GameItem.EQUIP_TYPE_NECKLACE && tmp.equipType != GameItem.EQUIP_TYPE_RING && tmp.equipType != GameItem.EQUIP_TYPE_WRIST))
                continue;
            attributes[BattleSprite.ATTR_PMIN] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_ATTACK_MIN) * v;
            attributes[BattleSprite.ATTR_PMAX] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_ATTACK_MAX) * v;
            weaponAttack += 0;
            equipDefence += tmp.getPropertiesValue(GameItem.EQUIP_ADD_DEFENCE) * v;
            attributes[BattleSprite.ATTR_PMIN] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_PATTACK) * v;
            attributes[BattleSprite.ATTR_PMAX] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_PATTACK) * v;
            attributes[BattleSprite.ATTR_MMIN] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_MATTACK) * v;
            attributes[BattleSprite.ATTR_MMAX] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_MATTACK) * v;
            attributes[BattleSprite.ATTR_PDEF] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_PDEFENCE) * v;
            attributes[BattleSprite.ATTR_MDEF] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_MDEFENCE) * v;
            attributes[BattleSprite.ATTR_PHIT] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_HIT) * v;
            attributes[BattleSprite.ATTR_MHIT] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_HIT) * v;
            attributes[BattleSprite.ATTR_FLEE] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_FLEE) * v;
            attributes[BattleSprite.ATTR_PCRI] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_PCRI) * v;
            attributes[BattleSprite.ATTR_MCRI] += tmp.getPropertiesValue(GameItem.EQUIP_ADD_MCRI) * v;
        }
    }

    public GameItem[] filteEquips(byte type){
        Vector vec = new Vector();
        for(int i = 0; i < equipsInBag.size(); i++){
            GameItem item = (GameItem)equipsInBag.elementAt(i);
            if(item.equipType == type && item.requiredLevel <= level)
                vec.addElement(item);
        }

        GameItem[] ret = new GameItem[vec.size()];
        vec.copyInto(ret);
        sortEquips(ret);
        return ret;
    }

    public static void sortEquips(GameItem[] equips){
        for(int i = 0; i < equips.length; i++){
            for(int j = i + 1; j < equips.length; j++){
                if(equips[j].compareTo(equips[i]) > 0){
                    GameItem tmp = equips[i];
                    equips[i] = equips[j];
                    equips[j] = tmp;
                }
            }
        }
    }

    public void setIndex(byte in){
        //        if(state == STATE_BATTLE){
        //            frame = frameSequence[in];
        //            if(battleDirect == LEFT)
        //                frame += 3;
        //        }else{
        frame = frameSequence[in];
        //        }
    }

    public void battleStart(byte dir){
        battleDirect = dir;
        if(state != Sprite.STATE_BATTLE){
            state_back = state;
        }

        hpShow = hp;
        mpShow = mp;
        //        World.info = "Sprite.setState " + Runtime.getRuntime().freeMemory();
        setState(STATE_BATTLE);
        //        World.info = "after Sprite.setState";
    }

    public void battleEnd(){
        setSequence(FRAMESEQUENCE_STAND[direct]);
        effSeq = null;
        flyString.removeAllElements();
        showDie = false;
        showHp = true;
        setState(state_back);
        resetBuff();
        clearEffect();
        show = true;
        debufID = 0;
    }

    public void setVisible(boolean visible){
        this.visible = visible;
    }

    public boolean isVisible(){
        return visible;
    }

    public byte getState(){
        return state;
    }

    public void setLocalIndex(byte localIndex){
        this.localIndex = localIndex;
        battleX = getLocalX(localIndex);
        battleY = getLocalY(localIndex);
    }

    public static void initAttackImageSet(int sex) {
    	int index = World.getFaceIndex(sex, true);
        if(World.attackImg[index] == null){
            if (index == 0){
            	World.attackImg[index] = World.getImageSetFromLocal("da_male");
            } else if (index == 1) {
            	World.attackImg[index] = World.getImageSetFromLocal("da_female");
            } else {
            	Object[] tmp = (Object[])World.faceData[index];
            	byte[] pdata = (byte[])tmp[2];
            	byte[] wdata = (byte[])tmp[3];
            	try {
            		World.attackImg[index] = ImageSet.createImageSet(Image.createImage(pdata, 0, pdata.length),
            			new DataInputStream(new ByteArrayInputStream(wdata)), true);
            	} catch (Exception e){
            	}
            }
        }
        if(World.attackWeaponImg[index] == null){
            if(index == 0){
            	World.attackWeaponImg[index] = World.getImageSetFromLocal("da_male_weapon");
            } else if (index == 1) {
            	World.attackWeaponImg[index] = World.getImageSetFromLocal("da_female_weapon");
            } else {
            	Object[] tmp = (Object[])World.faceData[index];
            	byte[] pdata = (byte[])tmp[6];
            	byte[] wdata = (byte[])tmp[7];
            	try {
            		World.attackWeaponImg[index] = ImageSet.createImageSet(Image.createImage(pdata, 0, pdata.length),
            			new DataInputStream(new ByteArrayInputStream(wdata)), true);
            	} catch (Exception e){
            	}
            }
        }
    }

    private void setAttackImageSet(int sex){
        initAttackImageSet(sex);
        int index = World.getFaceIndex(sex, true);
        imageSet = World.attackImg[index];
        weaponImageSet = World.attackWeaponImg[index];
    }

    private void clearAttackImageSet(){
        //attackImg = new ImageSet[2];
        //attackWeaponImg = new ImageSet[2];
    }

    public void setState(byte state){
        setState(state, false);
    }

    public void setState(byte state, boolean focus){

        if(this.state == STATE_WAYPOINT && state == STATE_IDLE && !focus){
            return;
        }

        if(state == STATE_BATTLE && this.state != STATE_BATTLE){
        	World.autoRun = false;
            setAttackImageSet(face);
            setSequence(FRAMESEQUENCE_BATTLE[subState]);

            if(playerEquips == null){
                setWeaponSequence(FRAMESEQUENCE_WEAPON[0]);
            }else{
                GameItem item = playerEquips[GameItem.EQUIP_TYPE_WEAPON];

                int weapon = 0;

                if(item != null && item.type != GameItem.TYPE_NULL){
                    weapon = item.getPropertiesValue(GameItem.EQUIP_ADD_WEAPON_TYPE) + 1;
                }
                setWeaponSequence(FRAMESEQUENCE_WEAPON[weapon]);
            }
        }else if(this.state == STATE_BATTLE && state != STATE_BATTLE){
            if(state == STATE_WAYPOINT && World.nowBattle >= 0){
                return;
            }
            clearAttackImageSet();
            imageSet = World.getPlayerImage(World.getFaceIndex(face, false));
            setSequence(FRAMESEQUENCE_WALK[direct]);
            frame = 0;
        }

        this.state = state;

        if(state == STATE_IDLE){
            setSequence(FRAMESEQUENCE_STAND[direct]);

            //            if(petCurrent != null){
            //                petCurrent.setSequenceIndex(0);
            //            }
        }
    }

    public void refreshImageSet(){
        imageSet = World.getPlayerImage(World.getFaceIndex(face, false));
        World.playerHead = null;
    }

    public void setSequenceIndex(int id){
        setSequenceIndex(id, false);
    }

    public void setSequenceIndex(int id, boolean focus){
        if(focus || /*!testDie()*/!showDie)
            setSequence(FRAMESEQUENCE_BATTLE[id]);
    }

    public short getX(){
        return x;
    }

    public short getY(){
        return y;
    }

    public int[] getOrigin(){
        int[] ret = new int[2];
        ret[0] = x + getWidth() / 2;
        ret[1] = y;
        return ret;
    }

    public short getXPoint(){
        return (short)((x + getWidth() / 2) / World.tileWidth);
    }

    public short getYPoint(){
        return (short)(y / World.tileHeight);
    }

    public short getWidth(){
        if(showDie){
            return (short)World.dieImageSet.getWidth(frame);
        }
        return (short)imageSet.getWidth(frame);
    }

    public short getHeight(){
        if(showDie){
            return (short)World.dieImageSet.getHeight(frame);
        }
        return (short)imageSet.getHeight(frame);
    }

    public short getWidth(int frame){
        if(showDie){
            return (short)World.dieImageSet.getWidth(frame);
        }
        return (short)imageSet.getWidth(frame);
    }

    public short getHeight(int frame){
        if(showDie){
            return (short)World.dieImageSet.getHeight(frame);
        }
        return (short)imageSet.getHeight(frame);
    }

    public short getLocalX(int localIndex){
        return (short)(World.viewWidth - (25 + (1 - localIndex % 2) * 5));
    }

    public short getLocalY(int loaclIndex){
        return (short)(World.LOCATION_TOP + World.LOCATION_HEIGHT * localIndex);
    }

    public void setX(short x){
        this.x = x;
    }

    public void setY(short y){
        this.y = y;
    }

    private void handleMove(World world){
        int xx, yy;
        int x1 = 0, y1 = 0;

        switch(direct){
            case UP:
                x1 = 0;
                y1 = -STEP;

                break;
            case DOWN:
                x1 = 0;
                y1 = STEP;

                break;
            case LEFT:
                x1 = -STEP;

                y1 = 0;
                break;
            case RIGHT:
                x1 = STEP;

                y1 = 0;
                break;
        }

        xx = x1 + x;
        yy = y1 + y;

        short realStep = (short)world.collisionMap(xx, yy - World.tileHeight, getWidth(), World.tileHeight, direct, STEP, x, y - World.tileHeight);

        if(realStep == 0){
            x1 = 0;
            y1 = 0;

            int x2 = 0, y2 = 0, x3 = 0, y3 = 0, newDirect2 = direct, newDirect3 = direct;
            short newRealStep2 = 0, newRealStep3 = 0;
            int newStep = STEP * 3;

            switch(direct){
                case UP:
                    newDirect2 = LEFT;

                    break;
                case DOWN:
                    newDirect2 = LEFT;

                    break;
                case LEFT:
                    newDirect2 = UP;

                    break;
                case RIGHT:
                    newDirect2 = UP;

                    break;
            }

            switch(newDirect2){
                case UP:
                    x2 = 0;
                    y2 = -newStep;

                    break;
                case DOWN:
                    x2 = 0;
                    y2 = newStep;

                    break;
                case LEFT:
                    x2 = -newStep;
                    y2 = 0;

                    break;
                case RIGHT:
                    x2 = newStep;
                    y2 = 0;

                    break;
            }

            xx = x2 + x;
            yy = y2 + y;

            newRealStep2 = (short)world.collisionMap(xx, yy - World.tileHeight, getWidth(), World.tileHeight, newDirect2, newStep, x, y - World.tileHeight);

            switch(direct){
                case UP:
                    newDirect3 = RIGHT;

                    break;
                case DOWN:
                    newDirect3 = RIGHT;

                    break;
                case LEFT:
                    newDirect3 = DOWN;

                    break;
                case RIGHT:
                    newDirect3 = DOWN;

                    break;
            }

            switch(newDirect3){
                case UP:
                    x3 = 0;
                    y3 = -newStep;

                    break;
                case DOWN:
                    x3 = 0;
                    y3 = newStep;

                    break;
                case LEFT:
                    x3 = -newStep;
                    y3 = 0;

                    break;
                case RIGHT:
                    x3 = newStep;
                    y3 = 0;

                    break;
            }

            xx = x3 + x;
            yy = y3 + y;

            newRealStep3 = (short)world.collisionMap(xx, yy - World.tileHeight, getWidth(), World.tileHeight, newDirect3, newStep, x, y - World.tileHeight);

            if(newRealStep2 != newRealStep3){
                if(newRealStep2 > newRealStep3){
                    newRealStep2 = (short)Math.min(newRealStep2, STEP / 2);

                    switch(newDirect2){
                        case UP:
                            x1 = 0;
                            y1 = -newRealStep2;

                            break;
                        case DOWN:
                            x1 = 0;
                            y1 = newRealStep2;

                            break;
                        case LEFT:
                            x1 = -newRealStep2;
                            y1 = 0;

                            break;
                        case RIGHT:
                            x1 = newRealStep2;
                            y1 = 0;

                            break;
                    }
                }else{
                    newRealStep3 = (short)Math.min(newRealStep3, STEP / 2);

                    switch(newDirect3){
                        case UP:
                            x1 = 0;
                            y1 = -newRealStep3;

                            break;
                        case DOWN:
                            x1 = 0;
                            y1 = newRealStep3;

                            break;
                        case LEFT:
                            x1 = -newRealStep3;
                            y1 = 0;

                            break;
                        case RIGHT:
                            x1 = newRealStep3;
                            y1 = 0;

                            break;
                    }
                }
            }
        }else{
            switch(direct){
                case UP:
                    x1 = 0;
                    y1 = -realStep;

                    break;
                case DOWN:
                    x1 = 0;
                    y1 = realStep;

                    break;
                case LEFT:
                    x1 = -realStep;
                    y1 = 0;

                    break;
                case RIGHT:
                    x1 = realStep;
                    y1 = 0;

                    break;
            }
        }

        x += x1;
        y += y1;

    }

    public int[] getBackXY(){
        int ret[] = {
                        x + getWidth() / 2, y
        };
        switch(direct){
            case Sprite.UP:
                ret[1] += /*World.tileHeight*/getHeight() / 2;
                break;
            case Sprite.DOWN:
                ret[1] -= /*World.tileHeight*/getHeight() / 2;
                break;
            case Sprite.LEFT:
                ret[0] += getWidth() + 2;
                break;
            case Sprite.RIGHT:
                ret[0] -= getWidth() + 2;
                break;
        }
        return ret;
    }

    public int[] getPetBackXY(){
        int ret[] = {
                        x + getWidth() / 2, y
        };
        switch(direct){
            case Sprite.UP:
            case Sprite.DOWN:
                ret[0] += getWidth();
                if(ret[0] < 5){
                    ret[0] = x + getWidth() / 2 + getWidth();
                }
                if(ret[0] - World.viewX > World.viewWidth - 5){
                    ret[0] = x + getWidth() / 2 - getWidth();
                }
                ret[1] += getHeight() / 2;
                if(ret[1] - World.viewY > World.viewHeight - 5){
                    ret[1] = y - getHeight() / 2;
                }
                break;
            case Sprite.LEFT:
            case Sprite.RIGHT:
                ret[0] -= getWidth();
                if(ret[0] < 5){
                    ret[0] = x + getWidth() / 2 + getWidth();
                }
                if(ret[0] - World.viewX > World.viewWidth - 5){
                    ret[0] = x + getWidth() / 2 - getWidth();
                }
                ret[1] += getHeight() / 2;
                if(ret[1] - World.viewY > World.viewHeight - 5){
                    ret[1] = y - getHeight() / 2;
                }
                break;
        }
        return ret;
    }

    public static Image topBarImage;

    public static final byte TOPBAR_OFFSET =

    //#if (Directory == SE-K700) || (Directory == NK-NGage) || (Directory == MT-General) || (Directory == Midp2-General) || (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403) || (MIDP2Common == true)
    0
    //#elif (Directory == NK-BigScreen) || (Directory == SE-S700)
    //# 25
    //#else
    //# 20
    //#endif
    ;

    public static final byte HEADIMG_LEFT = 3 + TOPBAR_OFFSET;
    public static final byte HEADIMG_TOP = 1;

    public static final byte TOPBAR_HP_LEFT = 48;
    public static final byte TOPBAR_HP_TOP = 3;
    public static final byte TOPBAR_HP_HEIGHT = 3;
    public static final byte TOPBAR_HP_WIDTH = 42;
    public static final byte TOPBAR_MP_TOP = 9;

    public static final byte TOPBAR_LV_LEFT = 29;
    public static final byte TOPBAR_LV_TOP = 4;

    public static final byte TOPBAR_EXP_LEFT = 27;
    public static final byte TOPBAR_EXP_TOP = 16;
    public static final byte TOPBAR_EXP_WIDTH = 19;
    public static final byte TOPBAR_EXP_HEIGHT = 1;

    public static final byte[] TOPBAR_BUF_X = {
                    51, 64, 77
    };
    public static final byte TOPBAR_BUF_Y = 14;

    public static final int[] CLR_HP = {
                    0xFFDCDC, 0xFF1919, 0xCB0325
    };

    public static final int[] CLR_MP = {
                    0xC9F6FF, 0x378BFF, 0x20346A
    };

    public void drawTopBar(Graphics g){
        if(state == STATE_BATTLE){
            return;
        }
        if(topBarImage == null){
            try{
                //#if Directory == MT-V300
                //# iTimesMIDlet.createImageFromResource("topbar.png");
                //#else
                topBarImage = Image.createImage("/topbar.png");
                //#endif
            }catch(IOException e){
                //#debug
                e.printStackTrace();
            }
        }

        if(topBarImage != null){

            int offset = 0;
            if(World.topbarFlag == 2){
                offset = 25;
            }

            int hw = hp * TOPBAR_HP_WIDTH / getAttribute(BattleSprite.ATTR_HPMAX);
            int mw = mp * TOPBAR_HP_WIDTH / getAttribute(BattleSprite.ATTR_MPMAX);

            if(World.topbarFlag != 1){
                if(hw != 0)
                    for(int i = 0; i < 3; i++){
                        g.setColor(CLR_HP[i]);
                        g.drawLine(TOPBAR_HP_LEFT + TOPBAR_OFFSET - offset, TOPBAR_HP_TOP + i, TOPBAR_HP_LEFT + TOPBAR_OFFSET - offset + hw, TOPBAR_HP_TOP + i);
                    }
                if(mw != 0)
                    for(int i = 0; i < 3; i++){
                        g.setColor(CLR_MP[i]);
                        g.drawLine(TOPBAR_HP_LEFT + TOPBAR_OFFSET - offset, TOPBAR_MP_TOP + i, TOPBAR_HP_LEFT + TOPBAR_OFFSET - offset + mw, TOPBAR_MP_TOP + i);
                    }
            }
            if(World.topbarFlag != 2){
                g.drawImage(World.getPlayerHead(), HEADIMG_LEFT, HEADIMG_TOP, Graphics.TOP | Graphics.LEFT);
            }

            if(World.topbarFlag == 2){
                g.setClip(TOPBAR_OFFSET, 0, topBarImage.getWidth() - offset, 20);
            }else if(World.topbarFlag == 1){
                g.setClip(TOPBAR_OFFSET, 0, 25, topBarImage.getHeight());
            }
            g.drawImage(topBarImage, TOPBAR_OFFSET - offset, 0, Graphics.TOP | Graphics.LEFT);

            if(World.topbarFlag != 0){
                g.setClip(0, 0, World.viewWidth, World.viewHeight);
            }

            if(World.topbarFlag != 1){
                for(int i = 0; i < buf.length; i++){
                    if(buf[i] != null){
                        buf[i].drawIcon(g, TOPBAR_BUF_X[i] + TOPBAR_OFFSET - offset, TOPBAR_BUF_Y);
                    }
                }

                //            g.setColor(0xffffff);
                //            g.setFont(GameState.font);
                //            g.drawString(String.valueOf((int)level), TOPBAR_LV_LEFT, TOPBAR_LV_TOP, Graphics.TOP | Graphics.LEFT);

                String l = String.valueOf(level);

                int lvWidth = l.length() * World.charImageSet.getWidth(0);
                int left = TOPBAR_LV_LEFT + TOPBAR_OFFSET - offset + (World.charImageSet.getWidth(0) * 3 - lvWidth) / 2;

                GameState.drawNumber(g, level, left, TOPBAR_LV_TOP);

                g.setColor(0xffffff);

                hw = exp * TOPBAR_EXP_WIDTH / upLevelExp;
                g.fillRect(TOPBAR_EXP_LEFT + TOPBAR_OFFSET - offset, TOPBAR_EXP_TOP, hw, TOPBAR_EXP_HEIGHT);
            }
        }
    }

    public void draw(Graphics g, short viewX, short viewY){
        if(state == STATE_BATTLE){
            if(showDie){
                World.dieImageSet.drawFrame(g, frame, battleX, battleY, Graphics.LEFT | Graphics.BOTTOM);
            }else{
                imageSet.drawFrame(g, frame, battleX + getWidth(0), battleY, Graphics.RIGHT | Graphics.BOTTOM);

                //draw weapon
                if(weaponIndex != -1){

                    short[] merge = imageSet.collision[frame];
                    short[] weaponFocus = weaponImageSet.collision[weaponFrame];

                    int x = 0;
                    int y = 0;

                    if(merge != null && weaponFocus != null){
                        x = battleX + getWidth(0) - getWidth() + merge[0] + merge[2];
                        y = battleY - getHeight() + merge[1] - weaponFocus[1];
                    }
                    weaponImageSet.drawFrame(g, weaponFrame, x, y, Graphics.TOP | Graphics.RIGHT);
                }
            }
            draw(g);
        }else{
            int fw = 0;
            int ty = y - World.viewY - imageSet.getHeight(0);

            if(protectMode != 0){
                fw = GameState.font.stringWidth(name) + World.protectImageWidth;
            }else{
                fw = GameState.font.stringWidth(name);
            }

            int tx = x - World.viewX - (fw - imageSet.getWidth(0)) / 2;

            if(protectMode != 0){
                g.drawImage(World.protectImage, tx, ty, Graphics.BOTTOM | Graphics.LEFT);
                tx += World.protectImageWidth;
            }

            g.setFont(GameState.font);
            World.draw3DString(g, name, tx, ty, Graphics.LEFT | Graphics.BOTTOM, nameColor==0x000000?Sprite.CLR_NAME:nameColor);

            String drawName = "";

            if(World.titleFlag == 0){
                if(!tongName.equals("")){
                    drawName =  "<" + tongName + ">";
                }
            }else if(World.titleFlag == 1){
                drawName = titleName;
            }else if(World.titleFlag == 2){
                drawName = creditName;
            }else if(World.titleFlag == 3){
                drawName = creditName + "(" + credit + ")";
            }

            if(!drawName.equals("")){
                ty -= GameState.LINE_HEIGHT - 2;

                tx = x - World.viewX - (GameState.font.stringWidth(drawName) - imageSet.getWidth(0)) / 2;
                //#if Draw3DString == TRUE
                World.draw3DString(g, drawName, tx, ty, Graphics.LEFT | Graphics.BOTTOM, nameColor==0x000000?Sprite.CLR_NAME:nameColor);
                //#else
                //# g.setColor(Sprite.CLR_NAME);
                //# g.drawString(drawName, tx, ty, Graphics.LEFT | Graphics.BOTTOM);
                //#endif

                fw = GameState.font.stringWidth(drawName);
                tx = x - World.viewX - (fw - imageSet.getWidth(0)) / 2;
            }

            imageSet.drawFrame(g, frame, x - viewX, y - viewY, Graphics.LEFT | Graphics.BOTTOM);

            if(World.teamLeader){
                ty -= GameState.font.getHeight() + 6;
                ty += ((World.tick / 3) % 2) * 2;
                GameState.drawButtons(g, (byte)3, tx + (fw - 6) / 2, ty);
            }
        }
    }

    public void backupAttributes(int type){

        if(type == 0){
            attributeBackup = new int[17];
            for(int i = 0; i < attributeBackup.length; i++){
                attributeBackup[i] = getAttribute(i);
            }
            /*attributeBackup[0] = attributes[ATTR_STR];
             attributeBackup[1] = attributes[ATTR_AGI];
             attributeBackup[2] = attributes[ATTR_VIT];
             attributeBackup[3] = attributes[ATTR_INT];
             attributeBackup[4] = attributes[ATTR_PMIN];
             attributeBackup[5] = attributes[ATTR_PMAX];
             attributeBackup[6] = getDefence();
             attributeBackup[7] = attributes[ATTR_MMIN];
             attributeBackup[8] = attributes[ATTR_MMAX];
             attributeBackup[9] = getMagicDefence();
             attributeBackup[10] = getPHit();
             attributeBackup[11] = getMHit();
             attributeBackup[12] = getFlee();
             attributeBackup[13] = getPCri();
             attributeBackup[14] = getMCri();
             attributeBackup[15] = attributes[ATTR_HPMAX];
             attributeBackup[16] = attributes[ATTR_MPMAX];*/
        }else{
            attributeBackup = new int[5];
            attributeBackup[0] = baseAttribute[0];
            attributeBackup[1] = baseAttribute[1];
            attributeBackup[2] = baseAttribute[2];
            attributeBackup[3] = baseAttribute[3];
            attributeBackup[4] = learnPoint;
        }
    }

    public void restoreBaseAttrBackup(){
        boolean flag = false;

        for(int i = 0; i < 4; i++){
            if(baseAttribute[i] != attributeBackup[i]){
                flag = true;

                break;
            }
        }

        baseAttribute[0] = (short)attributeBackup[0];
        baseAttribute[1] = (short)attributeBackup[1];
        baseAttribute[2] = (short)attributeBackup[2];
        baseAttribute[3] = (short)attributeBackup[3];
        learnPoint = (byte)attributeBackup[4];
        clearAttributesBackup();

        if(flag){
            World.showMessage("修改已取消", (byte)3);
        }
    }

    public void backupEquips(){
        playerEquipsBackup = new GameItem[playerEquips.length];
        System.arraycopy(playerEquips, 0, playerEquipsBackup, 0, playerEquips.length);
    }

    public void restoreEquipsBackup(){
        if(playerEquipsBackup != null){
            boolean flag = false;

            for(int i = 0; i <playerEquips.length; i++){
                if(playerEquips[i] != playerEquipsBackup[i]){
                    flag = true;

                    break;
                }
            }

            for(int i = 0; i < playerEquips.length; i++){
                if(playerEquips[i].type != GameItem.TYPE_NULL)
                    equipsInBag.addElement(playerEquips[i]);
            }

            for(int i = 0; i < playerEquipsBackup.length; i++){
                equipsInBag.removeElement(playerEquipsBackup[i]);
            }
            System.arraycopy(playerEquipsBackup, 0, playerEquips, 0, playerEquips.length);
            playerEquipsBackup = null;
            reCalculateAttributes();

            if(flag){
                World.showMessage("换装已取消", (byte)3);
            }
        }
    }

    public void clearAttributesBackup(){
        attributeBackup = null;
    }

    public void cycle(long delta, World world){
        lastX = x;
        lastY = y;

        index++;
        if(index >= frameSequence.length){
            index = 0;
        }
        setIndex(index);

        if(state == STATE_IDLE || state == STATE_MOVING){
            leaveParty = false;
        }

        switch(state){
            case STATE_MOVING:
                handleMove(world);
                break;
            case STATE_BATTLE:
                if(weaponIndex != -1){
                    weaponFrame = weaponFrameSequence[weaponIndex];
                    if(weaponIndex < weaponFrameSequence.length - 1){
                        weaponIndex++;
                        if(weaponIndex > weaponFrameSequence.length / 2)
                            if(battleDirect == LEFT){
                                battleX -= 3;
                            }else{
                                battleX += 3;
                            }
                    }
                }

                break;
            case STATE_WAYPOINT:
                cycleWayPoint();
                break;
        }

        if(petCurrent != null && state != STATE_BATTLE){
            int xy[] = getPetBackXY();

            int dx = petCurrent.x - xy[0];
            dx *= dx;

            int dy = petCurrent.y - xy[1];
            dy *= dy;

            if(dx + dy > World.viewWidth * World.viewWidth){
                petCurrent.x = (short)xy[0];
                petCurrent.y = (short)xy[1];
            }else{
                petCurrent.wpMoveTo(xy[0], xy[1]);
                petCurrent.cycle(delta, world);
            }
        }
    }

    public void handleKey(){
        byte direct = -1;

        if(state == STATE_BATTLE){
        }else{
        	if (World.autoRun){
        		direct = this.direct;
        	} else {
	            if(World.isKeyPressed(World.UP_PRESSED, false)){
	                direct = UP;
	            }

	            if(World.isKeyPressed(World.DOWN_PRESSED, false)){
	                direct = DOWN;
	            }

	            if(World.isKeyPressed(World.LEFT_PRESSED, false)){
	                direct = LEFT;
	            }

	            if(World.isKeyPressed(World.RIGHT_PRESSED, false)){
	                direct = RIGHT;
	            }
        	}

            if(direct != -1){
                if(state == STATE_MOVING){
                    if(this.direct != direct){
                        state = STATE_IDLE;
                        setSequence(FRAMESEQUENCE_STAND[direct]);

                        //                        if(petCurrent != null){
                        //                            petCurrent.setSequenceIndex(0);
                        //                        }
                    }
                }else if(state == STATE_IDLE){
                    if(this.direct == direct){
                        state = STATE_MOVING;
                        setSequence(FRAMESEQUENCE_WALK[direct]);

                        //                        if(petCurrent != null){
                        //                            petCurrent.setSequenceIndex(1);
                        //                        }
                    }
                }

                if(this.direct != direct){
                    if(frameSequence == FRAMESEQUENCE_WALK[this.direct]){
                        setSequence(FRAMESEQUENCE_WALK[direct]);
                    }else{
                        setSequence(FRAMESEQUENCE_STAND[direct]);
                    }
                }

                this.direct = direct;

            }else if(state != STATE_WAYPOINT){
                if(state != STATE_IDLE){
                    setSequence(FRAMESEQUENCE_STAND[this.direct]);
                }

                state = STATE_IDLE;
                time = 0;
                setIndex((byte)0);

                //                if(petCurrent != null){
                //                    petCurrent.setSequenceIndex(1);
                //                }
            }
        }
    }

    public int[] getCollisionBox(){
        int x1, y1, w1, h1;

        x1 = x;
        y1 = y - World.tileHeight;
        w1 = getWidth();
        h1 = World.tileHeight;

        return new int[]{
                        x1, y1, w1, h1
        };
    }

    public int[] getOldCollisionBox(){
        int x1, y1, w1, h1;

        x1 = lastX;
        y1 = lastY - World.tileHeight;
        w1 = getWidth();
        h1 = World.tileHeight;

        return new int[]{
                        x1, y1, w1, h1
        };
    }

    public void reCalculateAttributes(){
        int hpBackup, mpBackup;

        hpBackup = hp;
        mpBackup = mp;

        initBattleData(BattleSprite.TYPE_PLAYER, level, baseAttribute[2], baseAttribute[0], baseAttribute[3], baseAttribute[1], luck, hp, mp);
        equipDefence = 0;
        initEquipData(playerEquips, false, hp, mp);

        for(int i = 0; i < buf.length; i++){
            if(buf[i] != null && buf[i].bufType > Buf.BUFTYPE_INT){
                addBufEffect(buf[i]);
            }
        }

        hp = hpBackup;
        mp = mpBackup;

        if(hp > attributes[ATTR_HPMAX])
            hp = attributes[ATTR_HPMAX];
        if(mp > attributes[ATTR_MPMAX])
            mp = attributes[ATTR_MPMAX];
    }

    public int getLevelupExp(){
        return upLevelExp;
    }

    public void deleteAttachmentItem(GameItem attachmentItem){
        if(attachmentItem.type == GameItem.TYPE_MONEY){
            this.money -= attachmentItem.price;
        }else{
            attachmentItem.count *= -1;
            addItemToBag(attachmentItem);
            attachmentItem.count *= -1;
        }
    }
}
