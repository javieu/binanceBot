package com.javieu.crypto.binance.exceptions;

public class GeneralException extends Exception {

    private static final long serialVersionUID = 261011510446536616L;

    public GeneralException(String message) {
        super(message);
    }

    public GeneralException(Throwable throwable) {
        super(throwable);
    }
}
