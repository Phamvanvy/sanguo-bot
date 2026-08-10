package peony.game.itemeffect;

import com.pip.sanguo.data.BookConfig;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.service.read.Book;
import peony.service.read.BookUtil;
import peony.service.read.Books;

public class ReadBookEffect implements ItemEffect{
	
	 protected int bookId;
	 
	 public ReadBookEffect(int bookId) {
		    this.bookId = bookId;
	}

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)source;
		if(p!=null){
			if(p.books.books.keySet().contains(bookId)){
				throw new UseItemException("您已经拥有这本书了");
			}
			Book book = new Book();
			BookConfig bc = BookUtil.getBookConfig(bookId);
			book.create(bookId, bc.getTitle(), bc.property, bc.upLimit);
			p.books.addBook(book);
		}
	}
	
	public boolean needRemove() {
		return false;
	}
}
