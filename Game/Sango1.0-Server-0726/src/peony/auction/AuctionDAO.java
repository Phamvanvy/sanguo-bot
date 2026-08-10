package peony.auction;

import java.util.List;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class AuctionDAO extends GenericHibernateDAO<Auction, Integer> {

	/** 组拼查询字符串 */
	private String getParameters(int type, int quality, int leveldown, int levelup, String name) {
		StringBuffer buff = new StringBuffer();
		if (type != -1) {
			buff.append(" a.type=" + type);
		}
		if (quality == 0 || quality == 1 && type != 3) {
				buff.append(" and ");
				buff.append(" a.quality=" + quality);
		}else if(quality == 2 && type != 3){
			buff.append(" and ");
			buff.append(" a.quality>=" + quality);
		}
		if (leveldown>=0) {
			buff.append(" and ");
			/*int beginLevel = (level >> 16 & 0xFFFF);
			int endLevel = (level & 0xFFFF);
			if (endLevel == 0) {
				endLevel = 100;
			}
			buff.append(" a.level between " + beginLevel + " and " + endLevel);
			*/
			buff.append(" a.level>= " + leveldown);
		}
		if(levelup>0){
			buff.append(" and a.level<="+levelup);
		}
		if (name.length() != 0) {
			buff.append(" and ");
			buff.append(" a.name like '%" + name + "%'");
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
			asc = 1;
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
		String hql = "select a from Auction a where"
				+ getParameters(type, quality, leveldown, levelup, name)
				+ " order by " + sort + " " + adesc;
		// 分页查询
		List l = limitList(hql, (pageNum - 1) * amount, amount);
		Auction[] auctions = new Auction[l.size()];
		for (int i = 0; i < auctions.length; i++) {
			auctions[i] = (Auction) (l.get(i));
		}
		Long total = (Long) uniqueResult("select count(*) from Auction a where"+ getParameters(type, quality, leveldown, levelup, name));
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
