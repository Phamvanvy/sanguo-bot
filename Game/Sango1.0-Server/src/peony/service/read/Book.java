package peony.service.read;

import com.pip.sanguo.data.BookConfig;
import com.pip.sanguo.data.BookChapter;
import peony.game.PropertyCalculator;
import peony.game.PropertyEnhancer;

/**
 * 书籍
 * @author mfou
 */

public class Book implements PropertyEnhancer{
	
	 /**
     * 力量属性
     */
    public static final int TYPE_STREN = 0;
    
    /**
     * 敏捷
     */
    public static final int TYPE_AGI = 1;
    
    /**
     * 体力
     */
    public static final int TYPE_POWER = 2;
    
    /**
     * 智力
     */
    public static final int TYPE_INTE = 3;
    
  
    //三种状态
    public final static int STATE_UNREAD = 0;
    public final static int STATE_READ = 1;
    public final static int STATE_PAUSE = 2;
    
    public final static String[] propertyType = {"力量","敏捷","体力","智力"};
    
    public int id;
	public String name;
	public int property;
	public int upLimit;
	public String dec="";
	public int chapter = 0;
	public int value = 0;
	public long startReadTime = 0L;
	public long alreadyRead = 0L;
	public byte onRead = STATE_UNREAD;//(0未阅读，1，在阅读，2，暂停阅读)
	public int payTimes = 1;//缩短时间次数
	
	protected byte[] clientBytes;
	
	public Book(){}
	
	public void create(int id,String name,int property,int upLimit) {
		this.id = id;
	    this.name = name;
	    this.property = property;
	    this.upLimit = upLimit;
		createClientBytes();
	}
	
	public int getLevel(){
		return chapter;
	}
	
	
	public int getId(){
		return id;
	}
	
	
	public void createClientBytes(){
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		DataOutputStream dos = new DataOutputStream(baos);
//		try{
//			dos.writeInt(id);
//			String bookName = BookUtil.getBookName(this);
//			dos.writeUTF(bookName);
//			dos.writeInt(level);
//			dos.writeByte(onRead);
//			int minute = 0;
//			if(onRead == STATE_READ && level<BookUtil.getUpLimit(b)){
//				minute = (int)((System.currentTimeMillis() - startReadTime + alreadyRead)/(60 * 1000L));
//			}
//			dos.writeInt(minute);
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
//		clientBytes = baos.toByteArray();
	}
	
	public byte[] toClientBytes(){
		return clientBytes;
	}
	
	public String getPropertyName(BookConfig bc){
		BookChapter bookChapter = BookUtil.getBookChapter(chapter+1, bc);
		if(bookChapter == null){
			return "——";
		}else{
			if(bc.auto==1){
				if(bc.property == TYPE_STREN){
					 return (chapter+1)+"力量";
				}else if(bc.property == TYPE_AGI){
					 return (chapter+1)+"敏捷";
				}else if(bc.property == TYPE_POWER){
					return (chapter+1)+"体力";
				}else if(bc.property == TYPE_INTE){
					return (chapter+1)+"智力";
				}
			}else{
				if(bc.property == TYPE_STREN){
					 return bc.value/50+"力量";
				}else if(bc.property == TYPE_AGI){
					 return bc.value/50+"敏捷";
				}else if(bc.property == TYPE_POWER){
					return bc.value/50+"体力";
				}else if(bc.property == TYPE_INTE){
					return bc.value/50+"智力";
				}
			}
		}
		return "";
	}
	
	/**
	 * 计算增加属性,每级提升点数=书籍等级，逐级累加
	 * @param chapter
	 * @return
	 */
	public int getPropertyAdd(int chapter,BookConfig bc,BookChapter bookChapter){
		int v=0;
		if(chapter>0){
			if(bc.auto==1){
		        v = chapter * (chapter - 1) / 2 + chapter;
			}else{
				v = chapter;
			}
		}
		return v;
	}

	public void enhance(PropertyCalculator pc) {
		BookConfig bc = BookUtil.getBookConfig(id);
		if(chapter>0){
			BookChapter bookChapter = BookUtil.getBookChapter(chapter, bc);
			if(bookChapter != null){
				property = bc.property;
				if(property == TYPE_STREN){
					 pc.strength+=getPropertyAdd(chapter,bc,bookChapter);
				}else if(property == TYPE_AGI){
					pc.agility+=getPropertyAdd(chapter,bc,bookChapter);
				}else if(property == TYPE_POWER){
					pc.stamina+=getPropertyAdd(chapter,bc,bookChapter);
				}else if(property == TYPE_INTE){
					pc.intellect+=getPropertyAdd(chapter,bc,bookChapter);
				}
			}
		}
	}
}
    
