package com.pip.server.auth.cmcc;

import java.util.HashMap;
import java.util.Map;

/**
 * 卓望平台接口异常。
 */
public class CmccException extends Exception{
    /*
     * 平台错误代码。
     */
    private static Map<Integer, String> ERROR_CODES = new HashMap<Integer, String>();
    static {
        ERROR_CODES.put(100, "您的手机号码不存在，购买失败（状态码：100）");
        ERROR_CODES.put(101, "您的手机号码有错误，购买失败（状态码：101）");
        ERROR_CODES.put(102, "您的手机号码已停机，购买失败（状态码：102）");
        ERROR_CODES.put(103, "您的手机号码欠费，请缴费后再次购买（状态码：103）");
        ERROR_CODES.put(104, "您没有使用本业务的权限（状态码：104）");
        ERROR_CODES.put(105, "业务代码错误（状态码：105）");
        ERROR_CODES.put(106, "服务代码错误（状态码：106）");
        ERROR_CODES.put(107, "业务不存在（状态码：107）");
        ERROR_CODES.put(108, "本业务已经暂停服务，请使用其它业务（状态码：108）");
        ERROR_CODES.put(109, "该服务种类不存在(状态码：109）");
        ERROR_CODES.put(110, "该服务种类尚未开通（状态码：110）");
        ERROR_CODES.put(111, "该业务尚未开通，敬请期待（状态码：111）");
        ERROR_CODES.put(112, "游戏提供商的合作代码错误（状态码：112）");
        ERROR_CODES.put(113, "游戏提供商不存在（状态码：113）");
        ERROR_CODES.put(114, "该业务已暂停服务（状态码：114）");
        ERROR_CODES.put(115, "您还没有订购本业务，请先订购（状态码：115）");
        ERROR_CODES.put(116, "用户暂停定购该套餐业务（状态码：116）");
        ERROR_CODES.put(117, "您没有使用本业务的权限（状态码：117）");
        ERROR_CODES.put(118, "您已经订购本业务（状态码：118）");
        ERROR_CODES.put(119, "您承诺的服务期未到期，不能退订本业务（状态码：119）");
        ERROR_CODES.put(120, "系统异常（状态码：120）");
        ERROR_CODES.put(121, "没有该类业务（状态码：121）");
        ERROR_CODES.put(122, "系统异常（状态码：122）");
        ERROR_CODES.put(123, "业务价格为负（状态码：123）");
        ERROR_CODES.put(124, "业务价格格式错误（状态码：124）");
        ERROR_CODES.put(125, "业务价格超出范围（状态码：125）");
        ERROR_CODES.put(126, "您的手机号码无法使用本业务（状态码：126）");
        ERROR_CODES.put(127, "您的手机号码余额不足（状态码：127）");
        ERROR_CODES.put(128, "系统异常（状态码：128）");
        ERROR_CODES.put(129, "您已经是注册用户（状态码：129）");
        ERROR_CODES.put(130, "您的手机号码无法使用本业务（状态码：130）");
        ERROR_CODES.put(131, "系统异常（状态码：131）");
        ERROR_CODES.put(132, "您的手机号码无法使用本业务（状态码：132）");
        ERROR_CODES.put(133, "系统异常（状态码：133）");
        ERROR_CODES.put(134, "系统异常（状态码：134）");
        ERROR_CODES.put(135, "系统异常（状态码：135）");
        ERROR_CODES.put(136, "您的密码错误（状态码：136）");
        ERROR_CODES.put(139, "您的手机号码无法使用本业务（状态码：139）");
        ERROR_CODES.put(140, "您没有点播本业务（状态码：140）");
        ERROR_CODES.put(141, "您的手机号码无法使用本业务（状态码：141）");
        ERROR_CODES.put(142, "您的手机号码无法使用本业务（状态码：142）");
        ERROR_CODES.put(143, "您的手机号码无法使用本业务（状态码：143）");
        ERROR_CODES.put(201, "点卡账户已经存在（状态码：201）");
        ERROR_CODES.put(202, "您的手机余额不足（状态码：202）");
        ERROR_CODES.put(203, "系统异常（状态码：203）");
        ERROR_CODES.put(204, "您点数充值已经超过当天的最大限额（状态码：204）");
        ERROR_CODES.put(205, "您点数充值已经超过当月的最大限额（状态码：205）");
        ERROR_CODES.put(206, "您的点数消费已经超出每次的最大限额（状态码：206）");
        ERROR_CODES.put(207, "您的点数消费已经超出当日的最大限额（状态码：207）");
        ERROR_CODES.put(208, "您的点数消费已经超出当月的最大限额（状态码：208）");
        ERROR_CODES.put(280, "系统异常（状态码：280）");
        ERROR_CODES.put(281, "系统异常（状态码：281）");
        ERROR_CODES.put(282, "系统异常（状态码：282）");
        ERROR_CODES.put(1111, "计费代码错误（状态码：1111）");
        ERROR_CODES.put(1112, "充值代码无效（状态码：1112）");
        ERROR_CODES.put(1113, "充值代码过有效期（状态码：1113）");
        ERROR_CODES.put(1114, "您的手机号码现在无法充值（状态码：1114）");
        ERROR_CODES.put(1115, "系统异常（状态码：1115）");
        ERROR_CODES.put(1116, "系统异常（状态码：1116）");
        ERROR_CODES.put(1118, "充值代码错误（状态码：1118）");
        ERROR_CODES.put(1120, "网络错误（状态码：1120）");
        ERROR_CODES.put(1121, "网络错误（状态码：1121）");
        ERROR_CODES.put(1122, "网络错误（状态码：1122）");
        ERROR_CODES.put(1170, "未知的查询类型（状态码：1170）");
        ERROR_CODES.put(1171, "起始序号不应该小于1.（状态码：1171）");
        ERROR_CODES.put(1172, "记录数量应大于0（状态码：1172）");
        ERROR_CODES.put(1173, "查询开始日期应大于截至日期（状态码：1173）");
        ERROR_CODES.put(1175, "WAP 单机（状态码：1175）");
        ERROR_CODES.put(1176, "WAP 网游（状态码：1176）");
        ERROR_CODES.put(1177, "客户端单机（状态码：1177）");
        ERROR_CODES.put(1178, "客户端网游（状态码：1178）");
        ERROR_CODES.put(1180, "登录失败，请重行登录（状态码：1180）");
        ERROR_CODES.put(1181, "您的手机号码欠费，请缴费后再次购买（状态码：1181）");
        ERROR_CODES.put(1182, "您的点数余额不足（状态码：1182）");
        ERROR_CODES.put(1183, "您点数充值已经超过当天的最大限额（状态码：1183）");
        ERROR_CODES.put(1184, "您点数充值已经超过当月的最大限额（状态码：1184）");
        ERROR_CODES.put(1185, "由于计费子系统引起的逻辑错误（状态码：1185）");
        ERROR_CODES.put(1186, "您的点数消费已经超出当月的最大限额（状态码：1186）");
        ERROR_CODES.put(1199, "控制鉴权失败（状态码：1199）");
        ERROR_CODES.put(1270, "网络繁忙，请稍候再试（状态码：1270）");
        ERROR_CODES.put(1271, "网络繁忙，请稍候再试（状态码：1271）");
        ERROR_CODES.put(1272, "网络繁忙，请稍候再试（状态码：1272）");
        ERROR_CODES.put(1273, "网络繁忙，请稍候再试（状态码：1273）");
        ERROR_CODES.put(1274, "网络繁忙，请稍候再试（状态码：1274）");
        ERROR_CODES.put(1275, "网络繁忙，请稍候再试（状态码：1275）");
        ERROR_CODES.put(1276, "网络繁忙，请稍候再试（状态码：1276）");
        ERROR_CODES.put(1277, "网络繁忙，请稍候再试（状态码：1277）");
        ERROR_CODES.put(1278, "登录失败，请重行登录（状态码：1278）");
        ERROR_CODES.put(1281, "登录失败，请重行登录（状态码：1281）");
        ERROR_CODES.put(1282, "用户不存在-用户管理（状态码：1282）");
        ERROR_CODES.put(1288, "点数账户开户失败（状态码：1288）");
        ERROR_CODES.put(1297, "用户已经是注册用户（状态码：1297）");
        ERROR_CODES.put(1311, "用户请求消费业务所属的套餐为非商用状态（状态码：1311）");
        ERROR_CODES.put(1312, "用户请求消费业务所属的套餐不存在订购关系（状态码：1312）");
        ERROR_CODES.put(1316, "用户不存在该套餐订购关系（状态码：1316）");
        ERROR_CODES.put(1318, "用户请求消费业务所属的套餐为下线状态（状态码：1318）");
        ERROR_CODES.put(1320, "已存在订购关系（状态码：1320）");
        ERROR_CODES.put(1322, "用户请求消费业务所属的套餐不存在（状态码：1322）");
        ERROR_CODES.put(1323, "未找到用户下载记录，请重新下载。（状态码：1323）");
        ERROR_CODES.put(1324, "单机业务没有付费（状态码：1324）");
        ERROR_CODES.put(1325, "用户向 servlet 发送请求时参数不全（状态码：1325）");
        ERROR_CODES.put(1328, "用户状态暂停不能使用业务（状态码：1328）");
        ERROR_CODES.put(1399, "未知错误（状态码：1399）");
        ERROR_CODES.put(1611, "用户控制-日期格式不对（状态码：1611）");
        ERROR_CODES.put(1615, "用户控制-email 格式不正确（状态码：1615）");
        ERROR_CODES.put(1616, "用户密码为空（状态码：1616）");
        ERROR_CODES.put(1670, "查询用户信息失败（状态码：1670）");
        ERROR_CODES.put(1671, "查询用户密码失败（状态码：1671）");
        ERROR_CODES.put(1672, "用户冻结（状态码：1672）");
        ERROR_CODES.put(1673, "用户注销（状态码：1673）");
        ERROR_CODES.put(1674, "修改用户信息失败（状态码：1674）");
        ERROR_CODES.put(1675, "修改用户密码失败（状态码：1675）");
        ERROR_CODES.put(1677, "注册用户失败（状态码：1677）");
        ERROR_CODES.put(1678, "用户登录失败（状态码：1678）");
        ERROR_CODES.put(1680, "用户不存在-用户控制（状态码：1680）");
        ERROR_CODES.put(1681, "手机号码错误（状态码：1681）");
        ERROR_CODES.put(1682, "用户密码错误（状态码：1682）");
        ERROR_CODES.put(1691, "用户控制通讯鉴权失败（状态码：1691）");
        ERROR_CODES.put(1693, "用户控制-接口名称不存在（状态码：1693）");
        ERROR_CODES.put(1694, "用户控制-接口不可用（状态码：1694）");
        ERROR_CODES.put(1695, "用户控制-接口配置错误（状态码：1695）");
        ERROR_CODES.put(1697, "用户控制-未登录网游（状态码：1697）");
        ERROR_CODES.put(1699, "用户控制-性别不在范围内（状态码：1699）");
        ERROR_CODES.put(1801, "网络繁忙，请稍候再试（状态码：1801）");
        ERROR_CODES.put(1813, "游戏提供商已下线（状态码：1813）");
        ERROR_CODES.put(1814, "网络繁忙，请稍候再试（状态码：1814）");
        ERROR_CODES.put(1817, "网络繁忙，请稍候再试（状态码：1817）");
        ERROR_CODES.put(1818, "网络繁忙，请稍候再试（状态码：1818）");
        ERROR_CODES.put(1819, "游戏提供商不存在（状态码：1819）");
        ERROR_CODES.put(1831, "此业务已暂停（状态码：1831）");
        ERROR_CODES.put(1832, "此业务未上线，敬请期待（状态码：1832）");
        ERROR_CODES.put(1833, "此业务已下线（状态码：1833）");
        ERROR_CODES.put(1835, "此业务待下线（状态码：1835）");
        ERROR_CODES.put(1836, "此业务待测试（状态码：1836）");
        ERROR_CODES.put(1838, "网络繁忙，请稍候再试（状态码：1838）");
        ERROR_CODES.put(1839, "此业务不存在（状态码：1839）");
        ERROR_CODES.put(1853, "此业务已下线（状态码：1853）");
        ERROR_CODES.put(1854, "信息录入中（状态码：1854）");
        ERROR_CODES.put(1855, "此业务待测试（状态码：1855）");
        ERROR_CODES.put(1856, "此业务已暂停（状态码：1856）");
        ERROR_CODES.put(1857, "此业务待下线（状态码：1857）");
        ERROR_CODES.put(1858, "网络繁忙，请稍候再试（状态码：1858）");
        ERROR_CODES.put(1859, "此业务不存在（状态码：1859）");
        ERROR_CODES.put(1874, "您的手机号码无法使用本业务（状态码：1874）");
        ERROR_CODES.put(1875, "无法获取您的手机号，请稍候再试（状态码：1875）");
        ERROR_CODES.put(1876, "请使用中国移动手机号码使用本业务（状态码：1876）");
        ERROR_CODES.put(1877, "请使用手机访问（状态码：1877）");
        ERROR_CODES.put(1878, "您的手机号码无法使用本业务（状态码：1878）");
        ERROR_CODES.put(1883, "此业务已下线（状态码：1883）");
        ERROR_CODES.put(1885, "网络繁忙，请稍候再试（状态码：1885）");
        ERROR_CODES.put(1886, "网络繁忙，请稍候再试（状态码：1886）");
        ERROR_CODES.put(1889, "此业务不存在（状态码：1889）");
        ERROR_CODES.put(9000, "系统繁忙,现在无法使用,请您稍后再试(状态码:9000)");
        ERROR_CODES.put(9001, "系统繁忙,现在无法使用,请您稍后再试(状态码:9001)");
        ERROR_CODES.put(9002, "系统繁忙,现在无法使用,请您稍后再试(状态码:9002)");
        ERROR_CODES.put(9003, "系统繁忙,现在无法使用,请您稍后再试(状态码:9003)");
        ERROR_CODES.put(9005, "系统繁忙,现在无法使用,请您稍后再试(状态码:9005)");
        ERROR_CODES.put(9007, "系统繁忙,现在无法使用,请您稍后再试(状态码:9007)");
        ERROR_CODES.put(9008, "系统繁忙,现在无法使用,请您稍后再试(状态码:9008)");
        ERROR_CODES.put(9009, "系统繁忙,现在无法使用,请您稍后再试(状态码:9009)");
        ERROR_CODES.put(9010, "系统繁忙,现在无法使用,请您稍后再试(状态码:9010)");
        ERROR_CODES.put(9011, "系统繁忙,现在无法使用,请您稍后再试(状态码:9011)");
        ERROR_CODES.put(9012, "系统繁忙,现在无法使用,请您稍后再试(状态码:9012)");
        ERROR_CODES.put(9013, "系统繁忙,现在无法使用,请您稍后再试(状态码:9013)");
        ERROR_CODES.put(9014, "系统繁忙,现在无法使用,请您稍后再试(状态码:9014)");
        ERROR_CODES.put(9015, "系统繁忙,现在无法使用,请您稍后再试(状态码:9015)");
        ERROR_CODES.put(9017, "您的网络地址未经允许,无法访问(状态码:9017)");
        ERROR_CODES.put(9598, "系统繁忙,现在无法使用,请您稍后再试(状态码:9598)");
        ERROR_CODES.put(9599, "系统繁忙,现在无法使用,请您稍后再试(状态码：9599)");
        ERROR_CODES.put(9601, "系统繁忙,现在无法使用,请您稍后再试(状态码:9601)");
        ERROR_CODES.put(9602, "请您使用CMWAP 方式连接网络(状态码:9602)");
        ERROR_CODES.put(9701, "系统繁忙,现在无法使用,请您稍后再试(状态码:9701)");
        ERROR_CODES.put(9801, "系统繁忙,现在无法使用,请您稍后再试(状态码:9801)");
        ERROR_CODES.put(9802, "系统繁忙,现在无法使用,请您稍后再试(状态码:9802)");
        ERROR_CODES.put(9902, "WAP 网关错误(状态码：9902)");
        ERROR_CODES.put(9999, "未知错误(状态码:9999)");
        ERROR_CODES.put(1401, "短信指令签名不对");
        ERROR_CODES.put(1402, "短信指令别名不存在");
        ERROR_CODES.put(1403, "短信指令长度不够");
        ERROR_CODES.put(398, "系统异常（状态码：398）");
        ERROR_CODES.put(399, "系统异常（状态码：399）");
    }

    public CmccException(String msg) {
        super(msg);
    }
    
    public static CmccException create(int code) {
        String msg = ERROR_CODES.get(code);
        if (msg == null) {
            msg = "未知错误(状态码:" + code + ")";
        }
        return new CmccException(msg);
    }
}
