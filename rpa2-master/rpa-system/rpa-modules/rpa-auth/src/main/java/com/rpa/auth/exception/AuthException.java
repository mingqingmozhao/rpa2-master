package com.rpa.auth.exception;

public class AuthException extends RuntimeException {
    private final int code;

    public AuthException(String message) {
        super(message);
        this.code = 401;
    }

    public AuthException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
