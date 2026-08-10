package com.pip.itimes.server.util;

import com.pip.itimes.server.dao.IDDao;
import com.pip.itimes.server.bean.IDBean;
import com.pip.itimes.server.dao.*;

/**
 * @author Jeffery
 * @version 1.0
 */
public class IDGenerator {
    public static final IDDao dao = new IDDao();
    private static int id = 1;
    private static int maxId = 0;
    private static int petId = 1;
    private static int petMaxId = 0;
    private static int accountName = 1;
    private static int accountMaxName = 0;

    private static Object equipmentLock = new Object();
    private static Object petLock = new Object();
    private static Object accountLock = new Object();

    public static String getAccountName(){
        synchronized(accountLock){
            if (accountName >= accountMaxName) {
                getNewAccountName();
                return "" + accountName++;
            } else {
                return "" + accountName++;
            }
        }
    }

    public static void getNewAccountName(){
        try {
            IDBean bean = dao.getAccountNameBean();
            int usedId = bean.getUsedId();
            bean.setUsedId(usedId + 20);
            dao.makePersistent(bean);
            accountName = usedId;
            accountMaxName = usedId + 20;
        } catch (DataAccessException ex) {
            ex.printStackTrace();
        }
    }

    public static int getEquipmentId() {
        synchronized(equipmentLock){
            if (id >= maxId) {
                getNewEquipmentId();
                return id++;
            } else {
                return id++;
            }
        }
    }

    public static int getPetId(){
        synchronized(petLock){
            if (petId >= petMaxId) {
                getNewPetId();
                return petId++;
            } else {
                return petId++;
            }
        }
    }

    private static void getNewEquipmentId(){
        try {
            IDBean bean = dao.getEquipmentIdBean();
            int usedId = bean.getUsedId();
            bean.setUsedId(usedId + 20);
            dao.makePersistent(bean);
            id = usedId;
            maxId = usedId + 20;
        } catch (DataAccessException ex) {
            ex.printStackTrace();
        }
    }

    private static void getNewPetId(){
        try {
            IDBean bean = dao.getPetIdBean();
            int usedId = bean.getUsedId();
            bean.setUsedId(usedId + 20);
            dao.makePersistent(bean);
            petId = usedId;
            petMaxId = usedId + 20;
        } catch (DataAccessException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args){
        for(int i=0;i<50;i++){
            System.out.println("id:"+getEquipmentId());
        }
    }
}
