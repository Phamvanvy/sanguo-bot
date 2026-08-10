package pip;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import pip.io.UWAPSegment;


public class GameItem{
    /**
     * 物品类型
     */
    public byte type;

    /**
     * 物品id
     */
    public int itemId;

    /**
     * 生成id
     */
    public int id;

    /**
     * 物品名称
     */
    public String name;

    /**
     * 物品价格
     */
    public int price;

    /**
     * 堆叠数量
     */
    public short count;

    /**
     * 是否可使用
     */
    public boolean canUse;

    /**
     * 是否可堆叠
     */
    public boolean canHeap;

    //装备附加属性
    /**
     * 需要等级
     */
    public short requiredLevel;

    /**
     * 装备级别
     */
    public short equipLevel;

    /**
     * 装备类型
     */
    public byte equipType;

    /**
     * 耐久力
     */
    public short durability;

    /**
     * 剩余耐久力
     */
    public short currentDurability;

    /**
     * 打造次数
     */
    public short buildTimes;

    /**
     * 攻击力下限
     */
    public int attackMin;

    /**
     * 攻击力上限
     */
    public int attackMax;

    /**
     *  防御力
     */
    public int defence;

    //    /**
    //     * 力量加成
    //     */
    //    public int strAdd;
    //
    //    /**
    //     * 智力加成
    //     */
    //    public int intAdd;
    //
    //    /**
    //     * 体力加成
    //     */
    //    public int vitAdd;
    //
    //    /**
    //     * 敏捷加成
    //     */
    //    public int agiAdd;
    //
    //    /**
    //     * 物理攻击力加成
    //     */
    //    public int pattackAdd;
    //
    //    /**
    //     * 魔法攻击力加成
    //     */
    //    public int mattackAdd;
    //
    //    /**
    //     * 物理防御力加成
    //     */
    //    public int pdefenceAdd;
    //
    //    /**
    //     * 魔法防御力加成
    //     */
    //    public int mdefenceAdd;
    //
    //    /**
    //     * 命中加成
    //     */
    //    public int hitAdd;
    //
    //    /**
    //     * 物理暴击加成
    //     */
    //    public int pcriAdd;
    //
    //    /**
    //     * 魔法暴击加成
    //     */
    //    public int mcriAdd;
    //
    //    /**
    //     * 闪避加成
    //     */
    //    public int fleeAdd;
    //
    //    /**
    //     * 武器类型
    //     */
    //    public int weaponType;

    /**
     * 是否邦定
     */
    public boolean bind;

    public byte bindType;

    public String desc;

    /**
     * 修理费用
     */
    public int repairFee = 0;

    /**
     * 是否装备在身上
     */
    public boolean currentEquip = false;

    /**
     * 附加属性
     */
    public short[][] properties;
    
    /**
     * 装备特殊效果颜色
     */
    public int extraEffectColor;
    
    /**
     * 装备特殊效果
     */
    public String extraEffect;

    //装备附加属性对应数组
    public static final byte EQUIP_ADD_VIT = 1;
    public static final byte EQUIP_ADD_INT = 2;
    public static final byte EQUIP_ADD_STR = 3;
    public static final byte EQUIP_ADD_AGI = 4;
    public static final byte EQUIP_ADD_PATTACK = 5;
    public static final byte EQUIP_ADD_MATTACK = 6;
    public static final byte EQUIP_ADD_PDEFENCE = 7;
    public static final byte EQUIP_ADD_MDEFENCE = 8;
    public static final byte EQUIP_ADD_HIT = 9;
    public static final byte EQUIP_ADD_FLEE = 10;
    public static final byte EQUIP_ADD_PCRI = 11;
    public static final byte EQUIP_ADD_MCRI = 12;
    public static final byte EQUIP_ADD_DEFENCE = 20;
    public static final byte EQUIP_ADD_ATTACK_MAX = 21;
    public static final byte EQUIP_ADD_ATTACK_MIN = 22;
    public static final byte EQUIP_ADD_WEAPON_TYPE = 30;

    public static final int PETFOODID_BEGIN = 580000;
    public static final int PETFOODID_END = 600000;

    public static final String[] EQUIP_PROPERTIES_NAME = new String[]{
                    "体力", "智力", "力量", "敏捷", "物攻", "魔攻", "物防", "魔防", "命中", "闪躲", "物暴", "魔暴"
    };

    //物品类型定义

    /**
     * 空物品
     */
    public static final byte TYPE_NULL = -1;

    /**
     * 基本物品
     */
    public static final byte TYPE_BASIC = 0;

    /**
     * 任务物品
     */
    public static final byte TYPE_TASK = 1;

    /**
     * 一般物品
     */
    public static final byte TYPE_EXTEND = 2;

    /**
     * 装备
     */
    public static final byte TYPE_EQUIP = 3;

    /**
     * 宠物
     */
    public static final byte TYPE_PET = 4;

    /**
     * 金钱
     */
    public static final byte TYPE_MONEY = 99;

    /**
     * 最大堆叠数量
     */
    public static final byte HEAP_MAX = 99;

    //装备级别定义
    /**
     * 基本装备
     */
    public static final byte EQUIP_LEVEL_BASIC = 0;

    /**
     * 优良装备
     */
    public static final byte EQUIP_LEVEL_FINE = 1;

    /**
     * 精良装备
     */
    public static final byte EQUIP_LEVEL_EXCELLENT = 2;

    public static final byte EQUIP_TYPE_TOTAL = 9;

    //装备类型
    /**
     * 头盔
     */
    public static final byte EQUIP_TYPE_HELM = 0;

    /**
     * 项链
     */
    public static final byte EQUIP_TYPE_NECKLACE = 1;

    /**
     * 盔甲
     */
    public static final byte EQUIP_TYPE_ARMOR = 2;

    /**
     * 腰带
     */
    public static final byte EQUIP_TYPE_BELT = 3;

    /**
     * 护腕
     */
    public static final byte EQUIP_TYPE_WRIST = 4;

    /**
     * 戒指
     */
    public static final byte EQUIP_TYPE_RING = 5;

    /**
     * 鞋
     */
    public static final byte EQUIP_TYPE_SHOES = 6;

    /**
     * 武器
     */
    public static final byte EQUIP_TYPE_WEAPON = 7;

    /**
     * 盾牌
     */
    public static final byte EQUIP_TYPE_SHIELD = 8;

    /*/**
     * 宠物
     /
     public static final byte EQUIP_TYPE_PET = 9;*/

    public static final String[] EQUIP_TYPE_NAME = {
                    "头盔", "项链", "盔甲", "腰带", "护腕", "戒指", "鞋", "武器", "盾牌"/*, "宠物"*/
    };

    //武器类型
    /**
     * 剑
     */
    public static final byte WEAPON_TYPE_SWORD = 0;

    /**
     * 斧
     */
    public static final byte WEAPON_TYPE_AXE = 1;

    /**
     * 枪
     */
    public static final byte WEAPON_TYPE_SPEAR = 2;

    /**
     * 法杖
     */
    public static final byte WEAPON_TYPE_STAFF = 3;

    public static final String[] WEAPON_TYPE_NAME = {
                    "剑", "斧", "枪", "法杖"
    };

    //基本物品信息配置表
    public int[] basicEffect;

    public static final byte resourceBegin = 15;
    
    public static final String[] resourceNames = {
        "铜矿石", "锡矿石", "银矿石", "铁矿石", "金矿石","白金矿石", "炭木石", "钛矿石", "钨矿石", "秘银矿石", "藤条", "杨木", "铁杉", "檀木", "紫杉", "香草", "夜亭草", "还魂草", "碧幽果", "玫瑰果", "蝴蝶花", "柠檬草", "梦境草", "荆棘草", "太阳草", 
        "亚麻", "棉花", "蚕茧", "竹笋", "小麦", "大豆", "玉米", "水稻", "羊皮", "牛皮", "鹿皮", "虎皮", "龙皮", "羊角", "羊毛", "鹿茸", "虎爪", "龙鳞", "羊肉", "牛肉", "鹿肉", "虎肉", "龙肉", "杂鱼", "鲤鱼", "草虾", "鲈鱼", "黄鱼", "海蜇", "海星", 
        "海蟹", "海胆", "鲨鱼", "铜锭", "锡锭", "青铜锭", "银锭", "铁锭", "金锭", "白金锭", "煤精", "钢锭", "钛锭", "钨锭", "秘银锭", "棉线", "粗麻线", "细麻线", "毛线", "粗麻布", "细麻布", "棉布", "蚕丝", "毛料", "羊绒", "绒布", "丝绸"
    };
    
    public static final String[] effectDesc = {
                    "生命", "魔法", "生命和魔法"
    };

    public static final int CLR_WHITE = 0xffffff;
    public static final int CLR_GREEN = 0x70e970;
    public static final int CLR_BLUE = 0x6fBBF9;
    public static final int CLR_PURPLE = 0xC73FFF;
    public static final int CLR_RED = 0xFF7777;
    public static final int CLR_ORANGE = 0xFFA800;
    public static final int CLR_YELLOW = 0xFFFF00;

    public static final int[] CLR_EQUIP = new int[]{
                    CLR_WHITE, CLR_GREEN, CLR_BLUE, CLR_PURPLE, CLR_ORANGE, CLR_YELLOW
    };

    //    public static final int[] CLR = new int[]{
    //                    0xffffff, 0xffffff, 0x70e970, 0x6fBBF9, 0xFF7777, 0xFFFF00
    //    };

    public static final byte USETYPE_USE = 1;
    public static final byte USETYPE_THROW = 2;
    public static final byte USETYPE_PET = 3;

    public boolean request_desc = false;
    public int download_desc_state = 0;

    public GameItem(byte type){
        this.type = type;
    }

    public int compareTo(GameItem gi){
        if(gi.type != type)
            return type - gi.type;
        if(gi.type == TYPE_EQUIP && type == TYPE_EQUIP){
            if(gi.equipType != equipType)
                return equipType - gi.equipType;
            else
                return equipLevel - gi.equipLevel;
        }
        return 0;
    }

    private void requestDesc(){
        if(request_desc){
            return;
        }
        int serial = World.sendRequest(GameState.CONN_GET_DESC, new Object[]{
                        new Byte(GameState.DESCTYPE_ITEM), new Integer(itemId), new Byte(GameState.GETTYPE_VIEW)
        }, false);
        //GameState.requestGetDesc(GameState.DESCTYPE_ITEM, itemId, GameState.GETTYPE_VIEW);
        if(serial != -1){
            desc = "正在读取物品信息...";
            requestDescMap.put(new Integer(serial), this);
            request_desc = true;
        }
    }

    public String getItemDesc(){
        if(type == TYPE_BASIC || type == TYPE_EXTEND){
            if(type == TYPE_BASIC && basicEffect != null){
                return "可恢复 " + effectDesc[basicEffect[0]] + " " + basicEffect[1] + " 点";
            }else{
                requestDesc();
                return desc;
            }
        }else{
            return "";
        }
    }

    public String getName(boolean shownum, int width){
        String showName;
        if(type == TYPE_BASIC){
            if(name == null || name.length() == 0){
                name = resourceNames[itemId - resourceBegin];
            }
            
            showName = name;
        }else if(type == TYPE_NULL){
            return "无";
        }else if(type == TYPE_MONEY){
            return "金钱" + price + "J";
        }else{
            showName = name;
        }

        if(shownum)
            if(type == TYPE_EQUIP){
                showName += "";
            }else{
                if(count > 0)
                    showName += " + " + count;
                else
                    showName += count;
            }

        if(width != -1){
            int w = GameState.font.stringWidth(showName);
            boolean changed = false;
            while(w > width){
                showName = showName.substring(0, showName.length() - 1);
                w = GameState.font.stringWidth(showName + "..");
                changed = true;
            }
            if(changed)
                showName += "..";
        }

        return showName;
    }

    public int getColor(boolean select){
        int clr;
        if(select){
            clr = CLR_YELLOW;
        }else if(type == TYPE_EQUIP){
            clr = CLR_EQUIP[equipLevel];
        }else{
            clr = CLR_WHITE;
        }
        return clr;
    }

    public void drawName(Graphics g, int x, int y, boolean shownum, boolean showpart, int width, boolean select){
        g.setFont(GameState.font);

        int clr = getColor(select);

        if(showpart){
            x += 5;
            getEquipPartImage().drawFrame(g, equipType, x, y + 1, Graphics.LEFT | Graphics.TOP);
            x += getEquipPartImage().getWidth(equipType) + 5;
            g.setColor(CLR_WHITE);
            g.drawString("[", x, y, Graphics.TOP | Graphics.LEFT);
            x += GameState.font.stringWidth("[") + 1;

            if(width != -1){
                width -= getEquipPartImage().getWidth(equipType) + 10 + GameState.font.stringWidth("[") + GameState.font.stringWidth("]") + 1;
            }
        }

        String name = getName(shownum, width);
        World.draw3DString(g, name, x, y, Graphics.TOP | Graphics.LEFT, clr);

        if(showpart){
            x += GameState.font.stringWidth(name) + 2;
            g.setColor(CLR_WHITE);
            g.drawString("]", x, y, Graphics.TOP | Graphics.LEFT);
        }
    }

    public int[] calculateInfoTip(boolean showPrice){
        int width = 0;
        int height = 0;
        int plines = 0;
        switch(type){
            case TYPE_BASIC: {

                String desc = getItemDesc();
                int nwidth = GameState.font.stringWidth(getName(false, -1)) + 10;
                int dwidth = GameState.font.stringWidth(desc) + 10;
                width = nwidth > dwidth? nwidth: dwidth;
                if(width > World.viewWidth)
                    width = World.viewWidth - 30;
                Vector vec = World.formatString(desc, width, GameState.font);
                Object[] obj = new Object[vec.size()];
                vec.copyInto(obj);
                plines = 1 + World.getFormatedStringLine(obj, 0, -1);
                height = GameState.LINE_HEIGHT * plines;
                break;
            }
            case TYPE_EXTEND: {
                int nwidth = GameState.font.stringWidth(name) + 10;
                int dwidth = GameState.font.stringWidth(getItemDesc()) + 10;
                int pwidth = GameState.font.stringWidth(price + "ppp:") + 10;
                width = nwidth > pwidth? nwidth: pwidth;
                width = width > dwidth? width: dwidth;
                if(width > World.viewWidth)
                    width = World.viewWidth - 30;
                Vector vec = World.formatString(desc, width, GameState.font);
                Object[] obj = new Object[vec.size()];
                vec.copyInto(obj);
                plines = 3 + World.getFormatedStringLine(obj, 0, -1);
                height = GameState.LINE_HEIGHT * plines;
                break;
            }

            case TYPE_TASK: {
                width = GameState.font.stringWidth(name) + 10;
                height = GameState.LINE_HEIGHT * 2;
                plines = 1;
                break;
            }

            case TYPE_EQUIP: {
                int pnums = 0;
                if(properties != null)
                    for(int i = 0; i < properties.length; i++){
                        if(properties[i][0] <= EQUIP_ADD_MCRI){
                            pnums++;
                        }
                    }
                //属性所占行
                plines = pnums / 2 + pnums % 2;
                //装备名字、类型+绑定状态、需要等级、武器的攻击上下限或防具的防御
                plines += 4;

                if(showPrice)
                    plines++;

                //显示耐久

                if(durability > 0)
                    plines++;
                
                if(extraEffect != null){
                    plines++;
                }

                int fh = GameState.LINE_HEIGHT;
                height = plines * fh;
                int nwidth = GameState.font.stringWidth(name);
                int pwidth = GameState.font.stringWidth("WWW+100  WWW+100");
                width = nwidth > pwidth? nwidth: pwidth;
                break;
            }
            case TYPE_MONEY:
                width = GameState.font.stringWidth(getName(false, -1)) + 10;
                height = GameState.LINE_HEIGHT * 2;
                plines = 1;

                break;
        }
        return new int[]{
                        width, height, plines
        };
    }

    public void drawInfoTip(Graphics g, int x, int y, boolean alpha, boolean showPrice, boolean autoLayout){
        int[] wh = calculateInfoTip(showPrice);
        //int fh = GameState.LINE_HEIGHT;
        int width = wh[0];
        int height = wh[1];

        Object[] info = new Object[wh[2]];
        Hashtable clrTable = new Hashtable();

        if(autoLayout){
            x = (World.viewWidth - width) / 2;
            y = (World.viewHeight - height) / 2;

            if(x < 0){
                x = 0;
            }

            if(y < 0){
                y = 0;
            }
        }

        //GameState.drawBox(g, x, y, width, height, alpha);
        switch(type){
            case TYPE_BASIC: {

                Vector vec = World.formatString(getItemDesc(), width, GameState.font);
                Object[] obj = new Object[vec.size()];
                vec.copyInto(obj);
                info = new Object[1 + vec.size()];
                info[0] = getName(false, -1);
                System.arraycopy(obj, 0, info, 1, obj.length);
                clrTable.put(new Integer(0), new Integer(getColor(false)));
                //drawName(g, x + 3, y, false, false, -1, false);
                //g.setColor(0xffffff);
                //g.drawString(getBasicItemDesc(), x + 3, y + fh, Graphics.TOP | Graphics.LEFT);
                break;
            }
            case TYPE_EXTEND: {
                //                drawName(g, x + 3, y, false, false, -1, false);
                //                g.setColor(0xffffff);
                //                String b = bind? "已绑定": "未绑定";
                //                g.drawString(b, x + 3, y + fh, Graphics.TOP | Graphics.LEFT);
                //                if(showPrice){
                //                    g.drawString("p:" + price, x + 3, y + 2 * fh, Graphics.TOP | Graphics.LEFT);
                //                }

                Vector vec = World.formatString(getItemDesc(), width, GameState.font);
                Object[] obj = new Object[vec.size()];
                vec.copyInto(obj);
                info = new Object[3 + vec.size()];
                System.arraycopy(obj, 0, info, 2, obj.length);

                info[0] = getName(false, -1);
                info[1] = bind? "已绑定": "未绑定";
                clrTable.put(new Integer(0), new Integer(getColor(false)));
                if(showPrice){
                    info[2 + obj.length] = "售价:" + price;
                }else{
                    info[2 + obj.length] = "";
                }

                break;
            }
            case TYPE_TASK:
                //drawName(g, x + 3, y, false, false, -1, false);
                info[0] = getName(false, -1);
                clrTable.put(new Integer(0), new Integer(getColor(false)));
                break;
            case TYPE_EQUIP: {
                int pnums = 0;

                int l = 0;
                int dx = x + 3;
                //                drawName(g, dx, y + l * fh, false, false, -1, false);

                info[l] = this;
                clrTable.put(new Integer(0), new Integer(getColor(false)));

                l++;

                String part = EQUIP_TYPE_NAME[equipType];
                if(equipType == EQUIP_TYPE_WEAPON){
                    part = WEAPON_TYPE_NAME[getPropertiesValue(EQUIP_ADD_WEAPON_TYPE)];
                }

                //g.setColor(0xffffff);
                //g.drawString(part, dx, y + l * fh, Graphics.TOP | Graphics.LEFT);

                part += bind? "     已绑定": "     未绑定";
                //g.drawString(part, x + width - 2 - GameState.font.stringWidth(part), y + l * fh, Graphics.TOP | Graphics.LEFT);
                info[l] = part;
                l++;

                //显示耐久
                if(durability > 0){
                    info[l] = "耐久:" + currentDurability + "/" + durability;
                    l++;
                }

                if(equipType == EQUIP_TYPE_WEAPON){
                    part = "攻击:" + getPropertiesValue(EQUIP_ADD_ATTACK_MIN) + "-" + getPropertiesValue(EQUIP_ADD_ATTACK_MAX);
                }else{
                    part = "防御:" + getPropertiesValue(EQUIP_ADD_DEFENCE);
                }
                //g.drawString(part, dx, y + l * fh, Graphics.TOP | Graphics.LEFT);
                info[l] = part;
                l++;

                part = "";
                dx = x + 3;

                pnums = 0;
                for(int i = 0; i < properties.length; i++){
                    if(properties[i][0] <= EQUIP_ADD_MCRI){
                        g.setColor(CLR_GREEN);
                        part += EQUIP_PROPERTIES_NAME[properties[i][0] - 1];
                        //g.drawString(part, dx, y + l * fh, Graphics.TOP | Graphics.LEFT);
                        dx += GameState.font.stringWidth(part) + 2;
                        if(properties[i][1] > 0){
                            g.setColor(CLR_GREEN);
                            part += "+" + properties[i][1];
                        }else if(properties[i][1] < 0){
                            g.setColor(CLR_RED);
                            part += String.valueOf((int)properties[i][1]);
                        }

                        //g.drawString(part, dx, y + l * fh, Graphics.TOP | Graphics.LEFT);
                        if(pnums % 2 == 0){
                            dx += GameState.font.stringWidth(part) + 4;
                            part += "     ";
                        }else{
                            clrTable.put(new Integer(l), new Integer(CLR_GREEN));
                            info[l] = part;
                            part = "";
                            l++;
                            dx = x + 3;
                        }
                        pnums++;
                    }
                }
                if(pnums % 2 != 0){
                    clrTable.put(new Integer(l), new Integer(CLR_GREEN));
                    info[l] = part;
                    l++;
                }
                dx = x + 3;
                
                if(extraEffect != null){
                    clrTable.put(new Integer(l), new Integer(extraEffectColor));
                    info[l] = extraEffect;
                    l++;
                }
                
                //                g.setColor(0xffffff);
                //                g.drawString("需要等级:" + requiredLevel, dx, y + l * fh, Graphics.TOP | Graphics.LEFT);
                //                
                info[l] = "需要等级:" + requiredLevel;
                if(requiredLevel > World.player.level)
                    clrTable.put(new Integer(l), new Integer(0xff0000));

                l++;

                if(showPrice){
                    //g.drawString(String.valueOf(price), dx, y + l * fh, Graphics.TOP | Graphics.LEFT);
                    info[l] = "售价:" + price;
                    clrTable.put(new Integer(l), new Integer(0xffff00));
                }
            }

                break;
            case TYPE_MONEY:
                info[0] = getName(false, -1);
                clrTable.put(new Integer(0), new Integer(getColor(false)));

                break;
        }

        GameState.drawMsgTip(g, x, y, info, clrTable, null, 0);
    }

    public void setProperties(short[][] data){
        properties = data;
        for(int i = 0; i < properties.length; i++){
            for(int j = 0; j < properties.length; j++){
                if(properties[j][0] > properties[i][0]){
                    short[] v = properties[j];
                    properties[j] = properties[i];
                    properties[i] = v;
                }
            }
        }

    }

    public short getPropertiesValue(byte type){
        for(int i = 0; i < properties.length; i++){
            if(properties[i][0] == type)
                return properties[i][1];
        }
        return 0;
    }

    public static GameItem createNullEquip(byte part){
        GameItem item = new GameItem(TYPE_NULL);
        item.equipType = part;
        return item;
    }

    public boolean equals(Object obj){
        if(obj == null || !(obj instanceof GameItem)){
            return false;
        }

        GameItem item = (GameItem)obj;

        if(item.type != type)
            return false;

        switch(item.type){
            case TYPE_BASIC:
                return item.itemId == itemId;
            case TYPE_TASK:
                return item.name.equals(name);
            case TYPE_EXTEND:
                return item.itemId == itemId;
            case TYPE_EQUIP:
                return (item.itemId == itemId && item.id == id);
        }

        return false;
    }

    public static ImageSet part;

    public static ImageSet getEquipPartImage(){
        if(part == null){
            part = World.getImageSetFromLocal("equips");
        }
        return part;
    }

    public UWAPSegment getUseSegment(byte useType, int count) throws IOException{
        UWAPSegment segment = new UWAPSegment(GameState.CONN_USE_ITEM);
        segment.writeByte((byte)useType);//使用 or 丢弃
        switch(this.type){
            case TYPE_BASIC:
                segment.writeByte(TYPE_BASIC);
                segment.writeInt(this.itemId);
                segment.writeInt(count);
                break;
            case TYPE_EXTEND:
                segment.writeByte(TYPE_EXTEND);
                segment.writeInt(this.itemId);
                segment.writeInt(count);

                break;
            case TYPE_TASK:
                segment.writeByte(TYPE_TASK);
                segment.writeString(this.name);
                segment.writeInt(count);

                break;
            case TYPE_EQUIP:
                segment.writeByte(TYPE_EQUIP);
                segment.writeInt(this.itemId);
                segment.writeInt(this.id);

                break;
        }

        return segment;
    }

    public static byte[] getMailBytes(GameItem item){
        byte[] result = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try{
            switch(item.type){
                case TYPE_BASIC:
                    dos.writeByte(2);
                    dos.writeInt(item.itemId);
                    dos.writeByte((byte)item.count);

                    break;
                case TYPE_EXTEND:
                    dos.writeByte(4);
                    dos.writeInt(item.itemId);
                    dos.writeByte((byte)item.count);

                    break;
                case TYPE_TASK:
                    dos.writeByte(3);
                    dos.writeUTF(item.name);
                    dos.writeByte((byte)item.count);

                    break;
                case TYPE_EQUIP:
                    dos.writeByte(5);
                    dos.writeInt(item.itemId);
                    dos.writeInt(item.id);

                    break;
                case TYPE_MONEY:
                    dos.writeByte(1);
                    dos.writeInt(item.price);

                    break;
                case TYPE_NULL:
                    break;
            }

            result = bos.toByteArray();
        }catch(Exception e){
            result = null;
            //#debug
            e.printStackTrace();
        }finally{
            try{
                dos.close();
            }catch(Exception e){
            }
        }

        return result;
    }

    public static GameItem getAttachment(byte[] data){
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        GameItem result = null;

        try{
            byte type = dis.readByte();
            dis.readByte(); //count skip

            switch(type){
                case 1:
                    //金钱
                    dis.readByte(); //属性类型 skip

                    result = new GameItem(TYPE_MONEY);
                    result.price = dis.readInt(); //money

                    break;
                case 2:
                    //基本物品
                    result = Sprite.readItemsData(dis, GameItem.TYPE_BASIC);

                    break;
                case 3:
                    //任务物品
                    result = Sprite.readItemsData(dis, GameItem.TYPE_TASK);

                    break;
                case 4:
                    //扩展物品
                    result = Sprite.readItemsData(dis, GameItem.TYPE_EXTEND);

                    break;
                case 5:
                    //装备
                    result = Sprite.readItemsData(dis, GameItem.TYPE_EQUIP);

                    break;
            }
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }finally{
            try{
                dis.close();
            }catch(Exception e){
            }
        }

        return result;
    }

    public boolean isPetFood(){
        return itemId >= PETFOODID_BEGIN && itemId <= PETFOODID_END;
    }

    public boolean canUse(){
        return canUse;
    }

    public void use(BattleSprite target){
        byte usetype = 0;
        if(target instanceof PetSprite){
            usetype = USETYPE_PET;
        }else{
            usetype = USETYPE_USE;
        }
        World.requestUseItem(this, usetype, 1);
        if(type == TYPE_BASIC){
            count--;
            if(count <= 0){
                World.player.getBag(type).removeElement(this);
            }
            int[] eff = getEffect();
            if(eff[0] != 0){
                target.addHp(eff[0]);
            }
            if(eff[1] != 0){
                target.addMp(eff[1]);
            }
        }else if(type == TYPE_EXTEND){
            if(World.gameState != null && World.gameState.type == GameState.STATE_EDITITEM && usetype == USETYPE_USE){
                World.setGameState(null);
            }
        }
    }

    public int[] getEffect(){
        int[] ret = new int[2];
        
        if(basicEffect != null){
            switch(basicEffect[0]){
                case 0:
                    //加hp
                    ret[0] = basicEffect[1];
                    break;
                case 1:
                    //加mp
                    ret[1] = basicEffect[1];
                    break;
                case 2:
                    //加hp和mp
                    ret[0] = basicEffect[1];
                    ret[1] = basicEffect[1];
                    break;
            }
        }

        return ret;
    }

    public static Hashtable requestDescMap = new Hashtable();

    public static GameItem getItemFromRequestMap(int serial){
        return (GameItem)requestDescMap.remove(new Integer(serial));
    }
}