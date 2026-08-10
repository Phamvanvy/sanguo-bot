package peony.service.account.cmcc;

import java.util.List;

import com.pip.net.message.AbstractMessage;

public class CmccHistoryOkMessage extends AbstractMessage {

	protected List<CmccHistory> historys;

	public CmccHistoryOkMessage(int serial, List<CmccHistory> historys) {
		super((short)602, serial);
		this.historys = historys;
	}

	public List<CmccHistory> getHistorys(){
		return historys;
	}
}
