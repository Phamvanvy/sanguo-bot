package com.pip.itimes.server.stage;


import java.util.Random;
import java.util.List;

import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class FallCalculator {

    public static FallResult getFallItems(PlayerData player, Monster[] monster) {
        FallResult ret = new FallResult();
        Random rnd = new Random();
        //if(player.getMaxLevel()>player.getLevel())
            ret.addExp(calcExp(player,monster));
        Buf buf = player.getBuf(Buf.EXP_MONEY);
        if(buf!=null){
            ret.setExp(ret.getExp()*(buf.getValue()+100)/100);
        }else{
            if (player.isLuckyTime()) {
                ret.setExp(ret.getExp() * 125 / 100);
            }
        }
        for (int i = 0; i < monster.length; i++) {
            FallItem[] fallItems = monster[i].getFallItems();
            for (int j = 0; j < fallItems.length; j++) {
                calc(player, ret, fallItems[j],rnd);
            }
        }
        return ret;
    }

    public static FallResult[] getFallItems(PlayerData[] players, Monster[] monsters,
                                       int num,int avgLevel,List l) {
        FallResult[] ret = new FallResult[players.length];
        Random rnd = new Random();
        for(int i=0;i<players.length;i++){
            FallResult changed = new FallResult();
            //if(players[i].getMaxLevel()>players[i].getLevel()){
                int exp = calcExp(players[i], monsters);
                exp = adjustExp(exp, num, players[i].getLevel(), avgLevel);
                changed.addExp(exp);
            //}
            Buf buf = players[i].getBuf(Buf.EXP_MONEY);
            if(buf!=null){
                changed.setExp(changed.getExp()*(buf.getValue()+100)/100);
            }else{
                if (players[i].isLuckyTime()) {
                    changed.setExp(changed.getExp() * 125 / 100);
                }
            }
            ret[i] = changed;
        }
        for(int i=0;i<monsters.length;i++){
            FallItem[] fallItems = monsters[i].getFallItems();
            calc(players,ret,fallItems,rnd,l);
        }
        return ret;
    }

    public static int adjustExp(int exp,int num,int level,int avgLevel){
        if(num==1)
            return exp;
        if(num==2)
            return exp+exp*20/100;
        if(num==3)
            return exp+exp*30/100;
        else
            return exp;
    }

    public static void main(String[] args){
        System.out.println(adjustExp(400,3,38,50/3));
    }

    private static void calc(PlayerData[] players,FallResult[] ret,FallItem[] fallItems,Random rnd,List l){
        int money = 0;
        for(int i=0;i<fallItems.length;i++){
            FallItem fallItem = fallItems[i];
            byte type = fallItem.getType();
            if(type==0||type==2){
                int chance = fallItem.getChance();
                if(fallItem.getDropType() == 1){
                	for(int p=0; p<players.length; p++){
	                	if(hit(rnd,chance)){
		                    int count = getCount(rnd, fallItem.getMin(),
		                                         fallItem.getMax());
		                    if (count > 0) {
		                        ret[p].addItem(fallItem.getId(), count);
		                    }
		                }
                	}
                }else{
	                if(hit(rnd,chance)){
	                    int count = getCount(rnd, fallItem.getMin(),
	                                         fallItem.getMax());
	                    if (count > 0) {
	                        int index = rnd.nextInt(ret.length);
	                        ret[index].addItem(fallItem.getId(), count);
	                    }
	                }
                }
            }
            else if (type == 1) {
                TaskItem taskItem = (TaskItem) (Items.getTemplate(fallItem.getId())).newInstance();
                for(int j=0;j<players.length;j++){
                    PlayerData player = players[j];
                    if (player.hasTask(taskItem.getTaskId())||taskItem.getTaskId()==-1) {
                        int chance = fallItem.getChance();
                        if (hit(rnd, chance)) {
                            int count = getCount(rnd, fallItem.getMin(),
                                                 fallItem.getMax());
                            if (count > 0) {
                                ret[j].addItem(fallItem.getId(), count);
                            }
                        }
                    }
                }
            }
            else if (type == 3) {
                int chance = fallItem.getChance();
                if(fallItem.getDropType() == 1){
                	for(int p=0; p<players.length; p++){
	                	if (hit(rnd, chance)) {
		                    EquipmentTemplate template = (EquipmentTemplate) Items.
		                                                 getTemplate(fallItem.getId());
	                        ret[p].addItem(fallItem.getId(), 1);
		                }
                	}
                }else{
	                if (hit(rnd, chance)) {
	                    EquipmentTemplate template = (EquipmentTemplate) Items.
	                                                 getTemplate(fallItem.getId());
	                    if(template.getQuality()<2){
	                        int index = rnd.nextInt(ret.length);
	                        ret[index].addItem(fallItem.getId(), 1);
	                    }else{
	                        l.add(template.newInstance());
	                    }
	                }
                }
            }
            else if(type==5){
                int count = getCount(rnd,fallItem.getMin(),fallItem.getMax());
                if(fallItem.getDropType() == 1){
                	money += count * players.length;
                }else{
                	money += count;
                }
            }
            else if(type==8){
                DropGroup group = DropGroups.getDropGroup(fallItem.getId(),50);
                if(group != null){
	                int chance = fallItem.getChance();
	                if(fallItem.getDropType() == 1){
	                	for(int p=0; p<players.length; p++){
		                	if(hit(rnd,chance)){
			                    int rate = rnd.nextInt(group.getRate());
			                    DropItem dropItem = group.calcDropItem(rate);
			                    int count = getCount(rnd,dropItem.getMin(),dropItem.getMax());
			                    IItemTemplate template = dropItem.getItem();
			                    if(template.getType()!=IItem.TYPE_EQU){
			                        ret[p].addItem(dropItem.getItem(), count);
			                    }else{
			                        EquipmentTemplate t = (EquipmentTemplate)template;
			                        ret[p].addItem(dropItem.getItem(), count);
			                    }
			                }
	                	}
	                }else{
		                if(hit(rnd,chance)){
		                    int rate = rnd.nextInt(group.getRate());
		                    DropItem dropItem = group.calcDropItem(rate);
		                    int count = getCount(rnd,dropItem.getMin(),dropItem.getMax());
		                    IItemTemplate template = dropItem.getItem();
		                    if(template.getType()!=IItem.TYPE_EQU){
		                        int index = rnd.nextInt(ret.length);
		                        ret[index].addItem(dropItem.getItem(), count);
		                    }else{
		                        EquipmentTemplate t = (EquipmentTemplate)template;
		                        if(t.getQuality()<2){
		                            int index = rnd.nextInt(ret.length);
		                            ret[index].addItem(dropItem.getItem(), count);
		                        }else{
		                            l.add(t.newInstance());
		                        }
		                    }
		                }
	                }
                }
            }
        }
        if (money > 0) {
            for (int i = 0; i < players.length; i++) {
                ret[i].addMoney(money/players.length);
                Buf buf = players[i].getBuf(Buf.EXP_MONEY);
                if(buf!=null){
                    ret[i].setMoney(ret[i].getMoney()*(buf.getValue()+100)/100);
                }else{
                    if (players[i].isLuckyTime()) {
                        ret[i].setMoney(ret[i].getMoney() * 120 / 100);
                    }
                }
            }
        }
    }


    private static void calc(PlayerData player,FallResult ret,FallItem fallItem,Random rnd){
        byte type = fallItem.getType();
        if(type==0||type==2){ //basicItem //extendedItem
            int chance = fallItem.getChance();
            if(hit(rnd,chance)){
                int count = getCount(rnd,fallItem.getMin(),fallItem.getMax());
                if(count>0){
                    ret.addItem(fallItem.getId(),count);
                }
            }
        }
        else if(type==1){ //taskItem
            TaskItem taskItem = (TaskItem)(Items.getTemplate(fallItem.getId())).newInstance();
            if(player.hasTask(taskItem.getTaskId())||taskItem.getTaskId()==-1){
                int chance = fallItem.getChance();
                if(hit(rnd,chance)){
                    int count = getCount(rnd,fallItem.getMin(),fallItem.getMax());
                    if(count>0){
                        ret.addItem(fallItem.getId(),count);
                    }
                }
            }
        }
        else if(type==3){ //equipment
            int chance = fallItem.getChance();
            if(hit(rnd,chance)){
                ret.addItem(fallItem.getId(),1);
            }
        }
        else if(type==4){ //pet

        }
        else if(type==5){ //money
            int count = getCount(rnd,fallItem.getMin(),fallItem.getMax());
            ret.addMoney(count);
            Buf buf = player.getBuf(Buf.EXP_MONEY);
            if(buf!=null){
                ret.setMoney(ret.getMoney()*(buf.getValue()+100)/100);
            }else{
                if (player.isLuckyTime()) {
                    ret.setMoney(ret.getMoney() * 120 / 100);
                }
            }
        }
        else if (type == 8) {
            DropGroup group = DropGroups.getDropGroup(fallItem.getId(),50);
            if(group != null){
	            int chance = fallItem.getChance();
	            if (hit(rnd, chance)) {
	                int rate = rnd.nextInt(group.getRate());
	                DropItem dropItem = group.calcDropItem(rate);
	                int count = getCount(rnd, dropItem.getMin(), dropItem.getMax());
	                ret.addItem(dropItem.getItem(), count);
	            }
            }
        }
    }
    private static boolean hit(Random rnd,int chance){
        int r = rnd.nextInt(1000000);
        if(r<=chance) return true;
        return false;
    }

    private static int getCount(Random rnd,int min,int max){
        return rnd.nextInt(max-min+1)+min;
    }

    private static int calcExp(PlayerData player,Monster[] monsters){

        int total = 0;
        for(int i=0;i<monsters.length;i++){
            int lv = player.getLevel()-monsters[i].getLevel();
            total += ((monsters[i].getExp()*8)/((lv*lv)+8));
        }
        return total;
    }

}
