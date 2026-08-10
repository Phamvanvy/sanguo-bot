package com.pip.server.auth;

import com.pip.server.auth.cmcc.CmccException;

/**
 * ”‡∂Ó≤ª◊„“Ï≥£°£
 */
public class NoEnoughBalanceException extends CmccException {
    public NoEnoughBalanceException(String msg) {
        super(msg);
    }
}
