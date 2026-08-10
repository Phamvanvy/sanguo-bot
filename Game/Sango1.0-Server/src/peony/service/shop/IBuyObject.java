package peony.service.shop;

import java.util.List;

import peony.game.PlayerTransaction;

import com.pip.sanguo.data.Shop;

/**
 * 一个实际购买请求的实现，例如购买商店物品的请求。
 * @author lighthu
 */
public interface IBuyObject {
    /**
     * 取得购买此服务的所有需求。
     */
    List<Shop.BuyRequirement> getRequirements();
    /**
     * 尝试锁定购买目标。
     * @throws ShopException
     */
    void lock() throws ShopException;
    /**
     * 发货。
     * @param tx 玩家操作事务
     * @param supportMail 是否支持邮件发货
     * @throws ShopException
     */
    void receive(PlayerTransaction tx, boolean supportMail) throws ShopException;
    /**
     * 提交所有修改。
     */
    void commit();
    /**
     * 回滚所有修改。
     */
    void rollback();
    /**
     * 取得折扣率0-100。
     * @return
     */
    int getDiscount();
    /**
     * 取得购买数量。
     * @return
     */
    int getCount();
    /**
     * 购买结果通知客户端。
     * @param succ 是否成功
     * @param message 如果失败，错误消息
     */
    void notifyClient(boolean succ, String message);
    /**
     * 记录购买成功日志。
     */
    void log();
    /**
     * 取得操作类型。
     */
    String getCause();
    /**
     * 是否允许使用不被信任的元宝购买。
     */
    boolean allowUntrustIMoney();
    /**
     * 是否允许使用绑定元宝
     */
    boolean allowUseBindImoney();
}
