package pip;


import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.lcdui.Graphics;

import pip.io.UWAPSegment;


public class ArmySprite extends BattleSprite{

    public static final int CLR_NAME = 0xcc0000;

    public ImageSet imageSet;

    public byte id;

    public short imageID;

    public byte flag;

    public short exp;

    public byte petType;

    public static final byte PET_TYPE_CANNOT_CATCH = 0;

    public static final byte[][] FRAMESEQUENCE_BATTLE = new byte[][]{
                    //0 = stand
                    new byte[]{
                                    0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1
                    },
                    //1 = run
                    new byte[]{
                        0
                    },
                    //2 = attack
                    new byte[]{
                        2
                    },
                    //3 = take a beating
                    new byte[]{
                        0
                    },
                    //4 = runback
                    new byte[]{
                        0
                    },
                    //5 = die
                    new byte[]{
                                    3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 4, 4, 4, 4
                    }
    };

    public static final byte[][] FRAMESEQUENCE_WEAPON = new byte[][]{
        //空手
        {
                        9, 9, 9, 9, 9, 9
        },
        //剑
        {
                        10, 10, 11, 11, 11, 11
        },
        //斧
        {
                        12, 12, 13, 13, 13, 13
        },
        //枪
        {
                        14, 14, 15, 15, 15, 15
        },
        //仗
        {
                        16, 16, 17, 17, 17, 17
        }
    };
    
    public byte state = 0;

    /**
     * 0- 物品类型 1-物品或技能ID 2-物品数量
     */
    public int[][] items;
    
    /**
     * 武器图片
     */
    public ImageSet weaponImageSet;

    public ArmySprite(ArmySprite m){
        this();

        imageID = m.imageID;
        imageSet = m.imageSet;
        name = m.name;
        flag = m.flag;

        bsType = m.bsType;
        level = m.level;
        luck = m.luck;
        hp = m.hp;
        mp = m.mp;

        petType = m.petType;
        //      strength = m.strength;
        //      agility = m.agility;
        //      vitality = m.vitality;
        //      intelligence = m.intelligence;
        //        pattackMin = m.pattackMin;
        //        pattackMax = m.pattackMax;
        //        pDefence = m.pDefence;
        //        mAttackMin = m.mAttackMin;
        //        mAttackMax = m.mAttackMax;
        //        mDefence = m.mDefence;
        //        phit = m.phit;
        //        mhit = m.mhit;
        //        flee = m.flee;
        //        pcri = m.pcri;
        //        mcri = m.mcri;
        //        hpLimit = m.hpLimit;
        //        mpLimit = m.mpLimit;

        for(int i = 0; i < attributes.length; i++){
            attributes[i] = m.attributes[i];
        }

        hpShow = m.hpShow;
        mpShow = m.mpShow;
        debufStatus = m.debufStatus;
        bufStatus = m.bufStatus;
        weaponAttack = m.weaponAttack;
        equipDefence = m.equipDefence;
        attackAdd = m.attackAdd;
        magicAttackAdd = m.magicAttackAdd;
        defenceAdd = m.defenceAdd;
        magicDefenceAdd = m.magicDefenceAdd;
        hitAdd = m.hitAdd;
        fleeAdd = m.fleeAdd;
        criRateAdd = m.criRateAdd;
        phyDamageAdd = m.phyDamageAdd;
        mgcDamageAdd = m.mgcDamageAdd;
        letPhyDamageAdd = m.letPhyDamageAdd;
        letMgcDamageAdd = m.letMgcDamageAdd;
        skillList = m.skillList;
        skillId = m.skillId;

        hp = attributes[ATTR_HPMAX];
        mp = attributes[ATTR_MPMAX];

        setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);
        setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);

    }

    public ArmySprite(){
        setSequence(FRAMESEQUENCE_BATTLE[state]);
        //        index = (byte)World.random(0, frameSequence.length - 1);
        skillList = new short[0];
        index = 0;
        setIndex(index);
        bsType = TYPE_MONSTER;
    }

    public ArmySprite(int hp, int mp, short imageID, String name){
        this();
        this.attributes[ATTR_HPMAX] = this.hp = this.hpShow = hp;
        this.attributes[ATTR_MPMAX] = this.mp = this.mpShow = mp;
        this.imageID = imageID;
        this.name = name;
        imageSet = World.getSpriteImageSet((short)1, imageID);

        if(imageSet.equals(World.defaultImageSet[1]) && !World.closeMonImageDownload){

            World.sendRequest(GameState.CONN_GET_FILE, new Object[]{
                            GameState.getModel(), new Short((short)-1), new Short((short)GameState.GET_FILE_MONSTER), new Short(imageID)
            }, true, ASyncRequestThread.makeASyncSign(GameState.CONN_GET_FILE, GameState.GET_FILE_MONSTER, imageID));

            //World.requestDownloadImage(imageID, GameState.GET_FILE_MONSTER);
        }

    }

    public ArmySprite(int hp, int mp, int hpLimit, int mpLimit, int sex, String name){
        this();
        this.hp = this.hpShow = hp;
        this.attributes[ATTR_HPMAX] = hpLimit;
        this.mp = this.mpShow = mp;
        this.attributes[ATTR_MPMAX] = mpLimit;
        this.name = name;
        Sprite.initAttackImageSet(sex);
        int index = World.getFaceIndex(sex, true);
        imageSet = World.attackImg[index];
        weaponImageSet = World.attackWeaponImg[index];
        //bsType = TYPE_NET_PLAYER;
    }

    public ImageSet getImageSet(){
        if(showDie){
            return World.dieImageSet;
        }else{
            return imageSet;
        }
    }

    public void setSequenceIndex(int id){
        setSequenceIndex(id, false);
    }

    public void setSequenceIndex(int id, boolean focus){
        if(focus || !showDie)
            setSequence(FRAMESEQUENCE_BATTLE[id]);
    }

    public void cycle(long delta, World world){
        index++;
        if(index == frameSequence.length){
            index = 0;
        }
        setIndex(index);

        if(weaponFrameSequence != null && weaponIndex != -1){
            weaponFrame = weaponFrameSequence[weaponIndex];
            if(weaponIndex < weaponFrameSequence.length - 1){
                weaponIndex++;
                if(weaponIndex > weaponFrameSequence.length / 2)
                    if(battleDirect == Sprite.LEFT){
                        battleX -= 3;
                    }else{
                        battleX += 3;
                    }
            }
        }
    }

    public void draw(Graphics g){
        if(showDie){
            World.dieImageSet.drawFrame(g, frame, battleX, battleY, Graphics.LEFT | Graphics.BOTTOM);
        }else{
            imageSet.drawFrame(g, frame, battleX, battleY, Graphics.LEFT | Graphics.BOTTOM);
            
            //draw weapon
            if(weaponFrameSequence != null && weaponIndex != -1){
                short[] merge = imageSet.collision[frame];
                short[] weaponFocus = weaponImageSet.collision[weaponFrame];

                int x = 0;
                int y = 0;

                if(merge != null && weaponFocus != null){
                    x = battleX + getWidth(0) + merge[0] + merge[2];
                    y = battleY - getHeight() + merge[1] - weaponFocus[1];
                }
                weaponImageSet.drawFrame(g, weaponFrame, x, y, Graphics.TOP | Graphics.RIGHT);
            }
        }
        super.draw(g);
    }

    public void load(DataInputStream dis) throws IOException{
        imageID = dis.readShort();
        imageSet = World.getSpriteImageSet((short)1, imageID);
        if(imageSet.equals(World.defaultImageSet[1]) && !World.closeMonImageDownload){
            
            World.sendRequest(GameState.CONN_GET_FILE, new Object[]{
                            GameState.getModel(), new Short((short)-1), new Short((short)GameState.GET_FILE_MONSTER), new Short(imageID)
            }, true, ASyncRequestThread.makeASyncSign(GameState.CONN_GET_FILE, GameState.GET_FILE_MONSTER, imageID));

            
            //World.requestDownloadImage(imageID, GameState.GET_FILE_MONSTER);
        }
        name = dis.readUTF();
        flag = dis.readByte();

        initBattleData(BattleSprite.TYPE_MONSTER, dis.readShort(), dis.readShort(), dis.readShort(), dis.readShort(), dis.readShort(), 0, 0, 0);
        initSpecial(dis.readShort(), dis.readShort(), dis.readShort(), dis.readShort(), dis.readShort(), dis.readShort(), dis.readShort(), dis.readShort(), dis.readShort(), dis.readShort(), dis
                        .readInt(), dis.readInt());

        //        exp = dis.readShort();

        petType = dis.readByte(); //抓宠类型：0为不可抓

        hp = hpShow = attributes[ATTR_HPMAX];
        mp = mpShow = attributes[ATTR_MPMAX];
        //
        int n = dis.readByte();

        //        skillList = new short[n];
        //        for(int i = 0; i < n; i++){
        //            skillList[i] = dis.readShort();
        //        }

        dis.skip(n * 2);

        //        int n = dis.readByte();
        //        items = new int[n][3];
        //        for(int i = 0; i < n; i++){
        //            items[i][0] = dis.readByte();
        //            items[i][1] = dis.readInt();
        //            if(items[i][0] != 6)
        //                items[i][2] = dis.readByte();
        //        }
        //
        //        n = dis.readByte();
        //        for(int i = 0; i < n; i++){
        //            byte type = dis.readByte();
        //            switch(type){
        //                case 0:
        //                case 1:
        //                case 2:
        //                    dis.skip(8);
        //                case 3:
        //                case 4:
        //                    dis.skip(4);
        //                    break;
        //                case 5:
        //                    dis.skip(8);
        //                    break;
        //            }
        //            dis.skip(2);
        //        }

        name = name + "(" + level + ")";
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
        //#if (Directory == SE-K500) || (Directory == SE-K300) || (Directory == Nokia403)
        //# return (short)(World.LOCATION_LEFT + (1 - localIndex % 2) * 5);
        //#else
        return (short)(World.LOCATION_LEFT + (1 - localIndex % 2) * 20);
        //#endif
    }

    public short getLocalY(int loaclIndex){
        return (short)(World.LOCATION_TOP + World.LOCATION_HEIGHT * localIndex);
    }

    public void setLocalIndex(byte localIndex){
        this.localIndex = localIndex;
        battleX = getLocalX(localIndex);
        battleY = getLocalY(localIndex);
    }

}