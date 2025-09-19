package com.commerceguard.common.exception;

import lombok.Getter;

@Getter
public class CommerceGuardException extends RuntimeException {
    private final String code;
    private final String message;
    private final int status;

    public CommerceGuardException(String code, String message, int status) {
        super(message);
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public CommerceGuardException(String code, String message, int status, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
