package com.hrms.hrm.error;

public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String invalidCredentials) {
        super(invalidCredentials);
    }
}
