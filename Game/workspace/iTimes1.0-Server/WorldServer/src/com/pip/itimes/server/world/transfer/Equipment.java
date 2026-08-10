package com.pip.itimes.server.world.transfer;

import java.io.*;
import java.util.*;
import com.pip.itimes.server.stage.Enhance;

/**
 * @author Jeffery
 * @version 1.0
 */
public class Equipment{

    private short level;
    private short requiredLevel;
    private byte createType;
    private byte quality;
    private byte part;
    private short durability;
    private short currentDurability;
    private byte bindType;
    private boolean binded;
    private Map properties = new HashMap();
    private int id;
    private int price;
    private String name;
    private int times;
    private int itemId;
    private boolean isTemplate;
    private String desc;
    private List<Enhance> enhances = new ArrayList<Enhance>(9);


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

    public static final byte PART_HEAD = 0;
    public static final byte PART_NECK = 1;
    public static final byte PART_CHEST = 2;
    public static final byte PART_WAIST = 3;
    public static final byte PART_WRIST = 4;
    public static final byte PART_FINGER = 5;
    public static final byte PART_FEET = 6;
    public static final byte PART_WEAPON = 7;
    public static final byte PART_SHIELD = 8;

    public static final short WEAPON_SWORD = 0;
    public static final short WEAPON_AXE = 1;
    public static final short WEAPON_SPEAR = 2;
    public static final short WEAPON_STAFF = 3;



    public static final byte CREATE_NORMAL = 1;
    public static final byte CREATE_DYNAMIC = 2;

    public Equipment() {
    }


    public void setLevel(short level){
        this.level = level;
    }

    public short getLevel() {
        return level;
    }

    public void setRequiredLevel(short requiredLevel){
        this.requiredLevel = requiredLevel;
    }

    public short getRequiredLevel() {
        return requiredLevel;
    }

    public void setCreateType(byte createType){
        this.createType = createType;
    }

    public byte getCreateType(){
        return createType;
    }


    public byte getQuality(){
        return quality;
    }

    public void setQuality(byte quality){
        this.quality = quality;
    }


    public void setPart(byte part){
        this.part = part;
    }

    public byte getPart() {
        return part;
    }

    public void setDurability(short durability){
        this.durability = durability;
    }

    public short getDurability() {
        return durability;
    }

    public void setCurrentDurability(short durability){
        this.currentDurability = durability;
    }

    public short getCurrentDurability(){
        return currentDurability;
    }

    public void setBindType(byte bindType){
        this.bindType = bindType;
    }

    public byte getBindType() {
        return bindType;
    }

    public void setBinded(boolean binded){
        this.binded = binded;
    }

    public boolean isBinded(){
        return binded;
    }

    public void addProperty(int index,short value){
        if(value==0)
            return;
        Integer oldValue = (Integer)properties.get(index);
        if(oldValue!=null){
            value = (short)(value + oldValue.intValue());
        }
        properties.put(new Integer(index),new Integer(value));
    }

    public short getProperty(int index) {
        Integer ret = (Integer)properties.get(new Integer(index));
        if(ret==null){
            return 0;
        }
        return ret.shortValue();
    }

    public int[][] getProperties(){
        int[][] ret = new int[properties.size()][2];
        Set entrys = properties.entrySet();
        Iterator ite = entrys.iterator();
        int i=0;
        while(ite.hasNext()){
            Map.Entry entry = (Map.Entry)ite.next();
            ret[i][0] = ((Integer)entry.getKey()).intValue();
            ret[i][1] = ((Integer)entry.getValue()).intValue();
            i++;
        }
        return ret;
    }

    public void setItemId(int itemId){
        this.itemId = itemId;
    }

    public int getItemId(){
        return itemId;
    }

    public void setId(int id){
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public byte getType() {
        return 3;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public int getPrice() {
        return price;
    }


    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getTimes(){
        return enhances.size();
    }

    public String getDesc() {
        return desc;
    }

    public boolean isTemplate(){
        return isTemplate;
    }

    public void setTemplate(boolean b){
        this.isTemplate = b;
    }


    public List<Enhance> getEnhances(){
        return enhances;
    }

    public void addEnhance(Enhance enhance){
        if(enhance==null)
            throw new IllegalArgumentException("enhance can not be null");
        if(enhances.size()>=9)
            throw new IllegalStateException("enhances can not >9");
        enhances.add(enhance);
        addProperty(enhance.getProperty(),(short)enhance.getPoint(enhances.size()));
    }

//    public Equipment newInstance(){
//        int id = IDGenerator.getEquipmentId();
//        Equipment equ = new Equipment();
//        equ.level = level;
//        equ.requiredLevel = requiredLevel;
//        equ.createType = createType;
//        equ.quality = quality;
//        equ.part = part;
//        equ.durability = durability;
//        equ.currentDurability = durability;
//        equ.bindType = bindType;
//        equ.properties = new HashMap(properties);
//        equ.price = price;
//        equ.name = name;
//        equ.times = 0;
//        equ.itemId = itemId;
//        equ.id = id;
//        if(bindType==IItem.BIND_GET){
//            equ.binded = true;
//        }
//        return equ;
//    }

//    public static Equipment[] getEquipments(byte[] bytes) throws IOException{
//        List l = new ArrayList();
//        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//        DataInputStream dis = new DataInputStream(bis);
//        int count = dis.readShort();
//        for(int i=0;i<count;i++){
//            Equipment equ = new Equipment();
//            int itemId = dis.readInt();
//            equ.setItemId(itemId);
//            int id = dis.readInt();
//            equ.setId(id);
//            String title = dis.readUTF();
//            equ.setName(title);
//            byte itemLevel = dis.readByte();
//            equ.setLevel(itemLevel);
//            byte requiredLevel = dis.readByte();
//            equ.setRequiredLevel(requiredLevel);
//            byte equLevel = dis.readByte();
//            equ.setEquipmentLevel(equLevel);
//            byte part = dis.readByte();
//            equ.setPart(part);
//            byte durability = dis.readByte();
//            equ.setDurability(durability);
//            byte currentDurability = dis.readByte();
//            equ.setCurrentDurability(currentDurability);
//            int price = dis.readInt();
//            equ.setPrice(price);
//            byte bind = dis.readByte();
//            equ.setBindType((byte)((bind&127)&0xFF));
//            equ.setBinded((bind&0x80)!=0);
//            byte times = dis.readByte();
//            equ.setTimes(times);
//            byte size = dis.readByte();
//            for (int j = 0; j < size; j++) {
//                byte type = dis.readByte();
//                short value = dis.readByte();
//                equ.addProperty(type,value);
//            }
//            l.add(equ);
//        }
//        Equipment[] ret = new Equipment[l.size()];
//        l.toArray(ret);
//        return ret;
//    }

//    public static Equipment getEquipments(DataInputStream dis) throws
//            IOException {
//        Equipment equ = new Equipment();
//        int itemId = dis.readInt();
//        equ.setItemId(itemId);
//        int id = dis.readInt();
//        equ.setId(id);
//        String title = dis.readUTF();
//        equ.setName(title);
//        byte itemLevel = dis.readByte();
//        equ.setLevel(itemLevel);
//        byte requiredLevel = dis.readByte();
//        equ.setRequiredLevel(requiredLevel);
//        byte quality = dis.readByte();
//        equ.setQuality(quality);
//        byte part = dis.readByte();
//        equ.setPart(part);
//        short durability = dis.readShort();
//        equ.setDurability(durability);
//        short currentDurability = dis.readShort();
//        equ.setCurrentDurability(currentDurability);
//        int price = dis.readInt();
//        equ.setPrice(price);
//        byte bind = dis.readByte();
//        equ.setBindType((byte)((bind&127)&0xFF));
//        equ.setBinded((bind&0x80)!=0);
//        byte times = dis.readByte();
//        equ.setTimes(times);
//        byte size = dis.readByte();
//        for (int j = 0; j < size; j++) {
//            byte type = dis.readByte();
//            short value = dis.readShort();
//            equ.addProperty(type, value);
//        }
//        return equ;
//    }

    public byte[] toClientBytes() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(itemId);
            dos.writeInt(id);
            dos.writeUTF(name);
            dos.writeByte(level);
            dos.writeByte(requiredLevel);
            dos.writeByte(quality);
            dos.writeByte(part);
            dos.writeShort(durability);
            dos.writeShort(currentDurability);
            dos.writeInt(price);
            if(binded){
                bindType |= 0x80;
            }
            dos.writeByte(bindType);
            dos.writeByte(times);
            int[][] pros = getProperties();
            dos.writeByte(pros.length);
            for (int j = 0; j < pros.length; j++) {
                byte pro = (byte) pros[j][0];
                short value = (short) pros[j][1];
                dos.writeByte(pro);
                dos.writeShort(value);
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    public byte[] toDbBytes(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(itemId);
            dos.writeInt(id);
            dos.writeInt(0);
            dos.writeBoolean(binded);
            dos.writeShort(currentDurability);
//            dos.writeLong(0);
            dos.write(enhances.size());
            for(int i=0;i<enhances.size();i++){
                dos.write(enhances.get(i).getProperty());
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }
}
