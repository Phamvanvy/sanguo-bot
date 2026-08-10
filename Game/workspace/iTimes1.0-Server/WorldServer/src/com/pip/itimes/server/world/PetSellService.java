package com.pip.itimes.server.world;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class PetSellService implements Runnable{

    private Map id2sells = new HashMap();
    private int id = 0;
    private Set petids = new HashSet();

    public PetSellService() {
        new Thread(this).start();
    }

    public PetSell createPetSell(int srcId,int petId,int destId,int money, String petName) throws PetSellException{
        synchronized(this){
            if(petids.contains(new Integer(petId))){
                throw new PetSellException("宠物正在交易中");
            }
            PetSell ret = new PetSell(++id,srcId,destId,petId,money,System.currentTimeMillis(), petName);
            id2sells.put(new Integer(ret.getId()),ret);
            return ret;
        }
    }
    
    public PetSell getSellPet(int id, int destId){
    	synchronized(this){
            PetSell ret = (PetSell)id2sells.get(new Integer(id));
            if(ret!=null&&ret.getDestId()==destId){
                return ret;
            }
            return null;
        }
    }

    public PetSell release(int id,int destId){
        synchronized(this){
            PetSell ret = (PetSell)id2sells.get(new Integer(id));
            if(ret!=null&&ret.getDestId()==destId){
                petids.remove(new Integer(ret.getPetId()));
                return ret;
            }
            return null;
        }
    }

    public void run(){
        while(true){
            try {
                Thread.sleep(20000L);
            } catch (InterruptedException ex) {
            }
            checkTimeOut();
        }
    }

    public void checkTimeOut(){
        synchronized(this){
            Iterator ite = id2sells.values().iterator();
            long current = System.currentTimeMillis();
            while(ite.hasNext()){
                PetSell sell = (PetSell)ite.next();
                if(sell.getSellTime()+60000L<=current){
                    ite.remove();
                    petids.remove(new Integer(sell.getPetId()));
                }
            }
        }
    }

}
