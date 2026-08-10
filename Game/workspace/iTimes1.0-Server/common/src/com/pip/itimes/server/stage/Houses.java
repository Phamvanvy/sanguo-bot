package com.pip.itimes.server.stage;

import java.util.Collection;
import java.util.TreeMap;

public class Houses {

    public static final TreeMap<Integer,Styles> houses = new TreeMap<Integer,Styles>();
    public static final TreeMap<Integer,HouseWaiter> waiters = new TreeMap<Integer,HouseWaiter>();

    public static void clear(){
        houses.clear();
    }

    public static void addHouseTemplate(HouseTemplate house){
        Styles styles = houses.get(house.getLevel());
        if(styles==null){
            styles = new Styles(house.getLevel(),house.getPrice(),house.getDesc(),house.getConsumeCode());
            houses.put(house.getLevel(),styles);
        }
        styles.addHouse(house);
    }

    public static HouseTemplate getHouseTemplate(int level,int style){
        Styles styles = houses.get(level);
        if(styles!=null){
            return styles.getHouse(style);
        }
        return null;
    }

    public static Collection<HouseTemplate> getHouseTemplates(int level){
        Styles styles = houses.get(level);
        if(styles!=null){
            return styles.getHouses();
        }
        return null;
    }

    public static Collection<Styles> getStyles(){
        return houses.values();
    }

    public static Collection<HouseWaiter> getWaiters(){
        return waiters.values();
    }

    public static HouseWaiter getWaiter(int id){
        return waiters.get(id);
    }

    public static void addWaiter(HouseWaiter waiter){
        waiters.put(waiter.getId(),waiter);
    }


    public static class Styles{

        private int level;
        private String desc;
        private int price;
        
        // 卓望版本消费代码
        private String consumeCode;

        private TreeMap<Integer,HouseTemplate> styles = new TreeMap<Integer,HouseTemplate>();

        public Styles(int level,int price,String desc, String consumeCode){
            this.level = level;
            this.desc = desc;
            this.price = price;
            this.consumeCode = consumeCode;
        }
        
        public String getConsumeCode() {
            return consumeCode;
        }


        public void setConsumeCode(String consumeCode) {
            this.consumeCode = consumeCode;
        }

        public  int getLevel(){
            return level;
        }

        public String getDesc(){
            return desc;
        }

        public Collection<HouseTemplate> getHouses(){
            return styles.values();
        }

        public void addHouse(HouseTemplate house){
            styles.put(house.getStyle(),house);
        }

        public HouseTemplate getHouse(int style){
            return styles.get(style);
        }

        public int getPrice(){
            return price;
        }
    }
}
