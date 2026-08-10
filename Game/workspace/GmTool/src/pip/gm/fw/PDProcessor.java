package pip.gm.fw;

import pip.io.uwap.UWapData;

/**
 * 数据包处理器.
 */
public interface PDProcessor {
	/** 接收并处理一个数据包 */
    public boolean process(pip.gm.fw.AbstractClient master, UWapData d);
}
