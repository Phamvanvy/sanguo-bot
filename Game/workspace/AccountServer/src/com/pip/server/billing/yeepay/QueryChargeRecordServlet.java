package com.pip.server.billing.yeepay;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.billing.Server;
import com.pip.server.billing.appstore.Order_AppStore;
import com.pip.server.billing.appstore.Order_AppStoreDAO;
import com.pip.server.billing.chinarund.PayInfo;
import com.pip.server.billing.chinarund.PayInfoDAO;
import com.pip.server.billing.ruyifu.Order_RuYiFu;
import com.pip.server.billing.ruyifu.Order_RuYiFuDAO;
import com.pip.server.billing.u19pay.Order_19Pay;
import com.pip.server.billing.u19pay.Order_19PayDAO;

/**
 * 查询最近支付请求状态。
 * 请求参数：
 *     accountid - 账号ID
 * 输出（UTF-8编码文本）：
 *  每行一条记录。一条记录中各字段用TAB分隔。字段依次有：
 *      卡号(带屏蔽) 密码(带屏蔽) 下单时间(HH:mm) 金额(元) 状态(成功/失败/进行中/成功(i币未到账))
 *  例如：
 *      220xxx222 329xxx023 03:22 50.00 成功
 *      220xxx221 319xxx023 03:21 50.00 失败
 * @author lighthu
 */
public class QueryChargeRecordServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(QueryChargeRecordServlet.class);

	protected SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	protected Server server;
	protected PayInfoDAO yeepayDAO;
	protected Order_19PayDAO pay19DAO;
	protected Order_RuYiFuDAO ruyifuDAO;
	protected Order_AppStoreDAO appstoreDAO;
	private static final SimpleDateFormat payTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private Hashtable<Integer, QueryResult> resultCache = new Hashtable<Integer, QueryResult>();
	
	private static class QueryResult {
		public long queryTime;
		public ChargeRecord[] results;
		
		public QueryResult(ChargeRecord[] results) {
			queryTime = System.currentTimeMillis();
			this.results = results;
		}
		
		public boolean isExpired() {
			return queryTime < System.currentTimeMillis() - 60000L;
		}
	}
	
	private static class ChargeRecord implements Comparable<ChargeRecord> {
		public String cardno;     // 卡号（未屏蔽）
		public String cardpass;   // 密码（未屏蔽）
		public Date time;		  // 下单时间
		public int amount;		  // 金额（分）
		public int status;		  // 状态：0 - 进行中，1 - 成功， 2 - 失败，3 - 扣款成功，但i币未到账
		private static final SimpleDateFormat outputTimeFormat = new SimpleDateFormat("HH:mm");
		private static final DecimalFormat outputAmountFormat = new DecimalFormat("####.##");
		
		public int compareTo(ChargeRecord obj) {
			return time.compareTo(obj.time);
		}
		
		private String encrypt(String str) {
			if (str.length() < 9) {
				return "xxx";
			}
			String prefix = str.substring(0, 3);
			String suffix = str.substring(str.length() - 3);
			return prefix + "xxx" + suffix;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(encrypt(cardno));
			sb.append("\t");
			sb.append(encrypt(cardpass));
			sb.append("\t");
			sb.append(outputTimeFormat.format(time));
			sb.append("\t");
			sb.append(outputAmountFormat.format(amount / 100.0));
			sb.append("\t");
			switch (status) {
			case 0:
				sb.append("进行中");
				break;
			case 1:
				sb.append("成功");
				break;
			case 2:
				sb.append("失败");
				break;
			case 3:
				sb.append("成功(i币未到账)");
				break;
			}
			return sb.toString();
		}
		
		public void parse(PayInfo order) {
			cardno = order.getCardno();
			cardpass = order.getCardpass();
			try {
				time = payTimeFormat.parse(order.getPayTime());
			} catch (Exception e) {
				time = new Date();
			}
			amount = Integer.parseInt(order.getMoney()) * 100;
			if (order.isValid()) {
				if (order.isAddIFail()) {
					status = 3;
				} else {
					status = 1;
				}
			} else {
				if (order.getNotifyTime() == null) {
					status = 0;
				} else {
					status = 2;
				}
			}
		}
		
		public void parse(Order_19Pay order) {
			cardno = order.getCardNo();
			cardpass = order.getCardPass();
			time = order.getCreateTime();
			amount = order.getMoney();
			if (order.getStatus() == 0) {
				status = 0;
			} else if (order.getStatus() == 1) {
				if (order.getFeeID() == 0) {
					status = 3;
				} else {
					status = 1;
				}
			} else {
				status = 2;
			}
		}
		
		public void parse(Order_RuYiFu order) {
			cardno = order.getCardNo();
			cardpass = order.getCardPass();
			time = order.getCreateTime();
			amount = order.getMoney();
			if (order.getStatus() == 0) {
				status = 0;
			} else if (order.getStatus() == 1) {
				if (order.getFeeID() == 0) {
					status = 3;
				} else {
					status = 1;
				}
			} else {
				status = 2;
			}
		}
		
		public void parse(Order_AppStore order) {
			cardno = "itunes";
			cardpass = "itunes";
			time = order.getCreateTime();
			amount = order.getMoney();
			if (order.getStatus() == 0) {
				status = 0;
			} else if (order.getStatus() == 1) {
				if (order.getFeeID() == 0) {
					status = 3;
				} else {
					status = 1;
				}
			} else {
				status = 2;
			}
		}
	}

	public QueryChargeRecordServlet(Server server, PayInfoDAO yeepayDAO, Order_19PayDAO pay19DAO, 
			Order_RuYiFuDAO ruyifuDAO, Order_AppStoreDAO appstoreDAO) {
		this.server = server;
		this.yeepayDAO = yeepayDAO;
		this.pay19DAO = pay19DAO;
		this.ruyifuDAO = ruyifuDAO;
		this.appstoreDAO = appstoreDAO;
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 验证请求IP
        String addr = request.getRemoteAddr();
        if (!server.trustip_directfee.contains(addr)) {
            log.warn("Possible attack from [" + addr + "] is rejected.");
            return;
        }
        
		int accountID = Integer.parseInt(request.getParameter("accountid"));
		
        // 设置返回格式
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf8");
        PrintWriter out = response.getWriter();
        
        // 在缓存中搜索结果
        QueryResult result = resultCache.get(accountID);
        if (result != null) {
        	if (result.isExpired()) {
        		resultCache.remove(accountID);
        		result = null;
        	}
        }
        if (result == null) {
	        // 查找订单
			Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
			try {
				List<PayInfo> yeepayOrders = yeepayDAO.getRecentRecords(accountID);
				List<Order_19Pay> pay19Orders = pay19DAO.getRecentRecords(accountID);
				List<Order_RuYiFu> ruyifuOrders = ruyifuDAO.getRecentRecords(accountID);
				List<Order_AppStore> appstoreOrders = appstoreDAO.getRecentRecords(accountID);
				List<ChargeRecord> records = new ArrayList<ChargeRecord>();
				for (PayInfo order : yeepayOrders) {
					ChargeRecord cr = new ChargeRecord();
					cr.parse(order);
					records.add(cr);
				}
				for (Order_19Pay order : pay19Orders) {
					ChargeRecord cr = new ChargeRecord();
					cr.parse(order);
					records.add(cr);
				}
				for (Order_RuYiFu order : ruyifuOrders) {
					ChargeRecord cr = new ChargeRecord();
					cr.parse(order);
					records.add(cr);
				}
				for (Order_AppStore order : appstoreOrders) {
					ChargeRecord cr = new ChargeRecord();
					cr.parse(order);
					records.add(cr);
				}
				
				// 按时间排序
				ChargeRecord[] arr = new ChargeRecord[records.size()];
				records.toArray(arr);
				Arrays.sort(arr);
				
				// 存入缓存
				result = new QueryResult(arr);
				resultCache.put(accountID, result);
				tx.commit();
			} catch (Exception ex) {
				tx.rollback();
				log.error(ex, ex);
			}
        }
        
        if (result != null) {
	        // 按时间从大到小排序输出
			for (int i = result.results.length - 1; i >= 0; i--) {
				out.println(result.results[i].toString());
			}
        }
	}
}
