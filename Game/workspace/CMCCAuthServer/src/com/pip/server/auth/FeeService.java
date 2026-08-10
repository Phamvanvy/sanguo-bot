package com.pip.server.auth;

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import com.pip.security.SecurityUtils;
import com.pip.server.auth.bean.Account;
import com.pip.server.auth.bean.Fee;
import com.pip.server.auth.dao.FeeDao;
import com.pip.server.auth.net.*;

/**
 * 计费服务器接口。通过一个HTTP服务器向计费服务器提供服务；另外这个HTTP服务器还提供卓望版本 的用户登录信息同步接口。 /billing_if
 * 计费服务器接口 /QueryFee 查询余额 /Charge 直接充值 /UserNotify 卓望版本用户登录信息同步接口
 */
public class FeeService {

    private static final Logger log = Logger.getLogger(FeeService.class);

    private FeeDao feeDao;
    private AccountService accountService;
    private ConnectService connectService;
    private Set<String> trustips = new HashSet<String>();

    public FeeService(FeeDao feeDao, Configuration conf, AccountService accountService, JettyServer jServer)
            throws Exception {
        this.feeDao = feeDao;
        Collections.addAll(trustips, conf.getStringArray("fee_trustips"));
        this.accountService = accountService;

        // 续费接口
        jServer.addServlet("/billing_if", new BillingServlet());
        // 查询余额接口
        jServer.addServlet("/QueryFee", new BalanceServlet());
        // 充值接口
        jServer.addServlet("/Charge", new ChargeServlet());
    }

    public void setConnectService(ConnectService connectService) {
        this.connectService = connectService;
    }

    /**
     * 创建一条新的计费账单。对于预先知道续费渠道的账单，生成账单时提供金额和渠道号，完成账单时不需要再提供。
     * 对于不知道续费渠道的账单，生成账单时可先不提供这两个数据，但在完成账单时必须提供。
     * 
     * @param accountId
     *            计费帐户ID
     * @param amount
     *            账单金额（单位为i币*100），0表示不确定
     * @param channel
     *            渠道ID，参见Const类，null或空串表示不确定
     * @return 新创建的账单对象
     */
    public Fee newFee(int accountId, int amount, String channel) {
        Fee fee = new Fee();
        fee.setAccountId(accountId);
        fee.setCharged(false);
        fee.setCreateTime(new Date());
        fee.setFinishTime(null);
        fee.setAmount(amount);
        fee.setChannel(channel);
        feeDao.saveFee(fee);
        return fee;
    }

    /**
     * 在fee表插入一条数据，charged为true，finsihedtime=createtime
     * 
     * @param account
     *            int
     * @param amount
     *            int
     * @param channel
     *            String
     * @return Fee
     */
    public Fee newChargedFee(int accountId, int amount, String channel) {
        Fee fee = new Fee();
        fee.setAccountId(accountId);
        fee.setCharged(true);
        fee.setCreateTime(new Date());
        fee.setFinishTime(new Date());
        fee.setAmount(amount);
        fee.setChannel(channel);
        feeDao.saveFee(fee);
        return fee;
    }

    /**
     * 根据帐户ID查找帐户对象，如果帐户不存在，返回null。
     */
    private Account findAccount(int id) {
        try {
            AccountState account = accountService.getAccount(id);
            Account a = null;
            if (account == null) {
                a = accountService.loadAccountById(id);
            } else {
                a = account.getAccount();
            }
            return a;
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * 根据帐户名称查找帐户对象，如果帐户不存在，返回null。
     */
    private Account findAccountByName(String name) {
        try {
            int accountId = accountService.getAccountId(name);
            if (accountId == -1) {
                return null;
            }
            return findAccount(accountId);
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * 根据帐户对应包月手机号查找账户，如果帐户不存在，返回null。
     */
    private Account findAccountBySubscribePhone(String phone) {
        try {
            int accountId = accountService.getAccountIdBySubscribePhone(phone);
            if (accountId == -1) {
                return null;
            }
            return findAccount(accountId);
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * 根据ID查找账单对象，如果账单不存在，返回null。
     */
    private Fee findFee(int id) {
        try {
            Fee fee = feeDao.getFee(id);
            return fee;
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * 查找某个渠道的最新一条续费记录。
     */
    private Fee findLatestFee(String channel) {
        try {
            Fee fee = feeDao.getLatestFee(channel);
            return fee;
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * 更新账户对象，其中，iMoney部分提供的是修改值。newFee对象可以为null。
     */
    private boolean updateAccount(Account newAcc, Fee newFee) {
        try {
            Account acc = findAccount(newAcc.getId());
            Fee fee = null;
            if (newFee != null) {
                fee = findFee(newFee.getId());
            }
            synchronized (acc) {
                // 更新账户信息
                acc.setLastBillingTime(newAcc.getLastBillingTime());
                acc.setValid(newAcc.getValid());
                acc.setCause(newAcc.getCause());
                acc.setPhone(newAcc.getPhone());
                acc.setiMoney(acc.getiMoney() + newAcc.getiMoney());
                acc.setMonthFee(newAcc.getMonthFee());
                acc.setRecommend(newAcc.getRecommend());
                acc.setPassword(newAcc.getPassword());
                acc.setSubscribeStatus(newAcc.getSubscribeStatus());
                acc.setSubscribePhone(newAcc.getSubscribePhone());
                acc.setSubscribeBill(newAcc.getSubscribeBill());
                accountService.saveAccount(acc);

                // 如果指定了Fee对象，更新Fee对象信息
                if (fee != null) {
                    fee.setCharged(newFee.isCharged());
                    fee.setFinishTime(newFee.getFinishTime());
                    fee.setAmount(newFee.getAmount());
                    fee.setChannel(newFee.getChannel());
                    feeDao.saveFee(fee);
                }
            }
            if (newAcc.getiMoney() != 0) {
                UWAPSegment seg = new UWAPSegment(AccountConstants.SYNC_IMONEY);
                seg.writeInt(acc.getId());
                seg.writeInt(acc.getiMoney());
                if (acc.getMonthFee() >= Const.MONTH_MAX) {
                    seg.writeBoolean(true);
                } else {
                    seg.writeBoolean(false);

                }
                if (acc.getSubscribeStatus() == Account.SUBSCRIBED) {
                    seg.writeBoolean(true);
                } else {
                    seg.writeBoolean(false);
                }
                connectService.broadcast(seg);
            }
            return true;
        } catch (Exception e) {
            log.error(e, e);
            return false;
        }
    }

    /**
     * 查找某一个渠道本月的消费总额。
     */
    int getMonthPayment(String channel) {
        try {
            return feeDao.getMonthSum(channel);
        } catch (Exception e) {
            log.error(e, e);
            return 0;
        }
    }

    /**
     * 计费转发接口。
     */
    class BillingServlet extends HttpServlet {
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                IOException {
            response.setContentType("text/plain;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            String addr = request.getRemoteAddr();
            if (!trustips.contains(addr)) {
                log.info("BillingServlet: AddressRejected[" + addr + "]");
                return;
            }
            request.setCharacterEncoding("UTF-8");
            String cmd = request.getParameter("cmd");
            if ("findAccount".equals(cmd)) {
                // Account findAccount(int id)
                int id = Integer.parseInt(request.getParameter("id"));
                Account acc = findAccount(id);
                response.getWriter().println(Const.objectToString(acc));
            } else if ("findAccountByName".equals(cmd)) {
                // Account findAccountByName(String name)
                String name = request.getParameter("name");
                Account acc = findAccountByName(name);
                response.getWriter().println(Const.objectToString(acc));
            } else if ("findAccountBySubscribePhone".equals(cmd)) {
                // Account findAccountBySubscribePhone(String phone)
                String phone = request.getParameter("phone");
                Account acc = findAccountBySubscribePhone(phone);
                response.getWriter().println(Const.objectToString(acc));
            } else if ("findFee".equals(cmd)) {
                // Fee findFee(int id)
                int id = Integer.parseInt(request.getParameter("id"));
                Fee fee = findFee(id);
                response.getWriter().println(Const.objectToString(fee));
            } else if ("findLatestFee".equals(cmd)) {
                // Fee findLatestFee(String channel)
                String channel = request.getParameter("channel");
                Fee fee = findLatestFee(channel);
                response.getWriter().println(Const.objectToString(fee));
            } else if ("newFee".equals(cmd)) {
                // Fee newFee(int accountId, int amount, String channel) {
                int accountId = Integer.parseInt(request.getParameter("accountId"));
                int amount = Integer.parseInt(request.getParameter("amount"));
                String channel = request.getParameter("channel");
                Fee fee = newFee(accountId, amount, channel);
                response.getWriter().println(Const.objectToString(fee));
            } else if ("updateAccount".equals(cmd)) {
                // void updateAccount(Account newAcc, Fee newFee)
                Account newAcc = (Account) Const.stringToObject(request.getParameter("newAcc"));
                Fee newFee = (Fee) Const.stringToObject(request.getParameter("newFee"));
                boolean result = updateAccount(newAcc, newFee);
                response.getWriter().println(result ? "1" : "0");
            } else if ("getMonthPayment".equals(cmd)) {
                // int getMonthPayment(String channel)
                String channel = request.getParameter("channel");
                int amount = getMonthPayment(channel);
                response.getWriter().println(amount);
            } else {
                throw new ServletException("Invaid request.");
            }
        }
    }

    /**
     * 查询余额接口。
     */
    class BalanceServlet extends HttpServlet {
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                IOException {
            String name = request.getParameter("account");
            String password = request.getParameter("password");
            response.setCharacterEncoding("GBK");
            if (name == null || name.length() == 0 || password == null || password.length() == 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("1");
                response.getWriter().println("参数错误");
                return;
            }
            int accountId = accountService.getAccountId(name);
            if (accountId == -1) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("1");
                response.getWriter().println("帐号不存在");
            } else {
                AccountState account = accountService.getAccount(accountId);
                Account a = null;
                if (account == null) {
                    a = accountService.loadAccountById(accountId);
                } else {
                    a = account.getAccount();
                }
                if (!a.getPassword().equals(password)) {
                    if (a.getPassword().startsWith("#")) {
                        if (!SecurityUtils.verifyMD5(password, a.getPassword().substring(1))) {
                            log.info("Login Error Name[" + name + "]Pass[" + password + "]");
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().println("1");
                            response.getWriter().println("帐号或者密码错误");
                            return;
                        }
                    } else {
                        log.info("Login Error Name[" + name + "]Pass[" + password + "]");
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println("1");
                        response.getWriter().println("帐号或者密码错误");
                        return;
                    }

                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("0");
                response.getWriter().println(a.getiMoney() / 100);
                Date lastBillingTime = a.getLastBillingTime();
                java.util.Date currentTime = new java.util.Date();
                if (a.getSubscribeStatus() == Account.SUBSCRIBED) {
                    response.getWriter().println(Const.MONTH_MAX / 100);
                } else if (Const.inLaterMonth(lastBillingTime, currentTime)) {
                    if (a.getMonthFee() > Const.MONTH_MAX) {
                        response.getWriter().println(Const.MONTH_MAX / 100);
                    } else {
                        response.getWriter().println(a.getMonthFee() / 100);
                    }
                } else {
                    response.getWriter().println(0);
                }
            }
        }

        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                IOException {
            doPost(request, response);
        }
    }

    class ChargeServlet extends HttpServlet {
        private static final String IP = "218.206.80.186";
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain;charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            if (!trustips.contains(req.getRemoteAddr())) {
                resp.getWriter().println(1);
                resp.getWriter().println("地址非信任");
                return;
            }
            int accountId = Integer.parseInt(req.getParameter("AccountId"));
            int charge = Integer.parseInt(req.getParameter("Charge"));
            int money = Integer.parseInt(req.getParameter("Money"));
            String channel = req.getParameter("Channel");
            Account acc = findAccount(accountId);

            if (acc != null) {
                Fee fee = null;
                synchronized (acc) {
                    acc.setiMoney(acc.getiMoney() + charge * 100);
                    accountService.saveAccount(acc);
                    fee = newChargedFee(acc.getId(), charge * 100, channel + "_" + money);
                    log.info("AccountID[" + acc.getId() + "]Charge[" + charge + "]Money[" + money + "]OK");
                }
                if (acc.getiMoney() != 0) {
                    UWAPSegment seg = new UWAPSegment(AccountConstants.SYNC_IMONEY);
                    seg.writeInt(acc.getId());
                    seg.writeInt(acc.getiMoney());
                    if (acc.getMonthFee() >= Const.MONTH_MAX) {
                        seg.writeBoolean(true);
                    } else {
                        seg.writeBoolean(false);

                    }
                    if (acc.getSubscribeStatus() == Account.SUBSCRIBED) {
                        seg.writeBoolean(true);
                    } else {
                        seg.writeBoolean(false);
                    }

                    connectService.broadcast(seg);
                }
                resp.getWriter().println(0);
                if (fee != null) {
                    resp.getWriter().print(fee.getId());
                }
            } else {
                resp.getWriter().println(1);
                resp.getWriter().println("没找到对应帐号");
            }
        }
    }
}
