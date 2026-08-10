package peony.game;

public interface GameItemObject{
	
	public String getDesc();
	public GameItemObject clone();
	public Class<? extends Marshaller> marshallerClass();
	public Class<? extends Serializer> serializerClass();
	/**
	 * 做log记录的时候需要这个信息,本来想使用toString(),不过考虑到toString很容易让实现者忽略,所以添加了这么一个接口
	 * @return
	 */
	public String logString();
	/**
	 * 把对象添加到一个日志字符串中。
	 */
	public void dump(StringBuilder out);
}
