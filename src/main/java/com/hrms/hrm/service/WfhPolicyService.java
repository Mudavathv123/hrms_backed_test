package com.hrms.hrm.service;

import java.util.UUID;

public interface WfhPolicyService {
    void enableGlobalWFH();
    void enableEmployeeWFH(UUID employeeId);
    void disableAllWFH();
}
