package pip.gm.fw;

import pip.io.uwap.UWapData;
/**
 * 是否支持广播
 */
public interface Broadcastable {
	/**
	 * 构造广播包
	 */
	public UWapData genBroadCastInfo(String message, ReceiptListener receiptListener);
}
