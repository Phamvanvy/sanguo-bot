package peony.service.account.cmcc;

import com.pip.net.message.AbstractMessage;

public class CmccHistoryMessage extends AbstractMessage {
	
    protected int type;
    protected String startDate,endDate;
    protected int startSeq;
    protected int pageSize;
    protected int timeType;
    protected int queryType;
    protected String cmccUserId;

	public CmccHistoryMessage(int type,String startDate,String endDate,int startSeq,int pageSize,int timeType,int queryType,String cmccUserId) {
		super((short)601);
		this.type = type;
		this.startDate = startDate;
		this.endDate = endDate;
		this.startSeq = startSeq;
		this.pageSize = pageSize;
		this.timeType = timeType;
		this.queryType = queryType;
		this.cmccUserId = cmccUserId;
	}
	
	public int getType(){
		return this.type;
	}
	
	public String getStartDate(){
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public int getStartSeq() {
		return startSeq;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getTimeType() {
		return timeType;
	}

	public int getQueryType() {
		return queryType;
	}

	public String getCmccUserId() {
		return cmccUserId;
	}
    
}
