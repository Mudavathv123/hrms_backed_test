package com.hrms.hrm.error;

public class EmployeeAlreadyExistException extends RuntimeException{

    public EmployeeAlreadyExistException(String msg) {
        super(msg);
    }
}
