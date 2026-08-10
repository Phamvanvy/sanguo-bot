package pip;


import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.lcdui.Graphics;


public class PetSprite extends BattleSprite{
    public ImageSet imageSet;

    protected byte state;

    public short x;
    public short y;
    public byte direct;
    public Sprite ownerS;
    public MonsterSprite ownerM;
    public ArmySprite ownerA;

    public byte canCatch;

    /**
     * 宠物itemId
     */
    public int petItemId;

    /**
     * 宠物id
     */
    public int petId;

    /**
     * 宠物类型
     */
    public byte petType;

    /**
     * 经验
     */
    public int exp;

    /**
     * 未分配属性点
     */
    public short restPoint;

    /**
     * 未兑换属性点
     */
    public short unTradePoint;

    /**
     * 忠诚度
     */
    public byte fealty;

    /**
     * 是否宠物宝宝
     */
    public boolean isBaby;

    /**
     * 力量型   40-60%  1-10％   1-10％   10-20%  力－敏－智体  物(3)+魔(0-1)+治(0-1)+防(1)
     */
    public static final byte PET_TYPE_TIGER = 1;

    /**
     * 智力型   1-10％   40-60%  10-20%  1-10％   智－体－力敏  物(0-1)+魔(3)+治(1)+防(0-1)
     */
    public static final byte PET_TYPE_DRAGON = 2;

    /**
     * 体力型   10-20%  1-10％   40-60%  1-10％   体－力－智敏  物(1)+魔(0-1)+治(3)+防(0-1)
     */
    public static final byte PET_TYPE_BULL = 3;

    /**
     * 敏捷型   10-20%  1-10％   1-10％   40-60%  敏－力－智体  物(1)+魔(0-1)+治(0-1)+防(3)
     */
    public static final byte PET_TYPE_MONKEY = 4;

    /**
     * 智力体力型   1-10％   30-40％  30-40％  1-10％   智体－力－敏  物(0-1)+魔(2)+治(2)+防(0-1)
     */
    public static final byte PET_TYPE_PIG = 5;

    /**
     * 力量敏捷   30-40％  1-10％   1-10％   30-40％  力敏－智－体  物(2)+魔(0-1)+治(0-1)+防(2)
     */
    public static final byte PET_TYPE_DOG = 6;

    /**
     * 宠物类型名称
     */
    public static final String[] PET_TYPE_NAMES = {
                    "力量型", "智力型", "体力型", "敏捷型", "智力体力型", "力量敏捷型"
    };

    /**
     * 忠诚度名称
     */
    public static final String[] FEALTY_NAMES = {
                    "冷漠", "陌生", "一般", "熟悉", "忠诚"
    };

    /**
     * 忠诚度级别划分
     */
    public static final byte[] FEALTY_LEVEL_OPTION = {
                    0, 16, 31, 56, 81
    };

    /**
     * 冷漠（1～15） 不能跟随    逃跑  不能参与战斗  无增加
     */
    public static final byte FEALTY_LEVEL0 = 0;

    /**
     * 陌生（16～30）    可以跟随    警告  不能参与战斗  无增加
     */
    public static final byte FEALTY_LEVEL1 = 1;

    /**
     * 一般（31～55）    可以跟随    不逃跑 可以参与战斗  （好感度/5）％
     */
    public static final byte FEALTY_LEVEL2 = 2;

    /**
     * 熟悉（56～80）    可以跟随    不逃跑 可以参与战斗  （好感度/5）％
     */
    public static final byte FEALTY_LEVEL3 = 3;

    /**
     * 忠诚（81～100）   可以跟随    不逃跑 可以参与战斗  （好感度/5）％
     */
    public static final byte FEALTY_LEVEL4 = 4;

    public static final byte[] FRAMESEQUENCE_WALK = new byte[]{
                    0, 0, 1, 1
    };

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

    public PetSprite(Object owner, byte type, int hp, int mp, String name){
        this(owner);

        this.petType = type;
        this.attributes[ATTR_HPMAX] = this.hp = this.hpShow = hp;
        this.attributes[ATTR_MPMAX] = this.mp = this.mpShow = mp;
        this.name = name;
        this.canCatch = 0;
    }

    public PetSprite(Object owner){
        if(owner instanceof Sprite){
            ownerS = (Sprite)owner;
            bsType = BattleSprite.TYPE_PLAYER_PET;
        }else if(owner instanceof MonsterSprite){
            ownerM = (MonsterSprite)owner;
            bsType = BattleSprite.TYPE_MONSTER_PET;
        }else{
            ownerA = (ArmySprite)owner;
            bsType = BattleSprite.TYPE_MONSTER_PET;
        }

        int xy[] = null;

        if(ownerS != null){
            xy = ownerS.getBackXY();
        }else if(ownerM != null){
            xy = ownerM.getBackXY();
        }else{
            xy = new int[2];
        }

        x = (short)xy[0];
        y = (short)xy[1];

        if(ownerS != null){
            direct = ownerS.direct;
        }else if(ownerM != null){
            direct = ownerM.direction;
        }else{
            direct = ownerA.battleDirect;
        }

        imageSet = World.petImageSet;
        frameSequence = FRAMESEQUENCE_WALK;
        this.canCatch = 0;
    }

    public ImageSet getImageSet(){
        if(showDie){
            return World.dieImageSet;
        }else{
            return imageSet;
        }
    }

    public void initPetData(DataInputStream dis) throws IOException{
        petItemId = dis.readInt();
        petId = dis.readInt();
        name = dis.readUTF();
        petType = dis.readByte();
        isBaby = dis.readBoolean();
        level = dis.readShort();
        exp = dis.readInt();
        restPoint = dis.readShort();
        unTradePoint = dis.readShort();
        fealty = dis.readByte();
        short petStr = dis.readShort();
        short petAgi = dis.readShort();
        short petVit = dis.readShort();
        short petInt = dis.readShort();
        int petHp = dis.readInt();
        int petMp = dis.readInt();

        initBattleData(bsType, level, petVit, petStr, petInt, petAgi, 0, petHp, petMp);
        //#debug
        System.out.println("宠物名字：" + name + " , itemId：" + petItemId + " , petId：" + petId);
        //#debug
        System.out.println("  宠物类型：" + PET_TYPE_NAMES[petType - 1] + " , 是否宠物宝宝：" + isBaby);
        //#debug
        System.out.println("  级别：" + level + " , 当前经验：" + exp + " , 升级所需经验：" + getUpgradeExp());
        //#debug
        System.out.println("  未分配属性点：" + restPoint + " , 可兑换属性点：" + unTradePoint);
        //#debug
        System.out.println("  忠诚度：" + fealty + " , " + FEALTY_NAMES[getFealtyLevel()]);
        //#debug
        System.out.println("  力量：" + attributes[ATTR_STR] + " , 敏捷：" + attributes[ATTR_AGI] + " , 体力：" + attributes[ATTR_VIT] + " , 智力：" + attributes[ATTR_INT]);
        //#debug
        System.out.println("  生命：" + hp + " , 生命上限：" + attributes[ATTR_HPMAX] + " , 魔法：" + mp + " , 魔法上限：" + attributes[ATTR_MPMAX]);

        byte skillCount = dis.readByte();
        skillList = new short[skillCount];

        //#debug
        System.out.println("  技能数量：" + skillCount);

        for(int j = 0; j < skillCount; j++){
            skillList[j] = dis.readShort();
            //#debug
            System.out.println("    技能id：" + skillList[j]);
        }
    }

    public int[] getBackXY(){
        int ret[] = {
                        x + getWidth() / 2, y
        };
        switch(direct){
            case Sprite.UP:
                ret[1] += /*World.tileHeight*/getHeight();
                break;
            case Sprite.DOWN:
                ret[1] -= /*World.tileHeight*/getHeight();
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

    //----moveto---/
    public boolean wpMoveTo(int destx, int desty){
        int dx, dy;
        int npcX = x + getWidth() / 2;
        int npcY = y;

        dx = Sprite.STEP;
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

    //----moveto----/

    public int getUpgradeExp(){
        return 6 * level + 3;
    }

    public byte getFealtyLevel(){
        byte result = FEALTY_LEVEL0;

        for(int i = FEALTY_LEVEL_OPTION.length - 1; i >= 0; i--){
            if(fealty >= FEALTY_LEVEL_OPTION[i]){
                result = (byte)i;

                break;
            }
        }

        return result;
    }

    public void draw(Graphics g){
        draw(g, false);
    }

    public void draw(Graphics g, boolean focus/*, int dir, int x, int y, int width, int height, int viewX, int viewY*/){
        //        switch(dir){
        //            case Sprite.UP:
        //                y = y + imageSet.getHeight(0);
        //
        //                break;
        //            case Sprite.DOWN:
        //                y = y - imageSet.getHeight(0);
        //
        //                break;
        //            case Sprite.LEFT:
        //                x = x + width;
        //
        //                break;
        //            case Sprite.RIGHT:
        //                x = x - imageSet.getWidth(0);
        //
        //                break;
        //        }

        if(state == Sprite.STATE_BATTLE){
            if(showDie){
                if(direct == Sprite.LEFT){
                    frame -= 3;
                    if(frame < 0){
                        frame = 0;
                    }
                }

                World.dieImageSet.drawFrame(g, frame, battleX, battleY, Graphics.LEFT | Graphics.BOTTOM);
            }else{
                int realFrame = getRealFrame();
                if((realFrame + 1) % 3 == 0 && direct == Sprite.RIGHT){
                    //攻击贞，考虑方向
                    realFrame = 18 + realFrame / 3;
                }

                imageSet.drawFrame(g, realFrame, battleX + getWidth(0), battleY, Graphics.RIGHT | Graphics.BOTTOM);
            }
            super.draw(g);
        }else{

            if(canFollow() || focus){
                int fw = 0;
                int ty = y - World.viewY - imageSet.getHeight(0);
                fw = GameState.font.stringWidth(name);
                int tx = x - World.viewX - (fw - imageSet.getWidth(0)) / 2;
                if(World.nameFlag != 1){
                    g.setFont(GameState.font);
                    //#if Draw3DString == TRUE                
                    World.draw3DString(g, name, tx, ty, Graphics.LEFT | Graphics.BOTTOM, Sprite.CLR_NAME);
                    //#else
                    //# g.setColor(Sprite.CLR_NAME);
                    //# g.drawString(name, tx, ty, Graphics.LEFT | Graphics.BOTTOM);
                    //#endif
                    //World.draw3DString(g, name, tx, ty, Graphics.LEFT | Graphics.BOTTOM, Sprite.CLR_NAME);
                }
                int realFrame = getRealFrame();
                if(World.nameFlag != 2){
                    imageSet.drawFrame(g, realFrame, x - World.viewX, y - World.viewY, Graphics.LEFT | Graphics.BOTTOM);
                }
            }
        }
    }

    public int getRealFrame(){
        int result;
        int pngType = 0;

        switch(petType){
            case 1:
                pngType = 1;

                break;
            case 2:
                pngType = 2;

                break;
            case 3:
                pngType = 4;

                break;
            case 4:
                pngType = 5;

                break;
            case 5:
                pngType = 0;

                break;
            case 6:
                pngType = 3;

                break;
        }

        result = frame + pngType * 3;

        return result;
    }

    public void battleEnd(){
        frameSequence = FRAMESEQUENCE_WALK;
        index = 0;
        
        effSeq = null;
        flyString.removeAllElements();
        showDie = false;
        showHp = true;
        setState(Sprite.STATE_IDLE);
        resetBuff();
        clearEffect();
        debufID = 0;
    }

    public void battleStart(byte dir){
        battleDirect = dir;
        direct = dir;
        hpShow = hp;
        mpShow = mp;
        setState(Sprite.STATE_BATTLE);
        setSequenceIndex(0);
    }

    public void cycle(long delta, World world){
        index++;

        if(index >= frameSequence.length){
            index = 0;
        }

        setIndex(index);
    }

    public short getWidth(){
        if(showDie){
            return (short)World.dieImageSet.getWidth(frame);
        }
        return (short)imageSet.getWidth(frame);
    }

    public short getWidth(int frame){
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

    public short getHeight(int frame){
        if(showDie){
            return (short)World.dieImageSet.getHeight(frame);
        }

        return (short)imageSet.getHeight(frame);
    }

    public short getLocalX(int localIndex){

        int localx = 0;
        if(ownerS != null)
            localx = ownerS.getLocalX(localIndex);
        if(ownerA != null)
            localx = ownerA.getLocalX(localIndex);

        if(battleDirect == Sprite.LEFT){
            localx -= getWidth(0) + 5;
        }else{
            localx += 5;
            if(ownerS != null)
                localx += ownerS.getWidth(0);
            if(ownerA != null)
                localx += ownerA.getWidth(0);
        }

        return (short)localx;
    }

    public short getLocalY(int localIndex){
        return (short)(World.LOCATION_TOP + World.LOCATION_HEIGHT * localIndex);
    }

    public void setLocalIndex(byte localIndex){
        this.localIndex = localIndex;
        battleX = getLocalX(localIndex);
        battleY = getLocalY(localIndex);
    }

    public void setSequenceIndex(int id){
        setSequenceIndex(id, false);
    }

    public void setSequenceIndex(int id, boolean focus){
        if(focus || /*!testDie()*/!showDie){
            setSequence(FRAMESEQUENCE_BATTLE[id]);
        }
    }

    public void setState(byte state){
        this.state = state;
    }

    public boolean canFollow(){
        return fealty >= FEALTY_LEVEL_OPTION[FEALTY_LEVEL1];
    }

    public boolean canBattle(){
        return fealty >= FEALTY_LEVEL_OPTION[FEALTY_LEVEL2];
    }
}