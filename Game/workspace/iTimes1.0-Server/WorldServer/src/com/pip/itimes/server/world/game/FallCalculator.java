package com.pip.itimes.server.world.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.Discount;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.MateService;
import com.pip.itimes.server.world.MasterService;

public class FallCalculator {

    private static MateService mateService;
    private static MasterService masterService;

    public static void setMateService(MateService service){
        mateService = service;
    }

    public static void setMasterService(MasterService service){
        masterService = service;
    }

    public static FallResult getFallItems(WorldPlayer player, Monster[] monster) {
        FallResult ret = new FallResult();
        Random rnd = new Random();
        //if(player.getMaxLevel()>player.getLevel())
            ret.addExp(calcExp(player,monster));
//        LuckyBuf buf = player.getLuckyBuf();
        ret.setExp(ret.getExp()*getExpRatio(player,true)/100);
//        if(buf!=null){
//            ret.setExp(ret.getExp()*buf.getExpRatio()/100);
//        }else{
//            if (player.isLuckyTime()) {
//                ret.setExp(ret.getExp() * 125 / 100);
//            }
//        }
        for (int i = 0; i < monster.length; i++) {
            //FallItem[] fallItems = monster[i].getFallItems();
        	FallItem[] fallItems = monster[i].getLocalFallItems();
            for (int j = 0; j < fallItems.length; j++) {
                calc(player, ret, fallItems[j],rnd);
            }
        }
        return ret;
    }

    public static FallResult[] getFallItems(WorldPlayer[] players, Monster[] monsters,
                                       int num,int avgLevel,List l) {
        FallResult[] ret = new FallResult[players.length];
        Random rnd = new Random();
        int[] expRatio = getExpRatio(players);
        for(int i=0;i<players.length;i++){
            FallResult changed = new FallResult();
            //if(players[i].getMaxLevel()>players[i].getLevel()){
                int exp = calcExp(players[i], monsters);
                //用于服务器战斗 附加可配置经验加成
                exp = adjustExp(exp, num, players[i].getLevel(), avgLevel);
                exp = exp * Discount.EXPADDPERCENT/100;
                changed.addExp(exp);
            //}
            changed.setExp(changed.getExp()*expRatio[i]/100);
//            LuckyBuf buf = players[i].getLuckyBuf();
//            if(buf!=null){
//                changed.setExp(changed.getExp()*buf.getExpRatio()/100);
//            }else{
//                if (players[i].isLuckyTime()) {
//                    changed.setExp(changed.getExp() * 125 / 100);
//                }
//            }
            ret[i] = changed;
        }
        for(int i=0;i<monsters.length;i++){
            FallItem[] fallItems = monsters[i].getFallItems();
            int tmpLen = fallItems.length;
            switch(players.length){
	        	case 1:
	        		fallItems[tmpLen - 1].setChance(50000);
	        		break;
	        	case 2:
	        		fallItems[tmpLen - 1].setChance(100000);
	        		break;
	        	case 3:
	        		fallItems[tmpLen - 1].setChance(200000);
	        		break;
	        }
            calc(players, ret, fallItems, rnd, l);
            //add holiday fall
            ArrayList<FallItem[]> lstfallItem = MonsterConstants.getHolidayWorldFall();
            for(int f=0; f<lstfallItem.size(); f++){
            	fallItems = monsters[i].getholidayFallItems(lstfallItem.get(f));
            	calc(players, ret, fallItems, rnd, l);
            }
//            fallItems = monsters[i].getholidayFallItems();
//            calc(players, ret, fallItems, rnd, l);
        }
        // 金钱的获得，
        int[] moneyRatio = getMoneyRatio(players);
        for(int i = 0; i < players.length; i++){
        	ret[i].setMoney(ret[i].getMoney()*moneyRatio[i]/100);
        }
        return ret;
    }

    public static int getExpRatio(WorldPlayer player,boolean addBuf){
        int max = 100;
        Buf buf = player.getBuf(Buf.EXP_MONEY);
        if(buf!=null){
            max = 100+buf.getValue();
        }
        if(player.isLuckyTime()){
            if(max<125)
                max = 125;
        }
        if(addBuf){
            Buf b = player.getBuf(Buf.EXP);
            if (b != null) {
                max += b.getValue();
            }
            b = player.getBuf(Buf.EXP_LOGIN);
            if (b != null){
            	max += b.getValue();
            }
            b = player.getBuf(buf.CAMP_EXP);		//阵营科技
            if (b != null){
            	max += b.getValue();
            }
        }
        return max;
    }

    public static int getMoneyRatio(WorldPlayer player){
        int max = 100;
        Buf buf = player.getBuf(Buf.EXP_MONEY);
        if(buf!=null){
            max = 100+buf.getValue();
        }
        if(player.isLuckyTime()){
            if(max<120)
                max = 120;
        }
        return max;
    }

    private static int getExpRatio(WorldPlayer p1,WorldPlayer p2){
        int avgFavorite = getAvgFavorite(p1,p2);
        boolean done = false;
        int current = 0;
        if(mateService.isMate(p1,p2)){
//            110+（双方好友度平均值）/1500）％，最高为130％；
            current = Math.min(110+(avgFavorite)/1500,130);
            done = true;
        }
        if(masterService.hasRelation(p1,p2)){
            //（120+（双方好友度平均值）/2000）％，最高为130％
            int ratio = Math.min(120+avgFavorite/2000,130);
            if(ratio>current)
                current = ratio;
            done = true;
        }
        if(!done){
            //100+双方好友度平均值/1500）％，最高为120％
            current = Math.min(100+avgFavorite/1500,120);
        }
        return current;
    }

    private static int getMoneyRatio(WorldPlayer p1, WorldPlayer p2) {
        int avgFavorite = getAvgFavorite(p1, p2);
        boolean done = false;
        int current = 0;
        if (mateService.isMate(p1, p2)) {
//            110+（双方好友度平均值）/1500）％，最高为130％；
            current = Math.min(110 + (avgFavorite) / 1500, 130);
            done = true;
        }
        if (masterService.hasRelation(p1, p2)) {
            //（120+（双方好友度平均值）/2000）％，最高为130％
            int ratio = Math.min(120 + avgFavorite / 2000, 130);
            if (ratio > current)
                current = ratio;
            done = true;
        }
        if (!done) {
            //100+双方好友度平均值/1500）％，最高为120％
            current = Math.min(100 + avgFavorite / 1500, 120);
        }
        return current;
    }


    public static int[] getExpRatio(WorldPlayer[] players){
        int[] ret = new int[players.length];
        for(int i=0;i<players.length;i++){
            ret[i] = getExpRatio(players[i],false);
        }
//        if(ret.length==1)
//            return ret;
        if(ret.length==2){
            int ratio = getExpRatio(players[0],players[1]);
            if(ratio>ret[0])
                ret[0] = ratio;
            if(ratio>ret[1])
                ret[1] = ratio;
//            if(mateService.isMate(players[0],players[1])){
//                for(int i=0;i<2;i++){
//                    if(ret[i]<120)
//                        ret[i] = 120;
//                }
//            }
//            if(masterService.hasRelation(players[0],players[1])){
//                for(int i=0;i<2;i++){
//                    if(ret[i]<130)
//                        ret[i] = 130;
//                }
//            }
        }
        if(ret.length==3){
            int ratio = getExpRatio(players[0],players[1]);
            ret[0] = Math.max(ratio,ret[0]);
            ret[1] = Math.max(ratio,ret[1]);
            ratio = getExpRatio(players[0],players[2]);
            ret[0] = Math.max(ratio,ret[0]);
            ret[2] = Math.max(ratio,ret[2]);
            ratio = getExpRatio(players[1],players[2]);
            ret[1] = Math.max(ratio,ret[1]);
            ret[2] = Math.max(ratio,ret[2]);
//            if(mateService.isMate(players[0],players[1])){
//                ret[0] = Math.max(ret[0],120);
//                ret[1] = Math.max(ret[1],120);
//            }
//            else if(mateService.isMate(players[0],players[2])){
//                ret[0] = Math.max(ret[0],120);
//                ret[2] = Math.max(ret[2],120);
//            }
//            else if(mateService.isMate(players[1],players[2])){
//                ret[1] = Math.max(ret[1],120);
//                ret[2] = Math.max(ret[2],120);
//            }
//            if(masterService.hasRelation(players[0],players[1])){
//                ret[0] = Math.max(ret[0],130);
//                ret[1] = Math.max(ret[1],130);
//            }
//            else if(masterService.hasRelation(players[0],players[2])){
//                ret[0] = Math.max(ret[0],130);
//                ret[2] = Math.max(ret[2],130);
//            }
//            else if(masterService.hasRelation(players[1],players[2])){
//                ret[1] = Math.max(ret[1],130);
//                ret[2] = Math.max(ret[2],130);
//            }
        }
        for(int i=0;i<players.length;i++){
            Buf buf = players[i].getBuf(Buf.EXP);
            if(buf!=null){
                ret[i] += buf.getValue();
            }
            buf = players[i].getBuf(Buf.EXP_LOGIN);
            if(buf!=null){
                ret[i] += buf.getValue();
            }
            buf = players[i].getBuf(buf.CAMP_EXP);		//阵营科技
            if (buf != null){
            	ret[i] += buf.getValue();
            }
        }
        return ret;
    }

    public static int getAvgFavorite(WorldPlayer p1,WorldPlayer p2){
        return (p1.getFriendFavorite(p2)+p2.getFriendFavorite(p1))/2;
    }

    public static int[] getMoneyRatio(WorldPlayer[] players){
        int[] ret = new int[players.length];
        for(int i=0;i<players.length;i++){
            ret[i] = getMoneyRatio(players[i]);
        }
        if(ret.length==1)
            return ret;
        if (ret.length == 2) {
            int ratio = getMoneyRatio(players[0], players[1]);
            if (ratio > ret[0])
                ret[0] = ratio;
            if (ratio > ret[1])
                ret[1] = ratio;
        }
        if(ret.length==3){
            int ratio = getMoneyRatio(players[0],players[1]);
            ret[0] = Math.max(ratio,ret[0]);
            ret[1] = Math.max(ratio,ret[1]);
            ratio = getExpRatio(players[0],players[2]);
            ret[0] = Math.max(ratio,ret[0]);
            ret[2] = Math.max(ratio,ret[2]);
            ratio = getExpRatio(players[1],players[2]);
            ret[1] = Math.max(ratio,ret[1]);
            ret[2] = Math.max(ratio,ret[2]);
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

    private static void calc(WorldPlayer[] players, FallResult[] ret, FallItem[] fallItems, Random rnd, List l){
        int money = 0;
        int minLevel = players[0].getLevel();
        for(int i=1;i<players.length;i++){
            if(players[i].getLevel()<minLevel)
                minLevel = players[i].getLevel();
        }
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
	                    	IItemTemplate template = (IItemTemplate) Items.getTemplate(fallItem.getId());
	                    	if (template.getQuality() < 2) {
	                    		int index = rnd.nextInt(ret.length);
	                            ret[index].addItem(fallItem.getId(), count);
	                    	} else {
	                    		l.add(template.newInstance());
	                    	}
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
                DropGroup group = DropGroups.getDropGroup(fallItem.getId(),minLevel);
                if(group!=null){
                    int chance = fallItem.getChance();
                    if(fallItem.getDropType() == 1){
                    	for(int p=0; p<players.length; p++){
                    		if (hit(rnd, chance)) {
    	                        int rate = rnd.nextInt(group.getRate());
    	                        DropItem dropItem = group.calcDropItem(rate);
    	                        int count = getCount(rnd, dropItem.getMin(), dropItem.getMax());
    	                        ret[p].addItem(dropItem.getItem(), count);
    	                    }
                    	}
                    }else{
	                    if (hit(rnd, chance)) {
	                        int rate = rnd.nextInt(group.getRate());
	                        DropItem dropItem = group.calcDropItem(rate);
	                        int count = getCount(rnd, dropItem.getMin(), dropItem.getMax());
	                        IItemTemplate template = dropItem.getItem();
	                        if (template.getQuality() < 2) {
	                        	int index = rnd.nextInt(ret.length);
	                            ret[index].addItem(dropItem.getItem(), count);
	                        } else {
	                        	l.add(template.newInstance());
	                        }
	                    }
                    }
                }
            }
        }
        if (money > 0) {
//            int[] moneyRatio = getMoneyRatio(players);
            for (int i = 0; i < players.length; i++) {
                ret[i].addMoney(money/players.length);
//                ret[i].setMoney(ret[i].getMoney()*moneyRatio[i]/100);
//                LuckyBuf buf = players[i].getLuckyBuf();
//                if(buf!=null){
//                    ret[i].setMoney(ret[i].getMoney()*buf.getMoneyRatio()/100);
//                }else{
//                    if (players[i].isLuckyTime()) {
//                        ret[i].setMoney(ret[i].getMoney() * 120 / 100);
//                    }
//                }
            }
        }
    }


    private static void calc(WorldPlayer player,FallResult ret,FallItem fallItem,Random rnd){
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
            ret.setMoney(ret.getMoney()*getMoneyRatio(player)/100);
//            LuckyBuf buf = player.getLuckyBuf();
//            if(buf!=null){
//                ret.setMoney(ret.getMoney()*buf.getMoneyRatio()/100);
//            }else{
//                if (player.isLuckyTime()) {
//                    ret.setMoney(ret.getMoney() * 120 / 100);
//                }
//            }
        }
        else if (type == 8) {
            DropGroup group = DropGroups.getDropGroup(fallItem.getId(),player.getLevel());
            if(group!=null){
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

    private static int calcExp(WorldPlayer player,Monster[] monsters){

        int total = 0;
        for(int i=0;i<monsters.length;i++){
            int lv = player.getLevel()-monsters[i].getLevel();
//            total += ((monsters[i].getExp()*8)/((lv*lv)+8));
            total += ((monsters[i].getExp()*8)/(Math.abs(lv)+8));
        }
        return total;
    }
}
