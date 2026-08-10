package com.pip.datatransfer;


import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.datatransfer.bean.Petmanager;
import com.pip.datatransfer.bean.UserData;
import com.pip.datatransfer.dao.PetmanagerDAO;
import com.pip.datatransfer.dao.UserDataDAO;
import com.pip.db.hibernateDAO.HibernateUtil;


public class PetDataTransfer{
    private static Logger log = Logger.getLogger(PetDataTransfer.class);
    public static void main(String[] args){
        log.info("Pet Transfered begin");
        
        UserDataDAO userDataDAO = new UserDataDAO();
        PetmanagerDAO petmanagerDAO = new PetmanagerDAO();
        SessionFactory session = HibernateUtil.getSessionFactory();
        
        Transaction tx = session.getCurrentSession().beginTransaction();
        List playerIds = userDataDAO.getAllId();
        tx.commit();
        log.info(playerIds.size() + " userDatas need to be transfered");
        int playerRealCount = 0;
        
        for(int i = 0; i < playerIds.size(); i++){
            try{
                int playerId = (Integer)playerIds.get(i);

                tx = session.getCurrentSession().beginTransaction();

                UserData userData = userDataDAO.getUserDataById(playerId);
                transUserData(userData);

                userDataDAO.update(userData);
                
                tx.commit();
                
                playerRealCount++;
                log.info("UserData[" + userData.getId() + "] transfered OK");
            }catch(Exception e){
                log.info(e, e);
            }
        }
        
        log.info("[" + playerRealCount + "/" + playerIds.size() + "]userDatas prcessed");
        log.info("UserData Transfer completed successful");
        
        tx = session.getCurrentSession().beginTransaction();
        List petmanagerIds = petmanagerDAO.getAllId();
        tx.commit();

        log.info(petmanagerIds.size() + " petmanager need to be transfered");
        int petmanagerRealCount = 0;
        
        for(int i = 0; i < petmanagerIds.size(); i++){
            try{
                int petmanagerId = (Integer)petmanagerIds.get(i);

                tx = session.getCurrentSession().beginTransaction();

                Petmanager petmanager = petmanagerDAO.getPetmanagerById(petmanagerId);
                transPetmanager(petmanager);

                petmanagerDAO.update(petmanager);
                
                tx.commit();
                
                petmanagerRealCount++;
                log.info("PETMANAGER[" + petmanager.getId() + "] transfered OK");
            }catch(Exception e){
                log.info(e, e);
            }
        }
        log.info("[" + petmanagerRealCount + "/" + petmanagerIds.size() + "]petmanagers prcessed");
        log.info("petmanager Transfer completed successful");
        System.exit(1);
    }
    
    private static void transUserData(UserData userData){
        byte[] datapets = userData.getPets();
        
        if(datapets != null && datapets.length > 0){
        	byte[] datapets_new = new byte[datapets.length+1];
        	datapets_new[0]=1;
        	for (int i = 0; i < datapets.length; i++) {
        		datapets_new[i+1]=datapets[i];
        	}
         	userData.setPets(datapets_new);
        }
    }
    
    private static void transPetmanager(Petmanager petmanager){
        byte[] datapets = petmanager.getPet();
        
        if(datapets != null && datapets.length > 0){
        	byte[] datapets_new = new byte[datapets.length+1];
        	datapets_new[0]=1;
        	for (int i = 0; i < datapets.length; i++) {
        		datapets_new[i+1]=datapets[i];
        	}
        	petmanager.setPet(datapets_new);
        }
    }

}
