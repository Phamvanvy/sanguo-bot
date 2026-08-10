package peony.service.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.Card.Material;

/**
 * 卡片分组数据模板
 * 
 */
public class CardGroup implements Comparable<CardGroup>{
	/**
	 * 卡片组ID
	 */
	public int groupId;
	/**
	 * 分类名称
	 */
	public String cardGroupName;
	/**
	 * 该系列卡位总数
	 */
	public int totalCount;

	public CardGroup(int groupId, String cardGroupName){
		this.groupId = groupId;
		this.cardGroupName = cardGroupName;
	}

	public List<Card> cards = new ArrayList<Card>();
	/**
	 * 向组里添加卡片
	 * @param card
	 */
	public void addCard(Card card) {
		int start = -1;
		int end = cards.size();
		int mid = end;
		while(end - start > 1){
			mid = (start + end) / 2;
			Card cd = (Card)cards.get(mid);
			if(card.holeId < cd.holeId){
				end = mid;
			} else {
				start = mid;
			}
		}
		cards.add(end, card);
	}

	public int compareTo(CardGroup o) {
		if(this.groupId < o.groupId){
			return -1;
		} else if(this.groupId == o.groupId){
			return 0;
		} else {
			return 1;
		}
	}
}
