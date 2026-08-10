package com.pip.wulin.server.io;

import java.io.*;
import java.util.*;

public abstract class UWAPConnection extends UWAPWritter {
    /** to hold all the UWAPDataListeners for deliving incomming data */
    protected Vector listeners = new Vector();

    /** the http request header properties */

    /**
     * To add one UWAPDataListener to the reading thread
     */
    public void addDataListener(UWAPDataListener l) {
        synchronized (listeners) {
            if (!listeners.contains(l)) {
                listeners.addElement(l);
            }
        }
    }

    /**
     * To remove one UWAPDataListener to the reading thread
     */
    public void removeDataListener(UWAPDataListener l) {
        synchronized (listeners) {
            if (listeners.contains(l)) {
                listeners.removeElement(l);
            }
        }
    }

    public void close() {
    }

    public void writeErr(String err, int ser) throws IOException {
    }
}
