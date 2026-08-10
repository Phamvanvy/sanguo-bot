package com.pip.itimes.server.world;

public class MateException extends Exception {
    public MateException() {
        super();
    }

    public MateException(String message) {
        super(message);
    }

    public MateException(String message, Throwable cause) {
        super(message, cause);
    }

    public MateException(Throwable cause) {
        super(cause);
    }
}
