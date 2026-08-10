package peony.auction;

import java.util.Comparator;

public class AuctionComparator implements Comparator<Auction>{

	public int compare(Auction o1, Auction o2) {
		Auction auction1 = o1;
		Auction auction2 = o2;
		if(auction1.getValidTime().getTime()<auction2.getValidTime().getTime()){
			return 1;
		}else if(auction1.getValidTime().getTime()>auction2.getValidTime().getTime()){
			return -1;
		}
		return 0;
	}

}
