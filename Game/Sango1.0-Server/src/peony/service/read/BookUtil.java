package peony.service.read;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ch.javasoft.util.intcoll.IntHashMap;
import com.pip.sanguo.data.BookConfig;
import com.pip.sanguo.data.BookChapter;
import com.pip.sanguo.data.ProjectData;

import peony.game.Server;
import peony.game.Unit;

public class BookUtil {
    
	public static IntHashMap<BookConfig> configs = new IntHashMap<BookConfig>();
	public static List<Integer> initBookIds = new ArrayList<Integer>();
	
	@SuppressWarnings("unchecked")
	public static void initBook() {
		List gas = Server.server.getServiceRegistry().getDataService().data
				.getDataListByType(BookConfig.class);
		Iterator ite = gas.iterator();
		while (ite.hasNext()) {
			BookConfig book = (BookConfig) ite.next();
			configs.put(book.id, book);
			if(book.auto == 1){
				initBookIds.add(book.id);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void initBook2(ProjectData data) {
		List gas = data.getDataListByType(BookConfig.class);
		Iterator ite = gas.iterator();
		while (ite.hasNext()) {
			BookConfig book = (BookConfig) ite.next();
			configs.put(book.id, book);
			if(book.auto == 1){
				initBookIds.add(book.id);
			}
		}
	}
	
	public static void clearBooksData(){
		configs.clear();
		initBookIds.clear();
	}

	
	public static BookChapter getBookChapter(int bookLevel,BookConfig config){
	    if(bookLevel<=config.upLimit && config.attrAdvances.containsKey(bookLevel)){
		    return config.attrAdvances.get(bookLevel);
	    }
		return null;
	}
	
	public synchronized static BookConfig getBookConfig(int bookId){
		return configs.get(bookId);
	}
	
	public static void createInitBook(Unit owner){
		for(int i=0;i<initBookIds.size();i++){
			if(owner.books.books.size()<=0){
				owner.send = true;
			}
			if(owner.books.books.size()<=0 || !owner.books.books.containsKey(initBookIds.get(i))){
				Book book = new Book();
				int bookId = initBookIds.get(i);
				BookConfig bc = getBookConfig(bookId);
				if(bc != null){
					book.create(bc.id, bc.getTitle(), bc.property, bc.upLimit);
					owner.books.addBook(book);
				}
			}
		}
	}
	
}
	
	
