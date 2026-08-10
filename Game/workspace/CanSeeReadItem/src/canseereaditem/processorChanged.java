package canseereaditem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.regex.Pattern;

public class processorChanged {
	private static final int CHANGED_TYPE_PROS = 1;
    private static final int CHANGED_TYPE_BASIC_ITEM = 2;
    private static final int CHANGED_TYPE_TASK_ITEM = 3;
    private static final int CHANGED_TYPE_EXTENDED_ITEM = 4;
    private static final int CHANGED_TYPE_EQUIPTMENT = 5;
    private static final int CHANGED_TYPE_BUFF = 6;
    private static final int CHANGED_TYPE_REMOVED_EQUIPMENT = 7;
    private static final int CHANGED_TYPE_PET = 8;
    private static final int CHANGED_TYPE_REMOVED_PET = 9;
    private static final int CHANGED_TYPE_DURABILITY = 10;
    private static final int CHANGED_TYPE_BIND = 11;
//    public LogProcessorChanged(String id){
//        super(id);
//    }
//
//    @Override
//    public String process(String data){
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        DataOutputStream dos = new DataOutputStream(bos);
//
//        try{
//            Pattern pattern = Pattern.compile(" ");
//            String[] tmp = pattern.split(data);
//
//            for(String s : tmp){
//                dos.writeByte(Integer.parseInt(s, 16));
//            }
//
//            return print(bos.toByteArray());
//        }catch(Exception e){
//            e.printStackTrace();
//        }finally{
//            try{
//                dos.close();
//            }catch(Exception e){
//            }
//        }
//
//        return data;
//    }

    @SuppressWarnings("unused")
    public String print(byte[] data){
        StringBuffer sb = new StringBuffer();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bis);

        try{
            while(dis.available() > 0){
                int type = dis.readByte();
                int count = dis.readByte();

                for(int i = 0; i < count; i++){
                    if(i > 0){
                        sb.append(' ');
                    }

                    switch(type){
                        case CHANGED_TYPE_PROS: {
                            int proType = dis.readByte();
                            sb.append(getProsName(proType));
                            sb.append(':');

                            switch(proType){
                                case CREDIT_STRING:
                                case TITLE_STRING:
                                case PASSWORD:
                                case PLAYERNAME: {
                                    String str = dis.readUTF();
                                    sb.append(str);
                                }
                                    break;
                                case PET_NAME: {
                                    int petId = dis.readInt();
                                    String petName = dis.readUTF();
                                    sb.append(petId);
                                    sb.append(' ');
                                    sb.append(petName);
                                }
                                    break;
                                case PET_EXP:
                                case PET_LEVEL:
                                case PET_CURRENTPOINT:
                                case PET_POINT:
                                case PET_FAVOR:
                                case PET_AGILITY:
                                case PET_STRENGTH:
                                case PET_VITALITY:
                                case PET_INTELLIGENCE:
                                case PET_HP:
                                case PET_MP:
                                case PET_RUNAWAY:
                                case PET_UPLEVELEXP:
                                case PET_GRID: {
                                    int petId = dis.readInt();
                                    int value = dis.readInt();
                                    sb.append(petId);
                                    sb.append(' ');
                                    sb.append(value);
                                }
                                    break;
                                default: {
                                    int value = dis.readInt();
                                    sb.append(value);
                                }
                                    break;
                            }
                        }
                            break;
                        case CHANGED_TYPE_BASIC_ITEM: {
                            int itemId = dis.readByte();
                            int price = dis.readInt();
                            String itemName = dis.readUTF();
                            int flag = dis.readByte();
                            boolean canUse = ((flag & 0x1) != 0);

                            if(canUse){
                                int useEffect = dis.readByte();
                                int effectValue = dis.readInt();
                            }
                            dis.readByte();
                            dis.readByte();
                            int itemCount = dis.readByte();

                            sb.append("获得物品[");
                            sb.append(itemName);
                            sb.append(" 数量:");
                            sb.append(itemCount);
                            sb.append(']');
                        }
                            break;
                        case CHANGED_TYPE_TASK_ITEM: {
                            String itemName = dis.readUTF();
                            dis.readByte();
                            dis.readByte();
                            int itemCount = dis.readByte();

                            sb.append("获得任务物品[");
                            sb.append(itemName);
                            sb.append(" 数量:");
                            sb.append(itemCount);
                            sb.append(']');
                        }
                            break;
                        case CHANGED_TYPE_EXTENDED_ITEM: {
                            int itemId = dis.readInt();
                            int price = dis.readInt();
                            String itemName = dis.readUTF();
                            int flag = dis.readByte();
                            dis.readByte();
                            dis.readByte();
                            int itemCount = dis.readByte();

                            sb.append("获得物品[");
                            sb.append(itemName);
                            sb.append(" 数量:");
                            sb.append(itemCount);
                            sb.append(']');
                        }
                            break;
                        case CHANGED_TYPE_EQUIPTMENT: {
                            int itemId = dis.readInt();
                            int instanceId = dis.readInt();
                            String itemName = dis.readUTF();
                            int itemLevel = dis.readByte();
                            int reqairedLevel = dis.readByte();
                            int quality = dis.readByte();
                            int part = dis.readByte();
                            int maxdub = dis.readShort();
                            int curdub = dis.readShort();
                            int price = dis.readInt();
                            int bindType = dis.readByte();
                            int starLevel = dis.readByte();
                            try {
								int subCount = dis.readByte();

								for(int j = 0; j < subCount; j++){
								    int proType = dis.readByte();
								    int proValue = dis.readShort();
								}

								int suitColor = dis.readInt();
								String suitName = dis.readUTF();

								sb.append("获得装备[");
								sb.append(itemName);

								if(suitName.length() > 0){
								    sb.append(" 套装:");
								    sb.append(suitName);
								}
							} catch (Exception e) {
								sb.append("获得装备[");
								sb.append(itemName);
							}

                            sb.append(']');
                        }
                            break;
                        case CHANGED_TYPE_BUFF: {
                            int bufId = dis.readInt();
                            int proType = dis.readByte();
                            int proValue = dis.readInt();

                            sb.append("Buff变化[");
                            sb.append(bufId);
                            sb.append(' ');
                            sb.append(proType);
                            sb.append(' ');
                            sb.append(proValue);
                            sb.append(']');
                        }
                            break;
                        case CHANGED_TYPE_REMOVED_EQUIPMENT: {
                            int itemId = dis.readInt();
                            int instanceId = dis.readInt();

                            sb.append("失去装备[");
                            String equName = MenuType.equs.get(itemId);
                            if(equName!=null && equName.length()>0){
                            	sb.append(equName);
                            }else{
                            	sb.append(itemId);
                            }
                            sb.append("，实例ID："+instanceId);
                            sb.append(']');
                        }
                            break;
                        case CHANGED_TYPE_PET:
                        	int petId = -1;
                        	try {
								petId = dis.readInt();
								int instanceId = dis.readInt();
								String petName = dis.readUTF();
								if (petId>0){
									sb.append("获得宠物ID[");
									sb.append(instanceId + "] 名称[" + petName);
									sb.append("]");
								}
								byte petType = dis.readByte();
								boolean isBaby = dis.readBoolean();
								short petLevel = dis.readShort();
								int petExp = dis.readInt();
								int nextExp = dis.readInt();
								short currentPoint = dis.readShort();
								short point = dis.readShort();
								byte favor = dis.readByte();
								short str = dis.readShort();
								short agi = dis.readShort();
								short vit = dis.readShort();
								short intg = dis.readShort();
								int hp = dis.readInt();
								int mp = dis.readInt();
								
								int spiritualityLevel = dis.readInt();//灵性
								short perceptionLevel = dis.readShort();// 当前悟性等级
					            int perceptionPoint = dis.readInt();// 当前悟性经验
					            sb.append(" 灵性["+spiritualityLevel+"]");
					            sb.append(" 悟性等级["+perceptionLevel+"]");
					            sb.append(" 悟性经验["+perceptionPoint+"]");
					            
					            byte bindType = dis.readByte();//绑定状态
					            boolean binded = dis.readBoolean();
					            short size = dis.readShort();
					            for(int j=0;j<size;j++){
					            	dis.readByte();
					            }
					         
								int skillCount = dis.readByte();
								int[] skills = new int[skillCount];
								if(skillCount > 0){
									sb.append(" 技能：");
								}
								for(int j = 0; j < skillCount; j++){
								    skills[j] = dis.readShort();
								    sb.append("["+MenuType.Skills.get(skills[j]) +"] ");
								}

//								if(type == CHANGED_TYPE_PET){
//								    sb.append("获得宠物[");
//								}else{
//								    sb.append("失去宠物[");
//								}
//
//								sb.append(petId);
//								sb.append(' ');
//								sb.append(instanceId);
//								sb.append(' ');
//								sb.append(petName);
//								sb.append(" 宝宝:");
//								sb.append(isBaby);
//								sb.append(" 级别:");
//								sb.append(petLevel);
//								sb.append(" 经验:");
//								sb.append(petExp);
//								sb.append(" 可兑换技能点:");
//								sb.append(point);
//								sb.append(" 可分配技能点:");
//								sb.append(currentPoint);
//								sb.append(" 忠诚度:");
//								sb.append(favor);
//								sb.append(" 力量:");
//								sb.append(str);
//								sb.append(" 敏捷:");
//								sb.append(agi);
//								sb.append(" 体力:");
//								sb.append(vit);
//								sb.append(" 智力:");
//								sb.append(intg);
//								sb.append(" 生命:");
//								sb.append(hp);
//								sb.append(" 魔法:");
//								sb.append(mp);
//								sb.append(" 技能:");
//
//								for(int j = 0; j < skills.length; j++){
//								    sb.append(skills[j]);
//								    sb.append("["+readitem.Skills.get(skills[j])+"]");
//								    sb.append(' ');
//								}
//
//								sb.append(']');
							} catch (Exception e) {
								if (petId>0){
									if(type == CHANGED_TYPE_PET){
									    sb.append("获得宠物ID[");
									}else{
									    sb.append("失去宠物ID[");
									}

									sb.append(petId);
								}
							}
                        	break;
                        case CHANGED_TYPE_REMOVED_PET: 
                        	petId = -1;
                        	try {
								petId = dis.readInt();
								if (petId>0){
									sb.append("失去宠物ID[");
									sb.append(petId);
									sb.append("]");
								}
                        	} catch (Exception e) {
								if (petId>0){
									sb.append("失去宠物ID[");
									sb.append(petId);
								}
							}
                            break;
                        case CHANGED_TYPE_DURABILITY: {
                            int itemId = dis.readInt();
                            int instanceId = dis.readInt();
                            int dub = dis.readShort();

                            sb.append("耐久变化[");
                            sb.append(itemId);
                            sb.append(' ');
                            sb.append(instanceId);
                            sb.append(' ');
                            sb.append(dub);
                            sb.append(']');
                        }
                            break;
                        case CHANGED_TYPE_BIND: {
                            int itemId = dis.readInt();
                            int instanceId = dis.readInt();

                            sb.append("绑定装备[");
                            sb.append(itemId);
                            sb.append(' ');
                            sb.append(instanceId);
                            sb.append(']');
                        }
                            break;
                    }
                }
                if(sb.length()>0){
                	sb.append("，");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                dis.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    private static String getProsName(int pros){
        switch(pros){
            case SEX:
                return "性别";
            case FACE:
                return "形象";
            case RETURNTIMES:
                return "转生";
            case LEVEL:
                return "级别";
            case EXP:
                return "经验";
            case MONEY:
                return "金钱";
            case CREDIT:
                return "荣誉";
            case STRENGTH:
                return "力量";
            case AGILITY:
                return "敏捷";
            case VITALITY:
                return "体力";
            case INTELLIGENCE:
                return "智力";
            case LUCK:
                return "幸运";
            case HP:
                return "生命";
            case MP:
                return "魔法";
            case LEAVEPOINTS:
                return "剩余属性点";
            case PATTACK:
                return "物攻";
            case PDEFENSE:
                return "物防";
            case MATTACK:
                return "魔攻";
            case MDEFENSE:
                return "魔防";
            case HIT:
                return "命中";
            case PARRY:
                return "格挡";
            case PCRITICAL:
                return "物爆";
            case MCRITICAL:
                return "魔爆";
            case ARMOR:
                return "护甲";
            case GAINEXP:
                return "获得经验";
            case UPLEVELEXP:
                return "升级经验";
            case GRIDSIZE:
                return "包格";
            case POINT:
                return "技能点";
            case SKILL_BLACKSMITHING:
                return "锻造";
            case SKILL_ALCHEMY:
                return "炼金";
            case SKILL_TAILOR:
                return "裁缝";
            case SKILL_HERBALISM:
                return "采摘";
            case SKILL_HUNTERING:
                return "狩猎";
            case SKILL_MINING:
                return "采矿";
            case SKILL_COOKING:
                return "烹饪";
            case SKILL_FISHING:
                return "钓鱼";
            case REFRESH_ABILITY:
                return "遗忘生活技能";
            case PET_NAME:
                return "宠物名字";
            case PET_LEVEL:
                return "宠物级别";
            case PET_CURRENTPOINT:
                return "宠物可兑换属性点";
            case PET_POINT:
                return "宠物可分配技能点";
            case PET_FAVOR:
                return "宠物忠诚度";
            case PET_AGILITY:
                return "宠物敏捷";
            case PET_STRENGTH:
                return "宠物力量";
            case PET_VITALITY:
                return "宠物体力";
            case PET_INTELLIGENCE:
                return "宠物智力";
            case PET_HP:
                return "宠物生命";
            case PET_MP:
                return "宠物魔法";
            case PET_EXP:
                return "宠物经验";
            case PET_RUNAWAY:
                return "宠物逃跑";
            case PET_UPLEVELEXP:
                return "宠物升级经验";
            case PET_GRID:
                return "宠物栏";
            case CREDIT_STRING:
                return "军衔";
            case TITLE_STRING:
                return "称号";
            case PASSWORD:
                return "密码";
            case PLAYERNAME:
                return "角色名";
            case NAMECOLOR:
                return "名字颜色";
            case GUARDSTATE:
                return "保护盾";
            case GRIDFULL:
                return "背包满";
            case PETFULL:
                return "宠物栏满";
        }

        return "";
    }

    private static final byte SEX = 1;
    private static final byte FACE = 2;
    private static final byte RETURNTIMES = 3;
    private static final byte LEVEL = 4;
    private static final byte EXP = 5;
    private static final byte MONEY = 6;
    private static final byte CREDIT = 7;
    private static final byte STRENGTH = 8;
    private static final byte AGILITY = 9;
    private static final byte VITALITY = 10;
    private static final byte INTELLIGENCE = 11;
    private static final byte LUCK = 12;
    private static final byte HP = 13;
    private static final byte MP = 14;
    private static final byte LEAVEPOINTS = 15;
    private static final byte PATTACK = 16;
    private static final byte PDEFENSE = 17;
    private static final byte MATTACK = 18;
    private static final byte MDEFENSE = 19;
    private static final byte HIT = 20;
    private static final byte PARRY = 21;
    private static final byte PCRITICAL = 22;
    private static final byte MCRITICAL = 23;
    private static final byte ARMOR = 24;
    private static final byte GAINEXP = 25;
    private static final byte UPLEVELEXP = 26;
    private static final byte GRIDSIZE = 27;
    private static final byte POINT = 28;

    private static final byte SKILL_BLACKSMITHING = 29;
    private static final byte SKILL_ALCHEMY = 30;
    private static final byte SKILL_TAILOR = 31;
    private static final byte SKILL_HERBALISM = 32;
    private static final byte SKILL_HUNTERING = 33;
    private static final byte SKILL_MINING = 34;
    private static final byte SKILL_COOKING = 35;
    private static final byte SKILL_FISHING = 36;
    private static final byte REFRESH_ABILITY = 37;

    private static final byte PET_NAME = 40;
    private static final byte PET_LEVEL = 41;
    private static final byte PET_CURRENTPOINT = 42; //当前可分配的点数
    private static final byte PET_POINT = 43; //当前可兑换的点数
    private static final byte PET_FAVOR = 44;
    private static final byte PET_AGILITY = 45;
    private static final byte PET_STRENGTH = 46;
    private static final byte PET_VITALITY = 47;
    private static final byte PET_INTELLIGENCE = 48;
    private static final byte PET_HP = 49;
    private static final byte PET_MP = 50;
    private static final byte PET_EXP = 51;
    private static final byte PET_RUNAWAY = 53;
    private static final byte PET_UPLEVELEXP = 52;
    private static final byte PET_GRID = 54;

    private static final byte CREDIT_STRING = 61;
    private static final byte TITLE_STRING = 62;
    private static final byte PASSWORD = 63;
    private static final byte PLAYERNAME = 64;
    private static final byte NAMECOLOR = 65;
    private static final byte GUARDSTATE = 66;

    private static final byte GRIDFULL = 100;
    private static final byte PETFULL = 101;
}
