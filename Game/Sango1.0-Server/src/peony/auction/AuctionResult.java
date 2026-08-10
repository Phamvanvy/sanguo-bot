package peony.auction;
/**
 * 查询结果信息
 */
public class AuctionResult {
	private Auction[] auctions = null;
	private int pageAmount = 0;//总页数
	private int articleamount = 0;//每页实际显示条数
	private int total;//数据总条数
	public Auction[] getAuctions() {
		return auctions;
	}
	public void setAuctions(Auction[] auctions) {
		this.auctions = auctions;
	}
	public int getPageAmount() {
		return pageAmount;
	}
	public void setPageAmount(int pageAmount) {
		this.pageAmount = pageAmount;
	}
	public int getArticleamount() {
		return articleamount;
	}
	public void setArticleamount(int articleamount) {
		this.articleamount = articleamount;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
}
