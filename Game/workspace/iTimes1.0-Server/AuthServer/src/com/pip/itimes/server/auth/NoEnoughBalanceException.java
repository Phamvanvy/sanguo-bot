package com.pip.itimes.server.auth;

public class NoEnoughBalanceException extends CmccException {
    public NoEnoughBalanceException(String msg) {
        super(msg);
    }
}
