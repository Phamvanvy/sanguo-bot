package pip.gm.fw;

import pip.io.uwap.UWapData;
/** 
 * 收据接听者。一般是发出需要收据的请求后等待处理收据的功能。
 */
public interface ReceiptListener {
	/**
	 * 处理请求的收据信息。
	 * @param original 原发出的请求。
	 * @param receipt 收据协议。
	 */
	public void onReceipt(UWapData original, UWapData receipt);
}
