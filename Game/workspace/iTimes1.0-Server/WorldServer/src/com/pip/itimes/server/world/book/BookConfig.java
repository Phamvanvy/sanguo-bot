package com.pip.itimes.server.world.book;

import java.util.ArrayList;

public class BookConfig {
	public static ArrayList<BookNotice> notices = null;
	public static ArrayList<BookAction> actions = null;
	public static ArrayList<BookResource> resources = null;
	public static ArrayList<BookResource> accounts = null;
	public static ArrayList<BookResource> growups = null;
	public static ArrayList<BookAction> gifts = null;
	public static ArrayList<BookAction> citys = null;
	public static ArrayList<BookAction> instances = null;
	
	
	/**
	 * 重置现有的公告 将过时的公告去掉
	 */
	public static void resetNotice(){
		if(notices == null) return;
		synchronized (notices) {
			ArrayList<BookNotice> tmpNotices = new ArrayList<BookNotice>();
			for(BookNotice notice : notices){
				if(notice.isActioning()){
					tmpNotices.add(notice);
				}
			}
			notices.clear();
			notices = tmpNotices;
		}
	}
	/**
	 * 重置现有的活动 将过时的活动去掉
	 */
	public static void resetAction(){
		if(actions == null) return;
		synchronized (actions) {
			ArrayList<BookAction> tmpActions = new ArrayList<BookAction>();
			for(BookAction action : actions){
				if(action.isActioning()){
					tmpActions.add(action);
				}
			}
			actions.clear();
			actions = tmpActions;
		}
	}
	/**
	 * 重置现有的头大 将过时的奖励去掉
	 */
	public static void resetGift(){
		if(gifts == null) return;
		synchronized (gifts) {
			ArrayList<BookAction> tmpGifts = new ArrayList<BookAction>();
			for(BookAction gift : gifts){
				if(gift.isActioning()){
					tmpGifts.add(gift);
				}
			}
			gifts.clear();
			gifts = tmpGifts;
		}
	}
	/**
	 * 重置现有的城市  将过时的城市去掉
	 */
	public static void resetCity(){
		if(citys == null) return;
		synchronized (citys) {
			ArrayList<BookAction> tmpGifts = new ArrayList<BookAction>();
			for(BookAction city : citys){
				if(city.isActioning()){
					tmpGifts.add(city);
				}
			}
			citys.clear();
			citys = tmpGifts;
		}
	}
	/**
	 * 重置现有的副本  将过时的副本去掉
	 */
	public static void resetInstance(){
		if(instances == null) return;
		synchronized (instances) {
			ArrayList<BookAction> tmpGifts = new ArrayList<BookAction>();
			for(BookAction instance : instances){
				if(instance.isActioning()){
					tmpGifts.add(instance);
				}
			}
			instances.clear();
			instances = tmpGifts;
		}
	}
	
	
	public static void reset(){
		resetNotice();
		resetAction();
		resetGift();
		resetGift();
		resetCity();
		resetInstance();
	}
}
