package com.pip.itimes.utils;

import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.utils.award.PropertyAward;
import com.pip.itimes.utils.award.EmptyAward;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.utils.award.ItemAward;
import com.pip.itimes.utils.award.UnknowAward;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.List;
import java.util.ArrayList;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.utils.award.PetAward;
import java.io.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Utils {
    public static byte[] getBytes(String hexString){
        int len = hexString.length()/3;
        byte[] ret = new byte[len];
        for(int i=0;i<len;i++){
            char hi = hexString.charAt(i*3);
            char low = hexString.charAt(i*3+1);
            int nHi = Character.digit(hi,16);
            int nLow = Character.digit(low,16);
            ret[i] = (byte)((nHi<<4)+nLow);
        }
        return ret;
    }

    public static int getNumber(byte[] buf, int off, int len) {
        int l = 0;
        for (int i = 0; i < len; i++) {
            l <<= 8;
            l += ((int) buf[off + i]) & 0xff;
        }
        return l;
    }

    public static IAward getAwardFromAttachmentString(String sAtt){
        IAward award = null;
        if("null".equals(sAtt)||"empty".equals(sAtt)){
            award = new EmptyAward();
        }
        byte[] attachment = Utils.getBytes(sAtt);
        if(attachment[0]==8){ //Ç®
            award = new PropertyAward(PropertyAward.MONEY,Utils.getNumber(attachment,1,4));
        }else{
            int itemId = Utils.getNumber(attachment,1,4);
            int count = Utils.getNumber(attachment,5,8);
            IItemTemplate item = Items.getTemplate(itemId);
            if(item!=null)
                award = new ItemAward(item,count);
            else
                award = new UnknowAward(attachment);

        }
        return award;
    }

    public static IAward getAwardFromItemString(String sItem){
        byte[] itemBytes = getBytes(sItem);
        int itemId = (int)Utils.getNumber(itemBytes,0,4);
        IItemTemplate item = Items.getTemplate(itemId);
        return new ItemAward(item,0);
    }

    public static IAward[] getAwardsFromChanged(String sChanged){
        byte[] bytes = getBytes(sChanged);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bis);
        List l = new ArrayList();
        try {
            while (true) {
                byte type = dis.readByte();
                int size = dis.readByte();
                if (type == 1) { //ÊôÐÔ
                    for (int i = 0; i < size; i++) {
                        byte pro = dis.readByte();
                        if (pro == Changed.CREDIT_STRING ||
                            pro == Changed.TITLE_STRING ||
                            pro == Changed.PET_NAME) {
                            String value = dis.readUTF();
                        }
                        else {
                            int value = dis.readInt();
                            PropertyAward award = new PropertyAward(pro, value);
                            l.add(award);
                        }
                    }
                }
                else if (type == 2) { //basicitem
                    for (int i = 0; i < size; i++) {
                        int itemId = dis.readByte();
                        int count = dis.readByte();
                        IItemTemplate item = Items.getTemplate(itemId);
                        ItemAward award = new ItemAward(item, count);
                        l.add(award);
                    }
                }
                else if (type == 3) {
                    for (int i = 0; i < size; i++) {
                        String name = dis.readUTF();
                        int count = dis.readByte();
                        IItemTemplate item = Items.getTaskTemplate(name);
                        ItemAward award = new ItemAward(item, count);
                        l.add(award);
                    }
                }
                else if (type == 4) {
                    for (int i = 0; i < size; i++) {
                        int itemId = dis.readInt();
                        int price = dis.readInt();
                        String name = dis.readUTF();
                        byte binded = dis.readByte();
                        int count = dis.readByte();
                        IItemTemplate item = Items.getTemplate(itemId);
                        ItemAward award = new ItemAward(item, count);
                        l.add(award);
                    }
                }
                else if (type == 5) {
                    for (int i = 0; i < size; i++) {
                        int itemId = dis.readInt();
                        int id = dis.readInt();
                        String name = dis.readUTF();
                        dis.skip(14);
                        int petCount = dis.readByte();
                        dis.skip(petCount * 3);
                        IItemTemplate item = Items.getTemplate(itemId);
                        ItemAward award = new ItemAward(item, 1);
                        l.add(award);
                    }
                }
                else if (type == 7) {
                    for (int i = 0; i < size; i++) {
                        int itemId = dis.readInt();
                        int id = dis.readInt();
                        String name = dis.readUTF();
                        dis.skip(14);
                        int petCount = dis.readByte();
                        dis.skip(petCount * 3);
                        IItemTemplate item = Items.getTemplate(itemId);
                        ItemAward award = new ItemAward(item, -1);
                        l.add(award);
                    }
                }
                else if (type == 6) { // buf
                    dis.skip(size * 9);
                }
                else if (type == 8) { //pet
                    for (int i = 0; i < size; i++) {
                        Pet pet = new Pet();
                        int itemId = dis.readInt();
                        pet.setItemId(itemId);
                        int id = dis.readInt();
                        pet.setId(id);
                        String name = dis.readUTF();
                        pet.setName(name);
                        byte petType = dis.readByte();
                        pet.setPetType(petType);
                        boolean baby = dis.readBoolean();
                        pet.setBaby(baby);
                        short level = dis.readShort();
                        pet.setLevel(level);
                        int exp = dis.readInt();
                        pet.setExp(exp);
                        short currentPoint = dis.readShort();
                        pet.setCurrentPoint(currentPoint);
                        short point = dis.readShort();
                        pet.setPoint(point);
                        byte favorite = dis.readByte();
                        pet.setFavor(favorite);
                        short strength = dis.readShort();
                        pet.setStrength(strength);
                        short agility = dis.readShort();
                        pet.setAgility(agility);
                        short vitality = dis.readShort();
                        pet.setVitality(vitality);
                        short intelligence = dis.readShort();
                        pet.setIntelligence(intelligence);
                        int hp = dis.readInt();
                        pet.setHp(hp);
                        int mp = dis.readInt();
                        pet.setMp(mp);
                        byte len = dis.readByte();
                        for (int j = 0; j < len; j++) {
                            pet.addAbility(Ability.getAbility(dis.readShort()));
                        }
                        IAward award = new PetAward(pet, 1);
                        l.add(award);
                    }
                }
                else if (type == 9) { //pet
                    for (int i = 0; i < size; i++) {
                        Pet pet = new Pet();
                        int itemId = dis.readInt();
                        pet.setItemId(itemId);
                        int id = dis.readInt();
                        pet.setId(id);
                        String name = dis.readUTF();
                        pet.setName(name);
                        byte petType = dis.readByte();
                        pet.setPetType(petType);
                        boolean baby = dis.readBoolean();
                        pet.setBaby(baby);
                        short level = dis.readShort();
                        pet.setLevel(level);
                        int exp = dis.readInt();
                        pet.setExp(exp);
                        short currentPoint = dis.readShort();
                        pet.setCurrentPoint(currentPoint);
                        short point = dis.readShort();
                        pet.setPoint(point);
                        byte favorite = dis.readByte();
                        pet.setFavor(favorite);
                        short strength = dis.readShort();
                        pet.setStrength(strength);
                        short agility = dis.readShort();
                        pet.setAgility(agility);
                        short vitality = dis.readShort();
                        pet.setVitality(vitality);
                        short intelligence = dis.readShort();
                        pet.setIntelligence(intelligence);
                        int hp = dis.readInt();
                        pet.setHp(hp);
                        int mp = dis.readInt();
                        pet.setMp(mp);
                        byte len = dis.readByte();
                        for (int j = 0; j < len; j++) {
                            pet.addAbility(Ability.getAbility(dis.readShort()));
                        }
                        IAward award = new PetAward(pet, -1);
                        l.add(award);
                    }
                }
                else if (type == 10) {
                    dis.skip(size * 10);
                }

            }
        }
        catch (IOException ex) {
//            ex.printStackTrace();
        }
        IAward[] awards = new IAward[l.size()];
        l.toArray(awards);
        return awards;
    }

    public static void main(String[] args){
        System.out.println(getBytes("43 ")[0]);
    }
}
