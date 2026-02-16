package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.EodRequestDto;
import com.hrms.hrm.dto.EodResponseDto;
import com.hrms.hrm.dto.FileAttachmentDto;
import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.EodReport;
import com.hrms.hrm.model.FileAttachment;
import com.hrms.hrm.model.User;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.EodReportRepository;
import com.hrms.hrm.repository.FileAttachmentRepository;
import com.hrms.hrm.repository.UserRepository;
import com.hrms.hrm.service.EodReportService;
import com.hrms.hrm.service.FileStorageService;
import com.hrms.hrm.service.NotificationService;
import com.hrms.hrm.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EodReportServiceImpl implements EodReportService {

    private final EodReportRepository eodReportRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;
    private final FileAttachmentRepository fileAttachmentRepository;

    @Override
    public EodResponseDto createEod(EodRequestDto request) {

        Employee emp = employeeRepository.findById(UUID.fromString(request.getEmployeeId()))
                .orElseThrow(
                        () -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        String employeeFullName = emp.getFirstName() + " " + emp.getLastName();
        EodReport report = EodReport.builder()
                .employeeName(employeeFullName)
                .employeeCode(request.getEmployeeCode())
                .employeeId(request.getEmployeeId())
                .date(request.getDate())
                .workSummary(request.getWorkSummary())
                .blockers(request.getBlockers())
                .status(EodReport.Status.valueOf(request.getStatus().toUpperCase()))
                .build();

        EodReport saved = eodReportRepository.save(report);
        log.info("EOD report created - Employee: {}, Date: {}", employeeFullName, request.getDate());

        List<User> admins = userRepository.findByRole(User.Role.ROLE_ADMIN);
        admins.forEach(admin -> {
            try {
                if (admin.getEmployee() == null) {
                    log.warn("Admin {} has no employee mapping, skipping EOD notification", admin.getId());
                    return;
                }
                notificationService.sendNotification(NotificationRequestDto.builder()
                        .type("EOD")
                        .title("New EOD Report Submitted")
                        .date(LocalDate.now())
                        .message("EOD submitted by " + employeeFullName + " for " + request.getDate())
                        .senderId(emp.getId())
                        .receiverId(admin.getEmployee().getId())
                        .targetRole("ROLE_ADMIN")
                        .build());
                log.debug("EOD notification sent to admin: {}", admin.getId());
            } catch (Exception e) {
                log.error("Failed to send EOD notification to admin {}: {}", admin.getId(), e.getMessage(), e);
            }
        });

        return DtoMapper.toDto(saved);
    }

    @Override
    public EodResponseDto updateEod(UUID id, EodRequestDto request) {

        EodReport report = eodReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EOD not found with id: " + id));

        if (request.getEmployeeCode() != null)
            report.setEmployeeCode(request.getEmployeeCode());
        if (request.getEmployeeId() != null)
            report.setEmployeeId(request.getEmployeeId());
        if (request.getDate() != null)
            report.setDate(request.getDate());
        if (request.getWorkSummary() != null)
            report.setWorkSummary(request.getWorkSummary());
        if (request.getBlockers() != null)
            report.setBlockers(request.getBlockers());
        if (request.getStatus() != null)
            report.setStatus(EodReport.Status.valueOf(request.getStatus().toUpperCase()));

        EodReport updated = eodReportRepository.save(report);
        log.info("EOD report updated - Employee: {}, Date: {}", report.getEmployeeName(), report.getDate());

        List<User> admins = userRepository.findByRole(User.Role.ROLE_ADMIN);
        admins.forEach(admin -> {
            try {
                if (admin.getEmployee() == null) {
                    log.warn("Admin {} has no employee mapping, skipping EOD update notification", admin.getId());
                    return;
                }
                notificationService.sendNotification(NotificationRequestDto.builder()
                        .type("EOD")
                        .title("EOD Report Updated")
                        .date(LocalDate.now())
                        .message("EOD updated by " + report.getEmployeeName() + " for " + report.getDate())
                        .senderId(UUID.fromString(report.getEmployeeId()))
                        .receiverId(admin.getEmployee().getId())
                        .targetRole("ROLE_ADMIN")
                        .build());
                log.debug("EOD update notification sent to admin: {}", admin.getId());
            } catch (Exception e) {
                log.error("Failed to send EOD update notification to admin {}: {}", admin.getId(), e.getMessage(), e);
            }
        });

        return DtoMapper.toDto(updated);
    }

    @Override
    public List<EodResponseDto> getEmployeeEods(String employeeId) {

        return eodReportRepository.findByEmployeeId(employeeId)
                .stream()
                .map(report -> {

                    EodResponseDto dto = DtoMapper.toDto(report);

                    List<FileAttachmentDto> attachmentDtos = fileAttachmentRepository
                            .findByModuleAndReferenceId("EOD", report.getId())
                            .stream()
                            .map(file -> {

                                // Use fileUrl (actual stored filename) here, NOT fileName
                                String downloadUrl = fileStorageService.generatePresinedUrl(file.getFileUrl());

                                return DtoMapper.toDto(file, downloadUrl);
                            })
                            .toList();

                    dto.setAttachments(attachmentDtos);

                    return dto;
                })
                .toList();
    }

    @Override
    public List<EodResponseDto> getAllEods() {

        return eodReportRepository.findAll()
                .stream()
                .map(report -> {

                    EodResponseDto dto = DtoMapper.toDto(report);

                    List<FileAttachmentDto> attachmentDtos = fileAttachmentRepository
                            .findByModuleAndReferenceId("EOD", report.getId())
                            .stream()
                            .map(file -> {

                                // Use fileUrl (actual stored filename) here as well
                                String downloadUrl = fileStorageService.generatePresinedUrl(file.getFileUrl());

                                return DtoMapper.toDto(file, downloadUrl);
                            })
                            .toList();

                    dto.setAttachments(attachmentDtos);

                    return dto;
                })
                .toList();
    }

    @Override
    public void deleteEod(UUID id) {
        eodReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EOD not found with id: " + id));

        eodReportRepository.deleteById(id);
        log.info("EOD report deleted - ID: {}", id);
    }
}
