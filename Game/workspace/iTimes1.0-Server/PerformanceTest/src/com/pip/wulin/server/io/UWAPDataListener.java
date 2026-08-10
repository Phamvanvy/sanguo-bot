package com.pip.wulin.server.io;

/**
 * The listener for the UWAP data.
 */
public interface UWAPDataListener {
    public static final int SIG_CLOSING = 1;
    public static final int SIG_TIMEOUT = 2;
    public static final int SIG_LOST_RESPONSE = 3;

    /**
     * Callback method will be invoked if the reading thread read some
     * UWAP data.
     * @param serialNum the serial number of the data. The listener may keep
     * the serial number when requesting.
     * @return true if the data is sonsumed by the listener
     */
    public boolean onGotData(UWAPConnection conn, UWAPData data[],
                             int serialNum, int requestId) throws Exception;

    /**
     * Callback for the reading thread of UWAP while meeting some problems such as timeout and closing.
     * The listener may reopen the connection or do something else.
     */
    public void onSignal(UWAPConnection conn, int signal, String msg);
}
