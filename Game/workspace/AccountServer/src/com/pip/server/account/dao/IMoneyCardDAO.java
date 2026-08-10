package com.pip.server.account.dao;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.Query;

import com.pip.db.hibernateDAO.GenericHibernateDAO;
import com.pip.server.account.bean.IMoneyCard;

public class IMoneyCardDAO extends GenericHibernateDAO<IMoneyCard,Integer> {
	private static AtomicInteger cardSerial = new AtomicInteger(1000);
	private static Random rand = new Random(System.currentTimeMillis());
	private static Object useLock = new Object();
	
	public void add(IMoneyCard card){
		getSession().save(card);
	}
	
	public void update(IMoneyCard card){
		getSession().update(card);
	}
	
	public IMoneyCard getIMoneyCard(int id){
		return (IMoneyCard)getSession().get(IMoneyCard.class, new Integer(id));
	}
	
	public IMoneyCard getIMoneyCard(String cardno) {
		String sql = "from IMoneyCard c where c.cardno=:cardno";
		Query query = getSession().createQuery(sql);
		query.setString("cardno", cardno);
		return (IMoneyCard) query.uniqueResult();
	}
	
	/**
	 * 生成一张新i币卡。
	 * @param gamecode
	 * @param accountID
	 * @param amount
	 * @return 返回创建好的卡对象
	 */
	public IMoneyCard generateCard(String gamecode, int accountID, int amount) {
		IMoneyCard card = new IMoneyCard();
		card.setCardno(generateCardNo());
		card.setPassword(generatePassword());
		card.setGameCode(gamecode);
		card.setAmount(amount);
		card.setCreateTime(new java.util.Date());
		card.setAccountID(accountID);
		card.setUsed(false);
		card.setUseTime(null);
		card.setUseAccount(-1);
		card.setUseGameCode(null);
		add(card);
		return card;
	}
	
	/**
	 * 使用一张i币卡。
	 * @param cardno
	 * @param password
	 * @return 如果使用成功，返回使用后的卡对象
	 */
	public IMoneyCard useCard(String gamecode, int accountID, String cardno, String password) {
		synchronized (useLock) {
			IMoneyCard card = getIMoneyCard(cardno);
			if (card == null || card.isUsed() || !card.getPassword().equals(password)) {
				return null;
			}
			card.setUsed(true);
			card.setUseTime(new java.util.Date());
			card.setUseAccount(accountID);
			card.setUseGameCode(gamecode);
			update(card);
			return card;
		}
	}

	/**
	 * 随机生成卡号。卡号格式为：4位日期，4位序列号，2位随机码，1位校验码。
	 */
    private String generateCardNo() {
        int d = (int)((System.currentTimeMillis() / 86400000L) % 10000);
        String ds = String.valueOf(d);
        while (ds.length() < 4) {
            ds = "0" + ds;
        }
        int s = cardSerial.getAndIncrement() % 10000;
        String ss = String.valueOf(s);
        while (ss.length() < 4) {
            ss = "0" + ss;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ss.substring(0, 3));
        sb.append(ds.charAt(0));
        sb.append((char)('0' + rand.nextInt(10)));
        sb.append(ds.charAt(1));
        sb.append((char)('0' + rand.nextInt(10)));
        sb.append(ds.charAt(2));
        sb.append(ss.charAt(3));
        sb.append(ds.charAt(3));
        sb.append(Math.abs(sb.toString().hashCode()) % 10);
        return sb.toString();
    }
    
    /**
     * 随机生成密码。密码为10位随机数字。
     */
    private String generatePassword() {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < 10; i++) {
	        sb.append((char)('0' + rand.nextInt(10)));
    	}
        return sb.toString();
    }
}
