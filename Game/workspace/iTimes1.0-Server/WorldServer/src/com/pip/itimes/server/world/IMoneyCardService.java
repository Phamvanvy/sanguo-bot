package com.pip.itimes.server.world;


import java.util.Date;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.pip.itimes.server.bean.IMoneyCard;
import com.pip.itimes.server.dao.IMoneyCardDao;

public class IMoneyCardService{
    private static final Logger log = Logger.getLogger(IMoneyCardService.class);
    
    private IMoneyCardDao iMoneyCardDao;
    private LinkedList<IMoneyCard> availableIMoneyCards;
    
    public static final int IMONEY_CARD_STATUS_NORMAL = 0;
    public static final int IMONEY_CARD_STATUS_PREUSE = 2;
    public static final int IMONEY_CARD_STATUS_USED = 1;
    
    public static final int IMONEY_CARD_ITEM_ID_PIP = 200989;
    public static final int IMONEY_CARD_ITEM_ID_QQ = 200990;
    
    protected static int[] IMONEY_CARD_AMOUNT = {360000, 100000};
    
    public IMoneyCardService(){
        iMoneyCardDao = new IMoneyCardDao();
        availableIMoneyCards = new LinkedList<IMoneyCard>();
        
        init();
    }
    
    private void init(){
        try{
            IMoneyCard[] tmpList = iMoneyCardDao.getAvailableIMoneyCardList();
            
            for(IMoneyCard iMoneyCard : tmpList){
                availableIMoneyCards.add(iMoneyCard);
            }
        }catch(Exception e){
            log.error(e, e);
        }
    }
    
    public void addIMoneyCard(WorldPlayer player, String cardno, String password, int amount) throws Exception{
        synchronized(availableIMoneyCards){
            IMoneyCard card = new IMoneyCard();
            
            card.setCreateaccountid(player.getAccountId());
            card.setCreateplayerid(player.getId());
            card.setCreatetime(new Date());
            card.setUseaccountid(-1);
            card.setUseplayerid(-1);
            card.setCardno(cardno);
            card.setPassword(password);
            card.setAmount(amount);
            card.setStatus(IMONEY_CARD_STATUS_NORMAL);
            
            availableIMoneyCards.add(card);
            iMoneyCardDao.addIMoneyCard(card);
        }
    }
    
    public IMoneyCard preUseIMoneyCard(WorldPlayer player) throws Exception{
        synchronized(availableIMoneyCards){
            IMoneyCard card = availableIMoneyCards.removeFirst();
            
            card.setUseaccountid(player.getAccountId());
            card.setUseplayerid(player.getId());
            card.setUsetime(new Date());
            card.setStatus(IMONEY_CARD_STATUS_PREUSE);
            
            iMoneyCardDao.updateIMoneyCard(card);
            
            return card;
        }
    }
    
    public void doUseIMoneyCard(IMoneyCard card) throws Exception{
        card.setStatus(IMONEY_CARD_STATUS_USED);
        
        iMoneyCardDao.updateIMoneyCard(card);
    }
    
    public boolean hasAvailableIMoneyCard(){
        synchronized(availableIMoneyCards){
            return availableIMoneyCards.size() > 0;
        }
    }
}
