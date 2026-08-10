package com.pip.datatransfer;


import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.datatransfer.bean.Account;
import com.pip.datatransfer.bean.Balance;
import com.pip.datatransfer.bean.GameAccount;
import com.pip.datatransfer.bean.OldAccount;
import com.pip.datatransfer.bean.Purchased;
import com.pip.datatransfer.dao.AccountDAO;
import com.pip.datatransfer.dao.GameAccountDAO;
import com.pip.datatransfer.dao.OldAccountDAO;
import com.pip.datatransfer.dao.PurchasedDAO;
import com.pip.db.hibernateDAO.HibernateUtil;


public class AccountTransfer{
    private static Logger log = Logger.getLogger(AccountTransfer.class);

    public static void main(String[] args){
        log.info("Transfered begin");
        
        OldAccountDAO oldAccountDao = new OldAccountDAO();
        AccountDAO accountDao = new AccountDAO();
        GameAccountDAO gameAccountDao = new GameAccountDAO();
        PurchasedDAO purchasedDao = new PurchasedDAO();

        SessionFactory session = HibernateUtil.getSessionFactory();
        Transaction tx = session.getCurrentSession().beginTransaction();

        List oldAccountIds = oldAccountDao.getAllId();
        tx.commit();

        log.info(oldAccountIds.size() + " accounts need to be transfered");
        int realcount = 0;
        
        for(int i = 0; i < oldAccountIds.size(); i++){
            try{
                int accountId = (Integer)oldAccountIds.get(i);

                tx = session.getCurrentSession().beginTransaction();

                OldAccount oldAccount = oldAccountDao.getAccountById(accountId);
                Account newAccount = transAccount(oldAccount);
                GameAccount gameAccount = transGameAccount(oldAccount);
                Purchased purchased = transPurchased(oldAccount);

                accountDao.create(newAccount);
                gameAccountDao.create(gameAccount);
                
                if(purchased != null){
                    purchasedDao.create(purchased);
                }

                tx.commit();
                
                realcount++;
                log.info("ACCOUNT[" + oldAccount.getId() + "][" + oldAccount.getUserName() + "] transfered OK");
            }catch(Exception e){
                log.info(e, e);
            }
        }
        
        log.info("[" + realcount + "/" + oldAccountIds.size() + "]accounts prcessed");
        log.info("Transfer completed successful");
    }

    private static Account transAccount(OldAccount oldAccount){
        Account newAccount = new Account();

        newAccount.setId(oldAccount.getId());
        newAccount.setName(oldAccount.getUserName());
        newAccount.setPassWord(oldAccount.getPassWord());
        newAccount.setBalance(new Balance(oldAccount.getIMoney(), 0));
        newAccount.setCreateTime(oldAccount.getCreateTime());
        newAccount.setStatus(oldAccount.getValid());
        
        String gameCode = oldAccount.getGameCode();
        
        if(gameCode == null){
            newAccount.setCreateGameCode("");
        }else{
            newAccount.setCreateGameCode(oldAccount.getGameCode());
        }

        String channel = oldAccount.getChannel();
        
        if(channel == null){
            newAccount.setServiceVersion("");
        }else{
            newAccount.setServiceVersion(oldAccount.getChannel());
        }

        newAccount.setModifyPasswordTimes(0);
        newAccount.setPhone(oldAccount.getPhone());

        return newAccount;
    }
    
    private static GameAccount transGameAccount(OldAccount oldAccount){
        GameAccount gameAccount = new GameAccount();
        
        gameAccount.setId(oldAccount.getId());
        gameAccount.setName(oldAccount.getUserName());
        gameAccount.setCreateTime(oldAccount.getCreateTime());
        gameAccount.setSubscribe(false);
        gameAccount.setMonthFee(oldAccount.getMonthFee());
        gameAccount.setMonthPay(0);
        gameAccount.setLastmonthpay(0);
        
        return gameAccount;
    }

    private static Purchased transPurchased(OldAccount oldAccount){
        if(oldAccount.getSubscribeStatus() != 0){
            Purchased purchased = new Purchased();

            purchased.setAccountId(oldAccount.getId());
            purchased.setCode(1);
            purchased.setStatus(1);

            String feePhone = oldAccount.getSubscribePhone();

            if(feePhone == null){
                purchased.setPhone("");
            }else{
                purchased.setPhone(feePhone);
            }

            purchased.setCreateTime(new Date());
            purchased.setFeeId(oldAccount.getSubscribeBill());

            return purchased;
        }else{
            return null;
        }
    }
}
