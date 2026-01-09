package com.hrms.hrm.error;


public class AttendanceNotAllowedException extends RuntimeException {
    public AttendanceNotAllowedException(String message) {
        super(message);
    }
}
