package com.pip.itimes.server.world;

public class MasterException extends Exception {

    public MasterException(String message) {
        super(message);
    }

    public MasterException(String message, Throwable cause) {
        super(message, cause);
    }

    public MasterException(Throwable cause) {
        super(cause);
    }
}
