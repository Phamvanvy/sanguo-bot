package peony.service.sleepycat;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;

/**
 * 一个Sequence类，对应BDB里的一条记录。这个类一次性预约一批ID，用完后再继续申请。服务器关闭时如果没有用完，则未
 * 用完的ID退回。
 * @author lighthu
 */
public class BulkSequence {
	private Database database;
	private String keyName;
	private int nextID;					// 下一个要分配出的ID
	private int nextBulkID;				// 下一次预约将返回的ID
	
	public BulkSequence(Database database, String keyName, int initID) {
		this.database = database;
		this.keyName = keyName;
		this.nextID = initID;
		
		// 如果数据库中已经有记录，则读取新的nextID
		try {
			DatabaseEntry keyEntry = new DatabaseEntry();
			DatabaseEntry dataEntry = new DatabaseEntry();
			StringBinding.stringToEntry(keyName, keyEntry);
			if (database.get(null, keyEntry, dataEntry, null) != OperationStatus.NOTFOUND) {
				nextID = IntegerBinding.entryToInt(dataEntry);
            }
		} catch (Exception e) {
		}
		
		// 预约100万个ID出来，并保存nextBulkID
		nextBulkID = nextID + 200000;
		save();
	}
	
	/**
	 * 退回预先申请的所有ID。
	 */
	public synchronized void clearBuffer() {
		nextBulkID = nextID;
		save();
	}
	
	/**
	 * 保存最新的预约ID号。
	 */
	protected void save() {
		try {
			DatabaseEntry keyEntry = new DatabaseEntry();
			DatabaseEntry dataEntry = new DatabaseEntry();
			StringBinding.stringToEntry(keyName, keyEntry);
			IntegerBinding.intToEntry(nextBulkID, dataEntry);
			database.put(null, keyEntry, dataEntry);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 取得下一个ID。
	 */
	public synchronized int getNext() {
		if (nextID >= nextBulkID) {
			// 预先申请的缓冲已经用完，重新申请
			nextBulkID += 200000;
			save();
		}
		int ret = nextID;
		nextID++;
		return ret;
	}
}
