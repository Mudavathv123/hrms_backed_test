package com.hrms.hrm.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.WfhPolicy;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.WfhPolicyRepository;
import com.hrms.hrm.service.WfhPolicyService;

import lombok.RequiredArgsConstructor;


@Service
@Transactional
@RequiredArgsConstructor
public class WfhPolicyServiceImpl implements WfhPolicyService {

    private final WfhPolicyRepository wfhPolicyRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public void enableGlobalWFH() {
        wfhPolicyRepository.disableAll(); 
        WfhPolicy policy = new WfhPolicy();
        policy.setGlobal(true);
        policy.setEnabled(true);
        wfhPolicyRepository.save(policy);
    }

    @Override
    public void enableEmployeeWFH(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        WfhPolicy policy = new WfhPolicy();
        policy.setEmployee(employee);
        policy.setGlobal(false);
        policy.setEnabled(true);
        wfhPolicyRepository.save(policy);
    }

    @Override
    public void disableAllWFH() {
        wfhPolicyRepository.disableAll();
    }
}
