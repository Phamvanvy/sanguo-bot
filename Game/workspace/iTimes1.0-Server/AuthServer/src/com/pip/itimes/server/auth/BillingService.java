package com.pip.itimes.server.auth;

import java.util.List;

import org.apache.log4j.Logger;

import com.pip.itimes.server.bean.ChargePlan;
import com.pip.itimes.server.bean.FeePlan;
import com.pip.itimes.server.dao.ChargePlanDao;
import com.pip.itimes.server.dao.FeePlanDao;
import com.pip.itimes.server.dao.HibernateUtil;

public class BillingService {

    public static Logger log = Logger.getLogger(BillingService.class);

    private ChargePlan[] chargePlans;
    private FeePlan[] feePlans;

    public void init() {
        try {
            ChargePlanDao chargeDao = new ChargePlanDao();
            FeePlanDao feeDao = new FeePlanDao();
            List l = chargeDao.getAllChargePlan();
            chargePlans = new ChargePlan[l.size()];
            l.toArray(chargePlans);
            l = feeDao.getAllFePlanDao();
            feePlans = new FeePlan[l.size()];
            l.toArray(feePlans);
            HibernateUtil.commitTransaction();
        } catch (Exception ex) {
            log.debug(ex, ex);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public FeePlan getFeePlan(int id) {
        if (feePlans == null)return null;
        for (int i = 0; i < feePlans.length; i++) {
            FeePlan plan = feePlans[i];
            if (plan.getId() == id)return plan;
        }
        return null;
    }
}
