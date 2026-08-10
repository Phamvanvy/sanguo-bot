package pip.gm.fw;

import pip.io.uwap.UWapData;

/**
 * 协议包是否需要收据的回复。
 */
public interface Receiptable {
	public ReceiptListener getReceiptListener();
}
