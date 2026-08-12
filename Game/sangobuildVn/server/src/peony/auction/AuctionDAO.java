package peony.auction;

import java.util.ArrayList;
import java.util.List;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class AuctionDAO extends GenericHibernateDAO<Auction, Integer> {

	/** 组拼查询字符串 */
	private String getParameters(int type, int quality, int leveldown, int levelup, String name, List<Object> values) {
		StringBuffer buff = new StringBuffer(" 1=1");
		if (type != -1) {
			buff.append(" and a.type=?");
			values.add(Integer.valueOf(type));
		}
		if (quality == 0 || quality == 1 && type != 3) {
			buff.append(" and a.quality=?");
			values.add(Integer.valueOf(quality));
		} else if (quality == 2 && type != 3) {
			buff.append(" and a.quality>=?");
			values.add(Integer.valueOf(quality));
		}
		if (leveldown >= 0) {
			buff.append(" and a.level>=?");
			values.add(Integer.valueOf(leveldown));
		}
		if (levelup > 0) {
			buff.append(" and a.level<=?");
			values.add(Integer.valueOf(levelup));
		}
		if (name != null && name.length() != 0) {
			buff.append(" and a.name like ?");
			values.add("%" + name + "%");
		}
		return buff.toString();
	}

	/** 组合查询 */
	public AuctionResult getAuctions(int type, int quality, int leveldown, int levelup, String name, int sortfeild, int asc, int pageNum,
			int amount,int playerId) {
		AuctionResult result = new AuctionResult();
		String sort = "";
		String adesc = "";
		switch (sortfeild) {
		case 1:
			sort = "name";
			break;
		case 2:
			sort = "currentprice";
			break;
		case 3:
			sort = "validtime";
			break;
		}
		switch (asc) {
		case 0:
			adesc = "asc";
			break;
		case 1:
			adesc = "desc";
			break;
		}
		List<Object> values = new ArrayList<Object>();
		String parameters = getParameters(type, quality, leveldown, levelup, name, values);
		String hql = "select a from Auction a where"
				+ parameters
				+ " order by " + sort + " " + adesc;
		// 分页查询
		long offset = ((long) pageNum - 1L) * amount;
		if (pageNum <= 0 || amount <= 0 || amount > 100 || offset > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Invalid auction pagination");
		}
		Object[] queryValues = values.toArray();
		List l = limitList(hql, (int) offset, amount, queryValues);
		Auction[] auctions = new Auction[l.size()];
		for (int i = 0; i < auctions.length; i++) {
			auctions[i] = (Auction) (l.get(i));
		}
		Long total = (Long) uniqueResult("select count(*) from Auction a where" + parameters, queryValues);
		//计算总页数
		int pageAmount = (int) (total%amount==0 ? total/amount : (total/amount+1));
		//计算当前页显示条数
		int articleamount = l.size();
		result.setAuctions(auctions);
		result.setPageAmount(pageAmount);
		result.setArticleamount(articleamount);
		result.setTotal(total.intValue());
		return result;
	}

	/** 列出所有拍卖行,按到期时间排序 */
	public List<Auction> getAuctions() {
		List<Auction> auctions = list("from Auction");
		return auctions;
	}
}
