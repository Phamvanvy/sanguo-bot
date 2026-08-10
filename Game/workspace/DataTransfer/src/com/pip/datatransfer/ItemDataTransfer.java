package com.pip.datatransfer;


import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.datatransfer.bean.Acution;
import com.pip.datatransfer.bean.House;
import com.pip.datatransfer.bean.Mail;
import com.pip.datatransfer.bean.Shop;
import com.pip.datatransfer.bean.UserData;
import com.pip.datatransfer.dao.AcutionDAO;
import com.pip.datatransfer.dao.HouseDAO;
import com.pip.datatransfer.dao.MailDAO;
import com.pip.datatransfer.dao.ShopDAO;
import com.pip.datatransfer.dao.UserDataDAO;
import com.pip.db.hibernateDAO.HibernateUtil;


public class ItemDataTransfer{
    private static Logger log = Logger.getLogger(ItemDataTransfer.class);

    public static void main(String[] args){
        log.info("Transfered begin");
        
        AcutionDAO acutionDAO = new AcutionDAO();
        HouseDAO houseDAO = new HouseDAO();
        MailDAO mailDAO = new MailDAO();
        ShopDAO shopDAO = new ShopDAO();
        UserDataDAO userDataDAO = new UserDataDAO();

        SessionFactory session = HibernateUtil.getSessionFactory();
        
        Transaction tx = session.getCurrentSession().beginTransaction();
        List acutionIds = acutionDAO.getAllId();
        tx.commit();

        log.info(acutionIds.size() + " acutions need to be transfered");
        int acutionRealCount = 0;
        
        for(int i = 0; i < acutionIds.size(); i++){
            try{
                int acutionId = (Integer)acutionIds.get(i);

                tx = session.getCurrentSession().beginTransaction();

                Acution acution = acutionDAO.getAcutionById(acutionId);
                transAcution(acution);

                acutionDAO.update(acution);
                
                tx.commit();
                
                acutionRealCount++;
                log.info("ACUTION[" + acution.getId() + "] transfered OK");
            }catch(Exception e){
                log.info(e, e);
            }
        }
        
        log.info("[" + acutionRealCount + "/" + acutionIds.size() + "]Acutions prcessed");
        log.info("Acution Transfer completed successful");
        
        tx = session.getCurrentSession().beginTransaction();
        List houseIds = houseDAO.getAllId();
        tx.commit();

        log.info(houseIds.size() + " houses need to be transfered");
        int houseRealCount = 0;
        
        for(int i = 0; i < houseIds.size(); i++){
            try{
                int houseId = (Integer)houseIds.get(i);

                tx = session.getCurrentSession().beginTransaction();

                House house = houseDAO.getHouseById(houseId);
                transHouse(house);

                houseDAO.update(house);
                
                tx.commit();
                
                houseRealCount++;
                log.info("HOUSE[" + house.getId() + "] transfered OK");
            }catch(Exception e){
                log.info(e, e);
            }
        }
        
        log.info("[" + houseRealCount + "/" + houseIds.size() + "]Houses prcessed");
        log.info("House Transfer completed successful");
        
        tx = session.getCurrentSession().beginTransaction();
        List mailIds = mailDAO.getAllId();
        tx.commit();

        log.info(mailIds.size() + " mails need to be transfered");
        int mailRealCount = 0;
        
        for(int i = 0; i < mailIds.size(); i++){
            try{
                int mailId = (Integer)mailIds.get(i);

                tx = session.getCurrentSession().beginTransaction();

                Mail mail = mailDAO.getMailById(mailId);
                transMail(mail);

                mailDAO.update(mail);
                
                tx.commit();
                
                mailRealCount++;
                log.info("MAIL[" + mail.getId() + "] transfered OK");
            }catch(Exception e){
                log.info(e, e);
            }
        }
        
        log.info("[" + mailRealCount + "/" + mailIds.size() + "]Mails prcessed");
        log.info("Mail Transfer completed successful");
        
        tx = session.getCurrentSession().beginTransaction();
        List shopIds = shopDAO.getAllId();
        tx.commit();

        log.info(shopIds.size() + " shops need to be transfered");
        int shopRealCount = 0;
        
        for(int i = 0; i < shopIds.size(); i++){
            try{
                int shopId = (Integer)shopIds.get(i);

                tx = session.getCurrentSession().beginTransaction();

                Shop shop = shopDAO.getShopById(shopId);
                transShop(shop);

                shopDAO.update(shop);
                
                tx.commit();
                
                shopRealCount++;
                log.info("SHOP[" + shop.getId() + "] transfered OK");
            }catch(Exception e){
                log.info(e, e);
            }
        }
        
        log.info("[" + shopRealCount + "/" + shopIds.size() + "]Shops prcessed");
        log.info("Shop Transfer completed successful");
        
        tx = session.getCurrentSession().beginTransaction();
        List userDataIds = userDataDAO.getAllId();
        tx.commit();

        log.info(userDataIds.size() + " userDatas need to be transfered");
        int userDataRealCount = 0;
        
        for(int i = 0; i < userDataIds.size(); i++){
            try{
                int userDataId = (Integer)userDataIds.get(i);

                tx = session.getCurrentSession().beginTransaction();

                UserData userData = userDataDAO.getUserDataById(userDataId);
                transUserData(userData);

                userDataDAO.update(userData);
                
                tx.commit();
                
                userDataRealCount++;
                log.info("USERDATA[" + userData.getId() + "] transfered OK");
            }catch(Exception e){
                log.info(e, e);
            }
        }
        
        log.info("[" + userDataRealCount + "/" + userDataIds.size() + "]UserDatas prcessed");
        log.info("UserData Transfer completed successful");
    }
    
    private static void transAcution(Acution acution){
        byte[] data = acution.getItem();
        
        if(data != null && data.length > 0){
            if(data[0] == 0x01){
                if(data[1] == 0x01){
                    //log.info("Doubt Acution " + Arrays.toString(data));
                    byte[] newData = new byte[data.length - 1];
                    System.arraycopy(data, 2, newData, 1, data.length - 2);
                    newData[0] = 0x01;
                    acution.setItem(newData);
                }
            }else{
                byte[] newData = new byte[data.length + 1];
                System.arraycopy(data, 0, newData, 1, data.length);
                newData[0] = 0x01;
                acution.setItem(newData);
            }
        }
    }
    
    private static void transHouse(House house){
        byte[] data = house.getItems();
        
        if(data != null && data.length > 0){
            if(data[0] == 0x01){
                if(data[1] == 0x01){
                    //log.info("Doubt House " + Arrays.toString(data));
                    byte[] newData = new byte[data.length - 1];
                    System.arraycopy(data, 2, newData, 1, data.length - 2);
                    newData[0] = 0x01;
                    house.setItems(newData);
                }
            }else{
                byte[] newData = new byte[data.length + 1];
                System.arraycopy(data, 0, newData, 1, data.length);
                newData[0] = 0x01;
                house.setItems(newData);
            }
        }
    }
    
    private static void transMail(Mail mail){
        byte[] data = mail.getAttachment();
        
        if(data != null && data.length > 0){
            if(data[0] == 0x01){
                if(data[1] == 0x01){
                    //log.info("Doubt Mail " + Arrays.toString(data));
                    byte[] newData = new byte[data.length - 1];
                    System.arraycopy(data, 2, newData, 1, data.length - 2);
                    newData[0] = 0x01;
                    mail.setAttachment(newData);
                }
            }else{
                byte[] newData = new byte[data.length + 1];
                System.arraycopy(data, 0, newData, 1, data.length);
                newData[0] = 0x01;
                mail.setAttachment(newData);
            }
        }
    }
    
    private static void transShop(Shop shop){
        byte[] data = shop.getItems();
        
        if(data != null && data.length > 0){
            if(data[0] == 0x01){
                if(data[1] == 0x01){
                    //log.info("Doubt Shop " + Arrays.toString(data));
                    byte[] newData = new byte[data.length - 1];
                    System.arraycopy(data, 2, newData, 1, data.length - 2);
                    newData[0] = 0x01;
                    shop.setItems(newData);
                }
            }else{
                byte[] newData = new byte[data.length + 1];
                System.arraycopy(data, 0, newData, 1, data.length);
                newData[0] = 0x01;
                shop.setItems(newData);
            }
        }
    }
    
    private static void transUserData(UserData userData){
        byte[] dataEquip = userData.getEquipments();
        byte[] dataUsedEquip = userData.getUsedequipments();
        
        if(dataEquip != null && dataEquip.length > 0){
            if(dataEquip[0] == 0x01){
                if(dataEquip[1] == 0x01){
                    //log.info("Doubt Equip " + Arrays.toString(dataEquip));
                    byte[] newData = new byte[dataEquip.length - 1];
                    System.arraycopy(dataEquip, 2, newData, 1, dataEquip.length - 2);
                    newData[0] = 0x01;
                    userData.setEquipments(newData);
                }
            }else{
                byte[] newData = new byte[dataEquip.length + 1];
                System.arraycopy(dataEquip, 0, newData, 1, dataEquip.length);
                newData[0] = 0x01;
                userData.setEquipments(newData);
            }
        }
        
        if(dataUsedEquip != null && dataUsedEquip.length > 0){
            if(dataUsedEquip[0] == 0x01){
                if(dataUsedEquip[1] == 0x01){
                    //log.info("Doubt UsedEquip " + Arrays.toString(dataUsedEquip));
                    byte[] newData = new byte[dataUsedEquip.length - 1];
                    System.arraycopy(dataUsedEquip, 2, newData, 1, dataUsedEquip.length - 2);
                    newData[0] = 0x01;
                    userData.setUsedequipments(newData);
                }
            }else{
                byte[] newData = new byte[dataUsedEquip.length + 1];
                System.arraycopy(dataUsedEquip, 0, newData, 1, dataUsedEquip.length);
                newData[0] = 0x01;
                userData.setUsedequipments(newData);
            }
        }
    }
}
