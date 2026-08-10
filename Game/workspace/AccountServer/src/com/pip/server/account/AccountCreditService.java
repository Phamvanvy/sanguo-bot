package com.pip.server.account;

import java.util.Date;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import com.pip.server.account.bean.AccountCredit;
import com.pip.server.account.dao.AccountCreditDAO;

/**
 * 管理帐号的积分信息。
 * @author lighthu
 */
public class AccountCreditService {
    private final AccountCreditDAO accountCreditDAO;
	private final Cache cache;
	private static final Logger log = Logger.getLogger(AccountCreditService.class);

	public AccountCreditService(AccountCreditDAO dao, Cache cache) {
		this.accountCreditDAO = dao;
		this.cache = cache;
	}

	protected AccountCredit getAccountCreditFromCache(int id) {
        Element e = cache.get(new Integer(id));
        if (e == null)
            return null;
        return (AccountCredit) e.getObjectValue();
    }
	
    protected AccountCredit getAccountCreditImpl(int id) {
        synchronized (cache) {
            AccountCredit ae = getAccountCreditFromCache(id);
            if (ae != null) {
                return ae;
            }
            ae = accountCreditDAO.getAccountCredit(id);
            if (ae != null) {
                cache.put(new Element(new Integer(id), ae));
            }
            return ae;
        }
    }
	
    /**
     * 查询帐号的积分。
     * @param id 帐号ID
     * @return
     */
    public int getAccountCredit(int id) {
        return getAccountCreditImpl(id).getCredit();
    }
    
    /**
     * 添加/扣除积分。
     * @param id 帐号ID
     * @param addValue 如果小于0，表示扣除
     * @return 如果扣除时发现余额不足，返回false。
     */
    public boolean changeAccountCredit(int id, int addValue) {
        AccountCredit ae = getAccountCreditImpl(id);
        synchronized (ae) {
            if (addValue > 0) {
                ae.setCredit(ae.getCredit() + addValue);
                
                // 保存
                accountCreditDAO.update(ae);
            
                log.info("[ADD_CREDIT]ACCOUNTID[" + id + "]ADD[" + addValue + "]NEW[" + ae.getCredit() + "]");
                return true;
            } else {
                if (ae.getCredit() + addValue < 0) {
                    return false;
                }
                ae.setCredit(ae.getCredit() + addValue);

                // 保存
                accountCreditDAO.update(ae);
            
                log.info("[DEC_CREDIT]ACCOUNTID[" + id + "]DEC[" + (-addValue) + "]NEW[" + ae.getCredit() + "]");
                return true;
            }
        }
    }
    
    /**
     * 添加在线时长积分。用户每次下线时统计其在线时长，按每小时1分，每天最多5分的规则发放。
     * @param id 帐号ID
     * @param duration 在线时长(毫秒)
     */
    public void addTimeCredit(int id, int duration) {
        AccountCredit ae = getAccountCreditImpl(id);
        synchronized (ae) {
            log.info("[TIMECREDIT]ACCOUNTID[" + id + "]DURATION[" + (duration / 1000) + "]TRY");
            int oldValue = ae.getCredit();
            long end = System.currentTimeMillis();
            long start = end - duration;
            
            // 如果本次在线开始时间和上次下线时间在同一天，则计算当日总在线时间
            if (ae.getLogoutTime() != null && ae.getLogoutTime().getTime() / 86400000L == start / 86400000L) {
                long dayend = start - (start % 86400000L) + 86400000L;
                long thisLen = Math.min(dayend, end) - start;
                ae.setDayOnline(ae.getDayOnline() + (int)(thisLen / 1000));
                int newCredit = ae.getDayOnline() / 3600;
                if (newCredit > 5) {
                    newCredit = 5;
                }
                if (newCredit > ae.getDayCredit()) {
                    ae.setCredit(ae.getCredit() + newCredit - ae.getDayCredit());
                    ae.setDayCredit(newCredit);
                }
                start = dayend;
            }
            
            // 对经过的每一天做计算
            while (start < end) {
                long dayend = start - (start % 86400000L) + 86400000L;
                long thisLen = Math.min(dayend, end) - start;
                ae.setDayOnline((int)(thisLen / 1000));
                int newCredit = ae.getDayOnline() / 3600;
                if (newCredit > 5) {
                    newCredit = 5;
                }
                if (newCredit > 0) {
                    ae.setCredit(ae.getCredit() + newCredit);
                    ae.setDayCredit(newCredit);
                }
                start = dayend;
            }
            ae.setLogoutTime(new Date(end));
            
            // 保存
            accountCreditDAO.update(ae);
            
            log.info("[TIMECREDIT]ACCOUNTID[" + id + "]DURATION[" + (duration / 1000) + "]ADD[" + (ae.getCredit() - oldValue) + "]NEW[" + ae.getCredit() + "]");
        }
    }
}
